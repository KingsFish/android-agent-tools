package com.androidagent.tools.tools.state

import android.content.Context
import com.androidagent.tools.accessibility.AgentAccessibilityService
import com.androidagent.tools.core.*

/**
 * Tool to check if an app is currently running.
 */
class IsAppRunningTool : Tool {
    override val name = "is_app_running"
    override val description = "Check if an application is currently running."

    override fun validate(params: Map<String, Any?>): Result<Unit> {
        val validator = ParameterValidator(params)
        val packageNameResult = validator.requireNonEmptyString("package_name")
        return when (packageNameResult) {
            is Result.Success -> Result.Success(Unit)
            is Result.Failure -> Result.Failure(packageNameResult.error, packageNameResult.context)
        }
    }

    override suspend fun execute(context: Context, params: Map<String, Any?>): ToolResult {
        val validator = ParameterValidator(params)
        val packageName = (validator.requireNonEmptyString("package_name") as Result.Success).value

        val service = AgentAccessibilityService.instance
            ?: return ToolResult.failure(ToolError.ACCESSIBILITY_SERVICE_REQUIRED)

        val isRunning = service.isAppRunning(packageName)

        return ToolResult.success(mapOf(
            "package_name" to packageName,
            "running" to isRunning
        ))
    }
}