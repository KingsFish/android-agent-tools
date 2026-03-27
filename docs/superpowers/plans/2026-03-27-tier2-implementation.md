# Tier 2 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [x]`) syntax for tracking.

**Goal:** 实现 Android Agent Tools SDK 的 Tier 2 功能，包括 UI 交互和应用管理工具。

**Architecture:** 采用混合模式设计，优先使用 ROOT 权限，降级到无障碍服务。新增 EnvironmentDetector 检测环境能力，AgentAccessibilityService 提供无障碍服务能力。

**Tech Stack:** Kotlin, Android Accessibility Service, Android Shell Commands

---

## File Structure

```
sdk/src/main/java/com/androidagent/tools/
├── core/
│   ├── ToolError.kt                    # 扩展：新增 Tier 2 错误码
│   ├── ParameterValidator.kt           # 扩展：新增 requireInt 方法
│   ├── Capability.kt                   # 新增：能力枚举
│   └── EnvironmentDetector.kt          # 新增：环境检测器
├── accessibility/
│   ├── AgentAccessibilityService.kt    # 新增：无障碍服务实现
│   └── AccessibilityNodeInfoUtils.kt   # 新增：节点解析工具
├── tools/
│   ├── ui/                             # 新增：UI 交互工具
│   │   ├── TakeScreenshotTool.kt
│   │   ├── TapTool.kt
│   │   ├── SwipeTool.kt
│   │   ├── InputTextTool.kt
│   │   └── GetUiTreeTool.kt
│   └── appmgmt/                        # 新增：应用管理工具
│       ├── InstallAppTool.kt
│       ├── UninstallAppTool.kt
│       └── ForceStopAppTool.kt
└── res/xml/
    └── accessibility_service_config.xml # 新增：无障碍服务配置

sdk/src/main/
└── AndroidManifest.xml                  # 修改：添加权限和服务声明

sdk/src/test/java/com/androidagent/tools/
├── core/
│   ├── ToolErrorTest.kt                # 扩展：测试新错误码
│   └── EnvironmentDetectorTest.kt      # 新增
├── accessibility/
│   └── AccessibilityNodeInfoUtilsTest.kt # 新增
└── tools/
    ├── ui/                             # 新增
    └── appmgmt/                        # 新增
```

---

## Task 1: 扩展 ToolError 错误码

**Files:**
- Modify: `sdk/src/main/java/com/androidagent/tools/core/ToolError.kt`
- Modify: `sdk/src/test/java/com/androidagent/tools/core/ToolErrorTest.kt`

- [x] **Step 1: 扩展 ToolError 枚举，添加 Tier 2 错误码**

```kotlin
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
    LAUNCH_FAILED("Failed to launch application"),

    // Tier 2 - Environment errors
    ROOT_REQUIRED("Root access required"),
    ACCESSIBILITY_SERVICE_REQUIRED("Accessibility service not enabled"),
    MEDIA_PROJECTION_REQUIRED("Media projection permission not granted"),

    // Tier 2 - Operation errors
    SCREENSHOT_FAILED("Failed to capture screenshot"),
    GESTURE_FAILED("Failed to perform gesture"),
    UI_TREE_FAILED("Failed to get UI tree"),
    INSTALL_FAILED("Failed to install application"),
    UNINSTALL_FAILED("Failed to uninstall application"),
    FORCE_STOP_FAILED("Failed to force stop application"),
    APK_NOT_FOUND("APK file does not exist"),
    INVALID_APK("Invalid or corrupted APK file");

    fun withContext(context: String): String = "$message: $context"
}
```

- [x] **Step 2: 添加测试用例**

在 ToolErrorTest.kt 中添加:

```kotlin
@Test
fun `tier 2 environment errors have correct messages`() {
    assertEquals("Root access required", ToolError.ROOT_REQUIRED.message)
    assertEquals("Accessibility service not enabled", ToolError.ACCESSIBILITY_SERVICE_REQUIRED.message)
    assertEquals("Media projection permission not granted", ToolError.MEDIA_PROJECTION_REQUIRED.message)
}

@Test
fun `tier 2 operation errors have correct messages`() {
    assertEquals("Failed to capture screenshot", ToolError.SCREENSHOT_FAILED.message)
    assertEquals("Failed to perform gesture", ToolError.GESTURE_FAILED.message)
    assertEquals("Failed to get UI tree", ToolError.UI_TREE_FAILED.message)
    assertEquals("Failed to install application", ToolError.INSTALL_FAILED.message)
    assertEquals("Failed to uninstall application", ToolError.UNINSTALL_FAILED.message)
    assertEquals("Failed to force stop application", ToolError.FORCE_STOP_FAILED.message)
    assertEquals("APK file does not exist", ToolError.APK_NOT_FOUND.message)
    assertEquals("Invalid or corrupted APK file", ToolError.INVALID_APK.message)
}
```

- [x] **Step 3: 运行测试验证**

Run: `./gradlew :sdk:test --tests "com.androidagent.tools.core.ToolErrorTest"`
Expected: All tests pass

- [x] **Step 4: 提交**

```bash
git add sdk/src/main/java/com/androidagent/tools/core/ToolError.kt sdk/src/test/java/com/androidagent/tools/core/ToolErrorTest.kt
git commit -m "feat(core): add Tier 2 error codes to ToolError"
```

---

## Task 2: 扩展 ParameterValidator

**Files:**
- Modify: `sdk/src/main/java/com/androidagent/tools/core/ParameterValidator.kt`
- Modify: `sdk/src/test/java/com/androidagent/tools/core/ParameterValidatorTest.kt`

- [x] **Step 1: 添加 requireInt 和 optionalInt 方法**

在 ParameterValidator 类中添加:

```kotlin
fun requireInt(key: String): Result<Int> {
    val value = params[key]
    return when {
        value == null -> Result.Failure(ToolError.INVALID_PARAMETER, "Missing required parameter: $key")
        value is Int -> Result.Success(value)
        value is Number -> Result.Success(value.toInt())
        else -> Result.Failure(ToolError.INVALID_PARAMETER, "Parameter '$key' must be an integer")
    }
}

fun optionalInt(key: String, default: Int): Int {
    val value = params[key]
    return when {
        value == null -> default
        value is Int -> value
        value is Number -> value.toInt()
        else -> default
    }
}
```

- [x] **Step 2: 添加测试用例**

```kotlin
@Test
fun `requireInt returns success for integer value`() {
    val validator = ParameterValidator(mapOf("count" to 42))
    val result = validator.requireInt("count")
    assertTrue(result.isSuccess)
    assertEquals(42, (result as Result.Success).value)
}

@Test
fun `requireInt fails for missing parameter`() {
    val validator = ParameterValidator(emptyMap())
    val result = validator.requireInt("count")
    assertTrue(result.isFailure)
}

@Test
fun `optionalInt returns default for missing parameter`() {
    val validator = ParameterValidator(emptyMap())
    val result = validator.optionalInt("count", 10)
    assertEquals(10, result)
}
```

- [x] **Step 3: 运行测试验证**

Run: `./gradlew :sdk:test --tests "com.androidagent.tools.core.ParameterValidatorTest"`
Expected: All tests pass

- [x] **Step 4: 提交**

```bash
git add sdk/src/main/java/com/androidagent/tools/core/ParameterValidator.kt sdk/src/test/java/com/androidagent/tools/core/ParameterValidatorTest.kt
git commit -m "feat(core): add requireInt and optionalInt to ParameterValidator"
```

---

## Task 3: 实现 Capability 枚举

**Files:**
- Create: `sdk/src/main/java/com/androidagent/tools/core/Capability.kt`

- [x] **Step 1: 创建 Capability 枚举**

```kotlin
package com.androidagent.tools.core

enum class Capability {
    ROOT,
    ACCESSIBILITY_SERVICE,
    MEDIA_PROJECTION,
    OVERLAY
}
```

- [x] **Step 2: 提交**

```bash
git add sdk/src/main/java/com/androidagent/tools/core/Capability.kt
git commit -m "feat(core): add Capability enum for environment detection"
```

---

## Task 4: 实现 EnvironmentDetector

**Files:**
- Create: `sdk/src/main/java/com/androidagent/tools/core/EnvironmentDetector.kt`
- Create: `sdk/src/test/java/com/androidagent/tools/core/EnvironmentDetectorTest.kt`

- [x] **Step 1: 创建 EnvironmentDetector 类**

```kotlin
package com.androidagent.tools.core

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager

class EnvironmentDetector(private val context: Context) {

    fun hasCapability(capability: Capability): Boolean {
        return when (capability) {
            Capability.ROOT -> hasRoot()
            Capability.ACCESSIBILITY_SERVICE -> hasAccessibilityService()
            Capability.MEDIA_PROJECTION -> hasMediaProjection()
            Capability.OVERLAY -> hasOverlay()
        }
    }

    fun hasRoot(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su")
            val outputStream = process.outputStream
            outputStream.write("exit\n".toByteArray())
            outputStream.flush()
            outputStream.close()
            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    fun hasAccessibilityService(): Boolean {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val serviceName = "${context.packageName}/com.androidagent.tools.accessibility.AgentAccessibilityService"
        return enabledServices.contains(serviceName)
    }

    fun hasMediaProjection(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
    }

    fun hasOverlay(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    fun requestAccessibilityService() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    val capabilities: Map<Capability, Boolean>
        get() = Capability.entries.associateWith { hasCapability(it) }
}
```

- [x] **Step 2: 创建测试类**

```kotlin
package com.androidagent.tools.core

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import android.content.Context

class EnvironmentDetectorTest {

    @Test
    fun `capabilities returns all capabilities`() {
        val mockContext = mockk<Context>(relaxed = true)
        every { mockContext.contentResolver } returns mockk(relaxed = true)
        every { mockContext.packageName } returns "com.test"

        val detector = EnvironmentDetector(mockContext)
        val caps = detector.capabilities

        assertTrue(caps.containsKey(Capability.ROOT))
        assertTrue(caps.containsKey(Capability.ACCESSIBILITY_SERVICE))
        assertTrue(caps.containsKey(Capability.MEDIA_PROJECTION))
        assertTrue(caps.containsKey(Capability.OVERLAY))
    }

    @Test
    fun `hasMediaProjection returns true for Lollipop and above`() {
        val mockContext = mockk<Context>(relaxed = true)
        val detector = EnvironmentDetector(mockContext)
        assertTrue(detector.hasMediaProjection())
    }
}
```

- [x] **Step 3: 运行测试验证**

Run: `./gradlew :sdk:test --tests "com.androidagent.tools.core.EnvironmentDetectorTest"`
Expected: All tests pass

- [x] **Step 4: 提交**

```bash
git add sdk/src/main/java/com/androidagent/tools/core/EnvironmentDetector.kt sdk/src/test/java/com/androidagent/tools/core/EnvironmentDetectorTest.kt
git commit -m "feat(core): implement EnvironmentDetector for capability detection"
```

---

## Task 5: 实现 AccessibilityNodeInfoUtils

**Files:**
- Create: `sdk/src/main/java/com/androidagent/tools/accessibility/AccessibilityNodeInfoUtils.kt`
- Create: `sdk/src/test/java/com/androidagent/tools/accessibility/AccessibilityNodeInfoUtilsTest.kt`

- [x] **Step 1: 创建节点解析工具类**

```kotlin
package com.androidagent.tools.accessibility

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo

object AccessibilityNodeInfoUtils {

    fun nodeToMap(node: AccessibilityNodeInfo, index: Int = 0, maxDepth: Int = 10, includeInvisible: Boolean = false): Map<String, Any?>? {
        if (maxDepth < 0) return null
        if (!includeInvisible && !node.isVisibleToUser) return null

        val bounds = Rect()
        node.getBoundsInScreen(bounds)

        val children = mutableListOf<Map<String, Any?>>()
        for (i in 0 until node.childCount) {
            val childNode = node.getChild(i) ?: continue
            val childMap = nodeToMap(childNode, index + i + 1, maxDepth - 1, includeInvisible)
            if (childMap != null) {
                children.add(childMap)
            }
        }

        return mapOf(
            "node_id" to "node_$index",
            "bounds" to listOf(bounds.left, bounds.top, bounds.right, bounds.bottom),
            "text" to (node.text?.toString() ?: ""),
            "resource_id" to (node.viewIdResourceName ?: ""),
            "clickable" to node.isClickable,
            "scrollable" to node.isScrollable,
            "enabled" to node.isEnabled,
            "visible" to node.isVisibleToUser,
            "class" to (node.className?.toString() ?: ""),
            "children" to children
        )
    }

    fun countNodes(node: AccessibilityNodeInfo, includeInvisible: Boolean = false): Int {
        if (!includeInvisible && !node.isVisibleToUser) return 0
        var count = 1
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            count += countNodes(child, includeInvisible)
        }
        return count
    }
}
```

- [x] **Step 2: 创建测试类**

```kotlin
package com.androidagent.tools.accessibility

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo

class AccessibilityNodeInfoUtilsTest {

    @Test
    fun `nodeToMap converts basic node properties`() {
        val mockNode = mockk<AccessibilityNodeInfo>(relaxed = true)
        val bounds = Rect(0, 0, 100, 200)

        every { mockNode.getBoundsInScreen(any()) } answers {
            (it.invocation.args[0] as Rect).set(bounds)
        }
        every { mockNode.text } returns "Hello"
        every { mockNode.viewIdResourceName } returns "com.example:id/text"
        every { mockNode.isClickable } returns true
        every { mockNode.isScrollable } returns false
        every { mockNode.isEnabled } returns true
        every { mockNode.isVisibleToUser } returns true
        every { mockNode.className } returns "android.widget.TextView"
        every { mockNode.childCount } returns 0

        val result = AccessibilityNodeInfoUtils.nodeToMap(mockNode)!!

        assertEquals("node_0", result["node_id"])
        assertEquals(listOf(0, 0, 100, 200), result["bounds"])
        assertEquals("Hello", result["text"])
    }

    @Test
    fun `nodeToMap skips invisible nodes when includeInvisible is false`() {
        val mockNode = mockk<AccessibilityNodeInfo>(relaxed = true)
        every { mockNode.isVisibleToUser } returns false
        every { mockNode.childCount } returns 0

        val result = AccessibilityNodeInfoUtils.nodeToMap(mockNode, includeInvisible = false)
        assertNull(result)
    }
}
```

- [x] **Step 3: 运行测试验证**

Run: `./gradlew :sdk:test --tests "com.androidagent.tools.accessibility.AccessibilityNodeInfoUtilsTest"`
Expected: All tests pass

- [x] **Step 4: 提交**

```bash
git add sdk/src/main/java/com/androidagent/tools/accessibility/AccessibilityNodeInfoUtils.kt sdk/src/test/java/com/androidagent/tools/accessibility/AccessibilityNodeInfoUtilsTest.kt
git commit -m "feat(accessibility): implement AccessibilityNodeInfoUtils for node parsing"
```

---

## Task 6: 实现 AgentAccessibilityService

**Files:**
- Create: `sdk/src/main/java/com/androidagent/tools/accessibility/AgentAccessibilityService.kt`

- [x] **Step 1: 创建无障碍服务类** (详见设计文档 2.3 节)

- [x] **Step 2: 提交**

```bash
git add sdk/src/main/java/com/androidagent/tools/accessibility/AgentAccessibilityService.kt
git commit -m "feat(accessibility): implement AgentAccessibilityService"
```

---

## Task 7: 创建无障碍服务配置

**Files:**
- Create: `sdk/src/main/res/xml/accessibility_service_config.xml`
- Create: `sdk/src/main/res/values/strings.xml`

- [x] **Step 1: 创建配置文件和字符串资源**

- [x] **Step 2: 提交**

```bash
git add sdk/src/main/res/xml/accessibility_service_config.xml sdk/src/main/res/values/strings.xml
git commit -m "feat(resources): add accessibility service configuration"
```

---

## Task 8: 更新 AndroidManifest.xml

**Files:**
- Modify: `sdk/src/main/AndroidManifest.xml`

- [x] **Step 1: 添加权限和服务声明** (详见设计文档第 5 节)

- [x] **Step 2: 提交**

```bash
git add sdk/src/main/AndroidManifest.xml
git commit -m "feat(manifest): add Tier 2 permissions and accessibility service"
```

---

## Task 9-13: UI 交互工具

| Task | Tool | Files |
|------|------|-------|
| 9 | GetUiTreeTool | tools/ui/GetUiTreeTool.kt, tests |
| 10 | TapTool | tools/ui/TapTool.kt, tests |
| 11 | SwipeTool | tools/ui/SwipeTool.kt, tests |
| 12 | InputTextTool | tools/ui/InputTextTool.kt, tests |
| 13 | TakeScreenshotTool | tools/ui/TakeScreenshotTool.kt, tests |

每个工具遵循 TDD 流程: 写测试 → 运行验证失败 → 实现代码 → 运行验证通过 → 提交

---

## Task 14-16: 应用管理工具

| Task | Tool | Files |
|------|------|-------|
| 14 | ForceStopAppTool | tools/appmgmt/ForceStopAppTool.kt, tests |
| 15 | UninstallAppTool | tools/appmgmt/UninstallAppTool.kt, tests |
| 16 | InstallAppTool | tools/appmgmt/InstallAppTool.kt, tests |

---

## Task 17: 更新 AndroidAgentTools 注册新工具

**Files:**
- Modify: `sdk/src/main/java/com/androidagent/tools/AndroidAgentTools.kt`

- [x] **Step 1: 添加 Tier 2 工具注册，版本号升级到 1.1.0**

- [x] **Step 2: 运行所有测试验证**

Run: `./gradlew :sdk:test`
Expected: All tests pass

- [x] **Step 3: 提交**

---

## Task 18: 最终验证和文档更新

**Files:**
- Modify: `README.md`

- [x] **Step 1: 更新 README.md 添加 Tier 2 工具说明**

- [x] **Step 2: 运行完整测试套件**

- [x] **Step 3: 提交**
