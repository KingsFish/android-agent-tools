package com.androidagent.tools.accessibility

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import android.view.accessibility.AccessibilityNodeInfo

class AgentAccessibilityServiceTest {

    @BeforeEach
    fun setUp() {
        // Reset the static instance before each test
        // Note: We can't directly set it since it's private, but we can test the companion object methods
    }

    @AfterEach
    fun tearDown() {
        // Clean up after each test
    }

    @Test
    fun `isRunning returns false when instance is null`() {
        // The service instance should be null in unit tests
        // since we're not running in an Android environment
        assertFalse(AgentAccessibilityService.isRunning())
    }

    @Test
    fun `companion object has correct methods`() {
        // Verify the companion object has the expected static methods
        val methods = AgentAccessibilityService.Companion::class.java.methods
        val methodNames = methods.map { it.name }
        
        assertTrue(methodNames.contains("isRunning"))
        assertTrue(methodNames.contains("getInstance"))
    }
}
