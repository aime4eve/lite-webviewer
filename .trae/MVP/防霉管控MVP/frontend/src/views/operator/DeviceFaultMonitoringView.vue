<template>
  <OperatorLayout pageTitle="🔔 设备故障监控" activeNav="alarms">
    <div class="device-fault-monitoring-container">
      <!-- 页面操作栏 -->
      <div class="page-actions">
<<<<<<< HEAD
        <select class="filter-select" v-model="faultFilter.type">
=======
        <select class="filter-select" v-model="faultFilter.type" @change="loadFaults">
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
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

<<<<<<< HEAD
      <div class="faults-list">
=======
      <div class="faults-list" v-if="!loadingFaults">
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
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
<<<<<<< HEAD
          <div class="fault-column fault-time">{{ fault.time }}</div>
=======
          <div class="fault-column fault-time">{{ formatTime(fault.time) }}</div>
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
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
<<<<<<< HEAD
=======
      <div class="loading-container" v-else>
        <div class="loading-spinner"></div>
        <div class="loading-text">加载中...</div>
      </div>
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5

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
<<<<<<< HEAD
              <span class="alert-value">{{ currentAlert.time }}</span>
=======
              <span class="alert-value">{{ formatTime(currentAlert.time) }}</span>
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
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
<<<<<<< HEAD
=======
import AssetProtectionModal from '../../components/operator/AssetProtectionModal.vue'
import { alarmApi, diagnoseApi } from '../../api/alarm'
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5

export default {
  name: 'DeviceFaultMonitoringView',
  components: {
<<<<<<< HEAD
    OperatorLayout
=======
    OperatorLayout,
    AssetProtectionModal
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
  },
  data() {
    return {
      // 故障列表数据
<<<<<<< HEAD
      faults: [
        {
          id: 1,
          deviceName: '温湿度传感器',
          deviceSn: 'SN123456',
          type: 'tamper',
          location: '金南家园三期 3502',
          time: '2025-12-15 14:30',
          status: 'unhandled',
          description: '设备防拆开关触发，可能被非法拆除'
        },
        {
          id: 2,
          deviceName: 'LoRa开关面板',
          deviceSn: 'SN789012',
          type: 'offline',
          location: 'XX公寓 1201',
          time: '2025-12-15 14:25',
          status: 'handling',
          description: '设备心跳丢失，已超过3次未上报'
        },
        {
          id: 3,
          deviceName: '温湿度传感器',
          deviceSn: 'SN345678',
          type: 'malfunction',
          location: '阳光花园 708',
          time: '2025-12-15 14:20',
          status: 'resolved',
          description: '设备数据异常，湿度值持续超过100%'
        },
        {
          id: 4,
          deviceName: '温湿度传感器',
          deviceSn: 'SN901234',
          type: 'tamper',
          location: '金南家园一期 102',
          time: '2025-12-15 14:15',
          status: 'resolved',
          description: '设备防拆开关触发，已确认用户自行拆装'
        },
        {
          id: 5,
          deviceName: 'LoRa开关面板',
          deviceSn: 'SN567890',
          type: 'offline',
          location: 'XX小区 503',
          time: '2025-12-15 14:10',
          status: 'unhandled',
          description: '设备心跳丢失，可能是网络问题'
        },
        {
          id: 6,
          deviceName: '温湿度传感器',
          deviceSn: 'SN112233',
          type: 'malfunction',
          location: '蓝天小区 305',
          time: '2025-12-15 14:05',
          status: 'handling',
          description: '设备温度值异常，持续显示-20°C'
        }
      ],
=======
      faults: [],
      // 加载状态
      loadingFaults: false,
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
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
<<<<<<< HEAD
=======
      diagnoseId: null,
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
      // 告警处理流程相关
      showAlertProcessModal: false,
      currentAlert: null,
      alertProcessNotes: '',
      alertProcessStep: 1,
<<<<<<< HEAD
      maxProcessSteps: 3
    }
  },
  mounted() {
    this.searchFaults();
  },
  methods: {
=======
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

>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
    // 搜索故障
    searchFaults() {
      let filtered = [...this.faults]
      
<<<<<<< HEAD
      // 根据类型过滤
      if (this.faultFilter.type !== 'all') {
        filtered = filtered.filter(fault => fault.type === this.faultFilter.type)
      }
      
=======
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
      // 根据关键词搜索
      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase()
        filtered = filtered.filter(fault => 
          fault.deviceName.toLowerCase().includes(keyword) || 
          fault.deviceSn.toLowerCase().includes(keyword) || 
          (fault.location && fault.location.toLowerCase().includes(keyword))
        )
      }
      
<<<<<<< HEAD
      // 计算分页
      this.totalPages = Math.ceil(filtered.length / this.pageSize)
      this.currentPage = 1
=======
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
      this.updateFilteredFaults(filtered)
    },
    
    // 更新过滤后的故障列表
    updateFilteredFaults(filtered) {
<<<<<<< HEAD
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      this.filteredFaults = filtered.slice(start, end)
=======
      this.filteredFaults = filtered
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
    },
    
    // 上一页
    prevPage() {
      if (this.currentPage > 1) {
        this.currentPage--
<<<<<<< HEAD
        this.searchFaults()
=======
        this.loadFaults()
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
      }
    },
    
    // 下一页
    nextPage() {
      if (this.currentPage < this.totalPages) {
        this.currentPage++
<<<<<<< HEAD
        this.searchFaults()
=======
        this.loadFaults()
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
      }
    },
    
    // 查看故障
<<<<<<< HEAD
    viewFault(fault) {
      alert(`查看故障：${fault.deviceName}\n故障类型：${fault.type === 'tamper' ? '防拆告警' : 
                 fault.type === 'offline' ? '心跳丢失' : '设备故障'}\n\n${fault.description}`)
=======
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
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
    },
    
    // 处理故障
    handleFault(fault) {
<<<<<<< HEAD
      if (fault.status === 'unhandled') {
        fault.status = 'handling'
        alert(`已开始处理故障：${fault.deviceName}`)
      } else {
        fault.status = 'unhandled'
        alert(`故障${fault.deviceName}已重新标记为未处理`)
      }
    },
    
    // 解决故障
    resolveFault(fault) {
      fault.status = 'resolved'
      alert(`故障${fault.deviceName}已标记为解决`)
=======
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
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
    },
    
    // 远程诊断相关方法
    remoteDiagnose(fault) {
      this.currentFault = fault
      this.diagnoseStatus = 'idle'
      this.diagnoseResult = null
      this.showDiagnoseModal = true
    },
    
<<<<<<< HEAD
    startDiagnose() {
      this.diagnoseStatus = 'diagnosing'
      
      // 模拟远程诊断过程
      setTimeout(() => {
        this.diagnoseResult = {
          deviceSn: this.currentFault.deviceSn,
          deviceName: this.currentFault.deviceName,
          signalStrength: -55, // dBm
          batteryLevel: 78, // %
          packetLoss: 2, // %
          lastHeartbeat: '2025-12-15 14:29:30',
          diagnostics: [
            {
              item: '设备状态',
              status: '异常',
              message: '设备防拆开关触发'
            },
            {
              item: '网络连接',
              status: '正常',
              message: '信号强度良好'
            },
            {
              item: '电池电量',
              status: '正常',
              message: '电量充足'
            },
            {
              item: '传感器数据',
              status: '异常',
              message: '温度传感器数据异常'
            }
          ],
          recommendations: [
            '检查设备安装情况，确认防拆开关是否被触发',
            '重新校准温度传感器',
            '考虑更换设备外壳'
          ]
        }
        this.diagnoseStatus = 'completed'
      }, 2000)
=======
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
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
    },
    
    cancelDiagnose() {
      this.diagnoseStatus = 'idle'
      this.diagnoseResult = null
<<<<<<< HEAD
=======
      this.diagnoseId = null
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
    },
    
    closeDiagnoseModal() {
      this.showDiagnoseModal = false
      this.currentFault = null
      this.diagnoseStatus = 'idle'
      this.diagnoseResult = null
<<<<<<< HEAD
=======
      this.diagnoseId = null
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
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
    
<<<<<<< HEAD
    completeAlertProcess() {
      // 模拟完成告警处理流程
      console.log('告警处理完成:', {
        alert: this.currentAlert,
        notes: this.alertProcessNotes,
        stepsCompleted: this.alertProcessStep,
        timestamp: new Date().toISOString()
      })
      
      // 更新故障状态
      this.currentAlert.status = 'handling'
      
      // 关闭弹窗
      this.showAlertProcessModal = false
      this.currentAlert = null
      this.alertProcessNotes = ''
      this.alertProcessStep = 1
      
      alert('告警处理流程已完成，故障已标记为处理中')
=======
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
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
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
<<<<<<< HEAD
  max-width: 1400px;
  margin: 0 auto;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #2c3e50;
  color: white;
  padding: 16px 24px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.title {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
=======
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-actions {
  margin-bottom: 20px;
  display: flex;
  gap: 12px;
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
}

.filter-select {
  padding: 8px 12px;
<<<<<<< HEAD
  border: none;
  border-radius: 4px;
  font-size: 16px;
  background-color: white;
}

.faults-stats-section {
  padding: 24px;
}

.stats-card {
  display: flex;
  justify-content: space-around;
  align-items: center;
  background-color: white;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.stat-label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.stat-value {
=======
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
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
  font-size: 28px;
  font-weight: 600;
  color: #333;
}

<<<<<<< HEAD
.stat-item.unhandled .stat-value {
  color: #e74c3c;
}

.stat-item.handled .stat-value {
  color: #f39c12;
}

.stat-item.resolved .stat-value {
  color: #27ae60;
}

.faults-list-section {
  padding: 0 24px 24px;
=======
.faults-list-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
<<<<<<< HEAD
  margin-bottom: 16px;
=======
  margin-bottom: 20px;
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
}

.section-title {
  font-size: 20px;
  font-weight: 600;
<<<<<<< HEAD
  margin-bottom: 16px;
  color: #333;
=======
  color: #333;
  margin: 0;
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
}

.search-bar {
  display: flex;
  gap: 8px;
}

.search-bar input {
<<<<<<< HEAD
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
  width: 300px;
}

.search-btn {
  background-color: #3498db;
  color: white;
  border: none;
  padding: 10px 16px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.search-btn:hover {
  background-color: #2980b9;
}

.faults-list {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
=======
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
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
  overflow: hidden;
}

.faults-header {
<<<<<<< HEAD
  display: flex;
  background-color: #f8f9fa;
  padding: 12px 16px;
  font-weight: 600;
  border-bottom: 1px solid #e0e0e0;
}

.fault-item {
  display: flex;
  padding: 16px;
  border-bottom: 1px solid #e0e0e0;
  transition: background-color 0.2s;
}

.fault-item:hover {
  background-color: #f8f9fa;
}

.fault-item.unhandled {
  background-color: #fff5f5;
}

.fault-item.handling {
  background-color: #fff8e1;
}

.fault-item.resolved {
  background-color: #f0fff4;
}

.fault-column {
  flex: 1;
}

.device-name {
  flex: 1.5;
  font-weight: 500;
}

.device-sn, .fault-type, .fault-location, .fault-time, .fault-status {
  flex: 1;
}

.fault-actions {
  flex: 1;
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.view-btn, .handle-btn, .resolve-btn {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.view-btn {
  background-color: #3498db;
  color: white;
}

.view-btn:hover {
  background-color: #2980b9;
}

.handle-btn {
  background-color: #f39c12;
  color: white;
}

.handle-btn:hover {
  background-color: #e67e22;
}

.resolve-btn {
  background-color: #27ae60;
  color: white;
}

.resolve-btn:hover {
  background-color: #229954;
}

.status-unhandled {
  color: #e74c3c;
}

.status-handling {
  color: #f39c12;
}

.status-resolved {
  color: #27ae60;
=======
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
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
<<<<<<< HEAD
  margin-top: 24px;
=======
  margin-top: 20px;
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
}

.page-btn {
  padding: 8px 16px;
<<<<<<< HEAD
  border: 1px solid #ddd;
  background-color: #fff;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.page-btn:hover:not(:disabled) {
  background-color: #f0f0f0;
=======
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
<<<<<<< HEAD
  font-size: 16px;
  color: #666;
}

/* PC端优化样式 */
/* 增强卡片悬浮效果 */
.stats-card:hover {
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

/* 增强故障项悬浮效果 */
.fault-item:hover {
  transform: translateX(4px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
  background-color: #f0f4f8;
}

/* 增强按钮交互效果 */
.view-btn,
.handle-btn,
.resolve-btn {
  transition: all 0.3s ease;
}

.view-btn:hover,
.handle-btn:hover,
.resolve-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
}

/* 增强导航交互 */
.nav-item {
  transition: all 0.3s ease;
}

.nav-item:hover {
  transform: translateY(-1px);
  background-color: #2980b9;
}

/* 优化表单元素 */
.search-bar input:focus,
.filter-select:focus {
  outline: 2px solid #1abc9c;
  border-color: #1abc9c;
  transition: all 0.2s ease;
}

/* 增强统计卡片视觉效果 */
.stat-item:hover .stat-value {
  transform: scale(1.05);
  transition: all 0.3s ease;
}

.stat-item .stat-value {
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.1);
}

/* 增强分页按钮交互 */
.page-btn {
  transition: all 0.3s ease;
}

.page-btn:hover:not(:disabled) {
  background-color: #3498db;
  color: white;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

/* 增强状态切换效果 */
.status-toggle input:checked + label {
  background-color: #27ae60;
  box-shadow: 0 0 0 2px rgba(39, 174, 96, 0.3);
  transition: all 0.3s ease;
}

.status-toggle input + label {
  transition: all 0.3s ease;
}

.status-toggle input:hover + label {
  background-color: #b0b0b0;
}

/* 增强故障状态视觉效果 */
.status-badge {
  transition: all 0.3s ease;
}

.status-badge:hover {
  transform: scale(1.1);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

/* 远程诊断和告警处理弹窗样式 */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 20px;
  color: #333;
  text-align: center;
}

.modal-header-info {
  margin-bottom: 20px;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.device-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.device-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.device-sn, .device-location {
=======
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
  font-size: 14px;
  color: #666;
}

<<<<<<< HEAD
/* 远程诊断样式 */
.diagnose-idle, .diagnose-progress, .diagnose-result {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.diagnose-description {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}

.diagnose-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-top: 16px;
}

.start-diagnose-btn {
  background-color: #3498db;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s;
}

.start-diagnose-btn:hover {
  background-color: #2980b9;
}

.cancel-btn {
  background-color: #f0f0f0;
  color: #666;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s;
}

.cancel-btn:hover {
  background-color: #e0e0e0;
}

/* 诊断进度样式 */
.progress-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.progress-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
=======
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
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

<<<<<<< HEAD
.progress-text {
=======
.loading-text {
  margin-top: 16px;
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
  font-size: 14px;
  color: #666;
}

<<<<<<< HEAD
/* 诊断结果样式 */
.result-header {
  margin-bottom: 16px;
}

.result-title {
  font-size: 16px;
=======
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
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
  font-weight: 600;
  color: #333;
  margin: 0;
}

.diagnose-metrics {
  display: grid;
<<<<<<< HEAD
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.metric-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 12px;
  background-color: #f8f9fa;
=======
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.metric-item {
  background: #f8f9fa;
  padding: 12px;
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
  border-radius: 6px;
}

.metric-label {
<<<<<<< HEAD
  font-size: 12px;
  color: #666;
}

.metric-value {
  font-size: 16px;
=======
  display: block;
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

.metric-value {
  display: block;
  font-size: 18px;
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
  font-weight: 600;
  color: #333;
}

<<<<<<< HEAD
.diagnostics-list {
  margin-bottom: 24px;
=======
.diagnostics-list,
.recommendations {
  margin-bottom: 20px;
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
}

.list-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
<<<<<<< HEAD
  margin-bottom: 12px;
}

.diagnostic-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  background-color: #f8f9fa;
=======
  margin: 0 0 12px 0;
}

.diagnostic-item {
  background: #f8f9fa;
  padding: 12px;
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
  border-radius: 6px;
  margin-bottom: 8px;
}

.diagnostic-item.success {
<<<<<<< HEAD
  border-left: 4px solid #27ae60;
}

.diagnostic-item.error {
  border-left: 4px solid #e74c3c;
=======
  border-left: 3px solid #28a745;
}

.diagnostic-item.error {
  border-left: 3px solid #dc3545;
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
}

.diagnostic-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
<<<<<<< HEAD
}

.diagnostic-item-name {
  font-size: 14px;
=======
  margin-bottom: 4px;
}

.diagnostic-item-name {
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
  font-weight: 500;
  color: #333;
}

.diagnostic-status {
  font-size: 12px;
<<<<<<< HEAD
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 12px;
}

.diagnostic-item.success .diagnostic-status {
  background-color: #e8f5e9;
  color: #27ae60;
}

.diagnostic-item.error .diagnostic-status {
  background-color: #ffebee;
  color: #e74c3c;
}

.diagnostic-message {
  font-size: 13px;
  color: #666;
}

.recommendations {
  margin-bottom: 24px;
}

.recommendation-list {
  list-style-type: disc;
  padding-left: 20px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.recommendation-item {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}

.close-btn {
  background-color: #2ecc71;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s;
}

.close-btn:hover {
  background-color: #27ae60;
}

/* 告警处理流程样式 */
.alert-info {
  margin-bottom: 24px;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.alert-title {
  font-size: 14px;
=======
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
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.alert-detail {
<<<<<<< HEAD
  display: flex;
  flex-direction: column;
=======
  display: grid;
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
  gap: 8px;
}

.alert-item {
  display: flex;
<<<<<<< HEAD
  gap: 8px;
  align-items: center;
}

.alert-label {
  font-size: 14px;
  font-weight: 500;
  color: #666;
  width: 80px;
}

.alert-value {
  font-size: 14px;
=======
  gap: 12px;
}

.alert-label {
  font-weight: 500;
  color: #666;
  min-width: 80px;
}

.alert-value {
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
  color: #333;
}

.process-steps {
  margin-bottom: 24px;
}

.step-indicator {
  display: flex;
  align-items: center;
<<<<<<< HEAD
  justify-content: center;
=======
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
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
<<<<<<< HEAD
  background-color: #e0e0e0;
  color: #666;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  z-index: 1;
=======
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
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
}

.step-line {
  width: 60px;
  height: 2px;
<<<<<<< HEAD
  background-color: #e0e0e0;
  margin: 0 12px;
}

.step.active .step-number {
  background-color: #3498db;
  color: white;
}

.step.completed .step-number {
  background-color: #2ecc71;
  color: white;
}

.step.completed .step-line {
  background-color: #2ecc71;
}

.step-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.step-item {
  display: flex;
  flex-direction: column;
  gap: 12px;
=======
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
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
}

.step-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
<<<<<<< HEAD
  margin: 0;
=======
  margin: 0 0 8px 0;
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
}

.step-description {
  font-size: 14px;
  color: #666;
<<<<<<< HEAD
  line-height: 1.5;
=======
  margin-bottom: 16px;
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
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
<<<<<<< HEAD
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.confirmation-options, .result-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
=======
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.confirmation-options,
.result-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
}

.option-item {
  display: flex;
<<<<<<< HEAD
  gap: 8px;
  align-items: center;
  cursor: pointer;
  font-size: 14px;
  color: #333;
}

.option-item input {
  cursor: pointer;
}

.emergency-level, .analysis-textarea, .solution-textarea, .notes-textarea {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  font-family: inherit;
}

.emergency-level:focus, .analysis-textarea:focus, .solution-textarea:focus, .notes-textarea:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 2px rgba(52, 152, 219, 0.1);
}

.analysis-textarea, .solution-textarea, .notes-textarea {
=======
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
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
  resize: vertical;
}

.process-actions {
  display: flex;
  gap: 12px;
<<<<<<< HEAD
  justify-content: center;
  margin-top: 24px;
}

.prev-step-btn, .next-step-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s;
}

.prev-step-btn {
  background-color: #f0f0f0;
  color: #666;
}

.prev-step-btn:hover:not(:disabled) {
  background-color: #e0e0e0;
=======
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
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
}

.prev-step-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

<<<<<<< HEAD
.next-step-btn {
  background-color: #3498db;
  color: white;
}

.next-step-btn:hover {
  background-color: #2980b9;
}

/* 诊断按钮样式 */
.diagnose-btn {
  background-color: #9b59b6;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.diagnose-btn:hover {
  background-color: #8e44ad;
}

/* 弹窗动画 */
.modal-content {
  animation: modalFadeIn 0.3s ease;
}

@keyframes modalFadeIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .diagnose-metrics {
    grid-template-columns: 1fr;
  }
  
  .diagnose-actions, .process-actions {
    flex-direction: column;
  }
  
  .step-indicator {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .step {
    flex-direction: row;
    align-items: center;
    gap: 12px;
  }
  
  .step-line {
    width: 2px;
    height: 40px;
    margin: 0;
    margin-left: 15px;
    margin-top: -8px;
  }
}

</style>
=======
.prev-step-btn:hover:not(:disabled),
.next-step-btn:hover {
  opacity: 0.8;
}
</style>
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
