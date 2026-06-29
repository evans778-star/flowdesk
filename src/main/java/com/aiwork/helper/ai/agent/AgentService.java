package com.aiwork.helper.ai.agent;

import com.aiwork.helper.ai.tools.ChatTools;
import com.aiwork.helper.ai.tools.TodoTools;
import com.aiwork.helper.config.DashScopeAiEnabledCondition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Agent核心服务
 * 使用Spring AI的ChatClient + Function Calling实现Agent循环机制
 * 集成SummaryBufferChatMemory实现会话摘要缓冲记忆
 *
 * 工作流程:
 * 1. 用户输入 -> ChatClient接收
 * 2. MessageChatMemoryAdvisor自动加载历史对话（包含摘要）
 * 3. LLM分析用户意图，决定调用哪些Tool
 * 4. Tool执行并返回结果
 * 5. LLM继续推理，可能调用更多Tool
 * 6. 生成最终答案返回用户
 * 7. 对话自动保存到Memory，Token超限时生成摘要
 */
@Slf4j
@Service
@Conditional(DashScopeAiEnabledCondition.class)
@RequiredArgsConstructor
public class AgentService {

    private final ChatClient chatClient;

    /**
     * Agent对话入口
     *
     * @param userId 用户ID（同时作为会话ID，每个用户有独立的对话记忆）
     * @param userInput 用户输入
     * @param relationId 关联ID（群聊ID等）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return AI响应
     */
    public String chat(String userId, String userInput, String relationId, Long startTime, Long endTime) {
        log.info("AgentService处理请求: userId={}, input={}, relationId={}, startTime={}, endTime={}",
                userId, userInput, relationId, startTime, endTime);

        try {
            // 设置当前用户ID到ThreadLocal，供Tool使用
            TodoTools.setCurrentUserId(userId);

            // 设置聊天上下文到ThreadLocal，供ChatTools使用
            ChatTools.setChatContext(relationId, startTime, endTime);

            // 构建系统提示词
            String systemPrompt = buildSystemPrompt();

            // 使用ChatClient进行对话
            // 通过advisors参数传递conversationId，使用userId作为会话标识
            // 这样每个用户有独立的对话记忆，MessageChatMemoryAdvisor会自动：
            // 1. 加载该用户之前的对话历史（包含摘要）
            // 2. 将新对话保存到Memory
            // 3. Token超限时自动生成摘要
            String response = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userInput)
                    .advisors(advisorSpec -> advisorSpec
                            .param("chat_memory_conversation_id", userId))
                    .call()
                    .content();

            log.info("Agent响应成功: userId={}, responseLength={}",
                    userId, response != null ? response.length() : 0);

            return response;

        } catch (Exception e) {
            log.error("Agent处理失败: userId={}, input={}", userId, userInput, e);
            return "抱歉，处理请求时发生错误: " + e.getMessage();
        } finally {
            // 清理ThreadLocal
            TodoTools.clearCurrentUserId();
            ChatTools.clearChatContext();
        }
    }

    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalDate tomorrow = today.plusDays(1);

        long nowTimestamp = now.atZone(ZoneId.systemDefault()).toEpochSecond();
        long tomorrowMidnight = tomorrow.atStartOfDay().atZone(ZoneId.systemDefault()).toEpochSecond();

        return String.format("""
                你是一个智能工作助手，可以帮助用户处理各种工作事务。

                ## 当前时间信息
                - 当前时间: %s
                - 当前时间戳: %d
                - 明天00:00时间戳: %d

                ## 时间计算规则
                计算具体时间的公式: 明天00:00时间戳 + (小时 * 3600) + (分钟 * 60)
                示例:
                - 明天上午9点 = %d + (9 * 3600) = %d
                - 明天下午2点 = %d + (14 * 3600) = %d
                - 明天下午2点30分 = %d + (14 * 3600) + (30 * 60) = %d

                ## 你可以使用的工具
                你有以下工具可以调用，请根据用户需求选择合适的工具：

                ### 待办事项相关
                - createTodo: 创建待办事项
                - findTodos: 查询待办列表

                ### 审批相关
                - createLeaveApproval: 创建请假审批
                - createPunchApproval: 创建补卡审批
                - createGoOutApproval: 创建外出审批
                - findApprovals: 查询审批记录

                ### 知识库相关
                - queryKnowledge: 从知识库查询答案（公司制度、规章、政策等）
                - updateKnowledge: 更新知识库（使用上传的PDF文件）
                - clearKnowledge: 清空知识库

                ### 文件相关
                - getRecentUploadedFiles: 获取用户最近上传的文件列表
                - getLatestUploadedFilePath: 获取最近上传的一个文件路径（用于更新知识库）

                ### 聊天记录相关
                - getChatLogs: 获取当前会话的聊天记录
                - summarizeChatLogs: 总结群聊消息内容（当用户要求总结群聊时使用此工具）

                ### 辅助工具
                - parseTime: 将自然语言时间转换为Unix时间戳
                - getCurrentTime: 获取当前时间信息
                - getUserIdByName: 根据用户名查询用户ID

                ## 工作流程
                1. 分析用户的请求意图
                2. 如果需要时间转换，先调用parseTime获取准确的时间戳
                3. 如果需要用户ID，先调用getUserIdByName获取
                4. 调用相应的业务工具完成任务
                5. 将执行结果以友好的方式告知用户

                ## 特殊场景处理
                - 当用户要求"总结群聊消息"或"总结聊天记录"时，调用summarizeChatLogs工具获取聊天记录，然后进行总结分析
                - 总结时需要提取出重要事项，如待办任务、审批事项等
                - 当用户要求"根据我上传的文件更新知识库"时，先调用getLatestUploadedFilePath获取最近上传的文件路径，然后调用updateKnowledge更新知识库
                - 如果updateKnowledge没有传入文件路径，它会自动尝试获取最近上传的文件

                ## 注意事项
                - 时间戳必须是纯数字，不要使用表达式
                - 请假审批的leaveType: 1=事假, 2=调休, 3=病假, 4=年假, 5=产假, 6=陪产假, 7=婚假, 8=丧假, 9=哺乳假
                - 如果用户提到"感冒"、"生病"等，请假类型应为病假(3)
                - 如果用户没有指定待办执行人，默认为当前用户
                - 回复时使用中文，保持友好和专业
                """,
                now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                nowTimestamp,
                tomorrowMidnight,
                tomorrowMidnight, tomorrowMidnight + 9 * 3600,
                tomorrowMidnight, tomorrowMidnight + 14 * 3600,
                tomorrowMidnight, tomorrowMidnight + 14 * 3600 + 30 * 60);
    }
}
