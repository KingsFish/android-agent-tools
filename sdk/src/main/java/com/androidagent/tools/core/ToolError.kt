package com.androidagent.tools.core

enum class ToolError(val message: String) {
    // General errors
    PERMISSION_DENIED("Permission not granted"),
    INVALID_PARAMETER("Invalid parameter provided"),
    UNSUPPORTED_OPERATION("Operation not supported in current environment"),

    // File operation errors
    FILE_NOT_FOUND("File does not exist"),
    DIRECTORY_NOT_FOUND("Directory does not exist"),
    NOT_A_FILE("Path is not a file"),
    NOT_A_DIRECTORY("Path is not a directory"),
    FILE_ALREADY_EXISTS("File already exists"),
    STORAGE_FULL("Storage space insufficient"),
    READ_ERROR("Failed to read file"),
    WRITE_ERROR("Failed to write file"),

    // App operation errors
    APP_NOT_FOUND("Application not found"),
    APP_NOT_LAUNCHABLE("Application cannot be launched"),
    LAUNCH_FAILED("Failed to launch application");

    fun withContext(context: String): String = "$message: $context"
}