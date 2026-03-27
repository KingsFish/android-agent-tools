package com.androidagent.tools.tools.state

import android.content.Context
import com.androidagent.tools.accessibility.AgentAccessibilityService
import com.androidagent.tools.core.*

/**
 * Tool to get the current foreground app package name.
 */
class GetCurrentAppTool : Tool {
    override val name = "get_current_app"
    override val description = "Get the package name of the current foreground application."

    override fun validate(params: Map<String, Any?>): Result<Unit> = Result.Success(Unit)

    override suspend fun execute(context: Context, params: Map<String, Any?>): ToolResult {
        val service = AgentAccessibilityService.instance
            ?: return ToolResult.failure(ToolError.ACCESSIBILITY_SERVICE_REQUIRED)

        val packageName = service.getCurrentApp()
            ?: return ToolResult.failure(ToolError.UI_TREE_FAILED, "Failed to get current app")

        return ToolResult.success(mapOf("package_name" to packageName))
    }
}