import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
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
  define: {
    'process.env.PORT': JSON.stringify(process.env.PORT || 6688)
  }
})
