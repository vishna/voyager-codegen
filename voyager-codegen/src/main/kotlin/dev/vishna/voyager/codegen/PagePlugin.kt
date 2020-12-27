package dev.vishna.voyager.codegen

import dev.vishna.mvel.interpolate
import dev.vishna.stringcode.asResource
import dev.vishna.voyager.codegen.model.RouterPath

fun toPageMappings(routerPaths: List<RouterPath>, pagePlugin: Map<String, *>?) : List<PageMapping> {
    if (routerPaths.isEmpty() || pagePlugin == null) {
        return emptyList()
    }
    val skipItems = pagePlugin["skip"] as List<String>? ?: emptyList()
    return routerPaths
            .mapNotNull { routerPath -> routerPath.config?.get("page") as String? }
            .distinctBy { it }
            .filter { !skipItems.contains(it) }
            .map { PageMapping(it) }
}

data class PageMapping(val name: String) {
    fun emit() = name
}

class PagePluginEmitter(val name: String, val pageMappings: List<PageMapping>) {
    fun emit() : String {
        if (pageMappings.isEmpty()) {
            return ""
        }

        return dartVoyagerPageMappings.asResource().interpolate(
                mapOf(
                        "name" to name,
                        "mappings" to pageMappings
                )
        )!!
    }
}