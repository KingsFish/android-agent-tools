package com.androidagent.tools.tools.appmgmt

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.androidagent.tools.core.*
import java.io.File

class InstallAppTool : Tool {
    override val name = "install_app"
    override val description = "Install an application from APK file."

    override fun validate(params: Map<String, Any?>): Result<Unit> {
        val validator = ParameterValidator(params)
        return when (val result = validator.requireString("apk_path")) {
            is Result.Success -> Result.Success(Unit)
            is Result.Failure -> result
        }
    }

    override suspend fun execute(context: Context, params: Map<String, Any?>): ToolResult {
        val validator = ParameterValidator(params)
        val apkPath = (validator.requireString("apk_path") as Result.Success).value

        val apkFile = File(apkPath)
        if (!apkFile.exists()) {
            return ToolResult.failure(ToolError.APK_NOT_FOUND, apkPath)
        }

        if (!apkPath.endsWith(".apk", ignoreCase = true)) {
            return ToolResult.failure(ToolError.INVALID_APK, "File is not an APK")
        }

        val envDetector = EnvironmentDetector(context)
        if (envDetector.hasRoot()) {
            return executeWithRoot(context, apkPath)
        }

        return ToolResult.failure(ToolError.ROOT_REQUIRED, "Install requires ROOT access")
    }

    private fun executeWithRoot(context: Context, apkPath: String): ToolResult {
        return try {
            val process = Runtime.getRuntime().exec(
                arrayOf("su", "-c", "pm install -r -g $apkPath")
            )
            val inputStream = process.inputStream
            val output = inputStream.bufferedReader().readText()
            inputStream.close()
            val exitCode = process.waitFor()

            if (exitCode == 0 && output.contains("Success")) {
                val packageInfo = getPackageInfoFromApk(context, apkPath)
                ToolResult.success(mapOf(
                    "package_name" to (packageInfo?.packageName ?: "unknown"),
                    "version_name" to (packageInfo?.versionName ?: "unknown")
                ))
            } else {
                ToolResult.failure(ToolError.INSTALL_FAILED, output.trim())
            }
        } catch (e: Exception) {
            ToolResult.failure(ToolError.INSTALL_FAILED, e.message)
        }
    }

    private fun getPackageInfoFromApk(context: Context, apkPath: String): PackageInfo? {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageArchiveInfo(
                    apkPath,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageArchiveInfo(apkPath, 0)
            }
        } catch (e: Exception) {
            null
        }
    }
}
