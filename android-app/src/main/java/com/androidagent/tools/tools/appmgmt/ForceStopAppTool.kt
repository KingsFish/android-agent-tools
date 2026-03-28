package com.androidagent.tools.tools.appmgmt

import com.androidagent.core.*
import com.androidagent.androidapp.AndroidCapability
import com.androidagent.androidapp.AppToolContext

class ForceStopAppTool : Tool {
    override val name = "force_stop_app"
    override val description = "Force stop an application."
    override val inputSchema = ToolSchema(
        properties = mapOf(
            "package_name" to SchemaProperty(type = "string", description = "Package name of the application to stop")
        ),
        required = listOf("package_name")
    )

    override fun validate(params: Map<String, Any?>): ValidationResult {
        val validator = ParameterValidator(params)
        return validator.requireString("package_name")
    }

    override suspend fun execute(context: ToolContext, params: Map<String, Any?>): ToolResult {
        val validator = ParameterValidator(params)
        val packageName = validator.getString("package_name")
            ?: return ToolResult.failure(ToolError.INVALID_PARAMETER, "Missing package_name")

        if (!context.hasCapability(AndroidCapability.ROOT)) {
            return ToolResult.failure(ToolError.ROOT_REQUIRED)
        }

        return try {
            val proc = java.lang.Runtime.getRuntime().exec(arrayOf("su", "-c", "am force-stop $packageName"))
            val exitCode = proc.waitFor()
            if (exitCode == 0) {
                ToolResult.success(emptyMap())
            } else {
                ToolResult.failure(ToolError.FORCE_STOP_FAILED, "ROOT command failed with exit code $exitCode")
            }
        } catch (e: Exception) {
            ToolResult.failure(ToolError.FORCE_STOP_FAILED, e.message)
        }
    }
}
