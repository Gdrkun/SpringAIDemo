<template>
  <div class="message-bubble" :class="{ 'user-message': message.isUser, 'ai-message': !message.isUser, 'error-message': message.isError }">
    <div class="message-avatar" v-if="!message.isUser">
      <div class="avatar-icon">
        <svg viewBox="0 0 24 24" fill="currentColor">
          <path d="M12 2C13.1 2 14 2.9 14 4C14 5.1 13.1 6 12 6C10.9 6 10 5.1 10 4C10 2.9 10.9 2 12 2ZM21 9V7L15 1L9 7V9C9 10.1 9.9 11 11 11V14L13 16L15 14V11C16.1 11 17 10.1 17 9M11 22A2 2 0 0 1 9 20A2 2 0 0 1 11 18A2 2 0 0 1 13 20A2 2 0 0 1 11 22Z"/>
        </svg>
      </div>
    </div>

    <div class="message-content">
      <div class="message-text" v-html="formattedText"></div>
      <div class="message-time">
        {{ formatTime(message.timestamp) }}
      </div>
    </div>

    <div class="message-avatar" v-if="message.isUser">
      <div class="avatar-icon user-avatar">
        <svg viewBox="0 0 24 24" fill="currentColor">
          <path d="M12,4A4,4 0 0,1 16,8A4,4 0 0,1 12,12A4,4 0 0,1 8,8A4,4 0 0,1 12,4M12,14C16.42,14 20,15.79 20,18V20H4V18C4,15.79 7.58,14 12,14Z"/>
        </svg>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { Message } from '@/types'

// Props
interface Props {
  message: Message
}

const props = defineProps<Props>()

// 计算属性
const formattedText = computed((): string => {
  // 简单的文本格式化，将换行符转换为<br>
  return props.message.text.replace(/\n/g, '<br>')
})

// 方法
const formatTime = (timestamp: Date): string => {
  const now = new Date()
  const messageTime = new Date(timestamp)
  const diffInMinutes = Math.floor((now.getTime() - messageTime.getTime()) / (1000 * 60))

  if (diffInMinutes < 1) {
    return '刚刚'
  } else if (diffInMinutes < 60) {
    return `${diffInMinutes}分钟前`
  } else if (diffInMinutes < 1440) {
    const hours = Math.floor(diffInMinutes / 60)
    return `${hours}小时前`
  } else {
    return messageTime.toLocaleDateString('zh-CN', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }
}
</script>

<style scoped>
.message-bubble {
  display: flex;
  gap: 0.75rem;
  max-width: 70%;
  margin-bottom: 1.5rem;
  animation: slideIn 0.3s ease-out;
}

/* 大屏幕消息气泡优化 */
@media (min-width: 1200px) {
  .message-bubble {
    max-width: 60%;
  }
}

@media (min-width: 1440px) {
  .message-bubble {
    max-width: 55%;
  }
}

@media (min-width: 1920px) {
  .message-bubble {
    max-width: 50%;
  }
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.user-message {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.ai-message {
  align-self: flex-start;
}

.message-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: transform 0.2s ease;
}

.message-avatar:hover {
  transform: scale(1.05);
}

.avatar-icon {
  width: 24px;
  height: 24px;
  color: white;
}

.user-avatar {
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
}

.ai-message .message-avatar {
  background: linear-gradient(135deg, #06b6d4 0%, #3b82f6 100%);
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-text {
  padding: 1rem 1.25rem;
  border-radius: 20px;
  line-height: 1.6;
  word-wrap: break-word;
  white-space: pre-wrap;
  font-size: 0.95rem;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  transition: all 0.2s ease;
  position: relative;
}

.message-text:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 20px rgba(0,0,0,0.12);
}

.user-message .message-text {
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  color: white;
  border-bottom-right-radius: 8px;
}

.user-message .message-text::before {
  content: '';
  position: absolute;
  right: -8px;
  bottom: 8px;
  width: 0;
  height: 0;
  border-left: 8px solid #7c3aed;
  border-top: 8px solid transparent;
  border-bottom: 8px solid transparent;
}

.ai-message .message-text {
  background: white;
  color: #374151;
  border: 1px solid #e5e7eb;
  border-bottom-left-radius: 8px;
}

.ai-message .message-text::before {
  content: '';
  position: absolute;
  left: -8px;
  bottom: 8px;
  width: 0;
  height: 0;
  border-right: 8px solid white;
  border-top: 8px solid transparent;
  border-bottom: 8px solid transparent;
}

.error-message .message-text {
  background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%);
  border-color: #fca5a5;
  color: #dc2626;
}

.message-time {
  font-size: 0.75rem;
  color: #9ca3af;
  margin-top: 0.5rem;
  padding: 0 1.25rem;
  font-weight: 500;
}

.user-message .message-time {
  text-align: right;
}

.ai-message .message-time {
  text-align: left;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .message-bubble {
    max-width: 85%;
    gap: 0.5rem;
  }

  .message-avatar {
    width: 36px;
    height: 36px;
  }

  .avatar-icon {
    width: 20px;
    height: 20px;
  }

  .message-text {
    padding: 0.8rem 1rem;
    font-size: 0.9rem;
  }

  .message-time {
    padding: 0 1rem;
  }
}

@media (max-width: 480px) {
  .message-bubble {
    max-width: 95%;
  }

  .message-text {
    padding: 0.7rem 0.9rem;
  }
}
</style>
