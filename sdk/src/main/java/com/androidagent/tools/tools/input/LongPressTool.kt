package com.androidagent.tools.tools.input

import android.content.Context
import com.androidagent.tools.accessibility.AgentAccessibilityService
import com.androidagent.tools.core.*

/**
 * Tool to perform a long press at specified coordinates.
 */
class LongPressTool : Tool {
    override val name = "long_press"
    override val description = "Perform a long press at the specified coordinates."

    override fun validate(params: Map<String, Any?>): Result<Unit> {
        val validator = ParameterValidator(params)

        val xResult = validator.requireInt("x")
        if (xResult.isFailure) {
            return Result.Failure((xResult as Result.Failure).error, xResult.context)
        }

        val yResult = validator.requireInt("y")
        if (yResult.isFailure) {
            return Result.Failure((yResult as Result.Failure).error, yResult.context)
        }

        return Result.Success(Unit)
    }

    override suspend fun execute(context: Context, params: Map<String, Any?>): ToolResult {
        val validator = ParameterValidator(params)
        val x = (validator.requireInt("x") as Result.Success).value
        val y = (validator.requireInt("y") as Result.Success).value
        val duration = validator.optionalInt("duration", 1000).toLong()

        val envDetector = EnvironmentDetector(context)
        if (envDetector.hasRoot()) {
            return executeWithRoot(x, y, duration)
        }

        val service = AgentAccessibilityService.instance
            ?: return ToolResult.failure(ToolError.ACCESSIBILITY_SERVICE_REQUIRED)

        val success = service.performLongPress(x, y, duration)
        return if (success) {
            ToolResult.success(emptyMap())
        } else {
            ToolResult.failure(ToolError.GESTURE_FAILED, "Long press at ($x, $y)")
        }
    }

    private fun executeWithRoot(x: Int, y: Int, duration: Long): ToolResult {
        return try {
            val process = Runtime.getRuntime().exec(
                arrayOf("su", "-c", "input swipe $x $y $x $y $duration")
            )
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