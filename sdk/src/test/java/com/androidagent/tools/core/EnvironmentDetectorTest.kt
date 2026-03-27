package com.androidagent.tools.core

import android.content.Context
import io.mockk.every
import io.mockk.mockk
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
        every { mockContext.contentResolver } returns mockk(relaxed = true)

        val caps = detector.capabilities

        assertTrue(caps.containsKey(Capability.ROOT))
        assertTrue(caps.containsKey(Capability.ACCESSIBILITY_SERVICE))
        assertTrue(caps.containsKey(Capability.MEDIA_PROJECTION))
        assertTrue(caps.containsKey(Capability.OVERLAY))
    }

    @Test
    fun `hasMediaProjection returns true for Lollipop and above`() {
        // Build.VERSION.SDK_INT is always >= LOLLIPOP on modern Android
        assertTrue(detector.hasMediaProjection())
    }
}