<template>
  <div class="device-detail-container">
    <!-- 顶部导航栏 -->
    <header class="header">
      <button class="back-btn" @click="navigateBack">🔙 返回</button>
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
        </div>
      </div>
    </section>

    <!-- 自动防霉策略 -->
    <section class="strategy-section">
      <h2 class="section-title">🤖 自动防霉策略</h2>
      <div class="strategy-content">
        <div class="switch-item">
          <span class="switch-label">🔘 启用自动防霉</span>
          <label class="toggle-switch">
            <input type="checkbox" v-model="isAutoModeEnabled">
            <span class="toggle-slider"></span>
          </label>
        </div>
        <div class="rules-preview">
          <h3 class="rules-title">📜 规则预览:</h3>
          <ul class="rules-list">
            <li class="rule-item">• 湿度 > 85% 且持续 30min ➡️ 开启排风扇</li>
            <li class="rule-item">• 30min 后湿度仍 > 60% ➡️ 开启加热烘干</li>
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
        <button class="save-btn" @click="saveConfig">💾 保存配置</button>
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
import { defineComponent, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import FooterNavigation from '../components/FooterNavigation.vue'

export default defineComponent({
  name: 'DeviceDetailView',
  components: {
    FooterNavigation
  },
  setup() {
    const router = useRouter()
    const route = useRoute()
    
    // 模拟数据
    const isAutoModeEnabled = ref(true)
    const switch1Mapping = ref('fan')
    const switch2Mapping = ref('heater')
    const switch3Mapping = ref('light')

    const navigateBack = () => {
      router.go(-1)
    }

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

.status-section, .strategy-section, .linkage-section, .alerts-section {
  background-color: #fff;
  padding: 16px;
  margin: 12px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

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

/* 开关样式 */
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

.save-btn:hover {
  background-color: #45a049;
}

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
</style>