package dev.vishna.voyager.codegen.model

data class RouterPath(
        val path : String,
        val type : String
) {
    val params: List<String> = path
            .split("/")
            .filter { it.startsWith(":") }
            .map { it.removePrefix(":") }

}