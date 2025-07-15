<template>
  <div 
    class="conversation-item" 
    :class="{ active: isActive }"
    @click="selectConversation"
  >
    <div class="conversation-content">
      <div class="conversation-title">{{ conversation.title }}</div>
      <div class="conversation-preview" v-if="conversation.lastMessage">
        {{ conversation.lastMessage }}
      </div>
      <div class="conversation-time">
        {{ conversation.lastMessageTime ? formatTime(conversation.lastMessageTime) : '未知时间' }}
      </div>
    </div>
    
    <button 
      class="delete-btn"
      @click.stop="deleteConversation"
      title="删除对话"
    >
      <svg viewBox="0 0 24 24" fill="currentColor">
        <path d="M19,4H15.5L14.5,3H9.5L8.5,4H5V6H19M6,19A2,2 0 0,0 8,21H16A2,2 0 0,0 18,19V7H6V19Z" />
      </svg>
    </button>
  </div>
</template>

<script setup lang="ts">
import type { Conversation } from '@/types'

// Props
interface Props {
  conversation: Conversation
  isActive: boolean
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  select: [conversationId: string]
  delete: [conversationId: string]
}>()

// 方法
const selectConversation = () => {
  emit('select', props.conversation.id)
}

const deleteConversation = () => {
  if (confirm('确定要删除这个对话吗？此操作不可撤销。')) {
    emit('delete', props.conversation.id)
  }
}

const formatTime = (time: Date): string => {
  const now = new Date()
  const diff = now.getTime() - time.getTime()
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  
  return time.toLocaleDateString('zh-CN', {
    month: 'short',
    day: 'numeric'
  })
}
</script>

<style scoped>
.conversation-item {
  display: flex;
  align-items: center;
  padding: 0.75rem;
  margin: 0.25rem 0;
  border-radius: 0.5rem;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
  position: relative;
  background: white;
}

.conversation-item:hover {
  background: #f8fafc;
  border-color: #e2e8f0;
  transform: translateX(2px);
}

.conversation-item.active {
  background: #eff6ff;
  border-color: #3b82f6;
  box-shadow: 0 2px 4px rgba(59, 130, 246, 0.1);
}

.conversation-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: #3b82f6;
  border-radius: 0 2px 2px 0;
}

.conversation-content {
  flex: 1;
  min-width: 0;
}

.conversation-title {
  font-weight: 600;
  color: #1e293b;
  font-size: 0.9rem;
  margin-bottom: 0.25rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conversation-preview {
  color: #64748b;
  font-size: 0.8rem;
  margin-bottom: 0.25rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.3;
}

.conversation-time {
  color: #94a3b8;
  font-size: 0.75rem;
  font-weight: 500;
}

.delete-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.375rem;
  border-radius: 0.25rem;
  color: #94a3b8;
  transition: all 0.2s;
  opacity: 0;
  transform: scale(0.9);
  margin-left: 0.5rem;
}

.conversation-item:hover .delete-btn {
  opacity: 1;
  transform: scale(1);
}

.delete-btn:hover {
  background: #fee2e2;
  color: #dc2626;
}

.delete-btn svg {
  width: 16px;
  height: 16px;
}

/* 活跃状态下的删除按钮 */
.conversation-item.active .delete-btn {
  opacity: 0.7;
}

.conversation-item.active:hover .delete-btn {
  opacity: 1;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .conversation-item {
    padding: 0.625rem;
  }
  
  .conversation-title {
    font-size: 0.85rem;
  }
  
  .conversation-preview {
    font-size: 0.75rem;
  }
  
  .conversation-time {
    font-size: 0.7rem;
  }
}

/* 动画效果 */
.conversation-item {
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-10px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* 加载状态 */
.conversation-item.loading {
  pointer-events: none;
  opacity: 0.6;
}

.conversation-item.loading::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.4), transparent);
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(100%);
  }
}
</style>
