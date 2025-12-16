<template>
  <OperatorLayout pageTitle="👥 用户管理" activeNav="users">
    <div class="user-management-container">
      <!-- 页面操作栏 -->
      <div class="page-actions">
        <button class="add-user-btn" @click="showAddUserModal = true">➕ 添加用户</button>
      </div>

    <!-- 用户统计信息 -->
    <section class="users-stats-section">
      <div class="stats-card">
        <div class="stat-item">
          <span class="stat-label">用户总数</span>
          <span class="stat-value">{{ totalUsers }}</span>
        </div>
        <div class="stat-item c-end">
          <span class="stat-label">C端用户</span>
          <span class="stat-value">{{ cEndUsers }}</span>
        </div>
        <div class="stat-item b-end">
          <span class="stat-label">B端用户</span>
          <span class="stat-value">{{ bEndUsers }}</span>
        </div>
        <div class="stat-item active">
          <span class="stat-label">活跃用户</span>
          <span class="stat-value">{{ activeUsers }}</span>
        </div>
      </div>
    </section>

    <!-- 用户列表 -->
    <section class="users-list-section">
      <div class="section-header">
        <h2 class="section-title">用户列表</h2>
        <div class="search-bar">
          <input type="text" placeholder="搜索用户名、ID或手机号" v-model="searchKeyword" @input="searchUsers">
          <button class="search-btn">🔍</button>
        </div>
      </div>

      <div class="users-list">
        <div class="users-header">
          <div class="user-column user-id">用户ID</div>
          <div class="user-column user-name">用户名</div>
          <div class="user-column user-type">用户类型</div>
          <div class="user-column user-phone">手机号</div>
          <div class="user-column user-status">状态</div>
          <div class="user-column user-registration">注册时间</div>
          <div class="user-column user-subscription">订阅状态</div>
          <div class="user-column user-actions">操作</div>
        </div>
        <div 
          class="user-item" 
          v-for="user in filteredUsers" 
          :key="user.id"
          :class="user.status"
        >
          <div class="user-column user-id">{{ user.id }}</div>
          <div class="user-column user-name">{{ user.name }}</div>
          <div class="user-column user-type">
            {{ user.type === 'c' ? '🏠 C端' : '🏢 B端' }}
          </div>
          <div class="user-column user-phone">{{ user.phone }}</div>
          <div class="user-column user-status">
            {{ user.status === 'active' ? '✅ 活跃' : '❌ 禁用' }}
          </div>
          <div class="user-column user-registration">{{ user.registrationTime }}</div>
          <div class="user-column user-subscription">
            {{ user.subscription.status === 'active' ? `✅ 已订阅` : '❌ 未订阅' }}
          </div>
          <div class="user-column user-actions">
            <button class="view-btn" @click="viewUser(user)">查看</button>
            <button class="edit-btn" @click="editUser(user)">编辑</button>
            <button class="toggle-btn" @click="toggleUserStatus(user)">
              {{ user.status === 'active' ? '禁用' : '启用' }}
            </button>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="totalPages > 1">
        <button class="page-btn" :disabled="currentPage === 1" @click="prevPage">上一页</button>
        <span class="page-info">{{ currentPage }} / {{ totalPages }}</span>
        <button class="page-btn" :disabled="currentPage === totalPages" @click="nextPage">下一页</button>
      </div>
    </section>

    <!-- 添加用户弹窗 -->
    <div class="modal" v-if="showAddUserModal">
      <div class="modal-content">
        <h3 class="modal-title">添加用户</h3>
        <form @submit.prevent="addUser">
          <div class="form-row">
            <div class="form-group">
              <label for="user-name">用户名</label>
              <input type="text" id="user-name" v-model="newUser.name" required>
            </div>
            <div class="form-group">
              <label for="user-type">用户类型</label>
              <select id="user-type" v-model="newUser.type" required>
                <option value="c">🏠 C端用户</option>
                <option value="b">🏢 B端用户</option>
              </select>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label for="user-phone">手机号</label>
              <input type="tel" id="user-phone" v-model="newUser.phone" required>
            </div>
            <div class="form-group">
              <label for="user-email">邮箱（可选）</label>
              <input type="email" id="user-email" v-model="newUser.email">
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label for="user-password">初始密码</label>
              <input type="password" id="user-password" v-model="newUser.password" required>
            </div>
          </div>
          <div class="form-actions">
            <button type="button" class="cancel-btn" @click="showAddUserModal = false">取消</button>
            <button type="submit" class="confirm-btn">添加</button>
          </div>
        </form>
      </div>
    </div>
  </div>
  </OperatorLayout>
</template>

<script>
import OperatorLayout from '../../components/OperatorLayout.vue'

export default {
  name: 'UserManagementView',
  components: {
    OperatorLayout
  },
  data() {
    return {
      // 用户列表数据
      users: [
        {
          id: 'SMG-C-20251201-001',
          name: '张三',
          type: 'c',
          phone: '13800138001',
          email: 'zhangsan@example.com',
          status: 'active',
          registrationTime: '2025-12-01',
          subscription: {
            status: 'active',
            plan: 'premium',
            expiryDate: '2026-12-01'
          }
        },
        {
          id: 'SMG-B-20251202-002',
          name: '李四',
          type: 'b',
          phone: '13900139002',
          email: 'lisi@example.com',
          status: 'active',
          registrationTime: '2025-12-02',
          subscription: {
            status: 'active',
            plan: 'enterprise',
            expiryDate: '2026-12-02'
          }
        },
        {
          id: 'SMG-C-20251203-003',
          name: '王五',
          type: 'c',
          phone: '13700137003',
          email: 'wangwu@example.com',
          status: 'disabled',
          registrationTime: '2025-12-03',
          subscription: {
            status: 'inactive',
            plan: 'free',
            expiryDate: '2025-12-31'
          }
        },
        {
          id: 'SMG-B-20251204-004',
          name: '赵六',
          type: 'b',
          phone: '13600136004',
          email: 'zhaoliu@example.com',
          status: 'active',
          registrationTime: '2025-12-04',
          subscription: {
            status: 'active',
            plan: 'basic',
            expiryDate: '2026-06-04'
          }
        },
        {
          id: 'SMG-C-20251205-005',
          name: '孙七',
          type: 'c',
          phone: '13500135005',
          email: 'sunqi@example.com',
          status: 'active',
          registrationTime: '2025-12-05',
          subscription: {
            status: 'active',
            plan: 'premium',
            expiryDate: '2026-12-05'
          }
        }
      ],
      // 搜索关键词
      searchKeyword: '',
      // 过滤后的用户列表
      filteredUsers: [],
      // 分页信息
      currentPage: 1,
      pageSize: 5,
      totalPages: 1,
      // 添加用户弹窗
      showAddUserModal: false,
      // 新用户数据
      newUser: {
        name: '',
        type: 'c',
        phone: '',
        email: '',
        password: ''
      }
    }
  },
  mounted() {
    this.searchUsers();
  },
  methods: {
    // 搜索用户
    searchUsers() {
      let filtered = [...this.users]
      
      // 根据关键词搜索
      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase()
        filtered = filtered.filter(user => 
          user.name.toLowerCase().includes(keyword) || 
          user.id.toLowerCase().includes(keyword) || 
          user.phone.includes(keyword)
        )
      }
      
      // 计算分页
      this.totalPages = Math.ceil(filtered.length / this.pageSize)
      this.currentPage = 1
      this.updateFilteredUsers(filtered)
    },
    
    // 更新过滤后的用户列表
    updateFilteredUsers(filtered) {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      this.filteredUsers = filtered.slice(start, end)
    },
    
    // 上一页
    prevPage() {
      if (this.currentPage > 1) {
        this.currentPage--
        this.searchUsers()
      }
    },
    
    // 下一页
    nextPage() {
      if (this.currentPage < this.totalPages) {
        this.currentPage++
        this.searchUsers()
      }
    },
    
    // 查看用户
    viewUser(user) {
      alert(`查看用户：${user.name}`)
    },
    
    // 编辑用户
    editUser(user) {
      alert(`编辑用户：${user.name}`)
    },
    
    // 切换用户状态
    toggleUserStatus(user) {
      user.status = user.status === 'active' ? 'disabled' : 'active'
      alert(`用户${user.name}已${user.status === 'active' ? '启用' : '禁用'}`)
    },
    
    // 添加用户
    addUser() {
      // 模拟添加用户
      const newUser = {
        id: `${this.newUser.type === 'c' ? 'SMG-C' : 'SMG-B'}-${new Date().toISOString().split('T')[0].replace(/-/g, '')}-${Math.floor(Math.random() * 1000).toString().padStart(3, '0')}`,
        ...this.newUser,
        status: 'active',
        registrationTime: new Date().toISOString().split('T')[0],
        subscription: {
          status: 'inactive',
          plan: 'free',
          expiryDate: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
        }
      }
      this.users.push(newUser)
      this.showAddUserModal = false
      this.newUser = {
        name: '',
        type: 'c',
        phone: '',
        email: '',
        password: ''
      }
      this.searchUsers()
      alert('用户添加成功！')
    }
  },
  computed: {
    // 用户统计信息
    totalUsers() {
      return this.users.length
    },
    cEndUsers() {
      return this.users.filter(user => user.type === 'c').length
    },
    bEndUsers() {
      return this.users.filter(user => user.type === 'b').length
    },
    activeUsers() {
      return this.users.filter(user => user.status === 'active').length
    }
  }
}
</script>

<style scoped>
.user-management-container {
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

.add-user-btn {
  background-color: #1abc9c;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.add-user-btn:hover {
  background-color: #16a085;
}

.users-stats-section {
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

.stat-item.c-end .stat-value {
  color: #3498db;
}

.stat-item.b-end .stat-value {
  color: #e74c3c;
}

.stat-item.active .stat-value {
  color: #27ae60;
}

.users-list-section {
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

.users-list {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.users-header {
  display: flex;
  background-color: #f8f9fa;
  padding: 12px 16px;
  font-weight: 600;
  border-bottom: 1px solid #e0e0e0;
}

.user-item {
  display: flex;
  padding: 16px;
  border-bottom: 1px solid #e0e0e0;
  transition: background-color 0.2s;
}

.user-item:hover {
  background-color: #f8f9fa;
}

.user-item.active {
  background-color: #f0fff4;
}

.user-item.disabled {
  background-color: #fff5f5;
  opacity: 0.7;
}

.user-column {
  flex: 1;
}

.user-id {
  flex: 1.5;
}

.user-name, .user-phone, .user-status, .user-registration, .user-subscription {
  flex: 1;
}

.user-type {
  flex: 0.8;
}

.user-actions {
  flex: 1;
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.view-btn, .edit-btn, .toggle-btn {
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

.toggle-btn {
  background-color: #95a5a6;
  color: white;
}

.toggle-btn:hover {
  background-color: #7f8c8d;
}

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
  background-color: #27ae60;
  color: white;
}

.confirm-btn:hover {
  background-color: #229954;
}

/* PC端优化样式 */
/* 增强卡片悬浮效果 */
.stats-card:hover {
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

/* 增强用户项悬浮效果 */
.user-item:hover {
  transform: translateX(4px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
  background-color: #f0f4f8;
}

/* 增强按钮交互效果 */
.view-btn,
.edit-btn,
.toggle-btn {
  transition: all 0.3s ease;
}

.view-btn:hover,
.edit-btn:hover,
.toggle-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
}

/* 增强添加用户按钮 */
.add-user-btn {
  transition: all 0.3s ease;
}

.add-user-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
}

/* 优化表单元素 */
.search-bar input:focus {
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

/* 增强模态框效果 */
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

/* 增强表单交互 */
.form-group input:focus,
.form-group select:focus {
  outline: 2px solid #1abc9c;
  border-color: #1abc9c;
  transition: all 0.2s ease;
}

/* 增强按钮交互 */
.cancel-btn,
.confirm-btn {
  transition: all 0.3s ease;
}

.cancel-btn:hover,
.confirm-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
}

</style>