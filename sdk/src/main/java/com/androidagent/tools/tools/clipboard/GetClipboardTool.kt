package com.androidagent.tools.tools.clipboard

import android.content.Context
import com.androidagent.tools.accessibility.AgentAccessibilityService
import com.androidagent.tools.core.*

/**
 * Tool to get clipboard content.
 */
class GetClipboardTool : Tool {
    override val name = "get_clipboard"
    override val description = "Get the current text content from the clipboard."

    override fun validate(params: Map<String, Any?>): Result<Unit> = Result.Success(Unit)

    override suspend fun execute(context: Context, params: Map<String, Any?>): ToolResult {
        val service = AgentAccessibilityService.instance
            ?: return ToolResult.failure(ToolError.ACCESSIBILITY_SERVICE_REQUIRED)

        val content = service.getClipboard()
            ?: return ToolResult.success(mapOf("text" to "", "has_content" to false))

        return ToolResult.success(mapOf(
            "text" to content,
            "has_content" to true
        ))
    }
}