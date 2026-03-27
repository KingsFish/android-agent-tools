package com.androidagent.tools.tools.input

import android.content.Context
import com.androidagent.tools.accessibility.AgentAccessibilityService
import com.androidagent.tools.core.*
import com.androidagent.tools.core.KeyEventConstants

/**
 * Tool to press a key by name or key code.
 */
class PressKeyTool : Tool {
    override val name = "press_key"
    override val description = "Press a key by name (e.g., 'enter', 'delete', 'tab') or key code."

    override fun validate(params: Map<String, Any?>): Result<Unit> {
        val validator = ParameterValidator(params)
        val hasKeyName = params.containsKey("key_name")
        val hasKeyCode = params.containsKey("key_code")

        return if (hasKeyName || hasKeyCode) {
            Result.Success(Unit)
        } else {
            Result.Failure(ToolError.INVALID_PARAMETER, "Either 'key_name' or 'key_code' is required")
        }
    }

    override suspend fun execute(context: Context, params: Map<String, Any?>): ToolResult {
        val validator = ParameterValidator(params)

        val keyCode = when {
            params.containsKey("key_code") -> {
                val keyCodeResult = validator.requireInt("key_code")
                if (keyCodeResult.isFailure) {
                    return ToolResult.failure(
                        (keyCodeResult as Result.Failure).error,
                        keyCodeResult.context
                    )
                }
                (keyCodeResult as Result.Success).value
            }
            params.containsKey("key_name") -> {
                val keyNameResult = validator.requireString("key_name")
                if (keyNameResult.isFailure) {
                    return ToolResult.failure(
                        (keyNameResult as Result.Failure).error,
                        keyNameResult.context
                    )
                }
                val keyName = (keyNameResult as Result.Success).value
                KeyEventConstants.fromName(keyName)
                    ?: return ToolResult.failure(ToolError.INVALID_PARAMETER, "Unknown key name: $keyName")
            }
            else -> return ToolResult.failure(ToolError.INVALID_PARAMETER, "Either 'key_name' or 'key_code' is required")
        }

        val envDetector = EnvironmentDetector(context)
        if (envDetector.hasRoot()) {
            return executeWithRoot(keyCode)
        }

        val service = AgentAccessibilityService.instance
            ?: return ToolResult.failure(ToolError.ACCESSIBILITY_SERVICE_REQUIRED)

        return when (keyCode) {
            KeyEventConstants.BACK -> if (service.pressBack()) {
                ToolResult.success(emptyMap())
            } else {
                ToolResult.failure(ToolError.KEY_EVENT_FAILED, "back")
            }
            KeyEventConstants.HOME -> if (service.pressHome()) {
                ToolResult.success(emptyMap())
            } else {
                ToolResult.failure(ToolError.KEY_EVENT_FAILED, "home")
            }
            else -> ToolResult.failure(ToolError.ROOT_REQUIRED, "Key event $keyCode requires ROOT access")
        }
    }

    private fun executeWithRoot(keyCode: Int): ToolResult {
        return try {
            val process = Runtime.getRuntime().exec(
                arrayOf("su", "-c", "input keyevent $keyCode")
            )
            val exitCode = process.waitFor()
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