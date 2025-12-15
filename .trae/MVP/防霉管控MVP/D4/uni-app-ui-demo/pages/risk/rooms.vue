<template>
	<view class="container">
		<!-- 顶部导航栏 -->
		<view class="nav-bar">
			<view class="nav-title">
				<text class="title-icon">📋</text>
				<text class="title-text">今日高风险房间清单</text>
			</view>
			<button class="filter-btn" @click="showFilter">
				<text class="filter-icon">🔍</text>
			</button>
		</view>

		<!-- 筛选栏 -->
		<view class="filter-bar">
			<picker class="filter-picker" @change="buildingChange" :value="currentBuildingIndex" :range="buildings">
				<view class="picker">
					<text>{{ buildings[currentBuildingIndex] }}</text>
					<text class="picker-arrow">v</text>
				</view>
			</picker>
			
			<picker class="filter-picker" @change="riskLevelChange" :value="currentRiskLevelIndex" :range="riskLevels">
				<view class="picker">
					<text>{{ riskLevels[currentRiskLevelIndex] }}</text>
					<text class="picker-arrow">v</text>
				</view>
			</picker>
			
			<view class="export-btn" @click="exportList">
				📥 导出
			</view>
		</view>

		<!-- 风险列表 -->
		<view class="card">
			<view class="section-title">🚨 风险列表</view>
			
			<view class="risk-item" v-for="(room, index) in riskRooms" :key="index">
				<view class="room-info">
					<text class="room-name">{{ room.name }}</text>
					<text :class="['risk-level', getRiskLevelClass(room.riskLevel)]">
						{{ getRiskLevelEmoji(room.riskLevel) }} {{ room.riskLevel }}
					</text>
				</view>
				<view class="room-details">
					<text class="humidity">💧 {{ room.humidity }}%</text>
				</view>
				<text class="arrow-right" @click="goToRoomDetail(room.id)"></text>
			</view>
		</view>

		<!-- 操作按钮组 -->
		<view class="action-bar">
			<button class="action-btn primary" @click="assignCleaning">
				🧹 批量指派保洁
			</button>
			<button class="action-btn secondary" @click="markAsProcessed">
				✅ 标记为已处理
			</button>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			currentBuildingIndex: 0,
			currentRiskLevelIndex: 0,
			buildings: ['全部楼栋', '金南家园', 'XX公寓', 'YY小区'],
			riskLevels: ['风险等级', '高危', '中危', '低危', '全部'],
			riskRooms: [
				{
					id: '302',
					name: '🏨 Room 302',
					riskLevel: '高危 (指数 0.86)',
					humidity: 88,
					levelValue: 3 // 3代表高危
				},
				{
					id: '505',
					name: '🏨 Room 505',
					riskLevel: '中危 (指数 0.71)',
					humidity: 82,
					levelValue: 2 // 2代表中危
				},
				{
					id: '608',
					name: '🏨 Room 608',
					riskLevel: '低危 (指数 0.55)',
					humidity: 75,
					levelValue: 1 // 1代表低危
				}
			]
		}
	},
	onLoad() {
		// 页面加载时获取风险房间数据
		this.loadRiskRooms();
	},
	methods: {
		loadRiskRooms() {
			// 模拟从API获取风险房间数据
			console.log('加载高风险房间清单');
		},
		showFilter() {
			uni.showActionSheet({
				itemList: ['楼栋筛选', '风险等级筛选', '导出清单'],
				success: (res) => {
					if (res.tapIndex === 0) {
						// 楼栋筛选
						console.log('点击楼栋筛选');
					} else if (res.tapIndex === 1) {
						// 风险等级筛选
						console.log('点击风险等级筛选');
					} else if (res.tapIndex === 2) {
						// 导出清单
						this.exportList();
					}
				}
			});
		},
		buildingChange(event) {
			this.currentBuildingIndex = event.detail.value;
			console.log(`选择楼栋: ${this.buildings[this.currentBuildingIndex]}`);
		},
		riskLevelChange(event) {
			this.currentRiskLevelIndex = event.detail.value;
			console.log(`选择风险等级: ${this.riskLevels[this.currentRiskLevelIndex]}`);
		},
		exportList() {
			uni.showModal({
				title: '导出清单',
				content: '确定要导出高风险房间清单吗？',
				success: (res) => {
					if (res.confirm) {
						uni.showLoading({
							title: '导出中...'
						});
						// 模拟导出操作
						setTimeout(() => {
							uni.hideLoading();
							uni.showToast({
								title: '导出成功',
								icon: 'success'
							});
						}, 1500);
					}
				}
			});
		},
		goToRoomDetail(roomId) {
			uni.navigateTo({
				url: `/pages/risk/room-detail?id=${roomId}`
			});
		},
		assignCleaning() {
			uni.showModal({
				title: '批量指派保洁',
				content: '确定要为选中的高风险房间指派保洁吗？',
				success: (res) => {
					if (res.confirm) {
						uni.showToast({
							title: '保洁已指派',
							icon: 'success'
						});
					}
				}
			});
		},
		markAsProcessed() {
			uni.showModal({
				title: '标记为已处理',
				content: '确定要将选中的房间标记为已处理吗？',
				success: (res) => {
					if (res.confirm) {
						uni.showToast({
							title: '已标记',
							icon: 'success'
						});
					}
				}
			});
		},
		getRiskLevelClass(levelValue) {
			if (levelValue >= 3) return 'risk-danger';
			if (levelValue >= 2) return 'risk-warning';
			return 'risk-safe';
		},
		getRiskLevelEmoji(levelValue) {
			if (levelValue >= 3) return '🔴';
			if (levelValue >= 2) return '🟠';
			return '🟢';
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

.filter-btn {
	background-color: #f0f0f0;
	width: 80rpx;
	height: 60rpx;
	border-radius: 10rpx;
	display: flex;
	justify-content: center;
	align-items: center;
}

.filter-icon {
	font-size: 32rpx;
}

.filter-bar {
	display: flex;
	justify-content: space-between;
	align-items: center;
	background-color: #fff;
	padding: 20rpx;
	margin: 20rpx;
	border-radius: 16rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
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

.export-btn {
	background-color: #007aff;
	color: #fff;
	padding: 10rpx 20rpx;
	border-radius: 10rpx;
	font-size: 28rpx;
}

.risk-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 25rpx 0;
	border-bottom: 1rpx solid #f8f8f8;
}

.risk-item:last-child {
	border-bottom: none;
}

.room-info {
	flex: 1;
}

.room-name {
	font-size: 30rpx;
	color: #333;
	font-weight: bold;
	margin-right: 20rpx;
}

.risk-level {
	font-size: 26rpx;
	padding: 6rpx 12rpx;
	border-radius: 20rpx;
}

.risk-danger {
	background-color: #fff2f0;
	color: #ff4d4f;
}

.risk-warning {
	background-color: #fff7e6;
	color: #fa8c16;
}

.risk-safe {
	background-color: #f6ffed;
	color: #52c41a;
}

.room-details {
	margin-right: 20rpx;
}

.humidity {
	font-size: 28rpx;
	color: #666;
}

.action-bar {
	display: flex;
	justify-content: space-between;
	padding: 30rpx 20rpx;
}

.action-btn {
	flex: 1;
	padding: 25rpx;
	border-radius: 10rpx;
	font-size: 30rpx;
	font-weight: bold;
	text-align: center;
	margin: 0 10rpx;
}

.action-btn.primary {
	background-color: #007aff;
	color: #fff;
}

.action-btn.secondary {
	background-color: #f0f0f0;
	color: #333;
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