<template>
  <div class="add-device-container">
    <!-- 顶部导航栏 -->
    <header class="header">
      <button class="close-btn" @click="navigateBack">❌ 关闭</button>
      <h1 class="title">➕ 添加设备</h1>
    </header>

    <!-- 设备绑定流程 -->
    <div class="binding-flow">
      <!-- 步骤指示器 -->
      <div class="step-indicator">
        <div class="step-item" :class="{ active: !isSearching && !isConnecting && !isBindingSuccess }">
          <span class="step-number">1️⃣</span>
          <span class="step-text">选择绑定方式</span>
        </div>
        <div class="step-separator">➡️</div>
        <div class="step-item" :class="{ active: isSearching || isConnecting }">
          <span class="step-number">2️⃣</span>
          <span class="step-text">自动配网</span>
        </div>
        <div class="step-separator">➡️</div>
        <div class="step-item" :class="{ active: isBindingSuccess }">
          <span class="step-number">3️⃣</span>
          <span class="step-text">绑定成功</span>
        </div>
      </div>

      <!-- 步骤1：选择绑定方式 -->
      <div v-if="!isSearching && !isConnecting && !isBindingSuccess" class="step-content">
        <h2 class="step-title">1️⃣ 步骤 1: 选择绑定方式</h2>
        <div class="binding-options">
          <!-- 自动配网 -->
          <div class="option-item auto-item">
            <button class="auto-btn" @click="startAutoPairing">
              📱 自动搜索设备
            </button>
            <p class="auto-text">设备通电后，自动搜索附近可绑定设备</p>
          </div>
          
          <!-- 分隔线 -->
          <div class="divider">
            <span class="divider-text">OR</span>
          </div>
          
          <!-- 扫码绑定 -->
          <div class="option-item">
            <div class="scan-area">
              <span class="scan-icon">📷 扫一扫图标</span>
              <p class="scan-text">扫描设备背面的二维码</p>
            </div>
          </div>
          
          <!-- 分隔线 -->
          <div class="divider">
            <span class="divider-text">OR</span>
          </div>
          
          <!-- 手动输入 -->
          <div class="option-item">
            <label class="manual-label">⌨️ 手动输入 SN 码:</label>
            <div class="input-group">
              <input 
                type="text" 
                class="sn-input" 
                placeholder="请输入设备SN码" 
                v-model="deviceSN"
              >
              <button 
                class="bind-btn" 
                :disabled="!deviceSN"
                @click="bindDevice"
              >
                🔗 确认绑定
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 步骤2：自动配网 -->
      <div v-if="isSearching || isConnecting" class="step-content">
        <h2 class="step-title">2️⃣ 步骤 2: 自动配网</h2>
        
        <!-- 搜索设备 -->
        <div v-if="isSearching" class="auto-pairing">
          <div class="searching">
            <span class="search-icon">🔄</span>
            <p class="search-text">正在搜索附近设备...</p>
          </div>
        </div>
        
        <!-- 连接设备 -->
        <div v-else-if="isConnecting" class="auto-pairing">
          <div class="device-found">
            <h3 class="found-title">📌 设备已发现：{{ foundDevice.name }}</h3>
            <p class="found-sn">SN: {{ foundDevice.sn }}</p>
          </div>
          
          <div class="connection-status">
            <span class="status-text">[✅ 自动连接中...]</span>
            <button class="retry-btn" @click="startAutoPairing">⟳ 重试</button>
            <button class="cancel-btn" @click="cancelAutoPairing">❌ 取消</button>
          </div>
          
          <div class="progress-bar">
            <div class="progress" :style="{ width: connectionProgress + '%' }"></div>
          </div>
          <p class="progress-text">⏳ 连接进度：{{ connectionProgress }}%</p>
        </div>
      </div>

      <!-- 步骤3：绑定成功 -->
      <div v-else-if="isBindingSuccess" class="step-content">
        <h2 class="step-title">3️⃣ 步骤 3: 绑定成功 & 激活权益</h2>
        <div class="success-card">
          <div class="success-icon">✅</div>
          <h3 class="success-title">绑定成功！</h3>
          <div class="success-info">
            <p class="device-name">📱 设备名称：{{ boundDevice.name }}</p>
            <p class="device-location">🏠 设备位置：{{ boundDevice.location }}</p>
          </div>
          <div class="success-benefits">
            <p class="benefit-item">🎁 恭喜获得首月免费试用权益</p>
            <p class="benefit-item">📄 24小时后将生成首份风险报告</p>
          </div>
          <button class="config-btn" @click="navigateToConfigure">
            ⚙️ 立即配置防霉策略 >
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { defineComponent, ref } from 'vue'
import { useRouter } from 'vue-router'

export default defineComponent({
  name: 'AddDeviceView',
  setup() {
    const router = useRouter()
    
    // 基本绑定数据
    const deviceSN = ref('')
    const isBindingSuccess = ref(false)
    
    // 自动配网数据
    const isSearching = ref(false)
    const isConnecting = ref(false)
    const connectionProgress = ref(0)
    const foundDevice = ref({
      name: '温湿度传感器',
      sn: 'SN123456'
    })
    const boundDevice = ref({
      name: '主卧浴室',
      location: '主卧'
    })
    
    // 模拟连接定时器
    let progressTimer = null

    const navigateBack = () => {
      router.go(-1)
      clearInterval(progressTimer)
    }

    const bindDevice = () => {
      // 这里可以添加绑定设备的API调用
      if (deviceSN.value) {
        isBindingSuccess.value = true
      }
    }

    const navigateToConfigure = () => {
      // 跳转到设备详情页进行配置
      router.push('/c/device-detail/1')
    }
    
    // 开始自动配网
    const startAutoPairing = () => {
      isSearching.value = true
      
      // 模拟搜索设备，2秒后发现设备
      setTimeout(() => {
        isSearching.value = false
        isConnecting.value = true
        connectionProgress.value = 0
        
        // 模拟连接进度，每500ms更新一次
        progressTimer = setInterval(() => {
          connectionProgress.value += 5
          
          if (connectionProgress.value >= 100) {
            clearInterval(progressTimer)
            isConnecting.value = false
            isBindingSuccess.value = true
          }
        }, 500)
      }, 2000)
    }
    
    // 取消自动配网
    const cancelAutoPairing = () => {
      clearInterval(progressTimer)
      isSearching.value = false
      isConnecting.value = false
      connectionProgress.value = 0
    }

    return {
      // 基本绑定
      deviceSN,
      isBindingSuccess,
      navigateBack,
      bindDevice,
      navigateToConfigure,
      // 自动配网
      isSearching,
      isConnecting,
      connectionProgress,
      foundDevice,
      boundDevice,
      startAutoPairing,
      cancelAutoPairing
    }
  }
})
</script>

<style scoped>
.add-device-container {
  max-width: 480px;
  margin: 0 auto;
  background-color: #f5f5f5;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
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

.close-btn {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  transition: background-color 0.2s;
}

.close-btn:hover {
  background-color: #f0f0f0;
}

.title {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}

.binding-flow {
  flex: 1;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.step-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-bottom: 16px;
}

.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  opacity: 0.5;
  transition: opacity 0.3s;
}

.step-item.active {
  opacity: 1;
}

.step-number {
  font-size: 24px;
}

.step-text {
  font-size: 14px;
  font-weight: 500;
}

.step-separator {
  font-size: 20px;
}

.step-content {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.step-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
  text-align: center;
  color: #333;
}

.binding-options {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.option-item {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.scan-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  background-color: #f9f9f9;
  border: 2px dashed #ddd;
  border-radius: 8px;
  gap: 12px;
}

.scan-icon {
  font-size: 48px;
}

.scan-text {
  font-size: 16px;
  color: #666;
  margin: 0;
}

.divider {
  display: flex;
  align-items: center;
  gap: 16px;
  margin: 12px 0;
}

.divider::before, .divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background-color: #ddd;
}

.divider-text {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.manual-label {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 8px;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.sn-input {
  padding: 12px 16px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 16px;
  width: 100%;
  box-sizing: border-box;
}

.sn-input:focus {
  outline: none;
  border-color: #4CAF50;
  box-shadow: 0 0 0 2px rgba(76, 175, 80, 0.2);
}

.bind-btn {
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s;
}

.bind-btn:hover:not(:disabled) {
  background-color: #45a049;
}

.bind-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
  opacity: 0.7;
}

/* 绑定成功样式 */
.success-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  background-color: #E8F5E9;
  padding: 32px 24px;
  border-radius: 12px;
  text-align: center;
}

.success-icon {
  font-size: 64px;
}

.success-title {
  font-size: 24px;
  font-weight: 700;
  color: #4CAF50;
  margin: 0;
}

.success-benefits {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin: 12px 0;
}

.benefit-item {
  font-size: 16px;
  color: #333;
  margin: 0;
  line-height: 1.5;
}

.config-btn {
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s;
  margin-top: 8px;
}

.config-btn:hover {
  background-color: #45a049;
}

/* 自动配网样式 */
.auto-item {
  text-align: center;
  margin-bottom: 16px;
}

.auto-btn {
  background-color: #2196F3;
  color: white;
  border: none;
  padding: 16px 32px;
  border-radius: 8px;
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.3s;
  margin-bottom: 12px;
}

.auto-btn:hover {
  background-color: #1976D2;
}

.auto-text {
  font-size: 16px;
  color: #666;
  margin: 0;
}

/* 自动配网过程样式 */
.auto-pairing {
  display: flex;
  flex-direction: column;
  gap: 24px;
  align-items: center;
}

.searching {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 32px;
  background-color: #f9f9f9;
  border-radius: 8px;
  border: 2px dashed #ddd;
  width: 100%;
}

.search-icon {
  font-size: 48px;
  animation: rotate 2s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.search-text {
  font-size: 18px;
  color: #666;
  margin: 0;
}

.device-found {
  text-align: center;
  width: 100%;
  background-color: #E8F5E9;
  padding: 16px;
  border-radius: 8px;
}

.found-title {
  font-size: 20px;
  font-weight: 600;
  color: #4CAF50;
  margin: 0 0 8px 0;
}

.found-sn {
  font-size: 16px;
  color: #666;
  margin: 0;
}

.connection-status {
  display: flex;
  align-items: center;
  gap: 16px;
  width: 100%;
  justify-content: center;
}

.status-text {
  font-size: 16px;
  font-weight: 500;
  color: #4CAF50;
}

.retry-btn, .cancel-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.retry-btn {
  background-color: #FFC107;
  color: #333;
}

.retry-btn:hover {
  background-color: #FFA000;
}

.cancel-btn {
  background-color: #f0f0f0;
  color: #666;
}

.cancel-btn:hover {
  background-color: #e0e0e0;
}

.progress-bar {
  width: 100%;
  height: 12px;
  background-color: #f0f0f0;
  border-radius: 6px;
  overflow: hidden;
}

.progress {
  height: 100%;
  background-color: #4CAF50;
  border-radius: 6px;
  transition: width 0.5s ease;
}

.progress-text {
  font-size: 16px;
  color: #666;
  margin: 8px 0 0 0;
}

/* 步骤指示器样式 */
.step-separator {
  font-size: 20px;
  color: #666;
}

.success-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin: 12px 0;
  padding: 16px;
  background-color: rgba(255, 255, 255, 0.5);
  border-radius: 8px;
}

.device-name, .device-location {
  font-size: 16px;
  color: #333;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>