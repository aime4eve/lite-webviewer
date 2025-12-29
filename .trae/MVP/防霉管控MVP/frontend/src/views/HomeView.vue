<template>
  <div class="home-container">
<<<<<<< HEAD
    <!-- 试用到期警告横幅 -->
=======
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
    <div 
      v-if="subscriptionStatus.isTrial && subscriptionStatus.daysRemaining <= 7" 
      class="trial-warning"
    >
      <span class="warning-icon">⚠️</span>
      <div class="warning-content">
        <span class="warning-title">
          {{ subscriptionStatus.daysRemaining > 0 ? '您的试用期即将到期' : '您的试用期已结束' }}
        </span>
        <span class="warning-text">
          {{ 
            subscriptionStatus.daysRemaining > 0 
              ? `剩余 ${subscriptionStatus.daysRemaining} 天，点击立即订阅以继续享受全功能服务` 
              : '已限制部分功能，点击立即订阅以恢复全功能服务' 
          }}
        </span>
      </div>
      <button class="upgrade-btn" @click="navigateToSubscription">立即订阅</button>
    </div>
    
<<<<<<< HEAD
    <!-- 顶部标题栏 -->
=======
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
    <header class="header">
      <h1 class="title">🛡️ 防霉守护 · 家庭版</h1>
      <button class="add-device-btn" @click="navigateToAddDevice">
        ➕ 添加设备
      </button>
    </header>

<<<<<<< HEAD
    <!-- 浴室防霉状态 -->
    <section class="status-section">
      <h2 class="section-title">🛁 浴室防霉状态 (Today)</h2>
      <div class="status-content">
        <div class="risk-item">
          <span class="risk-label">🚨 风险指数:</span>
          <span class="risk-value safe">🟢 18% (安全)</span>
        </div>
        <div class="env-data">
          <span class="env-item">🌡️ 温度: 24°C</span>
          <span class="env-item">💧 湿度: 62%</span>
        </div>
        <div class="status-tags">
          <span class="tag safe">✅ 安全</span>
          <span class="tag auto">🤖 自动防霉已开启</span>
        </div>
      </div>
    </section>

    <!-- 设备概览 -->
    <section class="devices-section">
      <h2 class="section-title">📱 设备概览</h2>
      <div class="devices-list">
        <div 
          class="device-item" 
          :class="{ 'limited-feature': !subscriptionStatus.hasFullAccess }"
          @click="subscriptionStatus.hasFullAccess ? navigateToDeviceDetail(1) : null"
        >
          <div class="device-info">
            <span class="device-name">🏠 主卧浴室</span>
            <span class="device-status">🟢 在线 · 正常 · {{ subscriptionStatus.hasFullAccess ? '🔗 自动联动已配置' : '<span class=\"permission-tag\">🔒 功能受限</span>' }}</span>
          </div>
          <span class="detail-link">详情 ></span>
          <div v-if="!subscriptionStatus.hasFullAccess" class="limited-overlay">订阅后解锁全部功能</div>
        </div>
        <div 
          class="device-item" 
          :class="{ 'limited-feature': !subscriptionStatus.hasFullAccess }"
          @click="subscriptionStatus.hasFullAccess ? navigateToDeviceDetail(2) : null"
        >
          <div class="device-info">
            <span class="device-name">🏠 次卧浴室</span>
            <span class="device-status">🟢 在线 · 正常 · {{ subscriptionStatus.hasFullAccess ? '⚠️ 仅预警模式' : '<span class=\"permission-tag\">🔒 功能受限</span>' }}</span>
=======
    <section class="status-section">
      <h2 class="section-title">🛁 浴室防霉状态 (Today)</h2>
      <div v-if="loadingStatus" class="loading">加载中...</div>
      <div v-else-if="currentDevice" class="status-content">
        <div class="risk-item">
          <span class="risk-label">🚨 风险指数:</span>
          <span class="risk-value" :class="getRiskClass(currentRiskPrediction?.riskLevel)">
            {{ getRiskIcon(currentRiskPrediction?.riskLevel) }} {{ currentRiskPrediction?.riskScore || 0 }}% ({{ getRiskText(currentRiskPrediction?.riskLevel) }})
          </span>
        </div>
        <div class="env-data">
          <span class="env-item">🌡️ 温度: {{ currentEnvironment?.temperature || '--' }}°C</span>
          <span class="env-item">💧 湿度: {{ currentEnvironment?.humidity || '--' }}%</span>
        </div>
        <div class="status-tags">
          <span class="tag" :class="getRiskClass(currentRiskPrediction?.riskLevel)">
            {{ getRiskStatusIcon(currentRiskPrediction?.riskLevel) }} {{ getRiskStatusText(currentRiskPrediction?.riskLevel) }}
          </span>
          <span class="tag auto">🤖 自动防霉已开启</span>
        </div>
      </div>
      <div v-else class="no-data">暂无设备数据</div>
    </section>

    <section class="devices-section">
      <h2 class="section-title">📱 设备概览</h2>
      <div v-if="loadingDevices" class="loading">加载中...</div>
      <div v-else-if="devices.length > 0" class="devices-list">
        <div 
          v-for="device in devices" 
          :key="device.id"
          class="device-item" 
          :class="{ 'limited-feature': !subscriptionStatus.hasFullAccess }"
          @click="subscriptionStatus.hasFullAccess ? navigateToDeviceDetail(device.id) : null"
        >
          <div class="device-info">
            <span class="device-name">🏠 {{ device.name || '未命名设备' }}</span>
            <span class="device-status">
              {{ device.status === 'online' ? '🟢 在线' : '🔴 离线' }} · 
              {{ device.status === 'online' ? '正常' : '异常' }} · 
              <span v-if="subscriptionStatus.hasFullAccess">🔗 自动联动已配置</span>
              <span v-else class="permission-tag">🔒 功能受限</span>
            </span>
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
          </div>
          <span class="detail-link">详情 ></span>
          <div v-if="!subscriptionStatus.hasFullAccess" class="limited-overlay">订阅后解锁全部功能</div>
        </div>
      </div>
<<<<<<< HEAD
    </section>

    <!-- 本月防霉战报 -->
    <section class="report-section">
      <h2 class="section-title">📊 本月防霉战报</h2>
      <div class="report-content">
        <div class="report-item">
          <span class="report-label">🛡️ 阻断霉变:</span>
          <span class="report-value">12 次</span>
        </div>
        <div class="report-item">
          <span class="report-label">💰 节省电费:</span>
          <span class="report-value">¥4.8</span>
        </div>
        <div class="report-item">
          <span class="report-label">🪙 防霉积分:</span>
          <span class="report-value">180 分</span>
=======
      <div v-else class="no-data">暂无设备，请添加设备</div>
    </section>

    <section class="report-section">
      <h2 class="section-title">📊 本月防霉战报</h2>
      <div v-if="loadingReport" class="loading">加载中...</div>
      <div v-else class="report-content">
        <div class="report-item">
          <span class="report-label">🛡️ 阻断霉变:</span>
          <span class="report-value">{{ monthlyReport?.preventedCount || 0 }} 次</span>
        </div>
        <div class="report-item">
          <span class="report-label">💰 节省电费:</span>
          <span class="report-value">¥{{ monthlyReport?.savedCost || 0 }}</span>
        </div>
        <div class="report-item">
          <span class="report-label">🪙 防霉积分:</span>
          <span class="report-value">{{ monthlyReport?.points || 0 }} 分</span>
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
          <span class="exchange-link" @click="navigateToSubscription">🎁 兑换 ></span>
        </div>
      </div>
    </section>
    
<<<<<<< HEAD
    <!-- 预测反馈 -->
=======
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
    <section class="feedback-section">
      <h2 class="section-title">📝 预测反馈</h2>
      <div class="feedback-content">
        <div v-if="feedbackSubmitted" class="feedback-success">
          ✅ 反馈提交成功！感谢您的宝贵意见，我们将持续优化预测模型。
        </div>
        <template v-else>
          <div class="feedback-question">
            <span class="question-text">您对当前的霉菌风险预测结果满意吗？</span>
          </div>
          <div class="feedback-rating">
            <button 
              v-for="star in 5" 
              :key="star" 
              class="rating-star" 
              :class="{ 'active': feedbackRating >= star }"
              @click="setFeedbackRating(star)"
            >
              ⭐
            </button>
          </div>
          <div class="feedback-comment">
            <textarea 
              v-model="feedbackComment" 
              placeholder="请输入您的反馈意见（可选）" 
              class="comment-input"
            ></textarea>
          </div>
          <div class="feedback-actions">
<<<<<<< HEAD
            <button class="submit-btn" @click="submitFeedback">提交反馈</button>
=======
            <button class="submit-btn" @click="submitFeedback" :disabled="submittingFeedback">
              {{ submittingFeedback ? '提交中...' : '提交反馈' }}
            </button>
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
            <button class="cancel-btn" @click="resetFeedback">取消</button>
          </div>
        </template>
      </div>
    </section>

<<<<<<< HEAD
    <!-- 底部导航栏 -->
=======
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
    <FooterNavigation active="home" />
  </div>
</template>

<script>
<<<<<<< HEAD
import { defineComponent, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import FooterNavigation from '../components/FooterNavigation.vue'
=======
import { defineComponent, ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import FooterNavigation from '../components/FooterNavigation.vue'
import { deviceApi } from '../api/device'
import { environmentApi } from '../api/environment'
import { alarmApi } from '../api/alarm'
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5

export default defineComponent({
  name: 'HomeView',
  components: {
    FooterNavigation
  },
  setup() {
    const router = useRouter()
    
<<<<<<< HEAD
    // 模拟订阅状态
=======
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
    const subscriptionStatus = ref({
      isTrial: true,
      daysRemaining: 7,
      isExpired: false,
      hasFullAccess: true
    })
    
<<<<<<< HEAD
    // 预测反馈状态
    const feedbackRating = ref(0)
    const feedbackComment = ref('')
    const feedbackSubmitted = ref(false)
    
    // 检查订阅状态，模拟权限降级
    const checkSubscriptionStatus = () => {
      // 模拟试用期即将结束的情况
      if (subscriptionStatus.value.daysRemaining <= 7 && subscriptionStatus.value.daysRemaining > 0) {
        // 显示警告
      } else if (subscriptionStatus.value.daysRemaining <= 0) {
        // 试用期已过，权限降级
=======
    const devices = ref([])
    const loadingDevices = ref(false)
    
    const currentEnvironment = ref(null)
    const currentRiskPrediction = ref(null)
    const loadingStatus = ref(false)
    
    const monthlyReport = ref({
      preventedCount: 0,
      savedCost: 0,
      points: 0
    })
    const loadingReport = ref(false)
    
    const feedbackRating = ref(0)
    const feedbackComment = ref('')
    const feedbackSubmitted = ref(false)
    const submittingFeedback = ref(false)
    
    const currentDevice = computed(() => {
      return devices.value.length > 0 ? devices.value[0] : null
    })
    
    const checkSubscriptionStatus = () => {
      if (subscriptionStatus.value.daysRemaining <= 7 && subscriptionStatus.value.daysRemaining > 0) {
      } else if (subscriptionStatus.value.daysRemaining <= 0) {
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
        subscriptionStatus.value.isExpired = true
        subscriptionStatus.value.hasFullAccess = false
      }
    }
    
<<<<<<< HEAD
    onMounted(() => {
      checkSubscriptionStatus()
    })
    
    // 预测反馈相关函数
=======
    const loadDevices = async () => {
      try {
        loadingDevices.value = true
        const response = await deviceApi.getDeviceList({
          page: 1,
          size: 100
        })
        if (response && response.data) {
          devices.value = response.data
          if (devices.value.length > 0) {
            await loadDeviceStatus(devices.value[0].id)
          }
        }
      } catch (error) {
        console.error('加载设备列表失败:', error)
        devices.value = []
      } finally {
        loadingDevices.value = false
      }
    }
    
    const loadDeviceStatus = async (deviceId) => {
      try {
        loadingStatus.value = true
        const [envResponse, riskResponse] = await Promise.all([
          environmentApi.getLatestEnvironmentData(deviceId),
          deviceApi.getRiskPrediction(deviceId, 24)
        ])
        
        if (envResponse && envResponse.data) {
          currentEnvironment.value = envResponse.data
        }
        
        if (riskResponse && riskResponse.data) {
          currentRiskPrediction.value = riskResponse.data
        }
      } catch (error) {
        console.error('加载设备状态失败:', error)
      } finally {
        loadingStatus.value = false
      }
    }
    
    const loadMonthlyReport = async () => {
      try {
        loadingReport.value = true
        const now = new Date()
        const startDate = new Date(now.getFullYear(), now.getMonth(), 1)
        const endDate = new Date(now.getFullYear(), now.getMonth() + 1, 0)
        
        const response = await alarmApi.getAlarmStatistics({
          startDate: startDate.toISOString().split('T')[0],
          endDate: endDate.toISOString().split('T')[0]
        })
        
        if (response && response.data) {
          monthlyReport.value = {
            preventedCount: response.data.preventedCount || 0,
            savedCost: response.data.savedCost || 0,
            points: response.data.points || 0
          }
        }
      } catch (error) {
        console.error('加载月度报告失败:', error)
        monthlyReport.value = {
          preventedCount: 0,
          savedCost: 0,
          points: 0
        }
      } finally {
        loadingReport.value = false
      }
    }
    
    const getRiskClass = (level) => {
      switch (level) {
        case 'low':
          return 'safe'
        case 'medium':
          return 'medium'
        case 'high':
          return 'high'
        default:
          return 'safe'
      }
    }
    
    const getRiskIcon = (level) => {
      switch (level) {
        case 'low':
          return '🟢'
        case 'medium':
          return '🟡'
        case 'high':
          return '🔴'
        default:
          return '🟢'
      }
    }
    
    const getRiskText = (level) => {
      switch (level) {
        case 'low':
          return '安全'
        case 'medium':
          return '中等'
        case 'high':
          return '危险'
        default:
          return '安全'
      }
    }
    
    const getRiskStatusIcon = (level) => {
      switch (level) {
        case 'low':
          return '✅'
        case 'medium':
          return '⚠️'
        case 'high':
          return '🚨'
        default:
          return '✅'
      }
    }
    
    const getRiskStatusText = (level) => {
      switch (level) {
        case 'low':
          return '安全'
        case 'medium':
          return '注意'
        case 'high':
          return '危险'
        default:
          return '安全'
      }
    }
    
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
    const setFeedbackRating = (rating) => {
      feedbackRating.value = rating
    }
    
<<<<<<< HEAD
    const submitFeedback = () => {
      // 模拟提交反馈
      console.log('提交反馈:', {
        rating: feedbackRating.value,
        comment: feedbackComment.value,
        timestamp: new Date().toISOString()
      })
      
      // 显示成功提示
      feedbackSubmitted.value = true
      
      // 重置表单
      setTimeout(() => {
        resetFeedback()
        feedbackSubmitted.value = false
      }, 2000)
=======
    const submitFeedback = async () => {
      try {
        submittingFeedback.value = true
        if (currentDevice.value) {
          await deviceApi.submitFeedback(currentDevice.value.id, {
            rating: feedbackRating.value,
            comment: feedbackComment.value,
            timestamp: new Date().toISOString()
          })
        }
        feedbackSubmitted.value = true
        setTimeout(() => {
          resetFeedback()
          feedbackSubmitted.value = false
        }, 2000)
      } catch (error) {
        console.error('提交反馈失败:', error)
        alert('提交反馈失败，请稍后重试')
      } finally {
        submittingFeedback.value = false
      }
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
    }
    
    const resetFeedback = () => {
      feedbackRating.value = 0
      feedbackComment.value = ''
    }

    const navigateToAddDevice = () => {
      router.push('/c/add-device')
    }

    const navigateToDeviceDetail = (id) => {
      router.push(`/c/device-detail/${id}`)
    }

    const navigateToSubscription = () => {
      router.push('/c/subscription')
    }
<<<<<<< HEAD
=======
    
    onMounted(async () => {
      checkSubscriptionStatus()
      await Promise.all([
        loadDevices(),
        loadMonthlyReport()
      ])
    })
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5

    return {
      navigateToAddDevice,
      navigateToDeviceDetail,
      navigateToSubscription,
      subscriptionStatus,
<<<<<<< HEAD
      feedbackRating,
      feedbackComment,
      feedbackSubmitted,
      setFeedbackRating,
      submitFeedback,
      resetFeedback
=======
      devices,
      loadingDevices,
      currentDevice,
      currentEnvironment,
      currentRiskPrediction,
      loadingStatus,
      monthlyReport,
      loadingReport,
      feedbackRating,
      feedbackComment,
      feedbackSubmitted,
      submittingFeedback,
      setFeedbackRating,
      submitFeedback,
      resetFeedback,
      getRiskClass,
      getRiskIcon,
      getRiskText,
      getRiskStatusIcon,
      getRiskStatusText
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
    }
  }
})
</script>

<style scoped>
.home-container {
  max-width: 480px;
  margin: 0 auto;
  background-color: #f5f5f5;
  min-height: 100vh;
<<<<<<< HEAD
  padding-bottom: 60px; /* 为底部导航栏留出空间 */
}

/* 试用到期警告样式 */
=======
  padding-bottom: 60px;
}

>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
.trial-warning {
  background-color: #FFF3E0;
  border: 1px solid #FFE0B2;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  position: sticky;
  top: 0;
  z-index: 200;
  box-shadow: 0 2px 4px rgba(255, 152, 0, 0.1);
}

.warning-icon {
  font-size: 20px;
  flex-shrink: 0;
}

.warning-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.warning-title {
  font-weight: 600;
  color: #F57C00;
  font-size: 14px;
}

.warning-text {
  font-size: 13px;
  color: #FFA000;
}

.upgrade-btn {
  background-color: #FF9800;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s;
  flex-shrink: 0;
}

.upgrade-btn:hover {
  background-color: #F57C00;
}

<<<<<<< HEAD
/* 权限降级样式 */
=======
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
.limited-feature {
  opacity: 0.7;
  position: relative;
  cursor: not-allowed;
}

.limited-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  font-size: 12px;
  color: #F57C00;
  font-weight: 500;
  z-index: 10;
}

.permission-tag {
  background-color: #FFEBEE;
  color: #F44336;
}

<<<<<<< HEAD

=======
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
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

.add-device-btn {
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.add-device-btn:hover {
  background-color: #45a049;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #333;
}

<<<<<<< HEAD
.status-section, .devices-section, .report-section {
=======
.status-section, .devices-section, .report-section, .feedback-section {
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
  background-color: #fff;
  padding: 16px;
  margin: 12px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

<<<<<<< HEAD
=======
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

>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
.status-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.risk-item {
  display: flex;
  align-items: center;
}

.risk-label {
  font-weight: 500;
  margin-right: 8px;
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

.env-data {
  display: flex;
  gap: 20px;
}

.env-item {
  font-size: 16px;
  font-weight: 500;
}

.status-tags {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.tag {
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 14px;
  font-weight: 500;
}

.tag.safe {
  background-color: #E8F5E9;
  color: #4CAF50;
}

<<<<<<< HEAD
=======
.tag.medium {
  background-color: #FFF8E1;
  color: #FFC107;
}

.tag.high {
  background-color: #FFEBEE;
  color: #F44336;
}

>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
.tag.auto {
  background-color: #E3F2FD;
  color: #2196F3;
}

.devices-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.device-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background-color: #f9f9f9;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.device-item:hover {
  background-color: #f0f0f0;
}

.device-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.device-name {
  font-weight: 500;
  font-size: 15px;
}

.device-status {
  font-size: 13px;
  color: #666;
}

.detail-link {
  color: #2196F3;
  font-weight: 500;
  font-size: 14px;
}

.report-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.report-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.report-item:last-child {
  border-bottom: none;
}

.report-label {
  font-weight: 500;
}

.report-value {
  font-weight: 600;
  font-size: 16px;
}

.exchange-link {
  color: #FF9800;
  font-size: 14px;
  margin-left: 8px;
  cursor: pointer;
}

.exchange-link:hover {
<<<<<<< HEAD
      text-decoration: underline;
    }
    
    /* 预测反馈样式 */
    .feedback-section {
      background-color: #fff;
      padding: 16px;
      margin: 12px;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    }
    
    .feedback-content {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }
    
    .feedback-question {
      font-size: 15px;
      font-weight: 500;
      color: #333;
    }
    
    .feedback-rating {
      display: flex;
      gap: 8px;
      align-items: center;
    }
    
    .rating-star {
      background: none;
      border: none;
      font-size: 24px;
      cursor: pointer;
      transition: transform 0.2s;
      padding: 0;
    }
    
    .rating-star:hover {
      transform: scale(1.2);
    }
    
    .rating-star.active {
      color: #FFC107;
    }
    
    .feedback-comment {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }
    
    .comment-input {
      padding: 12px;
      border: 1px solid #e0e0e0;
      border-radius: 6px;
      font-size: 14px;
      resize: vertical;
      min-height: 80px;
      font-family: inherit;
    }
    
    .comment-input:focus {
      outline: none;
      border-color: #2196F3;
      box-shadow: 0 0 0 2px rgba(33, 150, 243, 0.1);
    }
    
    .feedback-actions {
      display: flex;
      gap: 12px;
      justify-content: flex-end;
    }
    
    .submit-btn, .cancel-btn {
      padding: 8px 16px;
      border: none;
      border-radius: 6px;
      font-size: 14px;
      font-weight: 500;
      cursor: pointer;
      transition: background-color 0.3s;
    }
    
    .submit-btn {
      background-color: #2196F3;
      color: white;
    }
    
    .submit-btn:hover {
      background-color: #1976D2;
    }
    
    .cancel-btn {
      background-color: #f5f5f5;
      color: #666;
      border: 1px solid #e0e0e0;
    }
    
    .cancel-btn:hover {
      background-color: #e0e0e0;
    }
    
    .feedback-success {
      background-color: #E8F5E9;
      color: #4CAF50;
      padding: 12px;
      border-radius: 6px;
      text-align: center;
      font-size: 14px;
      font-weight: 500;
    }
</style>
=======
  text-decoration: underline;
}

.feedback-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feedback-question {
  font-size: 15px;
  font-weight: 500;
  color: #333;
}

.feedback-rating {
  display: flex;
  gap: 8px;
  align-items: center;
}

.rating-star {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  transition: transform 0.2s;
  padding: 0;
}

.rating-star:hover {
  transform: scale(1.2);
}

.rating-star.active {
  color: #FFC107;
}

.feedback-comment {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.comment-input {
  padding: 12px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-size: 14px;
  resize: vertical;
  min-height: 80px;
  font-family: inherit;
}

.comment-input:focus {
  outline: none;
  border-color: #2196F3;
  box-shadow: 0 0 0 2px rgba(33, 150, 243, 0.1);
}

.feedback-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.submit-btn, .cancel-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s;
}

.submit-btn {
  background-color: #2196F3;
  color: white;
}

.submit-btn:hover:not(:disabled) {
  background-color: #1976D2;
}

.submit-btn:disabled {
  background-color: #B0BEC5;
  cursor: not-allowed;
}

.cancel-btn {
  background-color: #f5f5f5;
  color: #666;
  border: 1px solid #e0e0e0;
}

.cancel-btn:hover {
  background-color: #e0e0e0;
}

.feedback-success {
  background-color: #E8F5E9;
  color: #4CAF50;
  padding: 12px;
  border-radius: 6px;
  text-align: center;
  font-size: 14px;
  font-weight: 500;
}
</style>
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
