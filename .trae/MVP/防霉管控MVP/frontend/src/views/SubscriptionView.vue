<template>
  <div class="subscription-container">
    <!-- 顶部导航栏 -->
    <header class="header">
      <button class="back-btn" @click="navigateBack">🔙 返回</button>
      <h1 class="title">💎 云订阅与权益中心</h1>
    </header>

    <!-- 当前订阅状态 -->
    <section class="status-section">
      <h2 class="section-title">💳 当前订阅状态</h2>
      <div class="status-content">
        <div class="plan-item">
          <span class="plan-label">📦 套餐:</span>
          <span class="plan-value">{{ subscriptionStatus.plan }}</span>
        </div>
        <div class="expiry-item">
          <span class="expiry-label">📅 到期:</span>
          <span class="expiry-value">{{ subscriptionStatus.expiryDate }}</span>
        </div>
        <div class="status-item">
          <span class="status-label">⏳ 状态:</span>
          <span 
            class="status-tag" 
            :class="subscriptionStatus.isExpired ? 'expired' : 'trial'"
          >
            {{ subscriptionStatus.isExpired ? '❌ 已过期' : '🕒 试用中' }}
          </span>
          <span class="status-text">
            {{ subscriptionStatus.daysRemaining > 0 ? `剩余 ${subscriptionStatus.daysRemaining} 天` : '已过期' }}
          </span>
        </div>
        <button class="subscribe-btn" @click="handleSubscribe">🚀 立即订阅</button>
      </div>
    </section>

    <!-- 选择订阅周期 -->
    <section class="plans-section">
      <h2 class="section-title">🛒 选择订阅周期</h2>
      <div class="plans-list">
        <!-- 1年卡 -->
        <div 
          class="plan-card" 
          :class="{ active: selectedPlan === '1year' }" 
          @click="selectPlan('1year')"
        >
          <div class="plan-header">
            <span class="plan-name">📄 1年卡</span>
            <span class="plan-price">¥240</span>
          </div>
          <div class="plan-details">
            <span class="plan-desc">• 折合 ¥20/月</span>
          </div>
          <div class="plan-select">
            <span class="select-radio" :class="{ checked: selectedPlan === '1year' }">
              {{ selectedPlan === '1year' ? '✅' : '◯' }}
            </span>
            <span class="select-text">选择</span>
          </div>
        </div>

        <!-- 2年卡 -->
        <div 
          class="plan-card" 
          :class="{ active: selectedPlan === '2year' }" 
          @click="selectPlan('2year')"
        >
          <div class="plan-header">
            <span class="plan-name">📄 2年卡</span>
            <span class="plan-price">¥440</span>
          </div>
          <div class="plan-details">
            <span class="plan-desc">• 折合 ¥18.3/月 (立省 ¥40)</span>
          </div>
          <div class="plan-select">
            <span class="select-radio" :class="{ checked: selectedPlan === '2year' }">
              {{ selectedPlan === '2year' ? '✅' : '◯' }}
            </span>
            <span class="select-text">选择</span>
          </div>
        </div>

        <!-- 3年卡 -->
        <div 
          class="plan-card recommended" 
          :class="{ active: selectedPlan === '3year' }" 
          @click="selectPlan('3year')"
        >
          <div class="recommended-badge">👑 推荐选择</div>
          <div class="plan-header">
            <span class="plan-name">📄 3年卡</span>
            <span class="plan-price">¥600</span>
          </div>
          <div class="plan-details">
            <span class="plan-desc">• 折合 ¥16.7/月 (立省 ¥120)</span>
          </div>
          <div class="plan-select">
            <span class="select-radio" :class="{ checked: selectedPlan === '3year' }">
              {{ selectedPlan === '3year' ? '✅' : '◯' }}
            </span>
            <span class="select-text">选择</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 订阅权益 -->
    <section class="benefits-section">
      <h2 class="section-title">🎁 订阅权益 (所有套餐包含)</h2>
      <div class="benefits-list">
        <div class="benefit-item">
          <span class="benefit-icon">✅</span>
          <span class="benefit-text">实时监测与风险预警</span>
        </div>
        <div class="benefit-item">
          <span class="benefit-icon">✅</span>
          <span class="benefit-text">智能联动 (自动排风/加热)</span>
        </div>
        <div class="benefit-item">
          <span class="benefit-icon">✅</span>
          <span class="benefit-text">每日防霉报告 & 积分奖励</span>
        </div>
      </div>
    </section>

    <!-- 防霉积分 -->
    <section class="points-section">
      <h2 class="section-title">🪙 防霉积分</h2>
      <div class="points-content">
        <div class="points-item">
          <span class="points-label">💰 当前积分:</span>
          <span class="points-value">180 分</span>
        </div>
        <div class="points-item">
          <span class="points-label">🎁 可兑换:</span>
          <span class="points-redeem">¥18 订阅抵扣 / 清洁券 / 设备配件</span>
        </div>
        <button class="redeem-btn" @click="handleRedeem">🎁 积分兑换 ></button>
      </div>
    </section>

    <!-- 账单与发票 -->
    <section class="billing-section">
      <h2 class="section-title">🧾 账单与发票</h2>
      <div class="billing-list">
        <div class="bill-item">
          <div class="bill-info">
            <span class="bill-date">2025-12-01</span>
            <span class="bill-desc">年度订阅</span>
          </div>
          <div class="bill-amount">¥240</div>
          <button class="detail-btn">[详情]</button>
        </div>
        <div class="bill-item">
          <div class="bill-info">
            <span class="bill-date">2025-12-15</span>
            <span class="bill-desc">积分抵扣</span>
          </div>
          <div class="bill-amount credit">-¥10</div>
          <button class="detail-btn">[详情]</button>
        </div>
      </div>
    </section>

    <!-- 积分兑换模态框 -->
    <div v-if="showRedeemModal" class="modal-overlay" @click="closeRedeemModal">
      <div class="modal-content" @click.stop>
        <h3 class="section-title">🎁 积分兑换</h3>
        <div class="redeem-options">
          <div class="redeem-item" @click="redeem('discount')">
            <span class="icon">💰</span>
            <div class="info">
              <span class="name">¥10 订阅抵扣券</span>
              <span class="cost">100 积分</span>
            </div>
            <button class="action-btn">兑换</button>
          </div>
          <div class="redeem-item" @click="redeem('cleaning')">
            <span class="icon">🧹</span>
            <div class="info">
              <span class="name">免费清洁服务一次</span>
              <span class="cost">500 积分</span>
            </div>
            <button class="action-btn" disabled>积分不足</button>
          </div>
        </div>
        <button class="close-modal-btn" @click="closeRedeemModal">取消</button>
      </div>
    </div>

    <!-- 底部导航栏 -->
    <FooterNavigation active="subscription" />
  </div>
</template>

<script>
import { defineComponent, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import FooterNavigation from '../components/FooterNavigation.vue'

export default defineComponent({
  name: 'SubscriptionView',
  components: {
    FooterNavigation
  },
  setup() {
    const router = useRouter()
    
    // 模拟数据
    const selectedPlan = ref('3year')
    
    // 模拟订阅状态
    const subscriptionStatus = ref({
      plan: '全功能防霉版',
      expiryDate: '2026-01-30',
      isTrial: true,
      daysRemaining: 7,
      isExpired: false,
      hasFullAccess: true
    })
    
    // 检查订阅状态，模拟权限降级
    const checkSubscriptionStatus = () => {
      if (subscriptionStatus.value.daysRemaining <= 0) {
        subscriptionStatus.value.isExpired = true
        subscriptionStatus.value.hasFullAccess = false
      }
    }
    
    onMounted(() => {
      checkSubscriptionStatus()
    })
    
    // 模拟立即订阅操作
    const handleSubscribe = () => {
      // 这里可以添加订阅逻辑
      alert('订阅功能已触发，实际项目中会调用后端API')
    }

    const navigateBack = () => {
      router.go(-1)
    }

    const selectPlan = (plan) => {
      selectedPlan.value = plan
    }

    // 积分兑换相关
    const showRedeemModal = ref(false)
    
    const handleRedeem = () => {
      showRedeemModal.value = true
    }
    
    const closeRedeemModal = () => {
      showRedeemModal.value = false
    }
    
    const redeem = (type) => {
      alert('兑换功能暂未开放')
      closeRedeemModal()
    }

    return {
      selectedPlan,
      navigateBack,
      selectPlan,
      handleSubscribe,
      subscriptionStatus,
      showRedeemModal,
      handleRedeem,
      closeRedeemModal,
      redeem
    }
  }
})
</script>

<style scoped>
/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  width: 90%;
  max-width: 320px;
}

.redeem-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin: 20px 0;
}

.redeem-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border: 1px solid #eee;
  border-radius: 8px;
  cursor: pointer;
}

.redeem-item:hover {
  background-color: #f9f9f9;
}

.redeem-item.disabled {
  opacity: 0.6;
  cursor: not-allowed;
  background-color: #f5f5f5;
}

.redeem-item .icon {
  font-size: 24px;
  margin-right: 12px;
}

.redeem-item .info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.redeem-item .name {
  font-weight: 500;
  font-size: 14px;
}

.redeem-item .cost {
  font-size: 12px;
  color: #666;
}

.action-btn {
  background-color: #FF9800;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
}

.action-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.close-modal-btn {
  width: 100%;
  padding: 10px;
  background-color: #f5f5f5;
  border: none;
  border-radius: 6px;
  color: #666;
  font-weight: 500;
  cursor: pointer;
}

.subscription-container {
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

.status-section, .plans-section, .benefits-section, .points-section, .billing-section {
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

.plan-item, .expiry-item, .points-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.plan-label, .expiry-label, .points-label {
  font-weight: 500;
}

.plan-value, .expiry-value, .points-value {
  font-weight: 600;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-label {
  font-weight: 500;
  margin-right: 8px;
}

.status-tag {
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 14px;
  font-weight: 500;
}

.status-tag.trial {
  background-color: #FFF3E0;
  color: #FF9800;
}

.status-tag.active {
  background-color: #E8F5E9;
  color: #4CAF50;
}

.status-tag.expired {
  background-color: #FFEBEE;
  color: #F44336;
}

.status-text {
  font-size: 14px;
  color: #666;
}

.subscribe-btn {
  background-color: #FF9800;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s;
  align-self: flex-end;
  margin-top: 8px;
}

.subscribe-btn:hover {
  background-color: #F57C00;
}

.plans-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.plan-card {
  background-color: #f9f9f9;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.plan-card:hover {
  border-color: #FF9800;
  box-shadow: 0 4px 8px rgba(255, 152, 0, 0.1);
}

.plan-card.active {
  border-color: #FF9800;
  background-color: #FFF3E0;
}

.plan-card.recommended {
  border-color: #FFC107;
  background-color: #FFF8E1;
}

.recommended-badge {
  position: absolute;
  top: -8px;
  right: 16px;
  background-color: #FFC107;
  color: #333;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.plan-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.plan-name {
  font-weight: 600;
  font-size: 15px;
}

.plan-price {
  font-weight: 700;
  font-size: 18px;
  color: #FF9800;
}

.plan-details {
  margin-bottom: 12px;
}

.plan-desc {
  font-size: 14px;
  color: #666;
}

.plan-select {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
}

.select-radio {
  font-size: 16px;
  font-weight: bold;
}

.select-radio.checked {
  color: #FF9800;
}

.select-text {
  font-size: 14px;
  font-weight: 500;
  color: #666;
}

.benefits-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.benefit-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.benefit-icon {
  color: #4CAF50;
  font-weight: bold;
}

.benefit-text {
  font-weight: 500;
}

.points-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.points-redeem {
  font-size: 14px;
  color: #666;
}

.redeem-btn {
  background: none;
  border: none;
  color: #2196F3;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  padding: 0;
  text-align: left;
  transition: color 0.2s;
  align-self: flex-end;
  margin-top: 8px;
}

.redeem-btn:hover {
  color: #1976D2;
}

.billing-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.bill-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background-color: #f9f9f9;
  border-radius: 6px;
}

.bill-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.bill-date {
  font-size: 14px;
  color: #666;
}

.bill-desc {
  font-weight: 500;
}

.bill-amount {
  font-weight: 600;
  color: #333;
}

.bill-amount.credit {
  color: #4CAF50;
}

.detail-btn {
  background: none;
  border: 1px solid #ddd;
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.detail-btn:hover {
  background-color: #f0f0f0;
  border-color: #ccc;
}
</style>