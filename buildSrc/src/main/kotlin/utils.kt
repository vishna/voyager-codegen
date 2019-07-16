import org.gradle.api.Project

import org.gradle.kotlin.dsl.*

/**
 * Configures the current project as a Kotlin project by adding the Kotlin `stdlib` as a dependency.
 */
fun Project.kotlinProject() {
    dependencies {
        "compile"(kotlin("stdlib"))
        "compile"("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.0.1")
        "compile"("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.0.1")
    }
}
