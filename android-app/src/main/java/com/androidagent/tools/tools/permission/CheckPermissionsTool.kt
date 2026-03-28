package com.androidagent.tools.tools.permission

import android.content.pm.PackageManager
import com.androidagent.core.*
import com.androidagent.androidapp.AppToolContext

class CheckPermissionsTool : Tool {
    override val name = "check_permissions"
    override val description = "Check the status of specified permissions."
    override val inputSchema = ToolSchema(
        properties = mapOf(
            "permissions" to SchemaProperty(
                type = "array",
                description = "List of permissions to check (storage, camera, location, microphone, contacts, sms, phone)",
                items = SchemaProperty(type = "string")
            )
        ),
        required = listOf("permissions")
    )

    private val permissionMapping = mapOf(
        "storage" to listOf(
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"
        ),
        "camera" to listOf("android.permission.CAMERA"),
        "location" to listOf(
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION"
        ),
        "microphone" to listOf("android.permission.RECORD_AUDIO"),
        "contacts" to listOf(
            "android.permission.READ_CONTACTS",
            "android.permission.WRITE_CONTACTS"
        ),
        "sms" to listOf(
            "android.permission.SEND_SMS",
            "android.permission.RECEIVE_SMS",
            "android.permission.READ_SMS"
        ),
        "phone" to listOf(
            "android.permission.READ_PHONE_STATE",
            "android.permission.CALL_PHONE"
        )
    )

    override fun validate(params: Map<String, Any?>): ValidationResult {
        val validator = ParameterValidator(params)
        return validator.requireArray("permissions")
    }

    override suspend fun execute(context: ToolContext, params: Map<String, Any?>): ToolResult {
        val validator = ParameterValidator(params)
        val permissions = validator.getArray("permissions")
            ?: return ToolResult.failure(ToolError.INVALID_PARAMETER, "Missing permissions")

        val appContext = context as? AppToolContext
            ?: return ToolResult.failure(ToolError.UNSUPPORTED_OPERATION, "Requires Android context")
        val androidContext = appContext.getAndroidContext()

        val result = mutableMapOf<String, String>()

        for (permissionName in permissions) {
            val androidPermissions = permissionMapping[permissionName]

            if (androidPermissions == null) {
                result[permissionName] = "unknown"
            } else {
                val allGranted = androidPermissions.all { androidPermission ->
                    androidContext.checkSelfPermission(androidPermission) == PackageManager.PERMISSION_GRANTED
                }
                result[permissionName] = if (allGranted) "granted" else "denied"
            }
        }

        return ToolResult.success(result)
    }
}