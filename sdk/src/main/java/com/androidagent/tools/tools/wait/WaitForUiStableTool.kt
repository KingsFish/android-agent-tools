package com.androidagent.tools.tools.wait

import android.content.Context
import com.androidagent.tools.accessibility.AgentAccessibilityService
import com.androidagent.tools.core.*

/**
 * Tool to wait for the UI to become stable.
 * Useful for waiting after app launch or navigation.
 */
class WaitForUiStableTool : Tool {
    override val name = "wait_for_ui_stable"
    override val description = "Wait for the UI to become stable (no changes)."

    override fun validate(params: Map<String, Any?>): Result<Unit> = Result.Success(Unit)

    override suspend fun execute(context: Context, params: Map<String, Any?>): ToolResult {
        val validator = ParameterValidator(params)
        val timeout = validator.optionalInt("timeout", 5000).toLong()

        val service = AgentAccessibilityService.instance
            ?: return ToolResult.failure(ToolError.ACCESSIBILITY_SERVICE_REQUIRED)

        val success = service.waitForUiStable(timeout)

        return if (success) {
            ToolResult.success(mapOf("stable" to true))
        } else {
            ToolResult.failure(ToolError.WAIT_TIMEOUT, "UI did not stabilize within ${timeout}ms")
        }
    }
}