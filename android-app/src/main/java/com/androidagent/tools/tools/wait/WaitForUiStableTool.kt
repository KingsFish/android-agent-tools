package com.androidagent.tools.tools.wait

import com.androidagent.core.*
import com.androidagent.androidapp.AppToolContext

class WaitForUiStableTool : Tool {
    override val name = "wait_for_ui_stable"
    override val description = "Wait for the UI to become stable (no changes in UI tree)."
    override val inputSchema = ToolSchema(
        properties = mapOf(
            "timeout" to SchemaProperty(type = "integer", description = "Timeout in milliseconds (default: 5000)", default = 5000)
        ),
        required = emptyList()
    )

    override fun validate(params: Map<String, Any?>): ValidationResult = ValidationResult.success()

    override suspend fun execute(context: ToolContext, params: Map<String, Any?>): ToolResult {
        val appContext = context as? AppToolContext
            ?: return ToolResult.failure(ToolError.UNSUPPORTED_OPERATION, "Requires Android context")

        val service = appContext.getAccessibilityService()
            ?: return ToolResult.failure(ToolError.ACCESSIBILITY_SERVICE_REQUIRED)

        val validator = ParameterValidator(params)
        val timeout = validator.optionalLong("timeout", 5000)

        val success = service.waitForUiStable(timeout)
        return if (success) {
            ToolResult.success(emptyMap())
        } else {
            ToolResult.failure(ToolError.WAIT_TIMEOUT, "UI did not stabilize within ${timeout}ms")
        }
    }
}
