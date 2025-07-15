<template>
  <div class="chat-interface">
    <div class="chat-messages" ref="messagesContainer">
      <div v-if="messages.length === 0" class="welcome-message">
        <div class="welcome-icon">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 2C13.1 2 14 2.9 14 4C14 5.1 13.1 6 12 6C10.9 6 10 5.1 10 4C10 2.9 10.9 2 12 2ZM21 9V7L15 1L9 7V9C9 10.1 9.9 11 11 11V14L13 16L15 14V11C16.1 11 17 10.1 17 9M11 22A2 2 0 0 1 9 20A2 2 0 0 1 11 18A2 2 0 0 1 13 20A2 2 0 0 1 11 22Z"/>
          </svg>
        </div>
        <h3>欢迎使用AI智能助手</h3>
        <p>我是基于Spring AI的智能对话助手，有什么可以帮助您的吗？</p>
        <div class="welcome-suggestions">
          <div class="suggestion-card" @click="sendSuggestion('你好，请介绍一下自己')">
            <span>👋</span>
            <span>打个招呼</span>
          </div>
          <div class="suggestion-card" @click="sendSuggestion('请帮我写一首关于春天的诗')">
            <span>🌸</span>
            <span>写首诗</span>
          </div>
          <div class="suggestion-card" @click="sendSuggestion('解释一下人工智能的原理')">
            <span>🧠</span>
            <span>学习AI</span>
          </div>
        </div>
      </div>
      
      <MessageBubble
        v-for="message in messages"
        :key="message.id"
        :message="message"
        class="fade-in"
      />
      
      <div v-if="isLoading" class="typing-indicator">
        <div class="typing-dots">
          <span></span>
          <span></span>
          <span></span>
        </div>
        <span class="typing-text">AI正在思考中...</span>
      </div>
    </div>
    
    <div class="chat-input-container">
      <div class="input-wrapper">
        <textarea
          v-model="currentMessage"
          @keydown="handleKeyDown"
          placeholder="输入您的问题..."
          class="chat-input"
          rows="1"
          :disabled="isLoading"
        ></textarea>
        <button
          @click="sendMessage"
          :disabled="!currentMessage.trim() || isLoading"
          class="send-button btn btn-primary"
        >
          <span v-if="!isLoading">发送</span>
          <span v-else>发送中...</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, watch } from 'vue'
import MessageBubble from './MessageBubble.vue'
import { chatAPI } from '@/services/api'
import type { Message, BackendMessage } from '@/types'

// Props
interface Props {
  conversationId?: string
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  conversationCreated: [conversationId: string]
}>()

// 响应式数据
const messages = ref<Message[]>([])
const currentMessage = ref<string>('')
const isLoading = ref<boolean>(false)
const messageIdCounter = ref<number>(1)
const messagesContainer = ref<HTMLElement | null>(null)
const currentConversationId = ref<string | undefined>(props.conversationId)


// 方法
const sendMessage = async (): Promise<void> => {
  if (!currentMessage.value.trim() || isLoading.value) return

  const userMessage: Message = {
    id: messageIdCounter.value++,
    text: currentMessage.value.trim(),
    isUser: true,
    timestamp: new Date()
  }

  messages.value.push(userMessage)
  const messageText = currentMessage.value
  currentMessage.value = ''
  isLoading.value = true

  await nextTick(() => {
    scrollToBottom()
  })

  try {
    let response: string

    if (currentConversationId.value) {
      // 使用现有对话ID发送消息
      response = await chatAPI.sendMessageWithConversation(currentConversationId.value, messageText)
    } else {
      // 创建新对话
      const newConversationId = generateConversationId()
      currentConversationId.value = newConversationId
      response = await chatAPI.sendMessageWithConversation(newConversationId, messageText)
      emit('conversationCreated', newConversationId)
    }

    const aiMessage: Message = {
      id: messageIdCounter.value++,
      text: response,
      isUser: false,
      timestamp: new Date()
    }

    messages.value.push(aiMessage)
  } catch (error) {
    console.error('发送消息失败:', error)

    const errorMessage: Message = {
      id: messageIdCounter.value++,
      text: '抱歉，发生了错误，请稍后重试。',
      isUser: false,
      timestamp: new Date(),
      isError: true
    }

    messages.value.push(errorMessage)
  } finally {
    isLoading.value = false
    await nextTick(() => {
      scrollToBottom()
    })
  }
}

const handleKeyDown = (event: KeyboardEvent): void => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendMessage()
  }
}

const scrollToBottom = (): void => {
  const container = messagesContainer.value
  if (container) {
    container.scrollTop = container.scrollHeight
  }
}

const sendSuggestion = (suggestion: string): void => {
  currentMessage.value = suggestion
  sendMessage()
}

// 生成对话ID
const generateConversationId = (): string => {
  return `conv_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
}

// 加载历史消息
const loadHistoryMessages = async (conversationId: string): Promise<void> => {
  try {
    const backendMessages = await chatAPI.getConversationMessages(conversationId)

    // 转换后端消息格式为前端格式
    const convertedMessages: Message[] = backendMessages.map((msg: BackendMessage) => ({
      id: messageIdCounter.value++,
      text: msg.text,
      isUser: msg.messageType.toLowerCase() === 'user',
      timestamp: new Date(), // 这里可以从metadata中获取真实时间戳
      isError: false
    }))

    messages.value = convertedMessages
    messageIdCounter.value = convertedMessages.length + 1

    await nextTick(() => {
      scrollToBottom()
    })
  } catch (error) {
    console.error('加载历史消息失败:', error)
    messages.value = []
  }
}

// 清空当前对话
const clearCurrentConversation = (): void => {
  messages.value = []
  messageIdCounter.value = 1
  currentConversationId.value = undefined
}

// 监听conversationId变化
watch(() => props.conversationId, async (newConversationId) => {
  currentConversationId.value = newConversationId

  if (newConversationId) {
    await loadHistoryMessages(newConversationId)
  } else {
    clearCurrentConversation()
  }
}, { immediate: true })
</script>

<style scoped>
.chat-interface {
  display: flex;
  flex-direction: column;
  height: 100%;
  flex: 1;
  background: white;
  overflow: hidden;
}

/* 大屏幕适配 */
@media (min-width: 1440px) {
  .chat-interface {
    max-width: 1400px;
    height: calc(100vh - 180px);
  }
}

@media (min-width: 1920px) {
  .chat-interface {
    max-width: 1600px;
    height: calc(100vh - 200px);
  }
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 2rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  background: linear-gradient(135deg, #fafbfc 0%, #f8fafc 100%);
}

/* 大屏幕消息区域优化 */
@media (min-width: 1200px) {
  .chat-messages {
    padding: 3rem 4rem;
  }
}

@media (min-width: 1440px) {
  .chat-messages {
    padding: 3rem 6rem;
  }
}

.welcome-message {
  text-align: center;
  padding: 4rem 2rem;
  color: #64748b;
}

.welcome-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 2rem;
  background: linear-gradient(135deg, #06b6d4 0%, #3b82f6 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 8px 32px rgba(59, 130, 246, 0.3);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

.welcome-message h3 {
  margin-bottom: 1rem;
  color: #1e293b;
  font-size: 1.5rem;
  font-weight: 600;
}

.welcome-message p {
  margin-bottom: 2rem;
  font-size: 1.1rem;
  line-height: 1.6;
}

.welcome-suggestions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
  margin-top: 2rem;
}

.suggestion-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  padding: 1.5rem 1rem;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #e2e8f0;
  min-width: 120px;
}

.suggestion-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 32px rgba(0,0,0,0.15);
  border-color: #3b82f6;
}

.suggestion-card span:first-child {
  font-size: 1.5rem;
}

.suggestion-card span:last-child {
  font-size: 0.9rem;
  color: #64748b;
  font-weight: 500;
}

.typing-indicator {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1.25rem 1.5rem;
  background: white;
  border-radius: 20px;
  align-self: flex-start;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  border: 1px solid #e2e8f0;
  margin-bottom: 1rem;
}

.typing-dots {
  display: flex;
  gap: 6px;
}

.typing-dots span {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: linear-gradient(135deg, #06b6d4 0%, #3b82f6 100%);
  animation: typing 1.4s infinite ease-in-out;
}

.typing-dots span:nth-child(1) { animation-delay: -0.32s; }
.typing-dots span:nth-child(2) { animation-delay: -0.16s; }

@keyframes typing {
  0%, 80%, 100% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  40% {
    transform: scale(1.2);
    opacity: 1;
  }
}

.typing-text {
  font-size: 0.95rem;
  color: #64748b;
  font-weight: 500;
}

.chat-input-container {
  padding: 2rem;
  border-top: 1px solid #e2e8f0;
  background: white;
}

.input-wrapper {
  display: flex;
  gap: 1rem;
  align-items: flex-end;
  background: #f8fafc;
  border-radius: 24px;
  padding: 0.75rem;
  border: 2px solid #e2e8f0;
  transition: all 0.3s ease;
}

.input-wrapper:focus-within {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.chat-input {
  flex: 1;
  min-height: 44px;
  max-height: 120px;
  resize: none;
  border: none;
  background: transparent;
  padding: 0.75rem 1rem;
  font-size: 1rem;
  font-family: inherit;
  line-height: 1.5;
}

.chat-input:focus {
  outline: none;
}

.chat-input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.chat-input::placeholder {
  color: #94a3b8;
}

.send-button {
  height: 48px;
  padding: 0 2rem;
  white-space: nowrap;
  border-radius: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  transition: all 0.3s ease;
}

.send-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(79, 70, 229, 0.4);
}

.send-button:disabled {
  opacity: 0.5;
  transform: none;
  box-shadow: none;
}
</style>
