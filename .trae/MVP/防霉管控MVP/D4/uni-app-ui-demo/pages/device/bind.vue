<template>
	<view class="container">
		<!-- 顶部导航栏 -->
		<view class="nav-bar">
			<view class="nav-close" @click="closePage">
				<text class="close-icon">❌</text>
			</view>
			<view class="nav-title">
				<text class="title-text">➕ 添加设备</text>
			</view>
			<view class="nav-placeholder"></view>
		</view>

		<!-- 步骤1: 绑定设备 -->
		<view class="card">
			<view class="section-title">1️⃣ 步骤 1: 绑定设备</view>
			
			<view class="bind-methods">
				<!-- 扫码方式 -->
				<view class="scan-section" @click="scanQRCode">
					<view class="scan-icon">
						<text class="scan-text">📷</text>
					</view>
					<view class="scan-desc">扫一扫设备背面的二维码</view>
				</view>
				
				<view class="divider">OR</view>
				
				<!-- 手动输入方式 -->
				<view class="manual-section">
					<view class="form-group">
						<view class="form-label">⌨️ 手动输入 SN 码:</view>
						<input 
							class="form-input" 
							v-model="deviceSN" 
							placeholder="请输入设备SN码" 
							@input="onSNInput"
						/>
					</view>
					<button :class="['btn-primary', deviceSN ? '' : 'disabled']" 
					        :disabled="!deviceSN" 
					        @click="confirmBind">
						🔗 确认绑定
					</button>
				</view>
			</view>
		</view>

		<!-- 步骤2: 绑定成功 & 激活权益 (仅在绑定成功后显示) -->
		<view class="card success-card" v-if="bindSuccess">
			<view class="success-container">
				<text class="success-icon">✅</text>
				<view class="success-title">绑定成功！</view>
				<view class="success-benefits">
					<text class="benefit-item">🎁 恭喜获得首月免费试用权益</text>
					<text class="benefit-item">📄 24小时后将生成首份风险报告</text>
				</view>
				<button class="btn-primary config-btn" @click="goToConfig">
					⚙️ 立即配置防霉策略 >
				</button>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			deviceSN: '',
			bindSuccess: false
		}
	},
	onLoad() {
		// 页面加载时的初始化
		console.log('设备绑定页面加载');
	},
	methods: {
		scanQRCode() {
			// 调用微信扫码API
			uni.scanCode({
				success: (res) => {
					console.log('扫码结果：' + res.result);
					this.deviceSN = res.result;
					this.attemptBind(res.result);
				},
				fail: (err) => {
					console.log('扫码失败：', err);
					uni.showToast({
						title: '扫码失败',
						icon: 'none'
					});
				}
			});
		},
		onSNInput(event) {
			this.deviceSN = event.detail.value;
		},
		confirmBind() {
			if (!this.deviceSN) {
				uni.showToast({
					title: '请输入设备SN码',
					icon: 'none'
				});
				return;
			}
			
			this.attemptBind(this.deviceSN);
		},
		attemptBind(sn) {
			// 模拟设备绑定过程
			uni.showLoading({
				title: '绑定中...'
			});
			
			// 模拟API调用
			setTimeout(() => {
				uni.hideLoading();
				
				// 模拟绑定成功
				const isValidSN = sn.length >= 10; // 简单验证SN格式
				
				if (isValidSN) {
					uni.showToast({
						title: '绑定成功！',
						icon: 'success'
					});
					
					// 设置绑定成功状态
					this.bindSuccess = true;
					
					// 激活首月免费试用权益（模拟）
					console.log('激活首月免费试用权益');
				} else {
					uni.showToast({
						title: '设备SN码无效',
						icon: 'none'
					});
				}
			}, 1500);
		},
		goToConfig() {
			// 跳转到设备配置页面
			uni.navigateTo({
				url: '/pages/device/detail?roomName=新绑定设备'
			});
		},
		closePage() {
			// 关闭当前页面
			uni.navigateBack();
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

.nav-close {
	font-size: 36rpx;
}

.nav-title {
	flex: 1;
	text-align: center;
}

.title-text {
	font-size: 36rpx;
	font-weight: bold;
	color: #333;
}

.nav-placeholder {
	width: 36rpx; /* 占位符，保持居中 */
}

.bind-methods {
	margin-top: 20rpx;
}

.scan-section {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 40rpx 20rpx;
	background-color: #f8f8f8;
	border-radius: 16rpx;
	margin-bottom: 30rpx;
}

.scan-icon {
	width: 120rpx;
	height: 120rpx;
	background-color: #fff;
	border-radius: 50%;
	display: flex;
	justify-content: center;
	align-items: center;
	margin-bottom: 20rpx;
}

.scan-text {
	font-size: 60rpx;
}

.scan-desc {
	font-size: 28rpx;
	color: #666;
}

.divider {
	text-align: center;
	font-size: 28rpx;
	color: #999;
	margin: 20rpx 0;
	position: relative;
}

.divider::before {
	content: '';
	position: absolute;
	top: 50%;
	left: 0;
	right: 0;
	height: 1rpx;
	background-color: #e0e0e0;
	z-index: -1;
}

.divider::after {
	content: 'OR';
	background-color: #f5f5f5;
	padding: 0 20rpx;
}

.manual-section {
	padding: 20rpx 0;
}

.form-group {
	margin-bottom: 30rpx;
}

.form-label {
	display: block;
	margin-bottom: 12rpx;
	font-size: 30rpx;
	color: #333;
	font-weight: 500;
}

.form-input {
	width: 100%;
	height: 80rpx;
	border: 1rpx solid #ddd;
	border-radius: 10rpx;
	padding: 0 20rpx;
	font-size: 30rpx;
	box-sizing: border-box;
	background-color: #fff;
}

.btn-primary.disabled {
	background-color: #ccc;
}

.success-card {
	background: linear-gradient(135deg, #e6fffb 0%, #d4f8ff 100%);
}

.success-container {
	text-align: center;
	padding: 40rpx 20rpx;
}

.success-icon {
	font-size: 80rpx;
	display: block;
	margin-bottom: 20rpx;
}

.success-title {
	font-size: 36rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 20rpx;
}

.success-benefits {
	margin: 30rpx 0;
}

.benefit-item {
	display: block;
	font-size: 28rpx;
	color: #52c41a;
	margin-bottom: 15rpx;
}

.config-btn {
	background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
	color: #fff;
	border: none;
	width: 80%;
	margin-top: 30rpx;
}
</style>