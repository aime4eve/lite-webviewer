<<<<<<< HEAD
=======
import { fileURLToPath, URL } from 'node:url'
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  base: './',
  envDir: '..',
  plugins: [vue()],
<<<<<<< HEAD
  server: {
    port: process.env.PORT || 6688, // 使用环境变量PORT，默认6688
    host: true
  },
  // 配置不同环境下的端口
=======
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: process.env.PORT || 6688,
    host: true,
    proxy: {
      '/api': { 
        target: 'http://localhost:9999', 
        changeOrigin: true 
      }
    }
  },
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
  define: {
    'process.env.PORT': JSON.stringify(process.env.PORT || 6688)
  }
})
