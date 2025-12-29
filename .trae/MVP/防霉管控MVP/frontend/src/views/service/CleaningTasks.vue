<template>
  <div class="cleaning-tasks">
    <h2>Cleaning Tasks</h2>

    <div class="actions">
      <div class="create-form">
        <h3>Create New Task</h3>
        <input v-model="newTask.deviceId" placeholder="Device ID" type="number" />
        <input v-model="newTask.description" placeholder="Description" />
        <input v-model="newTask.assignee" placeholder="Assignee (Optional)" />
        <button @click="createTask">Create</button>
      </div>

      <div class="batch-assign">
        <h3>Batch Assign</h3>
        <input v-model="batchAssignee" placeholder="Assignee Name" />
        <button @click="batchAssign">Assign Selected</button>
      </div>
    </div>

    <div class="task-list">
      <h3>My Tasks ({{ tasks.length }})</h3>
      <div class="filter">
         <input v-model="currentUser" placeholder="View as User..." @change="fetchTasks" />
      </div>
      
      <div v-for="task in tasks" :key="task.id" class="task-item">
        <input type="checkbox" :value="task.id" v-model="selectedTasks" />
        <div class="task-info">
          <span class="id">#{{ task.id }}</span>
          <span class="type">{{ task.type }}</span>
          <span class="status" :class="task.status">{{ task.status }}</span>
          <p>{{ task.description }}</p>
          <small>Device: {{ task.deviceId }} | Assignee: {{ task.assignee }}</small>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { deviceApi } from '../../api/device'

const tasks = ref([])
const currentUser = ref('cleaner01') // Default mock user
const newTask = ref({ deviceId: '', description: '', assignee: '' })
const batchAssignee = ref('')
const selectedTasks = ref([])

const fetchTasks = async () => {
  try {
    tasks.value = await deviceApi.getAssignedWorkOrders(currentUser.value)
  } catch (e) {
    console.error(e)
  }
}

const createTask = async () => {
  try {
    await deviceApi.createWorkOrder({
      deviceId: newTask.value.deviceId,
      type: 'cleaning',
      description: newTask.value.description,
      assignee: newTask.value.assignee || null
    })
    newTask.value = { deviceId: '', description: '', assignee: '' }
    fetchTasks()
  } catch (e) {
    alert('Failed to create task')
  }
}

const batchAssign = async () => {
  if (selectedTasks.value.length === 0 || !batchAssignee.value) return
  try {
    await deviceApi.batchAssignWorkOrders({
      workOrderIds: selectedTasks.value,
      assignee: batchAssignee.value
    })
    selectedTasks.value = []
    fetchTasks()
    alert('Batch assign successful')
  } catch (e) {
    alert('Failed to batch assign')
  }
}

onMounted(fetchTasks)
</script>

<style scoped>
.cleaning-tasks { padding: 20px; }
.actions { display: flex; gap: 20px; margin-bottom: 20px; }
.create-form, .batch-assign { border: 1px solid #eee; padding: 15px; border-radius: 8px; flex: 1; }
input { display: block; margin-bottom: 10px; width: 100%; padding: 8px; }
button { background: #1890ff; color: white; border: none; padding: 8px 16px; border-radius: 4px; }

.task-item { display: flex; gap: 10px; border-bottom: 1px solid #f0f0f0; padding: 10px; align-items: flex-start; }
.task-info { flex: 1; }
.status.pending { color: orange; }
.status.assigned { color: blue; }
.status.closed { color: green; }
</style>
