// 消息类型
export interface Message {
  id: number
  text: string
  isUser: boolean
  timestamp: Date
  isError?: boolean
  isStreaming?: boolean; // 新增字段
}

// 聊天建议类型
export interface ChatSuggestion {
  icon: string
  text: string
  message: string
}

// 对话相关类型
export interface Conversation {
  id: string
  title: string
  lastMessage?: string
  lastMessageTime?: Date
  messageCount?: number
}

// 后端消息格式
export interface BackendMessage {
  messageType: string
  text: string
  metadata: Record<string, any>
}

// 对话摘要信息
export interface ConversationSummary {
  conversationId: string
  title: string
  lastMessage: string
  lastMessageTime: string
  messageCount: number
}

// 知识文件类型
export interface KnowledgeFile {
  id: number
  fileName: string
  fileType: string
  fileSize: number
  fileSuffix: string
  description: string
  status: string
  createdAt: string
  isDuplicate?: boolean
  message?: string
}
