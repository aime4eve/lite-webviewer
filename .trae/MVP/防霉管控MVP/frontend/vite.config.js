import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    port: process.env.PORT || 6688, // 使用环境变量PORT，默认6688
    host: true
  },
  // 配置不同环境下的端口
  define: {
    'process.env.PORT': JSON.stringify(process.env.PORT || 6688)
  }
})
