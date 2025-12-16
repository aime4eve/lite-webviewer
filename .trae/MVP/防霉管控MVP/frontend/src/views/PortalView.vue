<template>
  <div class="portal-container">
    <!-- 顶部标题 -->
    <header class="portal-header">
      <h1 class="portal-title">🛡️ 防霉守护系统</h1>
      <p class="portal-subtitle">智能防霉，守护健康生活</p>
    </header>

    <!-- 各端入口区域 -->
    <main class="portal-main">
      <!-- C端入口 -->
      <div class="portal-card c-card" @click="navigateTo('/c/')">
        <div class="card-icon">🏠</div>
        <h2 class="card-title">C端用户</h2>
        <p class="card-description">面向普通用户的智能防霉服务</p>
        <div class="card-features">
          <span class="feature-tag">设备管理</span>
          <span class="feature-tag">防霉策略</span>
          <span class="feature-tag">健康报告</span>
        </div>
        <div class="card-enter">进入系统 →</div>
      </div>

      <!-- B端入口 -->
      <div class="portal-card b-card" @click="navigateTo('/b/')">
        <div class="card-icon">🏢</div>
        <h2 class="card-title">B端商户</h2>
        <p class="card-description">面向商户的空间管理服务</p>
        <div class="card-features">
          <span class="feature-tag">空间管理</span>
          <span class="feature-tag">设备监控</span>
          <span class="feature-tag">策略定制</span>
        </div>
        <div class="card-enter">进入系统 →</div>
      </div>

      <!-- 运营端入口 -->
      <div class="portal-card operator-card" @click="navigateTo('/operator/')">
        <div class="card-icon">⚙️</div>
        <h2 class="card-title">运营运维</h2>
        <p class="card-description">面向管理员的运营管理服务</p>
        <div class="card-features">
          <span class="feature-tag">设备注册</span>
          <span class="feature-tag">用户管理</span>
          <span class="feature-tag">故障监控</span>
        </div>
        <div class="card-enter">进入系统 →</div>
      </div>
    </main>

    <!-- 底部信息 -->
    <footer class="portal-footer">
      <p class="footer-text">© 2025 防霉守护系统 | 智能健康生活解决方案</p>
    </footer>
  </div>
</template>

<script>
import { defineComponent } from 'vue'
import { useRouter } from 'vue-router'

export default defineComponent({
  name: 'PortalView',
  setup() {
    const router = useRouter()

    // 导航到指定端
    const navigateTo = (path) => {
      router.push(path)
    }

    return {
      navigateTo
    }
  }
})
</script>

<style scoped>
.portal-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.portal-header {
  text-align: center;
  margin-bottom: 40px;
  color: white;
}

.portal-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin: 0 0 10px 0;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.portal-subtitle {
  font-size: 1.2rem;
  margin: 0;
  opacity: 0.9;
}

.portal-main {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 30px;
  max-width: 1000px;
  width: 100%;
}

.portal-card {
  background: white;
  border-radius: 16px;
  padding: 30px;
  text-align: center;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.portal-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 6px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  transform: scaleX(0);
  transition: transform 0.3s ease;
}

.portal-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.3);
}

.portal-card:hover::before {
  transform: scaleX(1);
}

.card-icon {
  font-size: 4rem;
  margin-bottom: 20px;
  display: block;
}

.c-card .card-icon {
  color: #4CAF50;
}

.b-card .card-icon {
  color: #2196F3;
}

.operator-card .card-icon {
  color: #FF9800;
}

.card-title {
  font-size: 1.8rem;
  font-weight: 600;
  margin: 0 0 10px 0;
  color: #333;
}

.card-description {
  font-size: 1rem;
  color: #666;
  margin: 0 0 20px 0;
  line-height: 1.5;
}

.card-features {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
  margin-bottom: 25px;
}

.feature-tag {
  background: #f0f0f0;
  color: #666;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 500;
}

.card-enter {
  color: #667eea;
  font-weight: 600;
  font-size: 1.1rem;
  transition: all 0.3s ease;
}

.portal-card:hover .card-enter {
  transform: translateX(5px);
}

.portal-footer {
  margin-top: 60px;
  color: white;
  opacity: 0.8;
  font-size: 0.9rem;
}

.footer-text {
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .portal-title {
    font-size: 2rem;
  }
  
  .portal-main {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  
  .portal-card {
    padding: 20px;
  }
  
  .card-icon {
    font-size: 3rem;
  }
  
  .card-title {
    font-size: 1.5rem;
  }
}
</style>