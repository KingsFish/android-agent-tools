package com.androidagent.mcp.server

/**
 * Generates JSON Schema for tools based on their metadata.
 * Uses hardcoded schemas for MCP protocol compatibility.
 */
object ToolSchemaGenerator {

    /**
     * Generate a JSON Schema for a tool by name.
     */
    fun generateToolSchema(toolName: String): Map<String, Any?> {
        val schemas = mapOf(
            // Tier 1: File operations
            "read_file" to mapOf(
                "name" to "read_file",
                "description" to "Read the content of a file at the specified path. Only supports text files.",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "path" to mapOf("type" to "string", "description" to "Absolute path to the file"),
                        "encoding" to mapOf("type" to "string", "description" to "File encoding (default: utf-8)", "default" to "utf-8")
                    ),
                    "required" to listOf("path")
                )
            ),
            "write_file" to mapOf(
                "name" to "write_file",
                "description" to "Write content to a file at the specified path.",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "path" to mapOf("type" to "string", "description" to "Absolute path to the file"),
                        "content" to mapOf("type" to "string", "description" to "Content to write"),
                        "encoding" to mapOf("type" to "string", "description" to "File encoding", "default" to "utf-8")
                    ),
                    "required" to listOf("path", "content")
                )
            ),
            "list_directory" to mapOf(
                "name" to "list_directory",
                "description" to "List contents of a directory.",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "path" to mapOf("type" to "string", "description" to "Absolute path to the directory")
                    ),
                    "required" to listOf("path")
                )
            ),
            "delete_file" to mapOf(
                "name" to "delete_file",
                "description" to "Delete a file at the specified path.",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "path" to mapOf("type" to "string", "description" to "Absolute path to the file")
                    ),
                    "required" to listOf("path")
                )
            ),
            "file_exists" to mapOf(
                "name" to "file_exists",
                "description" to "Check if a file or directory exists.",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "path" to mapOf("type" to "string", "description" to "Absolute path to check")
                    ),
                    "required" to listOf("path")
                )
            ),
            "list_apps" to mapOf(
                "name" to "list_apps",
                "description" to "List all installed applications.",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "include_system_apps" to mapOf("type" to "boolean", "description" to "Include system apps", "default" to false)
                    ),
                    "required" to listOf<String>()
                )
            ),
            "get_app_info" to mapOf(
                "name" to "get_app_info",
                "description" to "Get detailed information about an application.",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "package_name" to mapOf("type" to "string", "description" to "Package name")
                    ),
                    "required" to listOf("package_name")
                )
            ),
            "launch_app" to mapOf(
                "name" to "launch_app",
                "description" to "Launch an application by package name.",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "package_name" to mapOf("type" to "string", "description" to "Package name to launch")
                    ),
                    "required" to listOf("package_name")
                )
            ),
            "get_device_info" to mapOf(
                "name" to "get_device_info",
                "description" to "Get device information.",
                "inputSchema" to mapOf("type" to "object", "properties" to mapOf<String, Any>(), "required" to listOf<String>())
            ),
            "get_battery_status" to mapOf(
                "name" to "get_battery_status",
                "description" to "Get current battery status.",
                "inputSchema" to mapOf("type" to "object", "properties" to mapOf<String, Any>(), "required" to listOf<String>())
            ),
            "check_permissions" to mapOf(
                "name" to "check_permissions",
                "description" to "Check the status of specified permissions.",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "permissions" to mapOf("type" to "array", "description" to "Permissions to check", "items" to mapOf("type" to "string"))
                    ),
                    "required" to listOf("permissions")
                )
            ),
            "take_screenshot" to mapOf(
                "name" to "take_screenshot",
                "description" to "Take a screenshot.",
                "inputSchema" to mapOf("type" to "object", "properties" to mapOf<String, Any>(), "required" to listOf<String>())
            ),
            "tap" to mapOf(
                "name" to "tap",
                "description" to "Perform a tap at specified coordinates.",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "x" to mapOf("type" to "integer", "description" to "X coordinate"),
                        "y" to mapOf("type" to "integer", "description" to "Y coordinate")
                    ),
                    "required" to listOf("x", "y")
                )
            ),
            "swipe" to mapOf(
                "name" to "swipe",
                "description" to "Perform a swipe gesture.",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "start_x" to mapOf("type" to "integer", "description" to "Start X"),
                        "start_y" to mapOf("type" to "integer", "description" to "Start Y"),
                        "end_x" to mapOf("type" to "integer", "description" to "End X"),
                        "end_y" to mapOf("type" to "integer", "description" to "End Y"),
                        "duration" to mapOf("type" to "integer", "description" to "Duration ms", "default" to 300)
                    ),
                    "required" to listOf("start_x", "start_y", "end_x", "end_y")
                )
            ),
            "input_text" to mapOf(
                "name" to "input_text",
                "description" to "Input text into focused field.",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf("text" to mapOf("type" to "string", "description" to "Text to input")),
                    "required" to listOf("text")
                )
            ),
            "get_ui_tree" to mapOf(
                "name" to "get_ui_tree",
                "description" to "Get the UI tree structure.",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "max_depth" to mapOf("type" to "integer", "description" to "Max depth", "default" to 10),
                        "include_invisible" to mapOf("type" to "boolean", "description" to "Include invisible", "default" to false)
                    ),
                    "required" to listOf<String>()
                )
            ),
            "press_back" to mapOf("name" to "press_back", "description" to "Press back button.", "inputSchema" to mapOf("type" to "object", "properties" to mapOf<String, Any>(), "required" to listOf<String>())),
            "press_home" to mapOf("name" to "press_home", "description" to "Press home button.", "inputSchema" to mapOf("type" to "object", "properties" to mapOf<String, Any>(), "required" to listOf<String>())),
            "press_recents" to mapOf("name" to "press_recents", "description" to "Press recents button.", "inputSchema" to mapOf("type" to "object", "properties" to mapOf<String, Any>(), "required" to listOf<String>()))
        )

        return schemas[toolName] ?: mapOf("name" to toolName, "description" to "Unknown tool", "inputSchema" to mapOf("type" to "object", "properties" to mapOf<String, Any>()))
    }
}
