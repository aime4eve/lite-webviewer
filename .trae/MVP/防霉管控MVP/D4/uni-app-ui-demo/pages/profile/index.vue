<template>
	<view class="container">
		<!-- 用户信息卡片 -->
		<view class="user-card">
			<view class="user-avatar">
				<text class="avatar-text">👤</text>
			</view>
			<view class="user-info">
				<text class="user-name">张三</text>
				<text class="user-level">🛡️ 防霉守护者</text>
			</view>
		</view>

		<!-- 功能列表 -->
		<view class="card">
			<view class="function-item" @click="goToPage('subscription')">
				<text class="function-icon">💎</text>
				<text class="function-name">订阅与权益</text>
				<text class="arrow-right"></text>
			</view>
			
			<view class="function-item" @click="goToPage('points')">
				<text class="function-icon">🪙</text>
				<text class="function-name">防霉积分</text>
				<text class="function-value">{{ points }}</text>
				<text class="arrow-right"></text>
			</view>
			
			<view class="function-item" @click="goToPage('help')">
				<text class="function-icon">❓</text>
				<text class="function-name">帮助与反馈</text>
				<text class="arrow-right"></text>
			</view>
			
			<view class="function-item" @click="goToPage('settings')">
				<text class="function-icon">⚙️</text>
				<text class="function-name">设置</text>
				<text class="arrow-right"></text>
			</view>
		</view>

		<!-- 我的服务 -->
		<view class="card">
			<view class="section-title">相關服務</view>
			
			<view class="service-grid">
				<view class="service-item" @click="goToService('reports')">
					<text class="service-icon">📊</text>
					<text class="service-name">健康报告</text>
				</view>
				
				<view class="service-item" @click="goToService('diagnosis')">
					<text class="service-icon">🔧</text>
					<text class="service-name">远程诊断</text>
				</view>
				
				<view class="service-item" @click="goToService('warranty')">
					<text class="service-icon">🛡️</text>
					<text class="service-name">设备保修</text>
				</view>
				
				<view class="service-item" @click="goToService('community')">
					<text class="service-icon">👥</text>
					<text class="service-name">用户社区</text>
				</view>
			</view>
		</view>

		<!-- 版本信息 -->
		<view class="version-info">
			<text class="version-text">SmartMoldGuard v1.0.0</text>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			points: 180
		}
	},
	methods: {
		goToPage(page) {
			let url = '';
			
			switch(page) {
				case 'subscription':
					url = '/pages/subscription/index';
					break;
				case 'points':
					uni.showToast({
						title: '积分商城暂未开放',
						icon: 'none'
					});
					return;
				case 'help':
					uni.showModal({
						title: '帮助与反馈',
						content: '请联系客服：400-XXX-XXXX',
						showCancel: false
					});
					return;
				case 'settings':
					uni.showToast({
						title: '设置页面暂未开放',
						icon: 'none'
					});
					return;
				default:
					return;
			}
			
			if (url) {
				uni.navigateTo({
					url: url
				});
			}
		},
		goToService(service) {
			switch(service) {
				case 'reports':
					uni.showToast({
						title: '健康报告功能开发中',
						icon: 'none'
					});
					break;
				case 'diagnosis':
					uni.showToast({
						title: '远程诊断功能开发中',
						icon: 'none'
					});
					break;
				case 'warranty':
					uni.showToast({
						title: '设备保修功能开发中',
						icon: 'none'
					});
					break;
				case 'community':
					uni.showToast({
						title: '用户社区功能开发中',
						icon: 'none'
					});
					break;
			}
		}
	}
}
</script>

<style>
.container {
	background-color: #f5f5f5;
	min-height: 100vh;
	padding: 20rpx;
	padding-top: 20rpx;
}

.user-card {
	background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
	border-radius: 16rpx;
	padding: 40rpx 30rpx;
	margin-bottom: 20rpx;
	display: flex;
	align-items: center;
	color: #fff;
	box-shadow: 0 4rpx 12rpx rgba(24, 144, 255, 0.3);
}

.user-avatar {
	width: 100rpx;
	height: 100rpx;
	background-color: rgba(255, 255, 255, 0.2);
	border-radius: 50%;
	display: flex;
	justify-content: center;
	align-items: center;
	margin-right: 20rpx;
}

.avatar-text {
	font-size: 50rpx;
}

.user-info {
	flex: 1;
}

.user-name {
	font-size: 36rpx;
	font-weight: bold;
	display: block;
	margin-bottom: 10rpx;
}

.user-level {
	font-size: 28rpx;
	opacity: 0.9;
}

.function-item {
	display: flex;
	align-items: center;
	padding: 30rpx 0;
	border-bottom: 1rpx solid #f8f8f8;
}

.function-item:last-child {
	border-bottom: none;
}

.function-icon {
	font-size: 40rpx;
	width: 60rpx;
	margin-right: 20rpx;
}

.function-name {
	font-size: 30rpx;
	color: #333;
	flex: 1;
}

.function-value {
	font-size: 28rpx;
	color: #ff6b35;
	margin-right: 20rpx;
	font-weight: bold;
}

.arrow-right {
	width: 0;
	height: 0;
	border-top: 8rpx solid transparent;
	border-left: 12rpx solid #ccc;
	border-bottom: 8rpx solid transparent;
}

.service-grid {
	display: grid;
	grid-template-columns: repeat(4, 1fr);
	gap: 20rpx;
	padding: 20rpx 0;
}

.service-item {
	display: flex;
	flex-direction: column;
	align-items: center;
}

.service-icon {
	font-size: 40rpx;
	margin-bottom: 10rpx;
}

.service-name {
	font-size: 24rpx;
	color: #666;
	text-align: center;
}

.version-info {
	text-align: center;
	margin-top: 40rpx;
	padding: 20rpx;
}

.version-text {
	font-size: 24rpx;
	color: #999;
}
</style>