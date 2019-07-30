package dev.vishna.voyager.codegen


import com.eyeem.routerconstants.DartResolver
import com.eyeem.routerconstants.RouterPath
import com.eyeem.routerconstants.dartfmt
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

private val log by lazy { defaultLogger() }

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

suspend fun generateCode(name: String, source: String, target: String, testTarget: String?, dryRun: Boolean) = supervisorScope {

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

    val routerPaths = voyagerYaml.asRouterPaths()

    val jobs = mutableListOf<Job>()
    jobs += async { generateVoyagerPaths(name, routerPaths, target, dryRun) }
    if (!testTarget.isNullOrBlank()) {
        jobs += async { generateVoyagerTests(name, routerPaths, testTarget, dryRun) }
    }
    jobs.forEach { it.join() }
}

suspend fun generateVoyagerPaths(name: String, routerPaths: List<RouterPath>, target: String, dryRun: Boolean) {
    toPathsDart(name, routerPaths)
        ?.saveToTarget(target, dryRun)
}

suspend fun generateVoyagerTests(name: String, routerPaths: List<RouterPath>, target: String, dryRun: Boolean) {
    toTestScenariosDart(name, routerPaths)
        ?.saveToTarget(target, dryRun)
}

private fun String.saveToTarget(target: String, dryRun: Boolean) {
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

fun Map<String, Map<String, *>>.asRouterPaths(): List<RouterPath> = keys
    .map {
        RouterPath(
            path = it,
            type = this[it]?.get("type")?.toString() ?: ""
        )
    }.filter { it.path.isNotBlank() }

internal suspend fun toPathsDart(name: String, routerPaths: List<RouterPath>): String? {
    // calculate the template
    return dartVoyagerPathsClass
        .asResource()
        .interpolate(
            mapOf(
                "resolver" to DartResolver(),
                "name" to name,
                "paths" to routerPaths
            )
        )
        ?.dartfmt()
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