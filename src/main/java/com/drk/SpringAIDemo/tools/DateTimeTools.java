package com.drk.SpringAIDemo.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @Author drk
 * @Date 2025/6/19 10:36
 * @Version 1.0
 */
@Component
public class DateTimeTools {

    @Tool(description = "Get the current date and time in the user's timezone",returnDirect = true)
    public String getCurrentDateTime() {
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
    }



}
