<template>
  <OperatorLayout pageTitle="🔔 设备故障监控" activeNav="alarms">
    <div class="device-fault-monitoring-container">
      <!-- 页面操作栏 -->
      <div class="page-actions">
        <select class="filter-select" v-model="faultFilter.type" @change="loadFaults">
          <option value="all">所有故障类型</option>
          <option value="tamper">🛠️ 防拆告警</option>
          <option value="offline">🔌 心跳丢失</option>
          <option value="malfunction">⚠️ 设备故障</option>
        </select>
      </div>

    <!-- 故障统计信息 -->
    <section class="faults-stats-section">
      <div class="stats-card">
        <div class="stat-item">
          <span class="stat-label">今日故障总数</span>
          <span class="stat-value">{{ todayFaults }}</span>
        </div>
        <div class="stat-item unhandled">
          <span class="stat-label">未处理故障</span>
          <span class="stat-value">{{ unhandledFaults }}</span>
        </div>
        <div class="stat-item handled">
          <span class="stat-label">已处理故障</span>
          <span class="stat-value">{{ handledFaults }}</span>
        </div>
        <div class="stat-item resolved">
          <span class="stat-label">已解决故障</span>
          <span class="stat-value">{{ resolvedFaults }}</span>
        </div>
      </div>
    </section>

    <!-- 故障设备列表 -->
    <section class="faults-list-section">
      <div class="section-header">
        <h2 class="section-title">故障设备列表</h2>
        <div class="search-bar">
          <input type="text" placeholder="搜索设备名称、SN码或位置" v-model="searchKeyword" @input="searchFaults">
          <button class="search-btn">🔍</button>
        </div>
      </div>

      <div class="faults-list" v-if="!loadingFaults">
        <div class="faults-header">
          <div class="fault-column device-name">设备名称</div>
          <div class="fault-column device-sn">SN码</div>
          <div class="fault-column fault-type">故障类型</div>
          <div class="fault-column fault-location">位置</div>
          <div class="fault-column fault-time">故障时间</div>
          <div class="fault-column fault-status">处理状态</div>
          <div class="fault-column fault-actions">操作</div>
        </div>
        <div 
          class="fault-item" 
          v-for="fault in filteredFaults" 
          :key="fault.id"
          :class="fault.status"
        >
          <div class="fault-column device-name">{{ fault.deviceName }}</div>
          <div class="fault-column device-sn">{{ fault.deviceSn }}</div>
          <div class="fault-column fault-type">
            {{ fault.type === 'tamper' ? '🛠️ 防拆告警' : 
               fault.type === 'offline' ? '🔌 心跳丢失' : '⚠️ 设备故障' }}
          </div>
          <div class="fault-column fault-location">{{ fault.location || '未分配' }}</div>
          <div class="fault-column fault-time">{{ formatTime(fault.time) }}</div>
          <div class="fault-column fault-status">
            <span :class="fault.status === 'unhandled' ? 'status-unhandled' : 
                     fault.status === 'handling' ? 'status-handling' : 'status-resolved'">
              {{ fault.status === 'unhandled' ? '❌ 未处理' : 
                 fault.status === 'handling' ? '⏳ 处理中' : '✅ 已解决' }}
            </span>
          </div>
          <div class="fault-column fault-actions">
            <button class="view-btn" @click="viewFault(fault)">查看</button>
            <button class="diagnose-btn" @click="remoteDiagnose(fault)">远程诊断</button>
            <button class="handle-btn" @click="handleFault(fault)">
              {{ fault.status === 'unhandled' ? '处理' : '重新处理' }}
            </button>
            <button class="resolve-btn" @click="resolveFault(fault)">解决</button>
          </div>
        </div>
      </div>
      <div class="loading-container" v-else>
        <div class="loading-spinner"></div>
        <div class="loading-text">加载中...</div>
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="totalPages > 1">
        <button class="page-btn" :disabled="currentPage === 1" @click="prevPage">上一页</button>
        <span class="page-info">{{ currentPage }} / {{ totalPages }}</span>
        <button class="page-btn" :disabled="currentPage === totalPages" @click="nextPage">下一页</button>
      </div>
    </section>
    
    <!-- 远程诊断弹窗 -->
    <div class="modal" v-if="showDiagnoseModal">
      <div class="modal-content diagnose-modal">
        <h3 class="modal-title">🔧 设备远程诊断</h3>
        <div class="modal-header-info">
          <div class="device-info">
            <div class="device-name">{{ currentFault.deviceName }}</div>
            <div class="device-sn">SN: {{ currentFault.deviceSn }}</div>
            <div class="device-location">位置: {{ currentFault.location }}</div>
          </div>
        </div>
        
        <div v-if="diagnoseStatus === 'idle'" class="diagnose-idle">
          <div class="diagnose-description">
            远程诊断将检测设备的网络连接、电池状态、传感器数据等信息，帮助您快速定位问题。
          </div>
          <div class="diagnose-actions">
            <button class="start-diagnose-btn" @click="startDiagnose">开始诊断</button>
            <button class="cancel-btn" @click="closeDiagnoseModal">取消</button>
          </div>
        </div>
        
        <div v-else-if="diagnoseStatus === 'diagnosing'" class="diagnose-progress">
          <div class="progress-content">
            <div class="progress-spinner"></div>
            <div class="progress-text">正在进行远程诊断...请稍候</div>
          </div>
          <div class="diagnose-actions">
            <button class="cancel-btn" @click="cancelDiagnose">取消诊断</button>
          </div>
        </div>
        
        <div v-else-if="diagnoseStatus === 'completed'" class="diagnose-result">
          <div class="result-header">
            <h4 class="result-title">诊断结果</h4>
          </div>
          
          <div class="diagnose-metrics">
            <div class="metric-item">
              <span class="metric-label">信号强度</span>
              <span class="metric-value">{{ diagnoseResult.signalStrength }} dBm</span>
            </div>
            <div class="metric-item">
              <span class="metric-label">电池电量</span>
              <span class="metric-value">{{ diagnoseResult.batteryLevel }}%</span>
            </div>
            <div class="metric-item">
              <span class="metric-label">丢包率</span>
              <span class="metric-value">{{ diagnoseResult.packetLoss }}%</span>
            </div>
            <div class="metric-item">
              <span class="metric-label">最后心跳</span>
              <span class="metric-value">{{ diagnoseResult.lastHeartbeat }}</span>
            </div>
          </div>
          
          <div class="diagnostics-list">
            <h5 class="list-title">详细诊断项</h5>
            <div 
              class="diagnostic-item" 
              v-for="(diagnostic, index) in diagnoseResult.diagnostics" 
              :key="index"
              :class="diagnostic.status === '正常' ? 'success' : 'error'"
            >
              <div class="diagnostic-info">
                <span class="diagnostic-item-name">{{ diagnostic.item }}</span>
                <span class="diagnostic-status">{{ diagnostic.status }}</span>
              </div>
              <div class="diagnostic-message">{{ diagnostic.message }}</div>
            </div>
          </div>
          
          <div class="recommendations">
            <h5 class="list-title">修复建议</h5>
            <ul class="recommendation-list">
              <li 
                class="recommendation-item" 
                v-for="(recommendation, index) in diagnoseResult.recommendations" 
                :key="index"
              >
                {{ recommendation }}
              </li>
            </ul>
          </div>
          
          <div class="diagnose-actions">
            <button class="start-diagnose-btn" @click="startDiagnose">重新诊断</button>
            <button class="close-btn" @click="closeDiagnoseModal">关闭</button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 告警处理流程弹窗 -->
    <div class="modal" v-if="showAlertProcessModal">
      <div class="modal-content alert-process-modal">
        <h3 class="modal-title">🚨 告警处理流程</h3>
        
        <div class="alert-info">
          <div class="alert-title">告警信息</div>
          <div class="alert-detail">
            <div class="alert-item">
              <span class="alert-label">设备名称:</span>
              <span class="alert-value">{{ currentAlert.deviceName }}</span>
            </div>
            <div class="alert-item">
              <span class="alert-label">告警类型:</span>
              <span class="alert-value">{{ currentAlert.type === 'tamper' ? '🛠️ 防拆告警' : 
                     currentAlert.type === 'offline' ? '🔌 心跳丢失' : '⚠️ 设备故障' }}</span>
            </div>
            <div class="alert-item">
              <span class="alert-label">告警时间:</span>
              <span class="alert-value">{{ formatTime(currentAlert.time) }}</span>
            </div>
          </div>
        </div>
        
        <div class="process-steps">
          <div class="step-indicator">
            <div 
              class="step" 
              v-for="step in maxProcessSteps" 
              :key="step"
              :class="{ 'active': step <= alertProcessStep, 'completed': step < alertProcessStep }"
            >
              <span class="step-number">{{ step }}</span>
              <span class="step-line" v-if="step < maxProcessSteps"></span>
            </div>
          </div>
          
          <div class="step-content">
            <div v-if="alertProcessStep === 1" class="step-item">
              <h4 class="step-title">1. 确认告警</h4>
              <div class="step-description">
                请确认告警的真实性和紧急程度
              </div>
              <div class="step-form">
                <div class="form-group">
                  <label class="form-label">告警确认</label>
                  <div class="confirmation-options">
                    <label class="option-item">
                      <input type="radio" name="confirmation" value="true">
                      ✅ 确认告警真实
                    </label>
                    <label class="option-item">
                      <input type="radio" name="confirmation" value="false">
                      ❌ 误报，忽略告警
                    </label>
                  </div>
                </div>
                <div class="form-group">
                  <label class="form-label">紧急程度</label>
                  <select class="emergency-level">
                    <option value="low">🟢 低</option>
                    <option value="medium">🟠 中</option>
                    <option value="high">🔴 高</option>
                  </select>
                </div>
              </div>
            </div>
            
            <div v-else-if="alertProcessStep === 2" class="step-item">
              <h4 class="step-title">2. 分析问题</h4>
              <div class="step-description">
                请分析告警产生的原因和可能的解决方案
              </div>
              <div class="step-form">
                <div class="form-group">
                  <label class="form-label">问题分析</label>
                  <textarea 
                    class="analysis-textarea" 
                    placeholder="请输入问题分析..."
                    v-model="alertProcessNotes"
                    rows="4"
                  ></textarea>
                </div>
                <div class="form-group">
                  <label class="form-label">建议解决方案</label>
                  <textarea 
                    class="solution-textarea" 
                    placeholder="请输入建议解决方案..."
                    rows="3"
                  ></textarea>
                </div>
              </div>
            </div>
            
            <div v-else-if="alertProcessStep === 3" class="step-item">
              <h4 class="step-title">3. 处理结果</h4>
              <div class="step-description">
                请记录告警处理的结果和后续跟进事项
              </div>
              <div class="step-form">
                <div class="form-group">
                  <label class="form-label">处理结果</label>
                  <div class="result-options">
                    <label class="option-item">
                      <input type="radio" name="result" value="resolved">
                      ✅ 已解决
                    </label>
                    <label class="option-item">
                      <input type="radio" name="result" value="handling">
                      ⏳ 处理中
                    </label>
                    <label class="option-item">
                      <input type="radio" name="result" value="unresolved">
                      ❌ 未解决
                    </label>
                  </div>
                </div>
                <div class="form-group">
                  <label class="form-label">备注信息</label>
                  <textarea 
                    class="notes-textarea" 
                    placeholder="请输入备注信息..."
                    rows="3"
                  ></textarea>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="process-actions">
          <button 
            class="prev-step-btn" 
            @click="prevProcessStep"
            :disabled="alertProcessStep === 1"
          >
            上一步
          </button>
          <button 
            class="next-step-btn" 
            @click="alertProcessStep < maxProcessSteps ? nextProcessStep : completeAlertProcess"
          >
            {{ alertProcessStep < maxProcessSteps ? '下一步' : '完成处理' }}
          </button>
          <button class="cancel-btn" @click="closeAlertProcessModal">取消</button>
        </div>
      </div>
    </div>
  </div>
  </OperatorLayout>
</template>

<script>
import OperatorLayout from '../../components/OperatorLayout.vue'
import AssetProtectionModal from '../../components/operator/AssetProtectionModal.vue'
import { alarmApi, diagnoseApi } from '../../api/alarm'

export default {
  name: 'DeviceFaultMonitoringView',
  components: {
    OperatorLayout,
    AssetProtectionModal
  },
  data() {
    return {
      // 故障列表数据
      faults: [],
      // 加载状态
      loadingFaults: false,
      // 搜索关键词
      searchKeyword: '',
      // 过滤后的故障列表
      filteredFaults: [],
      // 分页信息
      currentPage: 1,
      pageSize: 5,
      totalPages: 1,
      // 故障过滤器
      faultFilter: {
        type: 'all'
      },
      // 远程诊断相关
      showDiagnoseModal: false,
      currentFault: null,
      diagnoseStatus: 'idle', // idle, diagnosing, completed
      diagnoseResult: null,
      diagnoseId: null,
      // 告警处理流程相关
      showAlertProcessModal: false,
      currentAlert: null,
      alertProcessNotes: '',
      alertProcessStep: 1,
      maxProcessSteps: 3,

      // 资产保全相关
      showAssetProtectionModal: false,
      currentAssetProtectionFault: null
    }
  },
  mounted() {
    this.loadFaults()
  },
  methods: {
    // 加载故障列表
    async loadFaults() {
      try {
        this.loadingFaults = true
        const params = {
          page: this.currentPage,
          pageSize: this.pageSize
        }
        
        if (this.faultFilter.type !== 'all') {
          params.type = this.faultFilter.type
        }
        
        const response = await alarmApi.getOperatorAlarmList(params)
        
        if (response && response.data) {
          this.faults = response.data.list || []
          this.totalPages = Math.ceil((response.data.total || 0) / this.pageSize)
          this.searchFaults()
        }
      } catch (error) {
        console.error('加载故障列表失败:', error)
        this.faults = []
        this.filteredFaults = []
      } finally {
        this.loadingFaults = false
      }
    },

    // 格式化时间
    formatTime(time) {
      if (!time) return ''
      const date = new Date(time)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    },

    // 搜索故障
    searchFaults() {
      let filtered = [...this.faults]
      
      // 根据关键词搜索
      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase()
        filtered = filtered.filter(fault => 
          fault.deviceName.toLowerCase().includes(keyword) || 
          fault.deviceSn.toLowerCase().includes(keyword) || 
          (fault.location && fault.location.toLowerCase().includes(keyword))
        )
      }
      
      this.updateFilteredFaults(filtered)
    },
    
    // 更新过滤后的故障列表
    updateFilteredFaults(filtered) {
      this.filteredFaults = filtered
    },
    
    // 上一页
    prevPage() {
      if (this.currentPage > 1) {
        this.currentPage--
        this.loadFaults()
      }
    },
    
    // 下一页
    nextPage() {
      if (this.currentPage < this.totalPages) {
        this.currentPage++
        this.loadFaults()
      }
    },
    
    // 查看故障
    async viewFault(fault) {
      try {
        const response = await alarmApi.getAlarmDetail(fault.id)
        if (response && response.data) {
          const detail = response.data
          alert(`查看故障：${detail.deviceName}\n故障类型：${detail.type === 'tamper' ? '防拆告警' : 
                     detail.type === 'offline' ? '心跳丢失' : '设备故障'}\n\n${detail.description || ''}`)
        }
      } catch (error) {
        console.error('获取故障详情失败:', error)
        alert(`查看故障：${fault.deviceName}\n故障类型：${fault.type === 'tamper' ? '防拆告警' : 
                   fault.type === 'offline' ? '心跳丢失' : '设备故障'}\n\n${fault.description || ''}`)
      }
    },
    
    // 处理故障
    handleFault(fault) {
      if (fault.type === 'tamper' && fault.status === 'unhandled') {
        this.currentAssetProtectionFault = fault
        this.showAssetProtectionModal = true
        return
      }
      this.openAlertProcessModal(fault)
    },
    
    // 解决故障
    async resolveFault(fault) {
      try {
        await alarmApi.handleOperatorAlarm(fault.id, {
          remark: '故障已解决',
          step: 3,
          processNotes: '故障已标记为解决'
        })
        
        fault.status = 'resolved'
        alert(`故障${fault.deviceName}已标记为解决`)
      } catch (error) {
        console.error('解决故障失败:', error)
        alert('解决故障失败，请重试')
      }
    },
    
    // 远程诊断相关方法
    remoteDiagnose(fault) {
      this.currentFault = fault
      this.diagnoseStatus = 'idle'
      this.diagnoseResult = null
      this.showDiagnoseModal = true
    },
    
    async startDiagnose() {
      try {
        this.diagnoseStatus = 'diagnosing'
        
        const response = await diagnoseApi.startDiagnose(this.currentFault.deviceId)
        
        if (response && response.data) {
          this.diagnoseId = response.data.diagnoseId
          
          await this.pollDiagnoseResult()
        }
      } catch (error) {
        console.error('启动远程诊断失败:', error)
        this.diagnoseStatus = 'idle'
        alert('启动远程诊断失败，请重试')
      }
    },

    async pollDiagnoseResult() {
      const maxAttempts = 30
      let attempts = 0
      
      const poll = async () => {
        try {
          attempts++
          const response = await diagnoseApi.getDiagnoseResult(this.diagnoseId)
          
          if (response && response.data) {
            const result = response.data
            
            if (result.status === 'completed') {
              this.diagnoseResult = {
                deviceSn: result.deviceSn,
                deviceName: result.deviceName,
                signalStrength: result.signalStrength,
                batteryLevel: result.batteryLevel,
                packetLoss: result.packetLoss,
                lastHeartbeat: result.lastHeartbeat,
                diagnostics: result.diagnostics || [],
                recommendations: result.recommendations || []
              }
              this.diagnoseStatus = 'completed'
            } else if (result.status === 'failed') {
              this.diagnoseStatus = 'idle'
              alert('远程诊断失败，请重试')
            } else if (attempts < maxAttempts) {
              setTimeout(poll, 2000)
            } else {
              this.diagnoseStatus = 'idle'
              alert('远程诊断超时，请重试')
            }
          }
        } catch (error) {
          console.error('获取诊断结果失败:', error)
          if (attempts < maxAttempts) {
            setTimeout(poll, 2000)
          } else {
            this.diagnoseStatus = 'idle'
            alert('获取诊断结果失败，请重试')
          }
        }
      }
      
      poll()
    },
    
    cancelDiagnose() {
      this.diagnoseStatus = 'idle'
      this.diagnoseResult = null
      this.diagnoseId = null
    },
    
    closeDiagnoseModal() {
      this.showDiagnoseModal = false
      this.currentFault = null
      this.diagnoseStatus = 'idle'
      this.diagnoseResult = null
      this.diagnoseId = null
    },
    
    // 告警处理流程相关方法
    openAlertProcessModal(fault) {
      this.currentAlert = fault
      this.alertProcessNotes = ''
      this.alertProcessStep = 1
      this.showAlertProcessModal = true
    },
    
    nextProcessStep() {
      if (this.alertProcessStep < this.maxProcessSteps) {
        this.alertProcessStep++
      }
    },
    
    prevProcessStep() {
      if (this.alertProcessStep > 1) {
        this.alertProcessStep--
      }
    },
    
    async completeAlertProcess() {
      try {
        await alarmApi.handleOperatorAlarm(this.currentAlert.id, {
          remark: this.alertProcessNotes,
          step: this.alertProcessStep,
          processNotes: this.alertProcessNotes
        })
        
        this.currentAlert.status = 'handling'
        this.showAlertProcessModal = false
        this.currentAlert = null
        this.alertProcessNotes = ''
        this.alertProcessStep = 1
        
        alert('告警处理流程已完成，故障已标记为处理中')
        this.loadFaults()
      } catch (error) {
        console.error('完成告警处理失败:', error)
        alert('完成告警处理失败，请重试')
      }
    },
    
    closeAlertProcessModal() {
      this.showAlertProcessModal = false
      this.currentAlert = null
      this.alertProcessNotes = ''
      this.alertProcessStep = 1
    }
  },
  computed: {
    // 故障统计信息
    todayFaults() {
      return this.faults.length
    },
    unhandledFaults() {
      return this.faults.filter(fault => fault.status === 'unhandled').length
    },
    handledFaults() {
      return this.faults.filter(fault => fault.status === 'handling').length
    },
    resolvedFaults() {
      return this.faults.filter(fault => fault.status === 'resolved').length
    }
  }
}
</script>

<style scoped>
.device-fault-monitoring-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-actions {
  margin-bottom: 20px;
  display: flex;
  gap: 12px;
}

.filter-select {
  padding: 8px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  background: white;
  font-size: 14px;
  cursor: pointer;
}

.faults-stats-section {
  margin-bottom: 24px;
}

.stats-card {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.stat-item {
  text-align: center;
  padding: 16px;
  border-radius: 8px;
  background: #f8f9fa;
}

.stat-item.unhandled {
  background: #fff3f3;
}

.stat-item.handled {
  background: #fff8f0;
}

.stat-item.resolved {
  background: #f0fff4;
}

.stat-label {
  display: block;
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.stat-value {
  display: block;
  font-size: 28px;
  font-weight: 600;
  color: #333;
}

.faults-list-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.search-bar {
  display: flex;
  gap: 8px;
}

.search-bar input {
  padding: 8px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  width: 280px;
  font-size: 14px;
}

.search-btn {
  padding: 8px 16px;
  background: #4a90e2;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.faults-list {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
}

.faults-header {
  display: grid;
  grid-template-columns: 1.5fr 1fr 1fr 1.5fr 1fr 1fr 1.5fr;
  gap: 12px;
  padding: 12px 16px;
  background: #f8f9fa;
  font-weight: 600;
  font-size: 14px;
  color: #666;
}

.fault-item {
  display: grid;
  grid-template-columns: 1.5fr 1fr 1fr 1.5fr 1fr 1fr 1.5fr;
  gap: 12px;
  padding: 12px 16px;
  border-top: 1px solid #e0e0e0;
  font-size: 14px;
  align-items: center;
}

.fault-item:hover {
  background: #f8f9fa;
}

.fault-item.unhandled {
  background: #fff5f5;
}

.fault-item.handling {
  background: #fffbf0;
}

.fault-item.resolved {
  background: #f0fff4;
}

.fault-column {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-unhandled {
  color: #dc3545;
  font-weight: 500;
}

.status-handling {
  color: #ffc107;
  font-weight: 500;
}

.status-resolved {
  color: #28a745;
  font-weight: 500;
}

.fault-actions {
  display: flex;
  gap: 6px;
}

.fault-actions button {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s;
}

.view-btn {
  background: #e3f2fd;
  color: #1976d2;
}

.diagnose-btn {
  background: #fff3e0;
  color: #f57c00;
}

.handle-btn {
  background: #e8f5e9;
  color: #388e3c;
}

.resolve-btn {
  background: #fce4ec;
  color: #c2185b;
}

.fault-actions button:hover {
  opacity: 0.8;
  transform: translateY(-1px);
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 20px;
}

.page-btn {
  padding: 8px 16px;
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  color: #666;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #4a90e2;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-text {
  margin-top: 16px;
  font-size: 14px;
  color: #666;
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  max-width: 700px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
}

.diagnose-modal {
  padding: 24px;
}

.modal-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0 0 20px 0;
}

.modal-header-info {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.device-info .device-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.device-info .device-sn,
.device-info .device-location {
  font-size: 14px;
  color: #666;
  margin-bottom: 4px;
}

.diagnose-idle,
.diagnose-progress,
.diagnose-result {
  padding: 20px 0;
}

.diagnose-description {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 20px;
}

.diagnose-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.start-diagnose-btn,
.close-btn,
.cancel-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.start-diagnose-btn {
  background: #4a90e2;
  color: white;
}

.close-btn {
  background: #f0f0f0;
  color: #333;
}

.cancel-btn {
  background: #f0f0f0;
  color: #333;
}

.start-diagnose-btn:hover,
.close-btn:hover,
.cancel-btn:hover {
  opacity: 0.8;
}

.progress-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 20px;
}

.progress-spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #4a90e2;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

.progress-text {
  font-size: 16px;
  color: #666;
}

.result-header {
  margin-bottom: 20px;
}

.result-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.diagnose-metrics {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.metric-item {
  background: #f8f9fa;
  padding: 12px;
  border-radius: 6px;
}

.metric-label {
  display: block;
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

.metric-value {
  display: block;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.diagnostics-list,
.recommendations {
  margin-bottom: 20px;
}

.list-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin: 0 0 12px 0;
}

.diagnostic-item {
  background: #f8f9fa;
  padding: 12px;
  border-radius: 6px;
  margin-bottom: 8px;
}

.diagnostic-item.success {
  border-left: 3px solid #28a745;
}

.diagnostic-item.error {
  border-left: 3px solid #dc3545;
}

.diagnostic-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.diagnostic-item-name {
  font-weight: 500;
  color: #333;
}

.diagnostic-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
}

.diagnostic-item.success .diagnostic-status {
  background: #d4edda;
  color: #155724;
}

.diagnostic-item.error .diagnostic-status {
  background: #f8d7da;
  color: #721c24;
}

.diagnostic-message {
  font-size: 12px;
  color: #666;
}

.recommendation-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.recommendation-item {
  padding: 8px 0;
  padding-left: 20px;
  position: relative;
  font-size: 14px;
  color: #666;
}

.recommendation-item::before {
  content: "•";
  position: absolute;
  left: 8px;
  color: #4a90e2;
}

.alert-process-modal {
  padding: 24px;
}

.alert-info {
  background: #fff3f0;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 24px;
}

.alert-title {
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.alert-detail {
  display: grid;
  gap: 8px;
}

.alert-item {
  display: flex;
  gap: 12px;
}

.alert-label {
  font-weight: 500;
  color: #666;
  min-width: 80px;
}

.alert-value {
  color: #333;
}

.process-steps {
  margin-bottom: 24px;
}

.step-indicator {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
}

.step {
  display: flex;
  align-items: center;
  position: relative;
}

.step-number {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #e0e0e0;
  color: #999;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
}

.step.active .step-number {
  background: #4a90e2;
  color: white;
}

.step.completed .step-number {
  background: #28a745;
  color: white;
}

.step-line {
  width: 60px;
  height: 2px;
  background: #e0e0e0;
  margin: 0 8px;
}

.step.completed .step-line {
  background: #28a745;
}

.step-content {
  padding: 0 20px;
}

.step-item {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
}

.step-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px 0;
}

.step-description {
  font-size: 14px;
  color: #666;
  margin-bottom: 16px;
}

.step-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.confirmation-options,
.result-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.emergency-level,
.analysis-textarea,
.solution-textarea,
.notes-textarea {
  padding: 10px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-size: 14px;
}

.emergency-level {
  background: white;
}

.analysis-textarea,
.solution-textarea,
.notes-textarea {
  font-family: inherit;
  resize: vertical;
}

.process-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding-top: 20px;
  border-top: 1px solid #e0e0e0;
}

.prev-step-btn,
.next-step-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.prev-step-btn {
  background: #f0f0f0;
  color: #333;
}

.next-step-btn {
  background: #4a90e2;
  color: white;
}

.prev-step-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.prev-step-btn:hover:not(:disabled),
.next-step-btn:hover {
  opacity: 0.8;
}
</style>
