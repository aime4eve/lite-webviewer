<template>
  <div class="climate-config">
    <h2>Climate Configuration</h2>
    
    <div class="list">
      <div v-for="config in configs" :key="config.zoneCode" class="config-item">
        <div class="config-header">
          <h3>{{ config.zoneName }} ({{ config.zoneCode }})</h3>
          <button @click="deleteConfig(config.zoneCode)" class="btn-delete">Delete</button>
        </div>
        <div class="config-body">
          <p><strong>Base Temp:</strong> {{ config.baseTemp }}°C</p>
          <p><strong>Humidity Threshold:</strong> {{ config.humidityThreshold }}%</p>
          <p><strong>Mold Growth Factor:</strong> {{ config.moldGrowthFactor }}</p>
        </div>
      </div>
    </div>

    <div class="add-form">
      <h3>Add New Configuration</h3>
      <input v-model="newConfig.zoneCode" placeholder="Zone Code (e.g. CN-SH)" />
      <input v-model="newConfig.zoneName" placeholder="Zone Name (e.g. Shanghai)" />
      <input v-model.number="newConfig.baseTemp" type="number" placeholder="Base Temp" />
      <input v-model.number="newConfig.humidityThreshold" type="number" placeholder="Humidity Threshold" />
      <input v-model.number="newConfig.moldGrowthFactor" type="number" step="0.1" placeholder="Growth Factor" />
      <button @click="addConfig" class="btn-add">Add Configuration</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAllClimateConfigs, saveClimateConfig, deleteClimateConfig } from '../../api/ai'

const configs = ref([])
const newConfig = ref({
  zoneCode: '',
  zoneName: '',
  baseTemp: 20,
  humidityThreshold: 80,
  moldGrowthFactor: 1.0
})

const fetchConfigs = async () => {
  try {
    configs.value = await getAllClimateConfigs()
  } catch (e) {
    console.error(e)
  }
}

const addConfig = async () => {
  try {
    await saveClimateConfig(newConfig.value)
    newConfig.value = { zoneCode: '', zoneName: '', baseTemp: 20, humidityThreshold: 80, moldGrowthFactor: 1.0 }
    fetchConfigs()
  } catch (e) {
    alert('Failed to add configuration')
  }
}

const deleteConfig = async (code) => {
  if (!confirm('Are you sure?')) return
  try {
    await deleteClimateConfig(code)
    fetchConfigs()
  } catch (e) {
    alert('Failed to delete configuration')
  }
}

onMounted(fetchConfigs)
</script>

<style scoped>
.climate-config { padding: 20px; max-width: 800px; margin: 0 auto; }
.config-item { border: 1px solid #eee; padding: 15px; margin-bottom: 15px; border-radius: 8px; }
.config-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #f0f0f0; padding-bottom: 10px; margin-bottom: 10px; }
.btn-delete { background: #ff4d4f; color: white; border: none; padding: 5px 10px; border-radius: 4px; cursor: pointer; }
.add-form { margin-top: 30px; background: #f9f9f9; padding: 20px; border-radius: 8px; }
.add-form input { display: block; width: 100%; margin-bottom: 10px; padding: 8px; border: 1px solid #ddd; border-radius: 4px; }
.btn-add { background: #1890ff; color: white; border: none; padding: 10px 20px; border-radius: 4px; cursor: pointer; }
</style>
