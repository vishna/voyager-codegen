package com.eyeem.routerconstants

data class RouterPath(
        val path : String,
        val type : String
) {
    val params: List<String> = path
            .split("/")
            .filter { it.startsWith(":") }
            .map { it.removePrefix(":") }

}