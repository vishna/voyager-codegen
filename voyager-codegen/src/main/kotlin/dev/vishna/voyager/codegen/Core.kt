package dev.vishna.voyager.codegen


import dev.vishna.dartfmt.dartfmt
import dev.vishna.emojilog.android.warn
import dev.vishna.voyager.codegen.model.RouterPath
import dev.vishna.emojilog.std.*
import dev.vishna.mvel.interpolate
import dev.vishna.stringcode.asResource
import dev.vishna.stringcode.saveAs
import kotlinx.coroutines.*
import java.io.File
import java.lang.IllegalStateException

internal val log by lazy { defaultLogger() }

/**
 * Template for the router paths
 */
const val dartVoyagerPathsClass: ResourcePath = "/dart_voyager_paths_class.mvel"

/**
 * Template for the "strong-typed" voyager
 */
const val dartVoyagerDataClass: ResourcePath = "/dart_voyager_data_class.mvel"

/**
 * Template for the plugin stub
 */
const val dartVoyagerPluginStub: ResourcePath = "/dart_voyager_plugin_stub.mvel"

/**
 * Template for the widget mappings
 */
const val dartVoyagerWidgetMappings: ResourcePath = "/dart_voyager_widget_mappings.mvel"

/**
 * Initial template this tool consumes
 */
const val voyagerCodegen: ResourcePath = "/voyager-codegen.yaml"

fun bootstrapVoyagerPatrolConfig(patrolFile: File) = if (File(pwd, "pubspec.yaml").exists()) {
    log.alert.."${patrolFile.name} not found, creating one for you..."
    voyagerCodegen.asResource().saveAs(patrolFile.absolutePath)
    log.save.."${patrolFile.name} created, please edit it"
    true
} else {
    false
}

suspend fun generateCode(
    name: String,
    source: String,
    target: String,
    definitions: Map<String, Any>?,
    schema: Map<String, Map<String, *>>?,
    widgetPlugin: Map<String, Map<String, *>>?,
    dryRun: Boolean,
    runOnce: Boolean,
    setExitIfChanged: Boolean,
    `package`: String
) = supervisorScope {

    if (source.isBlank()) {
        throw IllegalStateException("No source value provided for $name")
    }

    val voyagerFile = source.asFile()

    if (!voyagerFile.exists()) {
        throw IllegalStateException("Provided source file for $name doesn't exist")
    }

    val targetFileName = target.split("/").last()

    if (!targetFileName.endsWith(".voyager.dart")) {
        throw IllegalStateException("Target file name must end with .voyager.dart extension")
    }

    val part = targetFileName.replace(".voyager.dart", ".dart")

    // load voyager yaml either from standalone file or source code
    val voyagerYaml = when (voyagerFile.extension.trim()) {
        "dart" -> voyagerFile
            .readText()
            .split("'''")[1]
        "yaml", "yml" -> voyagerFile.readText()
        else -> throw IllegalStateException("Unsupported file extension ${voyagerFile.extension}")
    }.asYaml()

    val validationResult = if (schema != null) {
        val validationOutput = validateVoyagerPaths(voyagerYaml, schema, definitions)
        validationOutput.errors.forEach { error ->
            log.warn..error
        }

        if (runOnce && validationOutput.errors.isNotEmpty()) {
            log.boom.."${validationOutput.errors.size} validation error(s) found. Exiting."
            System.exit(1)
            return@supervisorScope
        }

        if (validationOutput.errors.isEmpty()) {
            log.success.."Schema validated properly"
        }
        validationOutput
    } else {
        null
    }

    val routerPaths = voyagerYaml.asRouterPaths().filter { it.`package` == `package` }
    val widgetMapping = toWidgetMappings(routerPaths, widgetPlugin)

    val jobs = mutableListOf<Job>()
    jobs += async { generateVoyagerPaths(name, routerPaths, widgetMapping, target, part, dryRun, setExitIfChanged, validationResult, `package`) }
    jobs.forEach { it.join() }
}

suspend fun generateVoyagerPaths(
    name: String,
    routerPaths: List<RouterPath>,
    widgetMappings: List<WidgetMapping>,
    target: String,
    part: String,
    dryRun: Boolean,
    setExitIfChanged: Boolean,
    validationResult: ValidationResult?,
    `package`: String
) {
    toPathsDart(name, part, widgetMappings, routerPaths, validationResult, `package`)
        ?.saveToTarget(target, dryRun, setExitIfChanged)
}

private fun String.saveToTarget(target: String, dryRun: Boolean, setExitIfChanged: Boolean) {
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
            if (setExitIfChanged) {
                log.boom.."${targetFile.absolutePath} did change and shouldn't have."
                System.exit(1)
                return
            }
            saveAs(target)
            log.save..target
        } else {
            log.skip..target
        }
    }
}

fun Map<String, Map<String, *>>.asRouterPaths(): List<RouterPath> = keys
    .map {
        RouterPath(
            path = it,
            type = this[it]?.get("type")?.toString() ?: "",
            `package` = this[it]?.get("package")?.toString() ?: "",
            config = this[it]
        )
    }.filter { it.path.isNotBlank() }

internal suspend fun toPathsDart(
        name: String,
        part: String,
        widgetMappings: List<WidgetMapping>,
        routerPaths: List<RouterPath>,
        validationResult: ValidationResult? = null,
        `package`: String
): String? {
    var data : VoyagerDataClassEmitter? = null
    val imports = mutableListOf<String>()
    val outputsCount = validationResult?.validators?.mapNotNull { it.output }?.size ?: 0
    var stubs = emptyList<PluginStubClassEmitter>()
    if (outputsCount > 0 && validationResult != null && `package`.isNullOrBlank()) {
        imports += validationResult.validators.mapNotNull { it.import }
        imports += "package:voyager/voyager.dart"
        data = VoyagerDataClassEmitter(name = name, validationResult = validationResult)
        stubs = validationResult.validators.filter { it.pluginStub }.map { PluginStubClassEmitter(it) }
    }

    val generatedDart = try {
        dartVoyagerPathsClass
            .asResource()
            .interpolate(
                mapOf(
                    "resolver" to DartResolver(),
                    "name" to name,
                    "paths" to routerPaths,
                    "imports" to imports.distinctBy { it }.sortedBy { it },
                    "data" to data,
                    "stubs" to stubs,
                    "part" to part,
                    "widgetMappings" to WidgetPluginEmitter(name, widgetMappings)
                )
            )
    } catch (t: Throwable) {
        log.boom..t
        throw t
    }

    // calculate the template
    return generatedDart?.dartfmt()
}