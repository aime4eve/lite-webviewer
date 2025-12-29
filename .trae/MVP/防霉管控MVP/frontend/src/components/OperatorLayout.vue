<template>
  <div class="operator-layout-container">
    <!-- 顶部导航栏 -->
    <header class="header">
      <h1 class="title">{{ pageTitle }}</h1>
      <div class="header-actions">
        <div class="user-info">
          <span class="user-name">管理员</span>
          <button class="logout-btn" @click="logout">🚪 退出</button>
        </div>
      </div>
    </header>

    <!-- 主导航 -->
    <nav class="main-nav">
      <div 
        class="nav-item" 
        :class="{ active: activeNav === 'dashboard' }" 
        @click="navigateToDashboard"
      >
        📊 仪表盘
      </div>
      <div 
        class="nav-item" 
        :class="{ active: activeNav === 'users' }" 
        @click="navigateToUserManagement"
      >
        👥 用户管理
      </div>
      <div 
        class="nav-item" 
        :class="{ active: activeNav === 'devices' }" 
        @click="navigateToDeviceRegistration"
      >
        📱 设备管理
      </div>
      <div 
        class="nav-item" 
        :class="{ active: activeNav === 'alarms' }" 
        @click="navigateToFaultMonitoring"
      >
        🔔 告警与工单
      </div>
      <div 
        class="nav-item" 
        :class="{ active: activeNav === 'health' }" 
        @click="navigateToDeviceHealth"
      >
        🏥 设备健康
      </div>
      <div 
        class="nav-item" 
        :class="{ active: activeNav === 'strategies' }" 
        @click="navigateToStrategyManagement"
      >
        ⚙️ 策略管理
      </div>
      <div 
        class="nav-item" 
        :class="{ active: activeNav === 'billing' }" 
        @click="navigateToBillingManagement"
      >
        💳 计费管理
      </div>
    </nav>

    <!-- 页面内容 -->
    <main class="main-content">
      <slot></slot>
    </main>
  </div>
</template>

<script>
import { defineComponent } from 'vue'
import { useRouter } from 'vue-router'

export default defineComponent({
  name: 'OperatorLayout',
  props: {
    pageTitle: {
      type: String,
      required: true
    },
    activeNav: {
      type: String,
      required: true
    }
  },
  setup() {
    const router = useRouter()

    // 导航方法
    const navigateToDashboard = () => {
      router.push('/operator/')
    }

    const navigateToUserManagement = () => {
      router.push('/operator/user-management')
    }

    const navigateToDeviceRegistration = () => {
      router.push('/operator/device-registration')
    }

    const navigateToFaultMonitoring = () => {
      router.push('/operator/device-fault-monitoring')
    }

    const navigateToDeviceHealth = () => {
      router.push('/operator/device-health')
    }

    const navigateToStrategyManagement = () => {
      router.push('/operator/strategy-management')
    }

    const navigateToBillingManagement = () => {
      router.push('/operator/billing-management')
    }

    const logout = () => {
      if (confirm('确定要退出登录吗？')) {
        router.push('/portal')
      }
    }

    return {
      navigateToDashboard,
      navigateToUserManagement,
      navigateToDeviceRegistration,
      navigateToFaultMonitoring,
      navigateToDeviceHealth,
      navigateToStrategyManagement,
      navigateToBillingManagement,
      logout
    }
  }
})
</script>

<style scoped>
.operator-layout-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 顶部导航栏 */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #fff;
  padding: 16px 24px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.title {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
  color: #333;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-name {
  font-weight: 500;
  color: #333;
}

.logout-btn {
  background: none;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
  display: flex;
  align-items: center;
  gap: 4px;
}

.logout-btn:hover {
  background-color: #f0f0f0;
}

/* 主导航 */
.main-nav {
  display: flex;
  background-color: #fff;
  padding: 0 24px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 68px;
  z-index: 999;
  overflow-x: auto;
  white-space: nowrap;
}

.nav-item {
  padding: 16px 24px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 500;
  color: #666;
  border-bottom: 3px solid transparent;
}

.nav-item:hover {
  color: #2196F3;
  background-color: #f0f4f8;
}

.nav-item.active {
  color: #2196F3;
  border-bottom-color: #2196F3;
  background-color: #e3f2fd;
}

/* 主内容区域 */
.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header {
    padding: 12px 16px;
  }
  
  .title {
    font-size: 20px;
  }
  
  .main-nav {
    padding: 0 16px;
    top: 60px;
  }
  
  .nav-item {
    padding: 12px 16px;
    font-size: 14px;
  }
  
  .main-content {
    padding: 16px;
  }
}
</style>
