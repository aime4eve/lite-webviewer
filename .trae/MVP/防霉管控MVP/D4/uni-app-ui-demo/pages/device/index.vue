<template>
	<view class="container">
		<!-- 页面标题 -->
		<view class="page-title">
			<text class="title-text">📱 设备管理</text>
		</view>

		<!-- 添加设备按钮 -->
		<view class="add-device-section">
			<button class="add-device-btn" @click="addDevice">
				<text class="add-icon">➕</text>
				<text class="add-text">添加设备</text>
			</button>
		</view>

		<!-- 设备列表 -->
		<view class="card">
			<view class="section-title">🏠 我的设备</view>
			
			<view class="device-item" v-for="(device, index) in devices" :key="index" @click="goToDeviceDetail(device.name)">
				<view class="device-info">
					<text class="device-icon">{{ device.icon }}</text>
					<view class="device-details">
						<text class="device-name">{{ device.name }}</text>
						<view class="device-status-row">
							<text :class="['device-status', device.online ? 'online' : 'offline']">
								{{ device.online ? '🟢 在线' : '🔴 离线' }}
							</text>
							<text class="device-condition">· {{ device.condition }}</text>
						</view>
					</view>
				</view>
				<text class="arrow-right"></text>
			</view>
		</view>

		<!-- 快捷操作 -->
		<view class="card">
			<view class="section-title">⚡ 快捷操作</view>
			
			<view class="quick-actions">
				<button class="action-btn" @click="batchOperation('控制所有设备')">
					<text class="action-icon">🎛️</text>
					<text class="action-text">批量控制</text>
				</button>
				
				<button class="action-btn" @click="batchOperation('查看所有状态')">
					<text class="action-icon">🔍</text>
					<text class="action-text">批量查看</text>
				</button>
				
				<button class="action-btn" @click="batchOperation('设置联动')">
					<text class="action-icon">🔗</text>
					<text class="action-text">联动设置</text>
				</button>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			devices: [
				{
					name: '主卧浴室',
					icon: '🚿',
					online: true,
					condition: '正常 · 自动联动已配置',
					id: 'main-bathroom'
				},
				{
					name: '次卧浴室',
					icon: '🚿',
					online: true,
					condition: '正常 · 仅预警模式',
					id: 'secondary-bathroom'
				},
				{
					name: '客卫',
					icon: '🚿',
					online: false,
					condition: '离线 · 需检查',
					id: 'guest-bathroom'
				}
			]
		}
	},
	onLoad() {
		// 页面加载时获取设备列表
		this.loadDeviceList();
	},
	methods: {
		loadDeviceList() {
			// 模拟从API获取设备列表
			console.log('加载设备列表');
		},
		addDevice() {
			// 跳转到设备绑定页面
			uni.navigateTo({
				url: '/pages/device/bind'
			});
		},
		goToDeviceDetail(deviceName) {
			// 跳转到设备详情页
			uni.navigateTo({
				url: `/pages/device/detail?roomName=${deviceName}`
			});
		},
		batchOperation(operation) {
			uni.showToast({
				title: `${operation}功能暂未开放`,
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

.page-title {
	display: flex;
	justify-content: center;
	margin-bottom: 30rpx;
}

.title-text {
	font-size: 36rpx;
	font-weight: bold;
	color: #333;
}

.add-device-section {
	display: flex;
	justify-content: center;
	margin-bottom: 30rpx;
}

.add-device-btn {
	display: flex;
	align-items: center;
	background: linear-gradient(135deg, #007aff 0%, #00c6ff 100%);
	padding: 20rpx 40rpx;
	border-radius: 50rpx;
	color: #fff;
	font-size: 32rpx;
	font-weight: bold;
	box-shadow: 0 4rpx 12rpx rgba(0, 122, 255, 0.3);
}

.add-icon {
	font-size: 36rpx;
	margin-right: 10rpx;
}

.device-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 25rpx 0;
	border-bottom: 1rpx solid #f8f8f8;
}

.device-item:last-child {
	border-bottom: none;
}

.device-info {
	display: flex;
	align-items: center;
	flex: 1;
}

.device-icon {
	font-size: 40rpx;
	margin-right: 20rpx;
}

.device-details {
	flex: 1;
}

.device-name {
	font-size: 32rpx;
	color: #333;
	font-weight: bold;
	display: block;
}

.device-status-row {
	display: flex;
	align-items: center;
	margin-top: 8rpx;
}

.device-status {
	font-size: 26rpx;
	font-weight: bold;
}

.online {
	color: #52c41a;
}

.offline {
	color: #ff4d4f;
}

.device-condition {
	font-size: 26rpx;
	color: #666;
	margin-left: 8rpx;
}

.arrow-right {
	width: 0;
	height: 0;
	border-top: 8rpx solid transparent;
	border-left: 12rpx solid #ccc;
	border-bottom: 8rpx solid transparent;
	margin-left: 20rpx;
}

.quick-actions {
	display: flex;
	justify-content: space-between;
	padding: 20rpx 0;
}

.action-btn {
	flex: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 20rpx 10rpx;
	margin: 0 10rpx;
	background-color: #f8f8f8;
	border-radius: 16rpx;
}

.action-icon {
	font-size: 40rpx;
	margin-bottom: 10rpx;
}

.action-text {
	font-size: 26rpx;
	color: #333;
}
</style>