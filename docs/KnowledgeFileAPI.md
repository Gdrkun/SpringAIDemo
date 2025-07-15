# 知识文件管理 API 文档

## 概述

知识文件管理模块提供了完整的文件上传、下载、查询、删除功能，支持文件去重、分页查询等特性。**新增向量化功能**，支持将文件内容向量化存储到向量数据库中，实现基于语义的文档搜索和检索增强生成 (RAG)。

## 数据库表结构

首先需要执行以下 SQL 创建数据库表：

```sql
-- 执行 src/main/resources/sql/knowledge_file.sql 中的 SQL 语句
```

## API 接口

### 1. 文件上传

**接口地址：** `POST /api/knowledge-files/upload`

**请求参数：**
- `file` (MultipartFile, 必需): 上传的文件
- `description` (String, 可选): 文件描述
- `enableVectorization` (Boolean, 可选, 默认true): 是否启用向量化

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "fileName": "example.pdf",
    "fileType": "application/pdf",
    "fileSize": 1024000,
    "fileSuffix": ".pdf",
    "description": "示例文档",
    "status": 1,
    "createdAt": "2025-07-11T10:30:00",
    "isDuplicate": false,
    "message": "文件上传成功"
  }
}
```

**curl 示例：**
```bash
curl -X POST "http://localhost:8080/api/knowledge-files/upload" \
  -F "file=@/path/to/your/file.pdf" \
  -F "description=测试文档" \
  -F "enableVectorization=true"
```

### 2. 文件下载

**接口地址：** `GET /api/knowledge-files/download/{id}`

**路径参数：**
- `id` (Long): 文件ID

**响应：** 直接返回文件流

**curl 示例：**
```bash
curl -X GET "http://localhost:8080/api/knowledge-files/download/1" \
  --output downloaded_file.pdf
```

### 3. 获取文件信息

**接口地址：** `GET /api/knowledge-files/{id}`

**路径参数：**
- `id` (Long): 文件ID

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "fileName": "example.pdf",
    "filePath": "./uploads/uuid-filename.pdf",
    "fileType": "application/pdf",
    "fileSize": 1024000,
    "fileHash": "md5hash",
    "fileSuffix": ".pdf",
    "description": "示例文档",
    "status": 1,
    "createdAt": "2025-07-11T10:30:00",
    "updatedAt": "2025-07-11T10:30:00"
  }
}
```

### 4. 分页查询文件列表

**接口地址：** `GET /api/knowledge-files/list`

**查询参数：**
- `current` (Integer, 默认1): 当前页码
- `size` (Integer, 默认10): 每页大小
- `fileName` (String, 可选): 文件名模糊查询
- `fileType` (String, 可选): 文件类型精确查询

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [...],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  }
}
```

### 5. 文件名搜索

**接口地址：** `GET /api/knowledge-files/search`

**查询参数：**
- `fileName` (String, 必需): 搜索的文件名

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "fileName": "example.pdf",
      ...
    }
  ]
}
```

### 6. 按文件类型查询

**接口地址：** `GET /api/knowledge-files/by-type`

**查询参数：**
- `fileType` (String, 必需): 文件类型 (MIME类型)

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [...]
}
```

### 7. 删除文件

**接口地址：** `DELETE /api/knowledge-files/{id}`

**路径参数：**
- `id` (Long): 文件ID

**响应示例：**
```json
{
  "code": 200,
  "message": "文件删除成功",
  "data": null
}
```

### 8. 批量删除文件

**接口地址：** `DELETE /api/knowledge-files/batch`

**请求体：**
```json
[1, 2, 3, 4, 5]
```

**响应示例：**
```json
{
  "code": 200,
  "message": "批量删除完成，成功删除 5 个文件",
  "data": null
}
```

### 9. 更新文件描述

**接口地址：** `PUT /api/knowledge-files/{id}/description`

**路径参数：**
- `id` (Long): 文件ID

**查询参数：**
- `description` (String): 新的文件描述

**响应示例：**
```json
{
  "code": 200,
  "message": "描述更新成功",
  "data": {
    "id": 1,
    "description": "更新后的描述",
    ...
  }
}
```

### 10. 对已存在文件进行向量化

**接口地址：** `POST /api/knowledge-files/{id}/vectorize`

**路径参数：**
- `id` (Long): 文件ID

**响应示例：**
```json
{
  "code": 200,
  "message": "文件向量化成功",
  "data": null
}
```

### 11. 向量搜索相似文档

**接口地址：** `GET /api/knowledge-files/search-similar`

**查询参数：**
- `query` (String, 必需): 搜索查询
- `topK` (Integer, 默认5): 返回结果数量
- `threshold` (Float, 默认0.7): 相似度阈值

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "text": "文档内容片段...",
      "metadata": {
        "fileId": 1,
        "fileName": "example.txt",
        "chunkIndex": 0,
        "totalChunks": 3
      }
    }
  ]
}
```

### 12. 批量向量化文件

**接口地址：** `POST /api/knowledge-files/batch-vectorize`

**请求体：**
```json
[1, 2, 3, 4, 5]
```

**响应示例：**
```json
{
  "code": 200,
  "message": "批量向量化完成，成功处理 5 个文件",
  "data": null
}
```

## 特性说明

### 1. 文件去重
- 基于 MD5 哈希值进行文件去重
- 相同内容的文件只会存储一份
- 重复上传时返回已存在的文件信息

### 2. 文件存储
- 文件存储在配置的 `file.upload.path` 目录下
- 使用 UUID 生成唯一文件名避免冲突
- 保留原始文件名和后缀信息

### 3. 安全性
- 支持文件类型验证
- 文件大小限制 (默认100MB)
- 路径遍历攻击防护

### 4. 向量化功能
- 自动文档内容提取和分割
- 支持多种文件格式 (txt, md, html, json, xml)
- 智能文本分块，保持语义完整性
- 向量化状态跟踪和管理

### 5. 语义搜索
- 基于向量相似度的文档检索
- 支持自然语言查询
- 可配置的相似度阈值和返回数量
- 检索增强生成 (RAG) 支持

### 6. 错误处理
- 统一的错误响应格式
- 详细的错误信息记录
- 优雅的异常处理

## 配置说明

在 `application.yaml` 中可以配置：

```yaml
file:
  upload:
    path: ./uploads  # 文件上传路径
    max-file-size: 100MB  # 单个文件最大大小
    max-request-size: 100MB  # 请求最大大小
```

## 使用示例

### JavaScript 前端上传示例

```javascript
const uploadFile = async (file, description) => {
  const formData = new FormData();
  formData.append('file', file);
  if (description) {
    formData.append('description', description);
  }

  try {
    const response = await fetch('/api/knowledge-files/upload', {
      method: 'POST',
      body: formData
    });
    
    const result = await response.json();
    console.log('上传结果:', result);
    return result;
  } catch (error) {
    console.error('上传失败:', error);
    throw error;
  }
};
```

### Java 客户端调用示例

```java
// 使用 RestTemplate 或 WebClient 调用接口
// 具体实现可参考测试用例
```

## 快速开始

### 1. 数据库准备

执行以下 SQL 创建数据库表：

```sql
-- 执行 src/main/resources/sql/knowledge_file.sql 中的 SQL 语句
```

### 2. 启动应用

```bash
mvn spring-boot:run
```

### 3. 测试接口

访问测试页面：`http://localhost:8080/file-upload-test.html`

或使用 Postman/curl 测试 API 接口。

### 4. 目录结构

```
src/main/java/com/glk/SpringAIDemo/
├── controller/
│   └── KnowledgeFileController.java     # 文件管理控制器
├── service/
│   ├── KnowledgeFileService.java        # 文件服务接口
│   └── impl/
│       └── KnowledgeFileServiceImpl.java # 文件服务实现
├── mapper/
│   └── KnowledgeFileMapper.java         # MyBatis Mapper
├── entity/
│   └── KnowledgeFileEntity.java         # 文件实体类
├── pojo/
│   ├── ApiResponse.java                 # 通用响应类
│   └── FileUploadResponse.java          # 文件上传响应类
├── config/
│   └── FileUploadConfig.java            # 文件上传配置
└── exception/
    └── GlobalExceptionHandler.java      # 全局异常处理
```

## 注意事项

1. 确保 `file.upload.path` 目录有写权限
2. 根据需要调整文件大小限制
3. 生产环境建议配置文件存储到云存储服务
4. 建议定期清理无用的文件
5. 可以根据业务需求扩展文件分类、标签等功能
