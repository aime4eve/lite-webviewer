module.exports = {
  devServer: {
    port: 8080,
    proxy: {
      '/api': {
        target: 'https://api.example.com',
        changeOrigin: true,
        pathRewrite: {
          '^/api': ''
        }
      }
    }
  },
  transpileDependencies: [
    // 这里可以添加需要转译的依赖
  ],
  configureWebpack: {
    // 配置webpack
  }
}