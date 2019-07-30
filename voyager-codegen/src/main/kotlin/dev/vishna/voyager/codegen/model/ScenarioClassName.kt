package dev.vishna.voyager.codegen.model

import com.eyeem.routerconstants.LangResolver
import com.eyeem.routerconstants.RouterPath
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