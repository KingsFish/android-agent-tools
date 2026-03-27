package com.androidagent.tools.accessibility

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Utility object for parsing and converting AccessibilityNodeInfo to structured data.
 * This is a core dependency for the get_ui_tree tool.
 */
object AccessibilityNodeInfoUtils {

    /**
     * Converts an AccessibilityNodeInfo to a Map structure for JSON serialization.
     *
     * @param node The AccessibilityNodeInfo to convert
     * @param index The index for generating node IDs
     * @param maxDepth Maximum depth to traverse (default 10)
     * @param includeInvisible Whether to include invisible nodes (default false)
     * @return A Map representation of the node, or null if the node should be skipped
     */
    fun nodeToMap(
        node: AccessibilityNodeInfo,
        index: Int = 0,
        maxDepth: Int = 10,
        includeInvisible: Boolean = false
    ): Map<String, Any?>? {
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

    /**
     * Counts the total number of nodes in the accessibility tree.
     *
     * @param node The root AccessibilityNodeInfo
     * @param includeInvisible Whether to include invisible nodes (default false)
     * @return The total count of nodes
     */
    fun countNodes(node: AccessibilityNodeInfo, includeInvisible: Boolean = false): Int {
        if (!includeInvisible && !node.isVisibleToUser) return 0
        var count = 1
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            count += countNodes(child, includeInvisible)
        }
        return count
    }

    /**
     * Finds a node by its resource ID.
     *
     * @param node The root AccessibilityNodeInfo to search from
     * @param resourceId The resource ID to find
     * @return The found AccessibilityNodeInfo, or null if not found
     */
    fun findNodeByResourceId(
        node: AccessibilityNodeInfo,
        resourceId: String
    ): AccessibilityNodeInfo? {
        if (node.viewIdResourceName == resourceId) {
            return node
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findNodeByResourceId(child, resourceId)
            if (found != null) {
                return found
            }
        }
        return null
    }

    /**
     * Finds a node by its resource ID (alias for findNodeByResourceId).
     *
     * @param node The root AccessibilityNodeInfo to search from
     * @param id The resource ID to find
     * @return The found AccessibilityNodeInfo, or null if not found
     */
    fun findNodeById(node: AccessibilityNodeInfo, id: String): AccessibilityNodeInfo? {
        return findNodeByResourceId(node, id)
    }

    /**
     * Finds a node containing specific text.
     *
     * @param node The root AccessibilityNodeInfo to search from
     * @param text The text to search for
     * @return The found AccessibilityNodeInfo, or null if not found
     */
    fun findNodeByText(node: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        if (node.text?.toString()?.contains(text, ignoreCase = true) == true) {
            return node
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findNodeByText(child, text)
            if (found != null) {
                return found
            }
        }
        return null
    }

    /**
     * Finds nodes containing specific text.
     *
     * @param node The root AccessibilityNodeInfo to search from
     * @param text The text to search for
     * @param includeInvisible Whether to include invisible nodes
     * @return List of nodes containing the text
     */
    fun findNodesByText(
        node: AccessibilityNodeInfo,
        text: String,
        includeInvisible: Boolean = false
    ): List<AccessibilityNodeInfo> {
        val result = mutableListOf<AccessibilityNodeInfo>()

        if (!includeInvisible && !node.isVisibleToUser) {
            return result
        }

        if (node.text?.toString()?.contains(text, ignoreCase = true) == true) {
            result.add(node)
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            result.addAll(findNodesByText(child, text, includeInvisible))
        }

        return result
    }

    /**
     * Finds all clickable nodes in the tree.
     *
     * @param node The root AccessibilityNodeInfo to search from
     * @param includeInvisible Whether to include invisible nodes
     * @return List of clickable nodes
     */
    fun findClickableNodes(
        node: AccessibilityNodeInfo,
        includeInvisible: Boolean = false
    ): List<AccessibilityNodeInfo> {
        val result = mutableListOf<AccessibilityNodeInfo>()

        if (!includeInvisible && !node.isVisibleToUser) {
            return result
        }

        if (node.isClickable) {
            result.add(node)
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            result.addAll(findClickableNodes(child, includeInvisible))
        }

        return result
    }

    /**
     * Gets the bounds of a node as a formatted string.
     *
     * @param node The AccessibilityNodeInfo
     * @return A string representation of the bounds
     */
    fun getBoundsString(node: AccessibilityNodeInfo): String {
        val bounds = Rect()
        node.getBoundsInScreen(bounds)
        return "[${bounds.left},${bounds.top}][${bounds.right},${bounds.bottom}]"
    }
}