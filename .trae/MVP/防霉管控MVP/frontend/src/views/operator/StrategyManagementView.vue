<template>
  <OperatorLayout pageTitle="⚙️ 策略管理" activeNav="strategies">
    <div class="strategy-management-container">
      <!-- 页面操作栏 -->
      <div class="page-actions">
        <button class="add-strategy-btn" @click="showAddStrategyModal = true">➕ 添加策略</button>
      </div>

    <!-- 策略统计信息 -->
    <section class="strategies-stats-section">
      <div class="stats-card">
        <div class="stat-item">
          <span class="stat-label">策略总数</span>
          <span class="stat-value">{{ totalStrategies }}</span>
        </div>
        <div class="stat-item active">
          <span class="stat-label">已启用策略</span>
          <span class="stat-value">{{ activeStrategies }}</span>
        </div>
        <div class="stat-item default">
          <span class="stat-label">默认策略</span>
          <span class="stat-value">{{ defaultStrategies }}</span>
        </div>
        <div class="stat-item custom">
          <span class="stat-label">自定义策略</span>
          <span class="stat-value">{{ customStrategies }}</span>
        </div>
      </div>
    </section>

    <!-- 策略列表 -->
    <section class="strategies-list-section">
      <div class="section-header">
        <h2 class="section-title">策略列表</h2>
        <div class="search-bar">
          <input type="text" placeholder="搜索策略名称或描述" v-model="searchKeyword" @input="searchStrategies">
          <button class="search-btn">🔍</button>
        </div>
      </div>

      <div class="strategies-list">
        <div class="strategies-header">
          <div class="strategy-column strategy-name">策略名称</div>
          <div class="strategy-column strategy-type">类型</div>
          <div class="strategy-column strategy-status">状态</div>
          <div class="strategy-column strategy-apply-count">应用设备数</div>
          <div class="strategy-column strategy-created">创建时间</div>
          <div class="strategy-column strategy-actions">操作</div>
        </div>
        <div 
          class="strategy-item" 
          v-for="strategy in filteredStrategies" 
          :key="strategy.id"
          :class="strategy.status"
        >
          <div class="strategy-column strategy-name">
            <div class="strategy-name-text">{{ strategy.name }}</div>
            <div class="strategy-description">{{ strategy.description }}</div>
          </div>
          <div class="strategy-column strategy-type">
            {{ strategy.type === 'default' ? '📋 默认策略' : '🎨 自定义策略' }}
          </div>
          <div class="strategy-column strategy-status">
            <div class="status-toggle">
              <input 
                type="checkbox" 
                :id="`toggle-${strategy.id}`" 
                v-model="strategy.status"
                @change="toggleStrategyStatus(strategy)"
              >
              <label :for="`toggle-${strategy.id}`"></label>
            </div>
            <span class="status-text">
              {{ strategy.status === 'active' ? '✅ 已启用' : '❌ 已禁用' }}
            </span>
          </div>
          <div class="strategy-column strategy-apply-count">{{ strategy.applyCount }}</div>
          <div class="strategy-column strategy-created">{{ strategy.createdTime }}</div>
          <div class="strategy-column strategy-actions">
            <button class="view-btn" @click="viewStrategy(strategy)">查看</button>
            <button class="edit-btn" @click="editStrategy(strategy)">编辑</button>
            <button 
              class="delete-btn" 
              @click="deleteStrategy(strategy)"
              :disabled="strategy.type === 'default'"
            >
              删除
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

    <!-- 添加策略弹窗 -->
    <div class="modal" v-if="showAddStrategyModal">
      <div class="modal-content">
        <h3 class="modal-title">添加防霉策略</h3>
        <form @submit.prevent="addStrategy">
          <div class="form-row">
            <div class="form-group">
              <label for="strategy-name">策略名称</label>
              <input type="text" id="strategy-name" v-model="newStrategy.name" required>
            </div>
            <div class="form-group">
              <label for="strategy-type">策略类型</label>
              <select id="strategy-type" v-model="newStrategy.type" required>
                <option value="custom">🎨 自定义策略</option>
                <option value="default">📋 默认策略</option>
              </select>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label for="strategy-description">策略描述</label>
              <textarea id="strategy-description" v-model="newStrategy.description" rows="3" required></textarea>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label for="strategy-threshold">湿度阈值 (%)</label>
              <input type="number" id="strategy-threshold" v-model.number="newStrategy.threshold" min="0" max="100" required>
            </div>
            <div class="form-group">
              <label for="strategy-duration">持续时间 (分钟)</label>
              <input type="number" id="strategy-duration" v-model.number="newStrategy.duration" min="1" max="120" required>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label for="strategy-action">执行动作</label>
              <select id="strategy-action" v-model="newStrategy.action" required>
                <option value="fan">🌀 开启排风扇</option>
                <option value="heater">🔥 开启加热器</option>
                <option value="both">🌀🔥 同时开启</option>
              </select>
            </div>
          </div>
          <div class="form-actions">
            <button type="button" class="cancel-btn" @click="showAddStrategyModal = false">取消</button>
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
  name: 'StrategyManagementView',
  components: {
    OperatorLayout
  },
  data() {
    return {
      // 策略列表数据
      strategies: [
        {
          id: 1,
          name: '黄梅季防霉策略',
          description: '针对黄梅季高湿度环境的防霉策略，湿度超过80%持续30分钟触发',
          type: 'default',
          status: 'active',
          applyCount: 1256,
          createdTime: '2025-11-01',
          threshold: 80,
          duration: 30,
          action: 'both'
        },
        {
          id: 2,
          name: '普通防霉策略',
          description: '日常环境下的标准防霉策略，湿度超过75%持续20分钟触发',
          type: 'default',
          status: 'active',
          applyCount: 892,
          createdTime: '2025-11-01',
          threshold: 75,
          duration: 20,
          action: 'fan'
        },
        {
          id: 3,
          name: '低能耗防霉策略',
          description: '优先使用排风扇，仅在必要时使用加热器的低能耗策略',
          type: 'custom',
          status: 'active',
          applyCount: 345,
          createdTime: '2025-11-15',
          threshold: 85,
          duration: 40,
          action: 'fan'
        },
        {
          id: 4,
          name: '快速除湿策略',
          description: '针对突发高湿度的快速除湿策略，湿度超过90%立即触发',
          type: 'custom',
          status: 'disabled',
          applyCount: 123,
          createdTime: '2025-11-20',
          threshold: 90,
          duration: 10,
          action: 'both'
        },
        {
          id: 5,
          name: '夜间静音策略',
          description: '夜间使用的静音防霉策略，仅在必要时触发',
          type: 'custom',
          status: 'active',
          applyCount: 234,
          createdTime: '2025-12-01',
          threshold: 85,
          duration: 30,
          action: 'fan'
        },
        {
          id: 6,
          name: '冬季防霉策略',
          description: '冬季低温环境下的防霉策略，结合温度和湿度判断',
          type: 'custom',
          status: 'active',
          applyCount: 567,
          createdTime: '2025-12-05',
          threshold: 70,
          duration: 25,
          action: 'heater'
        }
      ],
      // 搜索关键词
      searchKeyword: '',
      // 过滤后的策略列表
      filteredStrategies: [],
      // 分页信息
      currentPage: 1,
      pageSize: 5,
      totalPages: 1,
      // 添加策略弹窗
      showAddStrategyModal: false,
      // 新策略数据
      newStrategy: {
        name: '',
        type: 'custom',
        description: '',
        threshold: 80,
        duration: 30,
        action: 'fan'
      }
    }
  },
  mounted() {
    this.searchStrategies();
  },
  methods: {
    // 搜索策略
    searchStrategies() {
      let filtered = [...this.strategies]
      
      // 根据关键词搜索
      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase()
        filtered = filtered.filter(strategy => 
          strategy.name.toLowerCase().includes(keyword) || 
          strategy.description.toLowerCase().includes(keyword)
        )
      }
      
      // 计算分页
      this.totalPages = Math.ceil(filtered.length / this.pageSize)
      this.currentPage = 1
      this.updateFilteredStrategies(filtered)
    },
    
    // 更新过滤后的策略列表
    updateFilteredStrategies(filtered) {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      this.filteredStrategies = filtered.slice(start, end)
    },
    
    // 上一页
    prevPage() {
      if (this.currentPage > 1) {
        this.currentPage--
        this.searchStrategies()
      }
    },
    
    // 下一页
    nextPage() {
      if (this.currentPage < this.totalPages) {
        this.currentPage++
        this.searchStrategies()
      }
    },
    
    // 查看策略
    viewStrategy(strategy) {
      alert(`查看策略：${strategy.name}\n描述：${strategy.description}\n湿度阈值：${strategy.threshold}%\n持续时间：${strategy.duration}分钟\n执行动作：${strategy.action === 'fan' ? '开启排风扇' : strategy.action === 'heater' ? '开启加热器' : '同时开启'}`)
    },
    
    // 编辑策略
    editStrategy(strategy) {
      alert(`编辑策略：${strategy.name}`)
    },
    
    // 删除策略
    deleteStrategy(strategy) {
      if (confirm(`确定要删除策略 ${strategy.name} 吗？`)) {
        const index = this.strategies.findIndex(s => s.id === strategy.id)
        if (index !== -1) {
          this.strategies.splice(index, 1)
          this.searchStrategies()
        }
      }
    },
    
    // 切换策略状态
    toggleStrategyStatus(strategy) {
      strategy.status = strategy.status === 'active' ? 'disabled' : 'active'
      alert(`策略${strategy.name}已${strategy.status === 'active' ? '启用' : '禁用'}`)
    },
    
    // 添加策略
    addStrategy() {
      // 模拟添加策略
      const newStrategy = {
        id: Date.now(),
        ...this.newStrategy,
        status: 'active',
        applyCount: 0,
        createdTime: new Date().toISOString().split('T')[0]
      }
      this.strategies.push(newStrategy)
      this.showAddStrategyModal = false
      this.newStrategy = {
        name: '',
        type: 'custom',
        description: '',
        threshold: 80,
        duration: 30,
        action: 'fan'
      }
      this.searchStrategies()
      alert('策略添加成功！')
    }
  },
  computed: {
    // 策略统计信息
    totalStrategies() {
      return this.strategies.length
    },
    activeStrategies() {
      return this.strategies.filter(strategy => strategy.status === 'active').length
    },
    defaultStrategies() {
      return this.strategies.filter(strategy => strategy.type === 'default').length
    },
    customStrategies() {
      return this.strategies.filter(strategy => strategy.type === 'custom').length
    }
  }
}
</script>

<style scoped>
.strategy-management-container {
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

.add-strategy-btn {
  background-color: #1abc9c;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.add-strategy-btn:hover {
  background-color: #16a085;
}

.strategies-stats-section {
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

.stat-item.active .stat-value {
  color: #27ae60;
}

.stat-item.default .stat-value {
  color: #3498db;
}

.stat-item.custom .stat-value {
  color: #f39c12;
}

.strategies-list-section {
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

.strategies-list {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.strategies-header {
  display: flex;
  background-color: #f8f9fa;
  padding: 12px 16px;
  font-weight: 600;
  border-bottom: 1px solid #e0e0e0;
}

.strategy-item {
  display: flex;
  padding: 16px;
  border-bottom: 1px solid #e0e0e0;
  transition: background-color 0.2s;
  align-items: center;
}

.strategy-item:hover {
  background-color: #f8f9fa;
}

.strategy-item.active {
  background-color: #f0fff4;
}

.strategy-item.disabled {
  background-color: #f8f9fa;
  opacity: 0.8;
}

.strategy-column {
  flex: 1;
}

.strategy-name {
  flex: 2;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.strategy-name-text {
  font-weight: 500;
  font-size: 16px;
}

.strategy-description {
  font-size: 14px;
  color: #666;
  line-height: 1.4;
}

.strategy-type, .strategy-apply-count, .strategy-created {
  flex: 1;
}

.strategy-status {
  flex: 1.2;
  display: flex;
  align-items: center;
  gap: 12px;
}

.status-toggle {
  position: relative;
  display: inline-block;
  width: 48px;
  height: 24px;
}

.status-toggle input {
  opacity: 0;
  width: 0;
  height: 0;
}

.status-toggle label {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ccc;
  transition: .4s;
  border-radius: 24px;
}

.status-toggle label:before {
  position: absolute;
  content: "";
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: .4s;
  border-radius: 50%;
}

.status-toggle input:checked + label {
  background-color: #27ae60;
}

.status-toggle input:checked + label:before {
  transform: translateX(24px);
}

.status-text {
  font-size: 14px;
  font-weight: 500;
}

.strategy-actions {
  flex: 1;
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

.delete-btn:hover:not(:disabled) {
  background-color: #c0392b;
}

.delete-btn:disabled {
  background-color: #bdc3c7;
  cursor: not-allowed;
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
.form-group select,
.form-group textarea {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
}

.form-group textarea {
  resize: vertical;
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

/* 增强策略项悬浮效果 */
.strategy-item:hover {
  transform: translateX(4px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
  background-color: #f0f4f8;
}

/* 增强按钮交互效果 */
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

/* 增强添加策略按钮 */
.add-strategy-btn {
  transition: all 0.3s ease;
}

.add-strategy-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
}

/* 优化表单元素 */
.search-bar input:focus,
.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
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

/* 增强状态切换效果 */
.status-toggle {
  transition: all 0.3s ease;
}

.status-toggle input:checked + label {
  background-color: #27ae60;
  box-shadow: 0 0 0 2px rgba(39, 174, 96, 0.3);
}

.status-toggle input:hover + label {
  background-color: #b0b0b0;
}

/* 增强开关按钮交互 */
.toggle-btn:hover {
  transform: scale(1.05);
  transition: all 0.3s ease;
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