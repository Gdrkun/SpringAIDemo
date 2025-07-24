import axios from 'axios'

// 自定义错误类
class ApiErrorClass extends Error {
  code?: number
  status?: number

  constructor(message: string, code?: number, status?: number) {
    super(message)
    this.name = 'ApiError'
    this.code = code
    this.status = status
  }
}

// 创建axios实例
const apiClient = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
apiClient.interceptors.request.use(
  (config) => {
    console.log('发送请求:', config)
    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
apiClient.interceptors.response.use(
  (response) => {
    console.log('收到响应:', response)
    return response
  },
  (error) => {
    console.error('响应错误:', error)

    // 处理不同类型的错误
    if (error.response) {
      // 服务器返回错误状态码
      const { status, data } = error.response
      switch (status) {
        case 400:
          throw new ApiErrorClass('请求参数错误', 400, status)
        case 401:
          throw new ApiErrorClass('未授权访问', 401, status)
        case 403:
          throw new ApiErrorClass('禁止访问', 403, status)
        case 404:
          throw new ApiErrorClass('接口不存在', 404, status)
        case 500:
          throw new ApiErrorClass('服务器内部错误', 500, status)
        default:
          throw new ApiErrorClass(data?.message || `请求失败 (${status})`, data?.code, status)
      }
    } else if (error.request) {
      // 网络错误
      throw new ApiErrorClass('网络连接失败，请检查网络设置')
    } else {
      // 其他错误
      throw new ApiErrorClass(error.message || '未知错误')
    }
  }
)

// API服务方法
export const chatAPI = {
  // 发送普通消息
  sendMessage: async (message: string): Promise<string> => {
    try {
      const response = await apiClient.get('/chat/ask2', {
        params: { msg: message }
      })
      return response.data
    } catch (error) {
      console.error('发送消息失败:', error)
      throw error
    }
  },

  // 发送带对话ID的消息
  sendMessageWithConversation: async (conversationId: string, message: string): Promise<string> => {
    try {
      const response = await apiClient.get('/chat/chatMemory', {
        params: {
          conversationId: conversationId,
          inputMsg: message
        }
      })
      return response.data
    } catch (error) {
      console.error('发送对话消息失败:', error)
      throw error
    }
  },

  // 获取所有对话列表
  getConversations: async (): Promise<Array<{conversationId: string, messages: any[]}>> => {
    try {
      const response = await apiClient.get('/chat/conversations')
      return response.data
    } catch (error) {
      console.error('获取对话列表失败:', error)
      throw error
    }
  },

  // 获取指定对话的历史消息
  getConversationMessages: async (conversationId: string): Promise<any[]> => {
    try {
      const response = await apiClient.get(`/chat/conversations/${conversationId}/messages`)
      return response.data
    } catch (error) {
      console.error('获取对话历史失败:', error)
      throw error
    }
  },

  // 删除指定对话
  deleteConversation: async (conversationId: string): Promise<void> => {
    try {
      await apiClient.delete(`/chat/conversations/${conversationId}`)
    } catch (error) {
      console.error('删除对话失败:', error)
      throw error
    }
  },

  // 发送流式消息 (暂时使用普通接口，后续可以实现SSE)
  sendStreamMessage: async (message: string): Promise<string> => {
    try {
      // 这里可以实现Server-Sent Events (SSE)
      // 目前先使用普通接口
      return await chatAPI.sendMessage(message)
    } catch (error) {
      console.error('发送流式消息失败:', error)
      throw error
    }
  },

  // 获取演员电影信息
  getActorFilms: async (actor: string): Promise<string> => {
    try {
      const response = await apiClient.get('/chat/films', {
        params: { actor }
      })
      return response.data
    } catch (error) {
      console.error('获取演员信息失败:', error)
      throw error
    }
  },

  // 生成图片
  generateImage: async (prompt: string): Promise<string> => {
    try {
      const response = await apiClient.get('/chat/image', {
        params: { prompt }
      })
      return response.data
    } catch (error) {
      console.error('生成图片失败:', error)
      throw error
    }
  },

  // 健康检查
  healthCheck: async (): Promise<boolean> => {
    try {
      await apiClient.get('/chat/health')
      return true
    } catch (error) {
      return false
    }
  }
}

export const knowledgeFileAPI = {
  // 获取知识文件列表
  getKnowledgeFiles: async (current: number = 1, size: number = 10, fileName?: string, fileType?: string) => {
    try {
      const response = await apiClient.get('/files/list', {
        params: { current, size, fileName, fileType },
      });
      return response.data.data; // The actual data is in response.data.data
    } catch (error) {
      console.error('获取文件列表失败:', error);
      throw error;
    }
  },

  // 上传文件
  uploadKnowledgeFile: async (file: File, description?: string, enableVectorization: boolean = true) => {
    const formData = new FormData();
    formData.append('file', file);
    if (description) {
      formData.append('description', description);
    }
    formData.append('enableVectorization', String(enableVectorization));

    try {
      const response = await apiClient.post('/files/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        },
      });
      return response.data.data;
    } catch (error) {
      console.error('上传文件失败:', error);
      throw error;
    }
  },

  // 删除文件
  deleteKnowledgeFile: async (id: number) => {
    try {
      const response = await apiClient.delete(`/files/${id}`);
      return response.data;
    } catch (error) {
      console.error(`删除文件 ${id} 失败:`, error);
      throw error;
    }
  },
  
  // 批量删除文件
  batchDeleteKnowledgeFiles: async (ids: number[]) => {
    try {
      const response = await apiClient.delete('/files/batch', {
        data: ids,
      });
      return response.data;
    } catch (error) {
      console.error('批量删除文件失败:', error);
      throw error;
    }
  },
  
  // 更新文件描述
  updateFileDescription: async (id: number, description: string) => {
    try {
      const response = await apiClient.put(`/files/${id}/description`, null, {
        params: { description },
      });
      return response.data.data;
    } catch (error) {
      console.error(`更新文件 ${id} 描述失败:`, error);
      throw error;
    }
  },

  // 向量化文件
  vectorizeFile: async (id: number) => {
    try {
      const response = await apiClient.post(`/files/${id}/vectorize`, null);
      return response.data;
    } catch (error) {
      console.error(`向量化文件 ${id} 失败:`, error);
      throw error;
    }
  },

  // 批量向量化文件
  batchVectorizeFiles: async (ids: number[]) => {
    try {
      const response = await apiClient.post('/files/batch-vectorize', ids);
      return response.data;
    } catch (error) {
      console.error('批量向量化文件失败:', error);
      throw error;
    }
  }
};


export default apiClient
