package com.androidagent.tools

import android.content.Context
import com.androidagent.tools.core.ToolError
import com.androidagent.tools.core.ToolResult
import com.androidagent.tools.executor.ToolExecutor
import com.androidagent.tools.tools.app.GetAppInfoTool
import com.androidagent.tools.tools.app.LaunchAppTool
import com.androidagent.tools.tools.app.ListAppsTool
import com.androidagent.tools.tools.file.DeleteFileTool
import com.androidagent.tools.tools.file.FileExistsTool
import com.androidagent.tools.tools.file.ListDirectoryTool
import com.androidagent.tools.tools.file.ReadFileTool
import com.androidagent.tools.tools.file.WriteFileTool
import com.androidagent.tools.tools.permission.CheckPermissionsTool
import com.androidagent.tools.tools.system.GetBatteryStatusTool
import com.androidagent.tools.tools.system.GetDeviceInfoTool
import kotlinx.coroutines.runBlocking
import org.json.JSONObject

class AndroidAgentTools(private val context: Context) {

    private val executor = ToolExecutor()

    init {
        // Register all tools
        // File tools
        executor.register(ReadFileTool())
        executor.register(WriteFileTool())
        executor.register(ListDirectoryTool())
        executor.register(DeleteFileTool())
        executor.register(FileExistsTool())

        // App tools
        executor.register(ListAppsTool())
        executor.register(GetAppInfoTool())
        executor.register(LaunchAppTool())

        // System tools
        executor.register(GetDeviceInfoTool())
        executor.register(GetBatteryStatusTool())

        // Permission tools
        executor.register(CheckPermissionsTool())
    }

    fun listTools(): List<String> = executor.listTools()

    suspend fun execute(toolName: String, params: Map<String, Any?>): ToolResult {
        return executor.execute(context, toolName, params)
    }

    fun executeJson(toolName: String, paramsJson: String): String {
        val params = try {
            parseJsonToMap(paramsJson)
        } catch (e: Exception) {
            return ToolResult.failure(
                ToolError.INVALID_PARAMETER,
                "Invalid JSON: ${e.message}"
            ).toJson().toString()
        }

        val result = runBlocking {
            executor.execute(context, toolName, params)
        }

        return result.toJson().toString()
    }

    companion object {
        const val VERSION = "1.0.0"
    }
}

private fun parseJsonToMap(json: String): Map<String, Any?> {
    return JSONObject(json).toMap()
}

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