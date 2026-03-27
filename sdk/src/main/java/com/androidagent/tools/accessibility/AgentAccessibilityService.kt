package com.androidagent.tools.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.ClipData
import android.content.ClipboardManager
import android.graphics.Bitmap
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo
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
}