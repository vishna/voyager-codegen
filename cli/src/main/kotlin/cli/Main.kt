@file:JvmName("Main")
package cli

import dev.vishna.patrol.*
import dev.vishna.voyager.codegen.*

fun main(args: CommandArgs) = args.patrol {
    name {
        "voyager-codegen"
    }

    help {
        "Code generation utility for the Voyager project."
    }

    onInspection { watchPoint, dryRun ->
        generateVoyagerPaths(
            name = watchPoint.name,
            source = watchPoint.source,
            target = requireNotNull(watchPoint["target"] as String?) { "target value not provided in $watchPoint" },
            dryRun = dryRun)
    }

    bootstrap(::bootstrapVoyagerPatrolConfig)
}