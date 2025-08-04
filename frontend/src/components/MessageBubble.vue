<template>
  <div class="message-wrapper" :class="{ 'user-message': message.isUser, 'ai-message': !message.isUser }">
    <!-- AI头像 -->
    <div v-if="!message.isUser" class="message-avatar ai-avatar">
      <div class="avatar-inner">
        <svg viewBox="0 0 24 24" fill="currentColor" class="avatar-icon">
          <path d="M12,2A2,2 0 0,1 14,4C14,4.74 13.6,5.39 13,5.73V7A1,1 0 0,0 14,8H18A1,1 0 0,0 19,7V5.73C18.4,5.39 18,4.74 18,4A2,2 0 0,1 20,2A2,2 0 0,1 22,4C22,4.74 21.6,5.39 21,5.73V7A3,3 0 0,1 18,10H14A3,3 0 0,1 11,7V5.73C10.4,5.39 10,4.74 10,4A2,2 0 0,1 12,2M7,10A2,2 0 0,1 9,12A2,2 0 0,1 7,14A2,2 0 0,1 5,12A2,2 0 0,1 7,10M17,10A2,2 0 0,1 19,12A2,2 0 0,1 17,14A2,2 0 0,1 15,12A2,2 0 0,1 17,10M12,10A2,2 0 0,1 14,12A2,2 0 0,1 12,14A2,2 0 0,1 10,12A2,2 0 0,1 12,10Z"/>
        </svg>
      </div>
    </div>

    <!-- 消息内容 -->
    <div class="message-content">
      <div class="message-bubble" :class="{
        'user-bubble': message.isUser,
        'ai-bubble': !message.isUser,
        'error-bubble': message.isError
      }">
        <div class="message-text" v-html="formattedText"></div>
        <div class="message-meta">
          <span class="message-time">{{ formatTime(message.timestamp) }}</span>
        </div>
      </div>
    </div>

    <!-- 用户头像 -->
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
import { marked } from 'marked';
import hljs from 'highlight.js';
import type { Message } from '@/types';

interface Props {
  message: Message;
}
const props = defineProps<Props>();

const formattedText = computed((): string => {
  if (!props.message || !props.message.text) {
    return '';
  }

  // 用户消息：简单替换换行符
  if (props.message.isUser) {
    return props.message.text.replace(/\n/g, '<br>');
  }

  // AI消息（包括流式消息）：执行完整的 Markdown 解析
  try {
    const renderer = new marked.Renderer();
    renderer.code = (code, lang) => {
      const language = lang && hljs.getLanguage(lang) ? lang : 'plaintext';
      try {
        const highlighted = hljs.highlight(code, { language, ignoreIllegals: true }).value;
        return `<pre><code class="hljs language-${language}">${highlighted}</code></pre>`;
      } catch (error) {
        console.warn(`代码高亮失败: ${error}`);
        const highlighted = hljs.highlightAuto(code).value;
        return `<pre><code class="hljs">${highlighted}</code></pre>`;
      }
    };

    return marked(props.message.text, {
      gfm: true,
      breaks: true,
      renderer,
      async: false,
    }) as string;
  } catch (error) {
    console.error('Markdown解析失败:', error);
    return props.message.text.replace(/\n/g, '<br>');
  }

  // 非流式的AI消息：执行完整的 Markdown 解析
  try {
    const renderer = new marked.Renderer();
    renderer.code = (code, lang) => {
      const language = lang && hljs.getLanguage(lang) ? lang : 'plaintext';
      try {
        const highlighted = hljs.highlight(code, { language, ignoreIllegals: true }).value;
        return `<pre><code class="hljs language-${language}">${highlighted}</code></pre>`;
      } catch (error) {
        console.warn(`代码高亮失败: ${error}`);
        const highlighted = hljs.highlightAuto(code).value;
        return `<pre><code class="hljs">${highlighted}</code></pre>`;
      }
    };

    return marked(props.message.text, {
      gfm: true,
      breaks: true,
      renderer,
      async: false,
    }) as string;
  } catch (error) {
    console.error('Markdown解析失败:', error);
    return props.message.text.replace(/\n/g, '<br>');
  }
});

const formatTime = (timestamp: Date): string => {
  const now = new Date();
  const messageTime = new Date(timestamp);
  const diffInMinutes = Math.floor((now.getTime() - messageTime.getTime()) / (1000 * 60));

  if (diffInMinutes < 1) {
    return '刚刚';
  } else if (diffInMinutes < 60) {
    return `${diffInMinutes}分钟前`;
  } else if (diffInMinutes < 1440) {
    const hours = Math.floor(diffInMinutes / 60);
    return `${hours}小时前`;
  } else {
    return messageTime.toLocaleDateString('zh-CN', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  }
};
</script>

<style scoped>
/* 消息容器 */
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

/* 头像样式 */
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

/* AI头像 */
.ai-avatar .avatar-inner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
}

.ai-avatar .avatar-inner::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(45deg, rgba(255,255,255,0.2) 0%, transparent 50%, rgba(255,255,255,0.1) 100%);
  border-radius: 50%;
}

/* 用户头像 */
.user-avatar .avatar-inner {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  position: relative;
}

.user-avatar .avatar-inner::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(45deg, rgba(255,255,255,0.2) 0%, transparent 50%, rgba(255,255,255,0.1) 100%);
  border-radius: 50%;
}

/* 消息内容 */
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

.message-bubble:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
}

/* AI消息气泡 */
.ai-bubble {
  background: rgba(255, 255, 255, 0.95);
  border-left: 3px solid #667eea;
  color: #2d3748;
  border-bottom-left-radius: 6px;
}

.ai-bubble::before {
  content: '';
  position: absolute;
  left: -8px;
  bottom: 12px;
  width: 0;
  height: 0;
  border-right: 8px solid rgba(255, 255, 255, 0.95);
  border-top: 6px solid transparent;
  border-bottom: 6px solid transparent;
}

/* 用户消息气泡 */
.user-bubble {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-bottom-right-radius: 6px;
}

.user-bubble::before {
  content: '';
  position: absolute;
  right: -8px;
  bottom: 12px;
  width: 0;
  height: 0;
  border-left: 8px solid #764ba2;
  border-top: 6px solid transparent;
  border-bottom: 6px solid transparent;
}

/* 错误消息气泡 */
.error-bubble {
  background: rgba(254, 242, 242, 0.95);
  border-left: 3px solid #ef4444;
  color: #dc2626;
}

/* 消息文本 */
.message-text {
  line-height: 1.65;
  word-wrap: break-word;
  font-size: 0.95rem;
  margin-bottom: 0.5rem;
  transition: color 0.3s ease;
  white-space: pre-wrap; /* 确保换行符显示 */
}

.user-bubble .message-text {
  color: white;
  font-weight: 500;
}

.ai-bubble .message-text {
  color: #2d3748;
}

.error-bubble .message-text {
  color: #dc2626;
}

/* 消息元信息 */
.message-meta {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 0.25rem;
}

.user-message .message-meta {
  justify-content: flex-start;
}

.message-time {
  font-size: 0.75rem;
  opacity: 0.7;
  font-weight: 500;
  transition: opacity 0.3s ease;
}

.user-bubble .message-time {
  color: rgba(255, 255, 255, 0.8);
}

.ai-bubble .message-time {
  color: #6b7280;
}

.message-bubble:hover .message-time {
  opacity: 1;
}

/* Markdown样式 */
.message-text :deep(h1),
.message-text :deep(h2),
.message-text :deep(h3),
.message-text :deep(h4),
.message-text :deep(h5),
.message-text :deep(h6) {
  margin: 1rem 0 0.5rem 0;
  font-weight: 600;
  line-height: 1.3;
}

.ai-bubble .message-text :deep(h1),
.ai-bubble .message-text :deep(h2),
.ai-bubble .message-text :deep(h3),
.ai-bubble .message-text :deep(h4),
.ai-bubble .message-text :deep(h5),
.ai-bubble .message-text :deep(h6) {
  color: #1a202c;
}

.user-bubble .message-text :deep(h1),
.user-bubble .message-text :deep(h2),
.user-bubble .message-text :deep(h3),
.user-bubble .message-text :deep(h4),
.user-bubble .message-text :deep(h5),
.user-bubble .message-text :deep(h6) {
  color: rgba(255, 255, 255, 0.95);
}

.message-text :deep(h1) { font-size: 1.5rem; }
.message-text :deep(h2) { font-size: 1.3rem; }
.message-text :deep(h3) { font-size: 1.1rem; }
.message-text :deep(h4) { font-size: 1rem; }
.message-text :deep(h5) { font-size: 0.9rem; }
.message-text :deep(h6) { font-size: 0.85rem; }

.message-text :deep(p) {
  margin: 0.5rem 0;
  line-height: 1.65;
}

.message-text :deep(ul),
.message-text :deep(ol) {
  margin: 0.5rem 0;
  padding-left: 1.5rem;
}

.message-text :deep(li) {
  margin: 0.25rem 0;
  line-height: 1.6;
}

.message-text :deep(blockquote) {
  margin: 1rem 0;
  padding: 0.75rem 1rem;
  border-left: 4px solid #667eea;
  background: rgba(102, 126, 234, 0.1);
  border-radius: 0 8px 8px 0;
  font-style: italic;
}

.ai-bubble .message-text :deep(blockquote) {
  color: #4a5568;
}

.user-bubble .message-text :deep(blockquote) {
  background: rgba(255, 255, 255, 0.15);
  border-left-color: rgba(255, 255, 255, 0.6);
  color: rgba(255, 255, 255, 0.9);
}

.message-text :deep(code) {
  background: rgba(102, 126, 234, 0.1);
  padding: 0.2rem 0.4rem;
  border-radius: 4px;
  font-family: 'SF Mono', 'Monaco', 'Inconsolata', 'Roboto Mono', 'Consolas', monospace;
  font-size: 0.85rem;
  color: #667eea;
  border: 1px solid rgba(102, 126, 234, 0.2);
}

.user-bubble .message-text :deep(code) {
  background: rgba(255, 255, 255, 0.2);
  color: rgba(255, 255, 255, 0.95);
  border-color: rgba(255, 255, 255, 0.3);
}

.message-text :deep(pre) {
  margin-top: 0.5rem; /* Reduced top margin */
  margin-bottom: 0.5rem; /* Adjusted bottom margin for consistency */
  padding: 1rem;
  background: rgba(45, 55, 72, 0.95);
  border: 1px solid rgba(102, 126, 234, 0.2);
  border-radius: 8px;
  overflow-x: auto;
  font-family: 'SF Mono', 'Monaco', 'Inconsolata', 'Roboto Mono', 'Consolas', monospace;
  font-size: 0.85rem;
  line-height: 1.5;
}

.message-text :deep(pre code) {
  background: none;
  padding: 0;
  border: none;
  color: #e2e8f0;
}

.message-text :deep(table) {
  width: 100%;
  margin: 1rem 0;
  border-collapse: collapse;
  border: 1px solid rgba(102, 126, 234, 0.2);
  border-radius: 8px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.5);
}

.user-bubble .message-text :deep(table) {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.3);
}

.message-text :deep(th),
.message-text :deep(td) {
  padding: 0.5rem 0.75rem;
  text-align: left;
  border-bottom: 1px solid rgba(102, 126, 234, 0.1);
}

.user-bubble .message-text :deep(th),
.user-bubble .message-text :deep(td) {
  border-bottom-color: rgba(255, 255, 255, 0.2);
}

.message-text :deep(th) {
  background: rgba(102, 126, 234, 0.1);
  font-weight: 600;
}

.ai-bubble .message-text :deep(th) {
  color: #2d3748;
}

.user-bubble .message-text :deep(th) {
  background: rgba(255, 255, 255, 0.15);
  color: rgba(255, 255, 255, 0.95);
}

.message-text :deep(tr:last-child td) {
  border-bottom: none;
}

.message-text :deep(a) {
  color: #667eea;
  text-decoration: none;
  border-bottom: 1px solid transparent;
  transition: all 0.2s ease;
  font-weight: 500;
}

.message-text :deep(a:hover) {
  border-bottom-color: #667eea;
  color: #5a67d8;
}

.user-bubble .message-text :deep(a) {
  color: rgba(255, 255, 255, 0.9);
  border-bottom-color: transparent;
}

.user-bubble .message-text :deep(a:hover) {
  color: white;
  border-bottom-color: rgba(255, 255, 255, 0.7);
}

.message-text :deep(strong) {
  font-weight: 600;
}

.ai-bubble .message-text :deep(strong) {
  color: #2d3748;
}

.user-bubble .message-text :deep(strong) {
  color: rgba(255, 255, 255, 0.95);
}

.message-text :deep(em) {
  font-style: italic;
}

.ai-bubble .message-text :deep(em) {
  color: #4a5568;
}

.user-bubble .message-text :deep(em) {
  color: rgba(255, 255, 255, 0.85);
}

.message-text :deep(hr) {
  margin: 1.5rem 0;
  border: none;
  height: 1px;
  background: rgba(102, 126, 234, 0.2);
}

.user-bubble .message-text :deep(hr) {
  background: rgba(255, 255, 255, 0.3);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .message-wrapper {
    max-width: 90%;
    gap: 0.75rem;
    margin-bottom: 1.25rem;
  }

  .message-avatar {
    width: 38px;
    height: 38px;
  }

  .avatar-icon {
    width: 20px;
    height: 20px;
  }

  .message-bubble {
    padding: 0.875rem 1rem 0.625rem;
    border-radius: 16px;
  }

  .message-text {
    font-size: 0.9rem;
    line-height: 1.6;
  }

  .message-time {
    font-size: 0.7rem;
  }

  .message-bubble::before {
    border-width: 6px;
  }

  .ai-bubble::before {
    left: -6px;
    border-right-width: 6px;
  }

  .user-bubble::before {
    right: -6px;
    border-left-width: 6px;
  }
}

@media (max-width: 480px) {
  .message-wrapper {
    max-width: 95%;
    gap: 0.625rem;
    margin-bottom: 1rem;
  }

  .message-avatar {
    width: 34px;
    height: 34px;
  }

  .avatar-icon {
    width: 18px;
    height: 18px;
  }

  .message-bubble {
    padding: 0.75rem 0.875rem 0.5rem;
    border-radius: 14px;
  }

  .message-text {
    font-size: 0.875rem;
    line-height: 1.55;
  }

  .message-time {
    font-size: 0.65rem;
  }

  .message-bubble::before {
    border-width: 5px;
  }

  .ai-bubble::before {
    left: -5px;
    border-right-width: 5px;
  }

  .user-bubble::before {
    right: -5px;
    border-left-width: 5px;
  }
}
</style>
