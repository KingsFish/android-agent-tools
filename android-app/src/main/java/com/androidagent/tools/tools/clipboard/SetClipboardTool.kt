package com.androidagent.tools.tools.clipboard

import com.androidagent.core.*
import com.androidagent.androidapp.AppToolContext

class SetClipboardTool : Tool {
    override val name = "set_clipboard"
    override val description = "Set the clipboard content."
    override val inputSchema = ToolSchema(
        properties = mapOf(
            "text" to SchemaProperty(type = "string", description = "Text to set in clipboard")
        ),
        required = listOf("text")
    )

    override fun validate(params: Map<String, Any?>): ValidationResult {
        val validator = ParameterValidator(params)
        return validator.requireString("text")
    }

    override suspend fun execute(context: ToolContext, params: Map<String, Any?>): ToolResult {
        val appContext = context as? AppToolContext
            ?: return ToolResult.failure(ToolError.UNSUPPORTED_OPERATION, "Requires Android context")

        val service = appContext.getAccessibilityService()
            ?: return ToolResult.failure(ToolError.ACCESSIBILITY_SERVICE_REQUIRED)

        val validator = ParameterValidator(params)
        val text = validator.getString("text")
            ?: return ToolResult.failure(ToolError.INVALID_PARAMETER, "Missing text")

        val success = service.setClipboard(text)
        return if (success) {
            ToolResult.success(emptyMap())
        } else {
            ToolResult.failure(ToolError.CLIPBOARD_FAILED, "Failed to set clipboard")
        }
    }
}
