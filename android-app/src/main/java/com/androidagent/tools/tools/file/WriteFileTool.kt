package com.androidagent.tools.tools.file

import com.androidagent.core.*
import java.io.File
import java.nio.charset.Charset

class WriteFileTool : Tool {
    override val name = "write_file"
    override val description = "Write content to a file at the specified path. Creates the file if it does not exist, overwrites if it does."
    override val inputSchema = ToolSchema(
        properties = mapOf(
            "path" to SchemaProperty(type = "string", description = "Absolute path to the file"),
            "content" to SchemaProperty(type = "string", description = "Content to write to the file"),
            "encoding" to SchemaProperty(type = "string", description = "File encoding (default: utf-8)", default = "utf-8")
        ),
        required = listOf("path", "content")
    )

    override fun validate(params: Map<String, Any?>): ValidationResult {
        val validator = ParameterValidator(params)
        val pathResult = validator.requireString("path")
        if (pathResult.isFailure) {
            return pathResult
        }
        return validator.requireString("content")
    }

    override suspend fun execute(context: ToolContext, params: Map<String, Any?>): ToolResult {
        val validator = ParameterValidator(params)
        val path = validator.getString("path") ?: return ToolResult.failure(ToolError.INVALID_PARAMETER, "Missing path")
        val content = validator.getString("content") ?: return ToolResult.failure(ToolError.INVALID_PARAMETER, "Missing content")
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