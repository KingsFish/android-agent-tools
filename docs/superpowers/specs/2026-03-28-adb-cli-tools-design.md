# ADB CLI 工具命令集设计

## 1. 概述

### 1.1 目标

为远程 LLM Agent 提供 CLI 命令行工具，通过 ADB 直接操控 Android 设备，无需在设备上安装 App。

### 1.2 使用场景

电脑端 Agent 执行 shell 命令调用：

```bash
aat tap 500 800
aat read_file /sdcard/test.txt
aat list_apps
```

Agent 只需要 Bash 工具就能调用所有 ADB 功能。

### 1.3 调用模式

CLI 命令包装模式：
- Agent 通过 Bash 工具执行 `aat <command>` 命令
- CLI 解析参数、选择设备、执行 ADB 命令
- 返回统一 JSON 格式结果

---

## 2. 技术栈

- **语言**: TypeScript
- **CLI 框架**: Commander
- **运行环境**: Node.js
- **构建输出**: 单一 CLI 入口 `bin/aat`

---

## 3. 项目结构

```
cli/
├── package.json
├── tsconfig.json
├── src/
│   ├── index.ts           # CLI 入口（自动命令注册）
│   ├── adb.ts             # ADB 命令执行封装
│   ├── device.ts          # 设备检测/选择
│   ├── output.ts          # JSON 输出格式化
│   ├── parser.ts          # 结果解析（UI树、截图等）
│   ├── types.ts           # 类型定义
│   └── commands/          # 各工具实现（自动扫描注册）
│       ├── tap.ts
│       ├── swipe.ts
│       ├── press_key.ts
│       ├── read_file.ts
│       ├── write_file.ts
│       ├── list_directory.ts
│       ├── delete_file.ts
│       ├── file_exists.ts
│       ├── list_apps.ts
│       ├── get_app_info.ts
│       ├── launch_app.ts
│       ├── install_app.ts
│       ├── uninstall_app.ts
│       ├── force_stop_app.ts
│       ├── get_device_info.ts
│       ├── get_battery_status.ts
│       ├── get_ui_tree.ts
│       ├── take_screenshot.ts
│       ├── input_text.ts
│       ├── press_back.ts
│       ├── press_home.ts
│       ├── press_recents.ts
│       ├── long_press.ts
│       ├── drag.ts
│       ├── get_current_app.ts
│       ├── is_app_running.ts
│       ├── click_node_by_text.ts
│       ├── click_node_by_id.ts
│       ├── wait_for_ui_stable.ts
│       ├── wait_for_element.ts
│       ├── list_tools.ts       # 元命令：列出所有工具
│       └── list_devices.ts     # 元命令：列出已连接设备
└── bin/
    └── aat                   # CLI 执行入口
```

---

## 4. 核心模块设计

### 4.1 ADB 执行模块 (adb.ts)

```typescript
interface AdbOptions {
  deviceId?: string       // 指定设备
  timeout?: number        // 超时时间（毫秒），默认 30000
}

interface AdbResult {
  exitCode: number
  stdout: string
  stderr: string
}

async function execAdb(args: string[], options?: AdbOptions): Promise<AdbResult>
```

执行逻辑：
1. 自动添加 `-s <deviceId>` 参数
2. 执行 `adb <args>` 命令
3. 处理超时（默认 30 秒）
4. 返回 stdout/stderr/exitCode

### 4.2 设备检测模块 (device.ts)

```typescript
interface Device {
  id: string              // 设备 ID，如 'emulator-5554'
  status: string          // 状态：'device' | 'offline' | 'unauthorized'
}

// 获取已连接设备列表
async function listDevices(): Promise<Device[]>

// 自动选择设备
async function selectDevice(deviceId?: string): Promise<string>
```

设备选择逻辑：
- 用户指定 `-d`：使用指定设备
- 只有 1 台设备：自动使用
- 多台设备未指定：报错提示用户指定
- 无设备连接：报错提示连接设备

支持环境变量：`ADB_DEVICE=<device-id>` 作为默认设备

### 4.3 JSON 输出模块 (output.ts)

```typescript
// 成功输出
function success(data: any): string
// {"success": true, "data": {...}}

// 失败输出
function failure(code: string, message: string): string
// {"success": false, "error": {"code": "...", "message": "..."}}
```

错误码定义（与现有设计文档一致）：

| Code | 说明 |
|------|------|
| PERMISSION_DENIED | 权限未授予 |
| INVALID_PARAMETER | 参数无效 |
| UNSUPPORTED_OPERATION | 操作不支持 |
| DEVICE_NOT_FOUND | 未找到设备 |
| FILE_NOT_FOUND | 文件不存在 |
| READ_ERROR | 读取失败 |
| WRITE_ERROR | 写入失败 |
| APP_NOT_FOUND | 应用不存在 |
| LAUNCH_FAILED | 启动失败 |
| UI_DUMP_FAILED | UI dump 失败 |
| TAP_FAILED | 点击失败 |

---

## 5. 命令自动注册机制

### 5.1 工具定义规范

每个命令文件导出统一格式的 definition：

```typescript
// commands/tap.ts

import { ToolDefinition } from '../types'
import { selectDevice, execAdb, success, failure } from '../utils'

export const definition: ToolDefinition = {
  name: 'tap',
  description: 'Perform a tap at the specified coordinates',
  parameters: [
    { name: 'x', type: 'number', required: true, description: 'X coordinate' },
    { name: 'y', type: 'number', required: true, description: 'Y coordinate' }
  ],
  adbCommand: 'shell input tap <x> <y>',
  async execute(x: number, y: number, options: CliOptions) {
    const device = await selectDevice(options.device)
    const result = await execAdb(
      ['shell', 'input', 'tap', String(x), String(y)],
      { deviceId: device }
    )
    return result.exitCode === 0
      ? success({})
      : failure('TAP_FAILED', result.stderr)
  }
}
```

### 5.2 类型定义 (types.ts)

```typescript
interface ToolDefinition {
  name: string
  description: string
  parameters: Parameter[]
  adbCommand?: string          // 文档用途
  execute: (...args: any[], options: CliOptions) => Promise<string>
}

interface Parameter {
  name: string
  type: 'string' | 'number' | 'boolean'
  required: boolean
  description?: string
  default?: any
}

interface CliOptions {
  device?: string
  timeout?: number
  pretty?: boolean
}
```

### 5.3 CLI 入口自动扫描 (index.ts)

```typescript
import { Command } from 'commander'
import fs from 'fs'
import path from 'path'

const program = new Command()
  .name('aat')
  .option('-d, --device <id>', '指定设备')
  .option('--pretty', '美化 JSON 输出')
  .option('-t, --timeout <ms>', '超时时间', '30000')

// 自动扫描 commands/ 目录
const commandsDir = path.join(__dirname, 'commands')
const files = fs.readdirSync(commandsDir).filter(f => f.endsWith('.js'))

for (const file of files) {
  const { definition } = require(path.join(commandsDir, file))
  registerCommand(program, definition)
}

function registerCommand(program: Command, def: ToolDefinition) {
  const paramsStr = def.parameters
    .filter(p => p.required)
    .map(p => `<${p.name}>`)
    .join(' ')

  const cmd = program.command(`${def.name} ${paramsStr}`)
    .description(def.description)

  // 注册可选参数
  def.parameters.filter(p => !p.required).forEach(p => {
    cmd.option(`--${p.name} <value>`, p.description, p.default)
  })

  cmd.action((...args) => {
    const options = getOptions()
    output(def.execute(...args, options))
  })
}

program.parse()
```

---

## 6. ADB 命令映射表

| 工具 | ADB 命令 |
|------|----------|
| tap | `adb shell input tap <x> <y>` |
| swipe | `adb shell input swipe <sx> <sy> <ex> <ey> <duration>` |
| long_press | `adb shell input swipe <x> <y> <x> <y> <duration>` |
| drag | `adb shell input drag <sx> <sy> <ex> <ey>` |
| press_key | `adb shell input keyevent <keycode>` |
| press_back | `adb shell input keyevent 4` |
| press_home | `adb shell input keyevent 3` |
| press_recents | `adb shell input keyevent 187` |
| input_text | `adb shell input text <text>` |
| read_file | `adb shell cat <path>` |
| write_file | `adb shell echo <content> > <path>` 或 `adb push` |
| list_directory | `adb shell ls -la <path>` |
| delete_file | `adb shell rm <path>` |
| file_exists | `adb shell test -e <path> && echo 1 || echo 0` |
| list_apps | `adb shell pm list packages [-3]` |
| get_app_info | `adb shell dumpsys package <pkg>` |
| launch_app | `adb shell am start -n <pkg>/<activity>` |
| force_stop_app | `adb shell am force-stop <pkg>` |
| install_app | `adb install <apk>` (非 shell) |
| uninstall_app | `adb uninstall <pkg>` (非 shell) |
| get_device_info | `adb shell getprop` 组合 |
| get_battery_status | `adb shell dumpsys battery` |
| get_ui_tree | `adb shell uiautomator dump` + `adb shell cat` |
| take_screenshot | `adb shell screencap -p` |
| get_current_app | `adb shell dumpsys activity activities` |
| is_app_running | `adb shell pidof <pkg>` |
| click_node_by_text | dump → 解析 → tap |
| click_node_by_id | dump → 解析 → tap |
| wait_for_ui_stable | 轮询截图对比 |
| wait_for_element | 轮询 UI 树 |

---

## 7. 受限功能

### 7.1 完全不支持（2个）

| 工具 | 原因 | 未来方案 |
|------|------|----------|
| get_clipboard | ADB shell 无法读写剪贴板 | 配合 Accessibility Service App，通过 ADB broadcast 调用 |
| set_clipboard | ADB shell 无法读写剪贴板 | 同上 |

调用时返回：
```json
{"success": false, "error": {"code": "UNSUPPORTED_OPERATION", "message": "get_clipboard requires Accessibility Service. Use MCP Server App instead."}}
```

### 7.2 效率受限（2个）

| 工具 | 限制 | 影响 |
|------|------|------|
| wait_for_ui_stable | 无实时监听，只能客户端轮询截图 | 延迟高、资源消耗大 |
| wait_for_element | 无实时监听，只能客户端轮询 UI 树 | 每次轮询需 uiautomator dump，耗时 1-3 秒 |

建议：如需高效等待功能，使用 MCP Server App（Accessibility Service 可实时监听）。

---

## 8. CLI 使用示例

```bash
# 基本调用
aat tap 500 800
# {"success": true, "data": {}}

aat read_file /sdcard/test.txt
# {"success": true, "data": {"content": "Hello World", "size": 11}}

aat list_apps --include-system
# {"success": true, "data": {"apps": [{"package_name": "com.example.app", ...}]}}

# 多设备时指定设备
aat -d emulator-5554 tap 500 800

# 或用环境变量
ADB_DEVICE=emulator-5554 aat tap 500 800

# 美化输出
aat --pretty get_device_info

# 元命令
aat list_devices
# {"success": true, "data": {"devices": [{"id": "emulator-5554", "status": "device"}]}}

aat list_tools
# {"success": true, "data": {"tools": ["tap", "swipe", ...], "unsupported": ["get_clipboard", "set_clipboard"]}}
```

---

## 9. 新增工具流程

只需 2 步：

1. 在 `commands/` 目录新建 `<tool_name>.ts`
2. 导出符合 ToolDefinition 接口的 definition

无需修改入口代码，CLI 自动扫描注册。

---

## 10. 构建与安装

```bash
cd cli
npm install
npm run build

# 本地使用
./bin/aat tap 500 800

# 或全局链接
npm link
aat tap 500 800
```

---

## 11. 变更历史

| 版本 | 日期 | 变更内容 |
|------|------|----------|
| 1.0.0 | 2026-03-28 | 初始设计 |