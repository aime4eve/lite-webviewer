<template>
  <div class="devices-container">
    <header class="header">
      <h1 class="title">📱 设备管理</h1>
      <button class="add-btn" @click="navigateToAddDevice">➕ 添加</button>
    </header>

    <section class="overview-section">
      <div class="overview-card">
        <div class="overview-item">
          <span class="overview-label">设备总数:</span>
          <span class="overview-value">{{ devices.length }} 台</span>
        </div>
        <div class="overview-item">
          <span class="overview-label">在线设备:</span>
          <span class="overview-value online">{{ onlineDevicesCount }} 台</span>
        </div>
        <div class="overview-item">
          <span class="overview-label">离线设备:</span>
          <span class="overview-value offline">{{ offlineDevicesCount }} 台</span>
        </div>
      </div>
    </section>

    <section class="devices-section">
      <h2 class="section-title">设备列表</h2>
      <div v-if="loadingDevices" class="loading">加载中...</div>
      <div v-else-if="devices.length > 0" class="devices-list">
        <div 
          v-for="device in devices" 
          :key="device.id" 
          class="device-item" 
          @click="navigateToDeviceDetail(device.id)"
        >
          <div class="device-icon">
            <span :class="device.status === 'online' ? 'status-dot online' : 'status-dot offline'"></span>
            <span class="icon">🏠</span>
          </div>
          <div class="device-info">
            <div class="device-name">{{ device.name || '未命名设备' }}</div>
            <div class="device-desc">{{ device.location || '未设置位置' }}</div>
          </div>
          <div class="device-status">
            <span :class="device.status === 'online' ? 'status-text online' : 'status-text offline'">
              {{ device.status === 'online' ? '在线' : '离线' }}
            </span>
            <span class="arrow">></span>
          </div>
        </div>
      </div>
      <div v-else class="no-data">暂无设备，请添加设备</div>
    </section>

    <FooterNavigation active="devices" />
  </div>
</template>

<script>
import { defineComponent, ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import FooterNavigation from '../components/FooterNavigation.vue'
import { deviceApi } from '../api/device'

export default defineComponent({
  name: 'DevicesView',
  components: {
    FooterNavigation
  },
  setup() {
    const router = useRouter()

    const devices = ref([])
    const loadingDevices = ref(false)

    const loadDevices = async () => {
      try {
        loadingDevices.value = true
        const response = await deviceApi.getDeviceList({
          page: 1,
          size: 100
        })
        if (response && response.data) {
          devices.value = response.data
        }
      } catch (error) {
        console.error('加载设备列表失败:', error)
        devices.value = []
      } finally {
        loadingDevices.value = false
      }
    }

    const onlineDevicesCount = computed(() => {
      return devices.value.filter(device => device.status === 'online').length
    })

    const offlineDevicesCount = computed(() => {
      return devices.value.filter(device => device.status === 'offline').length
    })

    const navigateToAddDevice = () => {
      router.push('/c/add-device')
    }

    const navigateToDeviceDetail = (id) => {
      router.push(`/c/device-detail/${id}`)
    }

    onMounted(() => {
      loadDevices()
    })

    return {
      devices,
      loadingDevices,
      onlineDevicesCount,
      offlineDevicesCount,
      navigateToAddDevice,
      navigateToDeviceDetail
    }
  }
})
</script>

<style scoped>
.devices-container {
  max-width: 480px;
  margin: 0 auto;
  background-color: #f5f5f5;
  min-height: 100vh;
  padding-bottom: 60px;
}

.header {
  display: flex;
  justify-content: space-between;
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

.add-btn {
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.add-btn:hover {
  background-color: #45a049;
}

.overview-section {
  padding: 16px;
}

.overview-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-around;
  gap: 16px;
}

.overview-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.overview-label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.overview-value {
  font-size: 18px;
  font-weight: 700;
  color: #333;
}

.overview-value.online {
  color: #4CAF50;
}

.overview-value.offline {
  color: #F44336;
}

.devices-section {
  padding: 0 16px 16px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 16px 0;
  color: #333;
}

.loading {
  text-align: center;
  color: #999;
  padding: 20px;
}

.no-data {
  text-align: center;
  color: #999;
  padding: 20px;
}

.devices-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.device-item {
  display: flex;
  align-items: center;
  background-color: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: background-color 0.2s;
}

.device-item:hover {
  background-color: #f9f9f9;
}

.device-icon {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-right: 16px;
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  position: relative;
}

.status-dot.online {
  background-color: #4CAF50;
}

.status-dot.online::after {
  content: '';
  position: absolute;
  top: -3px;
  left: -3px;
  right: -3px;
  bottom: -3px;
  background-color: #4CAF50;
  border-radius: 50%;
  opacity: 0.3;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 0.3;
  }
  50% {
    transform: scale(1.5);
    opacity: 0;
  }
  100% {
    transform: scale(1);
    opacity: 0.3;
  }
}

.status-dot.offline {
  background-color: #F44336;
}

.icon {
  font-size: 24px;
}

.device-info {
  flex: 1;
}

.device-name {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 4px;
}

.device-desc {
  font-size: 13px;
  color: #666;
}

.device-status {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-text {
  font-size: 14px;
  font-weight: 500;
}

.status-text.online {
  color: #4CAF50;
}

.status-text.offline {
  color: #F44336;
}

.arrow {
  font-size: 16px;
  color: #999;
}
</style>
