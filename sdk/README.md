# Android Agent Tools SDK

A Kotlin SDK for building AI agent applications on Android.

## Installation

Add to your `build.gradle.kts`:

```kotlin
implementation("com.androidagent:tools:1.0.0")
```

## Usage

### Initialize

```kotlin
val tools = AndroidAgentTools(context)
```

### List Available Tools

```kotlin
val availableTools = tools.listTools()
// ["read_file", "write_file", "list_directory", ...]
```

### Execute Tool

```kotlin
// Using Map
val result = tools.execute("read_file", mapOf(
    "path" to "/sdcard/test.txt"
))

when (result) {
    is ToolResult.Success -> {
        val content = result.data["content"]
        println(content)
    }
    is ToolResult.Failure -> {
        println("Error: ${result.error.message}")
    }
}
```

### JSON API

```kotlin
val jsonInput = """{"path": "/sdcard/test.txt"}"""
val jsonOutput = tools.executeJson("read_file", jsonInput)
// {"success":true,"data":{"content":"Hello","size":5}}
```

## Available Tools

### File Operations

| Tool | Description |
|------|-------------|
| `read_file` | Read file content |
| `write_file` | Write content to file |
| `list_directory` | List directory contents |
| `delete_file` | Delete a file |
| `file_exists` | Check if file exists |

### App Operations

| Tool | Description |
|------|-------------|
| `list_apps` | List installed apps |
| `get_app_info` | Get app details |
| `launch_app` | Launch an app |

### System Info

| Tool | Description |
|------|-------------|
| `get_device_info` | Get device information |
| `get_battery_status` | Get battery status |

### Permissions

| Tool | Description |
|------|-------------|
| `check_permissions` | Check permission status |

## Error Handling

All errors use the `ToolError` enum:

```kotlin
when (result) {
    is ToolResult.Failure -> {
        when (result.error) {
            ToolError.FILE_NOT_FOUND -> // Handle
            ToolError.PERMISSION_DENIED -> // Handle
            // ...
        }
    }
}
```

## Testing

The SDK is designed to be testable. Each tool can be unit tested independently.