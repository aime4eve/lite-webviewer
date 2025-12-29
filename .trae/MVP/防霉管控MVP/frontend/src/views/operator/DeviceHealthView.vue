<template>
  <OperatorLayout pageTitle="🩺 设备健康管理" activeNav="health">
    <div class="device-health-container">
      <!-- 健康概览 -->
      <section class="health-overview-section">
        <div class="stats-card">
          <div class="stat-item">
            <span class="stat-label">设备总数</span>
            <span class="stat-value">{{ totalDevices }}</span>
          </div>
          <div class="stat-item healthy">
            <span class="stat-label">健康设备 (95%+)</span>
            <span class="stat-value">{{ healthyDevices }}</span>
          </div>
          <div class="stat-item warning">
            <span class="stat-label">亚健康 (80-95%)</span>
            <span class="stat-value">{{ warningDevices }}</span>
          </div>
          <div class="stat-item critical">
            <span class="stat-label">不健康 (<80%)</span>
            <span class="stat-value">{{ criticalDevices }}</span>
          </div>
        </div>
      </section>

      <!-- 设备健康列表 -->
      <section class="health-list-section">
        <div class="section-header">
          <h2 class="section-title">设备健康列表</h2>
          <div class="search-bar">
            <input type="text" placeholder="搜索设备SN" v-model="searchKeyword">
            <button class="search-btn">🔍</button>
          </div>
        </div>

        <div class="health-list">
          <div class="health-header">
            <div class="col device-sn">SN码</div>
            <div class="col device-name">设备名称</div>
            <div class="col health-score">健康分</div>
            <div class="col battery">电池</div>
            <div class="col signal">信号</div>
            <div class="col actions">操作</div>
          </div>
          <div class="health-item" v-for="device in filteredDevices" :key="device.id">
            <div class="col device-sn">{{ device.sn }}</div>
            <div class="col device-name">{{ device.name }}</div>
            <div class="col health-score">
              <span class="score-badge" :class="getScoreClass(device.score)">{{ device.score }}</span>
            </div>
            <div class="col battery">
                <div class="battery-bar">
                    <div class="battery-level" :style="{ width: device.battery + '%' }" :class="getBatteryClass(device.battery)"></div>
                </div>
                {{ device.battery }}%
            </div>
            <div class="col signal">{{ device.signal }} dBm</div>
            <div class="col actions">
              <button class="view-btn" @click="viewFingerprint(device)">查看指纹</button>
            </div>
          </div>
        </div>
      </section>

      <!-- 健康指纹弹窗 -->
      <div class="modal" v-if="showFingerprintModal">
        <div class="modal-content">
          <h3 class="modal-title">🧬 设备健康指纹</h3>
          <div class="device-info">
             <p>设备: {{ currentDevice.name }} ({{ currentDevice.sn }})</p>
             <p>健康分: <span :class="getScoreClass(currentDevice.score)">{{ currentDevice.score }}</span></p>
          </div>
          <div class="fingerprint-chart">
            <!-- 模拟雷达图或指标列表 -->
            <div class="fingerprint-item">
               <span>网络稳定性</span>
               <div class="progress"><div class="bar" style="width: 95%"></div></div>
               <span>95</span>
            </div>
            <div class="fingerprint-item">
               <span>电池损耗</span>
               <div class="progress"><div class="bar" style="width: 88%"></div></div>
               <span>88</span>
            </div>
            <div class="fingerprint-item">
               <span>传感器精度</span>
               <div class="progress"><div class="bar" style="width: 92%"></div></div>
               <span>92</span>
            </div>
            <div class="fingerprint-item">
               <span>环境适应性</span>
               <div class="progress"><div class="bar" style="width: 85%"></div></div>
               <span>85</span>
            </div>
          </div>
          <div class="recommendation">
             <h4>💡 维护建议</h4>
             <p>电池状态良好，网络连接稳定。建议关注环境适应性指标，可能是由于近期湿度波动较大导致。</p>
          </div>
          <button class="close-btn" @click="showFingerprintModal = false">关闭</button>
        </div>
      </div>
    </div>
  </OperatorLayout>
</template>

<script>
import OperatorLayout from '../../components/OperatorLayout.vue'

export default {
  name: 'DeviceHealthView',
  components: {
    OperatorLayout
  },
  data() {
    return {
      searchKeyword: '',
      devices: [
        { id: 1, sn: 'SN123456', name: '温湿度传感器', score: 98, battery: 90, signal: -65 },
        { id: 2, sn: 'SN789012', name: 'LoRa开关面板', score: 85, battery: 100, signal: -70 },
        { id: 3, sn: 'SN345678', name: '温湿度传感器', score: 72, battery: 45, signal: -85 },
        { id: 4, sn: 'SN901234', name: 'LoRa开关面板', score: 95, battery: 100, signal: -60 },
        { id: 5, sn: 'SN112233', name: '温湿度传感器', score: 60, battery: 20, signal: -90 },
      ],
      showFingerprintModal: false,
      currentDevice: null
    }
  },
  computed: {
    totalDevices() { return this.devices.length },
    healthyDevices() { return this.devices.filter(d => d.score >= 95).length },
    warningDevices() { return this.devices.filter(d => d.score >= 80 && d.score < 95).length },
    criticalDevices() { return this.devices.filter(d => d.score < 80).length },
    filteredDevices() {
        if (!this.searchKeyword) return this.devices;
        return this.devices.filter(d => d.sn.toLowerCase().includes(this.searchKeyword.toLowerCase()));
    }
  },
  methods: {
    getScoreClass(score) {
        if (score >= 95) return 'score-excellent';
        if (score >= 80) return 'score-good';
        if (score >= 60) return 'score-warning';
        return 'score-critical';
    },
    getBatteryClass(level) {
        if (level > 50) return 'bg-green';
        if (level > 20) return 'bg-orange';
        return 'bg-red';
    },
    viewFingerprint(device) {
        this.currentDevice = device;
        this.showFingerprintModal = true;
    }
  }
}
</script>

<style scoped>
.device-health-container {
  max-width: 1400px;
  margin: 0 auto;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.health-overview-section {
  padding: 24px;
}

.stats-card {
  display: flex;
  justify-content: space-around;
  background-color: white;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
}

.stat-item.healthy .stat-value { color: #27ae60; }
.stat-item.warning .stat-value { color: #f39c12; }
.stat-item.critical .stat-value { color: #e74c3c; }

.health-list-section {
  padding: 0 24px 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.search-bar {
  display: flex;
  gap: 8px;
}
.search-bar input {
    padding: 8px;
    border: 1px solid #ddd;
    border-radius: 4px;
}
.search-btn {
    padding: 8px 16px;
    background-color: #3498db;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
}

.health-list {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.health-header {
  display: flex;
  padding: 12px 16px;
  background-color: #f8f9fa;
  font-weight: 600;
  border-bottom: 1px solid #eee;
}

.health-item {
  display: flex;
  padding: 16px;
  border-bottom: 1px solid #eee;
  align-items: center;
}

.col { flex: 1; }
.col.device-sn { flex: 1.5; }
.col.device-name { flex: 1.5; }
.col.actions { text-align: right; }

.score-badge {
    padding: 4px 8px;
    border-radius: 4px;
    font-weight: bold;
    color: white;
}
.score-excellent { background-color: #27ae60; }
.score-good { background-color: #2ecc71; }
.score-warning { background-color: #f39c12; }
.score-critical { background-color: #e74c3c; }

.battery-bar {
    width: 60px;
    height: 8px;
    background-color: #eee;
    border-radius: 4px;
    display: inline-block;
    margin-right: 8px;
    overflow: hidden;
}
.battery-level { height: 100%; }
.bg-green { background-color: #27ae60; }
.bg-orange { background-color: #f39c12; }
.bg-red { background-color: #e74c3c; }

.view-btn {
    background-color: #3498db;
    color: white;
    border: none;
    padding: 6px 12px;
    border-radius: 4px;
    cursor: pointer;
}

/* Modal */
.modal {
  position: fixed;
  top: 0; left: 0; width: 100%; height: 100%;
  background-color: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
}
.modal-content {
  background-color: white;
  padding: 24px;
  border-radius: 8px;
  width: 500px;
}
.device-info {
    margin-bottom: 16px;
    font-size: 16px;
}
.fingerprint-item {
    display: flex;
    align-items: center;
    margin-bottom: 12px;
}
.progress {
    flex: 1;
    height: 10px;
    background-color: #eee;
    margin: 0 12px;
    border-radius: 5px;
    overflow: hidden;
}
.bar {
    height: 100%;
    background-color: #3498db;
}
.recommendation {
    background-color: #fff8e1;
    padding: 12px;
    border-radius: 6px;
    margin-top: 16px;
}
.recommendation h4 { margin: 0 0 8px 0; font-size: 14px; color: #f57c00; }
.recommendation p { margin: 0; font-size: 13px; color: #666; }

.close-btn {
    width: 100%;
    padding: 10px;
    background-color: #f0f0f0;
    border: none;
    border-radius: 6px;
    margin-top: 16px;
    cursor: pointer;
}
</style>
