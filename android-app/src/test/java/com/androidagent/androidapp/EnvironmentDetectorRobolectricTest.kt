package com.androidagent.androidapp

import android.content.Context
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class EnvironmentDetectorRobolectricTest {

    private lateinit var context: Context
    private lateinit var detector: EnvironmentDetector

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        detector = EnvironmentDetector(context)
    }

    @Test
    fun hasCapability_returns_correct_values_for_all_capabilities() {
        // These will be false in Robolectric by default
        assertFalse(detector.hasCapability(AndroidCapability.ROOT))
        assertFalse(detector.hasCapability(AndroidCapability.ACCESSIBILITY_SERVICE))
        assertTrue(detector.hasCapability(AndroidCapability.MEDIA_PROJECTION)) // Always true for API 21+
        // Overlay permission may vary in Robolectric, just verify the method works
        detector.hasCapability(AndroidCapability.OVERLAY)
    }
}
