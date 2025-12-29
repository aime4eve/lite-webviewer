<template>
  <div class="b-devices-container">
    <!-- 顶部标题栏 -->
    <header class="header">
      <h1 class="title">📱 设备列表</h1>
      <button class="add-device-btn" @click="navigateToAddDevice">➕ 添加设备</button>
    </header>

    <!-- 设备统计信息 -->
    <section class="devices-stats-section">
      <div class="stats-card">
        <div class="stat-item">
          <span class="stat-label">设备总数</span>
          <span class="stat-value">{{ totalDevices }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">在线设备</span>
          <span class="stat-value online">{{ onlineDevices }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">离线设备</span>
          <span class="stat-value offline">{{ offlineDevices }}</span>
        </div>
      </div>
    </section>

    <!-- 设备列表 -->
    <section class="devices-list-section">
      <div class="section-header">
        <h2 class="section-title">设备详情</h2>
        <div class="search-bar">
          <input type="text" placeholder="搜索设备名称或SN码" v-model="searchKeyword" @input="searchDevices">
          <button class="search-btn">🔍</button>
        </div>
      </div>

      <div class="devices-list">
        <div class="device-item" v-for="device in filteredDevices" :key="device.id">
          <div class="device-info">
            <div class="device-type" :class="device.type">
              {{ device.type === 'sensor' ? '🌡️' : '🔌' }}
            </div>
            <div class="device-details">
              <div class="device-name">{{ device.name }}</div>
              <div class="device-sn">SN: {{ device.sn }}</div>
              <div class="device-location">{{ device.location }}</div>
            </div>
          </div>
          <div class="device-status-section">
            <div class="device-status" :class="device.status">
              {{ device.status === 'online' ? '🟢 在线' : '🔴 离线' }}
            </div>
            <div class="last-active" v-if="device.lastActive">
              最后活跃: {{ device.lastActive }}
            </div>
          </div>
          <div class="device-actions">
            <button class="detail-btn" @click="navigateToDeviceDetail(device.id)">详情</button>
            <button class="edit-btn" @click="editDevice(device)">✏️</button>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="totalPages > 1">
        <button class="page-btn" :disabled="currentPage === 1" @click="prevPage">上一页</button>
        <span class="page-info">{{ currentPage }} / {{ totalPages }}</span>
        <button class="page-btn" :disabled="currentPage === totalPages" @click="nextPage">下一页</button>
      </div>
    </section>

    <!-- 底部导航栏 -->
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
        :class="{ active: activeNav === 'home' }" 
        @click="navigateToHome"
      >
        <span class="nav-icon">🏠</span>
        <span class="nav-text">首页</span>
      </div>
      <div 
        class="nav-item" 
        :class="{ active: activeNav === 'devices' }" 
        @click="navigateToDevices"
      >
        <span class="nav-icon">📱</span>
        <span class="nav-text">设备</span>
      </div>
      <div 
        class="nav-item" 
        :class="{ active: activeNav === 'spaces' }" 
        @click="navigateToSpaces"
      >
        <span class="nav-icon">🏢</span>
        <span class="nav-text">空间</span>
      </div>
      <div 
        class="nav-item" 
        :class="{ active: activeNav === 'profile' }" 
        @click="navigateToProfile"
      >
        <span class="nav-icon">👤</span>
        <span class="nav-text">我的</span>
      </div>
    </nav>
  </div>
</template>

<script>
import { deviceApi } from '@/api/device'

export default {
  name: 'BDevicesView',
  data() {
    return {
      // 设备列表数据
      devices: [],
      // 搜索关键词
      searchKeyword: '',
      // 过滤后的设备列表
      filteredDevices: [],
      // 分页信息
      currentPage: 1,
      pageSize: 4,
      totalPages: 1,
      total: 0,
      // 底部导航激活状态
      activeNav: 'devices',
      loading: false
    }
  },
  mounted() {
    this.loadDevices();
  },
  methods: {
    async loadDevices() {
      try {
        this.loading = true
        
        const params = {
          page: this.currentPage,
          pageSize: this.pageSize
        }
        
        if (this.searchKeyword) {
          params.keyword = this.searchKeyword
        }
        
        const response = await deviceApi.getDeviceList(params)
        
        if (response && response.data) {
          this.devices = response.data.list || []
          this.total = response.data.total || 0
          this.totalPages = Math.ceil(this.total / this.pageSize)
          this.filteredDevices = this.devices
        }
      } catch (error) {
        console.error('加载设备列表失败:', error)
        this.devices = []
        this.filteredDevices = []
      } finally {
        this.loading = false
      }
    },
    // 导航方法
    navigateToPortal() {
      this.$router.push('/portal')
    },
    navigateToHome() {
      this.$router.push('/b/')
      this.activeNav = 'home'
    },
    navigateToDevices() {
      this.$router.push('/b/devices')
      this.activeNav = 'devices'
    },
    navigateToSpaces() {
      this.$router.push('/b/space-management')
      this.activeNav = 'spaces'
    },
    navigateToProfile() {
      this.$router.push('/b/profile')
      this.activeNav = 'profile'
    },
    navigateToAddDevice() {
      this.$router.push('/b/add-device')
    },
    navigateToDeviceDetail(deviceId) {
      this.$router.push(`/b/device-detail/${deviceId}`)
    },
    
    // 搜索设备
    searchDevices() {
      this.currentPage = 1
      this.loadDevices()
    },
    
    // 上一页
    prevPage() {
      if (this.currentPage > 1) {
        this.currentPage--
        this.loadDevices()
      }
    },
    
    // 下一页
    nextPage() {
      if (this.currentPage < this.totalPages) {
        this.currentPage++
        this.loadDevices()
      }
    },
    
    // 编辑设备
    editDevice(device) {
      alert(`编辑设备：${device.name}`)
    }
  },
  computed: {
    // 设备统计信息
    totalDevices() {
      return this.total
    },
    onlineDevices() {
      return this.devices.filter(device => device.status === 'online').length
    },
    offlineDevices() {
      return this.devices.filter(device => device.status === 'offline').length
    }
  }
}
</script>

<style scoped>
.b-devices-container {
  max-width: 1200px;
  margin: 0 auto;
  background-color: #f5f5f5;
  min-height: 100vh;
  padding-bottom: 80px; /* 为底部导航栏留出空间 */
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #fff;
  padding: 16px 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.title {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
  color: #333;
}

.add-device-btn {
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.add-device-btn:hover {
  background-color: #45a049;
}

.devices-stats-section {
  background-color: #fff;
  padding: 20px;
  margin: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.stats-card {
  display: flex;
  justify-content: space-around;
  align-items: center;
  background-color: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #333;
}

.stat-value.online {
  color: #4CAF50;
}

.stat-value.offline {
  color: #F44336;
}

.devices-list-section {
  background-color: #fff;
  padding: 20px;
  margin: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
  color: #333;
}

.search-bar {
  display: flex;
  gap: 8px;
}

.search-bar input {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
  width: 250px;
}

.search-btn {
  background-color: #2196F3;
  color: white;
  border: none;
  padding: 10px 16px;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.search-btn:hover {
  background-color: #1976D2;
}

.devices-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.device-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background-color: #f9f9f9;
  border-radius: 8px;
  transition: background-color 0.2s;
}

.device-item:hover {
  background-color: #f0f0f0;
}

.device-info {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
}

.device-type {
  font-size: 32px;
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #e3f2fd;
  border-radius: 8px;
}

.device-type.sensor {
  background-color: #e8f5e9;
}

.device-type.switch {
  background-color: #fff3e0;
}

.device-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.device-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.device-sn, .device-location {
  font-size: 14px;
  color: #666;
}

.device-status-section {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  margin: 0 16px;
}

.device-status {
  font-size: 16px;
  font-weight: 600;
}

.device-status.online {
  color: #4CAF50;
}

.device-status.offline {
  color: #F44336;
}

.last-active {
  font-size: 12px;
  color: #999;
}

.device-actions {
  display: flex;
  gap: 8px;
}

.detail-btn, .edit-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.detail-btn {
  background-color: #2196F3;
  color: white;
}

.detail-btn:hover {
  background-color: #1976D2;
}

.edit-btn {
  background-color: #f0f0f0;
  color: #333;
}

.edit-btn:hover {
  background-color: #e0e0e0;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 24px;
}

.page-btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background-color: #fff;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.page-btn:hover:not(:disabled) {
  background-color: #f0f0f0;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-size: 16px;
  color: #666;
}

/* 底部导航栏 */
.footer-nav {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 480px;
  background-color: #fff;
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 12px 0;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  border-radius: 16px 16px 0 0;
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

@media (max-width: 768px) {
  .b-devices-container {
    padding: 0;
  }
  
  .header {
    padding: 12px 16px;
  }
  
  .title {
    font-size: 20px;
  }
  
  .add-device-btn {
    padding: 8px 16px;
    font-size: 14px;
  }
  
  .devices-stats-section, .devices-list-section {
    margin: 12px;
    padding: 16px;
  }
  
  .stats-card {
    flex-direction: column;
    gap: 16px;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .search-bar input {
    width: 100%;
  }
  
  .device-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .device-status-section {
    align-items: flex-start;
    width: 100%;
  }
}
</style>