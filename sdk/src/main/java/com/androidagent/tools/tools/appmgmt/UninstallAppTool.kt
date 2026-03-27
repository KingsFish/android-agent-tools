package com.androidagent.tools.tools.appmgmt

import android.content.Context
import com.androidagent.tools.core.*

class UninstallAppTool : Tool {
    override val name = "uninstall_app"
    override val description = "Uninstall an application."

    override fun validate(params: Map<String, Any?>): Result<Unit> {
        val validator = ParameterValidator(params)
        return when (val result = validator.requireNonEmptyString("package_name")) {
            is Result.Success -> Result.Success(Unit)
            is Result.Failure -> Result.Failure(result.error, result.context)
        }
    }

    override suspend fun execute(context: Context, params: Map<String, Any?>): ToolResult {
        val validator = ParameterValidator(params)
        val packageName = (validator.requireNonEmptyString("package_name") as Result.Success).value

        try {
            context.packageManager.getPackageInfo(packageName, 0)
        } catch (e: android.content.pm.PackageManager.NameNotFoundException) {
            return ToolResult.failure(ToolError.APP_NOT_FOUND, packageName)
        }

        val envDetector = EnvironmentDetector(context)
        if (envDetector.hasRoot()) {
            return executeWithRoot(packageName)
        }

        return ToolResult.failure(ToolError.ROOT_REQUIRED, "Uninstall requires ROOT access")
    }

    private fun executeWithRoot(packageName: String): ToolResult {
        return try {
            val process = Runtime.getRuntime().exec(
                arrayOf("su", "-c", "pm uninstall $packageName")
            )
            val inputStream = process.inputStream
            val output = inputStream.bufferedReader().readText()
            inputStream.close()
            val exitCode = process.waitFor()

            if (exitCode == 0 && output.contains("Success")) {
                ToolResult.success(emptyMap())
            } else {
                ToolResult.failure(ToolError.UNINSTALL_FAILED, output.trim())
            }
        } catch (e: Exception) {
            ToolResult.failure(ToolError.UNINSTALL_FAILED, e.message)
        }
    }
}
