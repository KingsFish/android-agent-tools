package com.androidagent.tools.core

sealed class Result<out T> {
    data class Success<T>(val value: T) : Result<T>()
    data class Failure<T>(val error: ToolError, val context: String? = null) : Result<T>()

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure

    fun getOrNull(): T? = (this as? Success)?.value
    fun getErrorOrNull(): ToolError? = (this as? Failure)?.error
}

class ParameterValidator(private val params: Map<String, Any?>) {

    fun requireString(key: String): Result<String> {
        val value = params[key]
        return when {
            value == null -> Result.Failure(ToolError.INVALID_PARAMETER, "Missing required parameter: $key")
            value !is String -> Result.Failure(ToolError.INVALID_PARAMETER, "Parameter '$key' must be a string")
            else -> Result.Success(value)
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

    fun requireArray(key: String): Result<List<String>> {
        val value = params[key]
        return when {
            value == null -> Result.Failure(ToolError.INVALID_PARAMETER, "Missing required parameter: $key")
            value !is List<*> -> Result.Failure(ToolError.INVALID_PARAMETER, "Parameter '$key' must be an array")
            else -> Result.Success(value.mapNotNull { it?.toString() })
        }
    }
}