package com.androidagent.tools.tools.ui

import android.content.Context
import com.androidagent.tools.accessibility.AgentAccessibilityService
import com.androidagent.tools.core.*

class TapTool : Tool {
    override val name = "tap"
    override val description = "Perform a tap at the specified coordinates."

    override fun validate(params: Map<String, Any?>): Result<Unit> {
        val validator = ParameterValidator(params)
        val xResult = validator.requireInt("x")
        val yResult = validator.requireInt("y")

        return when {
            xResult.isFailure -> xResult as Result.Failure
            yResult.isFailure -> yResult as Result.Failure
            else -> Result.Success(Unit)
        }
    }

    override suspend fun execute(context: Context, params: Map<String, Any?>): ToolResult {
        val validator = ParameterValidator(params)
        val x = (validator.requireInt("x") as Result.Success).value
        val y = (validator.requireInt("y") as Result.Success).value

        val envDetector = EnvironmentDetector(context)
        if (envDetector.hasRoot()) {
            return executeWithRoot(x, y)
        }

        val service = AgentAccessibilityService.instance
            ?: return ToolResult.failure(ToolError.ACCESSIBILITY_SERVICE_REQUIRED)

        val success = service.performTap(x, y)
        return if (success) {
            ToolResult.success(emptyMap())
        } else {
            ToolResult.failure(ToolError.GESTURE_FAILED, "Tap at ($x, $y)")
        }
    }

    private fun executeWithRoot(x: Int, y: Int): ToolResult {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "input tap $x $y"))
            val exitCode = process.waitFor()
            if (exitCode == 0) {
                ToolResult.success(emptyMap())
            } else {
                ToolResult.failure(ToolError.GESTURE_FAILED, "ROOT command failed with exit code $exitCode")
            }
        } catch (e: Exception) {
            ToolResult.failure(ToolError.GESTURE_FAILED, e.message)
        }
    }
}
