package com.androidagent.tools.executor

import android.content.Context
import com.androidagent.tools.core.Tool
import com.androidagent.tools.core.ToolError
import com.androidagent.tools.core.ToolResult

class ToolExecutor {
    private val tools = mutableMapOf<String, Tool>()

    fun register(tool: Tool) {
        tools[tool.name] = tool
    }

    fun getTool(name: String): Tool? = tools[name]

    fun listTools(): List<String> = tools.keys.toList()

    suspend fun execute(context: Context, toolName: String, params: Map<String, Any?>): ToolResult {
        val tool = tools[toolName]
            ?: return ToolResult.failure(ToolError.UNSUPPORTED_OPERATION, "Unknown tool: $toolName")

        return tool.execute(context, params)
    }
}