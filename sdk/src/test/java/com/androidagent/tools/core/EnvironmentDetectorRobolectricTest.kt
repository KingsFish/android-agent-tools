package com.androidagent.tools.core

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric tests for EnvironmentDetector methods that require Android framework.
 * These tests use Robolectric to simulate Android SDK behavior.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [24], manifest = Config.NONE)
class EnvironmentDetectorRobolectricTest {

    private lateinit var mockContext: Context
    private lateinit var detector: EnvironmentDetector

    @Before
    fun setUp() {
        mockContext = mockk(relaxed = true)
        every { mockContext.packageName } returns "com.test"
        every { mockContext.contentResolver } returns mockk(relaxed = true)
        detector = EnvironmentDetector(mockContext)
    }

    @Test
    fun `capabilities returns all capabilities`() {
        val caps = detector.capabilities

        assertTrue(caps.containsKey(Capability.ROOT))
        assertTrue(caps.containsKey(Capability.ACCESSIBILITY_SERVICE))
        assertTrue(caps.containsKey(Capability.MEDIA_PROJECTION))
        assertTrue(caps.containsKey(Capability.OVERLAY))
    }

    @Test
    fun `hasMediaProjection returns true for Lollipop and above`() {
        assertTrue(detector.hasMediaProjection())
    }
}