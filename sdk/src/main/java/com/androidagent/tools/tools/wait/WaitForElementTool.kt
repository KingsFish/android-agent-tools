package com.androidagent.tools.tools.wait

import android.content.Context
import com.androidagent.tools.accessibility.AgentAccessibilityService
import com.androidagent.tools.core.*

/**
 * Tool to wait for an element with specific text to appear.
 */
class WaitForElementTool : Tool {
    override val name = "wait_for_element"
    override val description = "Wait for an element with specific text to appear on screen."

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
        val timeout = validator.optionalInt("timeout", 5000).toLong()

        val service = AgentAccessibilityService.instance
            ?: return ToolResult.failure(ToolError.ACCESSIBILITY_SERVICE_REQUIRED)

        val success = service.waitForElement(text, timeout)

        return if (success) {
            ToolResult.success(mapOf("found" to true, "text" to text))
        } else {
            ToolResult.failure(ToolError.ELEMENT_NOT_FOUND, "Element with text '$text' not found within ${timeout}ms")
        }
    }
}