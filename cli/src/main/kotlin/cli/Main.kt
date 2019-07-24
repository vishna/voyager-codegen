@file:JvmName("Main")
package cli

import dev.vishna.patrol.*
import dev.vishna.voyager.codegen.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

fun main(args: CommandArgs) = args.patrol {

    val inspectionJobs = ConcurrentHashMap<String, Job>()

    name {
        "voyager-codegen"
    }

    help {
        "Code generation utility for the Voyager project."
    }

    onInspection { scope, watchPoint, dryRun ->
        scope.launch {
            generateVoyagerPaths(
                name = watchPoint.name,
                source = watchPoint.source,
                target = requireNotNull(watchPoint["target"] as String?) { "target value not provided in $watchPoint" },
                dryRun = dryRun
            )
        }.apply {
            inspectionJobs[watchPoint.name]?.cancel()
            inspectionJobs[watchPoint.name] = this
        }
    }

    bootstrap(::bootstrapVoyagerPatrolConfig)
}