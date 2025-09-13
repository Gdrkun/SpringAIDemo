package com.drk.SpringAIDemo.utils;

/**
 * @Author glk
 * @Date 2025/8/7 11:54
 * @Version 1.0
 */
public class TextCleanerUtil {

    /**
     * 文件内容清洗类
     * @param content 内容
     * @param fileType 文件类型
     * @return 清洗后的内容
     */
    public static String clean(String content, String fileType) {
        if (content == null || content.isBlank()) {
            return "";
        }

        return switch (fileType.toLowerCase()) {
            case "md" -> cleanMarkdown(content);
            case "txt", "text" -> cleanText(content);
            case "pdf" -> cleanPdf(content);
            default -> cleanGeneric(content);
        };
    }

    /** 针对 Markdown 的清洗 */
    private static String cleanMarkdown(String markdown) {
        String noMdSyntax = markdown
                .replaceAll("(?m)^#+\\s*", "")            // # 标题
                .replaceAll("\\*\\*|__", "")              // 加粗
                .replaceAll("\\*|_", "")                  // 斜体
                .replaceAll("`+", "")                     // 代码块
                .replaceAll("!\\[.*?\\]\\(.*?\\)", "")    // 图片
                .replaceAll("\\[.*?\\]\\(.*?\\)", "")     // 链接
                .replaceAll("(?m)^>\\s*", "")             // 引用
                .trim();
        return cleanCommon(noMdSyntax);
    }

    /** 针对 TXT 文本的清洗 */
    private static String cleanText(String content) {
        return cleanCommon(content);
    }

    /** 针对 PDF 提取内容后的清洗（如页码） */
    private static String cleanPdf(String content) {
        String withoutPageNumber = content.replaceAll("(?m)^Page \\d+.*", "");  // 页码
        return cleanCommon(withoutPageNumber);
    }

    /** 默认通用清洗器 */
    private static String cleanGeneric(String content) {
        return cleanCommon(content);
    }

    /** 公共清洗逻辑 */
    private static String cleanCommon(String content) {
        return content
                .replaceAll("[\\t\\x0B\\f\\r]+", " ")     // 特殊空白符
                .replaceAll("\\s{2,}", " ")               // 多空格
                .replaceAll("(?m)^\\s*$", "")             // 空行
                .trim();
    }
}
