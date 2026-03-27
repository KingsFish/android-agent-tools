# Android Agent Tools SDK Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [x]`) syntax for tracking.

**Goal:** Build an Android SDK that provides 11 standardized tools for LLM agents to interact with Android devices.

**Architecture:** Interface-implementation separation pattern. Core module defines Tool interface and unified ToolResult/ToolError. Each tool category (file/app/system/permission) has its own package. Tools are stateless and receive Android Context for system operations.

**Tech Stack:** Kotlin, Android SDK, JUnit 5, MockK

---

## File Structure

```
sdk/
├── build.gradle.kts
├── src/
│   ├── main/java/com/androidagent/tools/
│   │   ├── core/
│   │   │   ├── ToolResult.kt          # Success/Failure sealed class
│   │   │   ├── ToolError.kt           # Error code enum
│   │   │   ├── Tool.kt                # Tool interface
│   │   │   └── ParameterValidator.kt  # Parameter validation helper
│   │   ├── tools/
│   │   │   ├── file/
│   │   │   │   ├── ReadFileTool.kt
│   │   │   │   ├── WriteFileTool.kt
│   │   │   │   ├── ListDirectoryTool.kt
│   │   │   │   ├── DeleteFileTool.kt
│   │   │   │   └── FileExistsTool.kt
│   │   │   ├── app/
│   │   │   │   ├── ListAppsTool.kt
│   │   │   │   ├── GetAppInfoTool.kt
│   │   │   │   └── LaunchAppTool.kt
│   │   │   ├── system/
│   │   │   │   ├── GetDeviceInfoTool.kt
│   │   │   │   └── GetBatteryStatusTool.kt
│   │   │   └── permission/
│   │   │       └── CheckPermissionsTool.kt
│   │   ├── executor/
│   │   │   └── ToolExecutor.kt
│   │   └── AndroidAgentTools.kt       # Main entry point
│   └── test/java/com/androidagent/tools/
│       ├── core/
│       │   ├── ToolResultTest.kt
│       │   ├── ToolErrorTest.kt
│       │   └── ParameterValidatorTest.kt
│       ├── tools/
│       │   ├── file/
│       │   │   ├── ReadFileToolTest.kt
│       │   │   ├── WriteFileToolTest.kt
│       │   │   ├── ListDirectoryToolTest.kt
│       │   │   ├── DeleteFileToolTest.kt
│       │   │   └── FileExistsToolTest.kt
│       │   ├── app/
│       │   │   ├── ListAppsToolTest.kt
│       │   │   ├── GetAppInfoToolTest.kt
│       │   │   └── LaunchAppToolTest.kt
│       │   ├── system/
│       │   │   ├── GetDeviceInfoToolTest.kt
│       │   │   └── GetBatteryStatusToolTest.kt
│       │   └── permission/
│       │       └── CheckPermissionsToolTest.kt
│       └── executor/
│           └── ToolExecutorTest.kt
```

---

## Task 1: Project Setup

**Files:**
- Create: `sdk/build.gradle.kts`
- Create: `sdk/src/main/AndroidManifest.xml`
- Create: `settings.gradle.kts` (root)
- Create: `build.gradle.kts` (root)

- [x] **Step 1: Create root settings.gradle.kts**

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "android-agent-tools"
include(":sdk")
```

- [x] **Step 2: Create root build.gradle.kts**

```kotlin
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
}
```

- [x] **Step 3: Create sdk/build.gradle.kts**

```kotlin
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.androidagent.tools"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    // Android instrumentation testing
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
```

- [x] **Step 4: Create AndroidManifest.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- Permissions for file operations -->
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
</manifest>
```

- [x] **Step 5: Create directory structure**

```bash
mkdir -p sdk/src/main/java/com/androidagent/tools/{core,tools/{file,app,system,permission},executor}
mkdir -p sdk/src/test/java/com/androidagent/tools/{core,tools/{file,app,system,permission},executor}
```

- [x] **Step 6: Commit**

```bash
git add .
git commit -m "chore: setup Android SDK project structure"
```

---

## Task 2: Core Module - ToolError and ToolResult

**Files:**
- Create: `sdk/src/main/java/com/androidagent/tools/core/ToolError.kt`
- Create: `sdk/src/main/java/com/androidagent/tools/core/ToolResult.kt`
- Create: `sdk/src/test/java/com/androidagent/tools/core/ToolErrorTest.kt`
- Create: `sdk/src/test/java/com/androidagent/tools/core/ToolResultTest.kt`

- [x] **Step 1: Write ToolError test**

```kotlin
// sdk/src/test/java/com/androidagent/tools/core/ToolErrorTest.kt
package com.androidagent.tools.core

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ToolErrorTest {

    @Test
    fun `error codes have correct messages`() {
        assertEquals("Permission not granted", ToolError.PERMISSION_DENIED.message)
        assertEquals("Invalid parameter provided", ToolError.INVALID_PARAMETER.message)
        assertEquals("File does not exist", ToolError.FILE_NOT_FOUND.message)
        assertEquals("Application not found", ToolError.APP_NOT_FOUND.message)
    }

    @Test
    fun `error creates full message with context`() {
        val error = ToolError.FILE_NOT_FOUND
        val fullMessage = error.withContext("/sdcard/test.txt")
        assertEquals("File does not exist: /sdcard/test.txt", fullMessage)
    }
}
```

- [x] **Step 2: Run test to verify it fails**

```bash
cd sdk && ./gradlew test --tests "com.androidagent.tools.core.ToolErrorTest"
```
Expected: FAIL with "Unresolved reference: ToolError"

- [x] **Step 3: Implement ToolError**

```kotlin
// sdk/src/main/java/com/androidagent/tools/core/ToolError.kt
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
```

- [x] **Step 4: Run test to verify it passes**

```bash
cd sdk && ./gradlew test --tests "com.androidagent.tools.core.ToolErrorTest"
```
Expected: PASS

- [x] **Step 5: Write ToolResult test**

```kotlin
// sdk/src/test/java/com/androidagent/tools/core/ToolResultTest.kt
package com.androidagent.tools.core

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.json.JSONObject

class ToolResultTest {

    @Test
    fun `success result creates correct JSON`() {
        val result = ToolResult.Success(mapOf(
            "content" to "Hello",
            "size" to 5
        ))
        val json = result.toJson()
        assertTrue(json.getBoolean("success"))
        assertEquals("Hello", json.getJSONObject("data").getString("content"))
    }

    @Test
    fun `failure result creates correct JSON`() {
        val result = ToolResult.Failure(
            ToolError.FILE_NOT_FOUND,
            "/sdcard/test.txt"
        )
        val json = result.toJson()
        assertFalse(json.getBoolean("success"))
        assertEquals("FILE_NOT_FOUND", json.getJSONObject("error").getString("code"))
        assertTrue(json.getJSONObject("error").getString("message").contains("/sdcard/test.txt"))
    }

    @Test
    fun `success from data factory works`() {
        val result = ToolResult.success(mapOf("key" to "value"))
        assertTrue(result is ToolResult.Success)
    }

    @Test
    fun `failure from error factory works`() {
        val result = ToolResult.failure(ToolError.INVALID_PARAMETER, "path")
        assertTrue(result is ToolResult.Failure)
    }
}
```

- [x] **Step 6: Run test to verify it fails**

```bash
cd sdk && ./gradlew test --tests "com.androidagent.tools.core.ToolResultTest"
```
Expected: FAIL with "Unresolved reference: ToolResult"

- [x] **Step 7: Implement ToolResult**

```kotlin
// sdk/src/main/java/com/androidagent/tools/core/ToolResult.kt
package com.androidagent.tools.core

import org.json.JSONObject

sealed class ToolResult {
    abstract fun toJson(): JSONObject

    data class Success(val data: Map<String, Any?>) : ToolResult() {
        override fun toJson(): JSONObject {
            return JSONObject().apply {
                put("success", true)
                put("data", JSONObject(data))
            }
        }
    }

    data class Failure(val error: ToolError, val context: String? = null) : ToolResult() {
        override fun toJson(): JSONObject {
            return JSONObject().apply {
                put("success", false)
                put("error", JSONObject().apply {
                    put("code", error.name)
                    put("message", context?.let { error.withContext(it) } ?: error.message)
                })
            }
        }
    }

    companion object {
        fun success(data: Map<String, Any?> = emptyMap()): ToolResult = Success(data)
        fun failure(error: ToolError, context: String? = null): ToolResult = Failure(error, context)
    }
}
```

- [x] **Step 8: Run test to verify it passes**

```bash
cd sdk && ./gradlew test --tests "com.androidagent.tools.core.ToolResultTest"
```
Expected: PASS

- [x] **Step 9: Commit**

```bash
git add sdk/src/main/java/com/androidagent/tools/core/*.kt
git add sdk/src/test/java/com/androidagent/tools/core/*.kt
git commit -m "feat(core): add ToolError and ToolResult with tests"
```

---

## Task 3: Core Module - Tool Interface and ParameterValidator

**Files:**
- Create: `sdk/src/main/java/com/androidagent/tools/core/Tool.kt`
- Create: `sdk/src/main/java/com/androidagent/tools/core/ParameterValidator.kt`
- Create: `sdk/src/test/java/com/androidagent/tools/core/ParameterValidatorTest.kt`

- [x] **Step 1: Write ParameterValidator test**

```kotlin
// sdk/src/test/java/com/androidagent/tools/core/ParameterValidatorTest.kt
package com.androidagent.tools.core

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ParameterValidatorTest {

    @Test
    fun `require non-null string returns success when present`() {
        val params = mapOf("path" to "/sdcard/test.txt")
        val result = ParameterValidator(params).requireString("path")
        assertTrue(result.isSuccess)
        assertEquals("/sdcard/test.txt", result.getOrNull())
    }

    @Test
    fun `require non-null string returns failure when missing`() {
        val params = mapOf<String, Any>()
        val result = ParameterValidator(params).requireString("path")
        assertTrue(result.isFailure)
        assertEquals(ToolError.INVALID_PARAMETER, (result as? Result.Failure<*>)?.error)
    }

    @Test
    fun `optional string returns default when missing`() {
        val params = mapOf<String, Any>()
        val result = ParameterValidator(params).optionalString("encoding", "utf-8")
        assertEquals("utf-8", result)
    }

    @Test
    fun `optional string returns value when present`() {
        val params = mapOf("encoding" to "gbk")
        val result = ParameterValidator(params).optionalString("encoding", "utf-8")
        assertEquals("gbk", result)
    }

    @Test
    fun `optional boolean returns default when missing`() {
        val params = mapOf<String, Any>()
        val result = ParameterValidator(params).optionalBoolean("include_system", false)
        assertFalse(result)
    }

    @Test
    fun `require array returns list when present`() {
        val params = mapOf("permissions" to listOf("storage", "camera"))
        val result = ParameterValidator(params).requireArray("permissions")
        assertTrue(result.isSuccess)
        assertEquals(listOf("storage", "camera"), result.getOrNull())
    }
}
```

- [x] **Step 2: Run test to verify it fails**

```bash
cd sdk && ./gradlew test --tests "com.androidagent.tools.core.ParameterValidatorTest"
```
Expected: FAIL with "Unresolved reference: ParameterValidator"

- [x] **Step 3: Implement ParameterValidator**

```kotlin
// sdk/src/main/java/com/androidagent/tools/core/ParameterValidator.kt
package com.androidagent.tools.core

sealed class Result<out T> {
    data class Success<T>(val value: T) : Result<T>()
    data class Failure<T>(val error: ToolError, val context: String? = null) : Result<T>()

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure

    fun getOrNull(): T? = (this as? Success)?.value
    fun getErrorOrNull(): ToolError? = (this as? Failure)?.error
}

class ParameterValidator(private val params: Map<String, Any?>) {

    fun requireString(key: String): Result<String> {
        val value = params[key]
        return when {
            value == null -> Result.Failure(ToolError.INVALID_PARAMETER, "Missing required parameter: $key")
            value !is String -> Result.Failure(ToolError.INVALID_PARAMETER, "Parameter '$key' must be a string")
            else -> Result.Success(value)
        }
    }

    fun optionalString(key: String, default: String): String {
        val value = params[key]
        return when {
            value == null -> default
            value is String -> value
            else -> default
        }
    }

    fun optionalBoolean(key: String, default: Boolean): Boolean {
        val value = params[key]
        return when {
            value == null -> default
            value is Boolean -> value
            else -> default
        }
    }

    fun requireArray(key: String): Result<List<String>> {
        val value = params[key]
        return when {
            value == null -> Result.Failure(ToolError.INVALID_PARAMETER, "Missing required parameter: $key")
            value !is List<*> -> Result.Failure(ToolError.INVALID_PARAMETER, "Parameter '$key' must be an array")
            else -> Result.Success(value.mapNotNull { it?.toString() })
        }
    }
}
```

- [x] **Step 4: Run test to verify it passes**

```bash
cd sdk && ./gradlew test --tests "com.androidagent.tools.core.ParameterValidatorTest"
```
Expected: PASS

- [x] **Step 5: Implement Tool interface**

```kotlin
// sdk/src/main/java/com/androidagent/tools/core/Tool.kt
package com.androidagent.tools.core

import android.content.Context
import org.json.JSONObject

interface Tool {
    val name: String
    val description: String

    fun validate(params: Map<String, Any?>): Result<Unit>

    suspend fun execute(context: Context, params: Map<String, Any?>): ToolResult

    fun executeJson(context: Context, paramsJson: String): String {
        val params = try {
            JSONObject(paramsJson).toMap()
        } catch (e: Exception) {
            return ToolResult.failure(ToolError.INVALID_PARAMETER, "Invalid JSON: ${e.message}").toJson().toString()
        }

        val validation = validate(params)
        if (validation.isFailure) {
            return ToolResult.failure(
                (validation as Result.Failure).error,
                validation.context
            ).toJson().toString()
        }

        // Note: execute is suspend, so this needs coroutine scope
        // For simplicity, we use runBlocking here; in production, use proper coroutine scope
        return kotlinx.coroutines.runBlocking {
            execute(context, params).toJson().toString()
        }
    }
}

fun JSONObject.toMap(): Map<String, Any?> {
    val map = mutableMapOf<String, Any?>()
    val keys = this.keys()
    while (keys.hasNext()) {
        val key = keys.next()
        map[key] = when (val value = this.get(key)) {
            JSONObject.NULL -> null
            is JSONObject -> value.toMap()
            is org.json.JSONArray -> value.toList()
            else -> value
        }
    }
    return map
}

fun org.json.JSONArray.toList(): List<Any?> {
    return (0 until length()).map { i ->
        when (val value = this.get(i)) {
            JSONObject.NULL -> null
            is JSONObject -> value.toMap()
            is org.json.JSONArray -> value.toList()
            else -> value
        }
    }
}
```

- [x] **Step 6: Commit**

```bash
git add sdk/src/main/java/com/androidagent/tools/core/*.kt
git add sdk/src/test/java/com/androidagent/tools/core/*.kt
git commit -m "feat(core): add Tool interface and ParameterValidator"
```

---

## Task 4: File Tools - ReadFileTool

**Files:**
- Create: `sdk/src/main/java/com/androidagent/tools/tools/file/ReadFileTool.kt`
- Create: `sdk/src/test/java/com/androidagent/tools/tools/file/ReadFileToolTest.kt`

- [x] **Step 1: Write ReadFileTool test**

```kotlin
// sdk/src/test/java/com/androidagent/tools/tools/file/ReadFileToolTest.kt
package com.androidagent.tools.tools.file

import com.androidagent.tools.core.ToolError
import com.androidagent.tools.core.ToolResult
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.io.File

class ReadFileToolTest {

    private val tool = ReadFileTool()
    private val mockContext = mockk<android.content.Context>(relaxed = true)

    @Test
    fun `validate fails when path is missing`() {
        val result = tool.validate(emptyMap())
        assertTrue(result.isFailure)
        assertEquals(ToolError.INVALID_PARAMETER, (result as? com.androidagent.tools.core.Result.Failure)?.error)
    }

    @Test
    fun `validate succeeds with valid path`() {
        val result = tool.validate(mapOf("path" to "/sdcard/test.txt"))
        assertTrue(result.isSuccess)
    }

    @Test
    fun `execute fails when file does not exist`() {
        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockContext, mapOf("path" to "/nonexistent/file.txt"))
        }
        assertTrue(result is ToolResult.Failure)
        assertEquals(ToolError.FILE_NOT_FOUND, (result as ToolResult.Failure).error)
    }

    @Test
    fun `execute succeeds for existing file`() {
        // Create temp file
        val tempFile = File.createTempFile("test", ".txt")
        tempFile.writeText("Hello, World!")

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockContext, mapOf("path" to tempFile.absolutePath))
        }

        assertTrue(result is ToolResult.Success)
        val data = (result as ToolResult.Success).data
        assertEquals("Hello, World!", data["content"])

        tempFile.delete()
    }
}
```

- [x] **Step 2: Run test to verify it fails**

```bash
cd sdk && ./gradlew test --tests "com.androidagent.tools.tools.file.ReadFileToolTest"
```
Expected: FAIL with "Unresolved reference: ReadFileTool"

- [x] **Step 3: Implement ReadFileTool**

```kotlin
// sdk/src/main/java/com/androidagent/tools/tools/file/ReadFileTool.kt
package com.androidagent.tools.tools.file

import android.content.Context
import com.androidagent.tools.core.*
import java.io.File
import java.nio.charset.Charset

class ReadFileTool : Tool {
    override val name = "read_file"
    override val description = "Read the content of a file at the specified path. Only supports text files."

    override fun validate(params: Map<String, Any?>): Result<Unit> {
        val validator = ParameterValidator(params)
        val pathResult = validator.requireString("path")
        return if (pathResult.isFailure) {
            pathResult as Result.Failure
        } else {
            Result.Success(Unit)
        }
    }

    override suspend fun execute(context: Context, params: Map<String, Any?>): ToolResult {
        val validator = ParameterValidator(params)
        val path = (validator.requireString("path") as Result.Success).value
        val encoding = validator.optionalString("encoding", "utf-8")

        val file = File(path)

        if (!file.exists()) {
            return ToolResult.failure(ToolError.FILE_NOT_FOUND, path)
        }

        if (!file.isFile) {
            return ToolResult.failure(ToolError.NOT_A_FILE, path)
        }

        return try {
            val charset = try {
                Charset.forName(encoding)
            } catch (e: Exception) {
                Charset.defaultCharset()
            }
            val content = file.readText(charset)
            ToolResult.success(mapOf(
                "content" to content,
                "size" to content.toByteArray(charset).size
            ))
        } catch (e: Exception) {
            ToolResult.failure(ToolError.READ_ERROR, "${e.message}")
        }
    }
}
```

- [x] **Step 4: Run test to verify it passes**

```bash
cd sdk && ./gradlew test --tests "com.androidagent.tools.tools.file.ReadFileToolTest"
```
Expected: PASS

- [x] **Step 5: Commit**

```bash
git add sdk/src/main/java/com/androidagent/tools/tools/file/ReadFileTool.kt
git add sdk/src/test/java/com/androidagent/tools/tools/file/ReadFileToolTest.kt
git commit -m "feat(file): add ReadFileTool with tests"
```

---

## Task 5: File Tools - WriteFileTool

**Files:**
- Create: `sdk/src/main/java/com/androidagent/tools/tools/file/WriteFileTool.kt`
- Create: `sdk/src/test/java/com/androidagent/tools/tools/file/WriteFileToolTest.kt`

- [x] **Step 1: Write WriteFileTool test**

```kotlin
// sdk/src/test/java/com/androidagent/tools/tools/file/WriteFileToolTest.kt
package com.androidagent.tools.tools.file

import com.androidagent.tools.core.ToolError
import com.androidagent.tools.core.ToolResult
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.io.File

class WriteFileToolTest {

    private val tool = WriteFileTool()
    private val mockContext = mockk<android.content.Context>(relaxed = true)

    @Test
    fun `validate fails when path is missing`() {
        val result = tool.validate(mapOf("content" to "test"))
        assertTrue(result.isFailure)
    }

    @Test
    fun `validate fails when content is missing`() {
        val result = tool.validate(mapOf("path" to "/test.txt"))
        assertTrue(result.isFailure)
    }

    @Test
    fun `validate succeeds with path and content`() {
        val result = tool.validate(mapOf("path" to "/test.txt", "content" to "Hello"))
        assertTrue(result.isSuccess)
    }

    @Test
    fun `execute creates file and writes content`() {
        val tempDir = System.getProperty("java.io.tmpdir")
        val testFile = File(tempDir, "write_test_${System.currentTimeMillis()}.txt")

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockContext, mapOf(
                "path" to testFile.absolutePath,
                "content" to "Hello, World!"
            ))
        }

        assertTrue(result is ToolResult.Success)
        assertTrue(testFile.exists())
        assertEquals("Hello, World!", testFile.readText())

        testFile.delete()
    }

    @Test
    fun `execute overwrites existing file`() {
        val tempFile = File.createTempFile("overwrite_test", ".txt")
        tempFile.writeText("Original")

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockContext, mapOf(
                "path" to tempFile.absolutePath,
                "content" to "New Content"
            ))
        }

        assertTrue(result is ToolResult.Success)
        assertEquals("New Content", tempFile.readText())

        tempFile.delete()
    }
}
```

- [x] **Step 2: Run test to verify it fails**

```bash
cd sdk && ./gradlew test --tests "com.androidagent.tools.tools.file.WriteFileToolTest"
```
Expected: FAIL

- [x] **Step 3: Implement WriteFileTool**

```kotlin
// sdk/src/main/java/com/androidagent/tools/tools/file/WriteFileTool.kt
package com.androidagent.tools.tools.file

import android.content.Context
import com.androidagent.tools.core.*
import java.io.File
import java.nio.charset.Charset

class WriteFileTool : Tool {
    override val name = "write_file"
    override val description = "Write content to a file at the specified path. Creates the file if it does not exist, overwrites if it does."

    override fun validate(params: Map<String, Any?>): Result<Unit> {
        val validator = ParameterValidator(params)
        val pathResult = validator.requireString("path")
        if (pathResult.isFailure) return pathResult as Result.Failure

        val contentResult = validator.requireString("content")
        if (contentResult.isFailure) return contentResult as Result.Failure

        return Result.Success(Unit)
    }

    override suspend fun execute(context: Context, params: Map<String, Any?>): ToolResult {
        val validator = ParameterValidator(params)
        val path = (validator.requireString("path") as Result.Success).value
        val content = (validator.requireString("content") as Result.Success).value
        val encoding = validator.optionalString("encoding", "utf-8")

        val file = File(path)

        return try {
            val charset = try {
                Charset.forName(encoding)
            } catch (e: Exception) {
                Charset.defaultCharset()
            }

            // Create parent directories if needed
            file.parentFile?.mkdirs()

            file.writeText(content, charset)

            ToolResult.success(mapOf(
                "bytes_written" to content.toByteArray(charset).size
            ))
        } catch (e: Exception) {
            ToolResult.failure(ToolError.WRITE_ERROR, "${e.message}")
        }
    }
}
```

- [x] **Step 4: Run test to verify it passes**

```bash
cd sdk && ./gradlew test --tests "com.androidagent.tools.tools.file.WriteFileToolTest"
```
Expected: PASS

- [x] **Step 5: Commit**

```bash
git add sdk/src/main/java/com/androidagent/tools/tools/file/WriteFileTool.kt
git add sdk/src/test/java/com/androidagent/tools/tools/file/WriteFileToolTest.kt
git commit -m "feat(file): add WriteFileTool with tests"
```

---

## Task 6: File Tools - ListDirectoryTool, DeleteFileTool, FileExistsTool

**Files:**
- Create: `sdk/src/main/java/com/androidagent/tools/tools/file/ListDirectoryTool.kt`
- Create: `sdk/src/main/java/com/androidagent/tools/tools/file/DeleteFileTool.kt`
- Create: `sdk/src/main/java/com/androidagent/tools/tools/file/FileExistsTool.kt`
- Create: `sdk/src/test/java/com/androidagent/tools/tools/file/ListDirectoryToolTest.kt`
- Create: `sdk/src/test/java/com/androidagent/tools/tools/file/DeleteFileToolTest.kt`
- Create: `sdk/src/test/java/com/androidagent/tools/tools/file/FileExistsToolTest.kt`

- [x] **Step 1: Write ListDirectoryTool test**

```kotlin
// sdk/src/test/java/com/androidagent/tools/tools/file/ListDirectoryToolTest.kt
package com.androidagent.tools.tools.file

import com.androidagent.tools.core.ToolError
import com.androidagent.tools.core.ToolResult
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.io.File

class ListDirectoryToolTest {

    private val tool = ListDirectoryTool()
    private val mockContext = mockk<android.content.Context>(relaxed = true)

    @Test
    fun `validate fails when path is missing`() {
        val result = tool.validate(emptyMap())
        assertTrue(result.isFailure)
    }

    @Test
    fun `execute fails when directory does not exist`() {
        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockContext, mapOf("path" to "/nonexistent/dir"))
        }
        assertTrue(result is ToolResult.Failure)
        assertEquals(ToolError.DIRECTORY_NOT_FOUND, (result as ToolResult.Failure).error)
    }

    @Test
    fun `execute lists directory contents`() {
        val tempDir = File(System.getProperty("java.io.tmpdir"), "list_test_${System.currentTimeMillis()}")
        tempDir.mkdirs()
        File(tempDir, "file1.txt").createNewFile()
        File(tempDir, "subdir").mkdirs()

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockContext, mapOf("path" to tempDir.absolutePath))
        }

        assertTrue(result is ToolResult.Success)
        val entries = (result as ToolResult.Success).data["entries"] as List<*>
        assertEquals(2, entries.size)

        tempDir.deleteRecursively()
    }
}
```

- [x] **Step 2: Implement ListDirectoryTool**

```kotlin
// sdk/src/main/java/com/androidagent/tools/tools/file/ListDirectoryTool.kt
package com.androidagent.tools.tools.file

import android.content.Context
import com.androidagent.tools.core.*
import java.io.File

class ListDirectoryTool : Tool {
    override val name = "list_directory"
    override val description = "List contents of a directory."

    override fun validate(params: Map<String, Any?>): Result<Unit> {
        val validator = ParameterValidator(params)
        val pathResult = validator.requireString("path")
        return if (pathResult.isFailure) pathResult as Result.Failure else Result.Success(Unit)
    }

    override suspend fun execute(context: Context, params: Map<String, Any?>): ToolResult {
        val validator = ParameterValidator(params)
        val path = (validator.requireString("path") as Result.Success).value

        val dir = File(path)

        if (!dir.exists()) {
            return ToolResult.failure(ToolError.DIRECTORY_NOT_FOUND, path)
        }

        if (!dir.isDirectory) {
            return ToolResult.failure(ToolError.NOT_A_DIRECTORY, path)
        }

        val entries = dir.listFiles()?.map { file ->
            mapOf(
                "name" to file.name,
                "type" to if (file.isDirectory) "directory" else "file",
                "size" to if (file.isFile) file.length() else null
            )
        } ?: emptyList()

        return ToolResult.success(mapOf("entries" to entries))
    }
}
```

- [x] **Step 3: Write DeleteFileTool test**

```kotlin
// sdk/src/test/java/com/androidagent/tools/tools/file/DeleteFileToolTest.kt
package com.androidagent.tools.tools.file

import com.androidagent.tools.core.ToolError
import com.androidagent.tools.core.ToolResult
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.io.File

class DeleteFileToolTest {

    private val tool = DeleteFileTool()
    private val mockContext = mockk<android.content.Context>(relaxed = true)

    @Test
    fun `execute deletes existing file`() {
        val tempFile = File.createTempFile("delete_test", ".txt")
        assertTrue(tempFile.exists())

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockContext, mapOf("path" to tempFile.absolutePath))
        }

        assertTrue(result is ToolResult.Success)
        assertFalse(tempFile.exists())
    }

    @Test
    fun `execute fails when file does not exist`() {
        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockContext, mapOf("path" to "/nonexistent/file.txt"))
        }
        assertTrue(result is ToolResult.Failure)
        assertEquals(ToolError.FILE_NOT_FOUND, (result as ToolResult.Failure).error)
    }
}
```

- [x] **Step 4: Implement DeleteFileTool**

```kotlin
// sdk/src/main/java/com/androidagent/tools/tools/file/DeleteFileTool.kt
package com.androidagent.tools.tools.file

import android.content.Context
import com.androidagent.tools.core.*
import java.io.File

class DeleteFileTool : Tool {
    override val name = "delete_file"
    override val description = "Delete a file at the specified path."

    override fun validate(params: Map<String, Any?>): Result<Unit> {
        val validator = ParameterValidator(params)
        val pathResult = validator.requireString("path")
        return if (pathResult.isFailure) pathResult as Result.Failure else Result.Success(Unit)
    }

    override suspend fun execute(context: Context, params: Map<String, Any?>): ToolResult {
        val validator = ParameterValidator(params)
        val path = (validator.requireString("path") as Result.Success).value

        val file = File(path)

        if (!file.exists()) {
            return ToolResult.failure(ToolError.FILE_NOT_FOUND, path)
        }

        return if (file.delete()) {
            ToolResult.success()
        } else {
            ToolResult.failure(ToolError.WRITE_ERROR, "Failed to delete: $path")
        }
    }
}
```

- [x] **Step 5: Write FileExistsTool test**

```kotlin
// sdk/src/test/java/com/androidagent/tools/tools/file/FileExistsToolTest.kt
package com.androidagent.tools.tools.file

import com.androidagent.tools.core.ToolResult
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.io.File

class FileExistsToolTest {

    private val tool = FileExistsTool()
    private val mockContext = mockk<android.content.Context>(relaxed = true)

    @Test
    fun `execute returns exists true for existing file`() {
        val tempFile = File.createTempFile("exists_test", ".txt")

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockContext, mapOf("path" to tempFile.absolutePath))
        }

        assertTrue(result is ToolResult.Success)
        val data = (result as ToolResult.Success).data
        assertTrue(data["exists"] as Boolean)
        assertEquals("file", data["type"])

        tempFile.delete()
    }

    @Test
    fun `execute returns exists false for nonexistent file`() {
        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockContext, mapOf("path" to "/nonexistent/file.txt"))
        }

        assertTrue(result is ToolResult.Success)
        val data = (result as ToolResult.Success).data
        assertFalse(data["exists"] as Boolean)
    }

    @Test
    fun `execute identifies directory type`() {
        val tempDir = File(System.getProperty("java.io.tmpdir"), "exists_dir_test_${System.currentTimeMillis()}")
        tempDir.mkdirs()

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockContext, mapOf("path" to tempDir.absolutePath))
        }

        assertTrue(result is ToolResult.Success)
        val data = (result as ToolResult.Success).data
        assertTrue(data["exists"] as Boolean)
        assertEquals("directory", data["type"])

        tempDir.delete()
    }
}
```

- [x] **Step 6: Implement FileExistsTool**

```kotlin
// sdk/src/main/java/com/androidagent/tools/tools/file/FileExistsTool.kt
package com.androidagent.tools.tools.file

import android.content.Context
import com.androidagent.tools.core.*
import java.io.File

class FileExistsTool : Tool {
    override val name = "file_exists"
    override val description = "Check if a file or directory exists at the specified path."

    override fun validate(params: Map<String, Any?>): Result<Unit> {
        val validator = ParameterValidator(params)
        val pathResult = validator.requireString("path")
        return if (pathResult.isFailure) pathResult as Result.Failure else Result.Success(Unit)
    }

    override suspend fun execute(context: Context, params: Map<String, Any?>): ToolResult {
        val validator = ParameterValidator(params)
        val path = (validator.requireString("path") as Result.Success).value

        val file = File(path)

        return ToolResult.success(mapOf(
            "exists" to file.exists(),
            "type" to when {
                !file.exists() -> null
                file.isDirectory -> "directory"
                else -> "file"
            }
        ))
    }
}
```

- [x] **Step 7: Run all file tool tests**

```bash
cd sdk && ./gradlew test --tests "com.androidagent.tools.tools.file.*"
```
Expected: PASS

- [x] **Step 8: Commit**

```bash
git add sdk/src/main/java/com/androidagent/tools/tools/file/*.kt
git add sdk/src/test/java/com/androidagent/tools/tools/file/*.kt
git commit -m "feat(file): add ListDirectoryTool, DeleteFileTool, FileExistsTool with tests"
```

---

## Task 7: App Tools - ListAppsTool, GetAppInfoTool, LaunchAppTool

**Files:**
- Create: `sdk/src/main/java/com/androidagent/tools/tools/app/ListAppsTool.kt`
- Create: `sdk/src/main/java/com/androidagent/tools/tools/app/GetAppInfoTool.kt`
- Create: `sdk/src/main/java/com/androidagent/tools/tools/app/LaunchAppTool.kt`
- Create: `sdk/src/test/java/com/androidagent/tools/tools/app/ListAppsToolTest.kt`
- Create: `sdk/src/test/java/com/androidagent/tools/tools/app/GetAppInfoToolTest.kt`
- Create: `sdk/src/test/java/com/androidagent/tools/tools/app/LaunchAppToolTest.kt`

- [x] **Step 1: Write ListAppsTool test**

```kotlin
// sdk/src/test/java/com/androidagent/tools/tools/app/ListAppsToolTest.kt
package com.androidagent.tools.tools.app

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.androidagent.tools.core.ToolResult
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ListAppsToolTest {

    private val tool = ListAppsTool()

    @Test
    fun `validate succeeds with no parameters`() {
        val result = tool.validate(emptyMap())
        assertTrue(result.isSuccess)
    }

    @Test
    fun `execute returns app list`() {
        val mockContext = mockk<android.content.Context>()
        val mockPackageManager = mockk<PackageManager>()

        val mockAppInfo = mockk<ApplicationInfo>()
        mockAppInfo.packageName = "com.example.app"
        mockAppInfo.flags = 0

        every { mockContext.packageManager } returns mockPackageManager
        every { mockPackageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns listOf(mockAppInfo)
        every { mockPackageManager.getApplicationLabel(mockAppInfo) } returns "Example App"
        every { mockContext.packageName } returns "com.test"

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockContext, emptyMap())
        }

        assertTrue(result is ToolResult.Success)
        val data = (result as ToolResult.Success).data
        assertTrue(data.containsKey("apps"))
    }
}
```

- [x] **Step 2: Implement ListAppsTool**

```kotlin
// sdk/src/main/java/com/androidagent/tools/tools/app/ListAppsTool.kt
package com.androidagent.tools.tools.app

import android.content.Context
import android.content.pm.PackageManager
import com.androidagent.tools.core.*

class ListAppsTool : Tool {
    override val name = "list_apps"
    override val description = "List all installed applications on the device."

    override fun validate(params: Map<String, Any?>): Result<Unit> = Result.Success(Unit)

    override suspend fun execute(context: Context, params: Map<String, Any?>): ToolResult {
        val validator = ParameterValidator(params)
        val includeSystemApps = validator.optionalBoolean("include_system_apps", false)

        val packageManager = context.packageManager
        val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        val currentPackage = context.packageName

        val apps = installedApps
            .filter { appInfo ->
                // Exclude current app
                appInfo.packageName != currentPackage &&
                // Filter system apps if not included
                (includeSystemApps || (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0)
            }
            .map { appInfo ->
                val packageInfo = try {
                    packageManager.getPackageInfo(appInfo.packageName, 0)
                } catch (e: Exception) { null }

                mapOf(
                    "package_name" to appInfo.packageName,
                    "label" to (packageManager.getApplicationLabel(appInfo)?.toString() ?: ""),
                    "version_name" to (packageInfo?.versionName ?: ""),
                    "is_system" to (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                )
            }

        return ToolResult.success(mapOf("apps" to apps))
    }
}
```

- [x] **Step 3: Write GetAppInfoTool test**

```kotlin
// sdk/src/test/java/com/androidagent/tools/tools/app/GetAppInfoToolTest.kt
package com.androidagent.tools.tools.app

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.androidagent.tools.core.ToolError
import com.androidagent.tools.core.ToolResult
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class GetAppInfoToolTest {

    private val tool = GetAppInfoTool()

    @Test
    fun `validate fails when package_name is missing`() {
        val result = tool.validate(emptyMap())
        assertTrue(result.isFailure)
    }

    @Test
    fun `execute fails when app not found`() {
        val mockContext = mockk<android.content.Context>()
        val mockPackageManager = mockk<PackageManager>()

        every { mockContext.packageManager } returns mockPackageManager
        every { mockPackageManager.getPackageInfo("com.nonexistent.app", any()) } throws PackageManager.NameNotFoundException()

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockContext, mapOf("package_name" to "com.nonexistent.app"))
        }

        assertTrue(result is ToolResult.Failure)
        assertEquals(ToolError.APP_NOT_FOUND, (result as ToolResult.Failure).error)
    }
}
```

- [x] **Step 4: Implement GetAppInfoTool**

```kotlin
// sdk/src/main/java/com/androidagent/tools/tools/app/GetAppInfoTool.kt
package com.androidagent.tools.tools.app

import android.content.Context
import android.content.pm.PackageManager
import com.androidagent.tools.core.*

class GetAppInfoTool : Tool {
    override val name = "get_app_info"
    override val description = "Get detailed information about an application."

    override fun validate(params: Map<String, Any?>): Result<Unit> {
        val validator = ParameterValidator(params)
        val result = validator.requireString("package_name")
        return if (result.isFailure) result as Result.Failure else Result.Success(Unit)
    }

    override suspend fun execute(context: Context, params: Map<String, Any?>): ToolResult {
        val validator = ParameterValidator(params)
        val packageName = (validator.requireString("package_name") as Result.Success).value

        val packageManager = context.packageManager

        return try {
            val packageInfo = packageManager.getPackageInfo(packageName,
                PackageManager.GET_PERMISSIONS or PackageManager.GET_META_DATA)
            val appInfo = packageInfo.applicationInfo

            val permissions = packageInfo.requestedPermissions?.toList() ?: emptyList()

            ToolResult.success(mapOf(
                "package_name" to packageName,
                "label" to (packageManager.getApplicationLabel(appInfo)?.toString() ?: ""),
                "version_name" to (packageInfo.versionName ?: ""),
                "version_code" to packageInfo.longVersionCode.toInt(),
                "is_system" to (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0,
                "install_time" to packageInfo.firstInstallTime,
                "update_time" to packageInfo.lastUpdateTime,
                "data_dir" to (appInfo.dataDir ?: ""),
                "permissions" to permissions
            ))
        } catch (e: PackageManager.NameNotFoundException) {
            ToolResult.failure(ToolError.APP_NOT_FOUND, packageName)
        }
    }
}
```

- [x] **Step 5: Write LaunchAppTool test**

```kotlin
// sdk/src/test/java/com/androidagent/tools/tools/app/LaunchAppToolTest.kt
package com.androidagent.tools.tools.app

import android.content.Intent
import android.content.pm.PackageManager
import com.androidagent.tools.core.ToolError
import com.androidagent.tools.core.ToolResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class LaunchAppToolTest {

    private val tool = LaunchAppTool()

    @Test
    fun `validate fails when package_name is missing`() {
        val result = tool.validate(emptyMap())
        assertTrue(result.isFailure)
    }

    @Test
    fun `execute launches app successfully`() {
        val mockContext = mockk<android.content.Context>(relaxed = true)
        val mockPackageManager = mockk<PackageManager>()
        val mockIntent = mockk<Intent>(relaxed = true)

        every { mockContext.packageManager } returns mockPackageManager
        every { mockPackageManager.getLaunchIntentForPackage("com.example.app") } returns mockIntent
        every { mockContext.startActivity(any()) } returns Unit

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockContext, mapOf("package_name" to "com.example.app"))
        }

        assertTrue(result is ToolResult.Success)
        verify { mockContext.startActivity(any()) }
    }

    @Test
    fun `execute fails when app not launchable`() {
        val mockContext = mockk<android.content.Context>()
        val mockPackageManager = mockk<PackageManager>()

        every { mockContext.packageManager } returns mockPackageManager
        every { mockPackageManager.getLaunchIntentForPackage("com.example.app") } returns null

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockContext, mapOf("package_name" to "com.example.app"))
        }

        assertTrue(result is ToolResult.Failure)
        assertEquals(ToolError.APP_NOT_LAUNCHABLE, (result as ToolResult.Failure).error)
    }
}
```

- [x] **Step 6: Implement LaunchAppTool**

```kotlin
// sdk/src/main/java/com/androidagent/tools/tools/app/LaunchAppTool.kt
package com.androidagent.tools.tools.app

import android.content.Context
import com.androidagent.tools.core.*

class LaunchAppTool : Tool {
    override val name = "launch_app"
    override val description = "Launch an application by package name."

    override fun validate(params: Map<String, Any?>): Result<Unit> {
        val validator = ParameterValidator(params)
        val result = validator.requireString("package_name")
        return if (result.isFailure) result as Result.Failure else Result.Success(Unit)
    }

    override suspend fun execute(context: Context, params: Map<String, Any?>): ToolResult {
        val validator = ParameterValidator(params)
        val packageName = (validator.requireString("package_name") as Result.Success).value

        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(packageName)

        if (intent == null) {
            return ToolResult.failure(ToolError.APP_NOT_LAUNCHABLE, packageName)
        }

        return try {
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            ToolResult.success()
        } catch (e: Exception) {
            ToolResult.failure(ToolError.LAUNCH_FAILED, "${e.message}")
        }
    }
}
```

- [x] **Step 7: Run all app tool tests**

```bash
cd sdk && ./gradlew test --tests "com.androidagent.tools.tools.app.*"
```
Expected: PASS

- [x] **Step 8: Commit**

```bash
git add sdk/src/main/java/com/androidagent/tools/tools/app/*.kt
git add sdk/src/test/java/com/androidagent/tools/tools/app/*.kt
git commit -m "feat(app): add ListAppsTool, GetAppInfoTool, LaunchAppTool with tests"
```

---

## Task 8: System Tools - GetDeviceInfoTool, GetBatteryStatusTool

**Files:**
- Create: `sdk/src/main/java/com/androidagent/tools/tools/system/GetDeviceInfoTool.kt`
- Create: `sdk/src/main/java/com/androidagent/tools/tools/system/GetBatteryStatusTool.kt`
- Create: `sdk/src/test/java/com/androidagent/tools/tools/system/GetDeviceInfoToolTest.kt`
- Create: `sdk/src/test/java/com/androidagent/tools/tools/system/GetBatteryStatusToolTest.kt`

- [x] **Step 1: Write GetDeviceInfoTool test**

```kotlin
// sdk/src/test/java/com/androidagent/tools/tools/system/GetDeviceInfoToolTest.kt
package com.androidagent.tools.tools.system

import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import com.androidagent.tools.core.ToolResult
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class GetDeviceInfoToolTest {

    private val tool = GetDeviceInfoTool()

    @Test
    fun `validate succeeds with no parameters`() {
        val result = tool.validate(emptyMap())
        assertTrue(result.isSuccess)
    }

    @Test
    fun `execute returns device info`() {
        val mockContext = mockk<android.content.Context>(relaxed = true)
        val mockWindowManager = mockk<WindowManager>()
        val mockDisplay = mockk<android.view.Display>()
        val mockMetrics = DisplayMetrics().apply {
            widthPixels = 1080
            heightPixels = 2340
            densityDpi = 420
        }

        every { mockContext.getSystemService(android.content.Context.WINDOW_SERVICE) } returns mockWindowManager
        every { mockWindowManager.defaultDisplay } returns mockDisplay
        every { mockDisplay.getMetrics(any()) } answers {
            val metrics = firstArg<DisplayMetrics>()
            metrics.widthPixels = mockMetrics.widthPixels
            metrics.heightPixels = mockMetrics.heightPixels
            metrics.densityDpi = mockMetrics.densityDpi
        }
        every { mockContext.resources.configuration } returns mockk(relaxed = true)

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockContext, emptyMap())
        }

        assertTrue(result is ToolResult.Success)
        val data = (result as ToolResult.Success).data
        assertEquals(Build.BRAND, data["brand"])
        assertEquals(Build.MODEL, data["model"])
        assertEquals(Build.VERSION.RELEASE, data["android_version"])
    }
}
```

- [x] **Step 2: Implement GetDeviceInfoTool**

```kotlin
// sdk/src/main/java/com/androidagent/tools/tools/system/GetDeviceInfoTool.kt
package com.androidagent.tools.tools.system

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import com.androidagent.tools.core.*
import java.util.Locale
import java.util.TimeZone

class GetDeviceInfoTool : Tool {
    override val name = "get_device_info"
    override val description = "Get device information including model, OS version, screen size, etc."

    override fun validate(params: Map<String, Any?>): Result<Unit> = Result.Success(Unit)

    override suspend fun execute(context: Context, params: Map<String, Any?>): ToolResult {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? android.view.WindowManager
        val display = windowManager?.defaultDisplay
        val metrics = DisplayMetrics()
        display?.getMetrics(metrics)

        val configuration = context.resources.configuration

        return ToolResult.success(mapOf(
            "brand" to Build.BRAND,
            "model" to Build.MODEL,
            "device" to Build.DEVICE,
            "android_version" to Build.VERSION.RELEASE,
            "sdk_version" to Build.VERSION.SDK_INT,
            "screen_width" to metrics.widthPixels,
            "screen_height" to metrics.heightPixels,
            "screen_density" to metrics.densityDpi,
            "locale" to "${configuration.locale?.language ?: Locale.getDefault().language}_${configuration.locale?.country ?: Locale.getDefault().country}",
            "timezone" to TimeZone.getDefault().id
        ))
    }
}
```

- [x] **Step 3: Write GetBatteryStatusTool test**

```kotlin
// sdk/src/test/java/com/androidagent/tools/tools/system/GetBatteryStatusToolTest.kt
package com.androidagent.tools.tools.system

import android.content.Intent
import android.os.BatteryManager
import com.androidagent.tools.core.ToolResult
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class GetBatteryStatusToolTest {

    private val tool = GetBatteryStatusTool()

    @Test
    fun `validate succeeds with no parameters`() {
        val result = tool.validate(emptyMap())
        assertTrue(result.isSuccess)
    }

    @Test
    fun `execute returns battery status`() {
        val mockContext = mockk<android.content.Context>()
        val mockIntent = mockk<Intent>()

        every { mockContext.registerReceiver(null, any<Intent>()) } returns mockIntent
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) } returns 85
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1) } returns 100
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1) } returns BatteryManager.BATTERY_STATUS_CHARGING
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) } returns BatteryManager.BATTERY_HEALTH_GOOD
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) } returns 250
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) } returns 4200

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockContext, emptyMap())
        }

        assertTrue(result is ToolResult.Success)
        val data = (result as ToolResult.Success).data
        assertEquals(85, data["level"])
        assertEquals("charging", data["status"])
    }
}
```

- [x] **Step 4: Implement GetBatteryStatusTool**

```kotlin
// sdk/src/main/java/com/androidagent/tools/tools/system/GetBatteryStatusTool.kt
package com.androidagent.tools.tools.system

import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import com.androidagent.tools.core.*

class GetBatteryStatusTool : Tool {
    override val name = "get_battery_status"
    override val description = "Get current battery status including level, charging state, etc."

    override fun validate(params: Map<String, Any?>): Result<Unit> = Result.Success(Unit)

    override suspend fun execute(context: Context, params: Map<String, Any?>): ToolResult {
        val intent = context.registerReceiver(null, Intent(Intent.ACTION_BATTERY_CHANGED))

        if (intent == null) {
            return ToolResult.failure(ToolError.UNSUPPORTED_OPERATION, "Cannot get battery status")
        }

        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
        val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)

        return ToolResult.success(mapOf(
            "level" to level,
            "scale" to scale,
            "status" to getBatteryStatusString(status),
            "health" to getBatteryHealthString(health),
            "temperature" to temperature / 10.0, // Convert from tenths of a degree
            "voltage" to voltage / 1000.0 // Convert from millivolts
        ))
    }

    private fun getBatteryStatusString(status: Int): String = when (status) {
        BatteryManager.BATTERY_STATUS_CHARGING -> "charging"
        BatteryManager.BATTERY_STATUS_DISCHARGING -> "discharging"
        BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "not_charging"
        BatteryManager.BATTERY_STATUS_FULL -> "full"
        else -> "unknown"
    }

    private fun getBatteryHealthString(health: Int): String = when (health) {
        BatteryManager.BATTERY_HEALTH_GOOD -> "good"
        BatteryManager.BATTERY_HEALTH_OVERHEAT -> "overheat"
        BatteryManager.BATTERY_HEALTH_DEAD -> "dead"
        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "over_voltage"
        BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "failure"
        else -> "unknown"
    }
}
```

- [x] **Step 5: Run all system tool tests**

```bash
cd sdk && ./gradlew test --tests "com.androidagent.tools.tools.system.*"
```
Expected: PASS

- [x] **Step 6: Commit**

```bash
git add sdk/src/main/java/com/androidagent/tools/tools/system/*.kt
git add sdk/src/test/java/com/androidagent/tools/tools/system/*.kt
git commit -m "feat(system): add GetDeviceInfoTool, GetBatteryStatusTool with tests"
```

---

## Task 9: Permission Tool - CheckPermissionsTool

**Files:**
- Create: `sdk/src/main/java/com/androidagent/tools/tools/permission/CheckPermissionsTool.kt`
- Create: `sdk/src/test/java/com/androidagent/tools/tools/permission/CheckPermissionsToolTest.kt`

- [x] **Step 1: Write CheckPermissionsTool test**

```kotlin
// sdk/src/test/java/com/androidagent/tools/tools/permission/CheckPermissionsToolTest.kt
package com.androidagent.tools.tools.permission

import android.content.pm.PackageManager
import com.androidagent.tools.core.ToolError
import com.androidagent.tools.core.ToolResult
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class CheckPermissionsToolTest {

    private val tool = CheckPermissionsTool()

    @Test
    fun `validate fails when permissions is missing`() {
        val result = tool.validate(emptyMap())
        assertTrue(result.isFailure)
    }

    @Test
    fun `validate fails when permissions is not array`() {
        val result = tool.validate(mapOf("permissions" to "storage"))
        assertTrue(result.isFailure)
    }

    @Test
    fun `execute returns permission status`() {
        val mockContext = mockk<android.content.Context>()

        every { mockContext.checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") } returns PackageManager.PERMISSION_GRANTED
        every { mockContext.checkSelfPermission("android.permission.CAMERA") } returns PackageManager.PERMISSION_DENIED

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockContext, mapOf("permissions" to listOf("storage", "camera")))
        }

        assertTrue(result is ToolResult.Success)
        val data = (result as ToolResult.Success).data
        assertEquals("granted", data["storage"])
        assertEquals("denied", data["camera"])
    }
}
```

- [x] **Step 2: Implement CheckPermissionsTool**

```kotlin
// sdk/src/main/java/com/androidagent/tools/tools/permission/CheckPermissionsTool.kt
package com.androidagent.tools.tools.permission

import android.content.Context
import android.content.pm.PackageManager
import com.androidagent.tools.core.*

class CheckPermissionsTool : Tool {
    override val name = "check_permissions"
    override val description = "Check the status of specified permissions."

    private val permissionMapping = mapOf(
        "storage" to listOf(
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"
        ),
        "camera" to listOf("android.permission.CAMERA"),
        "location" to listOf(
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION"
        ),
        "microphone" to listOf("android.permission.RECORD_AUDIO"),
        "contacts" to listOf(
            "android.permission.READ_CONTACTS",
            "android.permission.WRITE_CONTACTS"
        ),
        "sms" to listOf(
            "android.permission.SEND_SMS",
            "android.permission.RECEIVE_SMS",
            "android.permission.READ_SMS"
        ),
        "phone" to listOf(
            "android.permission.READ_PHONE_STATE",
            "android.permission.CALL_PHONE"
        )
    )

    override fun validate(params: Map<String, Any?>): Result<Unit> {
        val validator = ParameterValidator(params)
        val result = validator.requireArray("permissions")
        return if (result.isFailure) result as Result.Failure else Result.Success(Unit)
    }

    override suspend fun execute(context: Context, params: Map<String, Any?>): ToolResult {
        val validator = ParameterValidator(params)
        val permissions = (validator.requireArray("permissions") as Result.Success).value

        val statusMap = mutableMapOf<String, String>()

        for (permission in permissions) {
            val androidPermissions = permissionMapping[permission]
            if (androidPermissions.isNullOrEmpty()) {
                statusMap[permission] = "unknown"
                continue
            }

            // Check if all required permissions are granted
            val allGranted = androidPermissions.all {
                context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
            }

            statusMap[permission] = if (allGranted) "granted" else "denied"
        }

        return ToolResult.success(statusMap as Map<String, Any?>)
    }
}
```

- [x] **Step 3: Run test to verify it passes**

```bash
cd sdk && ./gradlew test --tests "com.androidagent.tools.tools.permission.CheckPermissionsToolTest"
```
Expected: PASS

- [x] **Step 4: Commit**

```bash
git add sdk/src/main/java/com/androidagent/tools/tools/permission/CheckPermissionsTool.kt
git add sdk/src/test/java/com/androidagent/tools/tools/permission/CheckPermissionsToolTest.kt
git commit -m "feat(permission): add CheckPermissionsTool with tests"
```

---

## Task 10: ToolExecutor and Main Entry Point

**Files:**
- Create: `sdk/src/main/java/com/androidagent/tools/executor/ToolExecutor.kt`
- Create: `sdk/src/main/java/com/androidagent/tools/AndroidAgentTools.kt`
- Create: `sdk/src/test/java/com/androidagent/tools/executor/ToolExecutorTest.kt`

- [x] **Step 1: Write ToolExecutor test**

```kotlin
// sdk/src/test/java/com/androidagent/tools/executor/ToolExecutorTest.kt
package com.androidagent.tools.executor

import com.androidagent.tools.core.Tool
import com.androidagent.tools.core.ToolResult
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ToolExecutorTest {

    @Test
    fun `executor registers and retrieves tools`() {
        val executor = ToolExecutor()
        val mockTool = mockk<Tool> {
            every { name } returns "test_tool"
        }

        executor.register(mockTool)

        assertEquals(mockTool, executor.getTool("test_tool"))
        assertNull(executor.getTool("nonexistent"))
    }

    @Test
    fun `executor lists all registered tools`() {
        val executor = ToolExecutor()
        val mockTool1 = mockk<Tool> { every { name } returns "tool1" }
        val mockTool2 = mockk<Tool> { every { name } returns "tool2" }

        executor.register(mockTool1)
        executor.register(mockTool2)

        val tools = executor.listTools()
        assertEquals(2, tools.size)
        assertTrue(tools.contains("tool1"))
        assertTrue(tools.contains("tool2"))
    }
}
```

- [x] **Step 2: Implement ToolExecutor**

```kotlin
// sdk/src/main/java/com/androidagent/tools/executor/ToolExecutor.kt
package com.androidagent.tools.executor

import android.content.Context
import com.androidagent.tools.core.Tool
import com.androidagent.tools.core.ToolResult

class ToolExecutor {
    private val tools = mutableMapOf<String, Tool>()

    fun register(tool: Tool) {
        tools[tool.name] = tool
    }

    fun getTool(name: String): Tool? = tools[name]

    fun listTools(): List<String> = tools.keys.toList()

    suspend fun execute(context: Context, toolName: String, params: Map<String, Any?>): ToolResult {
        val tool = tools[toolName]
            ?: return ToolResult.failure(com.androidagent.tools.core.ToolError.UNSUPPORTED_OPERATION, "Unknown tool: $toolName")

        return tool.execute(context, params)
    }
}
```

- [x] **Step 3: Write AndroidAgentTools entry point**

```kotlin
// sdk/src/main/java/com/androidagent/tools/AndroidAgentTools.kt
package com.androidagent.tools

import android.content.Context
import com.androidagent.tools.core.ToolResult
import com.androidagent.tools.executor.ToolExecutor
import com.androidagent.tools.tools.app.*
import com.androidagent.tools.tools.file.*
import com.androidagent.tools.tools.permission.CheckPermissionsTool
import com.androidagent.tools.tools.system.*
import kotlinx.coroutines.runBlocking
import org.json.JSONObject

class AndroidAgentTools(private val context: Context) {

    private val executor = ToolExecutor()

    init {
        // Register all tools
        // File tools
        executor.register(ReadFileTool())
        executor.register(WriteFileTool())
        executor.register(ListDirectoryTool())
        executor.register(DeleteFileTool())
        executor.register(FileExistsTool())

        // App tools
        executor.register(ListAppsTool())
        executor.register(GetAppInfoTool())
        executor.register(LaunchAppTool())

        // System tools
        executor.register(GetDeviceInfoTool())
        executor.register(GetBatteryStatusTool())

        // Permission tools
        executor.register(CheckPermissionsTool())
    }

    fun listTools(): List<String> = executor.listTools()

    suspend fun execute(toolName: String, params: Map<String, Any?>): ToolResult {
        return executor.execute(context, toolName, params)
    }

    fun executeJson(toolName: String, paramsJson: String): String {
        val params = try {
            JSONObject(paramsJson).toMap()
        } catch (e: Exception) {
            return ToolResult.failure(
                com.androidagent.tools.core.ToolError.INVALID_PARAMETER,
                "Invalid JSON: ${e.message}"
            ).toJson().toString()
        }

        val result = runBlocking {
            executor.execute(context, toolName, params)
        }

        return result.toJson().toString()
    }

    companion object {
        const val VERSION = "1.0.0"
    }
}

private fun JSONObject.toMap(): Map<String, Any?> {
    val map = mutableMapOf<String, Any?>()
    val keys = this.keys()
    while (keys.hasNext()) {
        val key = keys.next()
        map[key] = when (val value = this.get(key)) {
            JSONObject.NULL -> null
            is JSONObject -> value.toMap()
            is org.json.JSONArray -> value.toList()
            else -> value
        }
    }
    return map
}

private fun org.json.JSONArray.toList(): List<Any?> {
    return (0 until length()).map { i ->
        when (val value = this.get(i)) {
            JSONObject.NULL -> null
            is JSONObject -> value.toMap()
            is org.json.JSONArray -> value.toList()
            else -> value
        }
    }
}
```

- [x] **Step 4: Run all tests**

```bash
cd sdk && ./gradlew test
```
Expected: All PASS

- [x] **Step 5: Commit**

```bash
git add sdk/src/main/java/com/androidagent/tools/executor/*.kt
git add sdk/src/main/java/com/androidagent/tools/AndroidAgentTools.kt
git add sdk/src/test/java/com/androidagent/tools/executor/*.kt
git commit -m "feat: add ToolExecutor and AndroidAgentTools entry point"
```

---

## Task 11: Final Integration and Documentation

**Files:**
- Modify: `README.md`
- Create: `sdk/README.md`

- [x] **Step 1: Update root README.md**

```markdown
# Android Agent Tools

A standardized toolkit for LLM agents to interact with Android devices.

## Overview

This project provides a set of tools that enable AI agents to operate Android devices, similar to how desktop agents use `read_file`/`write_file` to interact with computer systems.

## Quick Start

```kotlin
// Initialize
val tools = AndroidAgentTools(context)

// List available tools
val tools = tools.listTools()

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

## License

MIT
```

- [x] **Step 2: Create SDK README**

```markdown
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
```

- [x] **Step 3: Commit**

```bash
git add README.md sdk/README.md
git commit -m "docs: update README with SDK usage guide"
```

---

## Self-Review Checklist

- [x] **Spec coverage**: All 11 tools from design.md are implemented
- [x] **No placeholders**: All code is complete, no TBD/TODO
- [x] **Type consistency**: Tool names, parameter names match design spec
- [x] **Error codes**: All error codes from spec implemented
- [x] **Return format**: Success/Failure structure matches spec