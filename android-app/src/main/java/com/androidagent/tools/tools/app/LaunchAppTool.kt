package com.androidagent.tools.tools.app

import com.androidagent.core.*
import com.androidagent.androidapp.AppToolContext

class LaunchAppTool : Tool {
    override val name = "launch_app"
    override val description = "Launch an application by package name."
    override val inputSchema = ToolSchema(
        properties = mapOf(
            "package_name" to SchemaProperty(type = "string", description = "Package name of the application to launch")
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
        val androidContext = appContext.getAndroidContext()

        val packageManager = androidContext.packageManager

        val intent = packageManager.getLaunchIntentForPackage(packageName)
            ?: return ToolResult.failure(ToolError.APP_NOT_LAUNCHABLE, packageName)

        return try {
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            androidContext.startActivity(intent)
            ToolResult.success(emptyMap())
        } catch (e: Exception) {
            ToolResult.failure(ToolError.LAUNCH_FAILED, "${e.message}")
        }
    }
}