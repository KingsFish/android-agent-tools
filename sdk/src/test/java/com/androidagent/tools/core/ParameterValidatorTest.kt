package com.androidagent.tools.core

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ParameterValidatorTest {

    @Test
    fun `require non-null string returns success when present`() {
        val params = mapOf("path" to "/sdcard/test.txt")
        val result = ParameterValidator(params).requireString("path")
        assertTrue(result.isSuccess)
        assertEquals("/sdcard/test.txt", result.getOrNull())
    }

    @Test
    fun `require non-null string returns failure when missing`() {
        val params = mapOf<String, Any>()
        val result = ParameterValidator(params).requireString("path")
        assertTrue(result.isFailure)
        assertEquals(ToolError.INVALID_PARAMETER, result.getErrorOrNull())
    }

    @Test
    fun `optional string returns default when missing`() {
        val params = mapOf<String, Any>()
        val result = ParameterValidator(params).optionalString("encoding", "utf-8")
        assertEquals("utf-8", result)
    }

    @Test
    fun `optional string returns value when present`() {
        val params = mapOf("encoding" to "gbk")
        val result = ParameterValidator(params).optionalString("encoding", "utf-8")
        assertEquals("gbk", result)
    }

    @Test
    fun `optional boolean returns default when missing`() {
        val params = mapOf<String, Any>()
        val result = ParameterValidator(params).optionalBoolean("include_system", false)
        assertFalse(result)
    }

    @Test
    fun `require array returns list when present`() {
        val params = mapOf("permissions" to listOf("storage", "camera"))
        val result = ParameterValidator(params).requireArray("permissions")
        assertTrue(result.isSuccess)
        assertEquals(listOf("storage", "camera"), result.getOrNull())
    }

    @Test
    fun `requireInt returns success for integer value`() {
        val validator = ParameterValidator(mapOf("count" to 42))
        val result = validator.requireInt("count")
        assertTrue(result.isSuccess)
        assertEquals(42, (result as Result.Success).value)
    }

    @Test
    fun `requireInt fails for missing parameter`() {
        val validator = ParameterValidator(emptyMap())
        val result = validator.requireInt("count")
        assertTrue(result.isFailure)
    }

    @Test
    fun `optionalInt returns default for missing parameter`() {
        val validator = ParameterValidator(emptyMap())
        val result = validator.optionalInt("count", 10)
        assertEquals(10, result)
    }

    @Test
    fun `requireInt returns success for Long value`() {
        val validator = ParameterValidator(mapOf("count" to 42L))
        val result = validator.requireInt("count")
        assertTrue(result.isSuccess)
        assertEquals(42, (result as Result.Success).value)
    }

    @Test
    fun `requireInt fails for non-numeric type`() {
        val validator = ParameterValidator(mapOf("count" to "not a number"))
        val result = validator.requireInt("count")
        assertTrue(result.isFailure)
    }

    @Test
    fun `optionalInt returns value when present`() {
        val validator = ParameterValidator(mapOf("count" to 42))
        val result = validator.optionalInt("count", 10)
        assertEquals(42, result)
    }

    @Test
    fun `optionalInt converts Long to Int`() {
        val validator = ParameterValidator(mapOf("count" to 99L))
        val result = validator.optionalInt("count", 10)
        assertEquals(99, result)
    }
}