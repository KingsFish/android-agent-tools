package com.androidagent.tools.tools.ui

import com.androidagent.core.*
import com.androidagent.androidapp.AppToolContext

class GetUiTreeTool : Tool {
    override val name = "get_ui_tree"
    override val description = "Get the UI hierarchy tree of current screen."
    override val inputSchema = ToolSchema(
        properties = mapOf(
            "max_depth" to SchemaProperty(type = "integer", description = "Maximum depth to traverse (default: 10)", default = 10),
            "include_invisible" to SchemaProperty(type = "boolean", description = "Whether to include invisible nodes (default: false)", default = false)
        ),
        required = emptyList()
    )

    override fun validate(params: Map<String, Any?>): ValidationResult {
        return ValidationResult.success()
    }

    override suspend fun execute(context: ToolContext, params: Map<String, Any?>): ToolResult {
        val appContext = context as? AppToolContext
            ?: return ToolResult.failure(ToolError.UNSUPPORTED_OPERATION, "Requires Android context")

        val service = appContext.getAccessibilityService()
            ?: return ToolResult.failure(ToolError.ACCESSIBILITY_SERVICE_REQUIRED)

        val validator = ParameterValidator(params)
        val maxDepth = validator.optionalInt("max_depth", 10)
        val includeInvisible = validator.optionalBoolean("include_invisible", false)

        val uiTree = service.getUiTree(maxDepth, includeInvisible)
            ?: return ToolResult.failure(ToolError.UI_TREE_FAILED, "Failed to get UI tree")

        return ToolResult.success(uiTree)
    }
}