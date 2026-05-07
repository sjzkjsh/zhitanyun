package com.example.McpServices;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class McpSystemService {

    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter CHINESE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy年M月d日");

    /**
     * 获取当前系统日期 - 实时动态获取
     */
    @Tool(name = "get_current_date",
          description = """
          【获取当前系统日期】
          
          返回今天的日期，格式：yyyy-MM-dd。
          ⚠️ 此工具实时获取系统时间，每次调用返回最新日期。
          
          适用场景：
          - 确定"今天"的具体日期
          - 计算"昨天"、"明天"的日期
          - 验证时间相关查询的准确性
          
          返回示例：{"date": "2026-04-01", "chinese": "2026年4月1日", "weekday": "周二"}
          """)
    public String getCurrentDate() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        
        return String.format(
            "{\"date\": \"%s\", \"chinese\": \"%s\", \"weekday\": \"%s\"}",
            now.format(DATE_FORMATTER),
            now.format(CHINESE_FORMATTER),
            getWeekday(now)
        );
    }

    /**
     * 获取当前系统时间 - 完整时间戳
     */
    @Tool(name = "get_current_datetime",
          description = """
          【获取当前系统完整时间】
          
          返回当前精确时间，格式：yyyy-MM-dd HH:mm:ss。
          包含日期、时间、星期、时区信息。
          
          返回示例：{"datetime": "2026-04-01 09:14:32", "timezone": "Asia/Shanghai", "timestamp": "2026-04-01T09:14:32"}
          """)
    public String getCurrentDateTime() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        
        return String.format(
            "{\"datetime\": \"%s\", \"timezone\": \"%s\", \"timestamp\": \"%s\"}",
            now.format(TIME_FORMATTER),
            "Asia/Shanghai",
            now.toString()
        );
    }

    /**
     * 解析相对时间词为具体日期
     */
    @Tool(name = "parse_relative_time",
          description = """
          【解析相对时间为具体日期】
          
          将"今天"、"昨天"、"明天"、"上周"等相对时间词转换为具体日期。
          
          参数：
          - relativeTime: 相对时间词（如：今天、昨天、明天、上周、上月、今年）
          
          返回具体日期和说明。
          """)
    public String parseRelativeTime(
            @ToolParam(description = "相对时间词，如：今天、昨天、明天", required = true) 
            String relativeTime) {
        
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        LocalDateTime targetTime;
        String description;
        
        switch (relativeTime.trim()) {
            case "今天", "今日", "现在", "当前" -> {
                targetTime = now;
                description = "今天";
            }
            case "昨天", "昨日", "前一天" -> {
                targetTime = now.minusDays(1);
                description = "昨天";
            }
            case "明天", "明日", "第二天" -> {
                targetTime = now.plusDays(1);
                description = "明天";
            }
            case "前天" -> {
                targetTime = now.minusDays(2);
                description = "前天";
            }
            case "后天" -> {
                targetTime = now.plusDays(2);
                description = "后天";
            }
            case "上周", "上星期" -> {
                targetTime = now.minusWeeks(1);
                description = "上周";
            }
            case "本周", "这星期", "这个星期" -> {
                targetTime = now;
                description = "本周";
            }
            case "上月", "上个月" -> {
                targetTime = now.minusMonths(1);
                description = "上月";
            }
            case "本月", "这个月" -> {
                targetTime = now;
                description = "本月";
            }
            case "去年" -> {
                targetTime = now.minusYears(1);
                description = "去年";
            }
            case "今年" -> {
                targetTime = now;
                description = "今年";
            }
            default -> {
                return String.format(
                    "{\"error\": \"无法解析'%s'，支持：今天、昨天、明天、前天、后天、上周、本周、上月、本月、去年、今年\"}",
                    relativeTime
                );
            }
        }
        
        return String.format(
            "{\"relative\": \"%s\", \"date\": \"%s\", \"chinese\": \"%s\", \"reference\": \"当前时间%s\"}",
            relativeTime,
            targetTime.format(DATE_FORMATTER),
            targetTime.format(CHINESE_FORMATTER),
            now.format(CHINESE_FORMATTER)
        );
    }

    // ========== 私有方法 ==========
    
    private String getWeekday(LocalDateTime dateTime) {
        String[] weekdays = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        // DayOfWeek: 1=Monday, 7=Sunday
        int index = dateTime.getDayOfWeek().getValue() - 1;
        return weekdays[index];
    }
}