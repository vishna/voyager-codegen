package dev.vishna.voyager.codegen


import com.eyeem.routerconstants.DartResolver
import com.eyeem.routerconstants.RouterPath
import dev.vishna.emojilog.std.*
import dev.vishna.mvel.interpolate
import dev.vishna.stringcode.asResource
import dev.vishna.stringcode.saveAs
import java.io.File
import java.lang.IllegalStateException

private val log by lazy { defaultLogger() }

/**
 * Template for the router paths
 */
const val dartVoyagerPathsClass : ResourcePath = "/dart_voyager_paths_class.mvel"

/**
 * Initial template this tool consumes
 */
const val voyagerCodegen : ResourcePath = "/voyager-codegen.yaml"

fun bootstrapVoyagerPatrolConfig(patrolFile: File) = if (File(pwd, "pubspec.yaml").exists()) {
        log.alert.."${patrolFile.name} not found, creating one for you..."
        voyagerCodegen.asResource().saveAs(patrolFile.absolutePath)
        log.save.."${patrolFile.name} created, please edit it"
        true
    } else {
        false
    }

fun generateVoyagerPaths(name: String, source: String, target: String, dryRun: Boolean) {

    if (source.isBlank()) {
        throw IllegalStateException("No source value provided for $name")
    }

    val voyagerFile = source.asFile()

    if (!voyagerFile.exists()) {
        throw IllegalStateException("Provided source file for $name doesn't exist")
    }

    // load voyager yaml either from standalone file or source code
    val voyagerYaml = when (voyagerFile.extension.trim()) {
        "dart" -> voyagerFile
            .readText()
            .split("'''")[1]
        "yaml", "yml" -> voyagerFile.readText()
        else -> throw IllegalStateException("Unsupported file extension ${voyagerFile.extension}")
    }.asYaml()

    // collect all the paths
    val paths = voyagerYaml
        .keys
        .map {
            RouterPath(
                path = it,
                type = voyagerYaml[it]?.get("type")?.toString() ?: ""
            )
        }.filter { it.path.isNotBlank() }

    // calculate template
    dartVoyagerPathsClass
        .asResource()
        .interpolate(
            mapOf(
                "resolver" to DartResolver(),
                "name" to name,
                "paths" to paths
            )
        )?.apply {
            if (dryRun) {
                log.tool..target
                "------------------------------".println
                println
                "------------------------------".println
            } else {
                // save to the target location
                val targetFile = target.asFile()
                val oldHashCode = if (!targetFile.exists()) {
                    File(targetFile.parent).mkdirs()
                    0
                } else {
                    targetFile.readText().hashCode()
                }

                if (oldHashCode != hashCode()) {
                    saveAs(target)
                    log.save..target
                } else {
                    log.skip..target
                }
            }
        }
}