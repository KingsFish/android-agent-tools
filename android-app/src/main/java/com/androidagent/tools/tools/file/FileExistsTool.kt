package com.androidagent.tools.tools.file

import com.androidagent.core.*
import java.io.File

class FileExistsTool : Tool {
    override val name = "file_exists"
    override val description = "Check if a file or directory exists at the specified path."
    override val inputSchema = ToolSchema(
        properties = mapOf(
            "path" to SchemaProperty(type = "string", description = "Absolute path to check")
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