<template>
  <div class="message-wrapper" :class="{ 'user-message': message.isUser, 'ai-message': !message.isUser }">
    <!-- AI Avatar -->
    <div v-if="!message.isUser" class="message-avatar ai-avatar">
      <div class="avatar-inner">
        <svg viewBox="0 0 24 24" fill="currentColor" class="avatar-icon">
          <path d="M12,2A2,2 0 0,1 14,4C14,4.74 13.6,5.39 13,5.73V7A1,1 0 0,0 14,8H18A1,1 0 0,0 19,7V5.73C18.4,5.39 18,4.74 18,4A2,2 0 0,1 20,2A2,2 0 0,1 22,4C22,4.74 21.6,5.39 21,5.73V7A3,3 0 0,1 18,10H14A3,3 0 0,1 11,7V5.73C10.4,5.39 10,4.74 10,4A2,2 0 0,1 12,2M7,10A2,2 0 0,1 9,12A2,2 0 0,1 7,14A2,2 0 0,1 5,12A2,2 0 0,1 7,10M17,10A2,2 0 0,1 19,12A2,2 0 0,1 17,14A2,2 0 0,1 15,12A2,2 0 0,1 17,10M12,10A2,2 0 0,1 14,12A2,2 0 0,1 12,14A2,2 0 0,1 10,12A2,2 0 0,1 12,10Z"/>
        </svg>
      </div>
    </div>

    <!-- Message Content -->
    <div class="message-content">
      <div class="message-bubble" :class="{
        'user-bubble': message.isUser,
        'ai-bubble': !message.isUser,
        'error-bubble': message.isError
      }">
        <!-- The v-html directive is used to render the Markdown-parsed content -->
        <div class="message-text" v-html="formattedText"></div>
        <div class="message-meta">
          <span class="message-time">{{ formatTime(message.timestamp) }}</span>
        </div>
      </div>
    </div>

    <!-- User Avatar -->
    <div v-if="message.isUser" class="message-avatar user-avatar">
      <div class="avatar-inner">
        <svg viewBox="0 0 24 24" fill="currentColor" class="avatar-icon">
          <path d="M12,4A4,4 0 0,1 16,8A4,4 0 0,1 12,12A4,4 0 0,1 8,8A4,4 0 0,1 12,4M12,14C16.42,14 20,15.79 20,18V20H4V18C4,15.79 7.58,14 12,14Z"/>
        </svg>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import MarkdownIt from 'markdown-it';
import type { Message } from '@/types';
import hljs from 'highlight.js/lib/core';
import javascript from 'highlight.js/lib/languages/javascript';
import xml from 'highlight.js/lib/languages/xml';
import css from 'highlight.js/lib/languages/css';
import json from 'highlight.js/lib/languages/json';
import bash from 'highlight.js/lib/languages/bash';
import python from 'highlight.js/lib/languages/python';
import java from 'highlight.js/lib/languages/java';
import sql from 'highlight.js/lib/languages/sql';
import plaintext from 'highlight.js/lib/languages/plaintext';

hljs.registerLanguage('javascript', javascript);
hljs.registerLanguage('jsx', javascript);
hljs.registerLanguage('ts', javascript);
hljs.registerLanguage('typescript', javascript);
hljs.registerLanguage('xml', xml);
hljs.registerLanguage('html', xml);
hljs.registerLanguage('css', css);
hljs.registerLanguage('json', json);
hljs.registerLanguage('bash', bash);
hljs.registerLanguage('sh', bash);
hljs.registerLanguage('python', python);
hljs.registerLanguage('py', python);
hljs.registerLanguage('java', java);
hljs.registerLanguage('sql', sql);
hljs.registerLanguage('plaintext', plaintext);

interface Props {
  message: Message;
}
const props = defineProps<Props>();

// 创建 markdown-it 实例
const createMarkdownInstance = () => {
  const md = new MarkdownIt({
    html: false,
    xhtmlOut: false,
    breaks: false,
    langPrefix: 'language-',
    linkify: true,
    typographer: true,
    highlight: (str: string, lang: string) => {
      if (lang && hljs.getLanguage(lang)) {
        try {
          return hljs.highlight(str, { language: lang, ignoreIllegals: true }).value;
        } catch (__) {}
      }
      return ''; // use external default escaping
    }
  });

  // Custom fence renderer
  md.renderer.rules.fence = (tokens, idx, options, env, self) => {
    const token = tokens[idx];
    let info = token.info ? md.utils.unescapeAll(token.info).trim() : '';
    let content = token.content;

    // Check for [language] syntax on the first line of the content,
    // especially when the fence is generic (e.g., ```plaintext or ```)
    const langMatch = content.match(/^\s*([a-zA-Z0-9_.-]+)\s*\n/);
    if ((!info || info === 'plaintext') && langMatch) {
      info = langMatch[1]; // Use language from content
      content = content.substring(langMatch[0].length); // Strip the language line
    }

    const langName = (info.split(/\s+/g)[0] || 'plaintext').toLowerCase();
    const validLanguage = hljs.getLanguage(langName) ? langName : 'plaintext';

    // Use the highlight function from options
    const highlighted = options.highlight(content, langName, '') || md.utils.escapeHtml(content);

    return `<div class="code-block-wrapper">
      <div class="code-block-header">
        <span class="code-language">${validLanguage}</span>
      </div>
      <pre class="code-block"><code class="hljs language-${validLanguage}">${highlighted}</code></pre>
    </div>`;
  };

  return md;
};

// 创建 markdown 实例
const markdownInstance = createMarkdownInstance();

/**
 * 修复流式传输中可能出现的 Markdown 问题
 */
const fixStreamingMarkdown = (markdown: string): string => {
    // This function robustly prepares streaming markdown for rendering.
    // It ensures that every code fence (` ``` `) is on its own line, which is
    // essential for the markdown parser to correctly identify separate code blocks.

    // Step 1: Ensure a newline *before* any code fence that isn't already preceded by one.
    // The negative lookbehind `(?<!\n)` is key to preventing extra lines.
    let fixed = markdown.replace(/(?<!\n)```/g, '\n```');

    // Step 2: Ensure a newline *after* any code fence that isn't followed by one.
    // The negative lookahead `(?!\n)` prevents adding extra lines if one already exists.
    fixed = fixed.replace(/```(?!\n)/g, '```\n');

    // Step 3: If a code block is still open (i.e., odd number of fences),
    // we add a temporary closing fence for the renderer. This fence
    // MUST also start on a new line to be parsed correctly.
    const fenceCount = (fixed.match(/```/g) || []).length;
    if (fenceCount % 2 === 1) {
        // Ensure there is a newline before adding the closing fence
        if (!fixed.endsWith('\n')) {
            fixed += '\n';
        }
        fixed += '```';
    }

    return fixed;
};

const formattedText = computed((): string => {
  if (!props.message?.text) {
    return '';
  }

  // 用户消息：简单的 HTML 转义和换行处理
  if (props.message.isUser) {
    const escapedText = props.message.text
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
    return escapedText.replace(/\n/g, '<br>');
  }

  // AI 消息：Markdown 解析
  try {
    const textToParse = fixStreamingMarkdown(props.message.text);
    const result = markdownInstance.render(textToParse);
    return result;
  } catch (error) {
    console.error('Markdown parsing failed:', error);
    console.error('Problematic text:', props.message.text.substring(0, 200) + '...');

    // 失败时返回简单的 HTML 转义版本
    const escapedText = props.message.text
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/\n/g, '<br>');
    return escapedText;
  }
});

const formatTime = (timestamp: Date): string => {
  if (!timestamp) return '';
  const messageTime = new Date(timestamp);
  return messageTime.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
};

</script>

<style>
/* Import a highlight.js theme for code blocks */
@import 'highlight.js/styles/atom-one-dark.css';

/* 基础消息样式 */
.message-wrapper {
  display: flex;
  gap: 0.875rem;
  max-width: 85%;
  margin-bottom: 1.5rem;
  animation: messageSlideIn 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  align-items: flex-end;
}

.message-wrapper.user-message {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-wrapper.ai-message {
  align-self: flex-start;
}

@keyframes messageSlideIn {
  from {
    opacity: 0;
    transform: translateY(24px) scale(0.92);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.message-avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  flex-shrink: 0;
  position: relative;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.message-avatar:hover {
  transform: scale(1.08) translateY(-2px);
}

.avatar-inner {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: box-shadow 0.3s ease;
}

.message-avatar:hover .avatar-inner {
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
}

.avatar-icon {
  width: 22px;
  height: 22px;
  color: white;
  z-index: 2;
}

.ai-avatar .avatar-inner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.user-avatar .avatar-inner {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.message-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.message-bubble {
  border-radius: 18px;
  padding: 1rem 1.25rem 0.75rem;
  position: relative;
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.ai-bubble {
  background: rgba(255, 255, 255, 0.95);
  border-left: 3px solid #667eea;
  color: #2d3748;
  border-bottom-left-radius: 6px;
}

.user-bubble {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-bottom-right-radius: 6px;
}

.error-bubble {
  background: rgba(254, 242, 242, 0.95);
  border-left: 3px solid #ef4444;
  color: #dc2626;
}

.message-text {
  line-height: 1.65;
  word-wrap: break-word;
  font-size: 0.95rem;
  margin-bottom: 0.5rem;
  white-space: pre-wrap;
}

.message-text :deep(p:first-child) {
  margin-top: 0;
}
.message-text :deep(p:last-child) {
  margin-bottom: 0;
}

/* 代码块容器样式 - 关键修复 */
.message-text :deep(.code-block-wrapper) {
  margin: 1.2rem 0;
  border-radius: 12px;
  overflow: hidden;
  background: #1e1e1e;
  border: 1px solid #333;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);
}

/* 代码块头部 */
.message-text :deep(.code-block-header) {
  background: #2d2d2d;
  padding: 0.5rem 1rem;
  border-bottom: 1px solid #444;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.message-text :deep(.code-language) {
  color: #a8a8a8;
  font-size: 0.75rem;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

/* 代码块主体 */
.message-text :deep(.code-block) {
  margin: 0 !important;
  padding: 1.2rem !important;
  background: #1e1e1e !important;
  border-radius: 0 !important;
  overflow-x: auto;
  font-family: 'SF Mono', 'Monaco', 'Inconsolata', 'Roboto Mono', 'Consolas', monospace;
  font-size: 0.85rem;
  line-height: 1.6;
  color: #d4d4d4 !important;
}

.message-text :deep(.code-block code.hljs) {
  background: transparent !important;
  padding: 0 !important;
  color: #d4d4d4 !important;
  display: block;
  font-family: inherit;
}

/* 滚动条样式 */
.message-text :deep(.code-block)::-webkit-scrollbar {
  height: 6px;
}

.message-text :deep(.code-block)::-webkit-scrollbar-track {
  background: #2d2d2d;
}

.message-text :deep(.code-block)::-webkit-scrollbar-thumb {
  background: #555;
  border-radius: 3px;
}

.message-text :deep(.code-block)::-webkit-scrollbar-thumb:hover {
  background: #777;
}

/* 内联代码样式 */
.message-text :deep(p code:not(.hljs)),
.message-text :deep(li code:not(.hljs)) {
  background: rgba(0, 0, 0, 0.08);
  color: #c7254e;
  padding: 0.2em 0.4em;
  border-radius: 4px;
  font-family: 'SF Mono', 'Monaco', 'Inconsolata', 'Roboto Mono', 'Consolas', monospace;
  font-size: 0.88em;
  font-weight: 500;
  border: 1px solid rgba(0, 0, 0, 0.1);
}

/* 用户消息中的内联代码 */
.user-bubble .message-text :deep(p code:not(.hljs)),
.user-bubble .message-text :deep(li code:not(.hljs)) {
  background: rgba(255, 255, 255, 0.2);
  color: rgba(255, 255, 255, 0.95);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

/* 表格样式 */
.message-text :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin: 1rem 0;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.message-text :deep(table th),
.message-text :deep(table td) {
  border: 1px solid #e2e8f0;
  padding: 0.75rem;
  text-align: left;
}

.message-text :deep(table th) {
  background-color: #f8fafc;
  font-weight: 600;
  color: #374151;
}

.message-text :deep(table tr:nth-child(even)) {
  background-color: #f9fafb;
}

/* 引用块样式 */
.message-text :deep(blockquote) {
  border-left: 4px solid #667eea;
  margin: 1rem 0;
  padding: 1rem 1.5rem;
  background: rgba(102, 126, 234, 0.08);
  border-radius: 0 8px 8px 0;
  font-style: italic;
  position: relative;
}

.message-text :deep(blockquote::before) {
  content: '"';
  font-size: 3rem;
  color: #667eea;
  position: absolute;
  top: -0.5rem;
  left: 0.5rem;
  opacity: 0.3;
}

/* 列表样式 */
.message-text :deep(ul),
.message-text :deep(ol) {
  padding-left: 1.5rem;
  margin: 0.75rem 0;
}

.message-text :deep(li) {
  margin: 0.4rem 0;
  line-height: 1.6;
}

.message-text :deep(ul li) {
  list-style-type: disc;
}

.message-text :deep(ol li) {
  list-style-type: decimal;
}

/* 链接样式 */
.message-text :deep(a) {
  color: #667eea;
  text-decoration: none;
  border-bottom: 1px solid transparent;
  transition: all 0.2s ease;
}

.message-text :deep(a:hover) {
  color: #5a67d8;
  border-bottom-color: #5a67d8;
}

.user-bubble .message-text :deep(a) {
  color: rgba(255, 255, 255, 0.9);
}

.user-bubble .message-text :deep(a:hover) {
  color: white;
  border-bottom-color: white;
}

/* 强调文本样式 */
.message-text :deep(strong) {
  font-weight: 600;
  color: inherit;
}

.message-text :deep(em) {
  font-style: italic;
  color: inherit;
}

/* 时间戳样式 */
.message-meta {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 0.25rem;
}

.message-time {
  font-size: 0.75rem;
  opacity: 0.7;
  font-weight: 500;
}

.user-bubble .message-time {
  color: rgba(255, 255, 255, 0.8);
}

.ai-bubble .message-time {
  color: #6b7280;
}


/* 响应式优化 */
@media (max-width: 768px) {
  .message-wrapper {
    max-width: 95%;
  }

  .message-text :deep(.code-block) {
    font-size: 0.8rem;
    padding: 1rem !important;
  }

  .message-text :deep(.code-block-header) {
    padding: 0.4rem 0.8rem;
  }
}
</style>