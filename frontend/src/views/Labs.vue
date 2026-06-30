<template>
  <div>
    <div class="toolbar">
      <h2 class="page-title">实验室信息</h2>
      <div class="toolbar-actions">
        <el-input v-model="keyword" placeholder="搜索实验室名称/位置" clearable style="width: 240px" />
        <el-button v-if="isAdmin" type="primary" @click="openDialog()">新增实验室</el-button>
      </div>
    </div>
    <div class="panel">
      <div class="table-summary">
        <span>共 <strong>{{ filteredRows.length }}</strong> 间实验室</span>
        <span>开放 <strong>{{ filteredRows.filter(row => row.status === 1).length }}</strong> 间</span>
        <span>停用 <strong>{{ filteredRows.filter(row => row.status !== 1).length }}</strong> 间</span>
      </div>
      <el-table :data="pagedRows" border stripe empty-text="暂无实验室数据">
        <el-table-column prop="name" label="实验室名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="location" label="位置" min-width="120" show-overflow-tooltip />
        <el-table-column prop="capacity" label="容量" width="90" />
        <el-table-column prop="manager" label="负责人" min-width="110" show-overflow-tooltip />
        <el-table-column label="开放星期" width="180">
          <template #default="{ row }">{{ openDayText(row.openDays) }}</template>
        </el-table-column>
        <el-table-column label="开放时间" width="150">
          <template #default="{ row }">{{ timeRangeText(row) }}</template>
        </el-table-column>
        <el-table-column prop="description" label="介绍" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '开放' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="isAdmin" label="操作" width="170">
          <template #default="{ row }">
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="remove(row.id)">删除</el-button>
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
    <el-dialog v-model="visible" title="实验室信息" width="560px">
      <el-form :model="form" label-width="92px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="位置"><el-input v-model="form.location" /></el-form-item>
        <el-form-item label="容量"><el-input-number v-model="form.capacity" :min="1" /></el-form-item>
        <el-form-item label="负责人"><el-input v-model="form.manager" /></el-form-item>
        <el-form-item label="开放星期">
          <el-checkbox-group v-model="form.openDayValues">
            <el-checkbox v-for="item in dayOptions" :key="item.value" :label="item.value">{{ item.label }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="开放时间">
          <div class="time-range">
            <el-time-picker
              v-model="form.openStartTime"
              placeholder="开始时间"
              value-format="HH:mm:ss"
              format="HH:mm"
            />
            <span>至</span>
            <el-time-picker
              v-model="form.openEndTime"
              placeholder="结束时间"
              value-format="HH:mm:ss"
              format="HH:mm"
            />
          </div>
        </el-form-item>
        <el-form-item label="介绍"><el-input v-model="form.description" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createLab, deleteLab, listLabs, updateLab } from '../api/modules'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()
const isAdmin = computed(() => userStore.user?.role === 'ADMIN')
const rows = ref([])
const keyword = ref('')
const page = ref(1)
const pageSize = ref(8)
const visible = ref(false)
const dayOptions = [
  { label: '周一', value: 1 },
  { label: '周二', value: 2 },
  { label: '周三', value: 3 },
  { label: '周四', value: 4 },
  { label: '周五', value: 5 },
  { label: '周六', value: 6 },
  { label: '周日', value: 7 }
]
const defaultForm = () => ({
  id: null,
  name: '',
  location: '',
  capacity: 30,
  manager: '',
  description: '',
  openDayValues: [1, 2, 3, 4, 5],
  openStartTime: '08:00:00',
  openEndTime: '18:00:00',
  status: 1
})
const form = reactive(defaultForm())

const filteredRows = computed(() => {
  const value = keyword.value.trim()
  if (!value) return rows.value
  return rows.value.filter(row => `${row.name || ''}${row.location || ''}${row.manager || ''}`.includes(value))
})
const pagedRows = computed(() => filteredRows.value.slice((page.value - 1) * pageSize.value, page.value * pageSize.value))

const load = async () => {
  rows.value = await listLabs()
}

const parseOpenDays = value => String(value || '1,2,3,4,5').split(',').map(item => Number(item)).filter(Boolean)
const openDayText = value => {
  const values = parseOpenDays(value)
  if (values.length === 5 && values.every(item => item >= 1 && item <= 5)) return '周一至周五'
  if (values.length === 7) return '每天'
  return values.map(value => dayOptions.find(item => item.value === value)?.label).filter(Boolean).join('、')
}
const shortTime = value => String(value || '').slice(0, 5)
const timeRangeText = row => `${shortTime(row.openStartTime || '08:00:00')}-${shortTime(row.openEndTime || '18:00:00')}`

const openDialog = row => {
  Object.assign(form, row ? {
    ...row,
    openDayValues: parseOpenDays(row.openDays),
    openStartTime: row.openStartTime || '08:00:00',
    openEndTime: row.openEndTime || '18:00:00'
  } : defaultForm())
  visible.value = true
}

const save = async () => {
  if (!form.name || !form.location || !form.manager) {
    ElMessage.warning('请填写名称、位置和负责人')
    return
  }
  if (!form.openDayValues.length || !form.openStartTime || !form.openEndTime) {
    ElMessage.warning('请设置开放星期和开放时间')
    return
  }
  if (form.openEndTime <= form.openStartTime) {
    ElMessage.warning('结束时间必须晚于开始时间')
    return
  }
  const payload = {
    ...form,
    openDays: form.openDayValues.join(',')
  }
  delete payload.openDayValues
  form.id ? await updateLab(form.id, payload) : await createLab(payload)
  ElMessage.success('保存成功')
  visible.value = false
  load()
}

const remove = async id => {
  await deleteLab(id)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>

<style scoped>
.time-range {
  display: flex;
  align-items: center;
  gap: 10px;
}
</style>
