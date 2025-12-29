<template>
  <div class="device-health">
    <h2>Device Health Fingerprint</h2>
    <div v-if="loading">Loading...</div>
    <div v-else-if="health">
      <div class="health-score" :class="health.healthLevel.toLowerCase()">
        <span class="score">{{ health.healthScore }}</span>
        <span class="level">{{ health.healthLevel }}</span>
      </div>
      <div class="factors">
        <h3>Health Factors</h3>
        <div v-for="factor in health.factors" :key="factor.name" class="factor-item">
          <div class="factor-header">
            <span class="factor-name">{{ factor.name }}</span>
            <span class="factor-impact">-{{ factor.impact }}</span>
          </div>
          <p class="factor-desc">{{ factor.description }}</p>
        </div>
        <div v-if="!health.factors || health.factors.length === 0" class="no-issues">
          No health issues detected.
        </div>
      </div>
    </div>
    <div v-else>No health data available.</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getHealthFingerprint } from '../../api/ai'

const route = useRoute()
const deviceId = route.params.id
const health = ref(null)
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await getHealthFingerprint(deviceId)
    health.value = res
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.device-health {
  padding: 20px;
}
.health-score {
  text-align: center;
  padding: 30px;
  border-radius: 10px;
  color: white;
  margin-bottom: 20px;
}
.health-score.excellent { background-color: #52c41a; }
.health-score.good { background-color: #1890ff; }
.health-score.fair { background-color: #faad14; }
.health-score.poor { background-color: #f5222d; }

.score {
  font-size: 48px;
  font-weight: bold;
  display: block;
}
.level {
  font-size: 18px;
}

.factor-item {
  border-bottom: 1px solid #eee;
  padding: 10px 0;
}
.factor-header {
  display: flex;
  justify-content: space-between;
  font-weight: bold;
}
.factor-impact {
  color: #f5222d;
}
.factor-desc {
  color: #666;
  font-size: 14px;
}
</style>
