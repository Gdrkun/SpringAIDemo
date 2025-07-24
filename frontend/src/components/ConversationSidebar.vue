<template>
  <aside class="conversation-sidebar" :class="{ collapsed: isCollapsed }">
    <!-- 侧边栏头部 -->
    <div class="sidebar-header">
      <button @click="toggleSidebar" class="toggle-btn">
        <svg viewBox="0 0 24 24" fill="currentColor">
          <path d="M3,6H21V8H3V6M3,11H21V13H3V11M3,16H21V18H3V16Z" />
        </svg>
      </button>

      <div v-if="!isCollapsed" class="header-content">
        <h3 class="sidebar-title">对话历史</h3>
        <button
          @click="refreshConversations"
          :disabled="refreshing"
          class="refresh-btn"
          :class="{ loading: refreshing }"
        >
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M17.65,6.35C16.2,4.9 14.21,4 12,4A8,8 0 0,0 4,12A8,8 0 0,0 12,20C15.73,20 18.84,17.45 19.73,14H17.65C16.83,16.33 14.61,18 12,18A6,6 0 0,1 6,12A6,6 0 0,1 12,6C13.66,6 15.14,6.69 16.22,7.78L13,11H20V4L17.65,6.35Z" />
          </svg>
        </button>
      </div>
    </div>

    <!-- 侧边栏内容 -->
    <div v-if="!isCollapsed" class="sidebar-content">
      <!-- 操作按钮 -->
      <div class="sidebar-actions">
        <button @click="startNewConversation" class="action-btn primary">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M19,13H13V19H11V13H5V11H11V5H13V11H19V13Z" />
          </svg>
          <span>新建对话</span>
        </button>

        <button @click="$emit('showFileManager')" class="action-btn secondary">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M13,9H18.5L13,3.5V9M6,2H14L20,8V20A2,2 0 0,1 18,22H6C4.89,22 4,21.1 4,20V4C4,2.89 4.89,2 6,2M15,18V16H6V18H15M18,14V12H6V14H18Z" />
          </svg>
          <span>知识库</span>
        </button>
      </div>

      <!-- 搜索框 -->
      <div class="search-container">
        <div class="search-input-wrapper">
          <svg class="search-icon" viewBox="0 0 24 24" fill="currentColor">
            <path d="M9.5,3A6.5,6.5 0 0,1 16,9.5C16,11.11 15.41,12.59 14.44,13.73L14.71,14H15.5L20.5,19L19,20.5L14,15.5V14.71L13.73,14.44C12.59,15.41 11.11,16 9.5,16A6.5,6.5 0 0,1 3,9.5A6.5,6.5 0 0,1 9.5,3M9.5,5C7,5 5,7 5,9.5C5,12 7,14 9.5,14C12,14 14,12 14,9.5C14,7 12,5 9.5,5Z" />
          </svg>
          <input
            v-model="searchQuery"
            placeholder="搜索对话..."
            class="search-input"
            @input="handleSearch"
          />
          <button
            v-if="searchQuery"
            @click="searchQuery = ''; handleSearch()"
            class="clear-btn"
          >
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M19,6.41L17.59,5L12,10.59L6.41,5L5,6.41L10.59,12L5,17.59L6.41,19L12,13.41L17.59,19L19,17.59L13.41,12L19,6.41Z" />
            </svg>
          </button>
        </div>
      </div>

      <!-- 对话列表 -->
      <div class="conversations-container">
        <div class="conversations-scrollbar" ref="scrollbarRef">
          <!-- 加载状态 -->
          <div v-if="loading" class="loading-container">
            <div class="skeleton-item" v-for="i in 5" :key="i">
              <div class="skeleton-avatar"></div>
              <div class="skeleton-content">
                <div class="skeleton-title"></div>
                <div class="skeleton-subtitle"></div>
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-else-if="filteredConversations.length === 0" class="empty-state">
            <div class="empty-icon">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M12,2A10,10 0 0,0 2,12A10,10 0 0,0 12,22A10,10 0 0,0 22,12A10,10 0 0,0 12,2M12,17A5,5 0 0,1 7,12A5,5 0 0,1 12,7A5,5 0 0,1 17,12A5,5 0 0,1 12,17Z" />
              </svg>
            </div>
            <p class="empty-text">
              {{ searchQuery ? '未找到相关对话' : '暂无对话记录' }}
            </p>
            <p class="empty-subtext">
              {{ searchQuery ? '尝试其他关键词' : '开始新的对话吧' }}
            </p>
          </div>

          <!-- 对话项列表 -->
          <div v-else class="conversations-list">
            <ConversationItem
              v-for="conversation in filteredConversations"
              :key="conversation.id"
              :conversation="conversation"
              :isActive="conversation.id === activeConversationId"
              @select="selectConversation"
              @delete="deleteConversation"
            />
          </div>

          <!-- 加载更多 -->
          <div v-if="hasMore && !loading && filteredConversations.length > 0" class="load-more-container">
            <button
              @click="loadMoreConversations"
              :disabled="loadingMore"
              class="load-more-btn"
            >
              <span v-if="!loadingMore">加载更多</span>
              <span v-else class="loading-text">
                <svg class="loading-spinner" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M12,4V2A10,10 0 0,0 2,12H4A8,8 0 0,1 12,4Z" />
                </svg>
                加载中...
              </span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
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
const refreshing = ref(false)
const loadingMore = ref(false)
const conversations = ref<Conversation[]>([])
const searchQuery = ref('')
const scrollbarRef = ref<any>(null)
const hasMore = ref(true)
const currentPage = ref(1)
const pageSize = 20

// 计算属性
const filteredConversations = computed(() => {
  if (!searchQuery.value.trim()) {
    return conversations.value
  }

  const query = searchQuery.value.toLowerCase()
  return conversations.value.filter(conversation =>
    conversation.title.toLowerCase().includes(query) ||
    conversation.lastMessage.toLowerCase().includes(query)
  )
})

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

const handleSearch = () => {
  // 搜索时重置到顶部
  if (scrollbarRef.value) {
    scrollbarRef.value.setScrollTop(0)
  }
}

// 简单的消息提示函数
const showMessage = (message: string, type: 'success' | 'error' = 'success') => {
  // 这里可以用简单的浏览器通知或者自定义提示组件
  console.log(`${type}: ${message}`)
}

const deleteConversation = async (conversationId: string) => {
  try {
    await chatAPI.deleteConversation(conversationId)
    await loadConversations()

    // 如果删除的是当前活跃对话，则开始新对话
    if (conversationId === props.activeConversationId) {
      emit('newConversation')
    }

    showMessage('对话删除成功')
  } catch (error) {
    console.error('删除对话失败:', error)
    showMessage('删除对话失败', 'error')
  }
}

const refreshConversations = async () => {
  refreshing.value = true

  try {
    currentPage.value = 1
    await loadConversations()
    showMessage('刷新成功')
  } catch (error) {
    console.error('刷新失败:', error)
    showMessage('刷新失败', 'error')
  } finally {
    refreshing.value = false
  }
}

const loadMoreConversations = async () => {
  if (loadingMore.value || !hasMore.value) return

  loadingMore.value = true
  try {
    currentPage.value++
    await loadConversations(false) // false 表示不清空现有数据
  } catch (error) {
    console.error('加载更多失败:', error)
    currentPage.value-- // 回退页码
  } finally {
    loadingMore.value = false
  }
}

const loadConversations = async (clearExisting = true) => {
  if (clearExisting) {
    loading.value = true
    conversations.value = []
  }

  try {
    const conversationList = await chatAPI.getConversations()

    // 为每个对话ID创建对话对象
    const newConversations = conversationList.map(item => {
      // 获取所有用户消息，按时间排序
      const userMessages = item.messages
        .filter(msg => msg.messageType === 'USER')
        .sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime())

      // 获取第一条用户消息作为标题
      const firstUserMessage = userMessages[0]

      // 获取最后一条用户消息作为预览（如果和第一条不同）
      const lastUserMessage = userMessages[userMessages.length - 1]

      // 获取最新的消息时间作为对话时间
      const latestMessage = item.messages
        .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())[0]

      // 生成对话标题：使用第一条用户消息的前30个字符
      const title = firstUserMessage
        ? (firstUserMessage.content.length > 30
           ? firstUserMessage.content.substring(0, 30) + '...'
           : firstUserMessage.content)
        : '新对话'

      // 预览消息：如果只有一条消息，不显示预览；如果有多条，显示最后一条
      const previewMessage = userMessages.length > 1 && lastUserMessage && lastUserMessage.id !== firstUserMessage.id
        ? (lastUserMessage.content.length > 50
           ? lastUserMessage.content.substring(0, 50) + '...'
           : lastUserMessage.content)
        : ''

      return {
        id: item.conversationId,
        title: title,
        lastMessage: previewMessage || '',
        lastMessageTime: latestMessage ? new Date(latestMessage.createdAt) : new Date()
      }
    })

    if (clearExisting) {
      conversations.value = newConversations
    } else {
      conversations.value = [...conversations.value, ...newConversations]
    }

    // 按时间倒序排列，最新的对话在前面
    conversations.value.sort((a, b) =>
      (b.lastMessageTime?.getTime() || 0) - (a.lastMessageTime?.getTime() || 0)
    )

    // 检查是否还有更多数据
    hasMore.value = newConversations.length >= pageSize

  } catch (error) {
    console.error('加载对话列表失败:', error)
    showMessage('加载对话列表失败', 'error')
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
  width: 320px;
  height: 100%;
  background: var(--bg-glass);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: var(--shadow-lg);
  position: relative;
  z-index: 5;
}

.conversation-sidebar.collapsed {
  width: 60px;
}

.sidebar-header {
  padding: 1.5rem 1rem 1rem;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  gap: 1rem;
  background: var(--bg-glass);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  transition: background 0.3s ease, border-color 0.3s ease;
}

.toggle-btn {
  background: var(--bg-glass);
  border: none;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  color: var(--primary-color);
  flex-shrink: 0;
}

.toggle-btn:hover {
  background: var(--bg-glass-hover);
  transform: translateY(-1px);
}

.toggle-btn svg {
  width: 20px;
  height: 20px;
}

.header-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.sidebar-title {
  margin: 0;
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.025em;
  transition: color 0.3s ease;
}

.refresh-btn {
  background: none;
  border: none;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #6b7280;
}

.refresh-btn:hover {
  background: rgba(107, 114, 128, 0.1);
  color: #374151;
}

.refresh-btn.loading svg {
  animation: spin 1s linear infinite;
}

.refresh-btn svg {
  width: 18px;
  height: 18px;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
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
  gap: 0.75rem;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.875rem 1rem;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-size: 0.95rem;
  font-weight: 600;
  transition: all 0.2s ease;
  text-align: left;
  width: 100%;
}

.action-btn.primary {
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
}

.action-btn.primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(79, 70, 229, 0.4);
}

.action-btn.secondary {
  background: rgba(107, 114, 128, 0.1);
  color: #374151;
}

.action-btn.secondary:hover {
  background: rgba(107, 114, 128, 0.2);
  transform: translateY(-1px);
}

.action-btn svg {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.search-container {
  padding: 0 1rem 1rem;
}

.search-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  background: rgba(243, 244, 246, 0.8);
  border: 1px solid rgba(209, 213, 219, 0.5);
  border-radius: 12px;
  transition: all 0.2s ease;
}

.search-input-wrapper:focus-within {
  background: white;
  border-color: #4f46e5;
  box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.1);
}

.search-icon {
  position: absolute;
  left: 12px;
  width: 18px;
  height: 18px;
  color: #9ca3af;
  pointer-events: none;
}

.search-input {
  width: 100%;
  padding: 0.75rem 0.75rem 0.75rem 2.5rem;
  border: none;
  background: transparent;
  font-size: 0.9rem;
  color: #374151;
  outline: none;
}

.search-input::placeholder {
  color: #9ca3af;
}

.clear-btn {
  position: absolute;
  right: 8px;
  background: none;
  border: none;
  width: 24px;
  height: 24px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #9ca3af;
  transition: all 0.2s ease;
}

.clear-btn:hover {
  background: rgba(107, 114, 128, 0.1);
  color: #6b7280;
}

.clear-btn svg {
  width: 14px;
  height: 14px;
}

.conversations-container {
  flex: 1;
  overflow: hidden;
  position: relative;
}

.conversations-scrollbar {
  height: 100%;
  overflow-y: auto;
  padding: 0 0.5rem;
}

.conversations-scrollbar::-webkit-scrollbar {
  width: 4px;
}

.conversations-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}

.conversations-scrollbar::-webkit-scrollbar-thumb {
  background: rgba(156, 163, 175, 0.3);
  border-radius: 2px;
}

.conversations-scrollbar::-webkit-scrollbar-thumb:hover {
  background: rgba(156, 163, 175, 0.5);
}

.loading-container {
  padding: 1rem 0.5rem;
}

.skeleton-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem;
  margin-bottom: 0.5rem;
}

.skeleton-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(90deg, #f3f4f6 25%, #e5e7eb 50%, #f3f4f6 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-content {
  flex: 1;
}

.skeleton-title {
  height: 16px;
  background: linear-gradient(90deg, #f3f4f6 25%, #e5e7eb 50%, #f3f4f6 75%);
  background-size: 200% 100%;
  border-radius: 4px;
  margin-bottom: 0.5rem;
  animation: shimmer 1.5s infinite;
}

.skeleton-subtitle {
  height: 12px;
  width: 70%;
  background: linear-gradient(90deg, #f3f4f6 25%, #e5e7eb 50%, #f3f4f6 75%);
  background-size: 200% 100%;
  border-radius: 4px;
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 1rem;
  text-align: center;
  color: #9ca3af;
}

.empty-icon {
  width: 64px;
  height: 64px;
  margin-bottom: 1rem;
  opacity: 0.5;
}

.empty-text {
  margin: 0 0 0.5rem 0;
  font-size: 1rem;
  font-weight: 600;
  color: #6b7280;
}

.empty-subtext {
  margin: 0;
  font-size: 0.875rem;
  color: #9ca3af;
}

.conversations-list {
  padding-bottom: 1rem;
}

.load-more-container {
  display: flex;
  justify-content: center;
  padding: 1rem;
}

.load-more-btn {
  background: rgba(79, 70, 229, 0.1);
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  color: #4f46e5;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.load-more-btn:hover:not(:disabled) {
  background: rgba(79, 70, 229, 0.2);
  transform: translateY(-1px);
}

.load-more-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.loading-text {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.loading-spinner {
  width: 16px;
  height: 16px;
  animation: spin 1s linear infinite;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .conversation-sidebar {
    width: 280px;
  }

  .conversation-sidebar.collapsed {
    width: 50px;
  }

  .sidebar-header {
    padding: 1rem 0.75rem 0.75rem;
  }

  .sidebar-actions {
    padding: 0.75rem;
  }

  .search-container {
    padding: 0 0.75rem 0.75rem;
  }
}
</style>
