<template>
  <div id="app" class="app-container">
    <!-- 顶部导航栏 -->
    <header class="app-header">
      <div class="header-content">
        <div class="logo-section">
          <div class="logo-icon">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M12,2A2,2 0 0,1 14,4C14,4.74 13.6,5.39 13,5.73V7A1,1 0 0,0 14,8H18A1,1 0 0,0 19,7V5.73C18.4,5.39 18,4.74 18,4A2,2 0 0,1 20,2A2,2 0 0,1 22,4C22,4.74 21.6,5.39 21,5.73V7A3,3 0 0,1 18,10H14A3,3 0 0,1 11,7V5.73C10.4,5.39 10,4.74 10,4A2,2 0 0,1 12,2M7,10A2,2 0 0,1 9,12A2,2 0 0,1 7,14A2,2 0 0,1 5,12A2,2 0 0,1 7,10M17,10A2,2 0 0,1 19,12A2,2 0 0,1 17,14A2,2 0 0,1 15,12A2,2 0 0,1 17,10M12,10A2,2 0 0,1 14,12A2,2 0 0,1 12,14A2,2 0 0,1 10,12A2,2 0 0,1 12,10Z"/>
            </svg>
          </div>
          <div class="logo-text">
            <h1>AI Assistant</h1>
            <span>智能对话助手</span>
          </div>
        </div>

        <div class="header-actions">
          <div class="theme-toggle-container">
            <span class="theme-label">{{ isDark ? '亮色' : '暗色' }}</span>
            <button class="theme-toggle-btn" @click="toggleTheme" :title="isDark ? '切换到亮色主题' : '切换到暗色主题'">
              <div class="toggle-track" :class="{ 'active': isDark }">
                <div class="toggle-thumb" :class="{ 'active': isDark }">
                  <!-- 亮色主题图标 (太阳) -->
                  <svg v-if="!isDark" viewBox="0 0 24 24" fill="currentColor" class="theme-icon sun-icon">
                    <path d="M12,8A4,4 0 0,0 8,12A4,4 0 0,0 12,16A4,4 0 0,0 16,12A4,4 0 0,0 12,8M12,18A6,6 0 0,1 6,12A6,6 0 0,1 12,6A6,6 0 0,1 18,12A6,6 0 0,1 12,18M20,8.69V4H15.31L12,0.69L8.69,4H4V8.69L0.69,12L4,15.31V20H8.69L12,23.31L15.31,20H20V15.31L23.31,12L20,8.69Z"/>
                  </svg>
                  <!-- 暗色主题图标 (月亮) -->
                  <svg v-else viewBox="0 0 24 24" fill="currentColor" class="theme-icon moon-icon">
                    <path d="M17.75,4.09L15.22,6.03L16.13,9.09L13.5,7.28L10.87,9.09L11.78,6.03L9.25,4.09L12.44,4L13.5,1L14.56,4L17.75,4.09M21.25,11L19.61,12.25L20.2,14.23L18.5,13.06L16.8,14.23L17.39,12.25L15.75,11L17.81,10.95L18.5,9L19.19,10.95L21.25,11M18.97,15.95C19.8,15.87 20.69,17.05 20.16,17.8C19.84,18.25 19.5,18.67 19.08,19.07C15.17,23 8.84,23 4.94,19.07C1.03,15.17 1.03,8.83 4.94,4.93C5.34,4.53 5.76,4.17 6.21,3.85C6.96,3.32 8.14,4.21 8.06,5.04C7.79,7.9 8.75,10.87 10.95,13.06C13.14,15.26 16.1,16.22 18.97,15.95M17.33,17.97C14.5,17.81 11.7,16.64 9.53,14.5C7.36,12.31 6.2,9.5 6.04,6.68C3.23,9.82 3.34,14.4 6.35,17.41C9.37,20.43 14,20.54 17.33,17.97Z"/>
                  </svg>
                </div>
              </div>
            </button>
          </div>
        </div>
      </div>
    </header>

    <!-- 主要内容区域 -->
    <main class="app-main">
      <div v-if="!showFileManager" class="main-layout">
        <ConversationSidebar
          ref="sidebarRef"
          :activeConversationId="currentConversationId"
          @selectConversation="selectConversation"
          @newConversation="startNewConversation"
          @showFileManager="showFileManager = true"
        />
        <div class="chat-container">
          <ChatInterface
            :conversationId="currentConversationId"
            @conversationCreated="handleConversationCreated"
          />
        </div>
      </div>
      <div v-else class="file-manager-container">
        <KnowledgeFileManagement @backToChat="showFileManager = false" />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import ChatInterface from './components/ChatInterface.vue'
import ConversationSidebar from './components/ConversationSidebar.vue'
import KnowledgeFileManagement from './components/KnowledgeFileManagement.vue'
import { useTheme } from './composables/useTheme'

// 主题管理
const { isDark, toggleTheme: switchTheme } = useTheme()

// 响应式数据
const currentConversationId = ref<string | undefined>(undefined)
const sidebarRef = ref<InstanceType<typeof ConversationSidebar> | null>(null)
const showFileManager = ref(false);

// 方法
const selectConversation = (conversationId: string) => {
  currentConversationId.value = conversationId
}

const startNewConversation = () => {
  currentConversationId.value = undefined
}

const handleConversationCreated = (conversationId: string) => {
  currentConversationId.value = conversationId
  // 刷新侧边栏对话列表
  sidebarRef.value?.loadConversations()
}

const toggleTheme = () => {
  switchTheme()
}
</script>

<style scoped>
/* CSS 变量现在由 useTheme composable 管理 */

.app-container {
  height: 100vh;
  background: var(--bg-gradient);
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: background 0.3s ease;
}

.app-header {
  background: var(--bg-glass);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid var(--border-color);
  box-shadow: var(--shadow-lg);
  padding: 1rem 2rem;
  z-index: 10;
  transition: background 0.3s ease, border-color 0.3s ease;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1400px;
  margin: 0 auto;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.logo-icon {
  width: 40px;
  height: 40px;
  color: white;
  animation: pulse 2s infinite;
}

.logo-text {
  display: flex;
  flex-direction: column;
}

.logo-text h1 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-inverse);
  line-height: 1.2;
}

.logo-text span {
  font-size: 0.9rem;
  color: var(--text-tertiary);
}

.header-actions {
  display: flex;
  gap: 1rem;
}

/* 主题切换容器 */
.theme-toggle-container {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  background: var(--bg-glass);
  padding: 0.5rem 1rem;
  border-radius: 2rem;
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;
}

.theme-toggle-container:hover {
  background: var(--bg-glass-hover);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.theme-label {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-inverse);
  opacity: 0.9;
  transition: color 0.3s ease;
  min-width: 2rem;
  text-align: center;
}

/* 切换按钮 */
.theme-toggle-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
  position: relative;
}

.toggle-track {
  width: 3.5rem;
  height: 1.75rem;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 1rem;
  position: relative;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.toggle-track.active {
  background: var(--primary-color);
  border-color: var(--primary-color);
}

.toggle-thumb {
  width: 1.5rem;
  height: 1.5rem;
  background: white;
  border-radius: 50%;
  position: absolute;
  top: 50%;
  left: 0.125rem;
  transform: translateY(-50%);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.toggle-thumb.active {
  left: 1.875rem;
  background: var(--bg-primary);
}

.theme-icon {
  width: 1rem;
  height: 1rem;
  transition: all 0.3s ease;
}

.sun-icon {
  color: #f59e0b;
}

.moon-icon {
  color: var(--primary-color);
}

.theme-toggle-btn:hover .toggle-thumb {
  transform: translateY(-50%) scale(1.1);
}

.theme-toggle-btn:hover .theme-icon {
  transform: rotate(180deg);
}

.action-btn svg {
  width: 20px;
  height: 20px;
}

.app-main {
  flex: 1;
  overflow: hidden;
  position: relative;
}

.main-layout {
  display: flex;
  height: 100%;
}

.chat-container {
  flex: 1;
  height: 100%;
  overflow: hidden;
  position: relative;
}

.file-manager-container {
  height: 100%;
  overflow: hidden;
}

.app-container::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 1px, transparent 1px);
  background-size: 50px 50px;
  animation: float 20s ease-in-out infinite;
  pointer-events: none;
  z-index: 0;
}

.app-container::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="25" cy="25" r="1" fill="rgba(255,255,255,0.1)"/><circle cx="75" cy="75" r="1" fill="rgba(255,255,255,0.1)"/><circle cx="50" cy="10" r="0.5" fill="rgba(255,255,255,0.05)"/><circle cx="20" cy="80" r="0.5" fill="rgba(255,255,255,0.05)"/></pattern></defs><rect width="100" height="100" fill="url(%23grain)"/></svg>');
  pointer-events: none;
  opacity: 0.3;
  z-index: 0;
}

.app-header,
.app-main {
  position: relative;
  z-index: 1;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) rotate(0deg);
  }
  33% {
    transform: translate(30px, -30px) rotate(120deg);
  }
  66% {
    transform: translate(-20px, 20px) rotate(240deg);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-header {
    padding: 1rem;
  }

  .header-content {
    flex-direction: row;
    gap: 1rem;
  }

  .logo-text h1 {
    font-size: 1.25rem;
  }

  .logo-text span {
    font-size: 0.8rem;
  }

  .main-layout {
    flex-direction: column;
  }

  .conversation-sidebar {
    width: 100% !important;
    height: auto;
    max-height: 40vh;
    border-right: none;
    border-bottom: 1px solid rgba(0, 0, 0, 0.1);
  }

  .conversation-sidebar.collapsed {
    height: 60px;
    max-height: 60px;
  }

  .chat-container {
    flex: 1;
    height: 60vh;
  }
}

@media (max-width: 480px) {
  .app-header {
    padding: 0.75rem;
  }

  .logo-section {
    gap: 0.75rem;
  }

  .logo-icon {
    width: 32px;
    height: 32px;
  }

  .theme-toggle-container {
    padding: 0.375rem 0.75rem;
    gap: 0.5rem;
  }

  .theme-label {
    font-size: 0.75rem;
    min-width: 1.5rem;
  }

  .toggle-track {
    width: 3rem;
    height: 1.5rem;
  }

  .toggle-thumb {
    width: 1.25rem;
    height: 1.25rem;
  }

  .toggle-thumb.active {
    left: 1.625rem;
  }

  .theme-icon {
    width: 0.875rem;
    height: 0.875rem;
  }
}
</style>
