package com.aiwork.helper.ai.tools;

import com.aiwork.helper.config.DashScopeAiEnabledCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 时间解析工具
 * 将自然语言时间转换为Unix时间戳
 */
@Slf4j
@Component
@Conditional(DashScopeAiEnabledCondition.class)
public class TimeParserTool {

    @Tool(description = "将自然语言时间转换为Unix时间戳(秒)。支持的格式：'明天上午9点'、'后天下午2点'、'今天晚上8点'、'明天一天'(返回明天00:00的时间戳)、'2024-01-15 09:00'等。")
    public Long parseTime(
            @ToolParam(description = "自然语言时间描述，如'明天上午9点'、'后天下午2点30分'、'今天'、'明天'") String timeText
    ) {
        log.info("Tool调用 - parseTime: timeText={}", timeText);

        if (timeText == null || timeText.trim().isEmpty()) {
            return null;
        }

        try {
            LocalDateTime result = parseTimeText(timeText.trim());
            if (result != null) {
                long timestamp = result.atZone(ZoneId.systemDefault()).toEpochSecond();
                log.info("时间解析成功: {} -> {} ({})", timeText, timestamp,
                        result.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                return timestamp;
            }
        } catch (Exception e) {
            log.warn("时间解析失败: {}, error={}", timeText, e.getMessage());
        }

        // 返回null表示无法解析
        log.warn("无法解析时间: {}", timeText);
        return null;
    }

    @Tool(description = "获取当前时间戳(秒)和格式化的日期时间字符串，用于帮助理解时间上下文。")
    public String getCurrentTime() {
        LocalDateTime now = LocalDateTime.now();
        long timestamp = now.atZone(ZoneId.systemDefault()).toEpochSecond();

        LocalDateTime tomorrow = now.plusDays(1).toLocalDate().atStartOfDay();
        long tomorrowTs = tomorrow.atZone(ZoneId.systemDefault()).toEpochSecond();

        return String.format("当前时间: %s\n当前时间戳: %d\n明天00:00时间戳: %d\n提示: 明天上午9点 = %d, 明天下午2点 = %d",
                now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                timestamp,
                tomorrowTs,
                tomorrowTs + 9 * 3600,
                tomorrowTs + 14 * 3600);
    }

    /**
     * 解析时间文本
     */
    private LocalDateTime parseTimeText(String timeText) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // 尝试解析标准格式
        try {
            // 尝试解析 yyyy-MM-dd HH:mm 格式
            if (timeText.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}")) {
                return LocalDateTime.parse(timeText, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            }
            // 尝试解析 yyyy-MM-dd HH:mm:ss 格式
            if (timeText.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
                return LocalDateTime.parse(timeText, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
        } catch (Exception e) {
            // 继续尝试其他格式
        }

        // 解析相对日期
        LocalDate targetDate = today;

        if (timeText.contains("今天") || timeText.contains("今日")) {
            targetDate = today;
        } else if (timeText.contains("明天") || timeText.contains("明日")) {
            targetDate = today.plusDays(1);
        } else if (timeText.contains("后天")) {
            targetDate = today.plusDays(2);
        } else if (timeText.contains("大后天")) {
            targetDate = today.plusDays(3);
        } else if (timeText.contains("下周一")) {
            targetDate = today.plusWeeks(1).with(java.time.DayOfWeek.MONDAY);
        } else if (timeText.contains("下周")) {
            targetDate = today.plusWeeks(1);
        }

        // 如果只有日期没有时间（如"明天"、"明天一天"）
        if (timeText.matches(".*(今天|明天|后天|大后天)$") ||
                timeText.matches(".*(今天|明天|后天|大后天)一天.*")) {
            return targetDate.atStartOfDay();
        }

        // 解析时间部分
        LocalTime time = parseTimePart(timeText);
        if (time != null) {
            return targetDate.atTime(time);
        }

        // 如果有日期但没有具体时间，返回当天开始
        if (!targetDate.equals(today)) {
            return targetDate.atStartOfDay();
        }

        return null;
    }

    /**
     * 解析时间部分（上午/下午 + 小时 + 分钟）
     */
    private LocalTime parseTimePart(String timeText) {
        int hour = -1;
        int minute = 0;

        // 匹配 "上午9点"、"下午2点"、"晚上8点"、"9点30分" 等格式
        Pattern pattern = Pattern.compile("(上午|早上|早晨|凌晨|中午|下午|晚上|傍晚)?(\\d{1,2})[点时](\\d{1,2})?[分]?");
        Matcher matcher = pattern.matcher(timeText);

        if (matcher.find()) {
            String period = matcher.group(1);
            hour = Integer.parseInt(matcher.group(2));
            if (matcher.group(3) != null) {
                minute = Integer.parseInt(matcher.group(3));
            }

            // 根据上午/下午调整小时
            if (period != null) {
                if ((period.equals("下午") || period.equals("晚上") || period.equals("傍晚")) && hour < 12) {
                    hour += 12;
                } else if ((period.equals("上午") || period.equals("早上") || period.equals("早晨")) && hour == 12) {
                    hour = 0;
                } else if (period.equals("凌晨") && hour >= 12) {
                    hour -= 12;
                } else if (period.equals("中午") && hour == 12) {
                    // 中午12点保持不变
                }
            }

            return LocalTime.of(hour, minute);
        }

        // 匹配 "9:30"、"14:00" 等格式
        Pattern pattern2 = Pattern.compile("(\\d{1,2}):(\\d{2})");
        Matcher matcher2 = pattern2.matcher(timeText);

        if (matcher2.find()) {
            hour = Integer.parseInt(matcher2.group(1));
            minute = Integer.parseInt(matcher2.group(2));
            return LocalTime.of(hour, minute);
        }

        return null;
    }
}
