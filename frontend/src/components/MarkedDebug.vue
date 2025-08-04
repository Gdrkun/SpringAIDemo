<template>
  <div class="debug-container">
    <h3 style="color: red; border-bottom: 1px solid red; padding-bottom: 5px;">
      --- MARKED 调试信息 ---
    </h3>
    <h4>输入文本:</h4>
    <pre class="debug-pre">{{ text }}</pre>
    <h4>生成的 HTML (v-html 渲染):</h4>
    <div class="debug-output" v-html="htmlOutput"></div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { marked } from 'marked';

// 这个组件只接收一个 text 属性
const props = defineProps<{
  text: string;
}>();

const htmlOutput = computed(() => {
  if (!props.text) {
    return '<p style="color: orange;">没有提供文本</p>';
  }

  // 打印即将进入 marked 函数的最原始的文本
  console.log(
      '%c[MARKED DEBUG] Input String:',
      'color: blue; font-weight: bold;',
      JSON.stringify(props.text)
  );

  try {
    // 使用最基础、最不可能出错的配置来调用 marked
    const output = marked(props.text, {
      gfm: true,
      breaks: true,
      async: false,
    });

    // 打印 marked 函数生成的最终 HTML
    console.log(
        '%c[MARKED DEBUG] Output HTML:',
        'color: green; font-weight: bold;',
        output
    );

    return output;
  } catch (e) {
    console.error('[MARKED DEBUG] marked() 函数执行失败:', e);
    return `<p style="color: red;">Markdown 解析过程中发生致命错误。</p>`;
  }
});
</script>

<style scoped>
.debug-container {
  border: 2px solid red;
  padding: 1rem;
  margin: 1rem 0;
  background-color: #fff5f5;
  width: 100%;
  box-sizing: border-box;
}
.debug-pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  background: #eee;
  padding: 10px;
  border-radius: 4px;
  font-family: monospace;
}
.debug-output {
  border: 1px dashed blue;
  padding: 10px;
  margin-top: 10px;
  background: #f0f8ff;
}
</style>