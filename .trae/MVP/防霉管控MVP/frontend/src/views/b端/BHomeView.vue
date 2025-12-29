<template>
  <div class="b-home-container">
    <!-- 顶部标题栏 -->
    <header class="header">
      <h1 class="title">🏢 防霉守护 · 商户版</h1>
      <div class="header-actions">
        <button class="batch-btn" @click="showBatchActions = true">🔢 批量操作</button>
        <button class="add-device-btn" @click="navigateToAddDevice">➕ 添加设备</button>
      </div>
    </header>

    <!-- 批量操作弹窗 -->
    <div class="modal" v-if="showBatchActions">
      <div class="modal-content">
        <h3 class="modal-title">批量操作</h3>
        <div class="batch-action-list">
<<<<<<< HEAD
          <div class="batch-action-item" @click="batchAssignCleaning">
            🧹 批量指派保洁
          </div>
=======
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
          <div class="batch-action-item" @click="batchMarkAsHandled">
            ✅ 标记为已处理
          </div>
          <div class="batch-action-item" @click="exportRiskReport">
            📥 导出风险报告
          </div>
        </div>
        <button class="close-btn" @click="showBatchActions = false">关闭</button>
      </div>
    </div>
<<<<<<< HEAD
    
    <!-- 批量指派保洁弹窗 -->
    <div class="modal" v-if="showAssignModal">
      <div class="modal-content">
        <h3 class="modal-title">指派保洁</h3>
        <div class="modal-info">
          即将为 {{ selectedRooms.length }} 个房间指派保洁
        </div>
        <div class="assign-content">
          <div class="form-group">
            <label class="form-label">选择保洁人员</label>
            <div class="staff-list">
              <div 
                class="staff-item" 
                v-for="staff in cleaningStaff" 
                :key="staff.id"
                :class="{ 'selected': selectedStaffId === staff.id, 'unavailable': !staff.available }"
                @click="staff.available && (selectedStaffId = staff.id)"
              >
                <div class="staff-info">
                  <div class="staff-name">{{ staff.name }}</div>
                  <div class="staff-phone">{{ staff.phone }}</div>
                </div>
                <div class="staff-status">
                  {{ staff.available ? '🟢 可用' : '🔴 不可用' }}
                </div>
              </div>
            </div>
          </div>
          
          <div class="form-group">
            <label class="form-label">备注信息（可选）</label>
            <textarea 
              v-model="assignNote" 
              class="note-input" 
              placeholder="请输入备注信息"
            ></textarea>
          </div>
        </div>
        <div class="modal-actions">
          <button class="cancel-btn" @click="showAssignModal = false">取消</button>
          <button class="confirm-btn" @click="submitAssignCleaning">确认指派</button>
        </div>
      </div>
    </div>
    
    <!-- 保洁任务反馈弹窗 -->
    <div class="modal" v-if="showFeedbackModal">
      <div class="modal-content">
        <h3 class="modal-title">保洁任务反馈</h3>
        <div class="feedback-content">
          <div class="form-group">
            <label class="form-label">反馈内容</label>
            <textarea 
              v-model="feedbackContent" 
              class="feedback-input" 
              placeholder="请输入保洁任务反馈内容"
              rows="4"
            ></textarea>
          </div>
          
          <div class="form-group">
            <label class="form-label">上传照片（可选）</label>
            <div class="image-upload">
              <div class="upload-btn">
                + 添加照片
              </div>
              <div class="image-preview" v-for="(image, index) in feedbackImages" :key="index">
                <div class="preview-item">
                  <img :src="image" alt="反馈照片">
                  <button class="remove-image" @click="feedbackImages.splice(index, 1)">×</button>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-actions">
          <button class="cancel-btn" @click="showFeedbackModal = false">取消</button>
          <button class="confirm-btn" @click="submitFeedback">提交反馈</button>
        </div>
      </div>
    </div>
=======
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5

    <!-- 风险概览 -->
    <section class="overview-section">
      <h2 class="section-title">📊 风险概览</h2>
      <div class="overview-cards">
        <div class="overview-card high-risk">
          <div class="card-title">🔴 高危房间</div>
          <div class="card-value">{{ highRiskCount }}</div>
        </div>
        <div class="overview-card medium-risk">
          <div class="card-title">🟠 中危房间</div>
          <div class="card-value">{{ mediumRiskCount }}</div>
        </div>
        <div class="overview-card low-risk">
          <div class="card-title">🟢 低危房间</div>
          <div class="card-value">{{ lowRiskCount }}</div>
        </div>
        <div class="overview-card total-rooms">
          <div class="card-title">🏠 总房间数</div>
          <div class="card-value">{{ totalRooms }}</div>
        </div>
      </div>
    </section>

    <!-- 今日高风险房间 -->
    <section class="high-risk-section">
      <div class="section-header">
        <h2 class="section-title">🚨 今日高风险房间</h2>
        <div class="header-actions">
          <label class="select-all-label">
            <input 
              type="checkbox" 
              v-model="selectAll" 
              @change="toggleSelectAll"
            >
            全选
          </label>
          <button class="view-all-btn" @click="navigateToRiskRooms">查看全部 ></button>
        </div>
      </div>
      <div class="risk-rooms-list">
        <div class="risk-room-item" v-for="room in highRiskRooms" :key="room.id">
          <div class="room-select">
            <input 
              type="checkbox" 
              v-model="room.isSelected" 
              @change="handleRoomSelect"
            >
          </div>
          <div class="room-info">
            <div class="room-name">{{ room.name }}</div>
            <div class="room-location">{{ room.location }}</div>
          </div>
          <div class="risk-details">
            <div class="risk-index" :class="room.riskLevel">
              {{ room.riskLevel === 'high' ? '🔴 高危' : room.riskLevel === 'medium' ? '🟠 中危' : '🟢 低危' }}
              ({{ room.riskValue }}%)
            </div>
            <div class="env-data">
              💧 {{ room.humidity }}% | 🌡️ {{ room.temperature }}°C
            </div>
          </div>
          <div class="room-actions">
            <button class="detail-btn" @click="navigateToRoomDetail(room.id)">详情</button>
<<<<<<< HEAD
            <button class="assign-btn" @click="assignCleaning(room.id)">指派保洁</button>
=======
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
          </div>
        </div>
      </div>
    </section>

    <!-- 设备概览 -->
    <section class="devices-section">
      <div class="section-header">
        <h2 class="section-title">📱 设备概览</h2>
        <button class="view-all-btn" @click="navigateToDevices">查看全部 ></button>
      </div>
      <div class="devices-overview">
        <div class="device-stat">
          <div class="stat-label">设备总数</div>
          <div class="stat-value">{{ totalDevices }}</div>
        </div>
        <div class="device-stat online">
          <div class="stat-label">在线设备</div>
          <div class="stat-value">{{ onlineDevices }}</div>
        </div>
        <div class="device-stat offline">
          <div class="stat-label">离线设备</div>
          <div class="stat-value">{{ offlineDevices }}</div>
        </div>
      </div>
      <div class="device-list">
        <div class="device-item" v-for="device in recentDevices" :key="device.id">
          <div class="device-info">
            <div class="device-name">{{ device.name }}</div>
            <div class="device-sn">SN: {{ device.sn }}</div>
            <div class="device-location">位置: {{ device.location }}</div>
          </div>
          <div class="device-status" :class="device.status">
            {{ device.status === 'online' ? '🟢 在线' : '🔴 离线' }}
          </div>
        </div>
      </div>
    </section>

    <!-- 防霉战报 -->
    <section class="report-section">
      <h2 class="section-title">📊 本月防霉战报</h2>
      <div class="report-content">
        <div class="report-item">
          <span class="report-icon">🛡️</span>
          <div class="report-detail">
            <div class="report-label">阻断霉变</div>
            <div class="report-value">{{ moldBlocked }} 次</div>
          </div>
        </div>
        <div class="report-item">
          <span class="report-icon">💰</span>
          <div class="report-detail">
            <div class="report-label">节省成本</div>
            <div class="report-value">¥{{ costSaved }}</div>
          </div>
        </div>
        <div class="report-item">
          <span class="report-icon">📊</span>
          <div class="report-detail">
            <div class="report-label">风险下降</div>
            <div class="report-value">{{ riskReduced }}%</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 底部导航栏 -->
    <nav class="footer-nav">
      <!-- 门户按钮 -->
      <div 
        class="nav-item" 
        @click="navigateToPortal"
      >
        <span class="nav-icon">🚪</span>
        <span class="nav-text">门户</span>
      </div>
      <div 
        class="nav-item" 
        :class="{ active: activeNav === 'home' }" 
        @click="navigateToHome"
      >
        <span class="nav-icon">🏠</span>
        <span class="nav-text">首页</span>
      </div>
      <div 
        class="nav-item" 
        :class="{ active: activeNav === 'devices' }" 
        @click="navigateToDevices"
      >
        <span class="nav-icon">📱</span>
        <span class="nav-text">设备</span>
      </div>
      <div 
        class="nav-item" 
        :class="{ active: activeNav === 'spaces' }" 
        @click="navigateToSpaces"
      >
        <span class="nav-icon">🏢</span>
        <span class="nav-text">空间</span>
      </div>
      <div 
        class="nav-item" 
        :class="{ active: activeNav === 'profile' }" 
        @click="navigateToProfile"
      >
        <span class="nav-icon">👤</span>
        <span class="nav-text">我的</span>
      </div>
    </nav>
  </div>
</template>

<script>
<<<<<<< HEAD
=======
import { controlApi } from "@/api/control"
import { deviceApi } from "@/api/device"

>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
export default {
  name: 'BHomeView',
  data() {
    return {
      // 风险概览数据
      highRiskCount: 2,
      mediumRiskCount: 5,
      lowRiskCount: 18,
      totalRooms: 25,
      
      // 高风险房间列表
      highRiskRooms: [
        {
          id: 1,
          name: '302室主卧浴室',
          location: '金南家园1号楼1单元',
          riskLevel: 'high',
          riskValue: 86,
          humidity: 88,
          temperature: 22,
          isSelected: false
        },
        {
          id: 2,
          name: '505室次卧浴室',
          location: '金南家园1号楼2单元',
          riskLevel: 'high',
          riskValue: 78,
          humidity: 82,
          temperature: 21,
          isSelected: false
        },
        {
          id: 3,
          name: '608室浴室',
          location: '金南家园2号楼1单元',
          riskLevel: 'medium',
          riskValue: 71,
          humidity: 75,
          temperature: 23,
          isSelected: false
        }
      ],
      
      // 设备概览数据
      totalDevices: 35,
      onlineDevices: 32,
      offlineDevices: 3,
      
      // 最近设备列表
      recentDevices: [
        {
          id: 1,
          name: '温湿度传感器',
          sn: 'SN123456',
          location: '302室主卧浴室',
          status: 'online'
        },
        {
          id: 2,
          name: 'LoRa开关面板',
          sn: 'SN789012',
          location: '302室主卧浴室',
          status: 'online'
        },
        {
          id: 3,
          name: '温湿度传感器',
          sn: 'SN345678',
          location: '505室次卧浴室',
          status: 'offline'
        },
        {
          id: 4,
          name: 'LoRa开关面板',
          sn: 'SN901234',
          location: '505室次卧浴室',
          status: 'online'
        }
      ],
<<<<<<< HEAD
      
      // 防霉战报数据
=======
// 防霉战报数据
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
      moldBlocked: 24,
      costSaved: 360,
      riskReduced: 45,
      
      // 底部导航激活状态
      activeNav: 'home',
      
      // 批量操作弹窗
      showBatchActions: false,
      
      // 批量选择状态
      selectAll: false,
      selectedRooms: [],
      
      // 保洁人员列表
      cleaningStaff: [
        { id: 1, name: '张阿姨', phone: '13800138001', available: true },
        { id: 2, name: '李阿姨', phone: '13800138002', available: true },
        { id: 3, name: '王阿姨', phone: '13800138003', available: false }
      ],
      
      // 批量指派保洁弹窗
      showAssignModal: false,
      selectedStaffId: null,
      assignNote: '',
      
      // 保洁任务反馈弹窗
      showFeedbackModal: false,
      currentTask: null,
      feedbackContent: '',
      feedbackImages: []
    }
  },
<<<<<<< HEAD
  methods: {
    // 导航方法
=======
  mounted() {
    this.loadData()
  },
  methods: {
  async loadData() {
      try {
        await Promise.all([
          this.loadRiskOverview(),
          this.loadHighRiskRooms(),
          this.loadDeviceOverview()
        ])
      } catch (error) {
        console.error("加载数据失败:", error)
      }
    },

    async loadRiskOverview() {
      try {
        const response = await controlApi.getRiskOverview()
        if (response && response.data) {
          this.highRiskCount = response.data.highRiskCount || 0
          this.mediumRiskCount = response.data.mediumRiskCount || 0
          this.lowRiskCount = response.data.lowRiskCount || 0
          this.totalRooms = response.data.totalRooms || 0
        }
      } catch (error) {
        console.error("加载风险概览失败:", error)
      }
    },

    async loadHighRiskRooms() {
      try {
        const response = await controlApi.getHighRiskRooms({
          page: 1,
          pageSize: 10
        })
        if (response && response.data && response.data.list) {
          this.highRiskRooms = response.data.list.map(room => ({
            ...room,
            isSelected: false
          }))
        }
      } catch (error) {
        console.error("加载高风险房间失败:", error)
      }
    },

    async loadDeviceOverview() {
      try {
        const response = await deviceApi.getDeviceList({
          page: 1,
          pageSize: 10
        })
        if (response && response.data) {
          this.totalDevices = response.data.total || 0
          this.onlineDevices = response.data.onlineCount || 0
          this.offlineDevices = response.data.offlineCount || 0
          this.recentDevices = (response.data.list || []).map(device => ({
            id: device.id,
            name: device.name,
            sn: device.sn || "N/A",
            location: device.location || "未知位置",
            status: device.status || "offline"
          }))
        }
      } catch (error) {
        console.error("加载设备概览失败:", error)
      }
    },

        // 导航方法
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
    navigateToPortal() {
      this.$router.push('/portal')
    },
    navigateToHome() {
      this.$router.push('/b/')
      this.activeNav = 'home'
    },
    navigateToDevices() {
      this.$router.push('/b/devices')
      this.activeNav = 'devices'
    },
    navigateToSpaces() {
      this.$router.push('/b/space-management')
      this.activeNav = 'spaces'
    },
    navigateToProfile() {
      this.$router.push('/b/profile')
      this.activeNav = 'profile'
    },
    navigateToAddDevice() {
      this.$router.push('/b/add-device')
    },
    navigateToRoomDetail(roomId) {
      this.$router.push(`/b/device-detail/${roomId}`)
    },
    navigateToRiskRooms() {
      // 跳转到风险房间清单
      alert('风险房间清单功能开发中...')
    },
    
    // 批量选择功能
    toggleSelectAll() {
      this.highRiskRooms.forEach(room => {
        room.isSelected = this.selectAll
      })
      this.updateSelectedRooms()
    },
    
    handleRoomSelect() {
      this.selectAll = this.highRiskRooms.every(room => room.isSelected)
      this.updateSelectedRooms()
    },
    
    updateSelectedRooms() {
      this.selectedRooms = this.highRiskRooms.filter(room => room.isSelected)
    },
    
    // 批量操作方法
<<<<<<< HEAD
    batchAssignCleaning() {
      this.showBatchActions = false
      if (this.selectedRooms.length === 0) {
        alert('请先选择需要指派保洁的房间')
        return
      }
      this.showAssignModal = true
    },
    
=======
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
    batchMarkAsHandled() {
      this.showBatchActions = false
      if (this.selectedRooms.length === 0) {
        alert('请先选择需要标记为已处理的房间')
        return
      }
      // 模拟批量标记为已处理
      console.log('批量标记为已处理:', this.selectedRooms)
      this.selectedRooms.forEach(room => {
        room.riskLevel = 'low'
        room.riskValue = 30
        room.humidity = 50
      })
      this.selectAll = false
      this.selectedRooms = []
      alert('已成功标记所选房间为已处理')
    },
    
    exportRiskReport() {
      this.showBatchActions = false
<<<<<<< HEAD
      alert('导出风险报告功能开发中...')
    },
    
    // 单个房间指派保洁
    assignCleaning(roomId) {
      // 选择该房间
      this.highRiskRooms.forEach(room => {
        room.isSelected = room.id === roomId
      })
      this.selectAll = false
      this.updateSelectedRooms()
      this.showAssignModal = true
    },
    
    // 提交指派保洁
    submitAssignCleaning() {
      if (!this.selectedStaffId) {
        alert('请选择保洁人员')
        return
      }
      
      // 模拟提交指派
      console.log('提交指派保洁:', {
        rooms: this.selectedRooms,
        staffId: this.selectedStaffId,
        note: this.assignNote,
        timestamp: new Date().toISOString()
      })
      
      // 关闭弹窗并重置
      this.showAssignModal = false
      this.selectedStaffId = null
      this.assignNote = ''
      
      // 显示成功提示
      alert(`已成功指派保洁人员处理 ${this.selectedRooms.length} 个房间`)
      
      // 重置选择
      this.selectAll = false
      this.selectedRooms.forEach(room => {
        room.isSelected = false
      })
      this.selectedRooms = []
    },
    
    // 保洁任务反馈
    openFeedbackModal(task) {
      this.currentTask = task
      this.showFeedbackModal = true
    },
    
    submitFeedback() {
      if (!this.feedbackContent.trim()) {
=======
      
      // 1. 准备数据
      const headers = ['ID', '房间名称', '位置', '风险等级', '风险值', '湿度(%)', '温度(°C)'];
      const rows = this.highRiskRooms.map(room => [
        room.id,
        room.name,
        room.location,
        room.riskLevel === 'high' ? '高危' : room.riskLevel === 'medium' ? '中危' : '低危',
        room.riskValue,
        room.humidity,
        room.temperature
      ]);
      
      // 2. 转换为CSV格式
      const csvContent = [
        headers.join(','),
        ...rows.map(row => row.join(','))
      ].join('\n');
      
      // 3. 触发下载
      const blob = new Blob(["\ufeff" + csvContent], { type: 'text/csv;charset=utf-8;' });
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.setAttribute("href", url);
      link.setAttribute("download", `risk_report_${new Date().toISOString().slice(0,10)}.csv`);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      
      alert('风险报告导出成功！');
    },

    submitFeedback() {
      if (!this.feedbackContent) {
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
        alert('请输入反馈内容')
        return
      }
      
<<<<<<< HEAD
      // 模拟提交反馈
      console.log('提交保洁反馈:', {
        task: this.currentTask,
        content: this.feedbackContent,
        images: this.feedbackImages,
        timestamp: new Date().toISOString()
      })
      
      // 关闭弹窗并重置
      this.showFeedbackModal = false
      this.currentTask = null
      this.feedbackContent = ''
      this.feedbackImages = []
      
      // 显示成功提示
      alert('反馈提交成功！')
    }
=======
      console.log('提交反馈:', {
        content: this.feedbackContent,
        images: this.feedbackImages
      })
      
      alert('反馈已提交')
      
      this.showFeedbackModal = false
      this.feedbackContent = ''
      this.feedbackImages = []
  },
  mounted() {
    this.loadData()
  },
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
  }
}
</script>

<style scoped>
.b-home-container {
  max-width: 1200px;
  margin: 0 auto;
  background-color: #f5f5f5;
  min-height: 100vh;
  padding-bottom: 80px; /* 为底部导航栏留出空间 */
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #fff;
  padding: 16px 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.title {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
  color: #333;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.batch-btn, .add-device-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.batch-btn {
  background-color: #2196F3;
  color: white;
}

.batch-btn:hover {
  background-color: #1976D2;
}

.add-device-btn {
  background-color: #4CAF50;
  color: white;
}

.add-device-btn:hover {
  background-color: #45a049;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
  color: #333;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.view-all-btn {
  background: none;
  border: none;
  color: #2196F3;
  font-size: 14px;
  cursor: pointer;
  padding: 0;
  display: flex;
  align-items: center;
  gap: 4px;
}

.view-all-btn:hover {
  text-decoration: underline;
}

.overview-section, .high-risk-section, .devices-section, .report-section {
  background-color: #fff;
  padding: 20px;
  margin: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.overview-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.overview-card {
  background-color: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
  text-align: center;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.overview-card.high-risk {
  border-left: 4px solid #F44336;
}

.overview-card.medium-risk {
  border-left: 4px solid #FFC107;
}

.overview-card.low-risk {
  border-left: 4px solid #4CAF50;
}

.overview-card.total-rooms {
  border-left: 4px solid #2196F3;
}

.card-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.card-value {
  font-size: 32px;
  font-weight: 600;
  color: #333;
}

.overview-card.high-risk .card-value {
  color: #F44336;
}

.overview-card.medium-risk .card-value {
  color: #FFC107;
}

.overview-card.low-risk .card-value {
  color: #4CAF50;
}

.overview-card.total-rooms .card-value {
  color: #2196F3;
}

.risk-rooms-list, .device-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.risk-room-item, .device-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background-color: #f9f9f9;
  border-radius: 8px;
  transition: background-color 0.2s;
}

.risk-room-item:hover, .device-item:hover {
  background-color: #f0f0f0;
}

.room-info, .device-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.room-name, .device-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.room-location, .device-location, .device-sn {
  font-size: 14px;
  color: #666;
}

.risk-details {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  margin: 0 16px;
}

.risk-index {
  font-size: 16px;
  font-weight: 600;
}

.risk-index.high {
  color: #F44336;
}

.risk-index.medium {
  color: #FFC107;
}

.risk-index.low {
  color: #4CAF50;
}

.env-data {
  font-size: 14px;
  color: #666;
}

.detail-btn {
  background-color: #2196F3;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.detail-btn:hover {
  background-color: #1976D2;
}

.devices-overview {
  display: flex;
  gap: 24px;
  margin-bottom: 16px;
}

.device-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.device-stat.online .stat-value {
  color: #4CAF50;
}

.device-stat.offline .stat-value {
  color: #F44336;
}

.device-status {
  font-size: 14px;
  font-weight: 500;
}

.device-status.online {
  color: #4CAF50;
}

.device-status.offline {
  color: #F44336;
}

.report-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.report-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.report-icon {
  font-size: 24px;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #e3f2fd;
  border-radius: 8px;
  color: #2196F3;
}

.report-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.report-label {
  font-size: 14px;
  color: #666;
}

.report-value {
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

/* 底部导航栏 */
.footer-nav {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 480px;
  background-color: #fff;
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 12px 0;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  border-radius: 16px 16px 0 0;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 8px;
}

.nav-item.active {
  color: #4CAF50;
  background-color: #E8F5E9;
}

.nav-icon {
  font-size: 20px;
}

.nav-text {
  font-size: 12px;
  font-weight: 500;
}

/* 弹窗样式 */
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
  z-index: 2000;
}

.modal-content {
  background-color: #fff;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  width: 90%;
  max-width: 400px;
}

.modal-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 20px;
  text-align: center;
}

.batch-action-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 24px;
}

.batch-action-item {
  padding: 16px;
  background-color: #f9f9f9;
  border-radius: 8px;
  cursor: pointer;
  text-align: center;
  font-size: 16px;
  transition: background-color 0.2s;
}

.batch-action-item:hover {
  background-color: #e3f2fd;
  color: #2196F3;
}

.close-btn {
  width: 100%;
  padding: 12px;
  background-color: #f0f0f0;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.close-btn:hover {
  background-color: #e0e0e0;
}

/* 新增样式 */
.select-all-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  cursor: pointer;
  user-select: none;
}

.room-select {
  margin-right: 12px;
  display: flex;
  align-items: center;
}

.room-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.assign-btn {
  background-color: #FF9800;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.assign-btn:hover {
  background-color: #F57C00;
}

/* 指派保洁弹窗样式 */
.modal-info {
  font-size: 14px;
  color: #666;
  margin-bottom: 16px;
  text-align: center;
}

.assign-content, .feedback-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.staff-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.staff-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background-color: #f9f9f9;
  border: 2px solid transparent;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.staff-item:hover:not(.unavailable) {
  background-color: #e3f2fd;
  border-color: #2196F3;
}

.staff-item.selected {
  background-color: #e3f2fd;
  border-color: #2196F3;
}

.staff-item.unavailable {
  opacity: 0.5;
  cursor: not-allowed;
}

.staff-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.staff-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.staff-phone {
  font-size: 14px;
  color: #666;
}

.staff-status {
  font-size: 14px;
  font-weight: 500;
}

.note-input, .feedback-input {
  padding: 12px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  resize: vertical;
  min-height: 80px;
  font-family: inherit;
}

.note-input:focus, .feedback-input:focus {
  outline: none;
  border-color: #2196F3;
  box-shadow: 0 0 0 2px rgba(33, 150, 243, 0.1);
}

.modal-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.cancel-btn, .confirm-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s;
}

.cancel-btn {
  background-color: #f0f0f0;
  color: #666;
}

.cancel-btn:hover {
  background-color: #e0e0e0;
}

.confirm-btn {
  background-color: #4CAF50;
  color: white;
}

.confirm-btn:hover {
  background-color: #45a049;
}

/* 图片上传样式 */
.image-upload {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.upload-btn {
  width: 80px;
  height: 80px;
  border: 2px dashed #e0e0e0;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
}

.upload-btn:hover {
  border-color: #2196F3;
  color: #2196F3;
  background-color: rgba(33, 150, 243, 0.05);
}

.image-preview {
  display: flex;
  gap: 12px;
}

.preview-item {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e0e0e0;
}

.preview-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-image {
  position: absolute;
  top: -8px;
  right: -8px;
  width: 24px;
  height: 24px;
  background-color: #F44336;
  color: white;
  border: none;
  border-radius: 50%;
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s;
}

.remove-image:hover {
  background-color: #D32F2F;
}

@media (max-width: 768px) {
  .b-home-container {
    padding: 0;
  }
  
  .header {
    padding: 12px 16px;
  }
  
  .title {
    font-size: 20px;
  }
  
  .header-actions {
    gap: 8px;
  }
  
  .batch-btn, .add-device-btn, .assign-btn {
    padding: 6px 12px;
    font-size: 12px;
  }
  
  .overview-section, .high-risk-section, .devices-section, .report-section {
    margin: 12px;
    padding: 16px;
  }
  
  .overview-cards, .report-content {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  
  .card-value {
    font-size: 24px;
  }
  
  .risk-room-item, .device-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .risk-details {
    align-items: flex-start;
    width: 100%;
  }
  
  .devices-overview {
    gap: 16px;
  }
  
  .room-actions {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .staff-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .staff-status {
    align-self: flex-end;
  }
  
  .modal-actions {
    flex-direction: column;
  }
  
  .cancel-btn, .confirm-btn {
    width: 100%;
  }
}
</style>