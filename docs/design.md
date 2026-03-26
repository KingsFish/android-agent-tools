# Android Agent Tools 设计文档

## 1. 项目概述

### 1.1 目标

为 LLM Agent 提供一套标准化的安卓平台工具集，使其能够操作安卓设备的文件、应用、系统信息等核心能力。

### 1.2 设计原则

- **接口与实现解耦**：工具接口统一，实现层可适配多种运行环境（本地 App、ADB、云端桥接）
- **统一格式**：JSON 输入输出，标准化返回结构
- **显式权限**：提供权限状态查询，Agent 自主决策引导用户授权
- **渐进式能力**：分层设计，首批支持核心能力，后续逐步扩展

---

## 2. 工具清单

### 2.1 Tier 1：核心能力（首批支持）

**文件操作（5个）**

| 工具 | 说明 |
|------|------|
| `read_file` | 读取文件内容 |
| `write_file` | 写入文件 |
| `list_directory` | 列出目录内容 |
| `delete_file` | 删除文件 |
| `file_exists` | 检查文件是否存在 |

**应用信息（3个）**

| 工具 | 说明 |
|------|------|
| `list_apps` | 获取已安装应用列表 |
| `get_app_info` | 获取应用详情 |
| `launch_app` | 启动应用 |

**系统信息（2个）**

| 工具 | 说明 |
|------|------|
| `get_device_info` | 获取设备信息 |
| `get_battery_status` | 获取电池状态 |

**权限查询（1个）**

| 工具 | 说明 |
|------|------|
| `check_permissions` | 查询权限状态 |

**共计 11 个工具。**

### 2.2 Tier 2：进阶能力（Roadmap）

**UI 交互**
| 工具 | 说明 |
|------|------|
| `take_screenshot` | 截图 |
| `tap` | 模拟点击 |
| `swipe` | 模拟滑动 |
| `input_text` | 输入文本 |
| `get_ui_tree` | 获取界面控件结构 |

**应用管理**
| 工具 | 说明 |
|------|------|
| `install_app` | 安装应用 |
| `uninstall_app` | 卸载应用 |
| `force_stop_app` | 强制停止应用 |

### 2.3 Tier 3：扩展能力（Roadmap）

| 领域 | 工具 |
|------|------|
| 剪贴板 | `get_clipboard`, `set_clipboard` |
| 通知管理 | `list_notifications`, `post_notification` |
| 媒体操作 | `take_photo`, `record_audio`, `play_media` |
| 传感器 | `get_sensor_data` |
| 短信/联系人 | `send_sms`, `list_contacts`（需敏感权限）|
| 定位 | `get_location` |

---

## 3. 接口规范

### 3.1 统一返回结构

**成功响应**
```json
{
  "success": true,
  "data": {
    // 工具返回的数据
  }
}
```

**失败响应**
```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable error message with context"
  }
}
```

### 3.2 命名规范

- 工具名称：`snake_case`（如 `read_file`, `list_apps`）
- 参数名称：`snake_case`（如 `package_name`, `include_system_apps`）
- 描述语言：英文

---

## 4. 工具接口定义

### 4.1 文件操作

#### read_file

Read the content of a file at the specified path. Only supports text files.

**Parameters**
| Name | Type | Required | Default | Description |
|------|------|----------|---------|-------------|
| path | string | true | - | Absolute path to the file |
| encoding | string | false | utf-8 | File encoding |

**Example**
```json
// Request
{"path": "/sdcard/test.txt"}

// Response
{
  "success": true,
  "data": {
    "content": "Hello, World!",
    "size": 13
  }
}
```

#### write_file

Write content to a file at the specified path. Creates the file if it does not exist, overwrites if it does.

**Parameters**
| Name | Type | Required | Default | Description |
|------|------|----------|---------|-------------|
| path | string | true | - | Absolute path to the file |
| content | string | true | - | Content to write to the file |
| encoding | string | false | utf-8 | File encoding |

**Example**
```json
// Request
{"path": "/sdcard/test.txt", "content": "Hello, World!"}

// Response
{
  "success": true,
  "data": {
    "bytes_written": 13
  }
}
```

#### list_directory

List contents of a directory.

**Parameters**
| Name | Type | Required | Default | Description |
|------|------|----------|---------|-------------|
| path | string | true | - | Absolute path to the directory |

**Example**
```json
// Request
{"path": "/sdcard/Download"}

// Response
{
  "success": true,
  "data": {
    "entries": [
      {"name": "file1.txt", "type": "file", "size": 1024},
      {"name": "folder1", "type": "directory"}
    ]
  }
}
```

#### delete_file

Delete a file at the specified path.

**Parameters**
| Name | Type | Required | Default | Description |
|------|------|----------|---------|-------------|
| path | string | true | - | Absolute path to the file |

**Example**
```json
// Request
{"path": "/sdcard/test.txt"}

// Response
{
  "success": true,
  "data": {}
}
```

#### file_exists

Check if a file or directory exists at the specified path.

**Parameters**
| Name | Type | Required | Default | Description |
|------|------|----------|---------|-------------|
| path | string | true | - | Absolute path to check |

**Example**
```json
// Request
{"path": "/sdcard/test.txt"}

// Response
{
  "success": true,
  "data": {
    "exists": true,
    "type": "file"
  }
}
```

### 4.2 应用操作

#### list_apps

List all installed applications on the device.

**Parameters**
| Name | Type | Required | Default | Description |
|------|------|----------|---------|-------------|
| include_system_apps | boolean | false | false | Whether to include system apps |

**Example**
```json
// Request
{}

// Response
{
  "success": true,
  "data": {
    "apps": [
      {
        "package_name": "com.example.app",
        "label": "Example App",
        "version_name": "1.0.0",
        "is_system": false
      }
    ]
  }
}
```

#### get_app_info

Get detailed information about an application.

**Parameters**
| Name | Type | Required | Default | Description |
|------|------|----------|---------|-------------|
| package_name | string | true | - | Package name of the application |

**Example**
```json
// Request
{"package_name": "com.example.app"}

// Response
{
  "success": true,
  "data": {
    "package_name": "com.example.app",
    "label": "Example App",
    "version_name": "1.0.0",
    "version_code": 1,
    "is_system": false,
    "install_time": 1711324800000,
    "update_time": 1711324800000,
    "data_dir": "/data/data/com.example.app",
    "permissions": ["android.permission.INTERNET"]
  }
}
```

#### launch_app

Launch an application by package name.

**Parameters**
| Name | Type | Required | Default | Description |
|------|------|----------|---------|-------------|
| package_name | string | true | - | Package name of the application to launch |

**Example**
```json
// Request
{"package_name": "com.example.app"}

// Response
{
  "success": true,
  "data": {}
}
```

### 4.3 系统信息

#### get_device_info

Get device information including model, OS version, screen size, etc.

**Parameters**: None

**Example**
```json
// Request
{}

// Response
{
  "success": true,
  "data": {
    "brand": "Samsung",
    "model": "Galaxy S24",
    "device": "s24",
    "android_version": "14",
    "sdk_version": 34,
    "screen_width": 1080,
    "screen_height": 2340,
    "screen_density": 420,
    "locale": "zh_CN",
    "timezone": "Asia/Shanghai"
  }
}
```

#### get_battery_status

Get current battery status including level, charging state, etc.

**Parameters**: None

**Example**
```json
// Request
{}

// Response
{
  "success": true,
  "data": {
    "level": 85,
    "scale": 100,
    "status": "charging",
    "health": "good",
    "temperature": 25.0,
    "voltage": 4.2
  }
}
```

### 4.4 权限查询

#### check_permissions

Check the status of specified permissions.

**Parameters**
| Name | Type | Required | Default | Description |
|------|------|----------|---------|-------------|
| permissions | array[string] | true | - | List of permissions to check |

**Permission names**: `storage`, `camera`, `location`, `microphone`, `contacts`, `sms`, `phone`

**Permission status values**:
- `granted`: Permission has been granted
- `denied`: Permission has been denied by user
- `not_requested`: Permission has not been requested yet

**Example**
```json
// Request
{"permissions": ["storage", "camera", "location"]}

// Response
{
  "success": true,
  "data": {
    "storage": "granted",
    "camera": "denied",
    "location": "not_requested"
  }
}
```

---

## 5. 错误码定义

### 5.1 通用错误

| Code | Message |
|------|---------|
| `PERMISSION_DENIED` | Permission not granted |
| `INVALID_PARAMETER` | Invalid parameter provided |
| `UNSUPPORTED_OPERATION` | Operation not supported in current environment |

### 5.2 文件操作错误

| Code | Message |
|------|---------|
| `FILE_NOT_FOUND` | File does not exist |
| `DIRECTORY_NOT_FOUND` | Directory does not exist |
| `NOT_A_FILE` | Path is not a file |
| `NOT_A_DIRECTORY` | Path is not a directory |
| `FILE_ALREADY_EXISTS` | File already exists |
| `STORAGE_FULL` | Storage space insufficient |
| `READ_ERROR` | Failed to read file |
| `WRITE_ERROR` | Failed to write file |

### 5.3 应用操作错误

| Code | Message |
|------|---------|
| `APP_NOT_FOUND` | Application not found |
| `APP_NOT_LAUNCHABLE` | Application cannot be launched |
| `LAUNCH_FAILED` | Failed to launch application |

---

## 6. 权限模型

### 6.1 设计原则

- **最大兼容性**：工具默认工作在 App 私有目录，有权限则扩展到共享存储
- **显式查询**：Agent 通过 `check_permissions` 主动查询权限状态
- **Agent 决策**：Agent 根据权限状态自行决定如何引导用户授权

### 6.2 权限与工具映射

| 工具 | 所需权限 |
|------|----------|
| `read_file` (私有目录) | 无 |
| `read_file` (共享存储) | `storage` |
| `write_file` (私有目录) | 无 |
| `write_file` (共享存储) | `storage` |
| `list_directory` | 同上 |
| `delete_file` | 同上 |
| `file_exists` | 同上 |
| `list_apps` | 无 |
| `get_app_info` | 无 |
| `launch_app` | 无 |
| `get_device_info` | 无 |
| `get_battery_status` | 无 |

---

## 7. 交付形式（待定）

以下交付形式可在实现阶段确定：

1. **Android SDK/库**：开发者集成到自己的 App 中
2. **独立 App**：Agent 运行在这个 App 内
3. **ADB 工具命令集**：供远程 Agent 通过 ADB 调用
4. **MCP Server**：让任何支持 MCP 的 Agent 都能调用

---

## 8. 开放问题

1. 是否需要支持二进制文件的读写？
2. `list_directory` 是否需要支持分页？
3. 是否需要提供文件监听能力（如 `watch_file`）？
4. 国际化支持：错误消息是否需要多语言？