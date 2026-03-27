package com.androidagent.mcp.server

/**
 * Generates JSON Schema for tools based on their metadata.
 */
object ToolSchemaGenerator {

    /**
     * Generate a JSON Schema for a tool.
     * This creates a schema definition that describes the tool's parameters.
     */
    fun generateToolSchema(toolName: String): Map<String, Any?> {
        // Map of tool names to their schema definitions
        // These schemas describe the parameters each tool accepts
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
                "description" to "Write content to a file at the specified path. Creates the file if it does not exist, overwrites if it does.",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "path" to mapOf("type" to "string", "description" to "Absolute path to the file"),
                        "content" to mapOf("type" to "string", "description" to "Content to write to the file"),
                        "encoding" to mapOf("type" to "string", "description" to "File encoding (default: utf-8)", "default" to "utf-8")
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
                "description" to "Check if a file or directory exists at the specified path.",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "path" to mapOf("type" to "string", "description" to "Absolute path to check")
                    ),
                    "required" to listOf("path")
                )
            ),

            // Tier 1: App operations
            "list_apps" to mapOf(
                "name" to "list_apps",
                "description" to "List all installed applications on the device.",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "include_system_apps" to mapOf("type" to "boolean", "description" to "Whether to include system apps (default: false)", "default" to false)
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
                        "package_name" to mapOf("type" to "string", "description" to "Package name of the application")
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
                        "package_name" to mapOf("type" to "string", "description" to "Package name of the application to launch")
                    ),
                    "required" to listOf("package_name")
                )
            ),

            // Tier 1: System info
            "get_device_info" to mapOf(
                "name" to "get_device_info",
                "description" to "Get device information including model, OS version, screen size, etc.",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf<String, Any>(),
                    "required" to listOf<String>()
                )
            ),
            "get_battery_status" to mapOf(
                "name" to "get_battery_status",
                "description" to "Get current battery status including level, charging state, etc.",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf<String, Any>(),
                    "required" to listOf<String>()
                )
            ),

            // Tier 1: Permissions
            "check_permissions" to mapOf(
                "name" to "check_permissions",
                "description" to "Check the status of specified permissions.",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "permissions" to mapOf(
                            "type" to "array",
                            "description" to "List of permissions to check (storage, camera, location, microphone, contacts, sms, phone)",
                            "items" to mapOf("type" to "string")
                        )
                    ),
                    "required" to listOf("permissions")
                )
            ),

            // Tier 2: UI interaction
            "take_screenshot" to mapOf(
                "name" to "take_screenshot",
                "description" to "Take a screenshot of the current screen.",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf<String, Any>(),
                    "required" to listOf<String>()
                )
            ),
            "tap" to mapOf(
                "name" to "tap",
                "description" to "Perform a tap at the specified coordinates.",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "x" to mapOf("type" to "integer", "description" to "X coordinate (absolute pixels)"),
                        "y" to mapOf("type" to "integer", "description" to "Y coordinate (absolute pixels)")
                    ),
                    "required" to listOf("x", "y")
                )
            ),
            "swipe" to mapOf(
                "name" to "swipe",
                "description" to "Perform a swipe gesture from start to end coordinates.",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "start_x" to mapOf("type" to "integer", "description" to "Start X coordinate"),
                        "start_y" to mapOf("type" to "integer", "description" to "Start Y coordinate"),
                        "end_x" to mapOf("type" to "integer", "description" to "End X coordinate"),
                        "end_y" to mapOf("type" to "integer", "description" to "End Y coordinate"),
                        "duration" to mapOf("type" to "integer", "description" to "Duration in milliseconds (default: 300)", "default" to 300)
                    ),
                    "required" to listOf("start_x", "start_y", "end_x", "end_y")
                )
            ),
            "input_text" to mapOf(
                "name" to "input_text",
                "description" to "Input text into the currently focused field.",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "text" to mapOf("type" to "string", "description" to "Text to input")
                    ),
                    "required" to listOf("text")
                )
            ),
            "get_ui_tree" to mapOf(
                "name" to "get_ui_tree",
                "description" to "Get the UI tree structure of the current screen.",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "max_depth" to mapOf("type" to "integer", "description" to "Maximum depth to traverse (default: 10)", "default" to 10),
                        "include_invisible" to mapOf("type" to "boolean", "description" to "Whether to include invisible nodes (default: false)", "default" to false)
                    ),
                    "required" to listOf<String>()
                )
            ),

            // Tier 2: App management
            "install_app" to mapOf(
                "name" to "install_app",
                "description" to "Install an application from an APK file.",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "apk_path" to mapOf("type" to "string", "description" to "Path to the APK file")
                    ),
                    "required" to listOf("apk_path")
                )
            ),
            "uninstall_app" to mapOf(
                "name" to "uninstall_app",
                "description" to "Uninstall an application.",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "package_name" to mapOf("type" to "string", "description" to "Package name of the application to uninstall")
                    ),
                    "required" to listOf("package_name")
                )
            ),
            "force_stop_app" to mapOf(
                "name" to "force_stop_app",
                "description" to "Force stop an application.",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "package_name" to mapOf("type" to "string", "description" to "Package name of the application to stop")
                    ),
                    "required" to listOf("package_name")
                )
            ),

            // Tier 3: Navigation
            "press_back" to mapOf(
                "name" to "press_back",
                "description" to "Press the back button.",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf<String, Any>(),
                    "required" to listOf<String>()
                )
            ),
            "press_home" to mapOf(
                "name" to "press_home",
                "description" to "Press the home button.",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf<String, Any>(),
                    "required" to listOf<String>()
                )
            ),
            "press_recents" to mapOf(
                "name" to "press_recents",
                "description" to "Press the recents (task switcher) button.",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf<String, Any>(),
                    "required" to listOf<String>()
                )
            ),

            // Tier 3: Input
            "press_key" to mapOf(
                "name" to "press_key",
                "description" to "Send a key event. Supported keys: back, home, enter, delete, volume_up, volume_down, menu, search, dpad_up, dpad_down, dpad_left, dpad_right, dpad_center.",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "key" to mapOf("type" to "string", "description" to "Key name to press")
                    ),
                    "required" to listOf("key")
                )
            ),
            "long_press" to mapOf(
                "name" to "long_press",
                "description" to "Perform a long press at the specified coordinates.",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "x" to mapOf("type" to "integer", "description" to "X coordinate"),
                        "y" to mapOf("type" to "integer", "description" to "Y coordinate"),
                        "duration" to mapOf("type" to "integer", "description" to "Duration in milliseconds (default: 1000)", "default" to 1000)
                    ),
                    "required" to listOf("x", "y")
                )
            ),
            "drag" to mapOf(
                "name" to "drag",
                "description" to "Perform a drag gesture from one point to another.",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "from_x" to mapOf("type" to "integer", "description" to "Start X coordinate"),
                        "from_y" to mapOf("type" to "integer", "description" to "Start Y coordinate"),
                        "to_x" to mapOf("type" to "integer", "description" to "End X coordinate"),
                        "to_y" to mapOf("type" to "integer", "description" to "End Y coordinate"),
                        "duration" to mapOf("type" to "integer", "description" to "Duration in milliseconds (default: 500)", "default" to 500)
                    ),
                    "required" to listOf("from_x", "from_y", "to_x", "to_y")
                )
            ),

            // Tier 3: Wait
            "wait_for_ui_stable" to mapOf(
                "name" to "wait_for_ui_stable",
                "description" to "Wait for the UI to become stable (no changes in UI tree).",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "timeout_ms" to mapOf("type" to "integer", "description" to "Timeout in milliseconds (default: 5000)", "default" to 5000),
                        "check_interval_ms" to mapOf("type" to "integer", "description" to "Check interval in milliseconds (default: 500)", "default" to 500)
                    ),
                    "required" to listOf<String>()
                )
            ),
            "wait_for_element" to mapOf(
                "name" to "wait_for_element",
                "description" to "Wait for an element with specific text to appear on the screen.",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "text" to mapOf("type" to "string", "description" to "Text to search for"),
                        "timeout_ms" to mapOf("type" to "integer", "description" to "Timeout in milliseconds (default: 5000)", "default" to 5000)
                    ),
                    "required" to listOf("text")
                )
            ),

            // Tier 3: State
            "get_current_app" to mapOf(
                "name" to "get_current_app",
                "description" to "Get the current foreground app package name.",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf<String, Any>(),
                    "required" to listOf<String>()
                )
            ),
            "is_app_running" to mapOf(
                "name" to "is_app_running",
                "description" to "Check if an app is running.",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "package_name" to mapOf("type" to "string", "description" to "Package name to check")
                    ),
                    "required" to listOf("package_name")
                )
            ),

            // Tier 3: Clipboard
            "get_clipboard" to mapOf(
                "name" to "get_clipboard",
                "description" to "Get the current clipboard content.",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf<String, Any>(),
                    "required" to listOf<String>()
                )
            ),
            "set_clipboard" to mapOf(
                "name" to "set_clipboard",
                "description" to "Set the clipboard content.",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "text" to mapOf("type" to "string", "description" to "Text to set in clipboard")
                    ),
                    "required" to listOf("text")
                )
            ),

            // Tier 3: Node interaction
            "click_node_by_text" to mapOf(
                "name" to "click_node_by_text",
                "description" to "Click a UI node by its text content.",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "text" to mapOf("type" to "string", "description" to "Text to search for"),
                        "exact" to mapOf("type" to "boolean", "description" to "Whether to match exact text (default: false)", "default" to false)
                    ),
                    "required" to listOf("text")
                )
            ),
            "click_node_by_id" to mapOf(
                "name" to "click_node_by_id",
                "description" to "Click a UI node by its resource ID.",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "resource_id" to mapOf("type" to "string", "description" to "Resource ID to search for")
                    ),
                    "required" to listOf("resource_id")
                )
            )
        )

        return schemas[toolName] ?: mapOf(
            "name" to toolName,
            "description" to "Unknown tool",
            "inputSchema" to mapOf(
                "type" to "object",
                "properties" to mapOf<String, Any>()
            )
        )
    }
}