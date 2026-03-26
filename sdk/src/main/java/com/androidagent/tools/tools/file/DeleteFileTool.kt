package com.androidagent.tools.tools.file

import android.content.Context
import com.androidagent.tools.core.*
import java.io.File

class DeleteFileTool : Tool {
    override val name = "delete_file"
    override val description = "Delete a file at the specified path."

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

        if (!file.exists()) {
            return ToolResult.failure(ToolError.FILE_NOT_FOUND, path)
        }

        return try {
            val deleted = file.delete()
            if (deleted) {
                ToolResult.success(emptyMap())
            } else {
                ToolResult.failure(ToolError.WRITE_ERROR, "Failed to delete file: $path")
            }
        } catch (e: Exception) {
            ToolResult.failure(ToolError.WRITE_ERROR, "${e.message}")
        }
    }
}