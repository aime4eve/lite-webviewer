<template>
  <div class="diagnostics">
    <h2>Remote Diagnostics</h2>
    
    <div class="control-panel">
      <input v-model="deviceId" placeholder="Device ID" type="number" />
      <button @click="fetchLogs">Fetch Logs</button>
    </div>

    <div class="logs-container" v-if="logs.length > 0">
      <h3>Device Logs</h3>
      <div class="log-window">
        <div v-for="(log, i) in logs" :key="i" class="log-line">{{ log }}</div>
      </div>
    </div>

    <div class="command-panel">
      <h3>Send Command</h3>
      <input v-model="command" placeholder="e.g. REBOOT, STATUS, PING" />
      <button @click="sendCmd">Send</button>
      <div v-if="cmdResponse" class="cmd-response">
        Response: {{ cmdResponse }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { deviceApi } from '../../api/device'

const deviceId = ref('')
const logs = ref([])
const command = ref('')
const cmdResponse = ref('')

const fetchLogs = async () => {
  if (!deviceId.value) return
  try {
    logs.value = await deviceApi.getDeviceLogs(deviceId.value)
  } catch (e) {
    console.error(e)
  }
}

const sendCmd = async () => {
  if (!deviceId.value || !command.value) return
  try {
    const res = await deviceApi.sendDiagnosticCommand(deviceId.value, command.value)
    cmdResponse.value = res
  } catch (e) {
    cmdResponse.value = 'Error sending command'
  }
}
</script>

<style scoped>
.diagnostics { padding: 20px; }
.control-panel, .command-panel { margin-bottom: 20px; padding: 15px; border: 1px solid #eee; border-radius: 8px; }
.log-window { background: #333; color: #0f0; padding: 15px; font-family: monospace; height: 200px; overflow-y: auto; border-radius: 4px; }
.log-line { margin-bottom: 5px; }
input { padding: 8px; margin-right: 10px; }
</style>
