<template>
  <div class="alarms">
    <h2>General Alarms</h2>
    
    <div class="filter">
      <input v-model="filterDeviceId" placeholder="Filter by Device ID" type="number" />
      <button @click="loadAlarms">Refresh</button>
    </div>

    <div class="alarm-list">
      <div v-for="alarm in alarms" :key="alarm.id" class="alarm-item" :class="alarm.severity.toLowerCase()">
        <div class="alarm-header">
          <span class="severity">{{ alarm.severity }}</span>
          <span class="time">{{ alarm.timestamp }}</span>
        </div>
        <p class="message">{{ alarm.message }}</p>
        <p class="device">Device: {{ alarm.deviceId }}</p>
        <div class="actions">
          <button @click="confirm(alarm.id)">Confirm</button>
          <button @click="clear(alarm.id)">Clear</button>
        </div>
      </div>
      <div v-if="alarms.length === 0">No active alarms.</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { deviceApi } from '../../api/device'

const alarms = ref([])
const filterDeviceId = ref('')

const loadAlarms = async () => {
  try {
    alarms.value = await deviceApi.getAlarms(filterDeviceId.value || null)
  } catch (e) {
    console.error(e)
  }
}

const confirm = async (id) => {
  await deviceApi.confirmAlarm(id)
  loadAlarms()
}

const clear = async (id) => {
  await deviceApi.clearAlarm(id)
  loadAlarms()
}

onMounted(loadAlarms)
</script>

<style scoped>
.alarms { padding: 20px; }
.alarm-item { border: 1px solid #ddd; padding: 15px; margin-bottom: 10px; border-radius: 8px; border-left-width: 5px; }
.alarm-item.high { border-left-color: red; }
.alarm-item.medium { border-left-color: orange; }
.alarm-item.low { border-left-color: blue; }

.alarm-header { display: flex; justify-content: space-between; font-weight: bold; margin-bottom: 5px; }
.actions button { margin-right: 10px; padding: 5px 10px; cursor: pointer; }
</style>
