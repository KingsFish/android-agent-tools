package com.androidagent.tools.tools.node

import android.content.Context
import com.androidagent.tools.accessibility.AgentAccessibilityService
import com.androidagent.tools.core.*

/**
 * Tool to click a node by its text content.
 */
class ClickNodeByTextTool : Tool {
    override val name = "click_node_by_text"
    override val description = "Click a UI element by its text content."

    override fun validate(params: Map<String, Any?>): Result<Unit> {
        val validator = ParameterValidator(params)
        val textResult = validator.requireNonEmptyString("text")
        return when (textResult) {
            is Result.Success -> Result.Success(Unit)
            is Result.Failure -> Result.Failure(textResult.error, textResult.context)
        }
    }

    override suspend fun execute(context: Context, params: Map<String, Any?>): ToolResult {
        val validator = ParameterValidator(params)
        val text = (validator.requireNonEmptyString("text") as Result.Success).value
        val exact = validator.optionalBoolean("exact", false)

        val service = AgentAccessibilityService.instance
            ?: return ToolResult.failure(ToolError.ACCESSIBILITY_SERVICE_REQUIRED)

        val success = service.clickNodeByText(text, exact)

        return if (success) {
            ToolResult.success(mapOf("clicked" to true, "text" to text))
        } else {
            ToolResult.failure(ToolError.ELEMENT_NOT_FOUND, "Element with text '$text' not found or not clickable")
        }
    }
}