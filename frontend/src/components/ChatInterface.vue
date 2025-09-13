<template>
  <div class="chat-interface">
    <div class="chat-messages-container">
      <div class="chat-messages" ref="messagesContainer">
        <div v-if="messages.length === 0" class="welcome-message">
          <div class="welcome-icon">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M12,2A2,2 0 0,1 14,4C14,4.74 13.6,5.39 13,5.73V7A1,1 0 0,0 14,8H18A1,1 0 0,0 19,7V5.73C18.4,5.39 18,4.74 18,4A2,2 0 0,1 20,2A2,2 0 0,1 22,4C22,4.74 21.6,5.39 21,5.73V7A3,3 0 0,1 18,10H14A3,3 0 0,1 11,7V5.73C10.4,5.39 10,4.74 10,4A2,2 0 0,1 12,2M7,10A2,2 0 0,1 9,12A2,2 0 0,1 7,14A2,2 0 0,1 5,12A2,2 0 0,1 7,10M17,10A2,2 0 0,1 19,12A2,2 0 0,1 17,14A2,2 0 0,1 15,12A2,2 0 0,1 17,10M12,10A2,2 0 0,1 14,12A2,2 0 0,1 12,14A2,2 0 0,1 10,12A2,2 0 0,1 12,10Z"/>
            </svg>
          </div>
          <h3>欢迎使用AI智能助手</h3>
          <p>我是基于Spring AI的智能对话助手，有什么可以帮助您的吗？</p>
          <div class="welcome-suggestions">
            <div class="suggestion-card" @click="sendSuggestion('你好，请介绍一下自己')">
              <div class="suggestion-emoji">👋</div>
              <div class="suggestion-text">打个招呼</div>
            </div>
            <div class="suggestion-card" @click="sendSuggestion('请帮我写一首关于春天的诗')">
              <div class="suggestion-emoji">🌸</div>
              <div class="suggestion-text">写首诗</div>
            </div>
            <div class="suggestion-card" @click="sendSuggestion('解释一下人工智能的原理')">
              <div class="suggestion-emoji">🧠</div>
              <div class="suggestion-text">学习AI</div>
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
          <div class="typing-avatar">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M12,2A2,2 0 0,1 14,4C14,4.74 13.6,5.39 13,5.73V7A1,1 0 0,0 14,8H18A1,1 0 0,0 19,7V5.73C18.4,5.39 18,4.74 18,4A2,2 0 0,1 20,2A2,2 0 0,1 22,4C22,4.74 21.6,5.39 21,5.73V7A3,3 0 0,1 18,10H14A3,3 0 0,1 11,7V5.73C10.4,5.39 10,4.74 10,4A2,2 0 0,1 12,2M7,10A2,2 0 0,1 9,12A2,2 0 0,1 7,14A2,2 0 0,1 5,12A2,2 0 0,1 7,10M17,10A2,2 0 0,1 19,12A2,2 0 0,1 17,14A2,2 0 0,1 15,12A2,2 0 0,1 17,10M12,10A2,2 0 0,1 14,12A2,2 0 0,1 12,14A2,2 0 0,1 10,12A2,2 0 0,1 12,10Z"/>
            </svg>
          </div>
          <div class="typing-content">
            <div class="typing-dots">
              <span></span>
              <span></span>
              <span></span>
            </div>
            <span class="typing-text">AI正在思考中...</span>
          </div>
        </div>
      </div>
    </div>

    <div class="chat-input-container">
      <div class="input-wrapper">
        <div class="input-field">
          <textarea
              v-model="currentMessage"
              @keydown="handleKeyDown"
              placeholder="输入您的问题..."
              class="chat-input"
              rows="1"
              :disabled="isLoading"
          ></textarea>
        </div>
        <button
            @click="sendMessage"
            :disabled="!currentMessage.trim() || isLoading"
            class="send-button"
            :class="{ loading: isLoading }"
        >
          <svg v-if="!isLoading" viewBox="0 0 24 24" fill="currentColor">
            <path d="M2,21L23,12L2,3V10L17,12L2,14V21Z" />
          </svg>
          <svg v-else class="loading-spinner" viewBox="0 0 24 24" fill="currentColor">
            <path d="M12,4V2A10,10 0 0,0 2,12H4A8,8 0 0,1 12,4Z" />
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, watch } from 'vue';
import MessageBubble from './MessageBubble.vue';
import { chatAPI } from '@/services/api';
import type { Message, BackendMessage } from '@/types';

// Props 和 Emits 保持不变
interface Props {
  conversationId?: string;
}
const props = defineProps<Props>();
const emit = defineEmits<{
  conversationCreated: [conversationId: string];
}>();

// --- 状态定义 ---
const messages = ref<Message[]>([]);
const currentMessage = ref<string>('');
const isLoading = ref<boolean>(false);
const messageIdCounter = ref<number>(1);
const messagesContainer = ref<HTMLElement | null>(null);
const currentConversationId = ref<string | undefined>(props.conversationId);
const isProcessingNewConversation = ref(false);

const scrollToBottom = (): void => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
    }
  });
};

const sendMessage = async (): Promise<void> => {
  if (!currentMessage.value.trim() || isLoading.value) return;

  const messageText = currentMessage.value.trim();

  // 1. 显示用户消息
  messages.value.push({
    id: messageIdCounter.value++,
    text: messageText,
    isUser: true,
    timestamp: new Date(),
    isStreaming: false,
  });
  currentMessage.value = '';

  // 2. 显示“正在思考”提示
  isLoading.value = true;
  scrollToBottom();

  try {
    // 3. 处理新对话ID
    if (!currentConversationId.value) {
      isProcessingNewConversation.value = true;
      const newId = generateConversationId();
      currentConversationId.value = newId;
      emit('conversationCreated', newId);
    }

    // 4. 准备一个变量来持有AI消息对象
    let aiMessage: Message | null = null;

    // 5. 调用流式API
    await chatAPI.sendMessageWithConversationStream(
        currentConversationId.value!,
        messageText,
        (chunk) => {
          if (!aiMessage) {
            isLoading.value = false;
            aiMessage = {
              id: messageIdCounter.value++,
              text: '',
              isUser: false,
              timestamp: new Date(),
              isStreaming: true,
            };
            messages.value.push(aiMessage);
          }
          const updatedAiMessage = {
            ...aiMessage,
            text: aiMessage.text + chunk,
            isStreaming: true,
          };
          const index = messages.value.findIndex(m => m.id === aiMessage.id);
          if (index !== -1) {
            messages.value[index] = updatedAiMessage;
          }
          aiMessage = updatedAiMessage;

          console.log('Received chunk:', chunk);
          console.log('Current AI message text:', aiMessage.text);
          scrollToBottom();
        }
    );

    // 6. 流结束后，标记为非流式
    if (aiMessage) {
      const index = messages.value.findIndex(m => m.id === aiMessage.id);
      if (index !== -1) {
        messages.value[index].isStreaming = false;
      }
    }
  } catch (error) {
    console.error('发送消息失败:', error);
    // 移除正在流式传输的消息（如果存在）
    if (messages.value.length > 0 && messages.value[messages.value.length - 1].isStreaming) {
      messages.value.pop();
    }
    messages.value.push({
      id: messageIdCounter.value++,
      text: '抱歉，发生了错误，请稍后重试。',
      isUser: false,
      timestamp: new Date(),
      isError: true,
      isStreaming: false,
    });
  } finally {
    isLoading.value = false;
    isProcessingNewConversation.value = false;
  }
};

const handleKeyDown = (event: KeyboardEvent): void => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault();
    sendMessage();
  }
};

const sendSuggestion = (suggestion: string): void => {
  currentMessage.value = suggestion;
  sendMessage();
};

const generateConversationId = (): string => `conv_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;

const loadHistoryMessages = async (conversationId: string): Promise<void> => {
  try {
    const backendMessages = await chatAPI.getConversationMessages(conversationId);

    const cleanedMessages = backendMessages.map((msg: BackendMessage) => {
      let cleanedText = msg.text;
      if (msg.messageType.toLowerCase() === 'assistant' && cleanedText.includes('data:')) {
        cleanedText = cleanedText
            .split('\n')
            .map(line => {
                if (line.startsWith('data:')) {
                    return line.substring(5).trim();
                }
                return line.trim();
            })
            .join('\n');
      }
      return {
        ...msg,
        text: cleanedText,
      };
    });

    messages.value = cleanedMessages.map((msg: BackendMessage, index: number) => ({
      id: index,
      text: msg.text,
      isUser: msg.messageType.toLowerCase() === 'user',
      timestamp: new Date(),
      isError: false,
      isStreaming: false, // 历史消息无需流式处理
    }));

    messageIdCounter.value = messages.value.length;
    scrollToBottom();
  } catch (error) {
    console.error('加载历史消息失败:', error);
    messages.value = [];
  }
};

const clearCurrentConversation = (): void => {
  messages.value = [];
  messageIdCounter.value = 1;
  currentConversationId.value = undefined;
};

// --- 监听器 ---
watch(
    () => props.conversationId,
    async (newConversationId) => {
      if (isProcessingNewConversation.value) {
        return;
      }
      currentConversationId.value = newConversationId;
      if (newConversationId) {
        await loadHistoryMessages(newConversationId);
      } else {
        clearCurrentConversation();
      }
    },
    { immediate: true }
);
</script>

<style scoped>
.chat-interface {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--bg-secondary);
  position: relative;
  transition: background 0.3s ease;
}

.chat-messages-container {
  flex: 1;
  overflow: hidden;
  position: relative;
}

.chat-messages {
  height: 100%;
  overflow-y: auto;
  padding: 2rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  scroll-behavior: smooth;
}

.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: transparent;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: var(--scrollbar-thumb);
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: var(--scrollbar-thumb-hover);
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
  color: var(--text-secondary);
  max-width: 800px;
  margin: 0 auto;
  transition: color 0.3s ease;
}

.welcome-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 2rem;
  background: var(--message-user-bg);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-inverse);
  box-shadow: var(--shadow-lg);
  animation: pulse 2s infinite;
  transition: background 0.3s ease, box-shadow 0.3s ease;
}

.welcome-icon svg {
  width: 40px;
  height: 40px;
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
  color: var(--text-primary);
  font-size: 2rem;
  font-weight: 700;
  letter-spacing: -0.025em;
  transition: color 0.3s ease;
}

.welcome-message p {
  margin-bottom: 3rem;
  font-size: 1.1rem;
  line-height: 1.6;
  color: var(--text-secondary);
  transition: color 0.3s ease;
}

.welcome-suggestions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  max-width: 600px;
  margin: 0 auto;
}

.suggestion-card {
  background: var(--bg-glass);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  padding: 1.5rem;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
  box-shadow: var(--shadow-md);
}

.suggestion-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
  border-color: var(--primary-color);
  background: var(--bg-glass-hover);
}

.suggestion-emoji {
  font-size: 2rem;
  line-height: 1;
}

.suggestion-text {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 0.95rem;
  text-align: center;
  transition: color 0.3s ease;
}

.typing-indicator {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  align-self: flex-start;
  max-width: 75%;
}

.typing-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(79, 70, 229, 0.3);
}

.typing-avatar svg {
  width: 20px;
  height: 20px;
  color: white;
}

.typing-content {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 20px;
  padding: 1rem 1.5rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.typing-dots {
  display: flex;
  gap: 4px;
  align-items: center;
}

.typing-dots span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
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
  color: #6b7280;
  font-size: 0.875rem;
  font-weight: 500;
}

.chat-input-container {
  padding: 1.5rem 2rem;
  border-top: 1px solid rgba(0, 0, 0, 0.1);
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.input-wrapper {
  display: flex;
  gap: 1rem;
  align-items: flex-end;
  max-width: 1000px;
  margin: 0 auto;
  background: white;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 24px;
  padding: 0.75rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.input-wrapper:focus-within {
  border-color: #4f46e5;
  box-shadow: 0 4px 20px rgba(79, 70, 229, 0.2);
}

.input-field {
  flex: 1;
}

.chat-input {
  width: 100%;
  min-height: 44px;
  max-height: 120px;
  resize: none;
  border: none;
  background: transparent;
  padding: 0.75rem 1rem;
  font-size: 1rem;
  font-family: inherit;
  line-height: 1.5;
  color: #374151;
  outline: none;
}

.chat-input::placeholder {
  color: #9ca3af;
}

.chat-input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.send-button {
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  border: none;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  color: white;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
  flex-shrink: 0;
}

.send-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(79, 70, 229, 0.4);
}

.send-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.2);
}

.send-button svg {
  width: 20px;
  height: 20px;
}

.send-button.loading svg {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-messages {
    padding: 1rem;
  }

  .welcome-message {
    padding: 2rem 1rem;
  }

  .welcome-message h3 {
    font-size: 1.5rem;
  }

  .welcome-suggestions {
    grid-template-columns: 1fr;
    gap: 0.75rem;
  }

  .chat-input-container {
    padding: 1rem;
  }

  .input-wrapper {
    gap: 0.75rem;
    border-radius: 20px;
    padding: 0.5rem;
  }

  .send-button {
    width: 40px;
    height: 40px;
  }

  .send-button svg {
    width: 18px;
    height: 18px;
  }

  .message-bubble {
    max-width: 90%;
  }

  .message-avatar {
    width: 36px;
    height: 36px;
  }

  .message-avatar svg {
    width: 18px;
    height: 18px;
  }

  .typing-avatar {
    width: 36px;
    height: 36px;
  }

  .typing-avatar svg {
    width: 18px;
    height: 18px;
  }
}

@media (max-width: 480px) {
  .chat-messages {
    padding: 0.75rem;
    gap: 1rem;
  }

  .welcome-message {
    padding: 1.5rem 0.75rem;
  }

  .welcome-icon {
    width: 60px;
    height: 60px;
  }

  .welcome-icon svg {
    width: 30px;
    height: 30px;
  }

  .welcome-message h3 {
    font-size: 1.25rem;
  }

  .welcome-message p {
    font-size: 1rem;
  }

  .suggestion-card {
    padding: 1rem;
  }

  .suggestion-emoji {
    font-size: 1.5rem;
  }

  .suggestion-text {
    font-size: 0.875rem;
  }

  .chat-input-container {
    padding: 0.75rem;
  }

  .input-wrapper {
    border-radius: 16px;
    padding: 0.5rem;
  }

  .chat-input {
    padding: 0.5rem 0.75rem;
    font-size: 0.9rem;
  }

  .send-button {
    width: 36px;
    height: 36px;
  }

  .send-button svg {
    width: 16px;
    height: 16px;
  }
}

/* 动画效果 */
.fade-in {
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
