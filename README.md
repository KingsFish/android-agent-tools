# Android Agent Tools

A standardized toolkit for LLM agents to interact with Android devices.

## Overview

This project provides a set of tools that enable AI agents to operate Android devices, similar to how desktop agents use `read_file`/`write_file` to interact with computer systems.

## Delivery Forms

| Form | Status | Description |
|------|--------|-------------|
| **Android SDK** | ✅ Available | Integrate into your own Android app |
| **MCP Server App** | ✅ Available | Standalone app exposing tools via HTTP |

## Quick Start

### Using SDK

```kotlin
// Initialize
val tools = AndroidAgentTools(context)

// List available tools
val availableTools = tools.listTools()

// Execute a tool
val result = tools.execute("read_file", mapOf("path" to "/sdcard/test.txt"))

// Or use JSON
val jsonResult = tools.executeJson("read_file", """{"path": "/sdcard/test.txt"}""")
```

### Using MCP Server

1. Build and install the MCP Server app:
```bash
./gradlew :mcp-server:installDebug
```

2. Open the app, grant Accessibility permission (for UI tools), and tap "Start Server"

3. Access the server via HTTP:

**WiFi (same network as your computer):**
```bash
# Get your phone's IP from the app
curl -X POST http://<phone-ip>:8080/mcp/tools/list
curl -X POST http://<phone-ip>:8080/mcp/tools/call \
  -H "Content-Type: application/json" \
  -d '{"name": "get_device_info", "arguments": {}}'
```

**USB (ADB port forwarding):**
```bash
adb forward tcp:8080 tcp:8080
curl -X POST http://localhost:8080/mcp/tools/list
```

## SDK Documentation

See [sdk/README.md](sdk/README.md) for detailed SDK usage.

## Documentation

- [Design Document](docs/design.md) - Full specification and interface definitions

## Project Structure

```
android-agent-tools/
├── sdk/                    # Android SDK (library module)
│   ├── src/main/          # Source code
│   └── src/test/          # Unit tests
├── mcp-server/            # MCP Server App (application module)
│   └── src/main/          # HTTP server, UI, and service
├── schemas/               # JSON schemas for tools
└── docs/                  # Documentation
```

## MCP Protocol

The MCP Server implements the Model Context Protocol for tool discovery and execution:

### List Tools
```http
POST /mcp/tools/list
Content-Type: application/json

{}
```

Response:
```json
{
  "tools": [
    {
      "name": "tap",
      "description": "Perform a tap at the specified coordinates.",
      "inputSchema": {
        "type": "object",
        "properties": {
          "x": { "type": "integer", "description": "X coordinate" },
          "y": { "type": "integer", "description": "Y coordinate" }
        },
        "required": ["x", "y"]
      }
    }
  ]
}
```

### Call Tool
```http
POST /mcp/tools/call
Content-Type: application/json

{
  "name": "tap",
  "arguments": {
    "x": 500,
    "y": 800
  }
}
```

Response:
```json
{
  "content": [
    {
      "type": "text",
      "text": "{\"success\": true, \"data\": {}}"
    }
  ]
}
```

## Features

**Tier 1 - Core Capabilities:**
- File operations: `read_file`, `write_file`, `list_directory`, `delete_file`, `file_exists`
- App info: `list_apps`, `get_app_info`, `launch_app`
- System info: `get_device_info`, `get_battery_status`
- Permissions: `check_permissions`

**Tier 2 - UI Interaction & App Management:**
- UI interaction: `get_ui_tree`, `tap`, `swipe`, `input_text`, `take_screenshot`
- App management: `force_stop_app`, `uninstall_app`, `install_app`

**Tier 3 - Navigation, Wait & Advanced:**
- Navigation: `press_back`, `press_home`, `press_recents`
- Input: `press_key`, `long_press`, `drag`
- Wait: `wait_for_ui_stable`, `wait_for_element`
- State: `get_current_app`, `is_app_running`
- Clipboard: `get_clipboard`, `set_clipboard`
- Node: `click_node_by_text`, `click_node_by_id`

### Tier 2 Requirements

- **UI interaction tools** require ROOT access or Accessibility Service enabled
- **App management tools** require ROOT access
- **Screenshot** on non-ROOT devices requires Android 11+ (API 30)

### Tier 3 Requirements

- **Navigation tools** require ROOT access or Accessibility Service enabled
- **Wait tools** require Accessibility Service enabled
- **State tools** require Accessibility Service enabled
- **Clipboard tools** require Accessibility Service enabled
- **Node tools** require Accessibility Service enabled
- **press_key** for non-navigation keys requires ROOT access

### Tool Parameters

| Tool | Parameters |
|------|------------|
| `get_ui_tree` | `max_depth` (optional), `include_invisible` (optional) |
| `tap` | `x`, `y` (required) |
| `swipe` | `start_x`, `start_y`, `end_x`, `end_y`, `duration` (optional) |
| `input_text` | `text` (required) |
| `take_screenshot` | None |
| `force_stop_app` | `package_name` (required) |
| `uninstall_app` | `package_name` (required) |
| `install_app` | `apk_path` (required) |
| `press_key` | `key_name` or `key_code` (required) |
| `long_press` | `x`, `y` (required), `duration` (optional) |
| `drag` | `from_x`, `from_y`, `to_x`, `to_y`, `duration` (optional) |
| `wait_for_ui_stable` | `timeout` (optional, default 5000ms) |
| `wait_for_element` | `text` (required), `timeout` (optional) |
| `get_current_app` | None |
| `is_app_running` | `package_name` (required) |
| `get_clipboard` | None |
| `set_clipboard` | `text` (required) |
| `click_node_by_text` | `text` (required), `exact` (optional) |
| `click_node_by_id` | `resource_id` (required) |

## License

MIT