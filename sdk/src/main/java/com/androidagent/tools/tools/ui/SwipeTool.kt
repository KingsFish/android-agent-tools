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
        if (startXResult.isFailure) return startXResult as Result.Failure

        val startYResult = validator.requireInt("start_y")
        if (startYResult.isFailure) return startYResult as Result.Failure

        val endXResult = validator.requireInt("end_x")
        if (endXResult.isFailure) return endXResult as Result.Failure

        val endYResult = validator.requireInt("end_y")
        if (endYResult.isFailure) return endYResult as Result.Failure

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
