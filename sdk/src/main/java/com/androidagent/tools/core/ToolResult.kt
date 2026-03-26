package com.androidagent.tools.core

import org.json.JSONObject

sealed class ToolResult {
    abstract fun toJson(): JSONObject

    data class Success(val data: Map<String, Any?>) : ToolResult() {
        override fun toJson(): JSONObject {
            return JSONObject().apply {
                put("success", true)
                put("data", JSONObject(data))
            }
        }
    }

    data class Failure(val error: ToolError, val context: String? = null) : ToolResult() {
        override fun toJson(): JSONObject {
            return JSONObject().apply {
                put("success", false)
                put("error", JSONObject().apply {
                    put("code", error.name)
                    put("message", context?.let { error.withContext(it) } ?: error.message)
                })
            }
        }
    }

    companion object {
        fun success(data: Map<String, Any?> = emptyMap()): ToolResult = Success(data)
        fun failure(error: ToolError, context: String? = null): ToolResult = Failure(error, context)
    }
}