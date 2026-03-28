package com.androidagent.tools.tools.input

import com.androidagent.core.*
import com.androidagent.androidapp.AndroidCapability
import com.androidagent.androidapp.AppToolContext

class PressKeyTool : Tool {
    override val name = "press_key"
    override val description = "Send a key event. Supported keys: back, home, enter, delete, volume_up, volume_down, menu, search, dpad_up, dpad_down, dpad_left, dpad_right, dpad_center."
    override val inputSchema = ToolSchema(
        properties = mapOf(
            "key_name" to SchemaProperty(type = "string", description = "Key name to press (e.g., 'enter', 'delete', 'volume_up')"),
            "key_code" to SchemaProperty(type = "integer", description = "Key code to press (Android KeyEvent code)")
        ),
        required = emptyList()
    )

    private val keyNameToCode = mapOf(
        "back" to 4, "home" to 3, "menu" to 82, "search" to 84,
        "up" to 19, "down" to 20, "left" to 21, "right" to 22, "center" to 23, "enter" to 66,
        "tab" to 61, "escape" to 111, "delete" to 67, "space" to 62,
        "volume_up" to 24, "volume_down" to 25, "volume_mute" to 164,
        "power" to 26, "camera" to 27,
        "play" to 126, "pause" to 127, "play_pause" to 85, "stop" to 86, "next" to 87, "previous" to 88
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
            keyName != null -> keyNameToCode[keyName.lowercase()]
                ?: return ToolResult.failure(ToolError.INVALID_PARAMETER, "Unknown key name: $keyName")
            else -> return ToolResult.failure(ToolError.INVALID_PARAMETER, "Either key_name or key_code is required")
        }

        val appContext = context as? AppToolContext
            ?: return ToolResult.failure(ToolError.UNSUPPORTED_OPERATION, "Requires Android context")

        if (context.hasCapability(AndroidCapability.ROOT)) {
            return executeWithRoot(actualKeyCode)
        }

        val service = appContext.getAccessibilityService()
            ?: return ToolResult.failure(ToolError.ACCESSIBILITY_SERVICE_REQUIRED)

        return if (service.pressBack()) {
            ToolResult.success(emptyMap())
        } else {
            ToolResult.failure(ToolError.KEY_EVENT_FAILED, "key_code: $actualKeyCode")
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
