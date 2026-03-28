package com.androidagent.tools.tools.node

import com.androidagent.core.*
import com.androidagent.androidapp.AppToolContext

class ClickNodeByIdTool : Tool {
    override val name = "click_node_by_id"
    override val description = "Click a UI node by its resource ID."
    override val inputSchema = ToolSchema(
        properties = mapOf(
            "resource_id" to SchemaProperty(type = "string", description = "Resource ID to search for")
        ),
        required = listOf("resource_id")
    )

    override fun validate(params: Map<String, Any?>): ValidationResult {
        val validator = ParameterValidator(params)
        return validator.requireString("resource_id")
    }

    override suspend fun execute(context: ToolContext, params: Map<String, Any?>): ToolResult {
        val appContext = context as? AppToolContext
            ?: return ToolResult.failure(ToolError.UNSUPPORTED_OPERATION, "Requires Android context")

        val service = appContext.getAccessibilityService()
            ?: return ToolResult.failure(ToolError.ACCESSIBILITY_SERVICE_REQUIRED)

        val validator = ParameterValidator(params)
        val resourceId = validator.getString("resource_id")
            ?: return ToolResult.failure(ToolError.INVALID_PARAMETER, "Missing resource_id")

        val success = service.clickNodeById(resourceId)
        return if (success) {
            ToolResult.success(emptyMap())
        } else {
            ToolResult.failure(ToolError.ELEMENT_NOT_FOUND, "Node with resource ID '$resourceId' not found or not clickable")
        }
    }
}
