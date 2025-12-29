<template>
  <div class="risk-prediction">
    <h2>AI Risk Prediction</h2>
    
    <div class="section" v-if="risk">
      <h3>Current Status</h3>
      <p>Risk Level: <span :class="risk.riskLevel.toLowerCase()">{{ risk.riskLevel }}</span></p>
      <p>Score: {{ risk.riskScore.toFixed(2) }}</p>
      <p>Recommendation: {{ risk.recommendation }}</p>
    </div>

    <div class="section" v-if="climateConfig">
      <h3>Climate Configuration</h3>
      <p>Zone: {{ climateConfig.zoneCode }} ({{ climateConfig.zoneName }})</p>
      <p>Thresholds: Temp > {{ climateConfig.tempThreshold }}°C, Humidity > {{ climateConfig.humidityThreshold }}%</p>
    </div>

    <div class="section feedback-form">
      <h3>Prediction Feedback</h3>
      <div class="form-group">
        <label>Was this prediction accurate?</label>
        <select v-model="feedback.rating">
          <option :value="5">5 - Very Accurate</option>
          <option :value="4">4 - Accurate</option>
          <option :value="3">3 - Neutral</option>
          <option :value="2">2 - Inaccurate</option>
          <option :value="1">1 - Very Inaccurate</option>
        </select>
      </div>
      <div class="form-group">
        <label>Perceived Risk Level</label>
        <select v-model="feedback.riskLevel">
          <option value="LOW">Low</option>
          <option value="MEDIUM">Medium</option>
          <option value="HIGH">High</option>
        </select>
      </div>
      <div class="form-group">
        <label>Comments</label>
        <textarea v-model="feedback.comment"></textarea>
      </div>
      <button @click="submit" :disabled="submitting">Submit Feedback</button>
      <p v-if="submitMsg">{{ submitMsg }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getLatestRisk, submitFeedback, getClimateConfig } from '../../api/ai'

const route = useRoute()
const deviceId = route.params.id
const risk = ref(null)
const climateConfig = ref(null)
const submitting = ref(false)
const submitMsg = ref('')

const feedback = ref({
  rating: 5,
  riskLevel: 'LOW',
  comment: ''
})

onMounted(async () => {
  try {
    const riskRes = await getLatestRisk(deviceId)
    risk.value = riskRes
    feedback.value.riskLevel = riskRes.riskLevel

    // Mock getting location -> zone. In real app, we'd get location from device/phone.
    // For demo, we just fetch a default or mock zone
    const configRes = await getClimateConfig('DEFAULT') 
    climateConfig.value = configRes
  } catch (e) {
    console.error(e)
  }
})

const submit = async () => {
  submitting.value = true
  try {
    await submitFeedback({
      deviceId: parseInt(deviceId),
      ...feedback.value
    })
    submitMsg.value = 'Feedback submitted! Thank you.'
  } catch (e) {
    submitMsg.value = 'Error submitting feedback.'
    console.error(e)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.risk-prediction { padding: 20px; }
.section { margin-bottom: 20px; border: 1px solid #eee; padding: 15px; border-radius: 8px; }
.critical { color: red; font-weight: bold; }
.high { color: orange; font-weight: bold; }
.medium { color: #faad14; }
.low { color: green; }

.form-group { margin-bottom: 10px; }
label { display: block; margin-bottom: 5px; font-weight: bold; }
select, textarea { width: 100%; padding: 8px; }
button { background: #1890ff; color: white; border: none; padding: 10px 20px; border-radius: 4px; cursor: pointer; }
button:disabled { background: #ccc; }
</style>
