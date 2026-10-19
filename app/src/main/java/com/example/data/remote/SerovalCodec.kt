package com.example.data.remote

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * Seroval codec to serialize requests and deserialize responses from
 * TanStack Start Server Functions on the QuickFood backend server.
 */
object SerovalCodec {

    fun parse(jsonStr: String): Any? {
        val json = JSONObject(jsonStr)
        val refs = mutableMapOf<Int, Any?>()
        return parseNode(json, refs)
    }

    private fun parseNode(node: Any?, refs: MutableMap<Int, Any?>): Any? {
        if (node !is JSONObject) return node

        val t = if (node.has("t")) node.optInt("t") else -1
        val hasI = node.has("i")
        val i = if (hasI) node.optInt("i") else -1

        return when (t) {
            1 -> { // String
                val s = node.optString("s")
                if (hasI) refs[i] = s
                s
            }
            0 -> { // Number
                val s = node.opt("s")
                if (hasI) refs[i] = s
                s
            }
            2 -> { // Constants (bool / null / undefined)
                val s = node.optInt("s")
                val res = when (s) {
                    2 -> true
                    3 -> false
                    else -> null
                }
                if (hasI) refs[i] = res
                res
            }
            4 -> { // Reference
                refs[i]
            }
            9 -> { // Array
                val a = node.optJSONArray("a") ?: JSONArray()
                val list = mutableListOf<Any?>()
                if (hasI) refs[i] = list
                for (idx in 0 until a.length()) {
                    list.add(parseNode(a.opt(idx), refs))
                }
                list
            }
            10 -> { // Object
                val p = node.optJSONObject("p") ?: JSONObject()
                val k = p.optJSONArray("k") ?: JSONArray()
                val v = p.optJSONArray("v") ?: JSONArray()
                val map = mutableMapOf<String, Any?>()
                if (hasI) refs[i] = map
                for (idx in 0 until k.length()) {
                    val key = k.optString(idx)
                    val value = parseNode(v.opt(idx), refs)
                    map[key] = value
                }
                map
            }
            11 -> { // Empty object / Map
                val map = mutableMapOf<String, Any?>()
                if (hasI) refs[i] = map
                map
            }
            else -> {
                // If it's a wrapper object like {"result": ..., "error": ...}
                val map = mutableMapOf<String, Any?>()
                val keys = node.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    map[key] = parseNode(node.opt(key), refs)
                }
                map
            }
        }
    }

    /**
     * Serializes any primitive, List or Map into Seroval payload
     * e.g. {"t": {...}, "f": 127, "m": []}
     */
    fun serialize(payloadObj: Any?): String {
        var counter = 0
        fun encode(item: Any?): JSONObject {
            val node = JSONObject()
            when (item) {
                null -> {
                    node.put("t", 2)
                    node.put("s", 0)
                }
                is Boolean -> {
                    node.put("t", 2)
                    node.put("s", if (item) 2 else 3)
                }
                is Number -> {
                    node.put("t", 0)
                    node.put("s", item)
                }
                is String -> {
                    node.put("t", 1)
                    node.put("s", item)
                }
                is List<*> -> {
                    val currId = counter++
                    val arr = JSONArray()
                    for (child in item) {
                        arr.put(encode(child))
                    }
                    node.put("t", 9)
                    node.put("i", currId)
                    node.put("a", arr)
                    node.put("o", 0)
                }
                is Map<*, *> -> {
                    val currId = counter++
                    val keysArr = JSONArray()
                    val valsArr = JSONArray()
                    for ((k, v) in item) {
                        keysArr.put(k.toString())
                        valsArr.put(encode(v))
                    }
                    val p = JSONObject()
                    p.put("k", keysArr)
                    p.put("v", valsArr)
                    node.put("t", 10)
                    node.put("i", currId)
                    node.put("p", p)
                    node.put("o", 0)
                }
                else -> {
                    node.put("t", 1)
                    node.put("s", item.toString())
                }
            }
            return node
        }

        val rootNode = encode(payloadObj)
        val container = JSONObject()
        container.put("t", rootNode)
        container.put("f", 127)
        container.put("m", JSONArray())
        return container.toString()
    }

    fun buildQueryPayload(dataObj: Any?): String {
        val serialized = serialize(mapOf("data" to dataObj))
        return URLEncoder.encode(serialized, "UTF-8")
    }
}
