package dev.vishna.voyager.codegen

import dev.vishna.mvel.interpolate
import dev.vishna.stringcode.asResource
import dev.vishna.voyager.codegen.model.RouterPath

fun toWidgetMappings(routerPaths: List<RouterPath>, widgetPlugin: Map<String, *>?) : List<WidgetMapping> {
    if (routerPaths.isEmpty() || widgetPlugin == null) {
        return emptyList()
    }
    val skipItems = widgetPlugin["skip"] as List<String>? ?: emptyList()
    return routerPaths
            .mapNotNull { routerPath -> routerPath.config?.get("widget") as String? }
            .distinctBy { it }
            .filter { !skipItems.contains(it) }
            .map { WidgetMapping(it) }
}

data class WidgetMapping(val className: String)

class WidgetPluginEmitter(val name: String, val widgetMappings: List<WidgetMapping>) {
    fun emit() : String {
        if (widgetMappings.isEmpty()) {
            return ""
        }

        return dartVoyagerWidgetMappings.asResource().interpolate(
                mapOf(
                        "name" to name,
                        "mappings" to widgetMappings
                )
        )!!
    }
}