package dev.vishna.voyager.codegen

import dev.vishna.stringcode.asResource
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.`should be equal to`
import org.junit.Test

/**
 * test_1.yaml is copied from https://github.com/vishna/voyager/tree/7f99fe1d9f3ae30ff9bd02343bd510b42b839700/example/lib
 */
class CoreTest {

    @Test
    fun calculateRouterPathsFromYaml() {
        val voyagerYaml = "/test_1.yaml".asResource().asYaml()
        val routerPaths = voyagerYaml.asRouterPaths()

        routerPaths.size `should be equal to` 3
        routerPaths[0].path `should be equal to` "/home"
        routerPaths[0].type `should be equal to` "home"
        routerPaths[1].path `should be equal to` "/other/:title"
        routerPaths[1].type `should be equal to` "other"
        routerPaths[2].path `should be equal to` "/fab"
        routerPaths[2].type `should be equal to` "fab"
    }

    @Test
    fun generateDartFromYaml() = runBlocking<Unit> {
        val voyagerYaml = "/test_1.yaml".asResource().asYaml()
        val targetDart = "test_1.dart".asResource()

        val generatedTargetDart = toPathsDart(name = "Voyager", routerPaths = voyagerYaml.asRouterPaths())

        requireNotNull(generatedTargetDart) { "Failed generating VoyagerPaths class for dart" }

        generatedTargetDart `should be equal to` targetDart
    }

    @Test
    fun generateDartTests() = runBlocking<Unit> {
        val voyagerYaml = "/test_1.yaml".asResource().asYaml()
        val targetDart = "test_1_scenarios.dart".asResource()

        val generatedTargetDart = toTestScenariosDart(name = "Voyager", routerPaths = voyagerYaml.asRouterPaths())

        requireNotNull(generatedTargetDart) { "Failed generating VoyagerScenarios class for dart" }

        generatedTargetDart `should be equal to` targetDart
    }

}