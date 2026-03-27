package com.androidagent.tools.tools.permission

import android.content.Context
import android.content.pm.PackageManager
import com.androidagent.tools.core.*

class CheckPermissionsTool : Tool {
    override val name = "check_permissions"
    override val description = "Check the status of specified permissions."

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

    override fun validate(params: Map<String, Any?>): Result<Unit> {
        val validator = ParameterValidator(params)
        val permissionsResult = validator.requireArray("permissions")
        return when (permissionsResult) {
            is Result.Success -> Result.Success(Unit)
            is Result.Failure -> Result.Failure(permissionsResult.error, permissionsResult.context)
        }
    }

    override suspend fun execute(context: Context, params: Map<String, Any?>): ToolResult {
        val validator = ParameterValidator(params)
        val permissions = (validator.requireArray("permissions") as Result.Success).value

        val result = mutableMapOf<String, String>()

        for (permissionName in permissions) {
            val androidPermissions = permissionMapping[permissionName]

            if (androidPermissions == null) {
                result[permissionName] = "unknown"
            } else {
                val allGranted = androidPermissions.all { androidPermission ->
                    context.checkSelfPermission(androidPermission) == PackageManager.PERMISSION_GRANTED
                }
                result[permissionName] = if (allGranted) "granted" else "denied"
            }
        }

        return ToolResult.success(result)
    }
}