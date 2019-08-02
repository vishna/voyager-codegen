package dev.vishna.voyager.codegen.model

import dev.vishna.voyager.codegen.LangResolver
import dev.vishna.stringcode.camelize

class ScenarioClassName(
    val scenarioClassName: String,
    val baseScenarioClassName: String,
    val routerPath: RouterPath,
    val resolver: LangResolver
) {
    fun params() = routerPath.params
    fun type() = routerPath.type.camelize(startWithLowerCase = true)
}