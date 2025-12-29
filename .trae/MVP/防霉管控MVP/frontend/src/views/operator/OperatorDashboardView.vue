<template>
  <OperatorLayout pageTitle="💻 运维控制台 · 防霉管控系统" activeNav="dashboard">
    <div class="operator-dashboard-container">

    <!-- 系统概览卡片 -->
    <section class="overview-section">
      <div class="overview-cards">
        <div class="overview-card">
          <div class="card-header">
            <h3 class="card-title">👥 用户总数</h3>
          </div>
          <div class="card-content">
            <div class="card-value">{{ totalUsers }}</div>
            <div class="card-trend" :class="userTrend.class">
              {{ userTrend.text }} ({{ userTrend.value }})
            </div>
          </div>
        </div>
        <div class="overview-card">
          <div class="card-header">
            <h3 class="card-title">📱 设备总数</h3>
          </div>
          <div class="card-content">
            <div class="card-value">{{ totalDevices }}</div>
            <div class="card-trend" :class="deviceTrend.class">
              {{ deviceTrend.text }} ({{ deviceTrend.value }})
            </div>
          </div>
        </div>
        <div class="overview-card">
          <div class="card-header">
            <h3 class="card-title">🔔 今日告警</h3>
          </div>
          <div class="card-content">
            <div class="card-value">{{ todayAlarms }}</div>
            <div class="card-trend" :class="alarmTrend.class">
              {{ alarmTrend.text }} ({{ alarmTrend.value }})
            </div>
          </div>
        </div>
        <div class="overview-card">
          <div class="card-header">
            <h3 class="card-title">💳 活跃订阅</h3>
          </div>
          <div class="card-content">
            <div class="card-value">{{ activeSubscriptions }}</div>
            <div class="card-trend" :class="subscriptionTrend.class">
              {{ subscriptionTrend.text }} ({{ subscriptionTrend.value }})
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 告警与工单面板 -->
    <section class="alarms-section">
      <div class="section-header">
        <h2 class="section-title">🔔 告警与工单面板</h2>
        <div class="section-actions">
<<<<<<< HEAD
          <select class="filter-select" v-model="alarmFilter.type">
            <option value="all">所有类型</option>
            <option value="tamper">🛠️ 防拆告警</option>
            <option value="offline">🔌 心跳丢失</option>
            <option value="risk">⚠️ 高风险</option>
          </select>
          <select class="filter-select" v-model="alarmFilter.status">
            <option value="all">所有状态</option>
            <option value="unhandled">❌ 未处理</option>
            <option value="handled">✅ 已处理</option>
          </select>
=======
          <button class="filter-btn" @click="navigateToAlarms">🔍 高级告警管理</button>
          <button class="filter-btn" @click="navigateToDiagnostics">🛠️ 远程诊断</button>
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
        </div>
      </div>

      <!-- 告警列表 -->
      <div class="alarms-list">
        <div class="alarms-header">
          <div class="alarm-column alarm-title">告警标题</div>
          <div class="alarm-column alarm-type">类型</div>
          <div class="alarm-column alarm-location">位置</div>
          <div class="alarm-column alarm-time">时间</div>
          <div class="alarm-column alarm-status">状态</div>
          <div class="alarm-column alarm-actions">操作</div>
        </div>
        <div 
          class="alarm-item" 
          v-for="alarm in filteredAlarms" 
          :key="alarm.id"
          :class="alarm.status"
        >
          <div class="alarm-column alarm-title">{{ alarm.title }}</div>
<<<<<<< HEAD
          <div class="alarm-column alarm-type">{{ alarm.type }}</div>
=======
          <div class="alarm-column alarm-type">{{ alarm.typeDisplay || alarm.type }}</div>
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
          <div class="alarm-column alarm-location">{{ alarm.location }}</div>
          <div class="alarm-column alarm-time">{{ alarm.time }}</div>
          <div class="alarm-column alarm-status">
            {{ alarm.status === 'unhandled' ? '❌ 未处理' : '✅ 已处理' }}
          </div>
          <div class="alarm-column alarm-actions">
            <button class="view-btn" @click="viewAlarm(alarm)">查看</button>
            <button class="handle-btn" @click="handleAlarm(alarm)">
              {{ alarm.status === 'unhandled' ? '处理' : '重新处理' }}
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- 设备状态分布 -->
    <section class="device-status-section">
      <h2 class="section-title">📊 设备状态分布</h2>
      <div class="device-status-cards">
        <div class="status-card online">
          <div class="status-icon">🟢</div>
          <div class="status-info">
            <div class="status-label">在线设备</div>
            <div class="status-value">{{ onlineDevices }}</div>
            <div class="status-percentage">{{ Math.round((onlineDevices / totalDevices) * 100) }}%</div>
          </div>
        </div>
        <div class="status-card offline">
          <div class="status-icon">🔴</div>
          <div class="status-info">
            <div class="status-label">离线设备</div>
            <div class="status-value">{{ offlineDevices }}</div>
            <div class="status-percentage">{{ Math.round((offlineDevices / totalDevices) * 100) }}%</div>
          </div>
        </div>
        <div class="status-card warning">
          <div class="status-icon">🟠</div>
          <div class="status-info">
            <div class="status-label">异常设备</div>
            <div class="status-value">{{ warningDevices }}</div>
            <div class="status-percentage">{{ Math.round((warningDevices / totalDevices) * 100) }}%</div>
          </div>
        </div>
      </div>
    </section>
<<<<<<< HEAD
=======

    <!-- 资产保全模态框 -->
    <AssetProtectionModal 
      v-if="showAssetModal" 
      :fault="selectedFault" 
      @close="showAssetModal = false"
      @resolved="onAssetResolved"
    />
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
  </div>
  </OperatorLayout>
</template>

<script>
import OperatorLayout from '../../components/OperatorLayout.vue'
<<<<<<< HEAD
=======
import AssetProtectionModal from '../../components/operator/AssetProtectionModal.vue'
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5

export default {
  name: 'OperatorDashboardView',
  components: {
<<<<<<< HEAD
    OperatorLayout
=======
    OperatorLayout,
    AssetProtectionModal
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
  },
  data() {
    return {
      // 活跃导航
      activeNav: 'dashboard',
      // 系统概览数据
      totalUsers: 1256,
      totalDevices: 3892,
      todayAlarms: 45,
      activeSubscriptions: 987,
      // 趋势数据
      userTrend: { text: '↑ 增长', value: '+12.5%', class: 'trend-up' },
      deviceTrend: { text: '↑ 增长', value: '+8.3%', class: 'trend-up' },
      alarmTrend: { text: '↓ 下降', value: '-23.1%', class: 'trend-down' },
      subscriptionTrend: { text: '↑ 增长', value: '+15.7%', class: 'trend-up' },
      // 设备状态分布
      onlineDevices: 3689,
      offlineDevices: 178,
      warningDevices: 25,
      // 告警列表
      alarms: [
        {
          id: 1,
          title: '金南家园三期 3502 防拆告警',
<<<<<<< HEAD
          type: '🛠️ 防拆告警',
=======
          type: 'tamper', // Use code for logic, display mapped in template or computed
          typeDisplay: '🛠️ 防拆告警',
          deviceSn: 'SN123456',
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
          location: '金南家园三期 3502',
          time: '2025-12-15 14:30',
          status: 'unhandled'
        },
        {
          id: 2,
          title: 'XX公寓 1201 心跳丢失',
<<<<<<< HEAD
          type: '🔌 心跳丢失',
=======
          type: 'offline',
          typeDisplay: '🔌 心跳丢失',
          deviceSn: 'SN789012',
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
          location: 'XX公寓 1201',
          time: '2025-12-15 14:25',
          status: 'unhandled'
        },
        {
          id: 3,
          title: '阳光花园 708 高风险告警',
<<<<<<< HEAD
          type: '⚠️ 高风险',
=======
          type: 'risk',
          typeDisplay: '⚠️ 高风险',
          deviceSn: 'SN345678',
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
          location: '阳光花园 708',
          time: '2025-12-15 14:20',
          status: 'handled'
        },
        {
          id: 4,
          title: '金南家园一期 102 防拆告警',
<<<<<<< HEAD
          type: '🛠️ 防拆告警',
=======
          type: 'tamper',
          typeDisplay: '🛠️ 防拆告警',
          deviceSn: 'SN901234',
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
          location: '金南家园一期 102',
          time: '2025-12-15 14:15',
          status: 'handled'
        },
        {
          id: 5,
          title: 'XX小区 503 心跳丢失',
<<<<<<< HEAD
          type: '🔌 心跳丢失',
=======
          type: 'offline',
          typeDisplay: '🔌 心跳丢失',
          deviceSn: 'SN567890',
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
          location: 'XX小区 503',
          time: '2025-12-15 14:10',
          status: 'unhandled'
        }
      ],
      // 告警过滤器
      alarmFilter: {
        type: 'all',
        status: 'all'
<<<<<<< HEAD
      }
=======
      },
      // 资产保全模态框控制
      showAssetModal: false,
      selectedFault: null
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
    }
  },
  methods: {
    // 导航方法
    navigateToDashboard() {
      this.$router.push('/operator/')
      this.activeNav = 'dashboard'
    },
    navigateToUserManagement() {
      this.$router.push('/operator/user-management')
      this.activeNav = 'users'
    },
    navigateToDeviceRegistration() {
      this.$router.push('/operator/device-registration')
      this.activeNav = 'devices'
    },
    navigateToFaultMonitoring() {
      this.$router.push('/operator/device-fault-monitoring')
      this.activeNav = 'alarms'
    },
    navigateToStrategyManagement() {
      this.$router.push('/operator/strategy-management')
      this.activeNav = 'strategies'
    },
    navigateToBillingManagement() {
      this.$router.push('/operator/billing-management')
      this.activeNav = 'billing'
    },
<<<<<<< HEAD
    // 查看告警
    viewAlarm(alarm) {
      alert(`查看告警：${alarm.title}`)
    },
    // 处理告警
    handleAlarm(alarm) {
      if (alarm.status === 'unhandled') {
=======
    navigateToAlarms() {
      this.$router.push('/operator/alarms')
    },
    navigateToDiagnostics() {
      this.$router.push('/operator/diagnostics')
    },
    // 查看告警
    viewAlarm(alarm) {
      if (alarm.type === 'tamper') {
          this.selectedFault = alarm;
          this.showAssetModal = true;
      } else {
          alert(`查看告警：${alarm.title}`);
      }
    },
    // 处理告警
    handleAlarm(alarm) {
      if (alarm.type === 'tamper') {
          this.selectedFault = alarm;
          this.showAssetModal = true;
      } else if (alarm.status === 'unhandled') {
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
        alarm.status = 'handled'
        alert(`处理告警：${alarm.title}`)
      } else {
        alarm.status = 'unhandled'
        alert(`重新处理告警：${alarm.title}`)
      }
<<<<<<< HEAD
=======
    },
    onAssetResolved(fault) {
        // Find the alarm and update status
        const alarm = this.alarms.find(a => a.id === fault.id);
        if (alarm) {
            alarm.status = 'handled';
        }
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
    }
  },
  computed: {
    // 过滤后的告警列表
    filteredAlarms() {
      return this.alarms.filter(alarm => {
<<<<<<< HEAD
        const typeMatch = this.alarmFilter.type === 'all' || alarm.type.includes(this.alarmFilter.type)
=======
        const typeMatch = this.alarmFilter.type === 'all' || alarm.type === this.alarmFilter.type
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
        const statusMatch = this.alarmFilter.status === 'all' || alarm.status === this.alarmFilter.status
        return typeMatch && statusMatch
      })
    }
  }
}
</script>

<style scoped>
.operator-dashboard-container {
  max-width: 1400px;
  margin: 0 auto;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #2c3e50;
  color: white;
  padding: 16px 24px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.title {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
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
}

.logout-btn {
  background-color: #e74c3c;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.logout-btn:hover {
  background-color: #c0392b;
}

.main-nav {
  display: flex;
  background-color: #34495e;
  padding: 0 24px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.nav-item {
  padding: 16px 24px;
  color: white;
  cursor: pointer;
  transition: background-color 0.3s;
  font-weight: 500;
}

.nav-item:hover {
  background-color: #2c3e50;
}

.nav-item.active {
  background-color: #1abc9c;
}

.overview-section {
  padding: 24px;
}

.overview-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.overview-card {
  background-color: white;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s;
}

.overview-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.card-header {
  margin-bottom: 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 500;
  color: #666;
  margin: 0;
}

.card-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-value {
  font-size: 32px;
  font-weight: 600;
  color: #333;
}

.card-trend {
  font-size: 14px;
  font-weight: 500;
}

.trend-up {
  color: #27ae60;
}

.trend-down {
  color: #e74c3c;
}

.alarms-section {
  padding: 0 24px 24px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #333;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-actions {
  display: flex;
  gap: 12px;
}

.filter-select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.alarms-list {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.alarms-header {
  display: flex;
  background-color: #f8f9fa;
  padding: 12px 16px;
  font-weight: 600;
  border-bottom: 1px solid #e0e0e0;
}

.alarm-item {
  display: flex;
  padding: 16px;
  border-bottom: 1px solid #e0e0e0;
  transition: background-color 0.2s;
}

.alarm-item:hover {
  background-color: #f8f9fa;
}

.alarm-item.unhandled {
  background-color: #fff5f5;
}

.alarm-item.handled {
  background-color: #f0fff4;
}

.alarm-column {
  flex: 1;
}

.alarm-title {
  flex: 2;
  font-weight: 500;
}

.alarm-type, .alarm-location, .alarm-time, .alarm-status {
  flex: 1;
}

.alarm-actions {
  flex: 1;
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.view-btn, .handle-btn {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.view-btn {
  background-color: #3498db;
  color: white;
}

.view-btn:hover {
  background-color: #2980b9;
}

.handle-btn {
  background-color: #27ae60;
  color: white;
}

.handle-btn:hover {
  background-color: #229954;
}

.device-status-section {
  padding: 0 24px 24px;
}

.device-status-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.status-card {
  background-color: white;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 16px;
  transition: transform 0.2s;
}

.status-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.status-icon {
  font-size: 48px;
}

.status-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.status-label {
  font-size: 16px;
  font-weight: 500;
  color: #666;
}

.status-value {
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.status-percentage {
  font-size: 14px;
  color: #666;
}

.status-card.online {
  border-left: 4px solid #27ae60;
}

.status-card.offline {
  border-left: 4px solid #e74c3c;
}

.status-card.warning {
  border-left: 4px solid #f39c12;
}

/* PC端优化样式 */
/* 增强卡片悬浮效果 */
.overview-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
}

/* 增强告警项悬浮效果 */
.alarm-item:hover {
  transform: translateX(4px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

/* 增强状态卡片悬浮效果 */
.status-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

/* 增强按钮交互效果 */
.view-btn,
.handle-btn {
  transition: all 0.3s ease;
}

.view-btn:hover,
.handle-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
}

/* 增强导航交互 */
.nav-item {
  transition: all 0.3s ease;
}

.nav-item:hover {
  transform: translateY(-1px);
  background-color: #2980b9;
}

.nav-item.active {
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  transform: translateY(-1px);
}

/* 优化表单元素 */
.filter-select:focus {
  outline: 2px solid #1abc9c;
  border-color: #1abc9c;
  transition: all 0.2s ease;
}

/* 增强统计卡片视觉效果 */
.stat-item .stat-value {
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.stat-item:hover .stat-value {
  transform: scale(1.05);
}

/* 增强数据展示 */
.card-value {
  font-weight: 700;
}

/* 优化布局间距 */
.overview-section,
.alarms-section,
.device-status-section {
  margin-bottom: 24px;
}

/* 增强阴影效果 */
.header,
.main-nav,
.stats-card,
.faults-list {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

</style>