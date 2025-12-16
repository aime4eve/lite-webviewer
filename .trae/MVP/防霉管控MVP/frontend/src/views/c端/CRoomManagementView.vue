<template>
  <div class="room-management-container">
    <!-- 顶部标题栏 -->
    <header class="header">
      <h1 class="title">🏠 房间管理</h1>
      <button class="add-btn" @click="showAddRoomModal = true">
        ➕ 添加房间
      </button>
    </header>

    <!-- 房屋选择 -->
    <section class="house-select-section">
      <h2 class="section-title">选择房屋</h2>
      <select class="house-select" v-model="selectedHouse" @change="loadRooms">
        <option value="">-- 请选择房屋 --</option>
        <option v-for="house in houses" :key="house.id" :value="house">
          {{ house.name }} ({{ house.location }})
        </option>
      </select>
    </section>

    <!-- 房间列表 -->
    <section class="room-list-section" v-if="selectedHouse">
      <h2 class="section-title">{{ selectedHouse.name }}的房间</h2>
      <div class="room-list">
        <div class="room-item" v-for="room in currentRooms" :key="room.id">
          <div class="room-info">
            <div class="room-name">{{ room.name }}</div>
            <div class="room-function">{{ room.function }}</div>
          </div>
          <div class="room-actions">
            <button class="edit-btn" @click="editRoom(room)">✏️</button>
            <button class="delete-btn" @click="deleteRoom(room)">🗑️</button>
          </div>
        </div>
        <div class="empty-state" v-if="currentRooms.length === 0">
          暂无房间，点击右上角添加按钮开始添加房间
        </div>
      </div>
    </section>

    <!-- 添加房间弹窗 -->
    <div class="modal" v-if="showAddRoomModal">
      <div class="modal-content">
        <h3 class="modal-title">添加房间</h3>
        <form @submit.prevent="addRoom">
          <div class="form-group">
            <label for="room-name">房间名称</label>
            <input type="text" id="room-name" v-model="newRoom.name" required placeholder="如：主卧、客厅">
          </div>
          <div class="form-group">
            <label for="room-function">功能区域</label>
            <select id="room-function" v-model="newRoom.function" required>
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
            <button type="button" class="cancel-btn" @click="showAddRoomModal = false">取消</button>
            <button type="submit" class="confirm-btn">确定</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 底部导航栏 -->
    <FooterNavigation active="profile" />
  </div>
</template>

<script>
import { defineComponent, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import FooterNavigation from '../../components/FooterNavigation.vue'

export default defineComponent({
  name: 'CRoomManagementView',
  components: {
    FooterNavigation
  },
  setup() {
    const router = useRouter()
    
    // 模拟用户房屋数据
    const houses = ref([
      {
        id: 1,
        name: '302室',
        location: '金南家园 1号楼 1单元 3楼',
        rooms: []
      }
    ])
    
    const selectedHouse = ref('')
    const currentRooms = ref([])
    const showAddRoomModal = ref(false)
    
    const newRoom = ref({
      name: '',
      function: '卧室'
    })
    
    // 加载房屋列表
    onMounted(() => {
      // 实际项目中这里会从API获取用户房屋数据
      if (houses.value.length > 0) {
        selectedHouse.value = houses.value[0]
        loadRooms()
      }
    })
    
    // 加载选中房屋的房间
    const loadRooms = () => {
      if (selectedHouse.value) {
        currentRooms.value = selectedHouse.value.rooms || []
      }
    }
    
    // 添加房间
    const addRoom = () => {
      if (!selectedHouse.value) {
        alert('请先选择房屋')
        return
      }
      
      const newRoomData = {
        id: Date.now(),
        name: newRoom.value.name,
        function: newRoom.value.function
      }
      
      selectedHouse.value.rooms = selectedHouse.value.rooms || []
      selectedHouse.value.rooms.push(newRoomData)
      loadRooms()
      
      // 重置表单并关闭弹窗
      showAddRoomModal.value = false
      newRoom.value = {
        name: '',
        function: '卧室'
      }
    }
    
    // 编辑房间
    const editRoom = (room) => {
      alert('编辑房间功能开发中...')
    }
    
    // 删除房间
    const deleteRoom = (room) => {
      if (confirm(`确定要删除${room.name}吗？`)) {
        const index = selectedHouse.value.rooms.findIndex(r => r.id === room.id)
        if (index !== -1) {
          selectedHouse.value.rooms.splice(index, 1)
          loadRooms()
        }
      }
    }
    
    return {
      houses,
      selectedHouse,
      currentRooms,
      showAddRoomModal,
      newRoom,
      loadRooms,
      addRoom,
      editRoom,
      deleteRoom
    }
  }
})
</script>

<style scoped>
.room-management-container {
  max-width: 480px;
  margin: 0 auto;
  background-color: #f5f5f5;
  min-height: 100vh;
  padding-bottom: 60px; /* 为底部导航栏留出空间 */
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.title {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}

.add-btn {
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.add-btn:hover {
  background-color: #45a049;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #333;
}

.house-select-section, .room-list-section {
  background-color: #fff;
  padding: 16px;
  margin: 12px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.house-select {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 16px;
  background-color: #fff;
  cursor: pointer;
}

.room-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.room-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background-color: #f9f9f9;
  border-radius: 8px;
  transition: background-color 0.2s;
}

.room-item:hover {
  background-color: #f0f0f0;
}

.room-info {
  flex: 1;
}

.room-name {
  font-weight: 500;
  font-size: 16px;
  margin-bottom: 4px;
}

.room-function {
  font-size: 14px;
  color: #666;
  background-color: #e3f2fd;
  padding: 2px 8px;
  border-radius: 12px;
  display: inline-block;
}

.room-actions {
  display: flex;
  gap: 8px;
}

.edit-btn, .delete-btn {
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

.delete-btn:hover {
  background-color: #ffebee;
}

.empty-state {
  text-align: center;
  padding: 32px;
  color: #999;
  font-size: 16px;
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
  z-index: 1000;
}

.modal-content {
  background-color: #fff;
  padding: 24px;
  border-radius: 8px;
  width: 90%;
  max-width: 400px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
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
  color: #333;
}

.form-group input, .form-group select {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 16px;
  background-color: #fff;
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
  border-radius: 6px;
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
</style>