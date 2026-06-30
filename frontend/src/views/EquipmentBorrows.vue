<template>
  <div>
    <div class="toolbar">
      <h2 class="page-title">{{ isStudent ? '我的借用' : '设备借用审核' }}</h2>
      <div class="toolbar-actions">
        <el-select v-model="statusFilter" clearable placeholder="按状态筛选" style="width: 160px">
          <el-option label="待审核" value="PENDING" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已驳回" value="REJECTED" />
          <el-option label="已归还" value="RETURNED" />
        </el-select>
        <el-button @click="exportBorrows">导出 Excel</el-button>
        <el-button v-if="isStudent" type="primary" @click="openDialog">新增借用申请</el-button>
      </div>
    </div>
    <div class="panel">
      <el-alert
        v-if="noticeText"
        class="page-alert"
        :title="noticeText"
        type="warning"
        show-icon
        :closable="false"
      />
      <div class="table-summary">
        <span>共 <strong>{{ filteredRows.length }}</strong> 条借用</span>
        <span>待审核 <strong>{{ filteredRows.filter(row => row.status === 'PENDING').length }}</strong> 条</span>
        <span>已归还 <strong>{{ filteredRows.filter(row => row.status === 'RETURNED').length }}</strong> 条</span>
      </div>
      <el-table :data="pagedRows" border stripe empty-text="暂无借用记录">
        <el-table-column label="申请人" min-width="110" show-overflow-tooltip>
          <template #default="{ row }">{{ userName(row.userId) }}</template>
        </el-table-column>
        <el-table-column label="设备" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ equipmentName(row.equipmentId) }}</template>
        </el-table-column>
        <el-table-column prop="borrowCount" label="数量" width="90" />
        <el-table-column prop="borrowTime" label="借用时间" width="180" />
        <el-table-column prop="returnTime" label="归还时间" width="180" />
        <el-table-column prop="purpose" label="用途" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="canReview" label="审核操作" width="250">
          <template #default="{ row }">
            <el-button size="small" type="success" :disabled="row.status !== 'PENDING'" @click="openReview(row, 'APPROVED')">通过</el-button>
            <el-button size="small" type="danger" :disabled="row.status !== 'PENDING'" @click="openReview(row, 'REJECTED')">驳回</el-button>
            <el-button size="small" :disabled="row.status !== 'APPROVED'" @click="openReview(row, 'RETURNED')">归还</el-button>
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

    <el-dialog v-model="visible" title="新增设备借用申请" width="560px">
      <el-form :model="form" label-width="96px">
        <el-form-item label="申请人"><el-input :model-value="userName(userStore.user?.id)" disabled /></el-form-item>
        <el-form-item label="设备">
          <el-select v-model="form.equipmentId" placeholder="请选择设备" style="width: 100%">
            <el-option
              v-for="item in equipment"
              :key="item.id"
              :label="`${item.name}（可借 ${item.availableCount}）`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="数量"><el-input-number v-model="form.borrowCount" :min="1" /></el-form-item>
        <el-form-item label="借用时间"><el-date-picker v-model="form.borrowTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="请选择借用时间" /></el-form-item>
        <el-form-item label="归还时间"><el-date-picker v-model="form.returnTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="请选择归还时间" /></el-form-item>
        <el-form-item label="用途"><el-input v-model.trim="form.purpose" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="save">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="reviewVisible" title="填写处理意见" width="460px">
      <el-input v-model="reviewForm.reviewOpinion" type="textarea" :rows="4" placeholder="请输入处理意见" />
      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createEquipmentBorrow, listEquipment, listEquipmentBorrows, listUsers, reviewEquipmentBorrow } from '../api/modules'
import { useUserStore } from '../stores/user'
import { exportCsv } from '../utils/exportCsv'

const userStore = useUserStore()
const isStudent = computed(() => userStore.user?.role === 'STUDENT')
const canReview = computed(() => userStore.user?.role === 'TEACHER' || userStore.user?.role === 'ADMIN')
const rows = ref([])
const equipment = ref([])
const users = ref([])
const statusFilter = ref('')
const visible = ref(false)
const reviewVisible = ref(false)
const page = ref(1)
const pageSize = ref(8)
const form = reactive({ equipmentId: null, borrowCount: 1, borrowTime: '', returnTime: '', purpose: '' })
const reviewForm = reactive({ id: null, status: '', reviewOpinion: '' })

const statusText = status => ({ PENDING: '待审核', APPROVED: '已通过', REJECTED: '已驳回', RETURNED: '已归还' }[status] || status)
const statusType = status => ({ PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger', RETURNED: 'info' }[status] || 'info')
const equipmentName = id => equipment.value.find(item => item.id === id)?.name || `#${id}`
const userName = id => {
  if (id === userStore.user?.id) return userStore.user.realName || userStore.user.username
  const user = users.value.find(item => item.id === id)
  return user?.realName || user?.username || `#${id}`
}
const filteredRows = computed(() => statusFilter.value ? rows.value.filter(row => row.status === statusFilter.value) : rows.value)
const pagedRows = computed(() => filteredRows.value.slice((page.value - 1) * pageSize.value, page.value * pageSize.value))
const pendingCount = computed(() => rows.value.filter(row => row.status === 'PENDING').length)
const resultCount = computed(() => rows.value.filter(row => ['APPROVED', 'REJECTED', 'RETURNED'].includes(row.status)).length)
const noticeText = computed(() => {
  if (canReview.value && pendingCount.value) return `当前有 ${pendingCount.value} 条设备借用申请待审核`
  if (isStudent.value && resultCount.value) return `你有 ${resultCount.value} 条设备借用申请已有处理结果`
  return ''
})

const load = async () => {
  const tasks = [listEquipmentBorrows(), listEquipment()]
  if (userStore.user?.role !== 'STUDENT') tasks.push(listUsers())
  const [borrowData, equipmentData, userData = []] = await Promise.all(tasks)
  rows.value = borrowData
  equipment.value = equipmentData
  users.value = userData
}

const openDialog = () => {
  Object.assign(form, { equipmentId: equipment.value[0]?.id || null, borrowCount: 1, borrowTime: '', returnTime: '', purpose: '' })
  visible.value = true
}

const save = async () => {
  if (!form.equipmentId || !form.borrowCount || !form.borrowTime || !form.returnTime || !form.purpose) {
    ElMessage.warning('请完整填写借用信息')
    return
  }
  await createEquipmentBorrow(form)
  ElMessage.success('提交成功')
  visible.value = false
  load()
}

const openReview = (row, status) => {
  const opinionMap = {
    APPROVED: '同意借用',
    REJECTED: '设备暂不可借',
    RETURNED: '设备已归还，库存恢复'
  }
  Object.assign(reviewForm, { id: row.id, status, reviewOpinion: opinionMap[status] })
  reviewVisible.value = true
}

const submitReview = async () => {
  if (!reviewForm.reviewOpinion) {
    ElMessage.warning('请填写处理意见')
    return
  }
  await reviewEquipmentBorrow(reviewForm.id, { status: reviewForm.status, reviewOpinion: reviewForm.reviewOpinion })
  ElMessage.success('处理完成')
  reviewVisible.value = false
  load()
}

const exportBorrows = () => {
  exportCsv('设备借用记录.csv', [
    { label: '申请人', value: row => userName(row.userId) },
    { label: '设备', value: row => equipmentName(row.equipmentId) },
    { label: '数量', value: 'borrowCount' },
    { label: '借用时间', value: 'borrowTime' },
    { label: '归还时间', value: 'returnTime' },
    { label: '用途', value: 'purpose' },
    { label: '状态', value: row => statusText(row.status) },
    { label: '处理意见', value: 'reviewOpinion' }
  ], filteredRows.value)
}

onMounted(load)
</script>

<style scoped>
.page-alert {
  margin-bottom: 14px;
}
</style>
