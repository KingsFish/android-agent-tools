package com.androidagent.tools.tools.file

import android.content.Context
import com.androidagent.tools.core.*
import java.io.File
import java.nio.charset.Charset

class ReadFileTool : Tool {
    override val name = "read_file"
    override val description = "Read the content of a file at the specified path. Only supports text files."

    override fun validate(params: Map<String, Any?>): Result<Unit> {
        val validator = ParameterValidator(params)
        val pathResult = validator.requireString("path")
        return when (pathResult) {
            is Result.Success -> Result.Success(Unit)
            is Result.Failure -> Result.Failure(pathResult.error, pathResult.context)
        }
    }

    override suspend fun execute(context: Context, params: Map<String, Any?>): ToolResult {
        val validator = ParameterValidator(params)
        val path = (validator.requireString("path") as Result.Success).value
        val encoding = validator.optionalString("encoding", "utf-8")

        val file = File(path)

        if (!file.exists()) {
            return ToolResult.failure(ToolError.FILE_NOT_FOUND, path)
        }

        if (!file.isFile) {
            return ToolResult.failure(ToolError.NOT_A_FILE, path)
        }

        return try {
            val charset = try {
                Charset.forName(encoding)
            } catch (e: Exception) {
                Charset.defaultCharset()
            }
            val content = file.readText(charset)
            ToolResult.success(mapOf(
                "content" to content,
                "size" to content.toByteArray(charset).size
            ))
        } catch (e: Exception) {
            ToolResult.failure(ToolError.READ_ERROR, "${e.message}")
        }
    }
}