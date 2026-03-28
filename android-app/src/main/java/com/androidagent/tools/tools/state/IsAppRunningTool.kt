package com.androidagent.tools.tools.state

import com.androidagent.core.*
import com.androidagent.androidapp.AppToolContext

class IsAppRunningTool : Tool {
    override val name = "is_app_running"
    override val description = "Check if an app is running."
    override val inputSchema = ToolSchema(
        properties = mapOf(
            "package_name" to SchemaProperty(type = "string", description = "Package name to check")
        ),
        required = listOf("package_name")
    )

    override fun validate(params: Map<String, Any?>): ValidationResult {
        val validator = ParameterValidator(params)
        return validator.requireString("package_name")
    }

    override suspend fun execute(context: ToolContext, params: Map<String, Any?>): ToolResult {
        val appContext = context as? AppToolContext
            ?: return ToolResult.failure(ToolError.UNSUPPORTED_OPERATION, "Requires Android context")

        val service = appContext.getAccessibilityService()
            ?: return ToolResult.failure(ToolError.ACCESSIBILITY_SERVICE_REQUIRED)

        val validator = ParameterValidator(params)
        val packageName = validator.getString("package_name")
            ?: return ToolResult.failure(ToolError.INVALID_PARAMETER, "Missing package_name")

        val isRunning = service.isAppRunning(packageName)
        return ToolResult.success(mapOf("is_running" to isRunning))
    }
}
