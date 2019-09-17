package dev.vishna.voyager.codegen

import dev.vishna.mvel.interpolate
import dev.vishna.stringcode.asResource
import dev.vishna.stringcode.camelize
import dev.vishna.voyager.codegen.model.RouterPath
import dev.vishna.voyager.codegen.model.ScenarioClassName

class DartResolver : LangResolver() {
    override fun pathExpression(routerPath: RouterPath): String {

        val name = "path_${routerPath.type}".camelize(startWithLowerCase = true);

        if (routerPath.params.isEmpty()) {
            return """const String $name = "${routerPath.path}";"""
        } else {
            return """String $name(${argsExpression(routerPath)}) {
                |    return "${interpolationExpression(routerPath)}";
                |  }""".trimMargin()
        }
    }

    fun testPathExpression(routerPath: RouterPath): String {
        if (routerPath.params.isEmpty()) {
            return """"${routerPath.path}""""
        } else {
            return """"${interpolationExpression(routerPath)}""""
        }
    }

    private fun argsExpression(routerPath: RouterPath) : String {
        val args = routerPath.params.map { "String ${it.dartSanitize()}" }.joinToString(separator = ",")

        if (routerPath.params.size > 1) {
            return "{$args}"
        }

        return args
    }

    private fun interpolationExpression(routerPath: RouterPath) : String {
        var output = routerPath.path
        routerPath.params.forEach {
            output = output.replace(":$it", "$${it.dartSanitize()}")
        }
        return output
    }

    override fun typeExpression(routerPath: RouterPath): String {
        return """const String ${"type_${routerPath.type}".camelize(startWithLowerCase = true)} = "${routerPath.type}";"""
    }

    override fun emit(scenarioClassName: ScenarioClassName): String {
        return dartVoyagerTestScenarioClass.asResource().interpolate(scenarioClassName)!!
    }

    fun emitAsExecutionBlock(scenarioClassName: ScenarioClassName): String {
        return dartVoyagerTestScenarioExecutionBlock.asResource().interpolate(scenarioClassName)!!
    }
}

