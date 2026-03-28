package com.androidagent.tools.executor

import com.androidagent.core.Tool
import com.androidagent.core.ToolContext
import com.androidagent.core.ToolError
import com.androidagent.core.ToolRegistry
import com.androidagent.core.ToolResult

class ToolExecutor : ToolRegistry {
    private val tools = mutableMapOf<String, Tool>()

    override fun register(tool: Tool) {
        tools[tool.name] = tool
    }

    override fun getTool(name: String): Tool? = tools[name]

    override fun listTools(): List<String> = tools.keys.toList()

    override fun getAllTools(): List<Tool> = tools.values.toList()

    suspend fun execute(context: ToolContext, toolName: String, params: Map<String, Any?>): ToolResult {
        val tool = tools[toolName]
            ?: return ToolResult.failure(ToolError.UNSUPPORTED_OPERATION, "Unknown tool: $toolName")

        return tool.execute(context, params)
    }
}
