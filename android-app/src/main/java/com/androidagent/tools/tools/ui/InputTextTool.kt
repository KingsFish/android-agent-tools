package com.androidagent.tools.tools.ui

import com.androidagent.core.*
import com.androidagent.androidapp.AndroidCapability
import com.androidagent.androidapp.AppToolContext

class InputTextTool : Tool {
    override val name = "input_text"
    override val description = "Input text into the currently focused field."
    override val inputSchema = ToolSchema(
        properties = mapOf(
            "text" to SchemaProperty(type = "string", description = "Text to input")
        ),
        required = listOf("text")
    )

    override fun validate(params: Map<String, Any?>): ValidationResult {
        val validator = ParameterValidator(params)
        return validator.requireNonEmptyString("text")
    }

    override suspend fun execute(context: ToolContext, params: Map<String, Any?>): ToolResult {
        val validator = ParameterValidator(params)
        val text = validator.getString("text")
            ?: return ToolResult.failure(ToolError.INVALID_PARAMETER, "Missing text")

        val appContext = context as? AppToolContext
            ?: return ToolResult.failure(ToolError.UNSUPPORTED_OPERATION, "Requires Android context")

        if (context.hasCapability(AndroidCapability.ROOT)) {
            return executeWithRoot(text)
        }

        val service = appContext.getAccessibilityService()
            ?: return ToolResult.failure(ToolError.ACCESSIBILITY_SERVICE_REQUIRED)

        val success = service.performInputText(text)
        return if (success) {
            ToolResult.success(emptyMap())
        } else {
            ToolResult.failure(ToolError.GESTURE_FAILED, "Input text: $text")
        }
    }

    private fun executeWithRoot(text: String): ToolResult {
        return try {
            val escapedText = text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\$", "\\\$")
                .replace("`", "\\`")
                .replace("\n", "\\n")
                .replace("\r", "\\r")

            val process = Runtime.getRuntime().exec(
                arrayOf("su", "-c", "input text \"$escapedText\"")
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