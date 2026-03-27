package com.androidagent.tools.tools.navigation

import android.content.Context
import com.androidagent.tools.accessibility.AgentAccessibilityService
import com.androidagent.tools.core.*
import com.androidagent.tools.core.KeyEventConstants

/**
 * Tool to press the home button.
 */
class PressHomeTool : Tool {
    override val name = "press_home"
    override val description = "Press the home button to go to the home screen."

    override fun validate(params: Map<String, Any?>): Result<Unit> = Result.Success(Unit)

    override suspend fun execute(context: Context, params: Map<String, Any?>): ToolResult {
        val envDetector = EnvironmentDetector(context)
        if (envDetector.hasRoot()) {
            return executeWithRoot()
        }

        val service = AgentAccessibilityService.instance
            ?: return ToolResult.failure(ToolError.ACCESSIBILITY_SERVICE_REQUIRED)

        return if (service.pressHome()) {
            ToolResult.success(emptyMap())
        } else {
            ToolResult.failure(ToolError.GLOBAL_ACTION_FAILED, "press_home")
        }
    }

    private fun executeWithRoot(): ToolResult {
        return try {
            val process = Runtime.getRuntime().exec(
                arrayOf("su", "-c", "input keyevent ${KeyEventConstants.HOME}")
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