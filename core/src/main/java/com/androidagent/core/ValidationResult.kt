package com.androidagent.core

/**
 * 验证结果类型
 *
 * 用于参数验证的统一结果封装。
 */
sealed class ValidationResult {
    data class Success(val value: Unit = Unit) : ValidationResult()
    data class Failure(val error: ToolError, val context: String? = null) : ValidationResult()

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure

    fun getErrorOrNull(): ToolError? = (this as? Failure)?.error
    fun getContextOrNull(): String? = (this as? Failure)?.context

    companion object {
        fun success(): ValidationResult = Success()
        fun failure(error: ToolError, context: String? = null): ValidationResult = Failure(error, context)
    }
}