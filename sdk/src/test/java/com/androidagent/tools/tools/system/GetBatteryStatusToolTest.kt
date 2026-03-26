package com.androidagent.tools.tools.system

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.androidagent.tools.core.ToolResult
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class GetBatteryStatusToolTest {
    private val tool = GetBatteryStatusTool()

    @Test
    fun `validate succeeds with no parameters`() {
        val result = tool.validate(emptyMap())
        assertTrue(result.isSuccess)
    }

    @Test
    fun `execute returns battery status`() {
        val mockContext = mockk<android.content.Context>()
        val mockIntent = mockk<Intent>()

        every { mockContext.registerReceiver(null, any<IntentFilter>()) } returns mockIntent
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) } returns 85
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1) } returns 100
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1) } returns BatteryManager.BATTERY_STATUS_CHARGING
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) } returns BatteryManager.BATTERY_HEALTH_GOOD
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) } returns 250
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) } returns 4200

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockContext, emptyMap())
        }

        assertTrue(result is ToolResult.Success)
        val data = (result as ToolResult.Success).data
        assertEquals(85, data["level"])
        assertEquals("charging", data["status"])
    }
}