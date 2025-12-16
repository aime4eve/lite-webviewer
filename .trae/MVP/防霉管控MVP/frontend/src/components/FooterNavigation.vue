<template>
  <nav class="footer-nav">
    <!-- 门户按钮 -->
    <div 
      class="nav-item" 
      @click="navigateToPortal"
    >
      <span class="nav-icon">🚪</span>
      <span class="nav-text">门户</span>
    </div>
    <div 
      class="nav-item" 
      :class="{ active: active === 'home' }" 
      @click="navigateTo('/')"
    >
      <span class="nav-icon">🏠</span>
      <span class="nav-text">首页</span>
    </div>
    <div 
      class="nav-item" 
      :class="{ active: active === 'devices' }" 
      @click="navigateTo('/devices')"
    >
      <span class="nav-icon">📱</span>
      <span class="nav-text">设备</span>
    </div>
    <div 
      class="nav-item" 
      :class="{ active: active === 'subscription' }" 
      @click="navigateTo('/subscription')"
    >
      <span class="nav-icon">💎</span>
      <span class="nav-text">订阅</span>
    </div>
    <div 
      class="nav-item" 
      :class="{ active: active === 'profile' }" 
      @click="navigateTo('/profile')"
    >
      <span class="nav-icon">👤</span>
      <span class="nav-text">我的</span>
    </div>
  </nav>
</template>

<script>
import { defineComponent } from 'vue'
import { useRouter } from 'vue-router'

export default defineComponent({
  name: 'FooterNavigation',
  props: {
    active: {
      type: String,
      default: 'home'
    }
  },
  setup() {
    const router = useRouter()

    const navigateTo = (path) => {
      // 为C端路由添加/c/前缀
      const fullPath = path === '/' ? '/c/' : `/c${path}`
      router.push(fullPath)
    }

    // 跳转到门户页面
    const navigateToPortal = () => {
      router.push('/portal')
    }

    return {
      navigateTo,
      navigateToPortal
    }
  }
})
</script>

<style scoped>
.footer-nav {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 480px;
  display: flex;
  justify-content: space-around;
  align-items: center;
  background-color: #fff;
  padding: 10px 0;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
  border-radius: 16px 16px 0 0;
  z-index: 1000;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 8px;
}

.nav-item:hover {
  background-color: #f5f5f5;
}

.nav-item.active {
  color: #4CAF50;
  background-color: #E8F5E9;
}

.nav-icon {
  font-size: 20px;
}

.nav-text {
  font-size: 12px;
  font-weight: 500;
}
</style>