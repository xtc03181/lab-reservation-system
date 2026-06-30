<template>
  <div class="dashboard">
    <section class="hero-panel">
      <div>
        <p class="eyebrow">Workbench</p>
        <h2>首页统计</h2>
        <p class="hero-desc">集中查看实验室预约、设备借用、公告和待处理审核情况。</p>
      </div>
      <div class="hero-user">
        <span>{{ roleText(userStore.user?.role) }}</span>
        <strong>{{ userStore.user?.realName || userStore.user?.username }}</strong>
      </div>
    </section>

    <section class="stat-grid">
      <div v-for="item in cards" :key="item.title" class="stat-card">
        <div class="stat-icon" :class="item.theme">{{ item.icon }}</div>
        <div>
          <div class="stat-title">{{ item.title }}</div>
          <div class="stat-value">{{ item.value }}</div>
          <div class="stat-note">{{ item.note }}</div>
        </div>
      </div>
    </section>

    <section class="content-grid">
      <div class="panel chart-panel">
        <div class="panel-head">
          <h3>申请状态统计</h3>
          <span>预约 + 借用</span>
        </div>
        <div class="donut-row">
          <div class="donut" :style="donutStyle">
            <div class="donut-inner">
              <strong>{{ totalApply }}</strong>
              <span>总申请</span>
            </div>
          </div>
          <div class="legend-list">
            <div v-for="item in statusBars" :key="item.title" class="legend-item">
              <i :style="{ background: item.color }"></i>
              <span>{{ item.title }}</span>
              <strong>{{ item.value }}</strong>
            </div>
          </div>
        </div>
      </div>

      <div class="panel chart-panel">
        <div class="panel-head">
          <h3>最近 7 天预约趋势</h3>
          <span>实验室预约</span>
        </div>
        <div class="trend-chart">
          <div v-for="item in trendBars" :key="item.label" class="trend-item">
            <div class="trend-track">
              <div class="trend-fill" :style="{ height: item.percent + '%' }"></div>
            </div>
            <span>{{ item.label }}</span>
          </div>
        </div>
      </div>
    </section>

    <section class="content-grid lower">
      <div class="panel">
        <div class="panel-head">
          <h3>设备库存概览</h3>
          <span>可借 / 总数</span>
        </div>
        <div class="bar-list">
          <div v-for="item in equipmentBars" :key="item.name" class="bar-line">
            <span>{{ item.name }}</span>
            <div class="bar-track">
              <div class="bar-fill stock" :style="{ width: item.percent + '%' }"></div>
            </div>
            <strong>{{ item.available }}/{{ item.total }}</strong>
          </div>
          <el-empty v-if="!equipmentBars.length" description="暂无设备数据" :image-size="80" />
        </div>
      </div>

      <div class="panel">
        <div class="panel-head">
          <h3>待处理事项</h3>
          <span>{{ pendingItems.length }} 条</span>
        </div>
        <div class="todo-list">
          <div v-for="item in pendingItems" :key="item.key" class="todo-item">
            <div>
              <strong>{{ item.title }}</strong>
              <span>{{ item.desc }}</span>
            </div>
            <el-tag type="warning">待审核</el-tag>
          </div>
          <el-empty v-if="!pendingItems.length" description="暂无待处理事项" :image-size="80" />
        </div>
      </div>
    </section>

    <section class="panel notice-panel">
      <div class="panel-head">
        <h3>最新公告</h3>
        <span>置顶优先显示</span>
      </div>
      <div class="notice-list">
        <div v-for="item in latestNotices" :key="item.id" class="notice-item clickable" @click="openNotice(item)">
          <el-tag v-if="item.status === 2" type="warning">置顶</el-tag>
          <el-tag v-else type="success">公告</el-tag>
          <div>
            <strong>{{ item.title }}</strong>
            <p>{{ item.content }}</p>
          </div>
          <time>{{ item.createTime }}</time>
        </div>
        <el-empty v-if="!latestNotices.length" description="暂无公告" :image-size="80" />
      </div>
    </section>

    <el-dialog v-model="noticeVisible" title="公告详情" width="680px">
      <div v-if="activeNotice" class="notice-detail">
        <div class="notice-detail-head">
          <el-tag v-if="activeNotice.status === 2" type="warning">置顶</el-tag>
          <el-tag v-else type="success">公告</el-tag>
          <span>{{ activeNotice.createTime }}</span>
        </div>
        <h3>{{ activeNotice.title }}</h3>
        <div class="notice-detail-content">{{ activeNotice.content }}</div>
      </div>
      <template #footer>
        <el-button type="primary" @click="noticeVisible = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { listEquipment, listEquipmentBorrows, listLabReservations, listLabs, listNotices } from '../api/modules'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()
const labs = ref([])
const equipment = ref([])
const reservations = ref([])
const borrows = ref([])
const notices = ref([])
const noticeVisible = ref(false)
const activeNotice = ref(null)

const text = {
  admin: '\u7cfb\u7edf\u7ba1\u7406\u5458',
  teacher: '\u6559\u5e08\u5ba1\u6838\u5458',
  student: '\u5b66\u751f\u7528\u6237',
  user: '\u7528\u6237',
  labTotal: '\u5b9e\u9a8c\u5ba4\u603b\u6570',
  labNote: '\u53ef\u9884\u7ea6\u5b9e\u9a8c\u7a7a\u95f4',
  equipmentAvailable: '\u8bbe\u5907\u53ef\u501f\u6570\u91cf',
  equipmentNote: '\u5f53\u524d\u53ef\u501f\u5e93\u5b58',
  pendingApply: '\u5f85\u5ba1\u6838\u7533\u8bf7',
  pendingNote: '\u9700\u8981\u53ca\u65f6\u5904\u7406',
  passRate: '\u5ba1\u6838\u901a\u8fc7\u7387',
  passRateNote: '\u542b\u5df2\u5f52\u8fd8\u8bb0\u5f55',
  pending: '\u5f85\u5ba1\u6838',
  approved: '\u5df2\u901a\u8fc7',
  rejected: '\u5df2\u9a73\u56de',
  returnedCanceled: '\u5df2\u5f52\u8fd8/\u53d6\u6d88',
  labTodo: '\u5b9e\u9a8c\u5ba4\u9884\u7ea6\u5f85\u5ba1\u6838',
  borrowTodo: '\u8bbe\u5907\u501f\u7528\u5f85\u5ba1\u6838',
  roomIcon: '\u5ba4',
  equipmentIcon: '\u8bbe',
  reviewIcon: '\u5ba1',
  rateIcon: '\u7387',
  to: '\u81f3',
  count: '\u6570\u91cf'
}

const roleText = role => ({ ADMIN: text.admin, TEACHER: text.teacher, STUDENT: text.student }[role] || text.user)
const totalApply = computed(() => reservations.value.length + borrows.value.length)
const pendingCount = computed(() => [...reservations.value, ...borrows.value].filter(item => item.status === 'PENDING').length)
const approvedCount = computed(() => [...reservations.value, ...borrows.value].filter(item => item.status === 'APPROVED' || item.status === 'RETURNED').length)

const cards = computed(() => [
  { title: text.labTotal, value: labs.value.length, note: text.labNote, icon: text.roomIcon, theme: 'blue' },
  { title: text.equipmentAvailable, value: equipment.value.reduce((sum, item) => sum + Number(item.availableCount || 0), 0), note: text.equipmentNote, icon: text.equipmentIcon, theme: 'green' },
  { title: text.pendingApply, value: pendingCount.value, note: text.pendingNote, icon: text.reviewIcon, theme: 'orange' },
  { title: text.passRate, value: totalApply.value ? `${Math.round((approvedCount.value / totalApply.value) * 100)}%` : '0%', note: text.passRateNote, icon: text.rateIcon, theme: 'red' }
])

const statusBars = computed(() => {
  const all = [...reservations.value, ...borrows.value]
  return [
    { title: text.pending, value: all.filter(item => item.status === 'PENDING').length, color: '#f59e0b' },
    { title: text.approved, value: all.filter(item => item.status === 'APPROVED').length, color: '#2563eb' },
    { title: text.rejected, value: all.filter(item => item.status === 'REJECTED').length, color: '#ef4444' },
    { title: text.returnedCanceled, value: all.filter(item => item.status === 'RETURNED' || item.status === 'CANCELED').length, color: '#10b981' }
  ]
})

const donutStyle = computed(() => {
  const total = Math.max(totalApply.value, 1)
  let start = 0
  const segments = statusBars.value.map(item => {
    const end = start + (item.value / total) * 100
    const part = `${item.color} ${start}% ${end}%`
    start = end
    return part
  })
  return { background: `conic-gradient(${segments.join(', ')})` }
})

const equipmentBars = computed(() => equipment.value.slice(0, 6).map(item => ({
  name: item.name,
  available: Number(item.availableCount || 0),
  total: Number(item.totalCount || 0),
  percent: item.totalCount ? Math.round((Number(item.availableCount || 0) / Number(item.totalCount)) * 100) : 0
})))

const trendBars = computed(() => {
  const days = Array.from({ length: 7 }, (_, index) => {
    const date = new Date()
    date.setDate(date.getDate() - (6 - index))
    const key = date.toISOString().slice(0, 10)
    return { key, label: `${date.getMonth() + 1}/${date.getDate()}`, value: 0 }
  })
  reservations.value.forEach(item => {
    const key = String(item.createTime || item.startTime || '').slice(0, 10)
    const target = days.find(day => day.key === key)
    if (target) target.value += 1
  })
  const max = Math.max(...days.map(item => item.value), 1)
  return days.map(item => ({ ...item, percent: Math.max(8, Math.round((item.value / max) * 100)) }))
})

const pendingItems = computed(() => {
  const reservationItems = reservations.value
    .filter(item => item.status === 'PENDING')
    .slice(0, 3)
    .map(item => ({ key: `r-${item.id}`, title: text.labTodo, desc: `${item.startTime || ''} ${text.to} ${item.endTime || ''}` }))
  const borrowItems = borrows.value
    .filter(item => item.status === 'PENDING')
    .slice(0, 3)
    .map(item => ({ key: `b-${item.id}`, title: text.borrowTodo, desc: `${item.borrowTime || ''}\uff0c${text.count} ${item.borrowCount || 0}` }))
  return [...reservationItems, ...borrowItems].slice(0, 5)
})

const latestNotices = computed(() => [...notices.value]
  .sort((a, b) => (b.status || 0) - (a.status || 0) || (b.id || 0) - (a.id || 0))
  .slice(0, 4))

const openNotice = notice => {
  activeNotice.value = notice
  noticeVisible.value = true
}

onMounted(async () => {
  const [labList, equipmentList, reservationList, borrowList, noticeList] = await Promise.all([
    listLabs(),
    listEquipment(),
    listLabReservations(),
    listEquipmentBorrows(),
    listNotices()
  ])
  labs.value = labList
  equipment.value = equipmentList
  reservations.value = reservationList
  borrows.value = borrowList
  notices.value = noticeList
})
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.hero-panel {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px;
  color: #ffffff;
  background: linear-gradient(135deg, #1d4ed8, #2563eb 58%, #0f766e);
  border-radius: 8px;
}

.eyebrow {
  margin: 0 0 8px;
  color: rgba(255, 255, 255, 0.78);
  font-size: 13px;
  text-transform: uppercase;
}

.hero-panel h2 {
  margin: 0;
  font-size: 28px;
}

.hero-desc {
  margin: 10px 0 0;
  color: rgba(255, 255, 255, 0.86);
}

.hero-user {
  display: flex;
  min-width: 180px;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
}

.hero-user span {
  color: rgba(255, 255, 255, 0.76);
}

.hero-user strong {
  font-size: 22px;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.stat-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 16px;
  min-height: 118px;
  padding: 20px;
  background: #ffffff;
  border: 1px solid #e3eaf3;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.04);
  transition: border-color 0.18s ease, box-shadow 0.18s ease, transform 0.18s ease;
}

.stat-card::before {
  position: absolute;
  top: 0;
  right: 0;
  left: 0;
  height: 3px;
  content: "";
  background: linear-gradient(90deg, #2563eb, #0f766e);
  border-radius: 8px 8px 0 0;
}

.stat-card:hover {
  border-color: #cbdaf0;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.08);
  transform: translateY(-1px);
}

.stat-icon {
  display: grid;
  width: 54px;
  height: 54px;
  flex: 0 0 auto;
  place-items: center;
  font-size: 18px;
  font-weight: 800;
  border-radius: 12px;
}

.stat-icon.blue {
  color: #1d4ed8;
  background: #dbeafe;
}

.stat-icon.green {
  color: #047857;
  background: #d1fae5;
}

.stat-icon.orange {
  color: #b45309;
  background: #fef3c7;
}

.stat-icon.red {
  color: #b91c1c;
  background: #fee2e2;
}

.stat-title {
  color: #6b7280;
  font-size: 14px;
}

.stat-value {
  margin-top: 6px;
  color: #111827;
  font-size: 30px;
  font-weight: 800;
  line-height: 1.1;
}

.stat-note {
  margin-top: 4px;
  color: #9ca3af;
  font-size: 13px;
}

.content-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 18px;
}

.panel-head h3 {
  margin: 0;
  color: #111827;
  font-size: 18px;
}

.panel-head span {
  color: #6b7280;
  font-size: 13px;
}

.donut-row {
  display: grid;
  grid-template-columns: 180px 1fr;
  align-items: center;
  gap: 24px;
}

.donut {
  display: grid;
  width: 170px;
  height: 170px;
  place-items: center;
  border-radius: 50%;
}

.donut-inner {
  display: grid;
  width: 106px;
  height: 106px;
  place-items: center;
  background: #ffffff;
  border-radius: 50%;
  box-shadow: inset 0 0 0 1px #e5e7eb;
}

.donut-inner strong {
  color: #111827;
  font-size: 28px;
}

.donut-inner span {
  margin-top: -22px;
  color: #6b7280;
  font-size: 13px;
}

.legend-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.legend-item {
  display: grid;
  grid-template-columns: 12px 1fr auto;
  align-items: center;
  gap: 10px;
}

.legend-item i {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.trend-chart {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  align-items: end;
  gap: 12px;
  height: 210px;
}

.trend-item {
  display: grid;
  height: 100%;
  grid-template-rows: 1fr auto;
  gap: 8px;
  text-align: center;
}

.trend-track {
  display: flex;
  align-items: flex-end;
  justify-content: center;
  height: 100%;
  background: #f3f4f6;
  border-radius: 8px;
}

.trend-fill {
  width: 100%;
  background: linear-gradient(180deg, #38bdf8, #2563eb);
  border-radius: 8px;
}

.trend-item span {
  color: #6b7280;
  font-size: 12px;
}

.bar-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.bar-line {
  display: grid;
  grid-template-columns: 120px 1fr 70px;
  align-items: center;
  gap: 12px;
}

.bar-line span {
  overflow: hidden;
  color: #374151;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.bar-track {
  height: 12px;
  overflow: hidden;
  background: #eef2f7;
  border-radius: 999px;
}

.bar-fill {
  height: 100%;
  background: #2563eb;
}

.bar-fill.stock {
  background: #059669;
}

.todo-list,
.notice-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.todo-item,
.notice-item {
  display: grid;
  grid-template-columns: 1fr auto;
  align-items: center;
  gap: 14px;
  padding: 12px;
  background: #f9fafb;
  border: 1px solid #eef2f7;
  border-radius: 8px;
}

.notice-item.clickable {
  cursor: pointer;
  transition: border-color 0.18s ease, box-shadow 0.18s ease, transform 0.18s ease;
}

.notice-item.clickable:hover {
  border-color: #bfdbfe;
  box-shadow: 0 8px 20px rgba(37, 99, 235, 0.08);
  transform: translateY(-1px);
}

.todo-item div,
.notice-item div {
  min-width: 0;
}

.todo-item strong,
.notice-item strong {
  display: block;
  color: #111827;
}

.todo-item span,
.notice-item p,
.notice-item time {
  display: block;
  margin: 4px 0 0;
  overflow: hidden;
  color: #6b7280;
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-item {
  grid-template-columns: auto 1fr 180px;
}

.notice-detail-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
  color: #64748b;
  font-size: 13px;
}

.notice-detail h3 {
  margin: 0 0 14px;
  color: #111827;
  font-size: 20px;
}

.notice-detail-content {
  max-height: 52vh;
  overflow: auto;
  color: #334155;
  font-size: 15px;
  line-height: 1.9;
  white-space: pre-wrap;
  word-break: break-word;
}

@media (max-width: 1100px) {
  .stat-grid,
  .content-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .hero-panel,
  .donut-row,
  .stat-grid,
  .content-grid {
    grid-template-columns: 1fr;
  }

  .hero-panel {
    display: grid;
  }

  .hero-user {
    align-items: flex-start;
    margin-top: 14px;
  }

  .notice-item {
    grid-template-columns: 1fr;
  }
}
</style>
