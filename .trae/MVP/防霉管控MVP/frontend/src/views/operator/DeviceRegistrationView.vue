<template>
  <OperatorLayout pageTitle="📱 设备注册管理" activeNav="devices">
    <div class="device-registration-container">
      <!-- 页面操作栏 -->
      <div class="page-actions">
<<<<<<< HEAD
        <button class="add-device-btn" @click="showAddDeviceModal = true">➕ 注册设备</button>
=======
        <button class="add-device-btn" @click="showAddDeviceModal = true" :disabled="loading">➕ 注册设备</button>
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
      </div>

    <!-- 设备统计信息 -->
    <section class="devices-stats-section">
      <div class="stats-card">
        <div class="stat-item">
          <span class="stat-label">设备总数</span>
          <span class="stat-value">{{ totalDevices }}</span>
        </div>
        <div class="stat-item online">
          <span class="stat-label">在线设备</span>
          <span class="stat-value">{{ onlineDevices }}</span>
        </div>
        <div class="stat-item offline">
          <span class="stat-label">离线设备</span>
          <span class="stat-value">{{ offlineDevices }}</span>
        </div>
        <div class="stat-item warning">
          <span class="stat-label">异常设备</span>
          <span class="stat-value">{{ warningDevices }}</span>
        </div>
      </div>
    </section>

    <!-- 设备列表 -->
    <section class="devices-list-section">
      <div class="section-header">
        <h2 class="section-title">设备列表</h2>
        <div class="search-bar">
          <input type="text" placeholder="搜索设备SN码或名称" v-model="searchKeyword" @input="searchDevices">
          <button class="search-btn">🔍</button>
        </div>
      </div>

<<<<<<< HEAD
      <div class="devices-list">
=======
      <div class="devices-list" v-if="!loading">
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
        <div class="devices-header">
          <div class="device-column device-name">设备名称</div>
          <div class="device-column device-type">类型</div>
          <div class="device-column device-sn">SN码</div>
          <div class="device-column device-status">状态</div>
          <div class="device-column device-activation">激活状态</div>
          <div class="device-column device-location">位置</div>
          <div class="device-column device-actions">操作</div>
        </div>
        <div 
          class="device-item" 
          v-for="device in filteredDevices" 
          :key="device.id"
          :class="device.status"
        >
          <div class="device-column device-name">{{ device.name }}</div>
          <div class="device-column device-type">
            {{ device.type === 'sensor' ? '🌡️ 温湿度传感器' : '🔌 LoRa开关面板' }}
          </div>
          <div class="device-column device-sn">{{ device.sn }}</div>
          <div class="device-column device-status">
            {{ device.status === 'online' ? '🟢 在线' : '🔴 离线' }}
          </div>
          <div class="device-column device-activation">
            {{ device.activated ? '✅ 已激活' : '❌ 未激活' }}
          </div>
          <div class="device-column device-location">{{ device.location || '未分配' }}</div>
          <div class="device-column device-actions">
            <button class="view-btn" @click="viewDevice(device)">查看</button>
            <button class="edit-btn" @click="editDevice(device)">编辑</button>
<<<<<<< HEAD
            <button class="delete-btn" @click="deleteDevice(device)">删除</button>
          </div>
        </div>
=======
            <button class="delete-btn" @click="deleteDevice(device)" :disabled="deleting">删除</button>
          </div>
        </div>
        <div class="empty-state" v-if="filteredDevices.length === 0">
          <p>暂无设备数据</p>
        </div>
      </div>
      <div class="loading-state" v-else>
        <p>加载中...</p>
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="totalPages > 1">
<<<<<<< HEAD
        <button class="page-btn" :disabled="currentPage === 1" @click="prevPage">上一页</button>
        <span class="page-info">{{ currentPage }} / {{ totalPages }}</span>
        <button class="page-btn" :disabled="currentPage === totalPages" @click="nextPage">下一页</button>
=======
        <button class="page-btn" :disabled="currentPage === 1 || loading" @click="prevPage">上一页</button>
        <span class="page-info">{{ currentPage }} / {{ totalPages }}</span>
        <button class="page-btn" :disabled="currentPage === totalPages || loading" @click="nextPage">下一页</button>
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
      </div>
    </section>

    <!-- 添加设备弹窗 -->
    <div class="modal" v-if="showAddDeviceModal">
      <div class="modal-content">
        <h3 class="modal-title">注册新设备</h3>
        <form @submit.prevent="addDevice">
          <div class="form-row">
            <div class="form-group">
              <label for="device-name">设备名称</label>
<<<<<<< HEAD
              <input type="text" id="device-name" v-model="newDevice.name" required>
            </div>
            <div class="form-group">
              <label for="device-type">设备类型</label>
              <select id="device-type" v-model="newDevice.type" required>
=======
              <input type="text" id="device-name" v-model="newDevice.name" required :disabled="submitting">
            </div>
            <div class="form-group">
              <label for="device-type">设备类型</label>
              <select id="device-type" v-model="newDevice.type" required :disabled="submitting">
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
                <option value="sensor">🌡️ 温湿度传感器</option>
                <option value="switch">🔌 LoRa开关面板</option>
              </select>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label for="device-sn">SN码</label>
<<<<<<< HEAD
              <input type="text" id="device-sn" v-model="newDevice.sn" required>
            </div>
            <div class="form-group">
              <label for="device-model">设备型号</label>
              <input type="text" id="device-model" v-model="newDevice.model" required>
=======
              <input type="text" id="device-sn" v-model="newDevice.sn" required :disabled="submitting">
            </div>
            <div class="form-group">
              <label for="device-model">设备型号</label>
              <input type="text" id="device-model" v-model="newDevice.model" required :disabled="submitting">
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label for="device-location">安装位置（可选）</label>
<<<<<<< HEAD
              <input type="text" id="device-location" v-model="newDevice.location">
            </div>
          </div>
          <div class="form-actions">
            <button type="button" class="cancel-btn" @click="showAddDeviceModal = false">取消</button>
            <button type="submit" class="confirm-btn">注册</button>
=======
              <input type="text" id="device-location" v-model="newDevice.location" :disabled="submitting">
            </div>
          </div>
          <div class="form-actions">
            <button type="button" class="cancel-btn" @click="showAddDeviceModal = false" :disabled="submitting">取消</button>
            <button type="submit" class="confirm-btn" :disabled="submitting">
              {{ submitting ? '提交中...' : '注册' }}
            </button>
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
          </div>
        </form>
      </div>
    </div>
  </div>
  </OperatorLayout>
</template>

<script>
import OperatorLayout from '../../components/OperatorLayout.vue'
<<<<<<< HEAD
=======
import { deviceApi } from '../../api/device'
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5

export default {
  name: 'DeviceRegistrationView',
  components: {
    OperatorLayout
  },
  data() {
    return {
<<<<<<< HEAD
      // 设备列表数据
      devices: [
        {
          id: 1,
          name: '温湿度传感器',
          type: 'sensor',
          sn: 'SN123456',
          model: 'SMG-SENSOR-001',
          status: 'online',
          activated: true,
          location: '金南家园三期 3502',
          createTime: '2025-12-01'
        },
        {
          id: 2,
          name: 'LoRa开关面板',
          type: 'switch',
          sn: 'SN789012',
          model: 'SMG-SWITCH-001',
          status: 'online',
          activated: true,
          location: '金南家园三期 3502',
          createTime: '2025-12-01'
        },
        {
          id: 3,
          name: '温湿度传感器',
          type: 'sensor',
          sn: 'SN345678',
          model: 'SMG-SENSOR-001',
          status: 'offline',
          activated: true,
          location: 'XX公寓 1201',
          createTime: '2025-12-02'
        },
        {
          id: 4,
          name: 'LoRa开关面板',
          type: 'switch',
          sn: 'SN901234',
          model: 'SMG-SWITCH-001',
          status: 'online',
          activated: true,
          location: 'XX公寓 1201',
          createTime: '2025-12-02'
        },
        {
          id: 5,
          name: '温湿度传感器',
          type: 'sensor',
          sn: 'SN567890',
          model: 'SMG-SENSOR-001',
          status: 'online',
          activated: false,
          location: '',
          createTime: '2025-12-03'
        },
        {
          id: 6,
          name: 'LoRa开关面板',
          type: 'switch',
          sn: 'SN112233',
          model: 'SMG-SWITCH-001',
          status: 'online',
          activated: false,
          location: '',
          createTime: '2025-12-03'
        }
      ],
      // 搜索关键词
      searchKeyword: '',
      // 过滤后的设备列表
      filteredDevices: [],
      // 分页信息
      currentPage: 1,
      pageSize: 5,
      totalPages: 1,
      // 添加设备弹窗
      showAddDeviceModal: false,
      // 新设备数据
=======
      devices: [],
      searchKeyword: '',
      filteredDevices: [],
      currentPage: 1,
      pageSize: 5,
      totalPages: 1,
      showAddDeviceModal: false,
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
      newDevice: {
        name: '',
        type: 'sensor',
        sn: '',
        model: '',
        location: ''
<<<<<<< HEAD
      }
    }
  },
  mounted() {
    this.searchDevices();
  },
  methods: {
    // 搜索设备
    searchDevices() {
      let filtered = [...this.devices]
      
      // 根据关键词搜索
=======
      },
      loading: false,
      submitting: false,
      deleting: false
    }
  },
  async mounted() {
    await this.loadDevices()
  },
  methods: {
    async loadDevices() {
      try {
        this.loading = true
        const response = await deviceApi.getDeviceList({
          page: this.currentPage,
          size: 100
        })
        if (response && response.data) {
          this.devices = response.data
          this.searchDevices()
        }
      } catch (error) {
        console.error('加载设备列表失败:', error)
        alert('加载设备列表失败，请稍后重试')
      } finally {
        this.loading = false
      }
    },

    searchDevices() {
      let filtered = [...this.devices]
      
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase()
        filtered = filtered.filter(device => 
          device.name.toLowerCase().includes(keyword) || 
          device.sn.toLowerCase().includes(keyword)
        )
      }
      
<<<<<<< HEAD
      // 计算分页
=======
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
      this.totalPages = Math.ceil(filtered.length / this.pageSize)
      this.currentPage = 1
      this.updateFilteredDevices(filtered)
    },
    
<<<<<<< HEAD
    // 更新过滤后的设备列表
=======
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
    updateFilteredDevices(filtered) {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      this.filteredDevices = filtered.slice(start, end)
    },
    
<<<<<<< HEAD
    // 上一页
=======
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
    prevPage() {
      if (this.currentPage > 1) {
        this.currentPage--
        this.searchDevices()
      }
    },
    
<<<<<<< HEAD
    // 下一页
=======
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
    nextPage() {
      if (this.currentPage < this.totalPages) {
        this.currentPage++
        this.searchDevices()
      }
    },
    
<<<<<<< HEAD
    // 查看设备
=======
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
    viewDevice(device) {
      alert(`查看设备：${device.name}`)
    },
    
<<<<<<< HEAD
    // 编辑设备
=======
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
    editDevice(device) {
      alert(`编辑设备：${device.name}`)
    },
    
<<<<<<< HEAD
    // 删除设备
    deleteDevice(device) {
      if (confirm(`确定要删除设备 ${device.name} 吗？`)) {
        const index = this.devices.findIndex(d => d.id === device.id)
        if (index !== -1) {
          this.devices.splice(index, 1)
          this.searchDevices()
=======
    async deleteDevice(device) {
      if (confirm(`确定要删除设备 ${device.name} 吗？`)) {
        try {
          this.deleting = true
          await deviceApi.deleteDevice(device.id)
          alert('设备删除成功！')
          await this.loadDevices()
        } catch (error) {
          console.error('删除设备失败:', error)
          alert('删除设备失败，请稍后重试')
        } finally {
          this.deleting = false
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
        }
      }
    },
    
<<<<<<< HEAD
    // 添加设备
    addDevice() {
      // 模拟添加设备
      const newDevice = {
        id: Date.now(),
        ...this.newDevice,
        status: 'offline',
        activated: false,
        createTime: new Date().toISOString().split('T')[0]
      }
      this.devices.push(newDevice)
      this.showAddDeviceModal = false
      this.newDevice = {
        name: '',
        type: 'sensor',
        sn: '',
        model: '',
        location: ''
      }
      this.searchDevices()
      alert('设备注册成功！')
    }
  },
  computed: {
    // 设备统计信息
=======
    async addDevice() {
      try {
        this.submitting = true
        const deviceData = {
          name: this.newDevice.name,
          type: this.newDevice.type,
          sn: this.newDevice.sn,
          model: this.newDevice.model,
          location: this.newDevice.location
        }
        await deviceApi.addDevice(deviceData)
        this.showAddDeviceModal = false
        this.newDevice = {
          name: '',
          type: 'sensor',
          sn: '',
          model: '',
          location: ''
        }
        alert('设备注册成功！')
        await this.loadDevices()
      } catch (error) {
        console.error('设备注册失败:', error)
        alert('设备注册失败，请稍后重试')
      } finally {
        this.submitting = false
      }
    }
  },
  computed: {
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
    totalDevices() {
      return this.devices.length
    },
    onlineDevices() {
      return this.devices.filter(device => device.status === 'online').length
    },
    offlineDevices() {
      return this.devices.filter(device => device.status === 'offline').length
    },
    warningDevices() {
      return this.devices.filter(device => device.status === 'warning').length
    }
  }
}
</script>

<style scoped>
.device-registration-container {
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
}

.add-device-btn {
  background-color: #1abc9c;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

<<<<<<< HEAD
.add-device-btn:hover {
  background-color: #16a085;
}

=======
.add-device-btn:hover:not(:disabled) {
  background-color: #16a085;
}

.add-device-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
.devices-stats-section {
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
  font-size: 28px;
  font-weight: 600;
  color: #333;
}

.stat-item.online .stat-value {
  color: #27ae60;
}

.stat-item.offline .stat-value {
  color: #e74c3c;
}

.stat-item.warning .stat-value {
  color: #f39c12;
}

.devices-list-section {
  padding: 0 24px 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #333;
}

.search-bar {
  display: flex;
  gap: 8px;
}

.search-bar input {
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

.devices-list {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.devices-header {
  display: flex;
  background-color: #f8f9fa;
  padding: 12px 16px;
  font-weight: 600;
  border-bottom: 1px solid #e0e0e0;
}

.device-item {
  display: flex;
  padding: 16px;
  border-bottom: 1px solid #e0e0e0;
  transition: background-color 0.2s;
}

.device-item:hover {
  background-color: #f8f9fa;
}

.device-item.online {
  background-color: #f0fff4;
}

.device-item.offline {
  background-color: #fff5f5;
}

.device-column {
  flex: 1;
}

.device-name {
  flex: 1.5;
  font-weight: 500;
}

.device-sn, .device-activation, .device-location, .device-type {
  flex: 1;
}

.device-status, .device-actions {
  flex: 1;
}

.device-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.view-btn, .edit-btn, .delete-btn {
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

.edit-btn {
  background-color: #f39c12;
  color: white;
}

.edit-btn:hover {
  background-color: #e67e22;
}

.delete-btn {
  background-color: #e74c3c;
  color: white;
}

<<<<<<< HEAD
.delete-btn:hover {
  background-color: #c0392b;
}

=======
.delete-btn:hover:not(:disabled) {
  background-color: #c0392b;
}

.delete-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.empty-state, .loading-state {
  padding: 40px;
  text-align: center;
  color: #999;
}

>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 24px;
}

.page-btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background-color: #fff;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.page-btn:hover:not(:disabled) {
  background-color: #f0f0f0;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-size: 16px;
  color: #666;
}

<<<<<<< HEAD
/* 弹窗样式 */
=======
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
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
  background-color: white;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  width: 90%;
  max-width: 600px;
}

.modal-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 20px;
  text-align: center;
}

.form-row {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.form-group {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-weight: 500;
  color: #666;
}

.form-group input,
.form-group select {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
}

<<<<<<< HEAD
=======
.form-group input:disabled,
.form-group select:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}

.cancel-btn, .confirm-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.cancel-btn {
  background-color: #f0f0f0;
  color: #333;
}

<<<<<<< HEAD
.cancel-btn:hover {
=======
.cancel-btn:hover:not(:disabled) {
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
  background-color: #e0e0e0;
}

.confirm-btn {
  background-color: #27ae60;
  color: white;
}

<<<<<<< HEAD
.confirm-btn:hover {
  background-color: #229954;
}

/* PC端优化样式 */
/* 增强卡片悬浮效果 */
=======
.confirm-btn:hover:not(:disabled) {
  background-color: #229954;
}

.cancel-btn:disabled,
.confirm-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
.stats-card:hover {
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

<<<<<<< HEAD
/* 增强设备项悬浮效果 */
=======
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
.device-item:hover {
  transform: translateX(4px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
  background-color: #f0f4f8;
}

<<<<<<< HEAD
/* 增强按钮交互效果 */
=======
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
.view-btn,
.edit-btn,
.delete-btn {
  transition: all 0.3s ease;
}

.view-btn:hover,
.edit-btn:hover,
.delete-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
}

<<<<<<< HEAD
/* 增强添加设备按钮 */
=======
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
.add-device-btn {
  transition: all 0.3s ease;
}

<<<<<<< HEAD
.add-device-btn:hover {
=======
.add-device-btn:hover:not(:disabled) {
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
}

<<<<<<< HEAD
/* 优化表单元素 */
=======
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
.search-bar input:focus,
.form-group input:focus,
.form-group select:focus {
  outline: 2px solid #1abc9c;
  border-color: #1abc9c;
  transition: all 0.2s ease;
}

<<<<<<< HEAD
/* 增强统计卡片视觉效果 */
=======
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
.stat-item:hover .stat-value {
  transform: scale(1.05);
  transition: all 0.3s ease;
}

.stat-item .stat-value {
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.1);
}

<<<<<<< HEAD
/* 增强模态框效果 */
=======
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
.modal-content {
  animation: modalSlideIn 0.3s ease;
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

<<<<<<< HEAD
/* 增强分页按钮交互 */
=======
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
.page-btn {
  transition: all 0.3s ease;
}

.page-btn:hover:not(:disabled) {
  background-color: #3498db;
  color: white;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

<<<<<<< HEAD
/* 增强删除按钮警告效果 */
.delete-btn:hover {
=======
.delete-btn:hover:not(:disabled) {
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
  background-color: #c0392b;
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(231, 76, 60, 0.4);
}

<<<<<<< HEAD
/* 增强按钮交互 */
=======
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
.cancel-btn,
.confirm-btn {
  transition: all 0.3s ease;
}

<<<<<<< HEAD
.cancel-btn:hover,
.confirm-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
}



</style>
=======
.cancel-btn:hover:not(:disabled),
.confirm-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
}
</style>
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
