package com.androidagent.tools.tools.app

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.androidagent.tools.core.*

class GetAppInfoTool : Tool {
    override val name = "get_app_info"
    override val description = "Get detailed information about an application."

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

        return try {
            val packageInfo = packageManager.getPackageInfo(
                packageName,
                PackageManager.GET_PERMISSIONS or PackageManager.GET_META_DATA
            )

            val appInfo = packageInfo.applicationInfo
            val isSystemApp = (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0

            val label = try {
                packageManager.getApplicationLabel(appInfo).toString()
            } catch (e: Exception) {
                packageName
            }

            val permissions = packageInfo.requestedPermissions?.toList() ?: emptyList()

            val result = mapOf(
                "package_name" to packageName,
                "label" to label,
                "version_name" to packageInfo.versionName,
                "version_code" to if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    packageInfo.longVersionCode
                } else {
                    @Suppress("DEPRECATION")
                    packageInfo.versionCode.toLong()
                },
                "is_system" to isSystemApp,
                "install_time" to packageInfo.firstInstallTime,
                "update_time" to packageInfo.lastUpdateTime,
                "data_dir" to appInfo.dataDir,
                "permissions" to permissions
            )

            ToolResult.success(result)
        } catch (e: PackageManager.NameNotFoundException) {
            ToolResult.failure(ToolError.APP_NOT_FOUND, packageName)
        } catch (e: Exception) {
            ToolResult.failure(ToolError.APP_NOT_FOUND, "${e.message}")
        }
    }
}