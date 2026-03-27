# Android Agent Tools - Tier 2 设计文档

> **创建日期**: 2026-03-27
> **状态**: 待实现

## 1. 概述

Tier 2 新增 8 个进阶工具，分为两个类别：

| 类别 | 工具 | 数量 |
|------|------|------|
| UI 交互 | `take_screenshot`, `tap`, `swipe`, `input_text`, `get_ui_tree` | 5 |
| 应用管理 | `install_app`, `uninstall_app`, `force_stop_app` | 3 |

### 1.1 核心设计原则

- **混合模式**: 优先使用高权限（ROOT），降级到无障碍服务
- **权限自适应**: UI 交互工具根据无障碍权限自动调整能力范围
- **接口一致性**: 遵循 Tier 1 的 Tool 接口规范

### 1.2 运行环境优先级

```
ROOT 环境 → 无障碍服务 → 降级/报错
```

---

## 2. 架构设计

### 2.1 文件结构

```
sdk/src/main/java/com/androidagent/tools/
├── core/
│   ├── EnvironmentDetector.kt      # 新增：环境能力检测
│   ├── ToolError.kt                # 扩展：新增错误码
│   └── ...
├── accessibility/
│   ├── AgentAccessibilityService.kt   # 新增：无障碍服务实现
│   └── AccessibilityNodeInfoUtils.kt  # 新增：节点解析工具
├── tools/
│   ├── ui/                            # 新增：UI 交互工具
│   │   ├── TakeScreenshotTool.kt
│   │   ├── TapTool.kt
│   │   ├── SwipeTool.kt
│   │   ├── InputTextTool.kt
│   │   └── GetUiTreeTool.kt
│   ├── appmgmt/                       # 新增：应用管理工具
│   │   ├── InstallAppTool.kt
│   │   ├── UninstallAppTool.kt
│   │   └── ForceStopAppTool.kt
│   └── ...
└── res/xml/
    └── accessibility_service_config.xml  # 新增：无障碍服务配置
```

### 2.2 EnvironmentDetector

检测当前运行环境的能力。

```kotlin
enum class Capability {
    ROOT,                    // 设备已 ROOT
    ACCESSIBILITY_SERVICE,   // 无障碍服务已授权
    MEDIA_PROJECTION,        // 媒体投影已授权
    OVERLAY                  // 悬浮窗权限
}

class EnvironmentDetector(private val context: Context) {

    /**
     * 检测是否具有指定能力
     */
    fun hasCapability(capability: Capability): Boolean

    /**
     * 检测 ROOT 权限
     */
    fun hasRoot(): Boolean

    /**
     * 检测无障碍服务是否启用
     */
    fun hasAccessibilityService(): Boolean

    /**
     * 引导用户开启无障碍服务
     */
    fun requestAccessibilityService()

    /**
     * 获取当前所有能力快照
     */
    val capabilities: Map<Capability, Boolean>
}
```

### 2.3 AgentAccessibilityService

无障碍服务实现，提供 UI 交互能力。

```kotlin
class AgentAccessibilityService : AccessibilityService() {

    companion object {
        var instance: AgentAccessibilityService? = null
        fun isRunning(): Boolean = instance != null
    }

    // === UI 交互操作 ===

    /**
     * 在指定坐标执行点击
     */
    fun performTap(x: Int, y: Int): Boolean

    /**
     * 执行滑动手势
     */
    fun performSwipe(startX: Int, startY: Int, endX: Int, endY: Int, duration: Long): Boolean

    /**
     * 输入文本到当前焦点控件
     */
    fun performInputText(text: String): Boolean

    /**
     * 执行全局操作（返回键、Home键等）
     */
    fun performGlobalAction(action: Int): Boolean

    // === UI 树获取 ===

    /**
     * 获取当前屏幕的 UI 树
     */
    fun getUiTree(maxDepth: Int = 10, includeInvisible: Boolean = false): List<Map<String, Any?>>

    /**
     * 根据文本查找节点
     */
    fun findNodeByText(text: String): AccessibilityNodeInfo?

    /**
     * 根据 resource_id 查找节点
     */
    fun findNodeById(id: String): AccessibilityNodeInfo?

    // === 截图 ===

    /**
     * 截取当前屏幕（Android R+）
     */
    fun takeScreenshot(): Bitmap?
}
```

---

## 3. 工具接口定义

### 3.1 take_screenshot

Capture the current screen.

**Parameters**: None

**实现逻辑**:
1. ROOT 环境: 执行 `screencap -p` 命令 → 静默截图
2. 无障碍服务 + Android R (API 30+): 使用 `takeScreenshot()` API → 静默截图
3. 无障碍服务 + Android R 以下: 需要 MediaProjection 授权
4. 无无障碍服务: 返回 `ACCESSIBILITY_SERVICE_REQUIRED` 错误

**Response**:
```json
{
  "success": true,
  "data": {
    "image_base64": "<base64_encoded_png>",
    "width": 1080,
    "height": 2340
  }
}
```

**Error Codes**: `SCREENSHOT_FAILED`, `MEDIA_PROJECTION_REQUIRED`, `ACCESSIBILITY_SERVICE_REQUIRED`

---

### 3.2 tap

Perform a tap at the specified coordinates.

**Parameters**:

| Name | Type | Required | Description |
|------|------|----------|-------------|
| x | int | true | X coordinate (absolute pixels) |
| y | int | true | Y coordinate (absolute pixels) |

**实现逻辑**:
- ROOT: 执行 `input tap x y` 命令
- 无障碍服务: 使用 `dispatchGesture()` 执行点击手势

**Response**:
```json
{
  "success": true,
  "data": {}
}
```

**Error Codes**: `GESTURE_FAILED`, `ACCESSIBILITY_SERVICE_REQUIRED`

---

### 3.3 swipe

Perform a swipe gesture.

**Parameters**:

| Name | Type | Required | Default | Description |
|------|------|----------|---------|-------------|
| start_x | int | true | - | Start X coordinate |
| start_y | int | true | - | Start Y coordinate |
| end_x | int | true | - | End X coordinate |
| end_y | int | true | - | End Y coordinate |
| duration | int | false | 300 | Duration in milliseconds |

**实现逻辑**:
- ROOT: 执行 `input swipe start_x start_y end_x end_y duration` 命令
- 无障碍服务: 使用 `dispatchGesture()` 执行滑动手势

**Response**:
```json
{
  "success": true,
  "data": {}
}
```

**Error Codes**: `GESTURE_FAILED`, `ACCESSIBILITY_SERVICE_REQUIRED`

---

### 3.4 input_text

Input text into the currently focused field.

**Parameters**:

| Name | Type | Required | Description |
|------|------|----------|-------------|
| text | string | true | Text to input |

**实现逻辑**:
- ROOT: 执行 `input text "xxx"` 命令
- 无障碍服务:
  - 优先使用焦点节点的 `ACTION_SET_TEXT`
  - 降级到剪贴板粘贴方式

**Response**:
```json
{
  "success": true,
  "data": {}
}
```

**Error Codes**: `GESTURE_FAILED`, `ACCESSIBILITY_SERVICE_REQUIRED`

---

### 3.5 get_ui_tree

Get the UI hierarchy tree of current screen.

**Parameters**:

| Name | Type | Required | Default | Description |
|------|------|----------|---------|-------------|
| max_depth | int | false | 10 | Maximum tree depth to traverse |
| include_invisible | boolean | false | false | Include invisible nodes |

**实现逻辑**:
- 无障碍服务: 遍历 `rootInActiveWindow` 获取节点树
- 无 ROOT 降级方案（需要无障碍服务）

**Response**:
```json
{
  "success": true,
  "data": {
    "nodes": [
      {
        "node_id": "node_0",
        "bounds": [0, 0, 1080, 2340],
        "text": "",
        "resource_id": "",
        "clickable": false,
        "scrollable": false,
        "enabled": true,
        "visible": true,
        "class": "android.widget.FrameLayout",
        "children": [
          {
            "node_id": "node_1",
            "bounds": [0, 100, 1080, 200],
            "text": "Hello",
            "resource_id": "com.example.app/title",
            "clickable": false,
            "scrollable": false,
            "enabled": true,
            "visible": true,
            "class": "android.widget.TextView",
            "children": []
          }
        ]
      }
    ],
    "package_name": "com.example.app",
    "node_count": 45
  }
}
```

**节点属性说明**:

| 属性 | 类型 | 说明 |
|------|------|------|
| node_id | string | 节点唯一标识，格式 `node_{index}` |
| bounds | int[4] | 边界坐标 [left, top, right, bottom] |
| text | string | 显示文本 |
| resource_id | string | 资源 ID |
| clickable | boolean | 是否可点击 |
| scrollable | boolean | 是否可滚动 |
| enabled | boolean | 是否可用 |
| visible | boolean | 是否可见 |
| class | string | 控件类名 |
| children | array | 子节点列表 |

**Error Codes**: `UI_TREE_FAILED`, `ACCESSIBILITY_SERVICE_REQUIRED`

---

### 3.6 install_app

Install an application from APK file.

**Parameters**:

| Name | Type | Required | Description |
|------|------|----------|-------------|
| apk_path | string | true | Absolute path to the APK file |

**实现逻辑**:
- ROOT: 执行 `pm install -r -g {apk_path}` 命令
- 无障碍服务: 打开系统安装器，模拟点击安装按钮

**Response**:
```json
{
  "success": true,
  "data": {
    "package_name": "com.example.app",
    "version_name": "1.0.0"
  }
}
```

**Error Codes**: `INSTALL_FAILED`, `APK_NOT_FOUND`, `INVALID_APK`, `ROOT_REQUIRED`, `ACCESSIBILITY_SERVICE_REQUIRED`

---

### 3.7 uninstall_app

Uninstall an application.

**Parameters**:

| Name | Type | Required | Description |
|------|------|----------|-------------|
| package_name | string | true | Package name to uninstall |

**实现逻辑**:
- ROOT: 执行 `pm uninstall {package_name}` 命令
- 无障碍服务: 打开应用详情页，模拟点击卸载按钮

**Response**:
```json
{
  "success": true,
  "data": {}
}
```

**Error Codes**: `UNINSTALL_FAILED`, `APP_NOT_FOUND`, `ROOT_REQUIRED`, `ACCESSIBILITY_SERVICE_REQUIRED`

---

### 3.8 force_stop_app

Force stop an application.

**Parameters**:

| Name | Type | Required | Description |
|------|------|----------|-------------|
| package_name | string | true | Package name to stop |

**实现逻辑**:
- ROOT: 执行 `am force-stop {package_name}` 命令
- 无障碍服务: 打开应用详情页，模拟点击强制停止按钮

**Response**:
```json
{
  "success": true,
  "data": {}
}
```

**Error Codes**: `FORCE_STOP_FAILED`, `APP_NOT_FOUND`, `ROOT_REQUIRED`, `ACCESSIBILITY_SERVICE_REQUIRED`

---

## 4. 错误码扩展

新增以下错误码到 `ToolError` 枚举：

```kotlin
// Tier 2 - 环境错误
ROOT_REQUIRED("Root access required"),
ACCESSIBILITY_SERVICE_REQUIRED("Accessibility service not enabled"),
MEDIA_PROJECTION_REQUIRED("Media projection permission not granted"),

// Tier 2 - 操作错误
SCREENSHOT_FAILED("Failed to capture screenshot"),
GESTURE_FAILED("Failed to perform gesture"),
UI_TREE_FAILED("Failed to get UI tree"),
INSTALL_FAILED("Failed to install application"),
UNINSTALL_FAILED("Failed to uninstall application"),
FORCE_STOP_FAILED("Failed to force stop application"),
APK_NOT_FOUND("APK file does not exist"),
INVALID_APK("Invalid or corrupted APK file");
```

---

## 5. AndroidManifest.xml 更新

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- Tier 1 权限 -->
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />

    <!-- Tier 2 权限 -->
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />

    <!-- 无障碍服务 -->
    <service
        android:name=".accessibility.AgentAccessibilityService"
        android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE"
        android:exported="true">
        <intent-filter>
            <action android:name="android.accessibilityservice.AccessibilityService" />
        </intent-filter>
        <meta-data
            android:name="android.accessibilityservice"
            android:resource="@xml/accessibility_service_config" />
    </service>
</manifest>
```

---

## 6. 测试策略

### 6.1 单元测试

| 组件 | 测试内容 |
|------|----------|
| EnvironmentDetector | ROOT 检测逻辑、权限状态检测 |
| UI Tools | 参数验证、错误码返回 |
| AppMgmt Tools | 参数验证、错误码返回 |

### 6.2 集成测试（需要真机或模拟器）

| 场景 | 测试内容 |
|------|----------|
| ROOT 环境 | 所有工具在 ROOT 模式下的功能 |
| 无障碍服务 | 所有工具在无障碍模式下的功能 |
| 降级场景 | 缺少权限时的错误提示 |

---

## 7. 实现优先级

建议按以下顺序实现：

1. **基础设施**
   - EnvironmentDetector
   - AgentAccessibilityService

2. **UI 交互工具**
   - get_ui_tree（其他工具的基础）
   - tap
   - swipe
   - input_text
   - take_screenshot

3. **应用管理工具**
   - force_stop_app（最常用）
   - uninstall_app
   - install_app

---

## 8. 兼容性说明

| 功能 | 最低 Android 版本 | 备注 |
|------|-------------------|------|
| 无障碍手势 | Android 7.0 (API 24) | `dispatchGesture()` |
| 无障碍截图 | Android 11 (API 30) | `takeScreenshot()` API |
| ROOT 操作 | 所有版本 | 需要设备已 ROOT |