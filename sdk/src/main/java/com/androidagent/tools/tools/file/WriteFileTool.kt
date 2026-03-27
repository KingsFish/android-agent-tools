package com.androidagent.tools.tools.file

import android.content.Context
import com.androidagent.tools.core.*
import java.io.File
import java.nio.charset.Charset

class WriteFileTool : Tool {
    override val name = "write_file"
    override val description = "Write content to a file at the specified path. Creates the file if it does not exist, overwrites if it does."

    override fun validate(params: Map<String, Any?>): Result<Unit> {
        val validator = ParameterValidator(params)
        val pathResult = validator.requireString("path")
        if (pathResult.isFailure) {
            return Result.Failure((pathResult as Result.Failure).error, pathResult.context)
        }

        val contentResult = validator.requireString("content")
        if (contentResult.isFailure) {
            return Result.Failure((contentResult as Result.Failure).error, contentResult.context)
        }

        return Result.Success(Unit)
    }

    override suspend fun execute(context: Context, params: Map<String, Any?>): ToolResult {
        val validator = ParameterValidator(params)
        val path = (validator.requireString("path") as Result.Success).value
        val content = (validator.requireString("content") as Result.Success).value
        val encoding = validator.optionalString("encoding", "utf-8")

        val file = File(path)

        return try {
            val charset = try {
                Charset.forName(encoding)
            } catch (e: Exception) {
                Charset.defaultCharset()
            }

            // Create parent directories if needed
            file.parentFile?.mkdirs()

            file.writeText(content, charset)

            ToolResult.success(mapOf(
                "bytes_written" to content.toByteArray(charset).size
            ))
        } catch (e: Exception) {
            ToolResult.failure(ToolError.WRITE_ERROR, "${e.message}")
        }
    }
}