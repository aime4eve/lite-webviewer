<template>
  <div class="provisioning">
    <h2>Device Auto-Provisioning</h2>
    
    <div v-if="step === 1" class="step">
      <h3>Step 1: Discover Devices</h3>
      <p>Scanning for devices with prefix: {{ info.ssidPrefix }}</p>
      <button @click="startScan" :disabled="scanning">
        {{ scanning ? 'Scanning...' : 'Start Scan' }}
      </button>
      
      <div v-if="discoveredDevices.length > 0" class="device-list">
        <div v-for="dev in discoveredDevices" :key="dev.sn" class="device-item">
          <span>{{ dev.sn }}</span>
          <button @click="connect(dev)">Connect</button>
        </div>
      </div>
    </div>

    <div v-if="step === 2" class="step">
      <h3>Step 2: Secure Handshake</h3>
      <p>Exchanging keys with {{ selectedDevice.sn }}...</p>
      <div class="log">
        <p v-for="(log, i) in logs" :key="i">{{ log }}</p>
      </div>
    </div>

    <div v-if="step === 3" class="step">
      <h3>Success!</h3>
      <p>Device connected securely.</p>
      <p>Session Token: {{ sessionToken }}</p>
      <button @click="reset">Provision Another</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { deviceApi } from '../../api/device'

const step = ref(1)
const info = ref({})
const scanning = ref(false)
const discoveredDevices = ref([])
const selectedDevice = ref(null)
const logs = ref([])
const sessionToken = ref('')

onMounted(async () => {
  try {
    info.value = await deviceApi.getProvisioningInfo()
  } catch (e) {
    console.error(e)
  }
})

const startScan = () => {
  scanning.value = true
  // Mock Scan
  setTimeout(() => {
    discoveredDevices.value = [
      { sn: 'SMART_MOLD_001', publicKey: 'MOCK_KEY_1' },
      { sn: 'SMART_MOLD_002', publicKey: 'MOCK_KEY_2' }
    ]
    scanning.value = false
  }, 2000)
}

const connect = async (dev) => {
  selectedDevice.value = dev
  step.value = 2
  logs.value.push('Initiating connection...')
  
  try {
    logs.value.push('Sending Public Key...')
    const res = await deviceApi.exchangeKeys({
      deviceSn: dev.sn,
      devicePublicKey: dev.publicKey
    })
    logs.value.push('Received Server Key: ' + res.serverPublicKey.substring(0, 10) + '...')
    logs.value.push('Handshake Successful!')
    sessionToken.value = res.sessionToken
    
    setTimeout(() => {
      step.value = 3
    }, 1000)
  } catch (e) {
    logs.value.push('Error: ' + e.message)
  }
}

const reset = () => {
  step.value = 1
  discoveredDevices.value = []
  logs.value = []
}
</script>

<style scoped>
.provisioning { padding: 20px; }
.step { margin-top: 20px; padding: 20px; border: 1px solid #eee; border-radius: 8px; }
.device-item { display: flex; justify-content: space-between; padding: 10px; border-bottom: 1px solid #f0f0f0; }
.log { background: #f5f5f5; padding: 10px; font-family: monospace; margin-top: 10px; }
</style>
