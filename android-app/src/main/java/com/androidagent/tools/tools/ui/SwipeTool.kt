package com.androidagent.tools.tools.ui

import com.androidagent.core.*
import com.androidagent.androidapp.AndroidCapability
import com.androidagent.androidapp.AppToolContext

class SwipeTool : Tool {
    override val name = "swipe"
    override val description = "Perform a swipe gesture."
    override val inputSchema = ToolSchema(
        properties = mapOf(
            "start_x" to SchemaProperty(type = "integer", description = "Start X coordinate"),
            "start_y" to SchemaProperty(type = "integer", description = "Start Y coordinate"),
            "end_x" to SchemaProperty(type = "integer", description = "End X coordinate"),
            "end_y" to SchemaProperty(type = "integer", description = "End Y coordinate"),
            "duration" to SchemaProperty(type = "integer", description = "Duration in milliseconds (default: 300)", default = 300)
        ),
        required = listOf("start_x", "start_y", "end_x", "end_y")
    )

    override fun validate(params: Map<String, Any?>): ValidationResult {
        val validator = ParameterValidator(params)
        val startXResult = validator.requireInt("start_x")
        if (startXResult.isFailure) return startXResult
        val startYResult = validator.requireInt("start_y")
        if (startYResult.isFailure) return startYResult
        val endXResult = validator.requireInt("end_x")
        if (endXResult.isFailure) return endXResult
        return validator.requireInt("end_y")
    }

    override suspend fun execute(context: ToolContext, params: Map<String, Any?>): ToolResult {
        val validator = ParameterValidator(params)
        val startX = validator.getInt("start_x") ?: return ToolResult.failure(ToolError.INVALID_PARAMETER, "Missing start_x")
        val startY = validator.getInt("start_y") ?: return ToolResult.failure(ToolError.INVALID_PARAMETER, "Missing start_y")
        val endX = validator.getInt("end_x") ?: return ToolResult.failure(ToolError.INVALID_PARAMETER, "Missing end_x")
        val endY = validator.getInt("end_y") ?: return ToolResult.failure(ToolError.INVALID_PARAMETER, "Missing end_y")
        val duration = validator.optionalInt("duration", 300).toLong()

        val appContext = context as? AppToolContext
            ?: return ToolResult.failure(ToolError.UNSUPPORTED_OPERATION, "Requires Android context")

        if (context.hasCapability(AndroidCapability.ROOT)) {
            return executeWithRoot(startX, startY, endX, endY, duration)
        }

        val service = appContext.getAccessibilityService()
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