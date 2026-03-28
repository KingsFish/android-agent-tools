package com.androidagent.core

/**
 * 工具输入 Schema 定义
 *
 * 描述工具参数的完整结构，符合 JSON Schema 规范。
 */
data class ToolSchema(
    val type: String = "object",
    val properties: Map<String, SchemaProperty> = emptyMap(),
    val required: List<String> = emptyList()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "type" to type,
            "properties" to properties.mapValues { it.value.toMap() },
            "required" to required
        )
    }

    companion object {
        /**
         * 创建无参数的工具 Schema
         */
        fun noParams(): ToolSchema = ToolSchema(
            type = "object",
            properties = emptyMap(),
            required = emptyList()
        )
    }
}