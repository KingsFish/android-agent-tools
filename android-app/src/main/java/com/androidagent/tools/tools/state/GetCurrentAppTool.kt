package com.androidagent.tools.tools.state

import com.androidagent.core.*
import com.androidagent.androidapp.AppToolContext

class GetCurrentAppTool : Tool {
    override val name = "get_current_app"
    override val description = "Get the current foreground app package name."
    override val inputSchema = ToolSchema.noParams()

    override fun validate(params: Map<String, Any?>): ValidationResult = ValidationResult.success()

    override suspend fun execute(context: ToolContext, params: Map<String, Any?>): ToolResult {
        val appContext = context as? AppToolContext
            ?: return ToolResult.failure(ToolError.UNSUPPORTED_OPERATION, "Requires Android context")

        val service = appContext.getAccessibilityService()
            ?: return ToolResult.failure(ToolError.ACCESSIBILITY_SERVICE_REQUIRED)

        val packageName = service.getCurrentApp()
            ?: return ToolResult.failure(ToolError.UNSUPPORTED_OPERATION, "Cannot get current app")

        return ToolResult.success(mapOf("package_name" to packageName))
    }
}
