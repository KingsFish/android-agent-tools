package com.androidagent.core

/**
 * Schema 属性定义
 *
 * 描述工具参数的单个属性。
 */
data class SchemaProperty(
    val type: String,
    val description: String = "",
    val default: Any? = null,
    val items: SchemaProperty? = null  // 用于数组类型
) {
    fun toMap(): Map<String, Any?> {
        return buildMap {
            put("type", type)
            if (description.isNotEmpty()) put("description", description)
            if (default != null) put("default", default)
            if (items != null) put("items", items.toMap())
        }
    }
}