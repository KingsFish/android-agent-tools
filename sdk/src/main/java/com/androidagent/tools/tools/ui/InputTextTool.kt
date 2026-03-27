package com.androidagent.tools.tools.ui

import android.content.Context
import com.androidagent.tools.accessibility.AgentAccessibilityService
import com.androidagent.tools.core.*

class InputTextTool : Tool {
    override val name = "input_text"
    override val description = "Input text into the currently focused field."

    override fun validate(params: Map<String, Any?>): Result<Unit> {
        val validator = ParameterValidator(params)
        return when (val result = validator.requireNonEmptyString("text")) {
            is Result.Success -> Result.Success(Unit)
            is Result.Failure -> Result.Failure(result.error, result.context)
        }
    }

    override suspend fun execute(context: Context, params: Map<String, Any?>): ToolResult {
        val validator = ParameterValidator(params)
        val text = (validator.requireNonEmptyString("text") as Result.Success).value

        val envDetector = EnvironmentDetector(context)
        if (envDetector.hasRoot()) {
            return executeWithRoot(text)
        }

        val service = AgentAccessibilityService.instance
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
            // Properly escape all shell special characters to prevent command injection
            // This includes: backslash, double quotes, dollar sign, backticks, newlines
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
