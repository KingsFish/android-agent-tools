package com.androidagent.tools.tools.app

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.androidagent.tools.core.*

class ListAppsTool : Tool {
    override val name = "list_apps"
    override val description = "List all installed applications on the device."

    override fun validate(params: Map<String, Any?>): Result<Unit> {
        // All parameters are optional, so validation always succeeds
        return Result.Success(Unit)
    }

    override suspend fun execute(context: Context, params: Map<String, Any?>): ToolResult {
        val validator = ParameterValidator(params)
        val includeSystemApps = validator.optionalBoolean("include_system_apps", false)

        val packageManager = context.packageManager

        // Get all installed applications
        val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        val apps = installedApps
            .filter { appInfo ->
                // Filter out system apps if not included
                includeSystemApps || (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0
            }
            .filter { appInfo ->
                // Filter out the current app
                appInfo.packageName != context.packageName
            }
            .map { appInfo ->
                val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val label = try {
                    packageManager.getApplicationLabel(appInfo).toString()
                } catch (e: Exception) {
                    appInfo.packageName
                }

                val versionName = try {
                    val packageInfo = packageManager.getPackageInfo(appInfo.packageName, 0)
                    packageInfo.versionName
                } catch (e: Exception) {
                    null
                }

                mapOf(
                    "package_name" to appInfo.packageName,
                    "label" to label,
                    "version_name" to versionName,
                    "is_system" to isSystemApp
                )
            }
            .sortedBy { (it["label"] as String).lowercase() }

        return ToolResult.success(mapOf("apps" to apps))
    }
}