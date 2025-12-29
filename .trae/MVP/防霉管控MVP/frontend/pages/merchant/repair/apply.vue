<template>
  <view class="container">
    <view class="section-title">🛠️ 设备报修 (商户版)</view>

    <!-- 设备信息 -->
    <view class="card">
      <view class="card-header">📌 设备信息</view>
      <view class="card-body">
        <text class="info-row">🏨 位置: {{ deviceInfo.location }}</text>
        <text class="info-row">📱 设备: {{ deviceInfo.name }} (SN: {{ deviceInfo.sn }})</text>
        <text class="info-row warning">⚠️ 状态: 🔴 {{ deviceInfo.status }}</text>
      </view>
    </view>

    <!-- 故障描述 -->
    <view class="card">
      <view class="card-header">📝 故障描述</view>
      <view class="card-body">
        <view class="tags">
          <view 
            class="tag" 
            v-for="type in issueTypes" 
            :key="type" 
            :class="{ active: selectedIssueType === type }"
            @click="selectedIssueType = type"
          >
            {{ type }}
          </view>
        </view>
        <textarea class="textarea" placeholder="请输入故障详情..." v-model="issueDescription"></textarea>
      </view>
    </view>

    <!-- 现场照片 -->
    <view class="card">
      <view class="card-header">📷 现场照片 (可选)</view>
      <view class="upload-box" @click="uploadSiteImage">
        <image v-if="siteImage" :src="siteImage" mode="aspectFill" class="uploaded-img"></image>
        <view v-else class="upload-placeholder">
          <text>➕ 上传照片</text>
        </view>
      </view>
    </view>

    <!-- 寄件信息 -->
    <view class="card">
      <view class="card-header">📦 寄件方式</view>
      <radio-group class="radio-group">
        <label class="radio"><radio value="self" checked /> 自行寄回 (请寄往: xx省xx市... 售后部)</label>
      </radio-group>
      
      <view class="logistics-input">
        <view class="upload-mini" @click="uploadLogisticsImage">
          <image v-if="logisticsImage" :src="logisticsImage" mode="aspectFill" class="uploaded-img"></image>
          <text v-else>➕ 凭证</text>
        </view>
        <input class="input-field" placeholder="请输入快递单号" v-model="logisticsNo" />
      </view>
    </view>

    <button class="submit-btn" @click="submitRepair">🚀 提交报修申请</button>
  </view>
</template>

<script>
export default {
  data() {
    return {
      deviceInfo: {
        location: '金南家园1号楼1单元 302室主卧浴室',
        name: '温湿度传感器',
        sn: 'SN123456',
        status: '离线 / 故障'
      },
      issueTypes: ['无法开机', '数据异常', '外观损坏', '其他'],
      selectedIssueType: '',
      issueDescription: '',
      siteImage: '',
      logisticsImage: '',
      logisticsNo: ''
    }
  },
  methods: {
    uploadSiteImage() {
      uni.chooseImage({
        count: 1,
        success: (res) => { this.siteImage = res.tempFilePaths[0]; }
      });
    },
    uploadLogisticsImage() {
      uni.chooseImage({
        count: 1,
        success: (res) => { this.logisticsImage = res.tempFilePaths[0]; }
      });
    },
    submitRepair() {
      if (!this.selectedIssueType || !this.logisticsNo) {
        uni.showToast({ title: '请填写完整信息', icon: 'none' });
        return;
      }
      uni.showLoading({ title: '提交中...' });
      setTimeout(() => {
        uni.hideLoading();
        uni.showToast({ title: '申请已提交' });
        // 返回或跳转到工单列表
        setTimeout(() => uni.navigateBack(), 1500);
      }, 1000);
    }
  }
}
</script>

<style>
.container { padding: 20px; background-color: #f5f5f5; min-height: 100vh; }
.section-title { font-size: 18px; font-weight: bold; margin-bottom: 15px; text-align: center; }
.card { background: white; border-radius: 10px; padding: 15px; margin-bottom: 15px; }
.card-header { font-size: 16px; font-weight: bold; margin-bottom: 10px; border-bottom: 1px solid #eee; padding-bottom: 5px; }
.info-row { display: block; font-size: 14px; color: #333; margin-bottom: 5px; }
.warning { color: red; }
.tags { display: flex; flex-wrap: wrap; gap: 10px; margin-bottom: 10px; }
.tag { padding: 5px 15px; background: #f0f0f0; border-radius: 20px; font-size: 12px; }
.tag.active { background: #007aff; color: white; }
.textarea { width: 100%; height: 80px; background: #f9f9f9; padding: 10px; border-radius: 5px; font-size: 14px; }
.upload-box { width: 100%; height: 100px; background: #f9f9f9; display: flex; align-items: center; justify-content: center; border-radius: 5px; border: 1px dashed #ccc; }
.uploaded-img { width: 100%; height: 100%; object-fit: cover; border-radius: 5px; }
.logistics-input { display: flex; align-items: center; margin-top: 10px; }
.upload-mini { width: 60px; height: 60px; background: #eee; display: flex; align-items: center; justify-content: center; margin-right: 10px; border-radius: 5px; font-size: 12px; }
.input-field { flex: 1; background: #f9f9f9; padding: 10px; border-radius: 5px; }
.submit-btn { background: #007aff; color: white; margin-top: 20px; }
</style>