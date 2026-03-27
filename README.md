# Android Agent Tools

A standardized toolkit for LLM agents to interact with Android devices.

## Overview

This project provides a set of tools that enable AI agents to operate Android devices, similar to how desktop agents use `read_file`/`write_file` to interact with computer systems.

## Quick Start

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

## SDK Documentation

See [sdk/README.md](sdk/README.md) for detailed SDK usage.

## Documentation

- [Design Document](docs/design.md) - Full specification and interface definitions

## Project Structure

```
android-agent-tools/
├── sdk/                    # Android SDK
│   ├── src/main/          # Source code
│   └── src/test/          # Unit tests
├── schemas/               # JSON schemas for tools
└── docs/                  # Documentation
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

### Tier 2 Requirements

- **UI interaction tools** require ROOT access or Accessibility Service enabled
- **App management tools** require ROOT access
- **Screenshot** on non-ROOT devices requires Android 11+ (API 30)

### Tier 2 Tool Parameters

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

## License

MIT