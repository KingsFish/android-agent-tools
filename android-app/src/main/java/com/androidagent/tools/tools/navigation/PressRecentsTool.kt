package com.androidagent.tools.tools.navigation

import com.androidagent.core.*
import com.androidagent.androidapp.AndroidCapability
import com.androidagent.androidapp.AppToolContext

class PressRecentsTool : Tool {
    override val name = "press_recents"
    override val description = "Press the recents button to show recent apps overview."
    override val inputSchema = ToolSchema.noParams()

    override fun validate(params: Map<String, Any?>): ValidationResult = ValidationResult.success()

    override suspend fun execute(context: ToolContext, params: Map<String, Any?>): ToolResult {
        val appContext = context as? AppToolContext
            ?: return ToolResult.failure(ToolError.UNSUPPORTED_OPERATION, "Requires Android context")

        if (context.hasCapability(AndroidCapability.ROOT)) {
            return executeWithRoot()
        }

        val service = appContext.getAccessibilityService()
            ?: return ToolResult.failure(ToolError.ACCESSIBILITY_SERVICE_REQUIRED)

        return if (service.pressRecents()) {
            ToolResult.success(emptyMap())
        } else {
            ToolResult.failure(ToolError.GLOBAL_ACTION_FAILED, "press_recents")
        }
    }

    private fun executeWithRoot(): ToolResult {
        return try {
            val proc = java.lang.Runtime.getRuntime().exec(arrayOf("su", "-c", "input keyevent 187"))
            val exitCode = proc.waitFor()
            if (exitCode == 0) {
                ToolResult.success(emptyMap())
            } else {
                ToolResult.failure(ToolError.GLOBAL_ACTION_FAILED, "ROOT command failed with exit code $exitCode")
            }
        } catch (e: Exception) {
            ToolResult.failure(ToolError.GLOBAL_ACTION_FAILED, e.message)
        }
    }
}
