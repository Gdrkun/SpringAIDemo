<template>
  <div id="app">
    <header class="app-header">
      <h1>🤖 AI 智能对话助手</h1>
      <p>基于 Spring AI 的智能对话系统</p>
    </header>
    <main class="app-main">
      <div class="main-content" v-if="!showFileManager">
        <ConversationSidebar
          ref="sidebarRef"
          :activeConversationId="currentConversationId"
          @selectConversation="selectConversation"
          @newConversation="startNewConversation"
          @showFileManager="showFileManager = true"
        />
        <ChatInterface
          :conversationId="currentConversationId"
          @conversationCreated="handleConversationCreated"
        />
      </div>
      <KnowledgeFileManagement v-else @backToChat="showFileManager = false" />
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import ChatInterface from './components/ChatInterface.vue'
import ConversationSidebar from './components/ConversationSidebar.vue'
import KnowledgeFileManagement from './components/KnowledgeFileManagement.vue';

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
</script>

<style scoped>
.app-header {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  color: white;
  text-align: center;
  padding: 2.5rem 2rem;
  box-shadow: 0 8px 32px rgba(0,0,0,0.1);
  border-bottom: 1px solid rgba(255,255,255,0.2);
}

.app-header h1 {
  margin: 0;
  font-size: 3rem;
  font-weight: 700;
  background: linear-gradient(135deg, #ffffff 0%, #f1f5f9 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.app-header p {
  margin: 1rem 0 0 0;
  opacity: 0.95;
  font-size: 1.2rem;
  font-weight: 500;
  text-shadow: 0 1px 2px rgba(0,0,0,0.1);
}

.app-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  position: relative;
}

.main-content {
  flex: 1;
  display: flex;
  height: 100%;
}

.app-main::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="25" cy="25" r="1" fill="rgba(255,255,255,0.1)"/><circle cx="75" cy="75" r="1" fill="rgba(255,255,255,0.1)"/><circle cx="50" cy="10" r="0.5" fill="rgba(255,255,255,0.05)"/><circle cx="20" cy="80" r="0.5" fill="rgba(255,255,255,0.05)"/></pattern></defs><rect width="100" height="100" fill="url(%23grain)"/></svg>');
  pointer-events: none;
  opacity: 0.3;
}

#app {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

#app::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 1px, transparent 1px);
  background-size: 50px 50px;
  animation: float 20s ease-in-out infinite;
  pointer-events: none;
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
    padding: 1.5rem 1rem;
  }

  .app-header h1 {
    font-size: 2rem;
  }

  .app-header p {
    font-size: 1rem;
  }

  .app-main {
    padding: 1rem;
  }
}
</style>
