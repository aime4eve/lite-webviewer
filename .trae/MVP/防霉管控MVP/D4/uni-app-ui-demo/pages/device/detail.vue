<template>
	<view class="container">
		<!-- 顶部导航栏 -->
		<view class="nav-bar">
			<view class="nav-back" @click="goBack">
				<text class="back-icon">🔙</text>
			</view>
			<view class="nav-title">
				<text class="room-icon">🏠</text>
				<text class="room-name">{{ roomName }}</text>
			</view>
		</view>

		<!-- 实时状态卡片 -->
		<view class="card">
			<view class="section-title">📊 实时状态</view>
			
			<view class="status-row">
				<text class="status-label">💧 湿度:</text>
				<text class="status-value">{{ currentData.humidity }}%</text>
			</view>
			
			<view class="status-row">
				<text class="status-label">🌡️ 温度:</text>
				<text class="status-value">{{ currentData.temperature }}°C</text>
			</view>
			
			<view class="status-row risk-prediction">
				<text class="status-label">🔮 3h后霉变概率:</text>
				<text :class="['status-value', getRiskLevelClass(riskPrediction.risk3h)]">
					{{ riskPrediction.risk3h }}% ({{ riskPrediction.level }})
				</text>
			</view>
		</view>

		<!-- 自动防霉策略卡片 -->
		<view class="card">
			<view class="section-title">🤖 自动防霉策略</view>
			
			<view class="switch-group">
				<switch :checked="autoModeEnabled" @change="toggleAutoMode" />
				<text class="switch-label">启用自动防霉</text>
			</view>
			
			<view class="rule-preview">
				<text class="rule-title">📜 规则预览:</text>
				<view class="rule-item">
					• 湿度 > 85% 且持续 30min ➡️ 开启排风扇
				</view>
				<view class="rule-item">
					• 30min 后湿度仍 > 60% ➡️ 开启加热烘干
				</view>
			</view>
			
			<button class="btn-secondary" @click="viewDetailedRules">
				🔍 查看详细规则 >
			</button>
		</view>

		<!-- 设备联动映射卡片 -->
		<view class="card">
			<view class="section-title">🔗 设备联动映射</view>
			
			<view class="mapping-item">
				<text class="mapping-label">🔘 开关位 1:</text>
				<picker @change="bindFanChange" :value="deviceMapping.fanIndex" :range="deviceOptions">
					<view class="picker">
						<text class="picker-icon">🌀</text>
						<text class="picker-text">{{ deviceOptions[deviceMapping.fanIndex] }}</text>
						<text class="picker-arrow">v</text>
					</view>
				</picker>
			</view>
			
			<view class="mapping-item">
				<text class="mapping-label">🔘 开关位 2:</text>
				<picker @change="bindHeaterChange" :value="deviceMapping.heaterIndex" :range="deviceOptions">
					<view class="picker">
						<text class="picker-icon">🔥</text>
						<text class="picker-text">{{ deviceOptions[deviceMapping.heaterIndex] }}</text>
						<text class="picker-arrow">v</text>
					</view>
				</picker>
			</view>
			
			<view class="mapping-item">
				<text class="mapping-label">🔘 开关位 3:</text>
				<picker @change="bindLightChange" :value="deviceMapping.lightIndex" :range="deviceOptions">
					<view class="picker">
						<text class="picker-icon">💡</text>
						<text class="picker-text">{{ deviceOptions[deviceMapping.lightIndex] }}</text>
						<text class="picker-arrow">v</text>
					</view>
				</picker>
			</view>
			
			<button class="btn-primary" @click="saveConfiguration">
				💾 保存配置
			</button>
		</view>

		<!-- 故障与告警卡片 -->
		<view class="card">
			<view class="section-title">🛠️ 故障与告警</view>
			
			<view class="alarm-item">
				<text class="alarm-label">🔔 最近告警:</text>
				<text class="alarm-value">无</text>
			</view>
			
			<view class="signal-item">
				<text class="signal-label">📶 信号强度:</text>
				<text :class="['signal-value', getSignalStrengthClass(diagnosticData.rssi)]">
					{{ getSignalStrengthText(diagnosticData.rssi) }} (RSSI {{ diagnosticData.rssi }}dBm)
				</text>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			roomName: '主卧浴室',
			currentData: {
				humidity: 72,
				temperature: 23
			},
			riskPrediction: {
				risk3h: 68,
				level: '🟠 中风险'
			},
			autoModeEnabled: true,
			deviceOptions: ['排风扇', '加热器', '照明灯', '未配置'],
			deviceMapping: {
				fanIndex: 0, // 默认排风扇
				heaterIndex: 1, // 默认加热器
				lightIndex: 2 // 默认照明灯
			},
			diagnosticData: {
				rssi: -95
			}
		}
	},
	onLoad(option) {
		// 获取传递的房间名称
		if (option.roomName) {
			this.roomName = option.roomName;
		}
		this.loadDeviceData();
	},
	methods: {
		loadDeviceData() {
			// 模拟加载设备数据
			console.log(`加载${this.roomName}设备数据`);
		},
		goBack() {
			uni.navigateBack();
		},
		toggleAutoMode(event) {
			this.autoModeEnabled = event.detail.value;
		},
		viewDetailedRules() {
			uni.showModal({
				title: '详细防霉规则',
				content: '1. 当湿度超过85%并持续30分钟时，自动开启排风扇\n2. 若30分钟后湿度仍高于60%，将联动加热器进行烘干\n3. 根据时间设置，夜间采用静音模式',
				showCancel: false,
				confirmText: '确定'
			});
		},
		bindFanChange(event) {
			this.deviceMapping.fanIndex = event.detail.value;
		},
		bindHeaterChange(event) {
			this.deviceMapping.heaterIndex = event.detail.value;
		},
		bindLightChange(event) {
			this.deviceMapping.lightIndex = event.detail.value;
		},
		saveConfiguration() {
			uni.showModal({
				title: '配置保存',
				content: '设备联动配置已保存成功！',
				showCancel: false,
				confirmText: '确定'
			});
		},
		getRiskLevelClass(riskValue) {
			if (riskValue > 80) return 'status-danger';
			if (riskValue > 60) return 'status-warning';
			return 'status-safe';
		},
		getSignalStrengthClass(rssi) {
			if (rssi > -80) return 'signal-excellent';
			if (rssi > -90) return 'signal-good';
			if (rssi > -100) return 'signal-fair';
			return 'signal-poor';
		},
		getSignalStrengthText(rssi) {
			if (rssi > -80) return '🟢 良好';
			if (rssi > -90) return '🟡 一般';
			if (rssi > -100) return '🟠 较差';
			return '🔴 很差';
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

.room-icon {
	font-size: 36rpx;
	margin-right: 10rpx;
}

.room-name {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
}

.status-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 15rpx 0;
	border-bottom: 1rpx solid #f8f8f8;
}

.status-row:last-child {
	border-bottom: none;
}

.status-label {
	font-size: 30rpx;
	color: #333;
}

.status-value {
	font-size: 30rpx;
	font-weight: bold;
	color: #333;
}

.risk-prediction {
	margin-top: 15rpx;
	padding-top: 15rpx;
	border-top: 1rpx solid #f8f8f8;
}

.switch-group {
	display: flex;
	align-items: center;
	margin-bottom: 20rpx;
}

.switch-label {
	font-size: 30rpx;
	color: #333;
	margin-left: 20rpx;
}

.rule-preview {
	margin: 20rpx 0;
}

.rule-title {
	font-size: 28rpx;
	color: #666;
	display: block;
	margin-bottom: 10rpx;
}

.rule-item {
	font-size: 26rpx;
	color: #666;
	line-height: 1.6;
	margin-bottom: 8rpx;
}

.mapping-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 20rpx 0;
	border-bottom: 1rpx solid #f8f8f8;
}

.mapping-item:last-child {
	border-bottom: none;
	margin-bottom: 30rpx;
}

.mapping-label {
	font-size: 30rpx;
	color: #333;
	flex: 1;
}

.picker {
	display: flex;
	align-items: center;
	flex: 2;
	justify-content: flex-end;
}

.picker-icon {
	font-size: 30rpx;
	margin-right: 10rpx;
}

.picker-text {
	font-size: 28rpx;
	color: #333;
	margin-right: 10rpx;
}

.picker-arrow {
	font-size: 24rpx;
	color: #999;
}

.alarm-item, .signal-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 15rpx 0;
}

.alarm-label, .signal-label {
	font-size: 30rpx;
	color: #333;
}

.alarm-value, .signal-value {
	font-size: 28rpx;
	color: #666;
}

.signal-excellent { color: #52c41a; }
.signal-good { color: #52c41a; }
.signal-fair { color: #faad14; }
.signal-poor { color: #ff4d4f; }

.status-safe { color: #52c41a; }
.status-warning { color: #faad14; }
.status-danger { color: #ff4d4f; }
</style>