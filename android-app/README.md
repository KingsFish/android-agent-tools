# Android Agent Tools SDK

A Kotlin SDK for building AI agent applications on Android.

## Installation

Add to your `build.gradle.kts`:

```kotlin
implementation("com.androidagent:tools:2.0.0")
```

## Quick Start

### Initialize

```kotlin
val tools = AndroidAgentTools(context)
```

### List Available Tools

```kotlin
val availableTools = tools.listTools()
// Returns: ["read_file", "write_file", "list_directory", "delete_file",
//           "file_exists", "list_apps", "get_app_info", "launch_app", ...]
```

### Execute Tool

```kotlin
// Using Map (suspend function)
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

### JSON API (Blocking)

```kotlin
val jsonInput = """{"path": "/sdcard/test.txt"}"""
val jsonOutput = tools.executeJson("read_file", jsonInput)
// {"success":true,"data":{"content":"Hello","size":5}}
```

## Available Tools

### Tier 1: Core Capabilities (11 tools)

**File Operations (5 tools)**

| Tool | Description | Parameters |
|------|-------------|------------|
| `read_file` | Read file content | `path` (required), `encoding` (optional) |
| `write_file` | Write content to file | `path`, `content` (required), `encoding` (optional) |
| `list_directory` | List directory contents | `path` (required) |
| `delete_file` | Delete a file | `path` (required) |
| `file_exists` | Check if file exists | `path` (required) |

**App Operations (3 tools)**

| Tool | Description | Parameters |
|------|-------------|------------|
| `list_apps` | List installed apps | `include_system_apps` (optional) |
| `get_app_info` | Get app details | `package_name` (required) |
| `launch_app` | Launch an app | `package_name` (required) |

**System Info (2 tools)**

| Tool | Description | Parameters |
|------|-------------|------------|
| `get_device_info` | Get device information | None |
| `get_battery_status` | Get battery status | None |

**Permissions (1 tool)**

| Tool | Description | Parameters |
|------|-------------|------------|
| `check_permissions` | Check permission status | `permissions` (array, required) |

### Tier 2: UI Interaction & App Management (8 tools)

**UI Interaction (5 tools)**

| Tool | Description | Parameters |
|------|-------------|------------|
| `get_ui_tree` | Get UI hierarchy | `max_depth`, `include_invisible` (optional) |
| `tap` | Tap at coordinates | `x`, `y` (required) |
| `swipe` | Swipe gesture | `start_x`, `start_y`, `end_x`, `end_y` (required), `duration` (optional) |
| `input_text` | Input text | `text` (required) |
| `take_screenshot` | Take screenshot | None |

**App Management (3 tools)**

| Tool | Description | Parameters |
|------|-------------|------------|
| `install_app` | Install APK | `apk_path` (required) |
| `uninstall_app` | Uninstall app | `package_name` (required) |
| `force_stop_app` | Force stop app | `package_name` (required) |

### Tier 3: Navigation, Wait & Advanced (14 tools)

**Navigation (3 tools)**

| Tool | Description | Parameters |
|------|-------------|------------|
| `press_back` | Press back key | None |
| `press_home` | Press home key | None |
| `press_recents` | Press recents key | None |

**Input (3 tools)**

| Tool | Description | Parameters |
|------|-------------|------------|
| `press_key` | Send key event | `key_name` or `key_code` (required) |
| `long_press` | Long press gesture | `x`, `y` (required), `duration` (optional) |
| `drag` | Drag gesture | `from_x`, `from_y`, `to_x`, `to_y` (required), `duration` (optional) |

**Wait (2 tools)**

| Tool | Description | Parameters |
|------|-------------|------------|
| `wait_for_ui_stable` | Wait for UI stability | `timeout` (optional, default 5000ms) |
| `wait_for_element` | Wait for element to appear | `text` (required), `timeout` (optional) |

**State Query (2 tools)**

| Tool | Description | Parameters |
|------|-------------|------------|
| `get_current_app` | Get foreground app | None |
| `is_app_running` | Check if app is running | `package_name` (required) |

**Clipboard (2 tools)**

| Tool | Description | Parameters |
|------|-------------|------------|
| `get_clipboard` | Get clipboard content | None |
| `set_clipboard` | Set clipboard content | `text` (required) |

**Node Interaction (2 tools)**

| Tool | Description | Parameters |
|------|-------------|------------|
| `click_node_by_text` | Click node by text | `text` (required), `exact` (optional) |
| `click_node_by_id` | Click node by resource ID | `resource_id` (required) |

## Capability Requirements

Different tools require different device capabilities:

| Capability | Required For |
|------------|--------------|
| **None** | File operations (private dir), app info, system info, permissions |
| **Storage Permission** | File operations (shared storage) |
| **ROOT Access** | App management (install/uninstall/force_stop), screenshot (alternative) |
| **Accessibility Service** | UI interaction, navigation, wait, clipboard, node interaction |
| **Media Projection** | Screenshot (non-ROOT, Android 11+) |

### Environment Detection

```kotlin
val envDetector = EnvironmentDetector(context)
val hasRoot = envDetector.hasRoot()
val hasAccessibility = envDetector.hasCapability(Capability.ACCESSIBILITY_SERVICE)
```

## Error Handling

All errors use the `ToolError` enum:

```kotlin
when (result) {
    is ToolResult.Failure -> {
        when (result.error) {
            ToolError.FILE_NOT_FOUND -> // Handle file not found
            ToolError.PERMISSION_DENIED -> // Handle permission denied
            ToolError.ACCESSIBILITY_SERVICE_REQUIRED -> // Need accessibility service
            ToolError.ROOT_REQUIRED -> // Need ROOT access
            // ...
        }
        println("Context: ${result.context}")
    }
}
```

### Common Error Codes

| Code | Description |
|------|-------------|
| `FILE_NOT_FOUND` | File does not exist |
| `PERMISSION_DENIED` | Required permission not granted |
| `ACCESSIBILITY_SERVICE_REQUIRED` | Accessibility service not enabled |
| `ROOT_REQUIRED` | ROOT access required |
| `INVALID_PARAMETER` | Invalid parameter provided |
| `GESTURE_FAILED` | Gesture execution failed |
| `ELEMENT_NOT_FOUND` | UI element not found |

## Testing

The SDK includes comprehensive unit tests. Run with:

```bash
./gradlew :sdk:test
```

## Version

Current version: **2.0.0**

- Tier 1: 11 tools (core capabilities)
- Tier 2: 8 tools (UI interaction & app management)
- Tier 3: 14 tools (navigation, wait & advanced)
- Total: 33 tools

## See Also

- [Design Document](../docs/design.md) - Full specification and interface definitions
- [README.md](../README.md) - Project overview and MCP Server usage