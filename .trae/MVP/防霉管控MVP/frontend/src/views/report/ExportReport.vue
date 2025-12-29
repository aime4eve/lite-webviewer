<template>
  <div class="export-report">
    <h2>Report Export</h2>
    
    <div class="date-picker">
      <label>Select Date:</label>
      <input type="date" v-model="selectedDate" />
    </div>

    <div class="preview" v-if="report">
      <h3>Report Preview ({{ report.reportDate }})</h3>
      <div class="stats">
        <div class="stat-item">
          <span>Total Risks</span>
          <strong>{{ report.totalRisksDetected }}</strong>
        </div>
        <div class="stat-item critical">
          <span>Critical</span>
          <strong>{{ report.criticalRisks }}</strong>
        </div>
        <div class="stat-item warning">
          <span>Warning</span>
          <strong>{{ report.warningRisks }}</strong>
        </div>
      </div>
    </div>

    <div class="actions">
      <button @click="generate" class="btn-generate">Generate Preview</button>
      <button @click="download" class="btn-download" :disabled="!report">Download PDF</button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { getDailyReport, exportReportPdf } from '../../api/report'

const selectedDate = ref(new Date().toISOString().split('T')[0])
const report = ref(null)

const generate = async () => {
  try {
    report.value = await getDailyReport(selectedDate.value)
  } catch (e) {
    alert('Failed to generate report')
  }
}

const download = async () => {
  try {
    const blob = await exportReportPdf(selectedDate.value)
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `report-${selectedDate.value}.pdf`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  } catch (e) {
    alert('Failed to download PDF')
  }
}
</script>

<style scoped>
.export-report { padding: 20px; max-width: 600px; margin: 0 auto; }
.date-picker { margin-bottom: 20px; }
.stats { display: flex; gap: 20px; margin: 20px 0; }
.stat-item { border: 1px solid #eee; padding: 15px; border-radius: 8px; flex: 1; text-align: center; }
.stat-item.critical { border-color: red; color: red; }
.stat-item.warning { border-color: orange; color: orange; }
.actions { display: flex; gap: 10px; }
button { padding: 10px 20px; cursor: pointer; border: none; border-radius: 4px; color: white; }
.btn-generate { background: #1890ff; }
.btn-download { background: #52c41a; }
button:disabled { background: #ccc; cursor: not-allowed; }
</style>
