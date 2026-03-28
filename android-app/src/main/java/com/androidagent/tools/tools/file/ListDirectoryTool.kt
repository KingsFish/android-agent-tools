package com.androidagent.tools.tools.file

import com.androidagent.core.*
import java.io.File

class ListDirectoryTool : Tool {
    override val name = "list_directory"
    override val description = "List contents of a directory."
    override val inputSchema = ToolSchema(
        properties = mapOf(
            "path" to SchemaProperty(type = "string", description = "Absolute path to the directory")
        ),
        required = listOf("path")
    )

    override fun validate(params: Map<String, Any?>): ValidationResult {
        val validator = ParameterValidator(params)
        return validator.requireString("path")
    }

    override suspend fun execute(context: ToolContext, params: Map<String, Any?>): ToolResult {
        val validator = ParameterValidator(params)
        val path = validator.getString("path") ?: return ToolResult.failure(ToolError.INVALID_PARAMETER, "Missing path")

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