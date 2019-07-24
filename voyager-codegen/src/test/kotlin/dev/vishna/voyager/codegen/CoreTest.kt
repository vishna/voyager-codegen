package dev.vishna.voyager.codegen

import kotlinx.coroutines.runBlocking
import org.amshove.kluent.`should be equal to`
import org.junit.Test

class CoreTest {

    /**
     * Samples copied from https://github.com/vishna/voyager/tree/7f99fe1d9f3ae30ff9bd02343bd510b42b839700/example/lib
     */
    @Test
    fun generateDartFromYaml() = runBlocking {

        val inputYaml = """
            ---
            '/home' :
              type: 'home'
              screen: PageWidget
              title: "This is Home"
              body: "Hello World"
              fabPath: /fab
            '/other/:title' :
              type: 'other'
              screen: PageWidget
              body: "Welcome to the other side"
              title: "This is %{title}"
            '/fab' :
              type: fab
              screen: FabWidget
              target: /other/thing
              icon: e88f # check icons.dart for reference
        """.trimIndent()

        val targetDart = """
            /// Generated file, DO NOT EDIT
            class VoyagerPaths {
              static const String pathHome = "/home";
              static const String typeHome = "home";
              static String pathOther(String title) {
                return "/other/${'$'}title";
              }
            
              static const String typeOther = "other";
              static const String pathFab = "/fab";
              static const String typeFab = "fab";
            }

        """.trimIndent()

        val voyagerYaml = inputYaml.asYaml()
        val generatedTargetDart = toPathsDart(name = "Voyager", input = voyagerYaml)

        requireNotNull(generatedTargetDart) { "Failed generating VoyagerPaths class for dart" }

        generatedTargetDart `should be equal to` targetDart
        Unit
    }

}