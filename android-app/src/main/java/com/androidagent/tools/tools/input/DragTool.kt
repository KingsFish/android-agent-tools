package com.androidagent.tools.tools.input

import com.androidagent.core.*
import com.androidagent.androidapp.AndroidCapability
import com.androidagent.androidapp.AppToolContext

class DragTool : Tool {
    override val name = "drag"
    override val description = "Perform a drag gesture from one point to another."
    override val inputSchema = ToolSchema(
        properties = mapOf(
            "from_x" to SchemaProperty(type = "integer", description = "Start X coordinate"),
            "from_y" to SchemaProperty(type = "integer", description = "Start Y coordinate"),
            "to_x" to SchemaProperty(type = "integer", description = "End X coordinate"),
            "to_y" to SchemaProperty(type = "integer", description = "End Y coordinate"),
            "duration" to SchemaProperty(type = "integer", description = "Duration in milliseconds (default: 500)", default = 500)
        ),
        required = listOf("from_x", "from_y", "to_x", "to_y")
    )

    override fun validate(params: Map<String, Any?>): ValidationResult {
        val validator = ParameterValidator(params)
        val fromXResult = validator.requireInt("from_x")
        if (fromXResult.isFailure) return fromXResult
        val fromYResult = validator.requireInt("from_y")
        if (fromYResult.isFailure) return fromYResult
        val toXResult = validator.requireInt("to_x")
        if (toXResult.isFailure) return toXResult
        return validator.requireInt("to_y")
    }

    override suspend fun execute(context: ToolContext, params: Map<String, Any?>): ToolResult {
        val validator = ParameterValidator(params)
        val fromX = validator.getInt("from_x") ?: return ToolResult.failure(ToolError.INVALID_PARAMETER, "Missing from_x")
        val fromY = validator.getInt("from_y") ?: return ToolResult.failure(ToolError.INVALID_PARAMETER, "Missing from_y")
        val toX = validator.getInt("to_x") ?: return ToolResult.failure(ToolError.INVALID_PARAMETER, "Missing to_x")
        val toY = validator.getInt("to_y") ?: return ToolResult.failure(ToolError.INVALID_PARAMETER, "Missing to_y")
        val duration = validator.optionalLong("duration", 500)

        val appContext = context as? AppToolContext
            ?: return ToolResult.failure(ToolError.UNSUPPORTED_OPERATION, "Requires Android context")

        if (context.hasCapability(AndroidCapability.ROOT)) {
            return executeWithRoot(fromX, fromY, toX, toY, duration)
        }

        val service = appContext.getAccessibilityService()
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
            val proc = java.lang.Runtime.getRuntime().exec(arrayOf("su", "-c", "input swipe $fromX $fromY $toX $toY $duration"))
            val exitCode = proc.waitFor()
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
