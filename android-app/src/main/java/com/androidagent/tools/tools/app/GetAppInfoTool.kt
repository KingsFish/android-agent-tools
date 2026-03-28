package com.androidagent.tools.tools.app

import android.content.pm.PackageManager
import android.os.Build
import com.androidagent.core.*
import com.androidagent.androidapp.AppToolContext

class GetAppInfoTool : Tool {
    override val name = "get_app_info"
    override val description = "Get detailed information about an application."
    override val inputSchema = ToolSchema(
        properties = mapOf(
            "package_name" to SchemaProperty(type = "string", description = "Package name of the application")
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