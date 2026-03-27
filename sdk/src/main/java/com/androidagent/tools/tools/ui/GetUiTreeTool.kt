package com.androidagent.tools.tools.ui

import android.content.Context
import com.androidagent.tools.accessibility.AgentAccessibilityService
import com.androidagent.tools.core.*

class GetUiTreeTool : Tool {
    override val name = "get_ui_tree"
    override val description = "Get the UI hierarchy tree of current screen."

    override fun validate(params: Map<String, Any?>): Result<Unit> {
        return Result.Success(Unit)
    }

    override suspend fun execute(context: Context, params: Map<String, Any?>): ToolResult {
        val service = AgentAccessibilityService.instance
            ?: return ToolResult.failure(ToolError.ACCESSIBILITY_SERVICE_REQUIRED)

        val validator = ParameterValidator(params)
        val maxDepth = validator.optionalInt("max_depth", 10)
        val includeInvisible = validator.optionalBoolean("include_invisible", false)

        val uiTree = service.getUiTree(maxDepth, includeInvisible)
            ?: return ToolResult.failure(ToolError.UI_TREE_FAILED, "Failed to get UI tree")

        return ToolResult.success(uiTree)
    }
}