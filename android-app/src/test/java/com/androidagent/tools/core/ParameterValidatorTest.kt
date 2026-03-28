package com.androidagent.tools.core

import com.androidagent.core.ParameterValidator
import com.androidagent.core.ToolError
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ParameterValidatorTest {

    @Test
    fun `requireString returns success for valid string`() {
        val params = mapOf("key" to "value")
        val validator = ParameterValidator(params)
        val result = validator.requireString("key")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `requireString returns failure for missing key`() {
        val params = emptyMap<String, Any?>()
        val validator = ParameterValidator(params)
        val result = validator.requireString("key")
        assertTrue(result.isFailure)
    }

    @Test
    fun `requireString returns failure for non-string value`() {
        val params = mapOf("key" to 123)
        val validator = ParameterValidator(params)
        val result = validator.requireString("key")
        assertTrue(result.isFailure)
    }

    @Test
    fun `optionalString returns default for missing key`() {
        val params = emptyMap<String, Any?>()
        val validator = ParameterValidator(params)
        val result = validator.optionalString("key", "default")
        assertEquals("default", result)
    }

    @Test
    fun `optionalBoolean returns default for missing key`() {
        val params = emptyMap<String, Any?>()
        val validator = ParameterValidator(params)
        val result = validator.optionalBoolean("key", true)
        assertTrue(result)
    }
}
