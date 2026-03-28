package com.androidagent.tools.tools.file

import com.androidagent.core.*
import com.androidagent.androidapp.AppToolContext
import java.io.File
import java.nio.charset.Charset

class ReadFileTool : Tool {
    override val name = "read_file"
    override val description = "Read the content of a file at the specified path. Only supports text files."
    override val inputSchema = ToolSchema(
        properties = mapOf(
            "path" to SchemaProperty(type = "string", description = "Absolute path to the file"),
            "encoding" to SchemaProperty(type = "string", description = "File encoding (default: utf-8)", default = "utf-8")
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