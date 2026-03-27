package com.androidagent.tools.tools.node

import android.content.Context
import com.androidagent.tools.accessibility.AgentAccessibilityService
import com.androidagent.tools.core.*

/**
 * Tool to click a node by its resource ID.
 */
class ClickNodeByIdTool : Tool {
    override val name = "click_node_by_id"
    override val description = "Click a UI element by its resource ID (e.g., 'com.example:id/button')."

    override fun validate(params: Map<String, Any?>): Result<Unit> {
        val validator = ParameterValidator(params)
        val idResult = validator.requireNonEmptyString("resource_id")
        return when (idResult) {
            is Result.Success -> Result.Success(Unit)
            is Result.Failure -> Result.Failure(idResult.error, idResult.context)
        }
    }

    override suspend fun execute(context: Context, params: Map<String, Any?>): ToolResult {
        val validator = ParameterValidator(params)
        val resourceId = (validator.requireNonEmptyString("resource_id") as Result.Success).value

        val service = AgentAccessibilityService.instance
            ?: return ToolResult.failure(ToolError.ACCESSIBILITY_SERVICE_REQUIRED)

        val success = service.clickNodeById(resourceId)

        return if (success) {
            ToolResult.success(mapOf("clicked" to true, "resource_id" to resourceId))
        } else {
            ToolResult.failure(ToolError.ELEMENT_NOT_FOUND, "Element with resource_id '$resourceId' not found or not clickable")
        }
    }
}