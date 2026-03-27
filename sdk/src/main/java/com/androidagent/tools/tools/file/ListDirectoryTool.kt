package com.androidagent.tools.tools.file

import android.content.Context
import com.androidagent.tools.core.*
import java.io.File

class ListDirectoryTool : Tool {
    override val name = "list_directory"
    override val description = "List contents of a directory."

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

        val dir = File(path)

        if (!dir.exists()) {
            return ToolResult.failure(ToolError.DIRECTORY_NOT_FOUND, path)
        }

        if (!dir.isDirectory) {
            return ToolResult.failure(ToolError.NOT_A_DIRECTORY, path)
        }

        return try {
            val entries = dir.listFiles()?.map { file ->
                mapOf(
                    "name" to file.name,
                    "type" to if (file.isDirectory) "directory" else "file",
                    "size" to if (file.isFile) file.length() else null
                )
            } ?: emptyList()

            ToolResult.success(mapOf("entries" to entries))
        } catch (e: Exception) {
            ToolResult.failure(ToolError.READ_ERROR, "${e.message}")
        }
    }
}