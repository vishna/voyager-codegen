package dev.vishna.voyager.codegen

import dev.vishna.stringcode.asResource
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.`should be empty`
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldBeEqualTo
import org.everit.json.schema.ValidationException
import org.junit.Test
import org.everit.json.schema.loader.SchemaLoader


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

        val generatedTargetDart = toPathsDart(name = "Voyager", routerPaths = voyagerYaml.asRouterPaths(), `package` = "", part = "test_1.dart")

        requireNotNull(generatedTargetDart) { "Failed generating VoyagerPaths class for dart" }

        generatedTargetDart `should be equal to` targetDart
    }

    @Test
    fun validateRectangleYamlPOC() = runBlocking<Unit> {
        val rectangle = "/rectangle.yaml".asResource().asJsonFromYaml()
        val loader = SchemaLoader.builder()
            .schemaJson("/rectangle_schema.yaml".asResource().asJsonFromYaml())
            .draftV7Support()
            .build()
        val schema = loader.load().build()
        try {
            schema.validate(rectangle)
        } catch (t: ValidationException) {
            // prints #/rectangle/a: -5.0 is not higher or equal to 0
            println(t.allMessages)
        }
    }

    @Test
    fun validateYaml() {
        // TEST DATA
        val voyagerYaml = "/test_2.yaml".asResource().asYaml()
        val voyagerCodegen = "/voyager-codegen-test_2.yaml".asResource().asYamlArray().first() as Map<String, *>

        // VALIDATION ROUTINE
        val globalDefinitions = voyagerCodegen["definitions"] as? Map<String, Any>
        val schema = voyagerCodegen["schema"] as Map<String, Map<String, *>>

        val output = validateVoyagerPaths(voyagerYaml, schema, globalDefinitions)

        output.errors.size `should be equal to` 4
    }

    @Test
    fun validateYamlPlusSpecialKeys() = runBlocking<Unit> {
        // TEST DATA
        val voyagerYaml = "/test_3.yaml".asResource().asYaml()
        val voyagerCodegen = "/test_3_validation.yaml".asResource().asYamlArray().first() as Map<String, *>

        // VALIDATION ROUTINE
        val globalDefinitions = voyagerCodegen["definitions"] as? Map<String, Any>
        val schema = voyagerCodegen["schema"] as Map<String, Map<String, *>>

        val validationResult = validateVoyagerPaths(voyagerYaml, schema, globalDefinitions)

        validationResult.errors.`should be empty`()

        val generatedTargetDart = toPathsDart(
            name = "Voyager",
            routerPaths = voyagerYaml.asRouterPaths(),
            validationResult = validationResult,
            `package` = "",
            part = "test_3.dart"
        )!!

        generatedTargetDart `should be equal to` "/test_3.dart".asResource()
    }

    @Test
    fun dartKeySanitization() {
        "const".dartSanitize() shouldBeEqualTo "const_"
        "Const".dartSanitize() shouldBeEqualTo "Const"
        "foo".dartSanitize() shouldBeEqualTo "foo"
        "class".dartSanitize() shouldBeEqualTo "class_"
        "Class".dartSanitize() shouldBeEqualTo "Class"
    }
}






