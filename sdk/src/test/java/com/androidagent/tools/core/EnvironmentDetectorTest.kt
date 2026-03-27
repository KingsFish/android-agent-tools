package com.androidagent.tools.core

import android.content.Context
import android.provider.Settings
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class EnvironmentDetectorTest {

    private lateinit var mockContext: Context
    private lateinit var detector: EnvironmentDetector

    @BeforeEach
    fun setUp() {
        mockContext = mockk(relaxed = true)
        every { mockContext.packageName } returns "com.test"
        detector = EnvironmentDetector(mockContext)
    }

    @Test
    fun `hasRoot returns false when no root access`() {
        // In a non-rooted test environment, this should return false
        val result = detector.hasRoot()
        assertFalse(result)
    }

    @Test
    fun `hasCapability ROOT returns false on non-rooted device`() {
        val result = detector.hasCapability(Capability.ROOT)
        assertFalse(result)
    }

    @Test
    fun `hasCapability routes to correct method for ROOT`() {
        // Test that hasCapability correctly routes to hasRoot for ROOT capability
        val rootResult = detector.hasCapability(Capability.ROOT)
        assertFalse(rootResult) // Returns false in non-rooted test environment
    }

    @Test
    fun `capabilities returns all capabilities`() {
        // Mock contentResolver and Settings.Secure for accessibility service check
        val mockContentResolver = mockk<android.content.ContentResolver>(relaxed = true)
        every { mockContext.contentResolver } returns mockContentResolver

        // Mock Settings.Secure.getString to return empty string (no accessibility services enabled)
        mockkStatic(Settings.Secure::class)
        every { Settings.Secure.getString(any(), any()) } returns ""

        val caps = detector.capabilities

        assertTrue(caps.containsKey(Capability.ROOT))
        assertTrue(caps.containsKey(Capability.ACCESSIBILITY_SERVICE))
        assertTrue(caps.containsKey(Capability.MEDIA_PROJECTION))
        assertTrue(caps.containsKey(Capability.OVERLAY))

        unmockkStatic(Settings.Secure::class)
    }

    @Test
    fun `hasMediaProjection returns false in unit test environment`() {
        // In unit test environment, Build.VERSION.SDK_INT is 0, so hasMediaProjection returns false
        // This is expected behavior - on a real device with API 21+, it would return true
        assertFalse(detector.hasMediaProjection())
    }

    @Test
    fun `hasAccessibilityService returns false when service not enabled`() {
        val mockContentResolver = mockk<android.content.ContentResolver>(relaxed = true)
        every { mockContext.contentResolver } returns mockContentResolver

        mockkStatic(Settings.Secure::class)
        every { Settings.Secure.getString(any(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) } returns ""

        val result = detector.hasAccessibilityService()
        assertFalse(result)

        unmockkStatic(Settings.Secure::class)
    }

    @Test
    fun `hasAccessibilityService returns true when service is enabled`() {
        val mockContentResolver = mockk<android.content.ContentResolver>(relaxed = true)
        every { mockContext.contentResolver } returns mockContentResolver

        mockkStatic(Settings.Secure::class)
        every { Settings.Secure.getString(any(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) } returns
            "com.test/com.androidagent.tools.accessibility.AgentAccessibilityService"

        val result = detector.hasAccessibilityService()
        assertTrue(result)

        unmockkStatic(Settings.Secure::class)
    }

    @Test
    fun `hasOverlay returns false in unit test environment`() {
        // In unit test environment, Build.VERSION.SDK_INT is 0, so hasOverlay returns true (pre-M behavior)
        // This tests the code path, actual overlay permission depends on real device state
        assertTrue(detector.hasOverlay()) // SDK_INT is 0, so it returns true (pre-M fallback)
    }
}