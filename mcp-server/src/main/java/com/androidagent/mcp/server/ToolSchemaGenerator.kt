package com.androidagent.mcp.server

import com.androidagent.core.Tool

/**
 * Generates JSON Schema for tools from Tool instances.
 */
object ToolSchemaGenerator {

    /**
     * Generate a JSON Schema from a Tool instance.
     */
    fun generateToolSchema(tool: Tool): Map<String, Any?> {
        return mapOf(
            "name" to tool.name,
            "description" to tool.description,
            "inputSchema" to tool.inputSchema.toMap()
        )
    }
}