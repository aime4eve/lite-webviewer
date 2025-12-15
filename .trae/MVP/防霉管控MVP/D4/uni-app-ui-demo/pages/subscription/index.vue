<template>
	<view class="container">
		<!-- 顶部导航栏 -->
		<view class="nav-bar">
			<view class="nav-back" @click="goBack">
				<text class="back-icon">🔙</text>
			</view>
			<view class="nav-title">
				<text class="title-icon">💎</text>
				<text class="title-text">云订阅与权益中心</text>
			</view>
		</view>

		<!-- 当前订阅状态卡片 -->
		<view class="card subscription-status">
			<view class="section-title">💳 当前订阅状态</view>
			
			<view class="subscription-info">
				<view class="info-row">
					<text class="info-label">📦 套餐:</text>
					<text class="info-value">{{ subscription.planName }}</text>
				</view>
				
				<view class="info-row">
					<text class="info-label">📅 到期:</text>
					<text class="info-value">{{ subscription.expiryDate }}</text>
				</view>
				
				<view class="info-row status-row">
					<text class="info-label">⏳ 状态:</text>
					<text :class="['status-badge', subscription.statusClass]">
						[{{ subscription.statusText }}] 剩余 {{ subscription.daysLeft }} 天
					</text>
				</view>
			</view>
			
			<button v-if="subscription.isTrial && subscription.daysLeft <= 3" class="btn-primary upgrade-btn" @click="upgradeSubscription">
				🚀 立即订阅
			</button>
		</view>

		<!-- 订阅周期选择卡片 -->
		<view class="card">
			<view class="section-title">🛒 选择订阅周期</view>
			
			<view class="plan-item" v-for="(plan, index) in subscriptionPlans" :key="index">
				<view class="plan-header">
					<text class="plan-name">📄 {{ plan.name }}</text>
					<text class="plan-price">¥{{ plan.price }}</text>
				</view>
				
				<view class="plan-desc">
					{{ plan.description }}
				</view>
				
				<button :class="['select-btn', plan.recommended ? 'recommended' : 'normal']" @click="selectPlan(plan)">
					<text v-if="plan.recommended" class="recommended-badge">👑 推荐选择</text>
					<text class="select-text">{{ plan.buttonText }}</text>
				</button>
			</view>
		</view>

		<!-- 订阅权益卡片 -->
		<view class="card">
			<view class="section-title">🎁 订阅权益 (所有套餐包含)</view>
			
			<view class="benefit-item" v-for="(benefit, index) in subscriptionBenefits" :key="index">
				<text class="benefit-icon">• {{ benefit.icon }}</text>
				<text class="benefit-text">{{ benefit.text }}</text>
			</view>
		</view>

		<!-- 防霉积分卡片 -->
		<view class="card">
			<view class="section-title">🪙 防霉积分</view>
			
			<view class="points-info">
				<text class="points-label">💰 当前积分:</text>
				<text class="points-value">{{ points.currentPoints }} 分</text>
			</view>
			
			<view class="points-desc">
				可兑换: ¥{{ points.currentPoints/10 }} 订阅抵扣 / 清洁券 / 设备配件
			</view>
			
			<button class="btn-secondary" @click="goToPointsMall">
				🎁 积分兑换 >
			</button>
		</view>

		<!-- 账单与发票卡片 -->
		<view class="card">
			<view class="section-title">🧾 账单与发票</view>
			
			<view class="bill-item" v-for="(bill, index) in billingHistory" :key="index">
				<view class="bill-date">{{ bill.date }}</view>
				<view class="bill-desc">{{ bill.description }}</view>
				<view class="bill-amount">{{ bill.amount > 0 ? '+' : '' }}{{ bill.amount }}</view>
				<text class="arrow-right"></text>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			subscription: {
				planName: '全功能防霉版',
				expiryDate: '2026-01-30',
				statusText: '试用中',
				statusClass: 'status-badge-trial',
				daysLeft: 7,
				isTrial: true
			},
			subscriptionPlans: [
				{
					name: '1年卡',
					price: 240,
					description: '折合 ¥20/月',
					buttonText: '选择',
					recommended: false
				},
				{
					name: '2年卡',
					price: 440,
					description: '折合 ¥18.3/月 (立省 ¥40)',
					buttonText: '选择',
					recommended: false
				},
				{
					name: '3年卡',
					price: 600,
					description: '折合 ¥16.7/月 (立省 ¥120)',
					buttonText: '选择',
					recommended: true
				}
			],
			subscriptionBenefits: [
				{ icon: '✅', text: '实时监测与风险预警' },
				{ icon: '✅', text: '智能联动 (自动排风/加热)' },
				{ icon: '✅', text: '每日防霉报告 & 积分奖励' }
			],
			points: {
				currentPoints: 180
			},
			billingHistory: [
				{ date: '2025-12-01', description: '年度订阅', amount: 240 },
				{ date: '2025-12-15', description: '积分抵扣', amount: -10 }
			]
		}
	},
	onLoad() {
		// 页面加载时获取订阅信息
		this.loadSubscriptionData();
	},
	methods: {
		loadSubscriptionData() {
			// 模拟从API获取订阅数据
			console.log('加载订阅数据');
		},
		goBack() {
			uni.navigateBack();
		},
		upgradeSubscription() {
			uni.showModal({
				title: '升级订阅',
				content: '即将为您升级到正式订阅，确定要继续吗？',
				success: (res) => {
					if (res.confirm) {
						console.log('用户选择升级订阅');
						// 这里应该跳转到支付页面
					}
				}
			});
		},
		selectPlan(plan) {
			uni.showModal({
				title: '选择订阅方案',
				content: `您选择了${plan.name}，价格为¥${plan.price}，确定要购买吗？`,
				success: (res) => {
					if (res.confirm) {
						console.log(`用户选择${plan.name}方案`);
						// 这里应该跳转到支付页面
					}
				}
			});
		},
		goToPointsMall() {
			uni.showToast({
				title: '积分商城暂未开放',
				icon: 'none'
			});
		}
	}
}
</script>

<style>
.container {
	background-color: #f5f5f5;
	min-height: 100vh;
	padding: 20rpx;
	padding-top: 40rpx;
}

.nav-bar {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 30rpx;
	padding: 0 20rpx;
}

.nav-back {
	font-size: 36rpx;
}

.nav-title {
	display: flex;
	align-items: center;
	flex: 1;
	justify-content: center;
}

.title-icon {
	font-size: 36rpx;
	margin-right: 10rpx;
}

.title-text {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
}

.subscription-status {
	background: linear-gradient(135deg, #f5f7fa 0%, #e4edf5 100%);
}

.subscription-info {
	margin: 20rpx 0;
}

.info-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 15rpx 0;
	border-bottom: 1rpx solid rgba(255, 255, 255, 0.3);
}

.info-row:last-child {
	border-bottom: none;
}

.info-label {
	font-size: 30rpx;
	color: #333;
}

.info-value {
	font-size: 30rpx;
	color: #333;
	font-weight: bold;
}

.status-row {
	margin-top: 15rpx;
	padding-top: 15rpx;
}

.status-badge {
	font-size: 26rpx;
	padding: 8rpx 16rpx;
	border-radius: 20rpx;
	font-weight: bold;
}

.status-badge-trial {
	background-color: #fffbe6;
	color: #fa8c16;
}

.upgrade-btn {
	margin-top: 30rpx;
	width: 100%;
}

.plan-item {
	margin-bottom: 30rpx;
	padding: 25rpx;
	background-color: #fff;
	border-radius: 16rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
	border: 1rpx solid #eee;
}

.plan-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 15rpx;
}

.plan-name {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
}

.plan-price {
	font-size: 36rpx;
	font-weight: bold;
	color: #ff6b35;
}

.plan-desc {
	font-size: 28rpx;
	color: #666;
	margin-bottom: 20rpx;
}

.select-btn {
	width: 100%;
	padding: 20rpx;
	border-radius: 10rpx;
	font-size: 32rpx;
	font-weight: bold;
	display: flex;
	justify-content: center;
	align-items: center;
}

.select-btn.normal {
	background-color: #007aff;
	color: #fff;
}

.select-btn.recommended {
	background: linear-gradient(135deg, #ff6b35 0%, #f7931e 100%);
	color: #fff;
	position: relative;
}

.recommended-badge {
	font-size: 24rpx;
	position: absolute;
	top: -10rpx;
	right: 10rpx;
	background-color: #ffd700;
	color: #333;
	padding: 4rpx 12rpx;
	border-radius: 20rpx;
	font-weight: bold;
}

.benefit-item {
	display: flex;
	align-items: center;
	padding: 15rpx 0;
	border-bottom: 1rpx solid #f8f8f8;
}

.benefit-item:last-child {
	border-bottom: none;
}

.benefit-icon {
	font-size: 32rpx;
	margin-right: 15rpx;
}

.benefit-text {
	font-size: 28rpx;
	color: #333;
	flex: 1;
}

.points-info {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 15rpx;
}

.points-label {
	font-size: 30rpx;
	color: #333;
}

.points-value {
	font-size: 32rpx;
	font-weight: bold;
	color: #ff6b35;
}

.points-desc {
	font-size: 26rpx;
	color: #999;
	margin-bottom: 20rpx;
}

.bill-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 20rpx 0;
	border-bottom: 1rpx solid #f8f8f8;
}

.bill-item:last-child {
	border-bottom: none;
}

.bill-date {
	font-size: 26rpx;
	color: #999;
	width: 120rpx;
}

.bill-desc {
	font-size: 28rpx;
	color: #333;
	flex: 1;
}

.bill-amount {
	font-size: 28rpx;
	color: #ff6b35;
	font-weight: bold;
	width: 100rpx;
	text-align: right;
}

.arrow-right {
	width: 0;
	height: 0;
	border-top: 8rpx solid transparent;
	border-left: 12rpx solid #ccc;
	border-bottom: 8rpx solid transparent;
	margin-left: 20rpx;
}
</style>