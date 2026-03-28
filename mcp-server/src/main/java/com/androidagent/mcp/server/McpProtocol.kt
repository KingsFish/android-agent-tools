package com.androidagent.mcp.server

import com.androidagent.core.ToolResult
import com.androidagent.tools.AndroidAgentTools
import org.json.JSONObject

/**
 * MCP Protocol implementation.
 */
object McpProtocol {

    /**
     * Create MCP tools/list response
     */
    fun createToolsListResponse(tools: AndroidAgentTools): String {
        val toolList = tools.listTools().map { toolName ->
            ToolSchemaGenerator.generateToolSchema(toolName)
        }

        return JSONObject().apply {
            put("tools", org.json.JSONArray(toolList.map { JSONObject(it) }))
        }.toString()
    }

    /**
     * Create MCP tools/call response
     */
    fun createToolCallResponse(result: ToolResult): String {
        val contentText = result.toJson().toString()

        return JSONObject().apply {
            put("content", org.json.JSONArray().apply {
                put(JSONObject().apply {
                    put("type", "text")
                    put("text", contentText)
                })
            })
        }.toString()
    }

    /**
     * Create error response
     */
    fun createErrorResponse(message: String): String {
        return JSONObject().apply {
            put("error", message)
        }.toString()
    }
}
