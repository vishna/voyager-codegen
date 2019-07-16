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
        generateVoyagerPaths(watchPoint.name, watchPoint.source, watchPoint.target, dryRun)
    }

    bootstrap(::bootstrapVoyagerPatrolConfig)
}