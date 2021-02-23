package dev.vishna.voyager.codegen

import dev.vishna.stringcode.camelize
import dev.vishna.voyager.codegen.model.RouterPath

class DartResolver(val nullsafety: Boolean) : LangResolver() {
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

    private fun argsExpression(routerPath: RouterPath) : String {

        if (nullsafety && routerPath.params.size > 1) {
            val args = routerPath.params.map { "required String ${it.dartSanitize()}" }.joinToString(separator = ",")
            return "{$args}"
        }

        val args = routerPath.params.map { "String ${it.dartSanitize()}" }.joinToString(separator = ",")

        if (routerPath.params.size > 1) {
            return "{$args}"
        }

        return args
    }

    private fun interpolationExpression(routerPath: RouterPath) : String {
        var output = routerPath.path
        routerPath.params.forEach {
            output = output.replace(":$it", "$${it.dartSanitize()}").removeSuffix(":")
        }
        return output
    }

    override fun typeExpression(routerPath: RouterPath): String {
        return """const String ${"type_${routerPath.type}".camelize(startWithLowerCase = true)} = "${routerPath.type}";"""
    }
}

