package com.eyeem.routerconstants

import dev.vishna.stringcode.camelize

class DartResolver : LangResolver() {
    override fun pathExpression(routerPath: RouterPath): String {

        val name = "path_${routerPath.type}".camelize(startWithLowerCase = true);

        if (routerPath.params.isEmpty()) {
            return """static const String $name = "${routerPath.path}";"""
        } else {
            return """static String $name(${argsExpression(routerPath)}) {
                |    return "${interpolationExpression(routerPath)}";
                |  }""".trimMargin()
        }
    }

    private fun argsExpression(routerPath: RouterPath) : String {
        val args = routerPath.params.map { "String $it" }.joinToString(separator = ",")

        if (routerPath.params.size > 1) {
            return "{$args}"
        }

        return args
    }

    private fun interpolationExpression(routerPath: RouterPath) : String {
        return routerPath.path.replace(":", "$")
    }

    override fun typeExpression(routerPath: RouterPath): String {
        return """static const String ${"type_${routerPath.type}".camelize(startWithLowerCase = true)} = "${routerPath.type}";"""
    }
}