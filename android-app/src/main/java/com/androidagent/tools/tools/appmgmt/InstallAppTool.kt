package com.androidagent.tools.tools.appmgmt

import com.androidagent.core.*
import com.androidagent.androidapp.AndroidCapability
import com.androidagent.androidapp.AppToolContext
import java.io.File

class InstallAppTool : Tool {
    override val name = "install_app"
    override val description = "Install an application from an APK file."
    override val inputSchema = ToolSchema(
        properties = mapOf(
            "apk_path" to SchemaProperty(type = "string", description = "Path to the APK file")
        ),
        required = listOf("apk_path")
    )

    override fun validate(params: Map<String, Any?>): ValidationResult {
        val validator = ParameterValidator(params)
        return validator.requireString("apk_path")
    }

    override suspend fun execute(context: ToolContext, params: Map<String, Any?>): ToolResult {
        val validator = ParameterValidator(params)
        val apkPath = validator.getString("apk_path")
            ?: return ToolResult.failure(ToolError.INVALID_PARAMETER, "Missing apk_path")

        val apkFile = File(apkPath)
        if (!apkFile.exists()) {
            return ToolResult.failure(ToolError.APK_NOT_FOUND, apkPath)
        }

        if (!apkPath.endsWith(".apk", ignoreCase = true)) {
            return ToolResult.failure(ToolError.INVALID_APK, "File must be an APK")
        }

        if (!context.hasCapability(AndroidCapability.ROOT)) {
            return ToolResult.failure(ToolError.ROOT_REQUIRED)
        }

        return try {
            val proc = java.lang.Runtime.getRuntime().exec(arrayOf("su", "-c", "pm install -r $apkPath"))
            val exitCode = proc.waitFor()
            if (exitCode == 0) {
                ToolResult.success(emptyMap())
            } else {
                ToolResult.failure(ToolError.INSTALL_FAILED, "ROOT command failed with exit code $exitCode")
            }
        } catch (e: Exception) {
            ToolResult.failure(ToolError.INSTALL_FAILED, e.message)
        }
    }
}
