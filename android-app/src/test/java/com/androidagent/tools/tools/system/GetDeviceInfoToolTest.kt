package com.androidagent.tools.tools.system

import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import com.androidagent.core.ToolResult
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class GetDeviceInfoToolTest {
    private val tool = GetDeviceInfoTool()

    @Test
    fun `validate succeeds with no parameters`() {
        val result = tool.validate(emptyMap())
        assertTrue(result.isSuccess)
    }

    @Test
    fun `execute returns device info`() {
        val mockContext = mockk<android.content.Context>(relaxed = true)
        val mockWindowManager = mockk<WindowManager>()
        val mockDisplay = mockk<android.view.Display>()
        val mockMetrics = DisplayMetrics().apply {
            widthPixels = 1080
            heightPixels = 2340
            densityDpi = 420
        }

        every { mockContext.getSystemService(android.content.Context.WINDOW_SERVICE) } returns mockWindowManager
        every { mockWindowManager.defaultDisplay } returns mockDisplay
        every { mockDisplay.getMetrics(any()) } answers {
            val metrics = firstArg<DisplayMetrics>()
            metrics.widthPixels = mockMetrics.widthPixels
            metrics.heightPixels = mockMetrics.heightPixels
            metrics.densityDpi = mockMetrics.densityDpi
        }
        every { mockContext.resources.configuration } returns mockk(relaxed = true)

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockContext, emptyMap())
        }

        assertTrue(result is ToolResult.Success)
        val data = (result as ToolResult.Success).data
        assertEquals(Build.BRAND, data["brand"])
        assertEquals(Build.MODEL, data["model"])
        assertEquals(Build.VERSION.RELEASE, data["android_version"])
    }
}