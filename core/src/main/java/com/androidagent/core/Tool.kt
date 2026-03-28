package com.androidagent.core

/**
 * 工具接口
 *
 * 所有工具必须实现的统一接口，定义工具的基本结构和执行逻辑。
 */
interface Tool {
    /**
     * 工具名称，使用 snake_case 格式
     */
    val name: String

    /**
     * 工具描述，简要说明工具的功能
     */
    val description: String

    /**
     * 输入参数 Schema，描述工具接受的参数结构
     */
    val inputSchema: ToolSchema

    /**
     * 验证参数是否符合工具要求
     *
     * @param params 参数映射
     * @return 验证结果，成功或失败
     */
    fun validate(params: Map<String, Any?>): ValidationResult

    /**
     * 执行工具逻辑
     *
     * @param context 执行上下文，提供设备能力和环境信息
     * @param params 参数映射
     * @return 执行结果
     */
    suspend fun execute(context: ToolContext, params: Map<String, Any?>): ToolResult
}