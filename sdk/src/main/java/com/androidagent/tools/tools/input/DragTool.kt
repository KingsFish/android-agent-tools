package com.androidagent.tools.tools.input

import android.content.Context
import com.androidagent.tools.accessibility.AgentAccessibilityService
import com.androidagent.tools.core.*

/**
 * Tool to perform a drag gesture from one point to another.
 */
class DragTool : Tool {
    override val name = "drag"
    override val description = "Perform a drag gesture from one point to another."

    override fun validate(params: Map<String, Any?>): Result<Unit> {
        val validator = ParameterValidator(params)

        val fromXResult = validator.requireInt("from_x")
        if (fromXResult.isFailure) {
            return Result.Failure((fromXResult as Result.Failure).error, fromXResult.context)
        }

        val fromYResult = validator.requireInt("from_y")
        if (fromYResult.isFailure) {
            return Result.Failure((fromYResult as Result.Failure).error, fromYResult.context)
        }

        val toXResult = validator.requireInt("to_x")
        if (toXResult.isFailure) {
            return Result.Failure((toXResult as Result.Failure).error, toXResult.context)
        }

        val toYResult = validator.requireInt("to_y")
        if (toYResult.isFailure) {
            return Result.Failure((toYResult as Result.Failure).error, toYResult.context)
        }

        return Result.Success(Unit)
    }

    override suspend fun execute(context: Context, params: Map<String, Any?>): ToolResult {
        val validator = ParameterValidator(params)
        val fromX = (validator.requireInt("from_x") as Result.Success).value
        val fromY = (validator.requireInt("from_y") as Result.Success).value
        val toX = (validator.requireInt("to_x") as Result.Success).value
        val toY = (validator.requireInt("to_y") as Result.Success).value
        val duration = validator.optionalInt("duration", 500).toLong()

        val envDetector = EnvironmentDetector(context)
        if (envDetector.hasRoot()) {
            return executeWithRoot(fromX, fromY, toX, toY, duration)
        }

        val service = AgentAccessibilityService.instance
            ?: return ToolResult.failure(ToolError.ACCESSIBILITY_SERVICE_REQUIRED)

        val success = service.performDrag(fromX, fromY, toX, toY, duration)
        return if (success) {
            ToolResult.success(emptyMap())
        } else {
            ToolResult.failure(ToolError.GESTURE_FAILED, "Drag from ($fromX, $fromY) to ($toX, $toY)")
        }
    }

    private fun executeWithRoot(fromX: Int, fromY: Int, toX: Int, toY: Int, duration: Long): ToolResult {
        return try {
            val process = Runtime.getRuntime().exec(
                arrayOf("su", "-c", "input swipe $fromX $fromY $toX $toY $duration")
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