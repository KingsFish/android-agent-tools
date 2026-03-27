package com.androidagent.mcp.server

import com.androidagent.tools.AndroidAgentTools
import com.androidagent.tools.core.ToolResult
import org.json.JSONObject

/**
 * MCP Protocol implementation.
 * Handles request/response formatting according to MCP specification.
 */
object McpProtocol {

    /**
     * Create MCP tools/list response
     */
    fun createToolsListResponse(tools: AndroidAgentTools): String {
        val toolList = tools.listTools().map { toolName ->
            // Get tool info from executor (we need to access internal registry)
            // For now, we use a simpler approach by generating schema based on tool name
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