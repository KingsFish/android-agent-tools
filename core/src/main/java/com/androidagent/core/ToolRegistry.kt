package com.androidagent.core

/**
 * 工具注册接口
 *
 * 定义工具的注册和查找机制。
 */
interface ToolRegistry {
    /**
     * 注册工具
     */
    fun register(tool: Tool)

    /**
     * 获取工具
     */
    fun getTool(name: String): Tool?

    /**
     * 获取所有已注册的工具名称
     */
    fun listTools(): List<String>

    /**
     * 获取所有已注册的工具
     */
    fun getAllTools(): List<Tool>
}