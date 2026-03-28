package com.androidagent.tools.tools.appmgmt

import android.content.pm.PackageManager
import com.androidagent.core.*
import com.androidagent.androidapp.AndroidCapability
import com.androidagent.androidapp.AppToolContext

class UninstallAppTool : Tool {
    override val name = "uninstall_app"
    override val description = "Uninstall an application."
    override val inputSchema = ToolSchema(
        properties = mapOf(
            "package_name" to SchemaProperty(type = "string", description = "Package name of the application to uninstall")
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

        val appContext = context as? AppToolContext
            ?: return ToolResult.failure(ToolError.UNSUPPORTED_OPERATION, "Requires Android context")

        // Check if app exists
        try {
            appContext.getAndroidContext().packageManager.getPackageInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            return ToolResult.failure(ToolError.APP_NOT_FOUND)
        }

        if (!context.hasCapability(AndroidCapability.ROOT)) {
            return ToolResult.failure(ToolError.ROOT_REQUIRED)
        }

        return try {
            val proc = java.lang.Runtime.getRuntime().exec(arrayOf("su", "-c", "pm uninstall $packageName"))
            val exitCode = proc.waitFor()
            if (exitCode == 0) {
                ToolResult.success(emptyMap())
            } else {
                ToolResult.failure(ToolError.UNINSTALL_FAILED, "ROOT command failed with exit code $exitCode")
            }
        } catch (e: Exception) {
            ToolResult.failure(ToolError.UNINSTALL_FAILED, e.message)
        }
    }
}
