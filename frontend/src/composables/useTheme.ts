import { ref, readonly, onMounted } from 'vue'

export type Theme = 'light' | 'dark'

// 主题状态
const currentTheme = ref<Theme>('light')
const isDark = ref(false)

// 主题配置
const themes = {
  light: {
    // 主要颜色 - 现代蓝绿色系
    '--primary-color': '#0ea5e9',
    '--primary-light': '#38bdf8',
    '--primary-dark': '#0284c7',
    '--secondary-color': '#06b6d4',
    '--accent-color': '#f97316',

    // 文本颜色 - 优雅的深色系
    '--text-primary': '#0f172a',
    '--text-secondary': '#475569',
    '--text-tertiary': '#94a3b8',
    '--text-inverse': '#ffffff',

    // 背景颜色 - 清新的白色系
    '--bg-primary': '#ffffff',
    '--bg-secondary': '#f8fafc',
    '--bg-tertiary': '#f1f5f9',
    '--bg-quaternary': '#e2e8f0',
    '--bg-gradient': 'linear-gradient(135deg, #0ea5e9 0%, #06b6d4 50%, #8b5cf6 100%)',
    '--bg-glass': 'rgba(255, 255, 255, 0.8)',
    '--bg-glass-hover': 'rgba(255, 255, 255, 0.9)',

    // 边框颜色 - 柔和的灰色系
    '--border-color': '#e2e8f0',
    '--border-light': '#f1f5f9',
    '--border-dark': '#cbd5e1',

    // 阴影 - 更柔和的阴影
    '--shadow-sm': '0 1px 2px 0 rgba(0, 0, 0, 0.05)',
    '--shadow-md': '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)',
    '--shadow-lg': '0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)',
    '--shadow-xl': '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)',

    // 消息气泡 - 现代渐变设计
    '--message-user-bg': 'linear-gradient(135deg, #0ea5e9 0%, #06b6d4 100%)',
    '--message-user-card-bg': 'rgba(14, 165, 233, 0.1)',
    '--message-user-text': '#ffffff',
    '--message-user-card-text': '#0ea5e9',
    '--message-ai-bg': '#f8fafc',
    '--message-ai-text': '#1e293b',
    '--message-ai-border': '#e2e8f0',

    // 滚动条 - 与主题色调和
    '--scrollbar-thumb': 'rgba(14, 165, 233, 0.2)',
    '--scrollbar-thumb-hover': 'rgba(14, 165, 233, 0.4)',
  },
  dark: {
    // 主要颜色 - 现代紫蓝色系
    '--primary-color': '#3b82f6',
    '--primary-light': '#60a5fa',
    '--primary-dark': '#2563eb',
    '--secondary-color': '#10b981',
    '--accent-color': '#f59e0b',

    // 文本颜色 - 柔和的亮色系
    '--text-primary': '#f8fafc',
    '--text-secondary': '#cbd5e1',
    '--text-tertiary': '#94a3b8',
    '--text-inverse': '#0f172a',

    // 背景颜色 - 深邃的蓝灰色系
    '--bg-primary': '#0f172a',
    '--bg-secondary': '#1e293b',
    '--bg-tertiary': '#334155',
    '--bg-quaternary': '#475569',
    '--bg-gradient': 'linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #312e81 100%)',
    '--bg-glass': 'rgba(15, 23, 42, 0.8)',
    '--bg-glass-hover': 'rgba(15, 23, 42, 0.9)',

    // 边框颜色 - 深色边框系
    '--border-color': '#334155',
    '--border-light': '#475569',
    '--border-dark': '#1e293b',

    // 阴影 - 深色阴影
    '--shadow-sm': '0 1px 2px 0 rgba(0, 0, 0, 0.4)',
    '--shadow-md': '0 4px 6px -1px rgba(0, 0, 0, 0.5), 0 2px 4px -1px rgba(0, 0, 0, 0.4)',
    '--shadow-lg': '0 10px 15px -3px rgba(0, 0, 0, 0.5), 0 4px 6px -2px rgba(0, 0, 0, 0.4)',
    '--shadow-xl': '0 20px 25px -5px rgba(0, 0, 0, 0.6), 0 10px 10px -5px rgba(0, 0, 0, 0.5)',

    // 消息气泡 - 优雅的深色设计
    '--message-user-bg': 'linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%)',
    '--message-user-card-bg': 'rgba(59, 130, 246, 0.15)',
    '--message-user-text': '#ffffff',
    '--message-user-card-text': '#60a5fa',
    '--message-ai-bg': '#1e293b',
    '--message-ai-text': '#e2e8f0',
    '--message-ai-border': '#334155',

    // 滚动条 - 与主题协调
    '--scrollbar-thumb': 'rgba(59, 130, 246, 0.3)',
    '--scrollbar-thumb-hover': 'rgba(59, 130, 246, 0.5)',
  }
}

// 应用主题到 DOM
const applyTheme = (theme: Theme) => {
  const root = document.documentElement
  const themeVars = themes[theme]
  
  Object.entries(themeVars).forEach(([property, value]) => {
    root.style.setProperty(property, value)
  })
  
  // 更新 body 类名
  document.body.classList.remove('light-theme', 'dark-theme')
  document.body.classList.add(`${theme}-theme`)
}

// 从本地存储获取主题
const getStoredTheme = (): Theme => {
  const stored = localStorage.getItem('theme')
  return (stored === 'dark' || stored === 'light') ? stored : 'light'
}

// 保存主题到本地存储
const saveTheme = (theme: Theme) => {
  localStorage.setItem('theme', theme)
}

// 切换主题
const toggleTheme = () => {
  const newTheme: Theme = currentTheme.value === 'light' ? 'dark' : 'light'
  setTheme(newTheme)
}

// 设置主题
const setTheme = (theme: Theme) => {
  currentTheme.value = theme
  isDark.value = theme === 'dark'
  applyTheme(theme)
  saveTheme(theme)
}

// 获取系统主题偏好
const getSystemTheme = (): Theme => {
  if (typeof window !== 'undefined' && window.matchMedia) {
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
  }
  return 'light'
}

// 监听系统主题变化
const watchSystemTheme = () => {
  if (typeof window !== 'undefined' && window.matchMedia) {
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
    mediaQuery.addEventListener('change', (e) => {
      // 只有在用户没有手动设置主题时才跟随系统
      const storedTheme = localStorage.getItem('theme')
      if (!storedTheme) {
        setTheme(e.matches ? 'dark' : 'light')
      }
    })
  }
}

export const useTheme = () => {
  // 初始化主题
  onMounted(() => {
    const storedTheme = getStoredTheme()
    setTheme(storedTheme)
    watchSystemTheme()
  })
  
  return {
    currentTheme: readonly(currentTheme),
    isDark: readonly(isDark),
    toggleTheme,
    setTheme,
    getSystemTheme
  }
}
