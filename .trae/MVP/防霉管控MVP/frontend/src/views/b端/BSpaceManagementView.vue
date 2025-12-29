<template>
  <div class="space-management-container">
    <!-- 顶部标题栏 -->
    <header class="header">
      <h1 class="title">🏢 空间管理</h1>
      <button class="add-btn" @click="showAddSpaceModal = true">
        ➕ 添加空间
      </button>
    </header>

    <!-- 空间层级导航 -->
    <nav class="space-nav">
      <div class="nav-item" v-for="(level, index) in currentPath" :key="index" @click="navigateToLevel(index)">
        {{ level.name }}
        <span v-if="index < currentPath.length - 1" class="nav-separator">></span>
      </div>
    </nav>

    <!-- 空间列表 -->
    <section class="space-list-section">
      <h2 class="section-title">{{ currentLevelName }}列表</h2>
      <div class="space-list">
        <div class="space-item" v-for="space in currentSpaces" :key="space.id" @click="enterSpace(space)">
          <div class="space-info">
            <div class="space-name">{{ space.name }}</div>
            <div class="space-type">
              {{ space.type }}
              <span v-if="space.function" class="space-function">({{ space.function }})</span>
            </div>
          </div>
          <div class="space-actions">
            <span v-if="space.children && space.children.length > 0" class="children-count">{{ space.children.length }}个子空间</span>
            <button class="edit-btn" @click.stop="editSpace(space)">✏️</button>
            <button class="delete-btn" @click.stop="deleteSpace(space)">🗑️</button>
          </div>
        </div>
      </div>
    </section>

    <!-- 设备关联列表 -->
    <section class="device-association-section" v-if="currentSpaceId">
      <h2 class="section-title">📱 关联设备</h2>
      <div class="device-list">
        <div class="device-item" v-for="device in associatedDevices" :key="device.id">
          <div class="device-info">
            <div class="device-name">{{ device.name }}</div>
            <div class="device-sn">SN: {{ device.sn }}</div>
            <div class="device-status" :class="device.status">
              {{ device.status === 'online' ? '🟢 在线' : '🔴 离线' }}
            </div>
          </div>
          <button class="remove-btn" @click="removeDeviceAssociation(device.id)">移除</button>
        </div>
      </div>
      <button class="add-device-btn" @click="showAddDeviceModal = true">
        ➕ 添加关联设备
      </button>
    </section>

    <!-- 添加空间弹窗 -->
    <div class="modal" v-if="showAddSpaceModal">
      <div class="modal-content">
        <h3 class="modal-title">添加{{ currentLevelName }}</h3>
        <form @submit.prevent="addSpace">
          <div class="form-group">
            <label for="space-name">名称</label>
            <input type="text" id="space-name" v-model="newSpace.name" required>
          </div>
          <div class="form-group">
            <label for="space-type">类型</label>
            <select id="space-type" v-model="newSpace.type" required>
              <option v-for="option in availableSpaceTypes" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </div>
          
          <!-- 房间功能区域选择 -->
          <div class="form-group" v-if="newSpace.type === '房间'">
            <label for="room-function">功能区域</label>
            <select id="room-function" v-model="newSpace.function" required>
              <option value="客厅">客厅</option>
              <option value="餐厅">餐厅</option>
              <option value="厨房">厨房</option>
              <option value="卫生间">卫生间</option>
              <option value="储物间">储物间</option>
              <option value="卧室">卧室</option>
              <option value="书房">书房</option>
              <option value="阳台">阳台</option>
            </select>
          </div>
          <div class="form-actions">
            <button type="button" class="cancel-btn" @click="showAddSpaceModal = false">取消</button>
            <button type="submit" class="confirm-btn">确定</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 添加关联设备弹窗 -->
    <div class="modal" v-if="showAddDeviceModal">
      <div class="modal-content">
        <h3 class="modal-title">添加关联设备</h3>
        <div class="device-select-list">
          <div class="select-item" v-for="device in availableDevices" :key="device.id">
            <input type="checkbox" :id="'device-' + device.id" v-model="selectedDevices" :value="device">
            <label :for="'device-' + device.id">
              <div class="device-info">
                <div class="device-name">{{ device.name }}</div>
                <div class="device-sn">SN: {{ device.sn }}</div>
              </div>
            </label>
          </div>
        </div>
        <div class="form-actions">
          <button type="button" class="cancel-btn" @click="showAddDeviceModal = false">取消</button>
          <button type="button" class="confirm-btn" @click="associateDevices">确定</button>
        </div>
      </div>
    </div>

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
import { controlApi } from '@/api/control'
import { deviceApi } from '@/api/device'

>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
export default {
  name: 'BSpaceManagementView',
  data() {
    return {
      // 底部导航激活状态
      activeNav: 'spaces',
      // 空间数据结构：小区 > 楼栋 > 单元 > 楼层 > 房屋 > 房间
      spaces: [
        {
          id: 1,
          name: '金南家园',
          type: '小区',
          parentId: null,
          children: [
            {
              id: 2,
              name: '1号楼',
              type: '楼栋',
              parentId: 1,
              children: [
                {
                  id: 3,
                  name: '1单元',
                  type: '单元',
                  parentId: 2,
                  children: [
                    {
                      id: 4,
                      name: '3楼',
                      type: '楼层',
                      parentId: 3,
                      children: [
                        {
                          id: 5,
                          name: '302室',
                          type: '房屋',
                          parentId: 4,
                          children: [
                            {
                              id: 6,
                              name: '主卧浴室',
                              type: '房间',
                              function: '卫生间',
                              parentId: 5,
                              children: [],
                              devices: [1, 2]
                            },
                            {
                              id: 7,
                              name: '次卧浴室',
                              type: '房间',
                              function: '卫生间',
                              parentId: 5,
                              children: [],
                              devices: [3]
                            }
                          ],
                          devices: []
                        }
                      ],
                      devices: []
                    }
                  ],
                  devices: []
                }
              ],
              devices: []
            }
          ],
          devices: []
        }
      ],
      currentPath: [{ name: '所有空间', id: null, type: 'root' }],
      currentSpaceId: null,
      currentSpaces: [],
      currentLevelName: '空间',
      showAddSpaceModal: false,
      showAddDeviceModal: false,
      newSpace: {
        name: '',
        type: '',
        function: ''
      },
      selectedDevices: [],
      // 模拟设备数据
      devices: [
        { id: 1, name: '温湿度传感器', sn: 'SN123456', status: 'online' },
        { id: 2, name: 'LoRa开关面板', sn: 'SN789012', status: 'online' },
        { id: 3, name: '温湿度传感器', sn: 'SN345678', status: 'offline' },
        { id: 4, name: 'LoRa开关面板', sn: 'SN901234', status: 'online' }
      ],
      associatedDevices: [],
<<<<<<< HEAD
      availableDevices: []
=======
      availableDevices: [],
      loading: false,
      loadingDevices: false
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
    }
  },
  computed: {
    // 根据当前空间类型动态计算可用的下一级空间类型
    availableSpaceTypes() {
      const currentSpace = this.currentSpaceId ? this.findSpaceById(this.spaces, this.currentSpaceId) : null;
      const currentType = currentSpace ? currentSpace.type : 'root';
      
      // 严格按照层级顺序规则：小区-->楼栋-->单元（可选）-->楼层-->房屋-->房间
      const typeRules = {
        'root': [
          { value: '小区', label: '小区' }
        ],
        '小区': [
          { value: '楼栋', label: '楼栋' }
        ],
        '楼栋': [
          { value: '单元', label: '单元' },
          { value: '楼层', label: '楼层（跳过单元）' }
        ],
        '单元': [
          { value: '楼层', label: '楼层' }
        ],
        '楼层': [
          { value: '房屋', label: '房屋' }
        ],
        '房屋': [
          { value: '房间', label: '房间' }
        ]
      };
      
      return typeRules[currentType] || [];
    }
  },
  mounted() {
    this.loadSpaces();
  },
  methods: {
<<<<<<< HEAD
    loadSpaces() {
      // 加载当前层级的空间列表
      if (this.currentSpaceId === null) {
        this.currentSpaces = this.spaces;
        this.currentLevelName = '小区';
      } else {
        // 查找当前空间
        const currentSpace = this.findSpaceById(this.spaces, this.currentSpaceId);
        if (currentSpace) {
          this.currentSpaces = currentSpace.children;
          // 根据当前空间类型确定下一级类型名称
          const levelNames = {
            '小区': '楼栋',
            '楼栋': '单元',
            '单元': '楼层',
            '楼层': '房屋',
            '房屋': '房间',
            '房间': '房间'
          };
          this.currentLevelName = levelNames[currentSpace.type] || '房间';
        }
      }
      this.loadAssociatedDevices();
=======
    async loadSpaces() {
      try {
        this.loading = true
        
        const params = {
          parentId: this.currentSpaceId
        }
        
        const response = await controlApi.getSpaceList(params)
        
        if (response && response.data) {
          this.currentSpaces = response.data.list || []
          
          if (this.currentSpaceId === null) {
            this.currentLevelName = '小区'
          } else {
            const currentSpace = this.findSpaceById(this.spaces, this.currentSpaceId)
            if (currentSpace) {
              const levelNames = {
                '小区': '楼栋',
                '楼栋': '单元',
                '单元': '楼层',
                '楼层': '房屋',
                '房屋': '房间',
                '房间': '房间'
              }
              this.currentLevelName = levelNames[currentSpace.type] || '房间'
            }
          }
        }
      } catch (error) {
        console.error('加载空间列表失败:', error)
        this.currentSpaces = []
      } finally {
        this.loading = false
      }
      
      this.loadAssociatedDevices()
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
    },
    findSpaceById(spaces, id) {
      for (const space of spaces) {
        if (space.id === id) {
          return space;
        }
        if (space.children && space.children.length > 0) {
          const found = this.findSpaceById(space.children, id);
          if (found) {
            return found;
          }
        }
      }
      return null;
    },
    enterSpace(space) {
      // 进入子空间
      this.currentPath.push({
        name: space.name,
        id: space.id,
        type: space.type
      });
      this.currentSpaceId = space.id;
      this.loadSpaces();
    },
    navigateToLevel(levelIndex) {
      // 导航到指定层级
      this.currentPath = this.currentPath.slice(0, levelIndex + 1);
      this.currentSpaceId = this.currentPath[this.currentPath.length - 1].id;
      this.loadSpaces();
    },
<<<<<<< HEAD
    addSpace() {
      // 添加新空间
      const newSpaceId = Date.now();
      const newSpace = {
        id: newSpaceId,
        name: this.newSpace.name,
        type: this.newSpace.type,
        parentId: this.currentSpaceId,
        children: [],
        devices: []
      };
      
      // 如果是房间，添加功能属性
      if (this.newSpace.type === '房间') {
        newSpace.function = this.newSpace.function;
      }

      if (this.currentSpaceId === null) {
        // 添加顶级空间（小区）
        this.spaces.push(newSpace);
      } else {
        // 添加子空间
        const parentSpace = this.findSpaceById(this.spaces, this.currentSpaceId);
        if (parentSpace) {
          parentSpace.children.push(newSpace);
        }
      }

      this.showAddSpaceModal = false;
      // 重置表单
      this.newSpace = {
        name: '',
        type: '',
        function: ''
      };
      this.loadSpaces();
=======
    async addSpace() {
      try {
        const spaceData = {
          name: this.newSpace.name,
          type: this.newSpace.type,
          parentId: this.currentSpaceId
        }
        
        if (this.newSpace.type === '房间') {
          spaceData.function = this.newSpace.function
        }
        
        await controlApi.createSpace(spaceData)
        
        this.showAddSpaceModal = false
        this.newSpace = {
          name: '',
          type: '',
          function: ''
        }
        
        alert('空间添加成功')
        await this.loadSpaces()
      } catch (error) {
        console.error('添加空间失败:', error)
        alert('添加空间失败，请重试')
      }
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
    },
    editSpace(space) {
      // 编辑空间
      alert('编辑空间功能开发中...');
    },
<<<<<<< HEAD
    deleteSpace(space) {
      // 删除空间
      if (confirm(`确定要删除${space.name}吗？`)) {
        if (space.parentId === null) {
          // 删除顶级空间
          const index = this.spaces.findIndex(s => s.id === space.id);
          if (index !== -1) {
            this.spaces.splice(index, 1);
          }
        } else {
          // 删除子空间
          const parentSpace = this.findSpaceById(this.spaces, space.parentId);
          if (parentSpace) {
            const index = parentSpace.children.findIndex(s => s.id === space.id);
            if (index !== -1) {
              parentSpace.children.splice(index, 1);
            }
          }
        }
        this.loadSpaces();
      }
    },
    loadAssociatedDevices() {
      // 加载关联设备
      if (!this.currentSpaceId) {
        this.associatedDevices = [];
        this.availableDevices = [];
        return;
      }

      const currentSpace = this.findSpaceById(this.spaces, this.currentSpaceId);
      if (currentSpace) {
        // 获取已关联设备ID列表
        const associatedDeviceIds = currentSpace.devices;
        // 过滤出已关联的设备
        this.associatedDevices = this.devices.filter(device => 
          associatedDeviceIds.includes(device.id)
        );
        // 过滤出可用的未关联设备
        this.availableDevices = this.devices.filter(device => 
          !associatedDeviceIds.includes(device.id)
        );
      }
    },
    associateDevices() {
      // 关联设备
      if (this.selectedDevices.length === 0) return;

      const currentSpace = this.findSpaceById(this.spaces, this.currentSpaceId);
      if (currentSpace) {
        // 添加新关联的设备ID
        this.selectedDevices.forEach(device => {
          if (!currentSpace.devices.includes(device.id)) {
            currentSpace.devices.push(device.id);
          }
        });
        this.loadAssociatedDevices();
        this.showAddDeviceModal = false;
        this.selectedDevices = [];
      }
    },
    removeDeviceAssociation(deviceId) {
      // 移除设备关联
      const currentSpace = this.findSpaceById(this.spaces, this.currentSpaceId);
      if (currentSpace) {
        const index = currentSpace.devices.indexOf(deviceId);
        if (index !== -1) {
          currentSpace.devices.splice(index, 1);
          this.loadAssociatedDevices();
        }
=======
    async deleteSpace(space) {
      if (confirm(`确定要删除${space.name}吗？`)) {
        try {
          await controlApi.deleteSpace(space.id)
          alert('空间删除成功')
          await this.loadSpaces()
        } catch (error) {
          console.error('删除空间失败:', error)
          alert('删除空间失败，请重试')
        }
      }
    },
    async loadAssociatedDevices() {
      if (!this.currentSpaceId) {
        this.associatedDevices = []
        this.availableDevices = []
        return
      }

      try {
        this.loadingDevices = true
        
        const response = await controlApi.getSpaceList({
          parentId: this.currentSpaceId,
          includeDevices: true
        })
        
        if (response && response.data) {
          const currentSpace = response.data.list.find(s => s.id === this.currentSpaceId)
          if (currentSpace && currentSpace.devices) {
            this.associatedDevices = currentSpace.devices
          } else {
            this.associatedDevices = []
          }
        }
        
        const deviceResponse = await deviceApi.getDeviceList({
          page: 1,
          pageSize: 100
        })
        
        if (deviceResponse && deviceResponse.data) {
          const allDevices = deviceResponse.data.list || []
          const associatedDeviceIds = this.associatedDevices.map(d => d.id)
          this.availableDevices = allDevices.filter(device => 
            !associatedDeviceIds.includes(device.id)
          )
        }
      } catch (error) {
        console.error('加载关联设备失败:', error)
        this.associatedDevices = []
        this.availableDevices = []
      } finally {
        this.loadingDevices = false
      }
    },
    async associateDevices() {
      if (this.selectedDevices.length === 0) return

      try {
        for (const device of this.selectedDevices) {
          await controlApi.associateDevice(this.currentSpaceId, device.id)
        }
        
        this.showAddDeviceModal = false
        this.selectedDevices = []
        alert('设备关联成功')
        await this.loadAssociatedDevices()
      } catch (error) {
        console.error('关联设备失败:', error)
        alert('关联设备失败，请重试')
      }
    },
    async removeDeviceAssociation(deviceId) {
      try {
        await controlApi.removeDeviceAssociation(this.currentSpaceId, deviceId)
        alert('设备关联移除成功')
        await this.loadAssociatedDevices()
      } catch (error) {
        console.error('移除设备关联失败:', error)
        alert('移除设备关联失败，请重试')
>>>>>>> 0140bada383e79ae44a5bc79b580238e3ac5caa5
      }
    },
    // 导航方法
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
    }
  }
}
</script>

<style scoped>
.space-management-container {
  max-width: 1200px;
  margin: 0 auto;
  background-color: #f5f5f5;
  min-height: 100vh;
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #fff;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.title {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
}

.add-btn {
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.add-btn:hover {
  background-color: #45a049;
}

.space-nav {
  display: flex;
  gap: 8px;
  background-color: #fff;
  padding: 12px 16px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.nav-item {
  cursor: pointer;
  font-weight: 500;
  color: #2196F3;
}

.nav-item:hover {
  text-decoration: underline;
}

.nav-separator {
  color: #666;
  margin: 0 4px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #333;
}

.space-list-section, .device-association-section {
  background-color: #fff;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.space-list, .device-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.space-item, .device-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background-color: #f9f9f9;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.space-item:hover {
  background-color: #f0f0f0;
}

.space-info, .device-info {
  flex: 1;
}

.space-name, .device-name {
  font-weight: 600;
  font-size: 16px;
  margin-bottom: 4px;
}

.space-type, .device-sn, .device-status {
  font-size: 14px;
  color: #666;
}

.device-status.online {
  color: #4CAF50;
}

.device-status.offline {
  color: #F44336;
}

.space-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.children-count {
  font-size: 14px;
  color: #666;
  margin-right: 12px;
}

.edit-btn, .delete-btn, .remove-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 16px;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.edit-btn:hover {
  background-color: #e3f2fd;
}

.delete-btn:hover, .remove-btn:hover {
  background-color: #ffebee;
}

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
  background-color: #fff;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  width: 90%;
  max-width: 500px;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 20px;
  text-align: center;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  font-weight: 500;
  margin-bottom: 8px;
}

.form-group input, .form-group select {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
}

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

.device-select-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 400px;
  overflow-y: auto;
}

.select-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background-color: #f9f9f9;
  border-radius: 6px;
}

.select-item input[type="checkbox"] {
  width: 18px;
  height: 18px;
  cursor: pointer;
}

.add-device-btn {
  background-color: #2196F3;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
  margin-top: 16px;
}

.add-device-btn:hover {
  background-color: #1976D2;
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

.space-management-container {
  padding-bottom: 80px; /* 为底部导航栏留出空间 */
}
</style>