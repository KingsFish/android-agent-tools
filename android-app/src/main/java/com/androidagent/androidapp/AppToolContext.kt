package com.androidagent.androidapp

import android.content.Context
import com.androidagent.core.ToolContext
import com.androidagent.tools.accessibility.AgentAccessibilityService

/**
 * Android App 工具执行上下文
 */
class AppToolContext(
    private val androidContext: Context,
    private val accessibilityService: AgentAccessibilityService? = null
) : ToolContext {

    private val environmentDetector = EnvironmentDetector(androidContext)

    override fun hasCapability(capability: com.androidagent.core.Capability): Boolean {
        return when (capability) {
            is AndroidCapability -> environmentDetector.hasCapability(capability)
            else -> false
        }
    }

    fun getAndroidContext(): Context = androidContext

    fun getAccessibilityService(): AgentAccessibilityService? = accessibilityService

    fun getEnvironmentDetector(): EnvironmentDetector = environmentDetector

    companion object {
        fun create(androidContext: Context): AppToolContext {
            return AppToolContext(
                androidContext,
                AgentAccessibilityService.instance
            )
        }
    }
}
