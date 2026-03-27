package com.androidagent.tools.tools.navigation

import android.content.Context
import com.androidagent.tools.accessibility.AgentAccessibilityService
import com.androidagent.tools.core.*

/**
 * Tool to press the recents button to show recent apps.
 */
class PressRecentsTool : Tool {
    override val name = "press_recents"
    override val description = "Press the recents button to show recent apps overview."

    override fun validate(params: Map<String, Any?>): Result<Unit> = Result.Success(Unit)

    override suspend fun execute(context: Context, params: Map<String, Any?>): ToolResult {
        val envDetector = EnvironmentDetector(context)
        if (envDetector.hasRoot()) {
            return executeWithRoot()
        }

        val service = AgentAccessibilityService.instance
            ?: return ToolResult.failure(ToolError.ACCESSIBILITY_SERVICE_REQUIRED)

        return if (service.pressRecents()) {
            ToolResult.success(emptyMap())
        } else {
            ToolResult.failure(ToolError.GLOBAL_ACTION_FAILED, "press_recents")
        }
    }

    private fun executeWithRoot(): ToolResult {
        return try {
            // KEYCODE_APP_SWITCH = 187
            val process = Runtime.getRuntime().exec(
                arrayOf("su", "-c", "input keyevent 187")
            )
            val exitCode = process.waitFor()
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