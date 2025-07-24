<template>
  <div
    class="conversation-item"
    :class="{ active: isActive }"
    @click="selectConversation"
  >
    <div class="conversation-avatar">
      <div class="avatar-icon">
        <svg viewBox="0 0 24 24" fill="currentColor">
          <path d="M12,2A2,2 0 0,1 14,4C14,4.74 13.6,5.39 13,5.73V7A1,1 0 0,0 14,8H18A1,1 0 0,0 19,7V5.73C18.4,5.39 18,4.74 18,4A2,2 0 0,1 20,2A2,2 0 0,1 22,4C22,4.74 21.6,5.39 21,5.73V7A3,3 0 0,1 18,10H14A3,3 0 0,1 11,7V5.73C10.4,5.39 10,4.74 10,4A2,2 0 0,1 12,2M7,10A2,2 0 0,1 9,12A2,2 0 0,1 7,14A2,2 0 0,1 5,12A2,2 0 0,1 7,10M17,10A2,2 0 0,1 19,12A2,2 0 0,1 17,14A2,2 0 0,1 15,12A2,2 0 0,1 17,10M12,10A2,2 0 0,1 14,12A2,2 0 0,1 12,14A2,2 0 0,1 10,12A2,2 0 0,1 12,10Z"/>
        </svg>
      </div>
    </div>

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
  gap: 0.75rem;
  padding: 1rem;
  margin: 0.25rem 0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid transparent;
  position: relative;
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(5px);
  -webkit-backdrop-filter: blur(5px);
}

.conversation-item:hover {
  background: rgba(255, 255, 255, 0.8);
  border-color: rgba(79, 70, 229, 0.2);
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.conversation-item.active {
  background: linear-gradient(135deg, rgba(79, 70, 229, 0.1) 0%, rgba(124, 58, 237, 0.1) 100%);
  border-color: #4f46e5;
  box-shadow: 0 4px 20px rgba(79, 70, 229, 0.2);
}

.conversation-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  border-radius: 0 2px 2px 0;
}

.conversation-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(79, 70, 229, 0.3);
  transition: transform 0.2s ease;
}

.conversation-item:hover .conversation-avatar {
  transform: scale(1.05);
}

.avatar-icon {
  width: 20px;
  height: 20px;
  color: white;
}

.conversation-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.conversation-title {
  font-weight: 600;
  font-size: 0.95rem;
  color: #1f2937;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.3;
}

.conversation-preview {
  color: #6b7280;
  font-size: 0.8rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.3;
  opacity: 0.8;
}

.conversation-time {
  color: #9ca3af;
  font-size: 0.75rem;
  font-weight: 500;
}

.delete-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 8px;
  color: #9ca3af;
  transition: all 0.2s ease;
  opacity: 0;
  transform: scale(0.9);
  flex-shrink: 0;
}

.conversation-item:hover .delete-btn {
  opacity: 1;
  transform: scale(1);
}

.delete-btn:hover {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.delete-btn svg {
  width: 16px;
  height: 16px;
}

.conversation-item.active .delete-btn {
  opacity: 0.7;
}

.conversation-item.active:hover .delete-btn {
  opacity: 1;
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

/* 响应式设计 */
@media (max-width: 768px) {
  .title-text {
    font-size: 13px;
  }

  .preview-text {
    font-size: 11px;
  }

  .time-text {
    font-size: 10px;
  }
}
</style>
