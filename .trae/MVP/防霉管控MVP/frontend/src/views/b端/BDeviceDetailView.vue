<template>
  <div class="device-detail-container">
    <!-- 顶部标题栏 -->
    <header class="header">
      <button class="back-btn" @click="goBack">🔙 返回</button>
      <h1 class="title">📱 设备详情</h1>
    </header>

    <!-- 设备基本信息 -->
    <section class="device-info-section">
      <div class="device-info-card">
        <div class="info-row">
          <div class="info-label">设备名称</div>
          <div class="info-value">{{ device.name }}</div>
        </div>
        <div class="info-row">
          <div class="info-label">设备类型</div>
          <div class="info-value">{{ device.type === 'sensor' ? '🌡️ 温湿度传感器' : '🔌 LoRa开关面板' }}</div>
        </div>
        <div class="info-row">
          <div class="info-label">设备SN码</div>
          <div class="info-value">{{ device.sn }}</div>
        </div>
        <div class="info-row">
          <div class="info-label">安装位置</div>
          <div class="info-value">{{ device.location }}</div>
        </div>
        <div class="info-row">
          <div class="info-label">设备状态</div>
          <div class="info-value" :class="device.status">
            {{ device.status === 'online' ? '🟢 在线' : '🔴 离线' }}
          </div>
        </div>
        <div class="info-row" v-if="device.lastActive">
          <div class="info-label">最后活跃</div>
          <div class="info-value">{{ device.lastActive }}</div>
        </div>
      </div>
    </section>

    <!-- 实时状态 -->
    <section class="real-time-section" v-if="device.type === 'sensor'">
      <h2 class="section-title">📊 实时状态</h2>
      <div class="status-card">
        <div class="status-item">
          <div class="status-label">💧 湿度</div>
          <div class="status-value">{{ device.realTimeData.humidity }}%</div>
        </div>
        <div class="status-item">
          <div class="status-label">🌡️ 温度</div>
          <div class="status-value">{{ device.realTimeData.temperature }}°C</div>
        </div>
        <div class="status-item">
          <div class="status-label">🔮 3h后霉变概率</div>
          <div class="status-value" :class="device.realTimeData.riskLevel">
            {{ device.realTimeData.riskLevel === 'high' ? '🔴' : device.realTimeData.riskLevel === 'medium' ? '🟠' : '🟢' }} 
            {{ device.realTimeData.riskValue }}% ({{ riskLevelMap[device.realTimeData.riskLevel] }})
          </div>
        </div>
      </div>
    </section>

    <!-- 自动防霉策略 -->
    <section class="strategy-section" v-if="device.type === 'switch'">
      <h2 class="section-title">🤖 自动防霉策略</h2>
      <div class="strategy-card">
        <div class="strategy-header">
          <label class="toggle-switch">
            <input type="checkbox" v-model="device.strategy.enabled" @change="updateStrategy">
            <span class="toggle-slider"></span>
          </label>
          <span class="strategy-title">{{ device.strategy.enabled ? '启用自动防霉' : '禁用自动防霉' }}</span>
        </div>
        <div class="strategy-rules" v-if="device.strategy.enabled">
          <h3 class="rules-title">📜 规则预览:</h3>
          <ul class="rules-list">
            <li v-for="(rule, index) in device.strategy.rules" :key="index">
              {{ rule }}
            </li>
          </ul>
          <button class="detail-rules-btn" @click="showRuleDetails = true">🔍 查看详细规则</button>
        </div>
      </div>
    </section>

    <!-- 设备联动映射 -->
    <section class="linkage-section" v-if="device.type === 'switch'">
      <h2 class="section-title">🔗 设备联动映射</h2>
      <div class="linkage-card">
        <div class="linkage-item" v-for="(mapping, index) in device.linkageMappings" :key="index">
          <div class="mapping-header">
            <span class="mapping-label">开关位 {{ index + 1 }}:</span>
          </div>
          <div class="mapping-select">
            <select v-model="mapping.device" @change="updateLinkage">
              <option value="fan">🌀 排风扇</option>
              <option value="heater">🔥 加热器</option>
              <option value="light">💡 照明灯</option>
            </select>
          </div>
        </div>
        <button class="save-linkage-btn" @click="saveLinkage">💾 保存配置</button>
      </div>
    </section>

    <!-- 故障与告警 -->
    <section class="alarm-section">
      <h2 class="section-title">🛠️ 故障与告警</h2>
      <div class="alarm-card">
        <div class="alarm-item">
          <div class="alarm-label">🔔 最近告警:</div>
          <div class="alarm-value">{{ device.alarmInfo.latestAlarm || '无' }}</div>
        </div>
        <div class="alarm-item">
          <div class="alarm-label">📶 信号强度:</div>
          <div class="alarm-value">{{ device.alarmInfo.signalStrength }}</div>
        </div>
      </div>
    </section>

    <!-- 详细规则弹窗 -->
    <div class="modal" v-if="showRuleDetails">
      <div class="modal-content">
        <h3 class="modal-title">详细规则</h3>
        <div class="rule-details">
          <div class="rule-detail-item" v-for="(rule, index) in device.strategy.rules" :key="index">
            <div class="rule-index">{{ index + 1 }}.</div>
            <div class="rule-text">{{ rule }}</div>
          </div>
        </div>
        <button class="close-btn" @click="showRuleDetails = false">关闭</button>
      </div>
    </div>

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
export default {
  name: 'BDeviceDetailView',
  props: {
    id: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      // 底部导航激活状态
      activeNav: 'devices',
      // 设备数据
      device: {
        id: 1,
        name: 'LoRa开关面板',
        type: 'switch',
        sn: 'SN789012',
        location: '302室主卧浴室',
        status: 'online',
        lastActive: '2025-12-15 14:25',
        realTimeData: {
          humidity: 72,
          temperature: 23,
          riskValue: 68,
          riskLevel: 'medium'
        },
        strategy: {
          enabled: true,
          rules: [
            '• 湿度 > 85% 且持续 30min ➡️ 开启排风扇',
            '• 30min 后湿度仍 > 60% ➡️ 开启加热烘干'
          ]
        },
        linkageMappings: [
          { device: 'fan' },
          { device: 'heater' },
          { device: 'light' }
        ],
        alarmInfo: {
          latestAlarm: '无',
          signalStrength: '🟢 良好 (RSSI -95dBm)'
        }
      },
      // 风险等级映射
      riskLevelMap: {
        high: '高风险',
        medium: '中风险',
        low: '低风险'
      },
      // 详细规则弹窗
      showRuleDetails: false
    }
  },
  mounted() {
    this.loadDeviceData();
  },
  methods: {
    // 加载设备数据
    loadDeviceData() {
      // 模拟从API获取设备数据
      console.log('加载设备数据:', this.id);
    },
    // 返回上一页
    goBack() {
      this.$router.go(-1);
    },
    // 更新策略
    updateStrategy() {
      console.log('更新策略:', this.device.strategy.enabled);
      alert('策略已更新');
    },
    // 更新联动映射
    updateLinkage() {
      console.log('更新联动映射:', this.device.linkageMappings);
    },
    // 保存联动配置
    saveLinkage() {
      console.log('保存联动配置:', this.device.linkageMappings);
      alert('联动配置已保存');
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
    }
  }
}
</script>

<style scoped>
.device-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  background-color: #f5f5f5;
  min-height: 100vh;
  padding: 20px;
}

.header {
  display: flex;
  align-items: center;
  gap: 16px;
  background-color: #fff;
  padding: 16px 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.back-btn {
  background-color: #f0f0f0;
  border: none;
  padding: 8px 12px;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.back-btn:hover {
  background-color: #e0e0e0;
}

.title {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
  color: #333;
}

.device-info-section, .real-time-section, .strategy-section, .linkage-section, .alarm-section {
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #333;
}

.device-info-card, .status-card, .strategy-card, .linkage-card, .alarm-card {
  background-color: #f9f9f9;
  padding: 16px;
  border-radius: 8px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #e0e0e0;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  font-weight: 500;
  color: #666;
}

.info-value {
  font-weight: 600;
  color: #333;
}

.info-value.online {
  color: #4CAF50;
}

.info-value.offline {
  color: #F44336;
}

.status-card {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
}

.status-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
  padding: 16px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.status-label {
  font-size: 14px;
  color: #666;
}

.status-value {
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.status-value.high {
  color: #F44336;
}

.status-value.medium {
  color: #FFC107;
}

.status-value.low {
  color: #4CAF50;
}

.strategy-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.strategy-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

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

input:focus + .toggle-slider {
  box-shadow: 0 0 1px #4CAF50;
}

input:checked + .toggle-slider:before {
  transform: translateX(24px);
}

.strategy-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.strategy-rules {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-left: 62px;
}

.rules-title {
  font-weight: 600;
  color: #333;
}

.rules-list {
  margin: 0;
  padding-left: 24px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-rules-btn {
  align-self: flex-start;
  background-color: #2196F3;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.detail-rules-btn:hover {
  background-color: #1976D2;
}

.linkage-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.linkage-item:last-child {
  margin-bottom: 0;
}

.mapping-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.mapping-label {
  font-weight: 500;
  color: #666;
}

.mapping-select select {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
}

.save-linkage-btn {
  margin-top: 16px;
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.save-linkage-btn:hover {
  background-color: #45a049;
}

.alarm-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
}

.alarm-label {
  font-weight: 500;
  color: #666;
}

.alarm-value {
  font-weight: 600;
  color: #333;
}

/* 弹窗样式 */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
}

.modal-content {
  background-color: #fff;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  width: 90%;
  max-width: 500px;
}

.modal-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 20px;
  text-align: center;
}

.rule-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 24px;
}

.rule-detail-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.rule-index {
  font-weight: 600;
  color: #333;
  min-width: 24px;
  text-align: right;
}

.rule-text {
  flex: 1;
  line-height: 1.5;
}

.close-btn {
  width: 100%;
  padding: 12px;
  background-color: #f0f0f0;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.close-btn:hover {
  background-color: #e0e0e0;
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

.device-detail-container {
  padding-bottom: 80px; /* 为底部导航栏留出空间 */
}

@media (max-width: 768px) {
  .device-detail-container {
  padding: 0 0 80px 0;
}
  
  .header {
    margin: 12px;
    padding: 12px 16px;
  }
  
  .title {
    font-size: 20px;
  }
  
  .device-info-section, .real-time-section, .strategy-section, .linkage-section, .alarm-section {
    margin: 12px;
    padding: 16px;
  }
  
  .status-card {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .strategy-rules {
    margin-left: 0;
  }
  
  .save-linkage-btn {
    width: 100%;
  }
}
</style>