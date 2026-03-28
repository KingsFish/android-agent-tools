package com.androidagent.tools.tools.wait

import com.androidagent.core.*
import com.androidagent.androidapp.AppToolContext

class WaitForElementTool : Tool {
    override val name = "wait_for_element"
    override val description = "Wait for an element with specific text to appear on the screen."
    override val inputSchema = ToolSchema(
        properties = mapOf(
            "text" to SchemaProperty(type = "string", description = "Text to search for"),
            "timeout" to SchemaProperty(type = "integer", description = "Timeout in milliseconds (default: 5000)", default = 5000)
        ),
        required = listOf("text")
    )

    override fun validate(params: Map<String, Any?>): ValidationResult {
        val validator = ParameterValidator(params)
        return validator.requireNonEmptyString("text")
    }

    override suspend fun execute(context: ToolContext, params: Map<String, Any?>): ToolResult {
        val appContext = context as? AppToolContext
            ?: return ToolResult.failure(ToolError.UNSUPPORTED_OPERATION, "Requires Android context")

        val service = appContext.getAccessibilityService()
            ?: return ToolResult.failure(ToolError.ACCESSIBILITY_SERVICE_REQUIRED)

        val validator = ParameterValidator(params)
        val text = validator.getString("text")
            ?: return ToolResult.failure(ToolError.INVALID_PARAMETER, "Missing text")
        val timeout = validator.optionalLong("timeout", 5000)

        val success = service.waitForElement(text, timeout)
        return if (success) {
            ToolResult.success(emptyMap())
        } else {
            ToolResult.failure(ToolError.ELEMENT_NOT_FOUND, "Element with text '$text' not found within ${timeout}ms")
        }
    }
}
