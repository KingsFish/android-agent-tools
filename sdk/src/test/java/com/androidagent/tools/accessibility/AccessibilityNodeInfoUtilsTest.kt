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
        assertEquals("com.example:id/text", result["resource_id"])
        assertEquals(true, result["clickable"])
        assertEquals(false, result["scrollable"])
        assertEquals(true, result["enabled"])
        assertEquals(true, result["visible"])
        assertEquals("android.widget.TextView", result["class"])
        assertEquals(emptyList<Map<String, Any?>>(), result["children"])
    }

    @Test
    fun `nodeToMap skips invisible nodes when includeInvisible is false`() {
        val mockNode = mockk<AccessibilityNodeInfo>(relaxed = true)
        every { mockNode.isVisibleToUser } returns false
        every { mockNode.childCount } returns 0

        val result = AccessibilityNodeInfoUtils.nodeToMap(mockNode, includeInvisible = false)
        assertNull(result)
    }

    @Test
    fun `nodeToMap includes invisible nodes when includeInvisible is true`() {
        val mockNode = mockk<AccessibilityNodeInfo>(relaxed = true)
        val bounds = Rect(0, 0, 100, 200)

        every { mockNode.getBoundsInScreen(any()) } answers {
            (it.invocation.args[0] as Rect).set(bounds)
        }
        every { mockNode.isVisibleToUser } returns false
        every { mockNode.text } returns null
        every { mockNode.viewIdResourceName } returns null
        every { mockNode.isClickable } returns false
        every { mockNode.isScrollable } returns false
        every { mockNode.isEnabled } returns true
        every { mockNode.className } returns null
        every { mockNode.childCount } returns 0

        val result = AccessibilityNodeInfoUtils.nodeToMap(mockNode, includeInvisible = true)

        assertNotNull(result)
        assertEquals(false, result!!["visible"])
    }

    @Test
    fun `nodeToMap returns null when maxDepth is negative`() {
        val mockNode = mockk<AccessibilityNodeInfo>(relaxed = true)
        every { mockNode.isVisibleToUser } returns true

        val result = AccessibilityNodeInfoUtils.nodeToMap(mockNode, maxDepth = -1)
        assertNull(result)
    }

    @Test
    fun `nodeToMap handles nodes with children`() {
        val mockParent = mockk<AccessibilityNodeInfo>(relaxed = true)
        val mockChild = mockk<AccessibilityNodeInfo>(relaxed = true)

        val parentBounds = Rect(0, 0, 200, 400)
        val childBounds = Rect(10, 10, 100, 50)

        every { mockParent.getBoundsInScreen(any()) } answers {
            (it.invocation.args[0] as Rect).set(parentBounds)
        }
        every { mockParent.isVisibleToUser } returns true
        every { mockParent.text } returns "Parent"
        every { mockParent.viewIdResourceName } returns "com.example:id/parent"
        every { mockParent.isClickable } returns false
        every { mockParent.isScrollable } returns true
        every { mockParent.isEnabled } returns true
        every { mockParent.className } returns "android.widget.ScrollView"
        every { mockParent.childCount } returns 1
        every { mockParent.getChild(0) } returns mockChild

        every { mockChild.getBoundsInScreen(any()) } answers {
            (it.invocation.args[0] as Rect).set(childBounds)
        }
        every { mockChild.isVisibleToUser } returns true
        every { mockChild.text } returns "Child"
        every { mockChild.viewIdResourceName } returns "com.example:id/child"
        every { mockChild.isClickable } returns true
        every { mockChild.isScrollable } returns false
        every { mockChild.isEnabled } returns true
        every { mockChild.className } returns "android.widget.Button"
        every { mockChild.childCount } returns 0

        val result = AccessibilityNodeInfoUtils.nodeToMap(mockParent)!!

        assertEquals("node_0", result["node_id"])
        @Suppress("UNCHECKED_CAST")
        val children = result["children"] as List<Map<String, Any?>>
        assertEquals(1, children.size)
        assertEquals("node_1", children[0]["node_id"])
        assertEquals("Child", children[0]["text"])
    }

    @Test
    fun `nodeToMap handles null text and resourceId`() {
        val mockNode = mockk<AccessibilityNodeInfo>(relaxed = true)
        val bounds = Rect(0, 0, 100, 200)

        every { mockNode.getBoundsInScreen(any()) } answers {
            (it.invocation.args[0] as Rect).set(bounds)
        }
        every { mockNode.text } returns null
        every { mockNode.viewIdResourceName } returns null
        every { mockNode.isClickable } returns false
        every { mockNode.isScrollable } returns false
        every { mockNode.isEnabled } returns true
        every { mockNode.isVisibleToUser } returns true
        every { mockNode.className } returns null
        every { mockNode.childCount } returns 0

        val result = AccessibilityNodeInfoUtils.nodeToMap(mockNode)!!

        assertEquals("", result["text"])
        assertEquals("", result["resource_id"])
        assertEquals("", result["class"])
    }

    @Test
    fun `countNodes returns correct count for visible nodes`() {
        val mockParent = mockk<AccessibilityNodeInfo>(relaxed = true)
        val mockChild1 = mockk<AccessibilityNodeInfo>(relaxed = true)
        val mockChild2 = mockk<AccessibilityNodeInfo>(relaxed = true)

        every { mockParent.isVisibleToUser } returns true
        every { mockParent.childCount } returns 2
        every { mockParent.getChild(0) } returns mockChild1
        every { mockParent.getChild(1) } returns mockChild2

        every { mockChild1.isVisibleToUser } returns true
        every { mockChild1.childCount } returns 0

        every { mockChild2.isVisibleToUser } returns true
        every { mockChild2.childCount } returns 0

        val count = AccessibilityNodeInfoUtils.countNodes(mockParent)
        assertEquals(3, count)
    }

    @Test
    fun `countNodes excludes invisible nodes by default`() {
        val mockParent = mockk<AccessibilityNodeInfo>(relaxed = true)
        val mockVisibleChild = mockk<AccessibilityNodeInfo>(relaxed = true)
        val mockInvisibleChild = mockk<AccessibilityNodeInfo>(relaxed = true)

        every { mockParent.isVisibleToUser } returns true
        every { mockParent.childCount } returns 2
        every { mockParent.getChild(0) } returns mockVisibleChild
        every { mockParent.getChild(1) } returns mockInvisibleChild

        every { mockVisibleChild.isVisibleToUser } returns true
        every { mockVisibleChild.childCount } returns 0

        every { mockInvisibleChild.isVisibleToUser } returns false
        every { mockInvisibleChild.childCount } returns 0

        val count = AccessibilityNodeInfoUtils.countNodes(mockParent)
        assertEquals(2, count)
    }

    @Test
    fun `countNodes includes invisible nodes when flag is set`() {
        val mockParent = mockk<AccessibilityNodeInfo>(relaxed = true)
        val mockVisibleChild = mockk<AccessibilityNodeInfo>(relaxed = true)
        val mockInvisibleChild = mockk<AccessibilityNodeInfo>(relaxed = true)

        every { mockParent.isVisibleToUser } returns true
        every { mockParent.childCount } returns 2
        every { mockParent.getChild(0) } returns mockVisibleChild
        every { mockParent.getChild(1) } returns mockInvisibleChild

        every { mockVisibleChild.isVisibleToUser } returns true
        every { mockVisibleChild.childCount } returns 0

        every { mockInvisibleChild.isVisibleToUser } returns false
        every { mockInvisibleChild.childCount } returns 0

        val count = AccessibilityNodeInfoUtils.countNodes(mockParent, includeInvisible = true)
        assertEquals(3, count)
    }

    @Test
    fun `findNodeByResourceId finds matching node`() {
        val mockRoot = mockk<AccessibilityNodeInfo>(relaxed = true)
        val mockChild = mockk<AccessibilityNodeInfo>(relaxed = true)

        every { mockRoot.viewIdResourceName } returns "com.example:id/root"
        every { mockRoot.childCount } returns 1
        every { mockRoot.getChild(0) } returns mockChild

        every { mockChild.viewIdResourceName } returns "com.example:id/target"
        every { mockChild.childCount } returns 0

        val result = AccessibilityNodeInfoUtils.findNodeByResourceId(mockRoot, "com.example:id/target")
        assertSame(mockChild, result)
    }

    @Test
    fun `findNodeByResourceId returns null when not found`() {
        val mockNode = mockk<AccessibilityNodeInfo>(relaxed = true)

        every { mockNode.viewIdResourceName } returns "com.example:id/other"
        every { mockNode.childCount } returns 0

        val result = AccessibilityNodeInfoUtils.findNodeByResourceId(mockNode, "com.example:id/target")
        assertNull(result)
    }

    @Test
    fun `findNodesByText returns matching nodes`() {
        val mockRoot = mockk<AccessibilityNodeInfo>(relaxed = true)
        val mockChild1 = mockk<AccessibilityNodeInfo>(relaxed = true)
        val mockChild2 = mockk<AccessibilityNodeInfo>(relaxed = true)

        every { mockRoot.isVisibleToUser } returns true
        every { mockRoot.text } returns "Hello World"
        every { mockRoot.childCount } returns 2
        every { mockRoot.getChild(0) } returns mockChild1
        every { mockRoot.getChild(1) } returns mockChild2

        every { mockChild1.isVisibleToUser } returns true
        every { mockChild1.text } returns "Hello Button"
        every { mockChild1.childCount } returns 0

        every { mockChild2.isVisibleToUser } returns true
        every { mockChild2.text } returns "Goodbye"
        every { mockChild2.childCount } returns 0

        val result = AccessibilityNodeInfoUtils.findNodesByText(mockRoot, "Hello")

        assertEquals(2, result.size)
        assertTrue(result.contains(mockRoot))
        assertTrue(result.contains(mockChild1))
    }

    @Test
    fun `findNodesByText is case insensitive`() {
        val mockNode = mockk<AccessibilityNodeInfo>(relaxed = true)

        every { mockNode.isVisibleToUser } returns true
        every { mockNode.text } returns "HELLO WORLD"
        every { mockNode.childCount } returns 0

        val result = AccessibilityNodeInfoUtils.findNodesByText(mockNode, "hello")

        assertEquals(1, result.size)
        assertSame(mockNode, result[0])
    }

    @Test
    fun `findClickableNodes returns only clickable nodes`() {
        val mockRoot = mockk<AccessibilityNodeInfo>(relaxed = true)
        val mockClickableChild = mockk<AccessibilityNodeInfo>(relaxed = true)
        val mockNonClickableChild = mockk<AccessibilityNodeInfo>(relaxed = true)

        every { mockRoot.isVisibleToUser } returns true
        every { mockRoot.isClickable } returns false
        every { mockRoot.childCount } returns 2
        every { mockRoot.getChild(0) } returns mockClickableChild
        every { mockRoot.getChild(1) } returns mockNonClickableChild

        every { mockClickableChild.isVisibleToUser } returns true
        every { mockClickableChild.isClickable } returns true
        every { mockClickableChild.childCount } returns 0

        every { mockNonClickableChild.isVisibleToUser } returns true
        every { mockNonClickableChild.isClickable } returns false
        every { mockNonClickableChild.childCount } returns 0

        val result = AccessibilityNodeInfoUtils.findClickableNodes(mockRoot)

        assertEquals(1, result.size)
        assertSame(mockClickableChild, result[0])
    }

    @Test
    fun `getBoundsString returns formatted bounds`() {
        val mockNode = mockk<AccessibilityNodeInfo>(relaxed = true)

        every { mockNode.getBoundsInScreen(any()) } answers {
            (it.invocation.args[0] as Rect).set(10, 20, 100, 200)
        }

        val result = AccessibilityNodeInfoUtils.getBoundsString(mockNode)
        assertEquals("[10,20][100,200]", result)
    }
}