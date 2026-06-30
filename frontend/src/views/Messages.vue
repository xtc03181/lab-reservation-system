<template>
  <div>
    <div class="toolbar">
      <h2 class="page-title">消息中心</h2>
      <div class="toolbar-actions">
        <el-select v-model="readFilter" clearable placeholder="按状态筛选" style="width: 150px">
          <el-option label="未读" :value="0" />
          <el-option label="已读" :value="1" />
        </el-select>
        <el-button @click="load">刷新</el-button>
        <el-button type="primary" :disabled="!unreadRows.length" @click="readAll">全部已读</el-button>
      </div>
    </div>

    <div class="panel">
      <div class="table-summary">
        <span>共 <strong>{{ filteredRows.length }}</strong> 条消息</span>
        <span>未读 <strong>{{ filteredRows.filter(row => row.readStatus === 0).length }}</strong> 条</span>
        <span>已读 <strong>{{ filteredRows.filter(row => row.readStatus === 1).length }}</strong> 条</span>
      </div>
      <el-table :data="pagedRows" border stripe empty-text="暂无消息数据">
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.readStatus === 0 ? 'danger' : 'info'">
              {{ row.readStatus === 0 ? '未读' : '已读' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="content" label="内容" min-width="260" show-overflow-tooltip />
        <el-table-column label="类型" width="130">
          <template #default="{ row }">{{ typeText(row.type) }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="180" />
        <el-table-column label="操作" width="110">
          <template #default="{ row }">
            <el-button size="small" :disabled="row.readStatus === 1" @click="readOne(row)">标为已读</el-button>
          </template>
        </el-table-column>
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
import { ElMessage } from 'element-plus'
import { listMessages, markAllMessagesRead, markMessageRead } from '../api/modules'

const rows = ref([])
const readFilter = ref('')
const page = ref(1)
const pageSize = ref(10)

const typeText = type => ({ LAB_RESERVATION: '实验室预约', EQUIPMENT_BORROW: '设备借用' }[type] || type)
const filteredRows = computed(() => readFilter.value === '' ? rows.value : rows.value.filter(row => row.readStatus === readFilter.value))
const pagedRows = computed(() => filteredRows.value.slice((page.value - 1) * pageSize.value, page.value * pageSize.value))
const unreadRows = computed(() => rows.value.filter(row => row.readStatus === 0))

const load = async () => {
  rows.value = await listMessages()
}

const readOne = async row => {
  await markMessageRead(row.id)
  ElMessage.success('已标记为已读')
  load()
}

const readAll = async () => {
  await markAllMessagesRead()
  ElMessage.success('全部消息已读')
  load()
}

onMounted(load)
</script>

<style scoped></style>
