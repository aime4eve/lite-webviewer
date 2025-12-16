<template>
  <OperatorLayout pageTitle="💳 计费管理" activeNav="billing">
    <div class="billing-management-container">
      <!-- 页面操作栏 -->
      <div class="page-actions">
        <select class="filter-select" v-model="orderFilter.status">
          <option value="all">所有订单状态</option>
          <option value="pending">⏳ 待支付</option>
          <option value="paid">✅ 已支付</option>
          <option value="cancelled">❌ 已取消</option>
        </select>
      </div>

    <!-- 计费统计信息 -->
    <section class="billing-stats-section">
      <div class="stats-card">
        <div class="stat-item">
          <span class="stat-label">今日订单</span>
          <span class="stat-value">{{ todayOrders }}</span>
        </div>
        <div class="stat-item revenue">
          <span class="stat-label">今日收入</span>
          <span class="stat-value">¥{{ todayRevenue }}</span>
        </div>
        <div class="stat-item total-revenue">
          <span class="stat-label">本月收入</span>
          <span class="stat-value">¥{{ monthlyRevenue }}</span>
        </div>
        <div class="stat-item total-orders">
          <span class="stat-label">总订单数</span>
          <span class="stat-value">{{ totalOrders }}</span>
        </div>
      </div>
    </section>

    <!-- 订单列表 -->
    <section class="orders-list-section">
      <div class="section-header">
        <h2 class="section-title">订单列表</h2>
        <div class="search-bar">
          <input type="text" placeholder="搜索订单号、用户名或设备SN码" v-model="searchKeyword" @input="searchOrders">
          <button class="search-btn">🔍</button>
        </div>
      </div>

      <div class="orders-list">
        <div class="orders-header">
          <div class="order-column order-id">订单号</div>
          <div class="order-column user-info">用户信息</div>
          <div class="order-column order-amount">金额</div>
          <div class="order-column order-status">状态</div>
          <div class="order-column order-time">创建时间</div>
          <div class="order-column order-actions">操作</div>
        </div>
        <div 
          class="order-item" 
          v-for="order in filteredOrders" 
          :key="order.id"
          :class="order.status"
        >
          <div class="order-column order-id">
            <div class="order-id-text">{{ order.id }}</div>
            <div class="order-type">
              {{ order.type === 'subscription' ? '📄 订阅订单' : '🛒 设备购买' }}
            </div>
          </div>
          <div class="order-column user-info">
            <div class="user-name">{{ order.user.name }}</div>
            <div class="user-id">{{ order.user.id }}</div>
          </div>
          <div class="order-column order-amount">
            <div class="amount">¥{{ order.amount }}</div>
            <div class="plan-name" v-if="order.planName">{{ order.planName }}</div>
          </div>
          <div class="order-column order-status">
            <div class="status-badge" :class="order.status">
              {{ order.status === 'pending' ? '⏳ 待支付' : 
                 order.status === 'paid' ? '✅ 已支付' : '❌ 已取消' }}
            </div>
          </div>
          <div class="order-column order-time">
            <div class="create-time">{{ order.createTime }}</div>
            <div class="pay-time" v-if="order.payTime">支付: {{ order.payTime }}</div>
          </div>
          <div class="order-column order-actions">
            <button class="view-btn" @click="viewOrder(order)">查看</button>
            <button 
              class="process-btn" 
              @click="processOrder(order)"
              :disabled="order.status !== 'pending'"
            >
              处理
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
  </div>
  </OperatorLayout>
</template>

<script>
import OperatorLayout from '../../components/OperatorLayout.vue'

export default {
  name: 'BillingManagementView',
  components: {
    OperatorLayout
  },
  data() {
    return {
      // 订单列表数据
      orders: [
        {
          id: 'ORD-20251215-0001',
          type: 'subscription',
          user: {
            name: '张三',
            id: 'SMG-C-20251201-001'
          },
          amount: 240,
          status: 'paid',
          createTime: '2025-12-15 14:30',
          payTime: '2025-12-15 14:32',
          planName: '全功能防霉版 - 1年卡'
        },
        {
          id: 'ORD-20251215-0002',
          type: 'subscription',
          user: {
            name: '李四',
            id: 'SMG-B-20251202-002'
          },
          amount: 600,
          status: 'pending',
          createTime: '2025-12-15 14:25',
          planName: '企业版 - 3年卡'
        },
        {
          id: 'ORD-20251215-0003',
          type: 'device',
          user: {
            name: '王五',
            id: 'SMG-C-20251203-003'
          },
          amount: 199,
          status: 'paid',
          createTime: '2025-12-15 14:20',
          payTime: '2025-12-15 14:21',
          planName: '温湿度传感器'
        },
        {
          id: 'ORD-20251215-0004',
          type: 'subscription',
          user: {
            name: '赵六',
            id: 'SMG-B-20251204-004'
          },
          amount: 440,
          status: 'cancelled',
          createTime: '2025-12-15 14:15',
          planName: '全功能防霉版 - 2年卡'
        },
        {
          id: 'ORD-20251215-0005',
          type: 'device',
          user: {
            name: '孙七',
            id: 'SMG-C-20251205-005'
          },
          amount: 299,
          status: 'paid',
          createTime: '2025-12-15 14:10',
          payTime: '2025-12-15 14:11',
          planName: 'LoRa开关面板'
        },
        {
          id: 'ORD-20251215-0006',
          type: 'subscription',
          user: {
            name: '周八',
            id: 'SMG-C-20251206-006'
          },
          amount: 240,
          status: 'pending',
          createTime: '2025-12-15 14:05',
          planName: '全功能防霉版 - 1年卡'
        }
      ],
      // 搜索关键词
      searchKeyword: '',
      // 过滤后的订单列表
      filteredOrders: [],
      // 分页信息
      currentPage: 1,
      pageSize: 5,
      totalPages: 1,
      // 订单过滤器
      orderFilter: {
        status: 'all'
      }
    }
  },
  mounted() {
    this.searchOrders();
  },
  methods: {
    // 搜索订单
    searchOrders() {
      let filtered = [...this.orders]
      
      // 根据状态过滤
      if (this.orderFilter.status !== 'all') {
        filtered = filtered.filter(order => order.status === this.orderFilter.status)
      }
      
      // 根据关键词搜索
      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase()
        filtered = filtered.filter(order => 
          order.id.toLowerCase().includes(keyword) || 
          order.user.name.toLowerCase().includes(keyword) || 
          order.user.id.toLowerCase().includes(keyword)
        )
      }
      
      // 计算分页
      this.totalPages = Math.ceil(filtered.length / this.pageSize)
      this.currentPage = 1
      this.updateFilteredOrders(filtered)
    },
    
    // 更新过滤后的订单列表
    updateFilteredOrders(filtered) {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      this.filteredOrders = filtered.slice(start, end)
    },
    
    // 上一页
    prevPage() {
      if (this.currentPage > 1) {
        this.currentPage--
        this.searchOrders()
      }
    },
    
    // 下一页
    nextPage() {
      if (this.currentPage < this.totalPages) {
        this.currentPage++
        this.searchOrders()
      }
    },
    
    // 查看订单
    viewOrder(order) {
      alert(`查看订单：${order.id}\n用户：${order.user.name}\n金额：¥${order.amount}\n状态：${order.status === 'pending' ? '待支付' : order.status === 'paid' ? '已支付' : '已取消'}\n创建时间：${order.createTime}${order.payTime ? `\n支付时间：${order.payTime}` : ''}`)
    },
    
    // 处理订单
    processOrder(order) {
      if (order.status === 'pending') {
        order.status = 'paid'
        order.payTime = new Date().toISOString().replace('T', ' ').substring(0, 16)
        alert(`订单${order.id}已标记为已支付`)
      }
    }
  },
  computed: {
    // 今日订单数
    todayOrders() {
      const today = new Date().toISOString().split('T')[0]
      return this.orders.filter(order => order.createTime.startsWith(today)).length
    },
    
    // 今日收入
    todayRevenue() {
      const today = new Date().toISOString().split('T')[0]
      return this.orders
        .filter(order => order.createTime.startsWith(today) && order.status === 'paid')
        .reduce((sum, order) => sum + order.amount, 0)
        .toFixed(2)
    },
    
    // 本月收入
    monthlyRevenue() {
      const thisMonth = new Date().toISOString().substring(0, 7) // YYYY-MM
      return this.orders
        .filter(order => order.createTime.startsWith(thisMonth) && order.status === 'paid')
        .reduce((sum, order) => sum + order.amount, 0)
        .toFixed(2)
    },
    
    // 总订单数
    totalOrders() {
      return this.orders.length
    }
  }
}
</script>

<style scoped>
.billing-management-container {
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

.filter-select {
  padding: 8px 12px;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  background-color: white;
}

.billing-stats-section {
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

.stat-item.revenue .stat-value,
.stat-item.total-revenue .stat-value {
  color: #27ae60;
}

.stat-item.total-orders .stat-value {
  color: #3498db;
}

.orders-list-section {
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

.orders-list {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.orders-header {
  display: flex;
  background-color: #f8f9fa;
  padding: 12px 16px;
  font-weight: 600;
  border-bottom: 1px solid #e0e0e0;
}

.order-item {
  display: flex;
  padding: 16px;
  border-bottom: 1px solid #e0e0e0;
  transition: background-color 0.2s;
  align-items: center;
}

.order-item:hover {
  background-color: #f8f9fa;
}

.order-item.pending {
  background-color: #fff8e1;
}

.order-item.paid {
  background-color: #f0fff4;
}

.order-item.cancelled {
  background-color: #fff5f5;
  opacity: 0.8;
}

.order-column {
  flex: 1;
}

.order-id {
  flex: 1.5;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.order-id-text {
  font-weight: 500;
  font-size: 16px;
}

.order-type {
  font-size: 14px;
  color: #666;
}

.user-info {
  flex: 1.2;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-name {
  font-weight: 500;
}

.user-id {
  font-size: 14px;
  color: #666;
}

.order-amount {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.amount {
  font-weight: 600;
  font-size: 16px;
  color: #27ae60;
}

.plan-name {
  font-size: 14px;
  color: #666;
}

.order-status {
  flex: 1;
  display: flex;
  align-items: center;
}

.status-badge {
  padding: 6px 12px;
  border-radius: 16px;
  font-size: 14px;
  font-weight: 500;
  color: white;
}

.status-badge.pending {
  background-color: #f39c12;
}

.status-badge.paid {
  background-color: #27ae60;
}

.status-badge.cancelled {
  background-color: #e74c3c;
}

.order-time {
  flex: 1.5;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.create-time {
  font-weight: 500;
}

.pay-time {
  font-size: 14px;
  color: #666;
}

.order-actions {
  flex: 1;
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.view-btn, .process-btn {
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

.process-btn {
  background-color: #27ae60;
  color: white;
}

.process-btn:hover:not(:disabled) {
  background-color: #229954;
}

.process-btn:disabled {
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

@media (max-width: 1200px) {
  .billing-management-container {
    padding: 0 16px;
  }
  
  .header {
    padding: 12px 16px;
  }
  
  .title {
    font-size: 20px;
  }
  
  .billing-stats-section,
  .orders-list-section {
    padding: 0 16px 16px;
  }
  
  .stats-card {
    flex-wrap: wrap;
    gap: 16px;
    justify-content: flex-start;
  }
  
  .stat-item {
    width: calc(50% - 8px);
  }
  
  .search-bar input {
    width: 200px;
  }
  
  .orders-header,
  .order-item {
    font-size: 14px;
  }
  
  .order-actions {
    flex-direction: column;
    gap: 4px;
  }
}

@media (max-width: 768px) {
  .stats-card {
    flex-direction: column;
    gap: 16px;
  }
  
  .stat-item {
    width: 100%;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .search-bar input {
    width: 100%;
  }
  
  .orders-header {
    display: none;
  }
  
  .order-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
    padding: 12px;
  }
  
  .order-column {
    width: 100%;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .order-column::before {
    content: attr(data-label);
    font-weight: 500;
    color: #666;
  }
  
  .order-id::before {
    content: '订单号: ';
  }
  
  .order-id {
    align-items: flex-start;
  }
  
  .order-id::after {
    display: none;
  }
  
  .user-info::before {
    content: '用户信息: ';
  }
  
  .order-amount::before {
    content: '金额: ';
  }
  
  .order-status::before {
    content: '状态: ';
  }
  
  .order-time::before {
    content: '时间: ';
  }
  
  .order-actions {
    flex-direction: row;
    justify-content: flex-start;
    width: 100%;
    gap: 8px;
    margin-top: 8px;
  }
  
  .order-actions::before {
    content: '操作: ';
    font-weight: 500;
    color: #666;
  }
}
</style>