<template>
  <view class="container">
    <!-- 步骤 1: 填写返修信息 -->
    <view v-if="step === 1" class="form-step">
      <view class="section-title">📦 申请返修</view>
      
      <view class="card">
        <view class="card-header">📝 寄件信息</view>
        <view class="card-body">
          <text class="info-text">请将设备寄回以下地址：</text>
          <text class="address-text">📍 地址: xx省xx市xx区xx路xx号 售后维修部</text>
          <text class="address-text">📞 电话: 400-123-4567</text>
        </view>
      </view>

      <view class="card">
        <view class="card-header">📷 上传物流凭证 (必填)</view>
        <view class="card-body">
          <view class="upload-box" @click="uploadImage">
            <image v-if="logisticsImage" :src="logisticsImage" mode="aspectFill" class="uploaded-img"></image>
            <view v-else class="upload-placeholder">
              <text class="icon">➕</text>
              <text>上传快递单照片</text>
            </view>
          </view>
          <input class="input-field" placeholder="请输入快递单号" v-model="logisticsNo" />
        </view>
      </view>

      <button class="submit-btn" @click="submitRepair">🚀 提交申请</button>
    </view>

    <!-- 步骤 2: 返修进度追踪 -->
    <view v-else-if="step === 2" class="progress-step">
      <view class="section-title">📦 返修进度</view>
      
      <view class="card">
        <view class="card-header">📅 进度时间轴</view>
        <view class="timeline">
          <view class="timeline-item" v-for="(item, index) in timeline" :key="index">
            <text class="dot" :class="{ active: index === 0 }">●</text>
            <view class="content">
              <text class="time">{{ item.time }}</text>
              <text class="status">{{ item.status }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="card" v-if="orderInfo.officialLogisticsNo">
        <view class="card-header">🚚 寄回物流信息</view>
        <view class="card-body">
          <text class="info-row">📦 快递公司: 顺丰速运</text>
          <text class="info-row">🔢 快递单号: {{ orderInfo.officialLogisticsNo }}</text>
          <image v-if="orderInfo.officialLogisticsImage" :src="orderInfo.officialLogisticsImage" mode="aspectFill" class="logistics-img"></image>
        </view>
        <button class="confirm-btn" @click="confirmReceipt">✅ 确认收到设备</button>
      </view>

      <view class="card" v-else>
        <view class="card-header">📷 您的寄件凭证</view>
        <view class="card-body">
           <image :src="logisticsImage" mode="aspectFill" class="logistics-img"></image>
           <text class="info-row">单号: {{ logisticsNo }}</text>
        </view>
        <button class="contact-btn">💬 联系客服</button>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      step: 1,
      deviceId: '',
      logisticsNo: '',
      logisticsImage: '',
      orderInfo: {},
      timeline: [
        { time: '2025-12-16 10:00', status: '用户已提交申请' },
        { time: '2025-12-16 10:05', status: '待运营确认收货' }
      ]
    }
  },
  onLoad(options) {
    this.deviceId = options.deviceId;
  },
  methods: {
    uploadImage() {
      // 模拟上传
      uni.chooseImage({
        count: 1,
        success: (res) => {
          this.logisticsImage = res.tempFilePaths[0];
        }
      });
    },
    submitRepair() {
      if (!this.logisticsNo || !this.logisticsImage) {
        uni.showToast({ title: '请填写完整信息', icon: 'none' });
        return;
      }
      uni.showLoading({ title: '提交中...' });
      setTimeout(() => {
        uni.hideLoading();
        this.step = 2;
        uni.showToast({ title: '提交成功' });
      }, 1000);
    },
    confirmReceipt() {
      uni.showModal({
        title: '确认收货',
        content: '确认已收到修好的设备？确认后将跳转至设备激活页面。',
        success: (res) => {
          if (res.confirm) {
            // 跳转至设备绑定与初始化
            uni.reLaunch({
              url: '/pages/device/bind?mode=reactivate&deviceId=' + this.deviceId
            });
          }
        }
      });
    }
  }
}
</script>

<style>
.container { padding: 20px; background-color: #f5f5f5; min-height: 100vh; }
.section-title { font-size: 18px; font-weight: bold; margin-bottom: 15px; text-align: center; }
.card { background: white; border-radius: 10px; padding: 15px; margin-bottom: 15px; box-shadow: 0 2px 5px rgba(0,0,0,0.05); }
.card-header { font-size: 16px; font-weight: bold; margin-bottom: 10px; border-bottom: 1px solid #eee; padding-bottom: 5px; }
.info-text, .address-text, .info-row { display: block; font-size: 14px; color: #666; margin-bottom: 5px; }
.upload-box { width: 100%; height: 150px; background: #f9f9f9; border: 1px dashed #ccc; display: flex; align-items: center; justify-content: center; margin-bottom: 10px; border-radius: 5px; }
.upload-placeholder { display: flex; flex-direction: column; align-items: center; color: #999; }
.uploaded-img, .logistics-img { width: 100%; height: 100%; object-fit: cover; border-radius: 5px; }
.input-field { background: #f9f9f9; padding: 10px; border-radius: 5px; font-size: 14px; }
.submit-btn { background: #007aff; color: white; margin-top: 20px; }
.confirm-btn { background: #4cd964; color: white; margin-top: 10px; }
.contact-btn { background: #f0ad4e; color: white; margin-top: 10px; }
.timeline-item { display: flex; margin-bottom: 15px; }
.dot { color: #ccc; margin-right: 10px; }
.dot.active { color: #4cd964; }
.time { font-size: 12px; color: #999; display: block; }
.status { font-size: 14px; }
</style>