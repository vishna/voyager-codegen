package dev.vishna.voyager.codegen

import dev.vishna.emojilog.Log
import org.json.JSONObject
import org.yaml.snakeyaml.Yaml
import java.io.*
import java.util.concurrent.ConcurrentHashMap

fun String.asYaml() :  Map<String, Map<String, *>> = Yaml().load(StringReader(this)) as Map<String, Map<String, *>>
internal fun String.asYamlArray() :  List<*> = Yaml().load(StringReader(this)) as List<*>
internal fun String.asJson() :  Map<String, Map<String, *>> {
    val jsonObject = JSONObject(this)
    return jsonObject.toMap() as Map<String, Map<String, *>>
}
internal fun String.asJsonFromYaml() = JSONObject(asYaml())

typealias ResourcePath = String

inline val <T> T.println
    get() = println(this)

/**
 * Holds current working directory as path.
 */
internal val pwd: FilePath by lazy { System.getProperty("user.dir") }

internal typealias FilePath = String
internal fun FilePath.asFile() : File {
    val file = File(this)
    return if (file.exists()) {
        file
    } else {
        File(pwd, this)
    }
}

val Log.success
    get() = { "✅" } lvl "success"

/**
 * debounce code block with a given id
 */
@Synchronized
fun debounced(id: String, time: Long = 300) : Boolean {
    val now = System.currentTimeMillis()
    val lastDebounce = debounceMap[id] ?: 0
    if (now - lastDebounce > time) {
        debounceMap[id] = now
        return false
    }
    return true
}

private val debounceMap = ConcurrentHashMap<String, Long>()
