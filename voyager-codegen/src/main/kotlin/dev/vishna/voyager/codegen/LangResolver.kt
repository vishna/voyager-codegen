package dev.vishna.voyager.codegen

import dev.vishna.voyager.codegen.model.RouterPath
import dev.vishna.voyager.codegen.model.ScenarioClassName

open class LangResolver {

    open fun pathExpression(routerPath: RouterPath) : String {
        return "// TODO"
    }

    open fun typeExpression(routerPath: RouterPath) : String {
        return "// TODO"
    }

    open fun emit(scenarioClassName: ScenarioClassName) : String {
        return "// TODO"
    }
}