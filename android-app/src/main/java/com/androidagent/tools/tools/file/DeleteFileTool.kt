package com.androidagent.tools.tools.file

import com.androidagent.core.*
import java.io.File

class DeleteFileTool : Tool {
    override val name = "delete_file"
    override val description = "Delete a file at the specified path."
    override val inputSchema = ToolSchema(
        properties = mapOf(
            "path" to SchemaProperty(type = "string", description = "Absolute path to the file")
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