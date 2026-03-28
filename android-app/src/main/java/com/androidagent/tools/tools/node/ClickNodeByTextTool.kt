package com.androidagent.tools.tools.node

import com.androidagent.core.*
import com.androidagent.androidapp.AppToolContext

class ClickNodeByTextTool : Tool {
    override val name = "click_node_by_text"
    override val description = "Click a UI node by its text content."
    override val inputSchema = ToolSchema(
        properties = mapOf(
            "text" to SchemaProperty(type = "string", description = "Text to search for"),
            "exact" to SchemaProperty(type = "boolean", description = "Whether to match exact text (default: false)", default = false)
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
        val exact = validator.optionalBoolean("exact", false)

        val success = service.clickNodeByText(text, exact)
        return if (success) {
            ToolResult.success(emptyMap())
        } else {
            ToolResult.failure(ToolError.ELEMENT_NOT_FOUND, "Node with text '$text' not found or not clickable")
        }
    }
}
