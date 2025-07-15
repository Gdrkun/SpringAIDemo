package com.drk.SpringAIDemo.tools;


import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @Author drk
 * @Date 2025/6/19 14:17
 * @Version 1.0
 */

@Component
public class TimingTools {
    @Tool(description = "Set a user alarm for the given time in ISO-8601 format, e.g. '2025-06-19T10:25:00'",returnDirect = true)
    void setAlarm(String time) {
        LocalDateTime alarmTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME);
        System.out.println("Alarm set for " + alarmTime);
    }
}
