@file:JvmName("Main")
package cli

import dev.vishna.emojilog.safe.safely
import dev.vishna.emojilog.std.boom
import dev.vishna.patrol.*
import dev.vishna.voyager.codegen.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap

fun main(args: CommandArgs) = args.patrol {

    val inspectionJobs = ConcurrentHashMap<String, Job>()

    name {
        "voyager-codegen"
    }

    help {
        "Code generation utility for the Voyager project."
    }

    onInspection { scope, watchPoint, dryRun, runOnce ->

        if (debounced(watchPoint.name)) {
            return@onInspection
        }

        var schema : Map<String, Map<String, *>>? = null
        var definitions : Map<String, Any>? = null
        val sourceSchema = watchPoint["sourceSchema"] as? String
        if (!sourceSchema.isNullOrBlank()) {
            scope.log.boom.safely {
                val schemaYaml = sourceSchema.asFile().readText().asYaml()
                schema = schemaYaml["schema"] as Map<String, Map<String, *>>?
                definitions = schemaYaml["definitions"] as Map<String, Any>?
            }
        }

        scope.launch(context = Dispatchers.IO) {
            generateCode(
                name = watchPoint.name,
                source = watchPoint.source,
                target = requireNotNull(watchPoint["target"] as String?) { "target value not provided in $watchPoint" },
                testTarget = watchPoint["testTarget"] as String?,
                schema = schema ?: (watchPoint["schema"] as Map<String, Map<String, *>>?),
                definitions = definitions ?: watchPoint["definitions"] as Map<String, Any>?,
                dryRun = dryRun,
                runOnce = runOnce,
                setExitIfChanged = scope.setExitIfChanged,
                `package` = watchPoint["package"] as String? ?: ""
            )
        }.apply {
            inspectionJobs[watchPoint.name]?.cancel()
            inspectionJobs[watchPoint.name] = this
            if (runOnce) {
                runBlocking {
                    join()
                }
            }
        }
    }

    bootstrap(::bootstrapVoyagerPatrolConfig)
}