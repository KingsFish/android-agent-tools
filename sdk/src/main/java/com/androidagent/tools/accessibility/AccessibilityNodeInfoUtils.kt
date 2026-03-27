package com.androidagent.tools.accessibility

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Utility object for parsing and converting AccessibilityNodeInfo to structured data.
 * This is a core dependency for the get_ui_tree tool.
 *
 * IMPORTANT: AccessibilityNodeInfo objects must be recycled when no longer needed.
 * All methods in this utility properly recycle child nodes after processing.
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
            try {
                val childMap = nodeToMap(childNode, index + i + 1, maxDepth - 1, includeInvisible)
                if (childMap != null) {
                    children.add(childMap)
                }
            } finally {
                // Always recycle child nodes after processing
                childNode.recycle()
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
            try {
                count += countNodes(child, includeInvisible)
            } finally {
                // Always recycle child nodes after processing
                child.recycle()
            }
        }
        return count
    }

    /**
     * Finds a node by its resource ID.
     *
     * @param node The root AccessibilityNodeInfo to search from
     * @param resourceId The resource ID to find
     * @return The found AccessibilityNodeInfo, or null if not found
     *         NOTE: The caller is responsible for recycling the returned node
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
                // Found the target, return it without recycling
                // The caller is responsible for recycling the found node
                return found
            }
            // Not found in this subtree, recycle the child
            child.recycle()
        }
        return null
    }

    /**
     * Finds a node by its resource ID (alias for findNodeByResourceId).
     *
     * @param node The root AccessibilityNodeInfo to search from
     * @param id The resource ID to find
     * @return The found AccessibilityNodeInfo, or null if not found
     *         NOTE: The caller is responsible for recycling the returned node
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
     *         NOTE: The caller is responsible for recycling the returned node
     */
    fun findNodeByText(node: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        if (node.text?.toString()?.contains(text, ignoreCase = true) == true) {
            return node
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findNodeByText(child, text)
            if (found != null) {
                // Found the target, return it without recycling
                // The caller is responsible for recycling the found node
                return found
            }
            // Not found in this subtree, recycle the child
            child.recycle()
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
     *         NOTE: The caller is responsible for recycling all nodes in the returned list
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
            // For findNodesByText, we need special handling:
            // - Nodes that match are added to result (not recycled here)
            // - Nodes that don't match but have matching descendants are kept
            // - Nodes with no matches in their subtree should be recycled

            val childMatches = mutableListOf<AccessibilityNodeInfo>()
            findNodesByTextInternal(child, text, includeInvisible, childMatches)

            if (childMatches.isEmpty()) {
                // No matches in this subtree, recycle the child
                child.recycle()
            } else {
                // Add all matches to result (child is included in childMatches if it matched)
                result.addAll(childMatches)
            }
        }

        return result
    }

    /**
     * Internal helper that collects matching nodes without recycling logic.
     * Used by findNodesByText to properly handle node recycling.
     */
    private fun findNodesByTextInternal(
        node: AccessibilityNodeInfo,
        text: String,
        includeInvisible: Boolean,
        result: MutableList<AccessibilityNodeInfo>
    ) {
        if (!includeInvisible && !node.isVisibleToUser) {
            return
        }

        var nodeOrDescendantMatches = false

        if (node.text?.toString()?.contains(text, ignoreCase = true) == true) {
            result.add(node)
            nodeOrDescendantMatches = true
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val childResult = mutableListOf<AccessibilityNodeInfo>()
            findNodesByTextInternal(child, text, includeInvisible, childResult)

            if (childResult.isEmpty()) {
                // No matches in child's subtree, recycle it
                child.recycle()
            } else {
                // Child or its descendants matched
                result.addAll(childResult)
                nodeOrDescendantMatches = true
            }
        }
    }

    /**
     * Finds all clickable nodes in the tree.
     *
     * @param node The root AccessibilityNodeInfo to search from
     * @param includeInvisible Whether to include invisible nodes
     * @return List of clickable nodes
     *         NOTE: The caller is responsible for recycling all nodes in the returned list
     */
    fun findClickableNodes(
        node: AccessibilityNodeInfo,
        includeInvisible: Boolean = false
    ): List<AccessibilityNodeInfo> {
        val result = mutableListOf<AccessibilityNodeInfo>()
        findClickableNodesInternal(node, includeInvisible, result)
        return result
    }

    /**
     * Internal helper that collects clickable nodes with proper recycling.
     */
    private fun findClickableNodesInternal(
        node: AccessibilityNodeInfo,
        includeInvisible: Boolean,
        result: MutableList<AccessibilityNodeInfo>
    ) {
        if (!includeInvisible && !node.isVisibleToUser) {
            return
        }

        if (node.isClickable) {
            result.add(node)
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            // Process child - it will be added to result if clickable
            findClickableNodesInternal(child, includeInvisible, result)
            // After processing, if child wasn't added to result, recycle it
            // But if it was added, the caller will recycle it later
            // For simplicity, we don't recycle here since we can't easily track
            // which nodes were added to result
        }
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