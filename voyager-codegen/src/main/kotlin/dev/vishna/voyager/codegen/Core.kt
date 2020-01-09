package dev.vishna.voyager.codegen


import dev.vishna.dartfmt.dartfmt
import dev.vishna.emojilog.android.warn
import dev.vishna.voyager.codegen.model.RouterPath
import dev.vishna.emojilog.std.*
import dev.vishna.mvel.interpolate
import dev.vishna.stringcode.asResource
import dev.vishna.stringcode.camelize
import dev.vishna.stringcode.saveAs
import dev.vishna.voyager.codegen.model.ScenarioClassName
import kotlinx.coroutines.*
import java.io.File
import java.lang.IllegalStateException
import java.util.*

internal val log by lazy { defaultLogger() }

/**
 * Template for the router paths
 */
const val dartVoyagerPathsClass: ResourcePath = "/dart_voyager_paths_class.mvel"

/**
 * Template for the automated test classes
 */
const val dartVoyagerTests: ResourcePath = "/dart_voyager_tests.mvel"

/**
 * Template for the automated scenario class
 */
const val dartVoyagerTestScenarioClass: ResourcePath = "/dart_voyager_tests_scenario_class.mvel"

/**
 * Template for the automated scenario execution block
 */
const val dartVoyagerTestScenarioExecutionBlock: ResourcePath = "/dart_voyager_tests_scenario_execution_block.mvel"

/**
 * Template for the "strong-typed" voyager
 */
const val dartVoyagerDataClass: ResourcePath = "/dart_voyager_data_class.mvel"

/**
 * Template for the plugin stub
 */
const val dartVoyagerPluginStub: ResourcePath = "/dart_voyager_plugin_stub.mvel"

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
    testTarget: String?,
    definitions: Map<String, Any>?,
    schema: Map<String, Map<String, *>>?,
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

    val jobs = mutableListOf<Job>()
    jobs += async { generateVoyagerPaths(name, routerPaths, target, dryRun, setExitIfChanged, validationResult, `package`) }
    if (!testTarget.isNullOrBlank()) {
        jobs += async { generateVoyagerTests(name, routerPaths, testTarget, dryRun, setExitIfChanged) }
    }
    jobs.forEach { it.join() }
}

suspend fun generateVoyagerPaths(
    name: String,
    routerPaths: List<RouterPath>,
    target: String,
    dryRun: Boolean,
    setExitIfChanged: Boolean,
    validationResult: ValidationResult?,
    `package`: String
) {
    toPathsDart(name, routerPaths, validationResult, `package`)
        ?.saveToTarget(target, dryRun, setExitIfChanged)
}

suspend fun generateVoyagerTests(name: String, routerPaths: List<RouterPath>, target: String, dryRun: Boolean, setExitIfChanged: Boolean) {
    toTestScenariosDart(name, routerPaths)
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
                    "stubs" to stubs
                )
            )
    } catch (t: Throwable) {
        log.boom..t
        throw t
    }

    // calculate the template
    return generatedDart?.dartfmt()
}

internal suspend fun toTestScenariosDart(name: String, routerPaths: List<RouterPath>): String? {
    val baseScenarioClassName = "${name}TestScenario"

    val resolver = DartResolver()

    val scenarioClasses = routerPaths.map {
        ScenarioClassName(
            baseScenarioClassName = baseScenarioClassName,
            scenarioClassName = "${name}Test${it.type.camelize()}Scenario",
            resolver = resolver,
            routerPath = it
        )
    }

    // calculate the template
    return dartVoyagerTests
        .asResource()
        .interpolate(
            mapOf(
                "name" to name.toLowerCase(Locale.US),
                "baseScenarioClassName" to baseScenarioClassName,
                "resolver" to resolver,
                "scenarioClasses" to scenarioClasses
            )
        )?.dartfmt()
}