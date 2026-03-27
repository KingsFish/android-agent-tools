package com.androidagent.tools.tools.ui

import android.content.Context
import com.androidagent.tools.accessibility.AgentAccessibilityService
import com.androidagent.tools.core.*

class InputTextTool : Tool {
    override val name = "input_text"
    override val description = "Input text into the currently focused field."

    override fun validate(params: Map<String, Any?>): Result<Unit> {
        val validator = ParameterValidator(params)
        return when (val result = validator.requireString("text")) {
            is Result.Success -> Result.Success(Unit)
            is Result.Failure -> result
        }
    }

    override suspend fun execute(context: Context, params: Map<String, Any?>): ToolResult {
        val validator = ParameterValidator(params)
        val text = (validator.requireString("text") as Result.Success).value

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
            val escapedText = text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\$", "\\\$")

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
