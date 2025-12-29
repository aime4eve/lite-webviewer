<template>
  <div class="points-history">
    <h2>Points History</h2>
    <div class="summary">
      <div class="card">
        <h3>Current Balance</h3>
        <span class="value">{{ points.points }}</span>
      </div>
      <div class="card">
        <h3>Total Earned</h3>
        <span class="value">{{ points.totalEarned }}</span>
      </div>
    </div>

    <div class="history-list">
      <h3>Transactions</h3>
      <table>
        <thead>
          <tr>
            <th>Date</th>
            <th>Description</th>
            <th>Type</th>
            <th>Amount</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="tx in history" :key="tx.id">
            <td>{{ new Date(tx.createdAt).toLocaleString() }}</td>
            <td>{{ tx.description }}</td>
            <td><span class="tag" :class="tx.type">{{ tx.type }}</span></td>
            <td :class="tx.amount > 0 ? 'positive' : 'negative'">
              {{ tx.amount > 0 ? '+' : '' }}{{ tx.amount }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getPoints, getPointsHistory } from '../../api/subscription'

// Mock User ID
const userId = 1001 
const points = ref({ points: 0, totalEarned: 0 })
const history = ref([])

onMounted(async () => {
  try {
    points.value = await getPoints(userId)
    history.value = await getPointsHistory(userId)
  } catch (e) {
    console.error(e)
  }
})
</script>

<style scoped>
.points-history { padding: 20px; }
.summary { display: flex; gap: 20px; margin-bottom: 20px; }
.card { background: #f0f2f5; padding: 20px; border-radius: 8px; flex: 1; text-align: center; }
.value { font-size: 32px; font-weight: bold; color: #1890ff; }

table { width: 100%; border-collapse: collapse; }
th, td { text-align: left; padding: 12px; border-bottom: 1px solid #eee; }
.tag { padding: 4px 8px; border-radius: 4px; font-size: 12px; }
.tag.EARN { background: #f6ffed; color: #52c41a; }
.tag.SPEND { background: #fff1f0; color: #f5222d; }
.positive { color: #52c41a; font-weight: bold; }
.negative { color: #f5222d; font-weight: bold; }
</style>
