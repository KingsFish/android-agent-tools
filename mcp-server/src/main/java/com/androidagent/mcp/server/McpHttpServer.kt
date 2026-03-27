package com.androidagent.mcp.server

import com.androidagent.tools.AndroidAgentTools
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.runBlocking
import org.json.JSONObject

/**
 * HTTP Server implementation for MCP protocol using NanoHTTPD.
 * Provides endpoints for tool listing and tool execution.
 */
class McpHttpServer(port: Int, private val tools: AndroidAgentTools) : NanoHTTPD(port) {

    companion object {
        const val DEFAULT_PORT = 8080
        const val MIME_JSON = "application/json"
    }

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri
        val method = session.method

        return when {
            // Health check endpoint
            uri == "/health" -> newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, "OK")

            // MCP tools list endpoint
            uri == "/mcp/tools/list" && method == Method.POST -> handleListTools()

            // MCP tool call endpoint
            uri == "/mcp/tools/call" && method == Method.POST -> handleCallTool(session)

            // Unknown endpoint
            else -> newFixedLengthResponse(
                Response.Status.NOT_FOUND,
                MIME_JSON,
                "{\"error\": \"Not found\", \"path\": \"$uri\"}"
            )
        }
    }

    /**
     * Handle MCP tools/list request
     */
    private fun handleListTools(): Response {
        val response = McpProtocol.createToolsListResponse(tools)
        return newFixedLengthResponse(Response.Status.OK, MIME_JSON, response)
    }

    /**
     * Handle MCP tools/call request
     */
    private fun handleCallTool(session: IHTTPSession): Response {
        try {
            // Read request body
            val body = parseRequestBody(session)

            // Parse MCP request
            val request = JSONObject(body)
            val toolName = request.optString("name")
            val arguments = request.optJSONObject("arguments")?.toMap() ?: emptyMap()

            if (toolName.isEmpty()) {
                return newFixedLengthResponse(
                    Response.Status.BAD_REQUEST,
                    MIME_JSON,
                    McpProtocol.createErrorResponse("Tool name is required")
                )
            }

            // Execute tool and return result (using runBlocking for suspend function)
            val result = runBlocking {
                tools.execute(toolName, arguments)
            }
            val response = McpProtocol.createToolCallResponse(result)
            return newFixedLengthResponse(Response.Status.OK, MIME_JSON, response)

        } catch (e: Exception) {
            return newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR,
                MIME_JSON,
                McpProtocol.createErrorResponse("Internal error: ${e.message}")
            )
        }
    }

    /**
     * Parse request body from session
     */
    private fun parseRequestBody(session: IHTTPSession): String {
        session.inputStream.use { input ->
            // Read content length
            val contentLength = session.headers.get("content-length")?.toIntOrNull() ?: 0
            if (contentLength > 0) {
                val buffer = ByteArray(contentLength)
                input.read(buffer)
                return String(buffer, Charsets.UTF_8)
            }
        }
        return ""
    }
}

/**
 * Convert JSONObject to Map
 */
private fun JSONObject.toMap(): Map<String, Any?> {
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

private fun org.json.JSONArray.toList(): List<Any?> {
    return (0 until length()).map { i ->
        when (val value = this.get(i)) {
            JSONObject.NULL -> null
            is JSONObject -> value.toMap()
            is org.json.JSONArray -> value.toList()
            else -> value
        }
    }
}