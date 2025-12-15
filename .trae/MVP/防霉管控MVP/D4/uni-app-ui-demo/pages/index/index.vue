<template>
	<view class="container">
		<!-- 顶部栏 -->
		<view class="top-bar">
			<view class="title-bar">
				<text class="title-icon">🛡️</text>
				<text class="title-text">防霉守护 · 家庭版</text>
				<button class="add-device-btn" @click="addDevice">
					<text class="add-icon">➕</text>
					<text class="add-text">添加设备</text>
				</button>
			</view>
		</view>

		<!-- 浴室防霉状态卡片 -->
		<view class="card">
			<view class="section-title">🛁 浴室防霉状态 (Today)</view>
			
			<view class="status-item">
				<text class="status-icon">🚨</text>
				<text class="status-label">风险指数:</text>
				<text class="status-value status-safe">🟢 18% (安全)</text>
			</view>
			
			<view class="temp-humidity">
				<text class="temp-icon">🌡️</text>
				<text class="temp-value">温度: 24°C</text>
				<text class="humidity-icon">💧</text>
				<text class="humidity-value">湿度: 62%</text>
			</view>
			
			<view class="status-tags">
				<text class="status-tag status-safe">[✅ 安全]</text>
				<text class="status-tag status-safe">[🤖 自动防霉已开启]</text>
			</view>
		</view>

		<!-- 设备概览卡片 -->
		<view class="card">
			<view class="section-title">📱 设备概览</view>
			
			<view class="device-item" @click="goToDeviceDetail('主卧浴室')">
				<text class="device-icon">🏠</text>
				<text class="device-name">主卧浴室</text>
				<text class="device-status device-online">🟢 在线 · 正常 · 🔗 自动联动已配置</text>
				<text class="arrow-right"></text>
			</view>
			
			<view class="device-item" @click="goToDeviceDetail('次卧浴室')">
				<text class="device-icon">🏠</text>
				<text class="device-name">次卧浴室</text>
				<text class="device-status device-warning">🟠 在线 · 正常 · ⚠️ 仅预警模式</text>
				<text class="arrow-right"></text>
			</view>
		</view>

		<!-- 本月防霉战报卡片 -->
		<view class="card">
			<view class="section-title">📊 本月防霉战报</view>
			
			<view class="report-item">
				<text class="report-icon">🛡️</text>
				<text class="report-label">阻断霉变:</text>
				<text class="report-value">12 次</text>
			</view>
			
			<view class="report-item">
				<text class="report-icon">💰</text>
				<text class="report-label">节省电费:</text>
				<text class="report-value">¥4.8</text>
			</view>
			
			<view class="report-item" @click="goToSubscription">
				<text class="report-icon">🪙</text>
				<text class="report-label">防霉积分:</text>
				<text class="report-value report-link">180 分</text>
				<text class="report-link-text">[🎁 兑换 >]</text>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			// 模拟数据
			bathroomStatus: {
				riskLevel: 18,
				temperature: 24,
				humidity: 62,
				status: '安全',
				autoMode: true
			},
			devices: [
				{
					name: '主卧浴室',
					online: true,
					status: '正常',
					linkMode: '自动联动已配置'
				},
				{
					name: '次卧浴室',
					online: true,
					status: '正常',
					linkMode: '仅预警模式'
				}
			],
			reportData: {
				interventions: 12,
				savings: 4.8,
				points: 180
			}
		}
	},
	onLoad() {
		// 页面加载时获取数据
		this.loadDashboardData();
	},
	methods: {
		loadDashboardData() {
			// 模拟从API获取数据
			console.log('加载首页数据');
		},
		addDevice() {
			// 跳转到设备绑定页面
			uni.navigateTo({
				url: '/pages/device/bind'
			});
		},
		goToDeviceDetail(roomName) {
			// 跳转到设备详情页
			uni.navigateTo({
				url: `/pages/device/detail?roomName=${roomName}`
			});
		},
		goToSubscription() {
			// 跳转到订阅页面
			uni.navigateTo({
				url: '/pages/subscription/index'
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

.top-bar {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 30rpx;
	padding: 0 20rpx;
}

.title-bar {
	display: flex;
	align-items: center;
	flex: 1;
}

.title-icon {
	font-size: 40rpx;
	margin-right: 10rpx;
}

.title-text {
	font-size: 36rpx;
	font-weight: bold;
	color: #333;
}

.add-device-btn {
	display: flex;
	align-items: center;
	background-color: #f0f0f0;
	padding: 10rpx 20rpx;
	border-radius: 10rpx;
}

.add-icon {
	font-size: 32rpx;
	margin-right: 5rpx;
}

.add-text {
	font-size: 28rpx;
	color: #666;
}

.status-item {
	display: flex;
	align-items: center;
	margin-bottom: 20rpx;
}

.status-icon {
	font-size: 36rpx;
	margin-right: 15rpx;
}

.status-label {
	font-size: 30rpx;
	color: #333;
	margin-right: 15rpx;
}

.status-value {
	font-size: 32rpx;
	font-weight: bold;
}

.temp-humidity {
	display: flex;
	align-items: center;
	margin-bottom: 20rpx;
}

.temp-icon, .humidity-icon {
	font-size: 32rpx;
	margin-right: 10rpx;
}

.temp-value, .humidity-value {
	font-size: 28rpx;
	color: #666;
	margin-right: 30rpx;
}

.status-tags {
	display: flex;
	gap: 20rpx;
}

.status-tag {
	font-size: 26rpx;
	padding: 8rpx 16rpx;
	border-radius: 20rpx;
	background-color: #f0f8ff;
}

.device-item {
	display: flex;
	align-items: center;
	padding: 20rpx 0;
	border-bottom: 1rpx solid #f0f0f0;
}

.device-item:last-child {
	border-bottom: none;
}

.device-icon {
	font-size: 36rpx;
	margin-right: 15rpx;
}

.device-name {
	font-size: 30rpx;
	color: #333;
	flex: 1;
}

.device-status {
	font-size: 26rpx;
	color: #999;
}

.report-item {
	display: flex;
	align-items: center;
	padding: 15rpx 0;
	border-bottom: 1rpx solid #f8f8f8;
}

.report-item:last-child {
	border-bottom: none;
}

.report-icon {
	font-size: 32rpx;
	margin-right: 15rpx;
}

.report-label {
	font-size: 30rpx;
	color: #333;
	margin-right: 15rpx;
	flex: 1;
}

.report-value {
	font-size: 30rpx;
	color: #333;
	font-weight: bold;
}

.report-link {
	color: #007aff;
}

.report-link-text {
	color: #999;
	font-size: 26rpx;
	margin-left: 10rpx;
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