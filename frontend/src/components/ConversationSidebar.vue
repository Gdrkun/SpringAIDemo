<template>
  <div class="conversation-sidebar" :class="{ collapsed: isCollapsed }">
    <div class="sidebar-header">
      <button @click="toggleSidebar" class="toggle-btn">
        <svg v-if="!isCollapsed" viewBox="0 0 24 24" fill="currentColor">
          <path d="M3,6H21V8H3V6M3,11H21V13H3V11M3,16H21V18H3V16Z" />
        </svg>
        <svg v-else viewBox="0 0 24 24" fill="currentColor">
          <path d="M3,6H21V8H3V6M3,11H21V13H3V11M3,16H21V18H3V16Z" />
        </svg>
      </button>
      <h3 v-if="!isCollapsed">对话历史</h3>
    </div>

    <div v-if="!isCollapsed" class="sidebar-content">
      <div class="sidebar-actions">
        <button @click="startNewConversation" class="new-conversation-btn">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M19,13H13V19H11V13H5V11H11V5H13V11H19V13Z" />
          </svg>
          <span v-if="!isCollapsed">新建对话</span>
        </button>
        <button @click="$emit('showFileManager')" class="file-manager-btn">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M16.5,9L13.5,12L16.5,15H22V9M9,16.5L12,13.5L15,16.5V22H9M7.5,9L10.5,12L7.5,15H2V9M15,7.5L12,10.5L9,7.5V2H15V7.5Z" />
          </svg>
          <span v-if="!isCollapsed">知识库</span>
        </button>
      </div>

      <div class="conversations-list">
        <div v-if="loading" class="loading">
          <div class="loading-spinner"></div>
          <span>加载中...</span>
        </div>
        
        <div v-else-if="conversations.length === 0" class="empty-state">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M12,2A10,10 0 0,0 2,12A10,10 0 0,0 12,22A10,10 0 0,0 22,12A10,10 0 0,0 12,2M12,17A5,5 0 0,1 7,12A5,5 0 0,1 12,7A5,5 0 0,1 17,12A5,5 0 0,1 12,17Z" />
          </svg>
          <p>暂无对话记录</p>
        </div>

        <ConversationItem
          v-else
          v-for="conversation in conversations"
          :key="conversation.id"
          :conversation="conversation"
          :isActive="conversation.id === activeConversationId"
          @select="selectConversation"
          @delete="deleteConversation"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import ConversationItem from './ConversationItem.vue'
import { chatAPI } from '@/services/api'
import type { Conversation } from '@/types'

// Props
interface Props {
  activeConversationId?: string
}

const props = withDefaults(defineProps<Props>(), {
  activeConversationId: undefined
})

// Emits
const emit = defineEmits<{
  selectConversation: [conversationId: string]
  newConversation: []
}>()

// 响应式数据
const isCollapsed = ref(false)
const loading = ref(false)
const conversations = ref<Conversation[]>([])

// 方法
const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

const startNewConversation = () => {
  emit('newConversation')
}

const selectConversation = (conversationId: string) => {
  emit('selectConversation', conversationId)
}

const deleteConversation = async (conversationId: string) => {
  try {
    await chatAPI.deleteConversation(conversationId)
    await loadConversations()
    
    // 如果删除的是当前活跃对话，则开始新对话
    if (conversationId === props.activeConversationId) {
      emit('newConversation')
    }
  } catch (error) {
    console.error('删除对话失败:', error)
  }
}

const loadConversations = async () => {
  loading.value = true
  try {
    const conversationIds = await chatAPI.getConversations()
    
    // 为每个对话ID创建对话对象
    conversations.value = conversationIds.map(id => ({
      id,
      title: `对话 ${id.substring(0, 8)}...`,
      lastMessage: '',
      lastMessageTime: new Date()
    }))
    
    // 可以进一步获取每个对话的详细信息
    // 这里为了性能考虑，暂时只显示对话ID
    
  } catch (error) {
    console.error('加载对话列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 生命周期
onMounted(() => {
  loadConversations()
})

// 暴露方法给父组件
defineExpose({
  loadConversations
})
</script>

<style scoped>
.conversation-sidebar {
  width: 300px;
  height: 100%;
  background: white;
  border-right: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  box-shadow: 2px 0 8px rgba(0,0,0,0.1);
}

.conversation-sidebar.collapsed {
  width: 60px;
}

.sidebar-header {
  padding: 1rem;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  background: #f8fafc;
}

.toggle-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 0.375rem;
  color: #64748b;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.toggle-btn:hover {
  background: #e2e8f0;
  color: #334155;
}

.toggle-btn svg {
  width: 20px;
  height: 20px;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: #334155;
}

.sidebar-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.sidebar-actions {
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.new-conversation-btn, .file-manager-btn {
  width: 100%;
  padding: 0.75rem 1rem;
  border: none;
  border-radius: 0.5rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-weight: 500;
  transition: all 0.2s;
  font-size: 0.95rem;
}

.new-conversation-btn {
  background: #3b82f6;
  color: white;
}

.new-conversation-btn:hover {
  background: #2563eb;
}

.file-manager-btn {
  background: #f1f5f9;
  color: #475569;
}

.file-manager-btn:hover {
  background: #e2e8f0;
}

.new-conversation-btn svg, .file-manager-btn svg {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.conversations-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 0.5rem;
}

.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 2rem;
  color: #64748b;
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid #e2e8f0;
  border-top: 2px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 1rem;
  color: #94a3b8;
  text-align: center;
}

.empty-state svg {
  width: 48px;
  height: 48px;
  margin-bottom: 1rem;
  opacity: 0.5;
}

.empty-state p {
  margin: 0;
  font-size: 0.9rem;
}

/* 滚动条样式 */
.conversations-list::-webkit-scrollbar {
  width: 6px;
}

.conversations-list::-webkit-scrollbar-track {
  background: #f1f5f9;
}

.conversations-list::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

.conversations-list::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .conversation-sidebar {
    width: 280px;
  }
  
  .conversation-sidebar.collapsed {
    width: 50px;
  }
}
</style>
