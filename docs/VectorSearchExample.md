# 向量搜索使用示例

## 概述

本文档展示如何使用知识文件管理系统的向量搜索功能，实现基于语义的文档检索和问答。

## 基本流程

### 1. 上传并向量化文件

```bash
# 上传文件并自动向量化
curl -X POST "http://localhost:8080/api/knowledge-files/upload" \
  -F "file=@knowledge_base.txt" \
  -F "description=知识库文档" \
  -F "enableVectorization=true"
```

### 2. 查看向量化状态

```bash
# 获取文件信息，查看向量化状态
curl -X GET "http://localhost:8080/api/knowledge-files/1"
```

响应中的 `vectorizationStatus` 字段表示向量化状态：
- 0: 未向量化
- 1: 向量化中
- 2: 向量化成功
- 3: 向量化失败

### 3. 进行向量搜索

```bash
# 搜索相关文档
curl -X GET "http://localhost:8080/api/knowledge-files/search-similar?query=人工智能的应用&topK=3&threshold=0.7"
```

## 支持的文件格式

当前支持以下文件格式的自动文本提取和向量化：

- **纯文本文件**: `.txt`, `.md`
- **标记语言**: `.html`, `.xml`
- **数据格式**: `.json`

## 文档分割策略

系统会自动将长文档分割成适合向量化的片段：

- **默认片段大小**: 1000 字符
- **片段重叠**: 200 字符
- **智能分割**: 尽量在句子边界分割
- **元数据保留**: 每个片段保留原文件信息

## 向量搜索参数

### topK
返回最相关的文档片段数量，建议值：
- 快速查询: 3-5
- 详细分析: 5-10
- 全面搜索: 10-20

### threshold
相似度阈值，范围 0.0-1.0：
- 0.9+: 非常相似，适合精确匹配
- 0.7-0.9: 相关性较高，适合一般查询
- 0.5-0.7: 相关性中等，适合探索性搜索
- 0.3-0.5: 相关性较低，适合广泛搜索

## 实际应用场景

### 1. 知识库问答

```javascript
// 前端示例：智能问答
async function askQuestion(question) {
    // 1. 搜索相关文档
    const searchResponse = await fetch(`/api/knowledge-files/search-similar?query=${encodeURIComponent(question)}&topK=5&threshold=0.7`);
    const searchResult = await searchResponse.json();
    
    if (searchResult.code === 200 && searchResult.data.length > 0) {
        // 2. 构建上下文
        const context = searchResult.data.map(doc => doc.text).join('\n\n');
        
        // 3. 调用 AI 模型进行问答
        const chatResponse = await fetch('/hello/api/chatMemory', {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                conversationId: 'knowledge-qa',
                inputMsg: `基于以下上下文回答问题：\n\n上下文：\n${context}\n\n问题：${question}`
            })
        });
        
        return await chatResponse.text();
    }
    
    return '抱歉，没有找到相关信息。';
}
```

### 2. 文档相似性分析

```bash
# 查找与特定主题相关的所有文档
curl -X GET "http://localhost:8080/api/knowledge-files/search-similar?query=机器学习算法&topK=10&threshold=0.6"
```

### 3. 内容推荐

```bash
# 基于用户兴趣推荐相关内容
curl -X GET "http://localhost:8080/api/knowledge-files/search-similar?query=用户历史浏览内容&topK=5&threshold=0.8"
```

## 性能优化建议

### 1. 文件上传优化
- 批量上传时建议分批处理
- 大文件建议先上传，后异步向量化
- 定期清理失败的向量化任务

### 2. 搜索优化
- 合理设置 topK 和 threshold 参数
- 使用缓存减少重复查询
- 对于频繁查询，考虑预计算结果

### 3. 存储优化
- 定期清理无用的向量数据
- 监控向量数据库的存储使用情况
- 考虑向量数据的备份策略

## 故障排除

### 1. 向量化失败
- 检查文件格式是否支持
- 确认文件内容不为空
- 查看日志了解具体错误信息

### 2. 搜索结果不准确
- 调整相似度阈值
- 检查查询语句的表达方式
- 确认向量化是否成功完成

### 3. 性能问题
- 检查向量数据库连接状态
- 监控系统资源使用情况
- 考虑优化查询参数

## 最佳实践

1. **文档质量**: 确保上传的文档内容质量高、结构清晰
2. **描述信息**: 为文件添加详细的描述信息，便于管理
3. **定期维护**: 定期清理过期或无用的文档
4. **监控状态**: 关注向量化状态，及时处理失败的任务
5. **测试查询**: 定期测试搜索功能，确保结果质量

## 集成示例

### 与聊天系统集成

系统已经集成了 `QuestionAnswerAdvisor`，在使用聊天接口时会自动进行向量搜索增强：

```bash
# 使用带记忆的聊天接口，自动进行 RAG 增强
curl -X GET "http://localhost:8080/hello/api/chatMemory?conversationId=test&inputMsg=请介绍一下人工智能的发展历程"
```

这样，AI 助手会自动搜索相关文档，并基于搜索结果提供更准确、更详细的回答。
