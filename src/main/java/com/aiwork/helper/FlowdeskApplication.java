package com.aiwork.helper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Flowdesk 主应用类
 * AI智能办公助手系统 - 基于Spring Boot 3.x + Spring AI Alibaba
 *
 * 功能特性:
 * - 用户管理系统
 * - 待办事项管理
 * - 审批流程管理
 * - 部门组织管理
 * - WebSocket实时聊天
 * - AI智能助手 (集成阿里云通义千问)
 * - 知识库检索与更新
 */
@SpringBootApplication
@EnableAsync
public class FlowdeskApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlowdeskApplication.class, args);
        System.out.println("==============================================");
        System.out.println("Flowdesk 启动成功!");
        System.out.println("HTTP API 服务: http://localhost:8888");
        System.out.println("WebSocket 服务: ws://localhost:9000/ws");
        System.out.println("==============================================");
    }
}
