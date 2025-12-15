<template>
	<view class="container">
		<!-- 顶部导航栏 -->
		<view class="nav-bar">
			<view class="nav-title">
				<text class="title-icon">💻</text>
				<text class="title-text">运维控制台 · 防霉管控系统</text>
			</view>
		</view>

		<!-- 导航标签 -->
		<view class="nav-tabs">
			<view 
				v-for="(tab, index) in tabs" 
				:key="index"
				:class="['tab-item', activeTab === index ? 'active' : '']"
				@click="switchTab(index)"
			>
				{{ tab }}
			</view>
		</view>

		<!-- 告警列表卡片 -->
		<view class="card">
			<view class="section-title">🔔 告警列表 (当前)</view>
			
			<!-- 筛选栏 -->
			<view class="filter-section">
				<picker class="filter-picker" @change="alertTypeChange" :value="currentAlertTypeIndex" :range="alertTypes">
					<view class="picker">
						<text>{{ alertTypes[currentAlertTypeIndex] }}</text>
						<text class="picker-arrow">v</text>
					</view>
				</picker>
				
				<picker class="filter-picker" @change="alertStatusChange" :value="currentAlertStatusIndex" :range="alertStatuses">
					<view class="picker">
						<text>{{ alertStatuses[currentAlertStatusIndex] }}</text>
						<text class="picker-arrow">v</text>
					</view>
				</picker>
			</view>
			
			<!-- 告警项列表 -->
			<view class="alert-item" v-for="(alert, index) in alerts" :key="index" @click="viewAlertDetail(alert)">
				<view class="alert-info">
					<text class="alert-building">{{ alert.building }}</text>
					<text class="alert-type">{{ getAlertTypeIcon(alert.type) }} {{ alert.type }}</text>
				</view>
				<view class="alert-status" :class="getAlertStatusClass(alert.status)">
					{{ alert.status }}
				</view>
				<text class="arrow-right"></text>
			</view>
		</view>

		<!-- 选中告警详情卡片 -->
		<view class="card" v-if="selectedAlert">
			<view class="section-title">📋 详情 (选中项)</view>
			
			<view class="detail-item">
				<text class="detail-label">🆔 SN:</text>
				<text class="detail-value">{{ selectedAlert.sn }}</text>
			</view>
			
			<view class="detail-item">
				<text class="detail-label">📅 剩余租期:</text>
				<text class="detail-value">{{ selectedAlert.rentalPeriod }} 个月</text>
			</view>
			
			<view class="detail-item">
				<text class="detail-label">💰 换件费用:</text>
				<text class="detail-value">¥{{ selectedAlert.replacementCost }}</text>
			</view>
			
			<view class="suggestion-title">💡 建议动作:</view>
			
			<view class="suggestion-item">
				<text class="suggestion-icon">📨</text>
				<text class="suggestion-text">发送告警到用户小程序</text>
			</view>
			
			<view class="suggestion-item">
				<text class="suggestion-icon">📝</text>
				<text class="suggestion-text">生成资产赔付工单</text>
			</view>
			
			<button class="btn-primary action-btn" @click="executeAction">
				⚡ 一键执行
			</button>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			tabs: ['📊 仪表盘', '🕸️ 拓扑', '🔔 告警', '💳 订阅'],
			activeTab: 2, // 默认显示告警标签
			currentAlertTypeIndex: 0,
			currentAlertStatusIndex: 0,
			alertTypes: ['全部类型', '🛠️ 防拆告警', '🔌 心跳丢失', '🚨 高风险', '🔧 维护提醒'],
			alertStatuses: ['全部状态', '❌ 未处理', '✅ 已处理', '⚠️ 处理中'],
			alerts: [
				{
					id: 1,
					building: '🏢 金南家园三期 3502',
					type: '防拆告警',
					status: '❌ 未处理',
					sn: 'SN-20251215001',
					rentalPeriod: 9,
					representationCost: 50
				},
				{
					id: 2,
					building: '🏢 XX公寓 1201',
					type: '心跳丢失',
					status: '❌ 未处理',
					sn: 'SN-20251215002',
					rentalPeriod: 6,
					representationCost: 45
				}
			],
			selectedAlert: null
		}
	},
	onLoad() {
		// 页面加载时获取告警数据
		this.loadAlerts();
	},
	onShow() {
		// 页面显示时更新数据
		this.loadAlerts();
	},
	methods: {
		loadAlerts() {
			// 模拟从API获取告警数据
			console.log('加载告警数据');
			if (this.alerts.length > 0) {
				// 默认选中第一个告警
				this.selectedAlert = this.alerts[0];
			}
		},
		switchTab(index) {
			this.activeTab = index;
			console.log(`切换到标签: ${this.tabs[index]}`);
		},
		alertTypeChange(event) {
			this.currentAlertTypeIndex = event.detail.value;
			console.log(`选择告警类型: ${this.alertTypes[this.currentAlertTypeIndex]}`);
		},
		alertStatusChange(event) {
			this.currentAlertStatusIndex = event.detail.value;
			console.log(`选择告警状态: ${this.alertStatuses[this.currentAlertStatusIndex]}`);
		},
		viewAlertDetail(alert) {
			this.selectedAlert = alert;
			console.log(`查看告警详情: ${alert.building}`);
		},
		getAlertTypeIcon(type) {
			switch (type) {
				case '防拆告警':
					return '🛠️';
				case '心跳丢失':
					return '🔌';
				case '高风险':
					return '🚨';
				case '维护提醒':
					return '🔧';
				default:
					return '🔔';
			}
		},
		getAlertStatusClass(status) {
			if (status.includes('未处理')) return 'status-unprocessed';
			if (status.includes('已处理')) return 'status-processed';
			if (status.includes('处理中')) return 'status-processing';
			return '';
		},
		executeAction() {
			if (!this.selectedAlert) {
				uni.showToast({
					title: '请先选择一个告警',
					icon: 'none'
				});
				return;
			}
			
			uni.showModal({
				title: '一键执行',
				content: `确定要对设备 ${this.selectedAlert.sn} 执行建议操作吗？`,
				success: (res) => {
					if (res.confirm) {
						uni.showLoading({
							title: '执行中...'
						});
						// 模拟执行操作
						setTimeout(() => {
							uni.hideLoading();
							uni.showToast({
								title: '操作执行成功',
								icon: 'success'
							});
							// 更新告警状态
							this.selectedAlert.status = '✅ 已处理';
						}, 1500);
					}
				}
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
	padding-top: 20rpx;
}

.nav-bar {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 20rpx;
	padding: 0 20rpx;
}

.nav-title {
	display: flex;
	align-items: center;
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

.nav-tabs {
	display: flex;
	background-color: #fff;
	border-radius: 16rpx;
	overflow: hidden;
	margin-bottom: 20rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.tab-item {
	flex: 1;
	text-align: center;
	padding: 20rpx 10rpx;
	font-size: 28rpx;
	color: #666;
}

.tab-item.active {
	background-color: #007aff;
	color: #fff;
	font-weight: bold;
}

.filter-section {
	display: flex;
	justify-content: space-between;
	margin: 20rpx 0;
}

.filter-picker {
	flex: 1;
	padding: 10rpx;
	background-color: #f8f8f8;
	border-radius: 10rpx;
	text-align: center;
	margin: 0 10rpx;
}

.picker {
	display: flex;
	justify-content: center;
	align-items: center;
}

.picker-arrow {
	font-size: 24rpx;
	color: #999;
	margin-left: 10rpx;
}

.alert-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 25rpx 0;
	border-bottom: 1rpx solid #f8f8f8;
}

.alert-item:last-child {
	border-bottom: none;
}

.alert-info {
	flex: 1;
}

.alert-building {
	font-size: 30rpx;
	color: #333;
	font-weight: bold;
	display: block;
	margin-bottom: 10rpx;
}

.alert-type {
	font-size: 26rpx;
	color: #666;
}

.alert-status {
	font-size: 26rpx;
	padding: 6rpx 12rpx;
	border-radius: 20rpx;
	margin-right: 20rpx;
}

.status-unprocessed {
	background-color: #fff2f0;
	color: #ff4d4f;
}

.status-processed {
	background-color: #f6ffed;
	color: #52c41a;
}

.status-processing {
	background-color: #fff7e6;
	color: #fa8c16;
}

.detail-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 15rpx 0;
	border-bottom: 1rpx solid #f8f8f8;
}

.detail-item:last-child {
	border-bottom: none;
}

.detail-label {
	font-size: 30rpx;
	color: #333;
}

.detail-value {
	font-size: 28rpx;
	color: #333;
	font-weight: bold;
}

.suggestion-title {
	font-size: 30rpx;
	color: #333;
	font-weight: bold;
	margin: 20rpx 0 15rpx 0;
}

.suggestion-item {
	display: flex;
	align-items: center;
	padding: 15rpx 0;
}

.suggestion-icon {
	font-size: 32rpx;
	margin-right: 15rpx;
}

.suggestion-text {
	font-size: 28rpx;
	color: #666;
}

.action-btn {
	margin-top: 30rpx;
	width: 100%;
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