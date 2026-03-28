package com.androidagent.tools.tools.ui

import com.androidagent.core.*
import com.androidagent.androidapp.AndroidCapability
import com.androidagent.androidapp.AppToolContext

class TapTool : Tool {
    override val name = "tap"
    override val description = "Perform a tap at the specified coordinates."
    override val inputSchema = ToolSchema(
        properties = mapOf(
            "x" to SchemaProperty(type = "integer", description = "X coordinate (absolute pixels)"),
            "y" to SchemaProperty(type = "integer", description = "Y coordinate (absolute pixels)")
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

        val appContext = context as? AppToolContext
            ?: return ToolResult.failure(ToolError.UNSUPPORTED_OPERATION, "Requires Android context")

        if (context.hasCapability(AndroidCapability.ROOT)) {
            return executeWithRoot(x, y)
        }

        val service = appContext.getAccessibilityService()
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