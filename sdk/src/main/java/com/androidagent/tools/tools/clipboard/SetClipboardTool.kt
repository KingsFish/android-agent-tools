package com.androidagent.tools.tools.clipboard

import android.content.Context
import com.androidagent.tools.accessibility.AgentAccessibilityService
import com.androidagent.tools.core.*

/**
 * Tool to set clipboard content.
 */
class SetClipboardTool : Tool {
    override val name = "set_clipboard"
    override val description = "Set text content to the clipboard."

    override fun validate(params: Map<String, Any?>): Result<Unit> {
        val validator = ParameterValidator(params)
        val textResult = validator.requireString("text")
        return when (textResult) {
            is Result.Success -> Result.Success(Unit)
            is Result.Failure -> Result.Failure(textResult.error, textResult.context)
        }
    }

    override suspend fun execute(context: Context, params: Map<String, Any?>): ToolResult {
        val validator = ParameterValidator(params)
        val text = (validator.requireString("text") as Result.Success).value

        val service = AgentAccessibilityService.instance
            ?: return ToolResult.failure(ToolError.ACCESSIBILITY_SERVICE_REQUIRED)

        val success = service.setClipboard(text)

        return if (success) {
            ToolResult.success(mapOf("text" to text))
        } else {
            ToolResult.failure(ToolError.CLIPBOARD_FAILED, "Failed to set clipboard")
        }
    }
}