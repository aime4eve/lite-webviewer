<template>
  <div class="strategy-customization-container">
    <!-- 顶部标题栏 -->
    <header class="header">
      <h1 class="title">⚙️ 防霉策略配置</h1>
      <button class="save-btn" @click="saveStrategy">💾 保存策略</button>
    </header>

    <!-- 策略选择区 -->
    <section class="strategy-selection-section">
      <h2 class="section-title">📋 策略选择</h2>
      <div class="strategy-selector">
        <label class="radio-option">
          <input type="radio" v-model="strategyType" value="default" @change="loadDefaultStrategy">
          <span class="radio-text">使用默认策略</span>
        </label>
        <label class="radio-option">
          <input type="radio" v-model="strategyType" value="custom" @change="createCustomStrategy">
          <span class="radio-text">自定义策略</span>
        </label>
      </div>
    </section>

    <!-- 策略配置区 -->
    <section class="strategy-config-section">
      <h2 class="section-title">🔧 策略配置</h2>
      
      <!-- 策略基本信息 -->
      <div class="config-group">
        <h3 class="config-title">基本信息</h3>
        <div class="form-row">
          <div class="form-group">
            <label for="strategy-name">策略名称</label>
            <input type="text" id="strategy-name" v-model="currentStrategy.name" required>
          </div>
          <div class="form-group">
            <label for="strategy-season">适用季节</label>
            <select id="strategy-season" v-model="currentStrategy.season">
              <option value="all">全年</option>
              <option value="spring">春季</option>
              <option value="summer">夏季</option>
              <option value="autumn">秋季</option>
              <option value="winter">冬季</option>
            </select>
          </div>
        </div>
        <div class="form-group">
          <label for="strategy-scope">适用范围</label>
          <select id="strategy-scope" v-model="currentStrategy.scope">
            <option value="all">所有房间</option>
            <option value="selected">选定房间</option>
          </select>
        </div>
        <div class="selected-rooms" v-if="currentStrategy.scope === 'selected'">
          <label>选定房间：</label>
          <div class="room-tags">
            <span class="room-tag" v-for="room in selectedRooms" :key="room.id">
              {{ room.name }}
              <button class="remove-tag" @click="removeRoom(room.id)">×</button>
            </span>
            <button class="add-room-btn" @click="showSelectRoomModal = true">+ 添加房间</button>
          </div>
        </div>
      </div>

      <!-- 排风扇开启策略 -->
      <div class="config-group">
        <h3 class="config-title">🌬️ 排风扇开启策略</h3>
        <div class="form-row">
          <div class="form-group">
            <label for="fan-humidity-threshold">湿度阈值 (%)</label>
            <input type="number" id="fan-humidity-threshold" v-model.number="currentStrategy.fan.humidityThreshold" min="0" max="100" required>
          </div>
          <div class="form-group">
            <label for="fan-duration">持续时间 (分钟)</label>
            <input type="number" id="fan-duration" v-model.number="currentStrategy.fan.duration" min="1" max="120" required>
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label for="fan-start-time">开始时间</label>
            <input type="time" id="fan-start-time" v-model="currentStrategy.fan.startTime">
          </div>
          <div class="form-group">
            <label for="fan-end-time">结束时间</label>
            <input type="time" id="fan-end-time" v-model="currentStrategy.fan.endTime">
          </div>
        </div>
      </div>

      <!-- 加热器开启策略 -->
      <div class="config-group">
        <h3 class="config-title">🔥 加热器开启策略</h3>
        <div class="form-row">
          <div class="form-group">
            <label for="heater-humidity-threshold">湿度阈值 (%)</label>
            <input type="number" id="heater-humidity-threshold" v-model.number="currentStrategy.heater.humidityThreshold" min="0" max="100" required>
          </div>
          <div class="form-group">
            <label for="heater-temperature-threshold">温度阈值 (°C)</label>
            <input type="number" id="heater-temperature-threshold" v-model.number="currentStrategy.heater.temperatureThreshold" min="0" max="50" required>
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label for="heater-delay">延迟启动 (分钟)</label>
            <input type="number" id="heater-delay" v-model.number="currentStrategy.heater.delay" min="0" max="60" required>
          </div>
          <div class="form-group">
            <label for="heater-duration">持续时间 (分钟)</label>
            <input type="number" id="heater-duration" v-model.number="currentStrategy.heater.duration" min="1" max="120" required>
          </div>
        </div>
      </div>

      <!-- 设备联动策略 -->
      <div class="config-group">
        <h3 class="config-title">🔗 设备联动策略</h3>
        <div class="form-group">
          <label class="checkbox-option">
            <input type="checkbox" v-model="currentStrategy.linkage.enabled">
            <span class="checkbox-text">启用设备联动</span>
          </label>
        </div>
        <div class="linkage-config" v-if="currentStrategy.linkage.enabled">
          <div class="form-group">
            <label for="linkage-condition">联动条件</label>
            <select id="linkage-condition" v-model="currentStrategy.linkage.condition">
              <option value="fan_ineffective">排风扇效果不佳</option>
              <option value="high_risk">高风险状态</option>
              <option value="both">以上两者</option>
            </select>
          </div>
          <div class="form-group">
            <label for="linkage-sequence">联动顺序</label>
            <select id="linkage-sequence" v-model="currentStrategy.linkage.sequence">
              <option value="fan_first">先开排风扇，后开加热器</option>
              <option value="heater_first">先开加热器，后排风扇</option>
              <option value="simultaneous">同时开启</option>
            </select>
          </div>
        </div>
      </div>

      <!-- 策略执行时机 -->
      <div class="config-group">
        <h3 class="config-title">⏰ 策略执行时机</h3>
        <div class="form-row">
          <div class="form-group">
            <label for="execution-frequency">执行频率</label>
            <select id="execution-frequency" v-model="currentStrategy.execution.frequency">
              <option value="continuous">持续监控</option>
              <option value="interval">定时执行</option>
              <option value="scheduled">按计划执行</option>
            </select>
          </div>
          <div class="form-group" v-if="currentStrategy.execution.frequency === 'interval'">
            <label for="execution-interval">执行间隔 (分钟)</label>
            <input type="number" id="execution-interval" v-model.number="currentStrategy.execution.interval" min="5" max="1440" required>
          </div>
        </div>
        <div class="form-group">
          <label for="stop-condition">停止条件</label>
          <select id="stop-condition" v-model="currentStrategy.execution.stopCondition">
            <option value="humidity_normal">湿度恢复正常</option>
            <option value="time_elapsed">达到指定时间</option>
            <option value="both">以上两者</option>
          </select>
        </div>
      </div>

      <!-- 人工干预设置 -->
      <div class="config-group">
        <h3 class="config-title">👤 人工干预设置</h3>
        <div class="form-group">
          <label class="checkbox-option">
            <input type="checkbox" v-model="currentStrategy.manualIntervention.required">
            <span class="checkbox-text">需要人工确认后执行</span>
          </label>
        </div>
        <div class="form-group" v-if="currentStrategy.manualIntervention.required">
          <label for="intervention-timeout">干预超时时间 (分钟)</label>
          <input type="number" id="intervention-timeout" v-model.number="currentStrategy.manualIntervention.timeout" min="1" max="60" required>
        </div>
        <div class="form-group">
          <label class="checkbox-option">
            <input type="checkbox" v-model="currentStrategy.manualIntervention.notification">
            <span class="checkbox-text">执行前发送通知</span>
          </label>
        </div>
      </div>
    </section>

    <!-- 策略预览区 -->
    <section class="strategy-preview-section">
      <h2 class="section-title">👀 策略预览</h2>
      <div class="preview-content">
        <h3 class="preview-title">{{ currentStrategy.name }}</h3>
        <div class="preview-item">
          <strong>适用范围：</strong>
          <span>{{ currentStrategy.scope === 'all' ? '所有房间' : selectedRooms.map(r => r.name).join(', ') }}</span>
        </div>
        <div class="preview-item">
          <strong>适用季节：</strong>
          <span>{{ seasonMap[currentStrategy.season] }}</span>
        </div>
        <div class="preview-item">
          <strong>排风扇策略：</strong>
          <span>
            当湿度 > {{ currentStrategy.fan.humidityThreshold }}% 持续 {{ currentStrategy.fan.duration }} 分钟，
            {{ currentStrategy.fan.startTime ? `在 ${currentStrategy.fan.startTime} - ${currentStrategy.fan.endTime} 期间` : '' }}
            开启排风扇
          </span>
        </div>
        <div class="preview-item">
          <strong>加热器策略：</strong>
          <span>
            当湿度 > {{ currentStrategy.heater.humidityThreshold }}% 且温度 < {{ currentStrategy.heater.temperatureThreshold }}°C 时，
            延迟 {{ currentStrategy.heater.delay }} 分钟后开启加热器，持续 {{ currentStrategy.heater.duration }} 分钟
          </span>
        </div>
        <div class="preview-item">
          <strong>设备联动：</strong>
          <span>
            {{ currentStrategy.linkage.enabled ? 
              `启用，当 ${linkageConditionMap[currentStrategy.linkage.condition]} 时，${linkageSequenceMap[currentStrategy.linkage.sequence]}` : 
              '未启用' }}
          </span>
        </div>
        <div class="preview-item">
          <strong>执行时机：</strong>
          <span>
            {{ executionFrequencyMap[currentStrategy.execution.frequency] }}
            {{ currentStrategy.execution.frequency === 'interval' ? `，间隔 ${currentStrategy.execution.interval} 分钟` : '' }}
            ，{{ stopConditionMap[currentStrategy.execution.stopCondition] }} 时停止
          </span>
        </div>
        <div class="preview-item">
          <strong>人工干预：</strong>
          <span>
            {{ currentStrategy.manualIntervention.required ? 
              `需要人工确认，超时 ${currentStrategy.manualIntervention.timeout} 分钟后自动执行` : 
              '自动执行，无需人工干预' }}
            {{ currentStrategy.manualIntervention.notification ? '，执行前发送通知' : '' }}
          </span>
        </div>
      </div>
    </section>

    <!-- 选择房间弹窗 -->
    <div class="modal" v-if="showSelectRoomModal">
      <div class="modal-content">
        <h3 class="modal-title">选择房间</h3>
        <div class="room-list">
          <label class="checkbox-option" v-for="room in availableRooms" :key="room.id">
            <input type="checkbox" v-model="selectedRoomIds" :value="room.id">
            <span class="checkbox-text">{{ room.name }}</span>
          </label>
        </div>
        <div class="form-actions">
          <button type="button" class="cancel-btn" @click="showSelectRoomModal = false">取消</button>
          <button type="button" class="confirm-btn" @click="confirmRoomSelection">确定</button>
        </div>
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
  name: 'BStrategyCustomizationView',
  data() {
    return {
      // 底部导航激活状态
      activeNav: 'home',
      // 策略类型
      strategyType: 'default',
      // 当前策略
      currentStrategy: {},
      // 默认策略
      defaultStrategy: {
        name: '默认防霉策略',
        season: 'all',
        scope: 'all',
        fan: {
          humidityThreshold: 85,
          duration: 30,
          startTime: '',
          endTime: ''
        },
        heater: {
          humidityThreshold: 80,
          temperatureThreshold: 20,
          delay: 30,
          duration: 30
        },
        linkage: {
          enabled: true,
          condition: 'fan_ineffective',
          sequence: 'fan_first'
        },
        execution: {
          frequency: 'continuous',
          interval: 30,
          stopCondition: 'both'
        },
        manualIntervention: {
          required: false,
          timeout: 10,
          notification: true
        }
      },
      // 自定义策略
      customStrategy: {
        name: '自定义防霉策略',
        season: 'all',
        scope: 'all',
        fan: {
          humidityThreshold: 80,
          duration: 20,
          startTime: '',
          endTime: ''
        },
        heater: {
          humidityThreshold: 75,
          temperatureThreshold: 18,
          delay: 20,
          duration: 20
        },
        linkage: {
          enabled: false,
          condition: 'high_risk',
          sequence: 'simultaneous'
        },
        execution: {
          frequency: 'interval',
          interval: 20,
          stopCondition: 'humidity_normal'
        },
        manualIntervention: {
          required: true,
          timeout: 5,
          notification: true
        }
      },
      // 房间数据
      availableRooms: [
        { id: 1, name: '主卧浴室', type: '浴室' },
        { id: 2, name: '次卧浴室', type: '浴室' },
        { id: 3, name: '客厅', type: '客厅' },
        { id: 4, name: '厨房', type: '厨房' }
      ],
      selectedRooms: [],
      selectedRoomIds: [],
      showSelectRoomModal: false,
      // 映射表
      seasonMap: {
        all: '全年',
        spring: '春季',
        summer: '夏季',
        autumn: '秋季',
        winter: '冬季'
      },
      linkageConditionMap: {
        fan_ineffective: '排风扇效果不佳',
        high_risk: '高风险状态',
        both: '排风扇效果不佳或高风险状态'
      },
      linkageSequenceMap: {
        fan_first: '先开排风扇，后开加热器',
        heater_first: '先开加热器，后排风扇',
        simultaneous: '同时开启排风扇和加热器'
      },
      executionFrequencyMap: {
        continuous: '持续监控',
        interval: '定时执行',
        scheduled: '按计划执行'
      },
      stopConditionMap: {
        humidity_normal: '湿度恢复正常',
        time_elapsed: '达到指定时间',
        both: '湿度恢复正常或达到指定时间'
      }
    }
  },
  mounted() {
    this.loadDefaultStrategy();
  },
  methods: {
    loadDefaultStrategy() {
      // 加载默认策略
      this.currentStrategy = JSON.parse(JSON.stringify(this.defaultStrategy));
      this.selectedRooms = [];
      this.selectedRoomIds = [];
    },
    createCustomStrategy() {
      // 创建自定义策略
      this.currentStrategy = JSON.parse(JSON.stringify(this.customStrategy));
      this.selectedRooms = [];
      this.selectedRoomIds = [];
    },
    saveStrategy() {
      // 保存策略
      alert('策略保存成功！');
    },
    confirmRoomSelection() {
      // 确认房间选择
      this.selectedRooms = this.availableRooms.filter(room => 
        this.selectedRoomIds.includes(room.id)
      );
      this.showSelectRoomModal = false;
    },
    removeRoom(roomId) {
      // 移除房间
      this.selectedRooms = this.selectedRooms.filter(room => room.id !== roomId);
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
.strategy-customization-container {
  max-width: 1200px;
  margin: 0 auto;
  background-color: #f5f5f5;
  min-height: 100vh;
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #fff;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.title {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
}

.save-btn {
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.save-btn:hover {
  background-color: #45a049;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #333;
}

.strategy-selection-section,
.strategy-config-section,
.strategy-preview-section {
  background-color: #fff;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.strategy-selector {
  display: flex;
  gap: 24px;
}

.radio-option {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 16px;
}

.radio-option input[type="radio"] {
  width: 18px;
  height: 18px;
}

.config-group {
  margin-bottom: 24px;
}

.config-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #333;
}

.form-row {
  display: flex;
  gap: 24px;
  margin-bottom: 16px;
}

.form-group {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-weight: 500;
  font-size: 14px;
  color: #666;
}

.form-group input,
.form-group select {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
}

.checkbox-option {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 16px;
}

.checkbox-option input[type="checkbox"] {
  width: 18px;
  height: 18px;
}

.selected-rooms {
  margin-top: 12px;
}

.room-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 8px;
}

.room-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background-color: #e3f2fd;
  color: #2196F3;
  padding: 6px 12px;
  border-radius: 16px;
  font-size: 14px;
}

.remove-tag {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 16px;
  color: #666;
  padding: 0;
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background-color 0.2s;
}

.remove-tag:hover {
  background-color: #bbdefb;
}

.add-room-btn {
  background-color: #2196F3;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 16px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.add-room-btn:hover {
  background-color: #1976D2;
}

.linkage-config {
  margin-left: 26px;
  margin-top: 12px;
}

.preview-content {
  background-color: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
}

.preview-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #333;
}

.preview-item {
  margin-bottom: 12px;
  font-size: 16px;
}

.preview-item strong {
  color: #333;
}

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
  z-index: 1000;
}

.modal-content {
  background-color: #fff;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  width: 90%;
  max-width: 500px;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 20px;
  text-align: center;
}

.room-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}

.cancel-btn,
.confirm-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.cancel-btn {
  background-color: #f0f0f0;
  color: #333;
}

.cancel-btn:hover {
  background-color: #e0e0e0;
}

.confirm-btn {
  background-color: #4CAF50;
  color: white;
}

.confirm-btn:hover {
  background-color: #45a049;
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

.strategy-customization-container {
  padding-bottom: 80px; /* 为底部导航栏留出空间 */
}
</style>