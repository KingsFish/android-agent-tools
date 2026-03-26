package com.androidagent.tools.tools.system

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.androidagent.tools.core.Result
import com.androidagent.tools.core.Tool
import com.androidagent.tools.core.ToolError
import com.androidagent.tools.core.ToolResult

class GetBatteryStatusTool : Tool {
    override val name = "get_battery_status"
    override val description = "Get current battery status including level, charging state, etc."

    override fun validate(params: Map<String, Any?>): Result<Unit> {
        // No parameters required
        return Result.Success(Unit)
    }

    override suspend fun execute(context: Context, params: Map<String, Any?>): ToolResult {
        return try {
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = context.registerReceiver(null, intentFilter)

            if (batteryStatus == null) {
                return ToolResult.failure(
                    ToolError.UNSUPPORTED_OPERATION,
                    "Cannot get battery status"
                )
            }

            val level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val health = batteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
            val temperature = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
            val voltage = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)

            val statusString = when (status) {
                BatteryManager.BATTERY_STATUS_CHARGING -> "charging"
                BatteryManager.BATTERY_STATUS_DISCHARGING -> "discharging"
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "not_charging"
                BatteryManager.BATTERY_STATUS_FULL -> "full"
                else -> "unknown"
            }

            val healthString = when (health) {
                BatteryManager.BATTERY_HEALTH_GOOD -> "good"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "overheat"
                BatteryManager.BATTERY_HEALTH_DEAD -> "dead"
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "over_voltage"
                BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "failure"
                else -> "unknown"
            }

            // Temperature is in tenths of a degree Celsius
            val temperatureCelsius = temperature / 10.0
            // Voltage is in millivolts
            val voltageVolts = voltage / 1000.0

            val result = mapOf(
                "level" to level,
                "scale" to scale,
                "status" to statusString,
                "health" to healthString,
                "temperature" to temperatureCelsius,
                "voltage" to voltageVolts
            )

            ToolResult.success(result)
        } catch (e: Exception) {
            ToolResult.failure(
                ToolError.UNSUPPORTED_OPERATION,
                "Failed to get battery status: ${e.message}"
            )
        }
    }
}