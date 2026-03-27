package com.androidagent.tools.tools.ui

import android.content.Context
import com.androidagent.tools.accessibility.AgentAccessibilityService
import com.androidagent.tools.core.*

class SwipeTool : Tool {
    override val name = "swipe"
    override val description = "Perform a swipe gesture."

    override fun validate(params: Map<String, Any?>): Result<Unit> {
        val validator = ParameterValidator(params)

        val startXResult = validator.requireInt("start_x")
        if (startXResult.isFailure) {
            val f = startXResult as Result.Failure
            return Result.Failure(f.error, f.context)
        }

        val startYResult = validator.requireInt("start_y")
        if (startYResult.isFailure) {
            val f = startYResult as Result.Failure
            return Result.Failure(f.error, f.context)
        }

        val endXResult = validator.requireInt("end_x")
        if (endXResult.isFailure) {
            val f = endXResult as Result.Failure
            return Result.Failure(f.error, f.context)
        }

        val endYResult = validator.requireInt("end_y")
        if (endYResult.isFailure) {
            val f = endYResult as Result.Failure
            return Result.Failure(f.error, f.context)
        }

        return Result.Success(Unit)
    }

    override suspend fun execute(context: Context, params: Map<String, Any?>): ToolResult {
        val validator = ParameterValidator(params)
        val startX = (validator.requireInt("start_x") as Result.Success).value
        val startY = (validator.requireInt("start_y") as Result.Success).value
        val endX = (validator.requireInt("end_x") as Result.Success).value
        val endY = (validator.requireInt("end_y") as Result.Success).value
        val duration = validator.optionalInt("duration", 300).toLong()

        val envDetector = EnvironmentDetector(context)
        if (envDetector.hasRoot()) {
            return executeWithRoot(startX, startY, endX, endY, duration)
        }

        val service = AgentAccessibilityService.instance
            ?: return ToolResult.failure(ToolError.ACCESSIBILITY_SERVICE_REQUIRED)

        val success = service.performSwipe(startX, startY, endX, endY, duration)
        return if (success) {
            ToolResult.success(emptyMap())
        } else {
            ToolResult.failure(ToolError.GESTURE_FAILED, "Swipe from ($startX, $startY) to ($endX, $endY)")
        }
    }

    private fun executeWithRoot(startX: Int, startY: Int, endX: Int, endY: Int, duration: Long): ToolResult {
        return try {
            val process = Runtime.getRuntime().exec(
                arrayOf("su", "-c", "input swipe $startX $startY $endX $endY $duration")
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
