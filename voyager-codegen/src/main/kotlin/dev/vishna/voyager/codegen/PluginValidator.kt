package dev.vishna.voyager.codegen

import dev.vishna.mvel.interpolate
import dev.vishna.stringcode.asResource
import dev.vishna.stringcode.camelize
import dev.vishna.voyager.codegen.model.RouterPath
import org.everit.json.schema.Schema
import org.everit.json.schema.ValidationException
import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONObject

val SNAKE_CASE = """[a-zA-Z0-9]+(_[a-zA-Z0-9]+)*""".toRegex()


class PluginValidator(
    val inputSchemaJson: JSONObject,
    val pluginNode: String,
    val output: String?,
    val import: String?,
    val input: Schema,
    val pluginStub: Boolean
)

sealed class PluginError(
    val message: String,
    val routerPath: RouterPath,
    private val pluginNode: String
) {
    class MissingConfig(routerPath: RouterPath) : PluginError("Missing config", routerPath, "")

    class TypeNotSnakeCase(routerPath: RouterPath, typeValue: String) :
        PluginError("Type $typeValue must be snake_case", routerPath, "type")

    class PackageNotSnakeCase(routerPath: RouterPath, packageValue: String) :
        PluginError("Package $packageValue must be snake_case", routerPath, "package")

    class MissingPluginSchema(routerPath: RouterPath, pluginNode: String) :
        PluginError("No schema defined for plugin $pluginNode", routerPath, pluginNode)

    class Validation(
        message: String,
        routerPath: RouterPath,
        pluginNode: String
    ) : PluginError(message, routerPath, pluginNode)


    override fun toString(): String =
        "${routerPath.path}${if (pluginNode.isBlank()) "" else "@"}$pluginNode: $message"
}

fun validateVoyagerPaths(
    voyagerYaml: Map<String, Map<String, *>>,
    schema: Map<String, Map<String, *>>,
    globalDefinitions: Map<String, Any>?
): ValidationResult {
    val pluginValidators = schema.keys.mapNotNull { pluginNode ->
        val validation = schema[pluginNode] ?: return@mapNotNull null
        val pluginSchema = validation["input"] as Map<String, Any>
        val inputSchema = compositeSchema(pluginNode, pluginSchema, globalDefinitions)
        val inputSchemaJson = JSONObject(inputSchema)

        val loader = SchemaLoader.builder()
            .schemaJson(inputSchemaJson)
            .draftV7Support()
            .build()
        val compiledSchema = loader.load().build()

        PluginValidator(
            inputSchemaJson = inputSchemaJson,
            pluginNode = pluginNode,
            import = validation["import"] as String?,
            output = validation["output"] as String?,
            input = compiledSchema,
            pluginStub = validation["pluginStub"] as? Boolean ?: false
        )
    }.associateBy({ it.pluginNode }, { it })

    val errors = mutableListOf<PluginError>()

    val routerPaths = voyagerYaml.asRouterPaths()
    routerPaths.forEach { routerPath ->
        if (routerPath.config == null) {
            errors += PluginError.MissingConfig(routerPath)
            return@forEach
        }

        val config = routerPath.config

        config.keys.forEach { pluginNode ->
            if (pluginNode == "type") {
                val typeValue = config["type"]
                if (typeValue !is String || !(typeValue matches SNAKE_CASE)) {
                    errors += PluginError.TypeNotSnakeCase(routerPath, typeValue.toString())
                }
                return@forEach
            }
            if (pluginNode == "package") {
                val packageValue = config["package"]
                if (packageValue !is String || !(packageValue matches SNAKE_CASE)) {
                    errors += PluginError.PackageNotSnakeCase(routerPath, packageValue.toString())
                }
                return@forEach
            }
            val pluginValidator = pluginValidators[pluginNode]
            if (pluginValidator == null) {
                errors += PluginError.MissingPluginSchema(routerPath, pluginNode)
                return@forEach
            }
            val pluginConfig = config[pluginNode]
            try {
                val pluginConfigPair = JSONObject(
                    mapOf(
                        pluginNode to pluginConfig
                    )
                )
                pluginValidator.input.validate(pluginConfigPair)
            } catch (exception: ValidationException) {
                errors += captureExceptionsRecursively(routerPath, pluginNode, exception)
            }
        }
    }

    return ValidationResult(
        errors = errors,
        validators = pluginValidators.values.toList()
    )
}

class ValidationResult(
    val validators: List<PluginValidator>,
    val errors: List<PluginError>
)

private const val KEY_DEFINITIONS = "definitions"
private fun compositeSchema(
    pluginNode: String,
    pluginSchema: Map<String, Any>,
    globalDefinitions: Map<String, Any>?
): Map<String, Any> {
    val output = mutableMapOf<String, Any>()

    val adjustedSchema = mutableMapOf<String, Any>()
    adjustedSchema.putAll(pluginSchema)
    adjustedSchema.remove(KEY_DEFINITIONS)

    val localDefinitions = pluginSchema[KEY_DEFINITIONS] as? Map<String, Any>
    if (localDefinitions != null || globalDefinitions != null) {
        val definitions = mutableMapOf<String, Any>()
        localDefinitions?.let { definitions += it }
        globalDefinitions?.let { definitions += it }
        output[KEY_DEFINITIONS] = definitions
    }

    output["type"] = "object"
    output["properties"] = mapOf(
        pluginNode to pluginSchema
    )
    return output
}

private fun captureExceptionsRecursively(
    routerPath: RouterPath,
    pluginNode: String,
    validationException: ValidationException
): List<PluginError.Validation> {
    val errors = mutableListOf<PluginError.Validation>()
    errors += PluginError.Validation(
        routerPath = routerPath,
        pluginNode = pluginNode,
        message = validationException.message ?: ""
    )
    validationException.causingExceptions.forEach {
        errors += captureExceptionsRecursively(routerPath, pluginNode, it)
    }
    return errors
}

class VoyagerDataClassEmitter(val name: String, val validationResult: ValidationResult) {

    val fields = validationResult.validators.filter { it.output != null }.map {
        Field(name = it.pluginNode, type = it.output!!)
    }

    fun emit(): String = requireNotNull(
        dartVoyagerDataClass.asResource().interpolate(
            mapOf(
                "name" to name,
                "nameDecap" to name.decapitalize(),
                "fields" to fields
            )
        )
    ) { "interpolation result for $name was null" }

    data class Field(val name: String, val type: String) {
        val nameSanitized = name.dartSanitize()
        val typeSanitized = type.dartNullableType()
    }
}

class PluginStubClassEmitter(val validator: PluginValidator) {
    fun emit() : String = dartVoyagerPluginStub.asResource().interpolate(
        mapOf(
            "className" to "${validator.pluginNode.camelize()}PluginStub",
            "outputClass" to validator.output,
            "nodeName" to validator.pluginNode
        )
    )!!
}