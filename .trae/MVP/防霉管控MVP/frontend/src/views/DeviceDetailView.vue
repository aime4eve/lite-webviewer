<template>
  <div class="device-detail-container">
    <!-- 顶部导航栏 -->
    <header class="header">
      <button class="back-btn" @click="navigateBack">🔙 返回</button>
<<<<<<< HEAD
      <h1 class="title">🏠 主卧浴室</h1>
    </header>

    <!-- 实时状态 -->
    <section class="status-section">
      <h2 class="section-title">📊 实时状态</h2>
      <div class="status-content">
        <div class="env-item">
          <span class="env-label">💧 湿度:</span>
          <span class="env-value">72%</span>
        </div>
        <div class="env-item">
          <span class="env-label">🌡️ 温度:</span>
          <span class="env-value">23°C</span>
        </div>
        <div class="risk-item">
          <span class="risk-label">🔮 3h后霉变概率:</span>
          <span class="risk-value medium">68% (🟠 中风险)</span>
=======
      <h1 class="title">{{ deviceName }}</h1>
      <button class="debug-btn" @click="toggleTamper">⚠️ 模拟告警</button>
    </header>

    <!-- 资产保全告警 -->
    <section v-if="isTampered" class="tamper-alert-section">
      <div class="alert-header">
        <span class="alert-icon-lg">🚨</span>
        <div class="alert-info">
           <h3>设备异常告警</h3>
           <p>检测到设备防拆开关触发，设备已离线！</p>
           <p class="alert-time">📅 时间: 2025-12-15 14:30</p>
        </div>
      </div>
      
      <div v-if="!tamperAction" class="alert-actions">
        <h4>💡 请确认设备状态</h4>
        <div class="action-buttons">
          <button class="action-btn safe" @click="handleTamper('accidental')">🔘 我不小心碰掉了</button>
          <button class="action-btn danger" @click="handleTamper('damaged')">🔘 设备被人为损坏/拆除</button>
        </div>
      </div>

      <div v-if="tamperAction === 'accidental'" class="action-content accidental">
        <p>✅ 请将设备重新安装回原位，确保防拆开关被压下。</p>
        <p>系统检测到心跳后将自动消除告警。</p>
        <button class="reset-btn" @click="resetTamper">🔄 模拟设备恢复</button>
      </div>

      <div v-if="tamperAction === 'damaged'" class="action-content damaged">
        <h4>💰 赔付方案</h4>
        <div class="payment-details">
           <div class="pay-row"><span>💵 押金余额:</span> <span>¥200</span></div>
           <div class="pay-row"><span>🛠️ 需扣除费用:</span> <span class="deduct">¥50</span></div>
           <div class="pay-row total"><span>💰 预计返还:</span> <span class="return">¥150</span></div>
        </div>
        <div class="pay-actions">
           <button class="pay-btn" @click="confirmPayment">💳 确认赔付并解绑</button>
           <button class="repair-btn">📦 申请返修</button>
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
        </div>
      </div>
    </section>

<<<<<<< HEAD
=======
    <!-- 实时状态 -->
    <section class="status-section">
      <h2 class="section-title">📊 实时状态</h2>
      <div class="status-content" v-if="!loadingEnvironment">
        <div class="env-item">
          <span class="env-label">💧 湿度:</span>
          <span class="env-value">{{ environmentData.humidity }}%</span>
        </div>
        <div class="env-item">
          <span class="env-label">🌡️ 温度:</span>
          <span class="env-value">{{ environmentData.temperature }}°C</span>
        </div>
        <div class="risk-item">
          <span class="risk-label">🔮 3h后霉变概率:</span>
          <span class="risk-value" :class="getRiskClass(riskPrediction.riskLevel)">
            {{ riskPrediction.probability }}% ({{ getRiskLabel(riskPrediction.riskLevel) }})
          </span>
        </div>
      </div>
      <div class="loading-state" v-else>
        <p>加载中...</p>
      </div>
    </section>

    <!-- 气候带配置 -->
    <section class="climate-section">
      <h2 class="section-title">🌍 气候带配置</h2>
      <div class="climate-content">
        <div class="climate-item">
          <span class="climate-label">📍 当前位置:</span>
          <span class="climate-value">{{ deviceLocation || '未设置' }}</span>
        </div>
        <div class="climate-item">
          <span class="climate-label">🌡️ 气候模式:</span>
          <select v-model="climateZone" class="climate-select" @change="updateClimateZone">
            <option value="humid_south">🌧️ 潮湿南方 (梅雨季模式)</option>
            <option value="dry_north">☀️ 干燥北方</option>
            <option value="coastal">🌊 沿海地区</option>
            <option value="custom">⚙️ 自定义</option>
          </select>
        </div>
        <p class="climate-desc" v-if="climateZone === 'humid_south'">
          已自动加载"潮湿南方"算法模型，针对梅雨季高湿环境优化，建议开启自动防霉。
        </p>
      </div>
    </section>

    <!-- 健康指纹 & 反馈 -->
    <section class="health-feedback-section">
      <h2 class="section-title">🩺 健康与反馈</h2>
      <div class="action-buttons-grid">
        <button class="feature-btn" @click="navigateToHealth">
          🏥 设备健康指纹
        </button>
        <button class="feature-btn" @click="navigateToRisk">
          🎯 风险预测详情
        </button>
      </div>
    </section>
    
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
    <!-- 自动防霉策略 -->
    <section class="strategy-section">
      <h2 class="section-title">🤖 自动防霉策略</h2>
      <div class="strategy-content">
        <div class="switch-item">
          <span class="switch-label">🔘 启用自动防霉</span>
          <label class="toggle-switch">
<<<<<<< HEAD
            <input type="checkbox" v-model="isAutoModeEnabled">
=======
            <input type="checkbox" v-model="isAutoModeEnabled" @change="toggleAutoMode">
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
            <span class="toggle-slider"></span>
          </label>
        </div>
        <div class="rules-preview">
          <h3 class="rules-title">📜 规则预览:</h3>
          <ul class="rules-list">
<<<<<<< HEAD
            <li class="rule-item">• 湿度 > 85% 且持续 30min ➡️ 开启排风扇</li>
            <li class="rule-item">• 30min 后湿度仍 > 60% ➡️ 开启加热烘干</li>
=======
            <li class="rule-item" v-for="(rule, index) in strategyRules" :key="index">• {{ rule }}</li>
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
          </ul>
        </div>
        <button class="view-rules-btn">🔍 查看详细规则 ></button>
      </div>
    </section>

    <!-- 设备联动映射 -->
    <section class="linkage-section">
      <h2 class="section-title">🔗 设备联动映射</h2>
      <div class="linkage-content">
        <div class="linkage-item">
          <span class="linkage-label">🔘 开关位 1:</span>
          <select class="linkage-select" v-model="switch1Mapping">
            <option value="fan">🌀 排风扇</option>
            <option value="heater">🔥 加热器</option>
            <option value="light">💡 照明灯</option>
            <option value="none">❌ 未配置</option>
          </select>
        </div>
        <div class="linkage-item">
          <span class="linkage-label">🔘 开关位 2:</span>
          <select class="linkage-select" v-model="switch2Mapping">
            <option value="fan">🌀 排风扇</option>
            <option value="heater">🔥 加热器</option>
            <option value="light">💡 照明灯</option>
            <option value="none">❌ 未配置</option>
          </select>
        </div>
        <div class="linkage-item">
          <span class="linkage-label">🔘 开关位 3:</span>
          <select class="linkage-select" v-model="switch3Mapping">
            <option value="fan">🌀 排风扇</option>
            <option value="heater">🔥 加热器</option>
            <option value="light">💡 照明灯</option>
            <option value="none">❌ 未配置</option>
          </select>
        </div>
<<<<<<< HEAD
        <button class="save-btn" @click="saveConfig">💾 保存配置</button>
=======
        <button class="save-btn" @click="saveConfig" :disabled="savingConfig">
          {{ savingConfig ? '保存中...' : '💾 保存配置' }}
        </button>
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
      </div>
    </section>

    <!-- 故障与告警 -->
    <section class="alerts-section">
      <h2 class="section-title">🛠️ 故障与告警</h2>
      <div class="alerts-content">
        <div class="alert-item">
          <span class="alert-icon">🔔</span>
          <span class="alert-text">最近告警: 无</span>
        </div>
        <div class="signal-item">
          <span class="signal-label">📶 信号强度:</span>
          <span class="signal-value good">🟢 良好 (RSSI -95dBm)</span>
        </div>
      </div>
    </section>

    <!-- 底部导航栏 -->
    <FooterNavigation active="devices" />
  </div>
</template>

<script>
<<<<<<< HEAD
import { defineComponent, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import FooterNavigation from '../components/FooterNavigation.vue'
=======
import { defineComponent, ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import FooterNavigation from '../components/FooterNavigation.vue'
import { deviceApi } from '../api/device'
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5

export default defineComponent({
  name: 'DeviceDetailView',
  components: {
    FooterNavigation
  },
  setup() {
    const router = useRouter()
    const route = useRoute()
    
<<<<<<< HEAD
    // 模拟数据
    const isAutoModeEnabled = ref(true)
    const switch1Mapping = ref('fan')
    const switch2Mapping = ref('heater')
    const switch3Mapping = ref('light')
=======
    const deviceId = route.params.id
    
    const deviceName = ref('加载中...')
    const deviceLocation = ref('')
    
    const loadingEnvironment = ref(false)
    const savingConfig = ref(false)
    
    const environmentData = ref({
      temperature: 0,
      humidity: 0,
      timestamp: ''
    })
    
    const riskPrediction = ref({
      probability: 0,
      riskLevel: 'low',
      predictionTime: ''
    })
    
    const isAutoModeEnabled = ref(false)
    const strategyRules = ref([])
    
    const switch1Mapping = ref('none')
    const switch2Mapping = ref('none')
    const switch3Mapping = ref('none')
    
    const climateZone = ref('humid_south')
    
    const isTampered = ref(false)
    const tamperAction = ref(null)

    const loadDeviceDetail = async () => {
      try {
        const response = await deviceApi.getDeviceDetail(deviceId)
        if (response && response.data) {
          const device = response.data
          deviceName.value = device.name || '未命名设备'
          deviceLocation.value = device.location || ''
        }
      } catch (error) {
        console.error('加载设备详情失败:', error)
        deviceName.value = '设备详情加载失败'
      }
    }

    const loadEnvironmentData = async () => {
      try {
        loadingEnvironment.value = true
        const response = await deviceApi.getEnvironmentData(deviceId)
        if (response && response.data) {
          environmentData.value = response.data
        }
      } catch (error) {
        console.error('加载环境数据失败:', error)
      } finally {
        loadingEnvironment.value = false
      }
    }

    const loadRiskPrediction = async () => {
      try {
        const response = await deviceApi.getRiskPrediction(deviceId, 3)
        if (response && response.data) {
          riskPrediction.value = response.data
        }
      } catch (error) {
        console.error('加载风险预测失败:', error)
      }
    }

    const loadAutoMoldStrategy = async () => {
      try {
        const response = await deviceApi.getAutoMoldStrategy(deviceId)
        if (response && response.data) {
          isAutoModeEnabled.value = response.data.enabled || false
          strategyRules.value = response.data.rules || []
        }
      } catch (error) {
        console.error('加载自动防霉策略失败:', error)
      }
    }

    const loadLinkageMapping = async () => {
      try {
        const response = await deviceApi.getLinkageMapping(deviceId)
        if (response && response.data) {
          const mapping = response.data.mapping || {}
          switch1Mapping.value = mapping.switch1 || 'none'
          switch2Mapping.value = mapping.switch2 || 'none'
          switch3Mapping.value = mapping.switch3 || 'none'
        }
      } catch (error) {
        console.error('加载设备联动映射失败:', error)
      }
    }

    const toggleAutoMode = async () => {
      try {
        await deviceApi.updateAutoMoldStrategy(deviceId, {
          enabled: isAutoModeEnabled.value
        })
        alert(isAutoModeEnabled.value ? '自动防霉已启用' : '自动防霉已禁用')
      } catch (error) {
        console.error('更新自动防霉策略失败:', error)
        isAutoModeEnabled.value = !isAutoModeEnabled.value
        alert('操作失败，请稍后重试')
      }
    }

    const saveConfig = async () => {
      try {
        savingConfig.value = true
        await deviceApi.updateLinkageMapping(deviceId, {
          mapping: {
            switch1: switch1Mapping.value,
            switch2: switch2Mapping.value,
            switch3: switch3Mapping.value
          }
        })
        alert('配置已保存！')
      } catch (error) {
        console.error('保存配置失败:', error)
        alert('保存配置失败，请稍后重试')
      } finally {
        savingConfig.value = false
      }
    }

    const updateClimateZone = () => {
      // 实际开发中会调用API更新配置
      console.log('Update climate zone:', climateZone.value)
    }

    const navigateToHealth = () => {
      router.push(`/c/device-health/${deviceId}`)
    }

    const navigateToRisk = () => {
      router.push(`/c/risk-prediction/${deviceId}`)
    }

    const getRiskClass = (level) => {
      const classes = {
        low: 'safe',
        medium: 'medium',
        high: 'high'
      }
      return classes[level] || 'safe'
    }

    const getRiskLabel = (level) => {
      const labels = {
        low: '🟢 低风险',
        medium: '🟠 中风险',
        high: '🔴 高风险'
      }
      return labels[level] || '🟢 低风险'
    }

    const toggleTamper = () => {
      isTampered.value = !isTampered.value
      tamperAction.value = null
    }

    const handleTamper = (action) => {
      tamperAction.value = action
    }

    const resetTamper = () => {
      isTampered.value = false
      tamperAction.value = null
      alert('设备已恢复正常，告警消除。')
    }

    const confirmPayment = () => {
      alert('赔付成功！押金余额 ¥150 将在 1-3 个工作日内退还至原支付账户。设备已解绑。')
      router.push('/')
    }
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5

    const navigateBack = () => {
      router.go(-1)
    }

<<<<<<< HEAD
    const saveConfig = () => {
      // 这里可以添加保存配置的API调用
      alert('配置已保存！')
    }

    return {
      isAutoModeEnabled,
      switch1Mapping,
      switch2Mapping,
      switch3Mapping,
      navigateBack,
      saveConfig
=======
    onMounted(async () => {
      await Promise.all([
        loadDeviceDetail(),
        loadEnvironmentData(),
        loadRiskPrediction(),
        loadAutoMoldStrategy(),
        loadLinkageMapping()
      ])
    })

    return {
      deviceName,
      deviceLocation,
      loadingEnvironment,
      savingConfig,
      environmentData,
      riskPrediction,
      isAutoModeEnabled,
      strategyRules,
      switch1Mapping,
      switch2Mapping,
      switch3Mapping,
      climateZone,
      navigateBack,
      saveConfig,
      toggleAutoMode,
      updateClimateZone,
      navigateToHealth,
      navigateToRisk,
      getRiskClass,
      getRiskLabel,
      
      toggleTamper,
      isTampered,
      tamperAction,
      handleTamper,
      resetTamper,
      confirmPayment
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
    }
  }
})
</script>

<style scoped>
.device-detail-container {
  max-width: 480px;
  margin: 0 auto;
  background-color: #f5f5f5;
  min-height: 100vh;
<<<<<<< HEAD
  padding-bottom: 60px; /* 为底部导航栏留出空间 */
=======
  padding-bottom: 60px;
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
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

.back-btn {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  margin-right: 16px;
  padding: 8px;
  border-radius: 50%;
  transition: background-color 0.2s;
}

.back-btn:hover {
  background-color: #f0f0f0;
}

.title {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #333;
}

<<<<<<< HEAD
.status-section, .strategy-section, .linkage-section, .alerts-section {
=======
.status-section, .strategy-section, .linkage-section, .alerts-section, .climate-section {
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
  background-color: #fff;
  padding: 16px;
  margin: 12px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

<<<<<<< HEAD
=======
.climate-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.climate-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.climate-label {
  font-weight: 500;
}

.climate-value {
  font-weight: 600;
  color: #333;
}

.climate-select {
  padding: 6px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  background-color: #fff;
}

.climate-desc {
  font-size: 13px;
  color: #666;
  background-color: #E3F2FD;
  padding: 8px;
  border-radius: 4px;
  margin: 0;
  line-height: 1.4;
}

>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
.status-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.env-item, .risk-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.env-label, .risk-label {
  font-weight: 500;
}

.env-value {
  font-size: 18px;
  font-weight: 600;
}

.risk-value {
  font-size: 18px;
  font-weight: 600;
}

.risk-value.safe {
  color: #4CAF50;
}

.risk-value.medium {
  color: #FFC107;
}

.risk-value.high {
  color: #F44336;
}

.strategy-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.switch-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.switch-label {
  font-weight: 500;
  font-size: 15px;
}

<<<<<<< HEAD
/* 开关样式 */
=======
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
.toggle-switch {
  position: relative;
  display: inline-block;
  width: 50px;
  height: 26px;
}

.toggle-switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.toggle-slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ccc;
  transition: .4s;
  border-radius: 26px;
}

.toggle-slider:before {
  position: absolute;
  content: "";
  height: 18px;
  width: 18px;
  left: 4px;
  bottom: 4px;
  background-color: white;
  transition: .4s;
  border-radius: 50%;
}

input:checked + .toggle-slider {
  background-color: #4CAF50;
}

input:checked + .toggle-slider:before {
  transform: translateX(24px);
}

.rules-preview {
  background-color: #f9f9f9;
  padding: 12px;
  border-radius: 6px;
}

.rules-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
}

.rules-list {
  margin: 0;
  padding-left: 20px;
}

.rule-item {
  font-size: 13px;
  line-height: 1.5;
  margin-bottom: 4px;
}

.view-rules-btn {
  background: none;
  border: none;
  color: #2196F3;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  padding: 0;
  text-align: left;
  transition: color 0.2s;
}

.view-rules-btn:hover {
  color: #1976D2;
}

.linkage-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.linkage-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.linkage-label {
  font-weight: 500;
  font-size: 15px;
}

.linkage-select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  background-color: #fff;
  cursor: pointer;
  min-width: 150px;
}

.save-btn {
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s;
  align-self: flex-end;
}

<<<<<<< HEAD
.save-btn:hover {
  background-color: #45a049;
}

=======
.save-btn:hover:not(:disabled) {
  background-color: #45a049;
}

.save-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
.alerts-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.alert-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.alert-icon {
  font-size: 16px;
}

.alert-text {
  font-weight: 500;
}

.signal-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.signal-label {
  font-weight: 500;
}

.signal-value {
  font-weight: 600;
}

.signal-value.good {
  color: #4CAF50;
}

.signal-value.fair {
  color: #FFC107;
}

.signal-value.poor {
  color: #F44336;
}
<<<<<<< HEAD
</style>
=======

.debug-btn {
  background-color: #f39c12;
  color: white;
  border: none;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  margin-left: auto;
  cursor: pointer;
}

.loading-state {
  padding: 20px;
  text-align: center;
  color: #999;
}

.tamper-alert-section {
  background-color: #fff;
  padding: 16px;
  margin: 12px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(244, 67, 54, 0.2);
  border: 1px solid #ffcdd2;
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

.alert-header {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ffebee;
}

.alert-icon-lg {
  font-size: 32px;
}

.alert-info h3 {
  margin: 0 0 4px 0;
  color: #d32f2f;
  font-size: 18px;
}

.alert-info p {
  margin: 0;
  color: #c62828;
  font-size: 14px;
  line-height: 1.4;
}

.alert-time {
  margin-top: 4px !important;
  font-size: 12px !important;
  color: #999 !important;
}

.alert-actions h4, .action-content h4 {
  margin: 0 0 12px 0;
  font-size: 15px;
  color: #333;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.action-btn {
  flex: 1;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: #fff;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn.safe {
  color: #2e7d32;
  border-color: #a5d6a7;
  background-color: #e8f5e9;
}

.action-btn.danger {
  color: #c62828;
  border-color: #ef9a9a;
  background-color: #ffebee;
}

.action-content {
  background-color: #f9f9f9;
  padding: 12px;
  border-radius: 6px;
  margin-top: 12px;
}

.action-content.accidental {
  border-left: 4px solid #4CAF50;
}

.action-content.damaged {
  border-left: 4px solid #F44336;
}

.reset-btn {
  width: 100%;
  margin-top: 12px;
  padding: 10px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
}

.payment-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.pay-row {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #666;
}

.pay-row.total {
  border-top: 1px solid #ddd;
  padding-top: 8px;
  margin-top: 4px;
  font-weight: 600;
  color: #333;
  font-size: 16px;
}

.deduct { color: #F44336; }
.return { color: #4CAF50; }

.pay-actions {
  display: flex;
  gap: 12px;
}

.pay-btn {
  flex: 2;
  padding: 10px;
  background-color: #2196F3;
  color: white;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
}

.repair-btn {
  flex: 1;
  padding: 10px;
  background-color: #fff;
  color: #666;
  border: 1px solid #ddd;
  border-radius: 6px;
  cursor: pointer;
}

/* 按钮样式 */
.feature-btn {
  background: #722ed1;
  color: white;
  padding: 12px;
  border-radius: 8px;
  border: none;
  font-weight: bold;
  cursor: pointer;
  box-shadow: 0 2px 4px rgba(114, 46, 209, 0.2);
}

.action-buttons-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
  margin-top: 15px;
}

.health-feedback-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}
</style>
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
