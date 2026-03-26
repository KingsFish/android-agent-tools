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
}