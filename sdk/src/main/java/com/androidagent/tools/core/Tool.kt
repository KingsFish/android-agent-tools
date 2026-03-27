package com.androidagent.tools.core

import android.content.Context
import org.json.JSONObject

interface Tool {
    val name: String
    val description: String

    fun validate(params: Map<String, Any?>): Result<Unit>

    suspend fun execute(context: Context, params: Map<String, Any?>): ToolResult

    fun executeJson(context: Context, paramsJson: String): String {
        val params = try {
            JSONObject(paramsJson).toMap()
        } catch (e: Exception) {
            return ToolResult.failure(ToolError.INVALID_PARAMETER, "Invalid JSON: ${e.message}").toJson().toString()
        }

        val validation = validate(params)
        if (validation.isFailure) {
            return ToolResult.failure(
                (validation as Result.Failure).error,
                validation.context
            ).toJson().toString()
        }

        // Note: execute is suspend, so this needs coroutine scope
        // For simplicity, we use runBlocking here; in production, use proper coroutine scope
        return kotlinx.coroutines.runBlocking {
            execute(context, params).toJson().toString()
        }
    }
}

fun JSONObject.toMap(): Map<String, Any?> {
    val map = mutableMapOf<String, Any?>()
    val keys = this.keys()
    while (keys.hasNext()) {
        val key = keys.next()
        map[key] = when (val value = this.get(key)) {
            JSONObject.NULL -> null
            is JSONObject -> value.toMap()
            is org.json.JSONArray -> value.toList()
            else -> value
        }
    }
    return map
}

fun org.json.JSONArray.toList(): List<Any?> {
    return (0 until length()).map { i ->
        when (val value = this.get(i)) {
            JSONObject.NULL -> null
            is JSONObject -> value.toMap()
            is org.json.JSONArray -> value.toList()
            else -> value
        }
    }
}