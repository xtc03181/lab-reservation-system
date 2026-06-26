<template>
  <div>
    <div class="toolbar">
      <h2 class="page-title">{{ isStudent ? '我的预约' : '实验室预约审核' }}</h2>
      <div class="toolbar-actions">
        <el-radio-group v-model="viewMode">
          <el-radio-button label="table">表格</el-radio-button>
          <el-radio-button label="calendar">日历</el-radio-button>
        </el-radio-group>
        <el-select v-model="statusFilter" clearable placeholder="按状态筛选" style="width: 160px">
          <el-option label="待审核" value="PENDING" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已驳回" value="REJECTED" />
          <el-option label="已取消" value="CANCELED" />
        </el-select>
        <el-button @click="exportReservations">导出 Excel</el-button>
        <el-button v-if="isStudent" type="primary" @click="openDialog()">新增预约</el-button>
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
      <div v-if="viewMode === 'calendar'" class="calendar-tools">
        <el-button @click="changeMonth(-1)">上个月</el-button>
        <strong>{{ calendarTitle }}</strong>
        <el-button @click="changeMonth(1)">下个月</el-button>
      </div>
      <div v-if="viewMode === 'calendar'" class="reservation-calendar">
        <div v-for="day in weekDays" :key="day" class="calendar-head">{{ day }}</div>
        <div v-for="day in calendarDays" :key="day.key" class="calendar-cell" :class="{ muted: !day.currentMonth }">
          <div class="calendar-date">{{ day.date.getDate() }}</div>
          <div
            v-for="item in day.items"
            :key="item.id"
            class="calendar-event"
            :class="item.status.toLowerCase()"
            :title="`${labName(item.labId)} ${item.startTime} - ${item.endTime}`"
          >
            {{ timeOnly(item.startTime) }} {{ labName(item.labId) }}
          </div>
        </div>
      </div>
      <el-table v-else :data="pagedRows" border>
        <el-table-column label="申请人">
          <template #default="{ row }">{{ userName(row.userId) }}</template>
        </el-table-column>
        <el-table-column label="实验室">
          <template #default="{ row }">{{ labName(row.labId) }}</template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
        <el-table-column prop="purpose" label="用途" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="isStudent" label="操作" width="170">
          <template #default="{ row }">
            <el-button size="small" :disabled="row.status !== 'PENDING'" @click="openDialog(row)">修改</el-button>
            <el-button size="small" type="warning" :disabled="row.status !== 'PENDING'" @click="cancel(row)">取消</el-button>
          </template>
        </el-table-column>
        <el-table-column v-if="canReview" label="审核操作" width="170">
          <template #default="{ row }">
            <el-button size="small" type="success" :disabled="row.status !== 'PENDING'" @click="openReview(row, 'APPROVED')">通过</el-button>
            <el-button size="small" type="danger" :disabled="row.status !== 'PENDING'" @click="openReview(row, 'REJECTED')">驳回</el-button>
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

    <el-dialog v-model="visible" :title="form.id ? '修改实验室预约' : '新增实验室预约'" width="560px">
      <el-form :model="form" label-width="96px">
        <el-form-item label="申请人"><el-input :model-value="userName(userStore.user?.id)" disabled /></el-form-item>
        <el-form-item label="实验室">
          <el-select v-model="form.labId" placeholder="请选择实验室" style="width: 100%">
            <el-option v-for="item in labs" :key="item.id" :label="`${item.name}（${item.location || '未填位置'}）`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间"><el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" /></el-form-item>
        <el-form-item label="结束时间"><el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" /></el-form-item>
        <el-form-item label="用途"><el-input v-model.trim="form.purpose" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="save">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="reviewVisible" title="填写审核意见" width="460px">
      <el-input v-model="reviewForm.reviewOpinion" type="textarea" :rows="4" placeholder="请输入审核意见" />
      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  cancelLabReservation,
  createLabReservation,
  listLabReservations,
  listLabs,
  listUsers,
  reviewLabReservation,
  updateLabReservation
} from '../api/modules'
import { useUserStore } from '../stores/user'
import { exportCsv } from '../utils/exportCsv'

const userStore = useUserStore()
const isStudent = computed(() => userStore.user?.role === 'STUDENT')
const canReview = computed(() => userStore.user?.role === 'TEACHER' || userStore.user?.role === 'ADMIN')
const rows = ref([])
const labs = ref([])
const users = ref([])
const statusFilter = ref('')
const viewMode = ref('table')
const visible = ref(false)
const reviewVisible = ref(false)
const page = ref(1)
const pageSize = ref(8)
const calendarDate = ref(new Date())
const form = reactive({ id: null, labId: null, startTime: '', endTime: '', purpose: '' })
const reviewForm = reactive({ id: null, status: '', reviewOpinion: '' })

const statusText = status => ({ PENDING: '待审核', APPROVED: '已通过', REJECTED: '已驳回', CANCELED: '已取消' }[status] || status)
const statusType = status => ({ PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger', CANCELED: 'info' }[status] || 'info')
const labName = id => labs.value.find(item => item.id === id)?.name || `#${id}`
const userName = id => {
  if (id === userStore.user?.id) return userStore.user.realName || userStore.user.username
  const user = users.value.find(item => item.id === id)
  return user?.realName || user?.username || `#${id}`
}
const filteredRows = computed(() => statusFilter.value ? rows.value.filter(row => row.status === statusFilter.value) : rows.value)
const pagedRows = computed(() => filteredRows.value.slice((page.value - 1) * pageSize.value, page.value * pageSize.value))
const pendingCount = computed(() => rows.value.filter(row => row.status === 'PENDING').length)
const resultCount = computed(() => rows.value.filter(row => ['APPROVED', 'REJECTED'].includes(row.status)).length)
const noticeText = computed(() => {
  if (canReview.value && pendingCount.value) return `当前有 ${pendingCount.value} 条实验室预约待审核`
  if (isStudent.value && resultCount.value) return `你有 ${resultCount.value} 条实验室预约已有审核结果`
  return ''
})
const weekDays = ['日', '一', '二', '三', '四', '五', '六']
const calendarTitle = computed(() => `${calendarDate.value.getFullYear()} 年 ${calendarDate.value.getMonth() + 1} 月`)
const calendarDays = computed(() => {
  const base = calendarDate.value
  const first = new Date(base.getFullYear(), base.getMonth(), 1)
  const start = new Date(first)
  start.setDate(first.getDate() - first.getDay())
  return Array.from({ length: 42 }, (_, index) => {
    const date = new Date(start)
    date.setDate(start.getDate() + index)
    const dateText = formatDate(date)
    return {
      key: dateText,
      date,
      currentMonth: date.getMonth() === base.getMonth(),
      items: filteredRows.value.filter(row => String(row.startTime || '').slice(0, 10) === dateText)
    }
  })
})

const formatDate = date => `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
const timeOnly = value => String(value || '').slice(11, 16)
const changeMonth = offset => {
  calendarDate.value = new Date(calendarDate.value.getFullYear(), calendarDate.value.getMonth() + offset, 1)
}

const load = async () => {
  const tasks = [listLabReservations(), listLabs()]
  if (userStore.user?.role !== 'STUDENT') tasks.push(listUsers())
  const [reservationData, labData, userData = []] = await Promise.all(tasks)
  rows.value = reservationData
  labs.value = labData
  users.value = userData
}

const openDialog = row => {
  Object.assign(form, row ? {
    id: row.id,
    labId: row.labId,
    startTime: row.startTime,
    endTime: row.endTime,
    purpose: row.purpose
  } : { id: null, labId: labs.value[0]?.id || null, startTime: '', endTime: '', purpose: '' })
  visible.value = true
}

const save = async () => {
  if (!form.labId || !form.startTime || !form.endTime || !form.purpose) {
    ElMessage.warning('请完整填写预约信息')
    return
  }
  const payload = { labId: form.labId, startTime: form.startTime, endTime: form.endTime, purpose: form.purpose }
  form.id ? await updateLabReservation(form.id, payload) : await createLabReservation(payload)
  ElMessage.success(form.id ? '修改成功' : '提交成功')
  visible.value = false
  load()
}

const cancel = async row => {
  await ElMessageBox.confirm('确定取消这条预约申请吗？', '取消确认', { type: 'warning' })
  await cancelLabReservation(row.id)
  ElMessage.success('已取消')
  load()
}

const openReview = (row, status) => {
  Object.assign(reviewForm, { id: row.id, status, reviewOpinion: status === 'APPROVED' ? '同意预约' : '预约条件不符合要求' })
  reviewVisible.value = true
}

const submitReview = async () => {
  if (!reviewForm.reviewOpinion) {
    ElMessage.warning('请填写审核意见')
    return
  }
  await reviewLabReservation(reviewForm.id, { status: reviewForm.status, reviewOpinion: reviewForm.reviewOpinion })
  ElMessage.success('处理完成')
  reviewVisible.value = false
  load()
}

const exportReservations = () => {
  exportCsv('实验室预约记录.csv', [
    { label: '申请人', value: row => userName(row.userId) },
    { label: '实验室', value: row => labName(row.labId) },
    { label: '开始时间', value: 'startTime' },
    { label: '结束时间', value: 'endTime' },
    { label: '用途', value: 'purpose' },
    { label: '状态', value: row => statusText(row.status) },
    { label: '审核意见', value: 'reviewOpinion' }
  ], filteredRows.value)
}

onMounted(load)
</script>

<style scoped>
.toolbar-actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.pagination {
  margin-top: 14px;
  justify-content: flex-end;
}

.page-alert {
  margin-bottom: 14px;
}

.calendar-tools {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-bottom: 14px;
}

.reservation-calendar {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  border-top: 1px solid #e5e7eb;
  border-left: 1px solid #e5e7eb;
}

.calendar-head,
.calendar-cell {
  border-right: 1px solid #e5e7eb;
  border-bottom: 1px solid #e5e7eb;
}

.calendar-head {
  padding: 10px;
  color: #64748b;
  font-weight: 700;
  text-align: center;
  background: #f8fafc;
}

.calendar-cell {
  min-height: 118px;
  padding: 8px;
  background: #ffffff;
}

.calendar-cell.muted {
  background: #f9fafb;
}

.calendar-date {
  margin-bottom: 6px;
  color: #111827;
  font-weight: 700;
}

.calendar-cell.muted .calendar-date {
  color: #9ca3af;
}

.calendar-event {
  overflow: hidden;
  margin-bottom: 5px;
  padding: 4px 6px;
  color: #1f2937;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
  background: #fef3c7;
  border-left: 3px solid #f59e0b;
  border-radius: 4px;
}

.calendar-event.approved {
  background: #dcfce7;
  border-left-color: #22c55e;
}

.calendar-event.rejected {
  background: #fee2e2;
  border-left-color: #ef4444;
}

.calendar-event.canceled {
  background: #f3f4f6;
  border-left-color: #9ca3af;
}
</style>
