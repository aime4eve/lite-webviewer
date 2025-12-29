<template>
  <div class="profile-container">
    <!-- 顶部用户信息区 -->
    <header class="header">
      <h1 class="title">👤 个人中心</h1>
    </header>

    <!-- 用户信息卡片 -->
    <section class="user-card">
      <div class="user-avatar">
        <span class="avatar">👤</span>
      </div>
      <div class="user-info">
        <div class="user-name">{{ user.name }}</div>
        <div class="user-id">用户ID: {{ user.id }}</div>
      </div>
      <button class="edit-btn">✏️ 编辑资料</button>
    </section>

    <!-- 核心信息卡片 -->
    <section class="info-cards">
      <!-- 订阅状态卡片 -->
      <div class="info-card subscription-card" @click="navigateToSubscription">
        <div class="card-icon">💎</div>
        <div class="card-content">
          <div class="card-title">订阅状态</div>
          <div class="card-value">{{ subscription.status }}</div>
          <div class="card-desc">有效期至: {{ subscription.expiryDate }}</div>
        </div>
        <div class="card-arrow">></div>
      </div>

      <!-- 积分卡片 -->
      <div class="info-card points-card" @click="navigateToPoints">
        <div class="card-icon">🪙</div>
        <div class="card-content">
          <div class="card-title">防霉积分</div>
          <div class="card-value points">{{ points }} 分</div>
          <div class="card-desc">可兑换 ¥{{ points / 10 }} 订阅抵扣</div>
        </div>
        <div class="card-arrow">></div>
      </div>

      <!-- 报告卡片 -->
      <div class="info-card report-card" @click="navigateToReports">
        <div class="card-icon">📊</div>
        <div class="card-content">
          <div class="card-title">防霉报告</div>
          <div class="card-value">{{ reportsCount }} 份</div>
          <div class="card-desc">查看历史报告</div>
        </div>
        <div class="card-arrow">></div>
      </div>
    </section>

    <!-- 功能菜单 -->
    <section class="menu-section">
      <h2 class="section-title">功能菜单</h2>
      <div class="menu-list">
        <div class="menu-item" @click="navigateToRoomManagement">
          <span class="menu-icon">🏠</span>
          <span class="menu-text">房间管理</span>
          <span class="menu-arrow">></span>
        </div>
<<<<<<< HEAD
=======
        <div class="menu-item" @click="navigateToProvisioning">
          <span class="menu-icon">➕</span>
          <span class="menu-text">设备配网</span>
          <span class="menu-arrow">></span>
        </div>
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
        <div class="menu-item" @click="navigateToSettings">
          <span class="menu-icon">⚙️</span>
          <span class="menu-text">设置</span>
          <span class="menu-arrow">></span>
        </div>
        <div class="menu-item" @click="navigateToHelp">
          <span class="menu-icon">❓</span>
          <span class="menu-text">帮助中心</span>
          <span class="menu-arrow">></span>
        </div>
        <div class="menu-item" @click="navigateToFeedback">
          <span class="menu-icon">💬</span>
          <span class="menu-text">意见反馈</span>
          <span class="menu-arrow">></span>
        </div>
        <div class="menu-item" @click="navigateToAbout">
          <span class="menu-icon">ℹ️</span>
          <span class="menu-text">关于我们</span>
          <span class="menu-arrow">></span>
        </div>
      </div>
    </section>

    <!-- 退出登录按钮 -->
    <section class="logout-section">
      <button class="logout-btn" @click="logout">🚪 退出登录</button>
    </section>

    <!-- 底部导航栏 -->
    <FooterNavigation active="profile" />
  </div>
</template>

<script>
import { defineComponent } from 'vue'
import { useRouter } from 'vue-router'
import FooterNavigation from '../components/FooterNavigation.vue'

export default defineComponent({
  name: 'ProfileView',
  components: {
    FooterNavigation
  },
  setup() {
    const router = useRouter()

    // 模拟用户数据
    const user = {
      id: 'SMG-20251215-001',
      name: '张三',
      avatar: '👤'
    }

    // 模拟订阅数据
    const subscription = {
      status: '试用中',
      expiryDate: '2026-01-30'
    }

    // 模拟积分数据
    const points = 180

    // 模拟报告数量
    const reportsCount = 12

    // 导航方法
    const navigateToSubscription = () => {
      router.push('/c/subscription')
    }

    const navigateToPoints = () => {
<<<<<<< HEAD
      alert('积分详情功能开发中...')
    }

    const navigateToReports = () => {
      alert('报告中心功能开发中...')
=======
      router.push('/c/points-history')
    }

    const navigateToReports = () => {
      // 报告中心暂无独立入口，可引导至首页查看或后续实现
      alert('报告中心功能完善中...')
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
    }

    const navigateToDevices = () => {
      router.push('/c/devices')
    }

    const navigateToSettings = () => {
      alert('设置功能开发中...')
    }

<<<<<<< HEAD
=======
    const navigateToProvisioning = () => {
      router.push('/c/provisioning')
    }

>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
    const navigateToHelp = () => {
      alert('帮助中心功能开发中...')
    }

    const navigateToFeedback = () => {
      alert('意见反馈功能开发中...')
    }

    const navigateToAbout = () => {
      alert('关于我们功能开发中...')
    }

    const navigateToRoomManagement = () => {
      router.push('/c/room-management')
    }

    const logout = () => {
      if (confirm('确定要退出登录吗？')) {
        alert('退出登录功能开发中...')
      }
    }

    return {
      user,
      subscription,
      points,
      reportsCount,
      navigateToSubscription,
      navigateToPoints,
      navigateToReports,
      navigateToDevices,
      navigateToRoomManagement,
<<<<<<< HEAD
=======
      navigateToProvisioning,
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
      navigateToSettings,
      navigateToHelp,
      navigateToFeedback,
      navigateToAbout,
      logout
    }
  }
})
</script>

<style scoped>
.profile-container {
  max-width: 480px;
  margin: 0 auto;
  background-color: #f5f5f5;
  min-height: 100vh;
  padding-bottom: 60px; /* 为底部导航栏留出空间 */
}

.header {
  display: flex;
  align-items: center;
  padding: 16px;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.title {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}

/* 用户信息卡片 */
.user-card {
  display: flex;
  align-items: center;
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  margin: 16px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  gap: 16px;
}

.user-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background-color: #E3F2FD;
  flex-shrink: 0;
}

.avatar {
  font-size: 32px;
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-name {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.user-id {
  font-size: 14px;
  color: #666;
}

.edit-btn {
  background-color: #f0f0f0;
  border: 1px solid #ddd;
  color: #666;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.edit-btn:hover {
  background-color: #e0e0e0;
  border-color: #ccc;
}

/* 核心信息卡片 */
.info-cards {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 16px;
}

.info-card {
  display: flex;
  align-items: center;
  background-color: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: background-color 0.2s;
  gap: 12px;
}

.info-card:hover {
  background-color: #f9f9f9;
}

.card-icon {
  font-size: 24px;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  flex-shrink: 0;
}

.subscription-card .card-icon {
  background-color: #FFF3E0;
  color: #FF9800;
}

.points-card .card-icon {
  background-color: #FFF8E1;
  color: #FFC107;
}

.report-card .card-icon {
  background-color: #E8F5E9;
  color: #4CAF50;
}

.card-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.card-title {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.card-value {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.card-value.points {
  color: #FFC107;
}

.card-desc {
  font-size: 13px;
  color: #999;
}

.card-arrow {
  font-size: 16px;
  color: #999;
}

/* 功能菜单 */
.menu-section {
  margin: 20px 0;
  padding: 0 16px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 12px 0;
  color: #333;
}

.menu-list {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s;
  gap: 12px;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-item:hover {
  background-color: #f9f9f9;
}

.menu-icon {
  font-size: 18px;
  width: 24px;
  text-align: center;
}

.menu-text {
  flex: 1;
  font-size: 15px;
  color: #333;
}

.menu-arrow {
  font-size: 16px;
  color: #999;
}

/* 退出登录按钮 */
.logout-section {
  padding: 0 16px 20px;
}

.logout-btn {
  width: 100%;
  background-color: #F44336;
  color: white;
  border: none;
  padding: 12px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.logout-btn:hover {
  background-color: #D32F2F;
}
</style>