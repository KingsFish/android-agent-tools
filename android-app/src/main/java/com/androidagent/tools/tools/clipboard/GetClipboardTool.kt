package com.androidagent.tools.tools.clipboard

import com.androidagent.core.*
import com.androidagent.androidapp.AppToolContext

class GetClipboardTool : Tool {
    override val name = "get_clipboard"
    override val description = "Get the current clipboard content."
    override val inputSchema = ToolSchema.noParams()

    override fun validate(params: Map<String, Any?>): ValidationResult = ValidationResult.success()

    override suspend fun execute(context: ToolContext, params: Map<String, Any?>): ToolResult {
        val appContext = context as? AppToolContext
            ?: return ToolResult.failure(ToolError.UNSUPPORTED_OPERATION, "Requires Android context")

        val service = appContext.getAccessibilityService()
            ?: return ToolResult.failure(ToolError.ACCESSIBILITY_SERVICE_REQUIRED)

        val content = service.getClipboard()
            ?: return ToolResult.failure(ToolError.CLIPBOARD_FAILED, "Cannot get clipboard content")

        return ToolResult.success(mapOf("content" to content))
    }
}
