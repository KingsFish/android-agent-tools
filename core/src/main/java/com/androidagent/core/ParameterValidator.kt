package com.androidagent.core

/**
 * 参数验证器
 *
 * 提供统一的参数验证工具方法。
 */
class ParameterValidator(private val params: Map<String, Any?>) {

    fun requireString(key: String): ValidationResult {
        val value = params[key]
        return when {
            value == null -> ValidationResult.failure(ToolError.INVALID_PARAMETER, "Missing required parameter: $key")
            value !is String -> ValidationResult.failure(ToolError.INVALID_PARAMETER, "Parameter '$key' must be a string")
            else -> ValidationResult.success()
        }
    }

    fun requireNonEmptyString(key: String): ValidationResult {
        val value = params[key]
        return when {
            value == null -> ValidationResult.failure(ToolError.INVALID_PARAMETER, "Missing required parameter: $key")
            value !is String -> ValidationResult.failure(ToolError.INVALID_PARAMETER, "Parameter '$key' must be a string")
            value.isBlank() -> ValidationResult.failure(ToolError.INVALID_PARAMETER, "Parameter '$key' must not be empty")
            else -> ValidationResult.success()
        }
    }

    fun optionalString(key: String, default: String): String {
        val value = params[key]
        return when {
            value == null -> default
            value is String -> value
            else -> default
        }
    }

    fun optionalBoolean(key: String, default: Boolean): Boolean {
        val value = params[key]
        return when {
            value == null -> default
            value is Boolean -> value
            else -> default
        }
    }

    fun requireArray(key: String): ValidationResult {
        val value = params[key]
        return when {
            value == null -> ValidationResult.failure(ToolError.INVALID_PARAMETER, "Missing required parameter: $key")
            value !is List<*> -> ValidationResult.failure(ToolError.INVALID_PARAMETER, "Parameter '$key' must be an array")
            else -> ValidationResult.success()
        }
    }

    fun requireInt(key: String): ValidationResult {
        val value = params[key]
        return when {
            value == null -> ValidationResult.failure(ToolError.INVALID_PARAMETER, "Missing required parameter: $key")
            value is Int || value is Number -> ValidationResult.success()
            else -> ValidationResult.failure(ToolError.INVALID_PARAMETER, "Parameter '$key' must be an integer")
        }
    }

    fun optionalInt(key: String, default: Int): Int {
        val value = params[key]
        return when {
            value == null -> default
            value is Int -> value
            value is Number -> value.toInt()
            else -> default
        }
    }

    fun optionalLong(key: String, default: Long): Long {
        val value = params[key]
        return when {
            value == null -> default
            value is Long -> value
            value is Number -> value.toLong()
            else -> default
        }
    }

    // Helper methods to get actual values after validation
    fun getString(key: String): String? = params[key] as? String
    fun getInt(key: String): Int? = (params[key] as? Number)?.toInt()
    fun getLong(key: String): Long? = (params[key] as? Number)?.toLong()
    fun getBoolean(key: String): Boolean? = params[key] as? Boolean
    fun getArray(key: String): List<String>? = (params[key] as? List<*>)?.mapNotNull { it?.toString() }
}