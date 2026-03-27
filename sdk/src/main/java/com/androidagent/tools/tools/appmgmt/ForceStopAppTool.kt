package com.androidagent.tools.tools.appmgmt

import android.content.Context
import com.androidagent.tools.core.*

class ForceStopAppTool : Tool {
    override val name = "force_stop_app"
    override val description = "Force stop an application."

    override fun validate(params: Map<String, Any?>): Result<Unit> {
        val validator = ParameterValidator(params)
        return when (val result = validator.requireString("package_name")) {
            is Result.Success -> Result.Success(Unit)
            is Result.Failure -> result
        }
    }

    override suspend fun execute(context: Context, params: Map<String, Any?>): ToolResult {
        val validator = ParameterValidator(params)
        val packageName = (validator.requireString("package_name") as Result.Success).value

        try {
            context.packageManager.getPackageInfo(packageName, 0)
        } catch (e: android.content.pm.PackageManager.NameNotFoundException) {
            return ToolResult.failure(ToolError.APP_NOT_FOUND, packageName)
        }

        val envDetector = EnvironmentDetector(context)
        if (envDetector.hasRoot()) {
            return executeWithRoot(packageName)
        }

        return ToolResult.failure(ToolError.ROOT_REQUIRED, "Force stop requires ROOT access")
    }

    private fun executeWithRoot(packageName: String): ToolResult {
        return try {
            val process = Runtime.getRuntime().exec(
                arrayOf("su", "-c", "am force-stop $packageName")
            )
            val exitCode = process.waitFor()
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
