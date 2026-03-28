package com.androidagent.androidapp

import android.content.Context
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class EnvironmentDetectorRobolectricTest {

    private lateinit var context: Context
    private lateinit var detector: EnvironmentDetector

    @BeforeEach
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        detector = EnvironmentDetector(context)
    }

    @Test
    fun `hasCapability returns correct values for all capabilities`() {
        // These will be false in Robolectric by default
        assertFalse(detector.hasCapability(AndroidCapability.ROOT))
        assertFalse(detector.hasCapability(AndroidCapability.ACCESSIBILITY_SERVICE))
        assertTrue(detector.hasCapability(AndroidCapability.MEDIA_PROJECTION)) // Always true for API 21+
        // Overlay permission is true by default in Robolectric
        assertTrue(detector.hasCapability(AndroidCapability.OVERLAY))
    }
}
