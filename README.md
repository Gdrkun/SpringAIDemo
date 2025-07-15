# AI 智能对话系统

这是一个基于 Spring AI 和 Vue.js 的智能对话系统，提供了现代化的聊天界面和强大的AI对话功能。

## 🚀 功能特性

- **现代化UI设计**: 采用渐变背景、毛玻璃效果和流畅动画
- **TypeScript支持**: 完整的类型安全和智能提示
- **实时对话**: 支持与AI进行实时对话交流
- **智能建议**: 提供快速开始对话的建议选项
- **响应式设计**: 完美适配各种屏幕尺寸（手机、平板、桌面、4K、超宽屏）
- **优雅的消息气泡**: 区分用户和AI消息，带有SVG图标和时间戳
- **错误处理**: 完善的错误提示和处理机制
- **模块化架构**: 清晰的代码结构和可维护性

## 🛠️ 技术栈

### 后端
- **Spring Boot 3.3.1**: 主框架
- **Spring AI**: AI集成框架
- **OpenAI API**: AI模型接口（配置为Gemini）
- **Maven**: 依赖管理

### 前端
- **Vue.js 3**: 前端框架 (Composition API + TypeScript)
- **TypeScript**: 类型安全的JavaScript
- **Vite**: 构建工具
- **Axios**: HTTP客户端 (完整类型支持)
- **CSS3**: 现代样式（渐变、动画、毛玻璃效果）
- **响应式设计**: 完美适配各种屏幕尺寸

## 📦 项目结构

```
SpringAIDemo/
├── src/main/java/com/glk/SpringAIDemo/
│   ├── SpringAiDemoApplication.java     # 主应用类
│   ├── controller/
│   │   └── HelloController.java         # AI对话控制器
│   └── pojo/
│       ├── ActorsFilms.java            # 演员电影数据模型
│       └── StringInputRequest.java     # 字符串输入请求模型
├── src/main/resources/
│   └── application.yaml                # 应用配置
└── frontend/
    ├── src/
    │   ├── components/
    │   │   ├── ChatInterface.vue       # 聊天界面组件 (TypeScript)
    │   │   └── MessageBubble.vue       # 消息气泡组件 (TypeScript)
    │   ├── services/
    │   │   └── api.ts                  # API服务 (TypeScript封装)
    │   ├── types/
    │   │   └── index.ts                # TypeScript类型定义
    │   ├── styles/
    │   │   └── responsive.css          # 响应式样式配置
    │   ├── App.vue                     # 主应用组件 (TypeScript)
    │   ├── main.ts                     # 应用入口 (TypeScript)
    │   └── style.css                   # 全局样式
    ├── tsconfig.json                   # TypeScript配置
    ├── env.d.ts                        # 环境类型声明
    ├── package.json                    # 前端依赖配置
    ├── vite.config.js                  # Vite配置
    └── index.html                      # HTML入口
```

## 🚀 快速开始

### 1. 启动后端服务

```bash
# 在项目根目录下
./mvnw spring-boot:run
```

后端服务将在 `http://localhost:8086` 启动

### 2. 启动前端服务

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端服务将在 `http://localhost:3001` 启动

### 3. 访问应用

打开浏览器访问 `http://localhost:3001` 即可使用AI对话系统

## 🔧 配置说明

### AI模型配置

在 `src/main/resources/application.yaml` 中配置AI模型：

```yaml
spring:
  ai:
    openai:
      api-key: YOUR_API_KEY
      base-url: https://generativelanguage.googleapis.com/v1beta/openai
      chat:
        options:
          model: gemini-2.0-flash
```

### 前端代理配置

前端通过Vite代理转发API请求到后端，配置在 `frontend/vite.config.js`：

```javascript
server: {
  proxy: {
    '/hello': {
      target: 'http://localhost:8086',
      changeOrigin: true
    }
  }
}
```

## 📱 界面预览

- **欢迎界面**: 渐变背景配合动画效果，提供快速开始建议
- **聊天界面**: 现代化消息气泡设计，支持用户和AI消息区分
- **输入框**: 圆角设计配合发送按钮，支持回车发送
- **响应式**: 完美适配各种屏幕尺寸

## 🎨 设计特色

- **渐变背景**: 紫蓝色渐变营造科技感
- **毛玻璃效果**: 现代化的视觉层次
- **流畅动画**: 消息出现、按钮悬停等交互动画
- **优雅配色**: 精心调配的颜色方案
- **现代图标**: SVG图标替代emoji，更加专业

## 🔗 API接口

- `GET /hello/api/ask2?msg={message}`: 发送消息获取AI回复
- `GET /hello/api/stream?msg={message}`: 流式对话接口
- `GET /hello/api/films?actor={actor}`: 获取演员电影信息
- `GET /hello/api/image?prompt={prompt}`: 生成图片

## 📝 开发说明

### TypeScript开发

```bash
# 开发模式（自动类型检查）
npm run dev

# 构建（包含类型检查）
npm run build

# 手动类型检查（可选）
npm run type-check
```

### 添加新功能

1. **后端**：在 `HelloController` 中添加新的API端点
2. **类型定义**：在 `frontend/src/types/index.ts` 中添加相关类型
3. **API服务**：在 `frontend/src/services/api.ts` 中添加对应的API调用方法
4. **组件**：根据需要创建或修改Vue组件（使用TypeScript）

### 样式定制

- **全局样式**：修改 `frontend/src/style.css`
- **响应式样式**：修改 `frontend/src/styles/responsive.css`
- **组件样式**：在各组件的 `<style scoped>` 中修改
- **主题色彩**：主要在CSS变量和渐变中定义

### 响应式断点

- **手机**: < 640px
- **平板**: 640px - 767px
- **小桌面**: 768px - 1023px
- **桌面**: 1024px - 1279px
- **2K屏**: 1280px - 1535px
- **4K屏**: 1536px - 1919px
- **超宽屏**: ≥ 1920px

## 🤝 贡献

欢迎提交Issue和Pull Request来改进这个项目！

## 📄 许可证

MIT License
