package com.androidagent.tools.tools.file

import android.content.Context
import com.androidagent.tools.core.*
import java.io.File

class FileExistsTool : Tool {
    override val name = "file_exists"
    override val description = "Check if a file or directory exists at the specified path."

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

        val file = File(path)

        val exists = file.exists()
        val type = when {
            !exists -> null
            file.isDirectory -> "directory"
            else -> "file"
        }

        return ToolResult.success(mapOf(
            "exists" to exists,
            "type" to type
        ))
    }
}