package com.androidagent.tools.tools.input

import com.androidagent.core.*
import com.androidagent.androidapp.AndroidCapability
import com.androidagent.androidapp.AppToolContext

class LongPressTool : Tool {
    override val name = "long_press"
    override val description = "Perform a long press at the specified coordinates."
    override val inputSchema = ToolSchema(
        properties = mapOf(
            "x" to SchemaProperty(type = "integer", description = "X coordinate"),
            "y" to SchemaProperty(type = "integer", description = "Y coordinate"),
            "duration" to SchemaProperty(type = "integer", description = "Duration in milliseconds (default: 1000)", default = 1000)
        ),
        required = listOf("x", "y")
    )

    override fun validate(params: Map<String, Any?>): ValidationResult {
        val validator = ParameterValidator(params)
        val xResult = validator.requireInt("x")
        if (xResult.isFailure) return xResult
        return validator.requireInt("y")
    }

    override suspend fun execute(context: ToolContext, params: Map<String, Any?>): ToolResult {
        val validator = ParameterValidator(params)
        val x = validator.getInt("x") ?: return ToolResult.failure(ToolError.INVALID_PARAMETER, "Missing x")
        val y = validator.getInt("y") ?: return ToolResult.failure(ToolError.INVALID_PARAMETER, "Missing y")
        val duration = validator.optionalLong("duration", 1000)

        val appContext = context as? AppToolContext
            ?: return ToolResult.failure(ToolError.UNSUPPORTED_OPERATION, "Requires Android context")

        if (context.hasCapability(AndroidCapability.ROOT)) {
            return executeWithRoot(x, y, duration)
        }

        val service = appContext.getAccessibilityService()
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
            val proc = java.lang.Runtime.getRuntime().exec(arrayOf("su", "-c", "input swipe $x $y $x $y $duration"))
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
