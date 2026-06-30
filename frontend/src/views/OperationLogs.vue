<template>
  <div>
    <div class="toolbar">
      <h2 class="page-title">操作日志</h2>
      <div class="toolbar-actions">
        <el-input v-model="keyword" placeholder="搜索模块/操作/账号/内容" clearable style="width: 280px" />
        <el-button @click="exportLogs">导出 Excel</el-button>
      </div>
    </div>
    <div class="panel">
      <div class="table-summary">
        <span>共 <strong>{{ filteredRows.length }}</strong> 条日志</span>
        <span>模块 <strong>{{ moduleCount }}</strong> 个</span>
      </div>
      <el-table :data="pagedRows" border stripe empty-text="暂无操作日志">
        <el-table-column prop="moduleName" label="模块" width="130" show-overflow-tooltip />
        <el-table-column prop="operation" label="操作" width="130" show-overflow-tooltip />
        <el-table-column prop="detail" label="内容" min-width="260" show-overflow-tooltip />
        <el-table-column prop="username" label="操作账号" width="140" show-overflow-tooltip />
        <el-table-column prop="createTime" label="操作时间" width="180" />
      </el-table>
      <el-pagination
        class="pagination"
        layout="prev, pager, next"
        :total="filteredRows.length"
        :page-size="pageSize"
        v-model:current-page="page"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { listOperationLogs } from '../api/modules'
import { exportCsv } from '../utils/exportCsv'

const rows = ref([])
const keyword = ref('')
const page = ref(1)
const pageSize = ref(10)

const filteredRows = computed(() => {
  const value = keyword.value.trim()
  if (!value) return rows.value
  return rows.value.filter(row => `${row.moduleName || ''}${row.operation || ''}${row.detail || ''}${row.username || ''}`.includes(value))
})
const pagedRows = computed(() => filteredRows.value.slice((page.value - 1) * pageSize.value, page.value * pageSize.value))
const moduleCount = computed(() => new Set(filteredRows.value.map(row => row.moduleName).filter(Boolean)).size)

const load = async () => {
  const data = await listOperationLogs()
  rows.value = [...data].sort((a, b) => (b.id || 0) - (a.id || 0))
}

const exportLogs = () => {
  exportCsv('操作日志.csv', [
    { label: '模块', value: 'moduleName' },
    { label: '操作', value: 'operation' },
    { label: '内容', value: 'detail' },
    { label: '操作账号', value: 'username' },
    { label: '操作时间', value: 'createTime' }
  ], filteredRows.value)
}

onMounted(load)
</script>

<style scoped></style>
