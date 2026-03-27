package com.androidagent.tools.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.app.ActivityManager
import android.content.ClipData
import android.content.ClipboardManager
import android.graphics.Bitmap
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * 无障碍服务实现，提供 UI 交互能力
 */
class AgentAccessibilityService : AccessibilityService() {

    companion object {
        var instance: AgentAccessibilityService? = null
            private set

        fun isRunning(): Boolean = instance != null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: android.view.accessibility.AccessibilityEvent?) {
        // Not used for agent tools
    }

    override fun onInterrupt() {
        // Not used for agent tools
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    // === UI Interaction Operations ===

    /**
     * Perform tap at specified coordinates
     */
    suspend fun performTap(x: Int, y: Int): Boolean {
        val path = Path().apply {
            moveTo(x.toFloat(), y.toFloat())
        }

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 100))
            .build()

        return dispatchGesture(gesture)
    }

    /**
     * Perform swipe gesture
     */
    suspend fun performSwipe(startX: Int, startY: Int, endX: Int, endY: Int, duration: Long = 300): Boolean {
        val path = Path().apply {
            moveTo(startX.toFloat(), startY.toFloat())
            lineTo(endX.toFloat(), endY.toFloat())
        }

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
            .build()

        return dispatchGesture(gesture)
    }

    /**
     * Input text into focused field
     */
    fun performInputText(text: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val focusNode = rootNode.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)

        if (focusNode == null) {
            rootNode.recycle()
            return false
        }

        val result = try {
            // Try ACTION_SET_TEXT first
            val arguments = Bundle()
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            focusNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        } catch (e: Exception) {
            // Fallback to clipboard paste
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("text", text)
            clipboard.setPrimaryClip(clip)
            focusNode.performAction(AccessibilityNodeInfo.ACTION_PASTE)
        }

        focusNode.recycle()
        rootNode.recycle()
        return result
    }

    // === UI Tree Operations ===

    /**
     * Get UI tree of current screen
     */
    fun getUiTree(maxDepth: Int = 10, includeInvisible: Boolean = false): Map<String, Any?>? {
        val rootNode = rootInActiveWindow ?: return null
        val packageName = rootNode.packageName?.toString() ?: ""

        val nodes = mutableListOf<Map<String, Any?>>()
        for (i in 0 until rootNode.childCount) {
            val child = rootNode.getChild(i) ?: continue
            val nodeMap = AccessibilityNodeInfoUtils.nodeToMap(child, i, maxDepth, includeInvisible)
            if (nodeMap != null) {
                nodes.add(nodeMap)
            }
        }

        val nodeCount = AccessibilityNodeInfoUtils.countNodes(rootNode, includeInvisible)
        rootNode.recycle()

        return mapOf(
            "nodes" to nodes,
            "package_name" to packageName,
            "node_count" to nodeCount
        )
    }

    /**
     * Find node by text
     */
    fun findNodeByText(text: String): AccessibilityNodeInfo? {
        val rootNode = rootInActiveWindow ?: return null
        val result = AccessibilityNodeInfoUtils.findNodeByText(rootNode, text)
        if (result != rootNode) {
            rootNode.recycle()
        }
        return result
    }

    /**
     * Find node by resource ID
     */
    fun findNodeById(id: String): AccessibilityNodeInfo? {
        val rootNode = rootInActiveWindow ?: return null
        val result = AccessibilityNodeInfoUtils.findNodeById(rootNode, id)
        if (result != rootNode) {
            rootNode.recycle()
        }
        return result
    }

    // === Screenshot ===

    /**
     * Take screenshot (Android R+)
     */
    suspend fun takeScreenshot(): Bitmap? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return null
        }

        return suspendCancellableCoroutine { continuation ->
            takeScreenshot(
                android.view.Display.DEFAULT_DISPLAY,
                mainExecutor,
                object : TakeScreenshotCallback {
                    override fun onSuccess(screenshot: ScreenshotResult) {
                        val bitmap = Bitmap.wrapHardwareBuffer(
                            screenshot.hardwareBuffer,
                            screenshot.colorSpace
                        )?.copy(Bitmap.Config.ARGB_8888, false)
                        continuation.resume(bitmap)
                    }

                    override fun onFailure(error: Int) {
                        continuation.resume(null)
                    }
                }
            )
        }
    }

    // === Private Helpers ===

    private suspend fun dispatchGesture(gesture: GestureDescription): Boolean {
        return suspendCancellableCoroutine { continuation ->
            dispatchGesture(gesture, object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    continuation.resume(true)
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    continuation.resume(false)
                }
            }, null)
        }
    }

    // === Global Actions (Navigation) ===

    /**
     * Press the back button
     */
    fun pressBack(): Boolean = performGlobalAction(GLOBAL_ACTION_BACK)

    /**
     * Press the home button
     */
    fun pressHome(): Boolean = performGlobalAction(GLOBAL_ACTION_HOME)

    /**
     * Press the recents button
     */
    fun pressRecents(): Boolean = performGlobalAction(GLOBAL_ACTION_RECENTS)

    /**
     * Open notifications panel
     */
    fun openNotifications(): Boolean = performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)

    /**
     * Open quick settings panel
     */
    fun openQuickSettings(): Boolean = performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)

    /**
     * Open power dialog
     */
    fun openPowerDialog(): Boolean = performGlobalAction(GLOBAL_ACTION_POWER_DIALOG)

    /**
     * Lock the screen (Android P+)
     */
    fun lockScreen(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
        } else {
            false
        }
    }

    /**
     * Take screenshot using global action (Android P+)
     */
    fun takeScreenshotAction(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
        } else {
            false
        }
    }

    // === Advanced Gestures ===

    /**
     * Perform long press at specified coordinates
     */
    suspend fun performLongPress(x: Int, y: Int, duration: Long = 1000): Boolean {
        val path = Path().apply {
            moveTo(x.toFloat(), y.toFloat())
        }

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
            .build()

        return dispatchGesture(gesture)
    }

    /**
     * Perform drag gesture
     */
    suspend fun performDrag(
        fromX: Int, fromY: Int,
        toX: Int, toY: Int,
        duration: Long = 500
    ): Boolean {
        val path = Path().apply {
            moveTo(fromX.toFloat(), fromY.toFloat())
            lineTo(toX.toFloat(), toY.toFloat())
        }

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
            .build()

        return dispatchGesture(gesture)
    }

    // === Wait Operations ===

    /**
     * Wait for UI to become stable by comparing UI trees
     */
    suspend fun waitForUiStable(timeoutMs: Long = 5000, checkIntervalMs: Long = 500): Boolean {
        val startTime = System.currentTimeMillis()
        var prevTreeHash: Int? = null

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            val rootNode = rootInActiveWindow
            if (rootNode != null) {
                val currentHash = computeUiTreeHash(rootNode)
                rootNode.recycle()

                if (prevTreeHash != null && prevTreeHash == currentHash) {
                    return true
                }
                prevTreeHash = currentHash
            }
            delay(checkIntervalMs)
        }
        return false
    }

    /**
     * Wait for an element with specific text to appear
     */
    suspend fun waitForElement(text: String, timeoutMs: Long = 5000, checkIntervalMs: Long = 500): Boolean {
        val startTime = System.currentTimeMillis()

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            val rootNode = rootInActiveWindow
            if (rootNode != null) {
                val found = AccessibilityNodeInfoUtils.findNodeByText(rootNode, text)
                rootNode.recycle()
                if (found != null) {
                    found.recycle()
                    return true
                }
            }
            delay(checkIntervalMs)
        }
        return false
    }

    /**
     * Wait for an element with specific resource ID to appear
     */
    suspend fun waitForElementById(resourceId: String, timeoutMs: Long = 5000, checkIntervalMs: Long = 500): Boolean {
        val startTime = System.currentTimeMillis()

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            val rootNode = rootInActiveWindow
            if (rootNode != null) {
                val found = AccessibilityNodeInfoUtils.findNodeByResourceId(rootNode, resourceId)
                rootNode.recycle()
                if (found != null) {
                    found.recycle()
                    return true
                }
            }
            delay(checkIntervalMs)
        }
        return false
    }

    private fun computeUiTreeHash(node: AccessibilityNodeInfo): Int {
        val bounds = Rect()
        node.getBoundsInScreen(bounds)
        var hash = bounds.hashCode()
        hash = 31 * hash + (node.className?.hashCode() ?: 0)
        hash = 31 * hash + (node.text?.hashCode() ?: 0)
        hash = 31 * hash + (node.viewIdResourceName?.hashCode() ?: 0)
        hash = 31 * hash + node.childCount

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            hash = 31 * hash + computeUiTreeHash(child)
            child.recycle()
        }
        return hash
    }

    // === State Query ===

    /**
     * Get the current foreground app package name
     */
    fun getCurrentApp(): String? {
        val rootNode = rootInActiveWindow ?: return null
        val packageName = rootNode.packageName?.toString()
        rootNode.recycle()
        return packageName
    }

    /**
     * Check if an app is running
     */
    fun isAppRunning(packageName: String): Boolean {
        val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        return am.runningAppProcesses?.any { it.processName == packageName } ?: false
    }

    // === Clipboard Operations ===

    /**
     * Get clipboard content
     */
    fun getClipboard(): String? {
        return try {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.primaryClip?.getItemAt(0)?.text?.toString()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Set clipboard content
     */
    fun setClipboard(text: String): Boolean {
        return try {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("text", text)
            clipboard.setPrimaryClip(clip)
            true
        } catch (e: Exception) {
            false
        }
    }

    // === Node Interaction ===

    /**
     * Click a node by its text content
     */
    fun clickNodeByText(text: String, exact: Boolean = false): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val node = if (exact) {
            AccessibilityNodeInfoUtils.findNodeByTextExact(rootNode, text)
        } else {
            AccessibilityNodeInfoUtils.findNodeByText(rootNode, text)
        }

        if (node == null) {
            rootNode.recycle()
            return false
        }

        val result = performNodeClick(node)
        node.recycle()
        rootNode.recycle()
        return result
    }

    /**
     * Click a node by its resource ID
     */
    fun clickNodeById(resourceId: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val node = AccessibilityNodeInfoUtils.findNodeByResourceId(rootNode, resourceId)

        if (node == null) {
            rootNode.recycle()
            return false
        }

        val result = performNodeClick(node)
        node.recycle()
        rootNode.recycle()
        return result
    }

    /**
     * Perform click on a node, with fallback to gesture click
     */
    private fun performNodeClick(node: AccessibilityNodeInfo): Boolean {
        // Try ACTION_CLICK first
        if (node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
            return true
        }

        // Fallback: get bounds and perform gesture click
        val bounds = Rect()
        node.getBoundsInScreen(bounds)
        val centerX = (bounds.left + bounds.right) / 2
        val centerY = (bounds.top + bounds.bottom) / 2

        // We need to return synchronously here, so we'll use the gesture approach
        // The caller should handle this in a coroutine context
        return try {
            val path = Path().apply {
                moveTo(centerX.toFloat(), centerY.toFloat())
            }
            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, 100))
                .build()
            dispatchGestureSync(gesture)
        } catch (e: Exception) {
            false
        }
    }

    private fun dispatchGestureSync(gesture: GestureDescription): Boolean {
        var result = false
        val latch = java.util.concurrent.CountDownLatch(1)
        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                result = true
                latch.countDown()
            }
            override fun onCancelled(gestureDescription: GestureDescription?) {
                latch.countDown()
            }
        }, null)
        latch.await(5, java.util.concurrent.TimeUnit.SECONDS)
        return result
    }
}