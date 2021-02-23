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
            val widgetPlugin = if (watchPoint["widgetPlugin"] is Boolean) {
                val shouldHaveWidgetPlugin = watchPoint["widgetPlugin"] as Boolean
                if (shouldHaveWidgetPlugin) {
                    emptyMap<String, Map<String, *>>()
                } else {
                    null
                }
            } else {
                watchPoint["widgetPlugin"] as Map<String, Map<String, *>>?
            }
            val pagePlugin = if (watchPoint["pagePlugin"] is Boolean) {
                val shouldHaveWidgetPlugin = watchPoint["pagePlugin"] as Boolean
                if (shouldHaveWidgetPlugin) {
                    emptyMap<String, Map<String, *>>()
                } else {
                    null
                }
            } else {
                watchPoint["pagePlugin"] as Map<String, Map<String, *>>?
            }
            val nullsafety = if (watchPoint["nullsafety"] is Boolean) {
                watchPoint["nullsafety"] as Boolean
            } else {
                true
            }
            generateCode(
                name = watchPoint.name,
                source = watchPoint.source,
                target = requireNotNull(watchPoint["target"] as String?) { "target value not provided in $watchPoint" },
                schema = schema ?: (watchPoint["schema"] as Map<String, Map<String, *>>?),
                definitions = definitions ?: watchPoint["definitions"] as Map<String, Any>?,
                widgetPlugin = widgetPlugin,
                pagePlugin = pagePlugin,
                dryRun = dryRun,
                runOnce = runOnce,
                setExitIfChanged = scope.setExitIfChanged,
                `package` = watchPoint["package"] as String? ?: "",
                nullsafety = nullsafety
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