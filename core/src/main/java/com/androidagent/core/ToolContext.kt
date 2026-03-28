package com.androidagent.core

/**
 * 工具执行上下文接口
 *
 * 抽象的执行上下文，不同实现层（Android App、ADB、云端桥接）提供不同的上下文实现。
 */
interface ToolContext {
    /**
     * 检查设备是否具备指定能力
     */
    fun hasCapability(capability: Capability): Boolean
}