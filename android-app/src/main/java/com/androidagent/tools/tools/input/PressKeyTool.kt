package com.androidagent.tools.tools.input

import com.androidagent.core.*
import com.androidagent.androidapp.AndroidCapability
import com.androidagent.androidapp.AppToolContext
import com.androidagent.androidapp.KeyEventConstants

class PressKeyTool : Tool {
    override val name = "press_key"
    override val description = "Send a key event. Supported keys: back, home, enter, delete, volume_up, volume_down, menu, search, dpad_up, dpad_down, dpad_left, dpad_right, dpad_center. Also supports aliases: vol_up, vol_down, esc, prev, del, backspace, mute."
    override val inputSchema = ToolSchema(
        properties = mapOf(
            "key_name" to SchemaProperty(type = "string", description = "Key name to press (e.g., 'enter', 'delete', 'volume_up')"),
            "key_code" to SchemaProperty(type = "integer", description = "Key code to press (Android KeyEvent code)")
        ),
        required = emptyList()
    )

    override fun validate(params: Map<String, Any?>): ValidationResult {
        val validator = ParameterValidator(params)
        val keyName = validator.getString("key_name")
        val keyCode = validator.getInt("key_code")

        return if (keyName != null || keyCode != null) {
            ValidationResult.success()
        } else {
            ValidationResult.failure(ToolError.INVALID_PARAMETER, "Either key_name or key_code is required")
        }
    }

    override suspend fun execute(context: ToolContext, params: Map<String, Any?>): ToolResult {
        val validator = ParameterValidator(params)
        val keyName = validator.getString("key_name")
        val keyCode = validator.getInt("key_code")

        val actualKeyCode = when {
            keyCode != null -> keyCode
            keyName != null -> KeyEventConstants.fromName(keyName)
                ?: return ToolResult.failure(ToolError.INVALID_PARAMETER, "Unknown key name: $keyName")
            else -> return ToolResult.failure(ToolError.INVALID_PARAMETER, "Either key_name or key_code is required")
        }

        val appContext = context as? AppToolContext
            ?: return ToolResult.failure(ToolError.UNSUPPORTED_OPERATION, "Requires Android context")

        if (context.hasCapability(AndroidCapability.ROOT)) {
            return executeWithRoot(actualKeyCode)
        }

        // Without ROOT, try accessibility service for supported global actions
        val service = appContext.getAccessibilityService()
            ?: return ToolResult.failure(ToolError.ACCESSIBILITY_SERVICE_REQUIRED)

        return executeWithAccessibility(service, actualKeyCode)
    }

    private fun executeWithAccessibility(
        service: com.androidagent.tools.accessibility.AgentAccessibilityService,
        keyCode: Int
    ): ToolResult {
        val success = when (keyCode) {
            KeyEventConstants.BACK -> service.pressBack()
            KeyEventConstants.HOME -> service.pressHome()
            // Keys that require ROOT - return appropriate error
            else -> return ToolResult.failure(
                ToolError.ROOT_REQUIRED,
                "Key code $keyCode requires ROOT access. Only BACK and HOME are supported via accessibility service."
            )
        }

        return if (success) {
            ToolResult.success(emptyMap())
        } else {
            ToolResult.failure(ToolError.KEY_EVENT_FAILED, "Failed to execute key event: $keyCode")
        }
    }

    private fun executeWithRoot(keyCode: Int): ToolResult {
        return try {
            val proc = java.lang.Runtime.getRuntime().exec(arrayOf("su", "-c", "input keyevent $keyCode"))
            val exitCode = proc.waitFor()
            if (exitCode == 0) {
                ToolResult.success(emptyMap())
            } else {
                ToolResult.failure(ToolError.KEY_EVENT_FAILED, "ROOT command failed with exit code $exitCode")
            }
        } catch (e: Exception) {
            ToolResult.failure(ToolError.KEY_EVENT_FAILED, e.message)
        }
    }
}