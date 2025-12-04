import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    host: true,
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8090',
        changeOrigin: true,
        secure: false,
        ws: false,
      },
      '/fs': {
        target: 'http://localhost:8090/api/v1/fs',
        changeOrigin: true,
        secure: false,
        ws: false,
        rewrite: (path) => path.replace(/^\/fs/, '')
      },
    },
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          // 确保mermaid库被单独打包
          mermaid: ['mermaid'],
          // 确保react相关库被单独打包
          react: ['react', 'react-dom'],
        }
      }
    }
  }
})
