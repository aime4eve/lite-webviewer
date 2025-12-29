<template>
  <view class="container">
    <view class="section-title">📥 导出风险报告</view>

    <!-- 批量操作说明 -->
    <view class="card">
      <view class="card-header">📌 已选择: {{ selectedCount }} 个房间</view>
      <view class="card-body">
        <text class="room-list">{{ selectedRooms }}</text>
      </view>
    </view>

    <!-- 导出选项 -->
    <view class="card">
      <view class="card-header">⚙️ 导出选项</view>
      <view class="card-body">
        <view class="form-item">
          <text class="label">📅 报告周期</text>
          <picker mode="selector" :range="periods" @change="onPeriodChange">
            <view class="picker">{{ period || '请选择' }}</view>
          </picker>
        </view>
        
        <view class="form-item">
          <text class="label">📊 报告格式</text>
          <picker mode="selector" :range="formats" @change="onFormatChange">
            <view class="picker">{{ format || '请选择' }}</view>
          </picker>
        </view>
      </view>
    </view>

    <button class="submit-btn" @click="exportReport">📥 立即导出</button>
  </view>
</template>

<script>
export default {
  data() {
    return {
      selectedCount: 3,
      selectedRooms: '302室主卧浴室, 505室次卧浴室, 608室浴室',
      periods: ['今日', '本周', '本月'],
      period: '今日',
      formats: ['Excel', 'PDF'],
      format: 'Excel'
    }
  },
  methods: {
    onPeriodChange(e) {
      this.period = this.periods[e.detail.value];
    },
    onFormatChange(e) {
      this.format = this.formats[e.detail.value];
    },
    exportReport() {
      uni.showLoading({ title: '生成报告中...' });
      // 模拟 API 调用
      setTimeout(() => {
        uni.hideLoading();
        uni.showToast({ title: '导出成功', icon: 'success' });
        // 模拟下载
        console.log(`Downloading report: Period=${this.period}, Format=${this.format}`);
        // 返回上一页
        setTimeout(() => uni.navigateBack(), 1500);
      }, 1500);
    }
  }
}
</script>

<style>
.container { padding: 20px; background-color: #f5f5f5; min-height: 100vh; }
.section-title { font-size: 18px; font-weight: bold; margin-bottom: 15px; text-align: center; }
.card { background: white; border-radius: 10px; padding: 15px; margin-bottom: 15px; }
.card-header { font-size: 16px; font-weight: bold; margin-bottom: 10px; border-bottom: 1px solid #eee; padding-bottom: 5px; }
.room-list { font-size: 14px; color: #666; line-height: 1.5; }
.form-item { display: flex; align-items: center; justify-content: space-between; padding: 15px 0; border-bottom: 1px solid #f0f0f0; }
.label { font-size: 14px; color: #333; }
.picker { font-size: 14px; color: #007aff; }
.submit-btn { background: #007aff; color: white; margin-top: 20px; }
</style>