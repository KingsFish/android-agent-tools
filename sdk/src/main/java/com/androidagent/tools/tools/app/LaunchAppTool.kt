package com.androidagent.tools.tools.app

import android.content.Context
import com.androidagent.tools.core.*

class LaunchAppTool : Tool {
    override val name = "launch_app"
    override val description = "Launch an application by package name."

    override fun validate(params: Map<String, Any?>): Result<Unit> {
        val validator = ParameterValidator(params)
        val packageNameResult = validator.requireString("package_name")
        return when (packageNameResult) {
            is Result.Success -> Result.Success(Unit)
            is Result.Failure -> Result.Failure(packageNameResult.error, packageNameResult.context)
        }
    }

    override suspend fun execute(context: Context, params: Map<String, Any?>): ToolResult {
        val validator = ParameterValidator(params)
        val packageName = (validator.requireString("package_name") as Result.Success).value

        val packageManager = context.packageManager

        val intent = packageManager.getLaunchIntentForPackage(packageName)
            ?: return ToolResult.failure(ToolError.APP_NOT_LAUNCHABLE, packageName)

        return try {
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            ToolResult.success(emptyMap())
        } catch (e: Exception) {
            ToolResult.failure(ToolError.LAUNCH_FAILED, "${e.message}")
        }
    }
}