<template>
  <div class="modal-overlay">
    <div class="modal-content asset-protection-modal">
      <div class="modal-header">
        <h3 class="modal-title">💻 资产保全处理</h3>
        <button class="close-btn" @click="$emit('close')">×</button>
      </div>

      <div class="modal-body">
        <!-- 告警信息 -->
        <section class="info-section">
          <h4 class="section-title">🚨 告警信息</h4>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">告警类型:</span>
              <span class="value warning">🛠️ {{ fault.type === 'tamper' ? '设备防拆告警' : fault.type }}</span>
            </div>
            <div class="info-item">
              <span class="label">设备SN:</span>
              <span class="value">{{ fault.deviceSn }}</span>
            </div>
            <div class="info-item">
              <span class="label">设备位置:</span>
              <span class="value">{{ fault.location }}</span>
            </div>
            <div class="info-item">
              <span class="label">告警时间:</span>
              <span class="value">{{ fault.time }}</span>
            </div>
          </div>
        </section>

        <!-- 租赁信息 -->
        <section class="info-section">
          <h4 class="section-title">💰 租赁信息</h4>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">剩余租期:</span>
              <span class="value">9 个月</span>
            </div>
            <div class="info-item">
              <span class="label">押金余额:</span>
              <span class="value">¥200</span>
            </div>
            <div class="info-item">
              <span class="label">换件费用:</span>
              <span class="value highlight">¥50</span>
            </div>
          </div>
        </section>

        <!-- 赔付方案 -->
        <section class="info-section highlight-section">
          <h4 class="section-title">💡 赔付方案</h4>
          <div class="compensation-details">
            <div class="detail-row">
              <span class="check-icon">✅</span>
              <span>优先从押金中扣除:</span>
              <span class="amount">¥50</span>
            </div>
            <div class="detail-row total">
              <span>💰 押金抵扣后余额:</span>
              <span class="amount final">¥150</span>
            </div>
          </div>
        </section>

        <!-- 处理流程 -->
        <section class="process-section">
          <h4 class="section-title">📋 处理流程</h4>
          <div class="process-steps">
            <button 
              class="process-btn" 
              :class="{ active: currentStep >= 1, completed: currentStep > 1 }"
              @click="handleStep(1)"
              :disabled="currentStep > 1"
            >
              📱 通知用户
            </button>
            <div class="step-arrow">→</div>
            <button 
              class="process-btn" 
              :class="{ active: currentStep >= 2, completed: currentStep > 2 }"
              @click="handleStep(2)"
              :disabled="currentStep !== 1"
            >
              📝 生成赔付单
            </button>
            <div class="step-arrow">→</div>
            <button 
              class="process-btn" 
              :class="{ active: currentStep >= 3, completed: currentStep > 3 }"
              @click="handleStep(3)"
              :disabled="currentStep !== 2"
            >
              📦 生成返修码
            </button>
            <div class="step-arrow">→</div>
            <button 
              class="process-btn" 
              :class="{ active: currentStep >= 4, completed: currentStep > 4 }"
              @click="handleStep(4)"
              :disabled="currentStep !== 3"
            >
              🔄 刷新状态
            </button>
          </div>
        </section>

        <!-- 操作记录 -->
        <section class="logs-section">
          <h4 class="section-title">📌 操作记录</h4>
          <div class="logs-list">
            <div v-for="(log, index) in logs" :key="index" class="log-item">
              <span class="log-time">{{ log.time }}:</span>
              <span class="log-content">{{ log.content }}</span>
            </div>
          </div>
        </section>
      </div>

      <div class="modal-footer">
        <button class="btn cancel-btn" @click="$emit('close')">关闭</button>
        <button class="btn confirm-btn" @click="finish" :disabled="currentStep < 4">完成处理</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AssetProtectionModal',
  props: {
    fault: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      currentStep: 0,
      logs: []
    }
  },
  mounted() {
    // Initialize logs
    this.logs.push({ time: this.fault.time, content: '告警触发' });
    this.logs.push({ time: this.getRelativeTime(30), content: '系统自动计算赔付金额' });
    
    // 自动开始第一步
    setTimeout(() => {
        this.currentStep = 1;
        this.addLog('待用户确认');
    }, 500);
  },
  methods: {
    handleStep(step) {
      this.currentStep = step;
      let content = '';
      switch(step) {
        case 1: content = '已发送通知给用户'; break;
        case 2: content = '已生成赔付单 #PAY-20251215-001'; break;
        case 3: content = '已生成返修码 #RET-88291'; break;
        case 4: content = '状态已刷新，等待用户支付'; break;
      }
      
      this.addLog(content);
      
      if (step < 4) {
          // Simulate user action or auto progression for demo
          // In real app, might wait for backend
      }
    },
    addLog(content) {
       const now = new Date();
       const timeStr = `${now.getFullYear()}-${String(now.getMonth()+1).padStart(2,'0')}-${String(now.getDate()).padStart(2,'0')} ${String(now.getHours()).padStart(2,'0')}:${String(now.getMinutes()).padStart(2,'0')}:${String(now.getSeconds()).padStart(2,'0')}`;
       this.logs.push({ time: timeStr, content });
    },
    getRelativeTime(secondsAdd) {
        // Simple helper for demo initial logs
        // Assuming fault.time is valid string, but for safety just using current time for demo
        return '2025-12-15 14:30:30'; 
    },
    finish() {
      this.$emit('resolved', this.fault);
      this.$emit('close');
    }
  }
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  border-radius: 8px;
  width: 700px;
  max-width: 90%;
  max-height: 90vh;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.modal-header {
  padding: 16px 24px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #f8f9fa;
}

.modal-title {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
}

.modal-body {
  padding: 24px;
  flex: 1;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 12px;
  border-left: 4px solid #3498db;
  padding-left: 8px;
}

.info-section {
  margin-bottom: 24px;
  background: #f8f9fa;
  padding: 16px;
  border-radius: 6px;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.info-item {
  display: flex;
  align-items: center;
}

.label {
  color: #666;
  width: 80px;
  font-size: 14px;
}

.value {
  color: #333;
  font-weight: 500;
  font-size: 14px;
}

.value.warning {
  color: #e74c3c;
}

.value.highlight {
  color: #e67e22;
  font-weight: bold;
}

.highlight-section {
  background-color: #fff8e1;
  border: 1px solid #ffe0b2;
}

.compensation-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.amount {
  font-weight: bold;
  margin-left: auto;
}

.amount.final {
  color: #27ae60;
  font-size: 18px;
}

.process-section {
  margin-bottom: 24px;
}

.process-steps {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 0;
}

.process-btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background-color: white;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 13px;
}

.process-btn.active {
  border-color: #3498db;
  color: #3498db;
  font-weight: 500;
}

.process-btn.completed {
  background-color: #3498db;
  color: white;
  border-color: #3498db;
}

.process-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.step-arrow {
  color: #999;
  font-weight: bold;
}

.logs-section {
  border: 1px solid #eee;
  padding: 16px;
  border-radius: 6px;
  max-height: 150px;
  overflow-y: auto;
}

.logs-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.log-item {
  font-size: 13px;
  color: #666;
}

.log-time {
  margin-right: 8px;
  color: #999;
}

.modal-footer {
  padding: 16px 24px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.btn {
  padding: 8px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  border: none;
}

.cancel-btn {
  background-color: #f1f2f6;
  color: #666;
}

.confirm-btn {
  background-color: #3498db;
  color: white;
}

.confirm-btn:disabled {
  background-color: #a0cceb;
  cursor: not-allowed;
}
</style>
