<template>
  <div>
    <div class="toolbar">
      <h2 class="page-title">{{ isAdmin ? '公告管理' : '公告查看' }}</h2>
      <div class="toolbar-actions">
        <el-input v-model="keyword" placeholder="搜索公告标题/内容" clearable style="width: 240px" />
        <el-button v-if="isAdmin" type="primary" @click="openDialog()">发布公告</el-button>
      </div>
    </div>
    <div class="panel">
      <div class="table-summary">
        <span>共 <strong>{{ filteredRows.length }}</strong> 条公告</span>
        <span>置顶 <strong>{{ filteredRows.filter(row => row.status === 2).length }}</strong> 条</span>
        <span>已发布 <strong>{{ filteredRows.filter(row => row.status === 1).length }}</strong> 条</span>
      </div>
      <el-table :data="pagedRows" border stripe empty-text="暂无公告数据" row-class-name="notice-row" @row-click="openViewDialog">
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <el-table-column label="内容">
          <template #default="{ row }">
            <div class="notice-content">{{ row.content }}</div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publisherId" label="发布人ID" width="110" />
        <el-table-column prop="createTime" label="发布时间" width="180" />
        <el-table-column v-if="isAdmin" label="操作" width="300">
          <template #default="{ row }">
            <el-button size="small" @click.stop="openDialog(row)">编辑</el-button>
            <el-button v-if="row.status !== 2" size="small" type="success" @click.stop="setPinned(row.id)">置顶</el-button>
            <el-button v-else size="small" @click.stop="setNormal(row.id)">取消置顶</el-button>
            <el-button v-if="row.status !== 0" size="small" type="warning" @click.stop="setOffline(row.id)">下架</el-button>
            <el-button v-else size="small" type="success" @click.stop="setNormal(row.id)">上架</el-button>
            <el-button size="small" type="danger" @click.stop="remove(row.id)">删除</el-button>
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

    <el-dialog v-model="visible" :title="form.id ? '编辑公告' : '发布公告'" width="620px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题"><el-input v-model.trim="form.title" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="8" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="save">{{ form.id ? '保存' : '发布' }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="viewVisible" title="公告详情" width="680px">
      <div v-if="activeNotice" class="notice-detail">
        <div class="notice-detail-head">
          <div class="notice-detail-meta">
            <el-tag :type="statusType(activeNotice.status)">{{ statusText(activeNotice.status) }}</el-tag>
            <span>发布人ID：{{ activeNotice.publisherId }}</span>
          </div>
          <span>{{ activeNotice.createTime }}</span>
        </div>
        <h3>{{ activeNotice.title }}</h3>
        <div class="notice-detail-content">{{ activeNotice.content }}</div>
      </div>
      <template #footer>
        <el-button type="primary" @click="viewVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createNotice, deleteNotice, listNotices, normalNotice, offlineNotice, pinNotice, updateNotice } from '../api/modules'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()
const isAdmin = computed(() => userStore.user?.role === 'ADMIN')
const rows = ref([])
const keyword = ref('')
const page = ref(1)
const pageSize = ref(8)
const visible = ref(false)
const viewVisible = ref(false)
const activeNotice = ref(null)
const form = reactive({ id: null, title: '', content: '', status: 1 })

const statusText = status => ({ 0: '已下架', 1: '已发布', 2: '置顶' }[status] || '未知')
const statusType = status => ({ 0: 'info', 1: 'success', 2: 'warning' }[status] || 'info')
const filteredRows = computed(() => {
  const value = keyword.value.trim()
  if (!value) return rows.value
  return rows.value.filter(row => `${row.title || ''}${row.content || ''}`.includes(value))
})
const pagedRows = computed(() => filteredRows.value.slice((page.value - 1) * pageSize.value, page.value * pageSize.value))

const load = async () => {
  rows.value = await listNotices()
}

const openDialog = row => {
  Object.assign(form, row ? { id: row.id, title: row.title, content: row.content, status: row.status } : { id: null, title: '', content: '', status: 1 })
  visible.value = true
}

const openViewDialog = row => {
  activeNotice.value = row
  viewVisible.value = true
}

const save = async () => {
  if (!form.title || !form.content) {
    ElMessage.warning('请填写公告标题和内容')
    return
  }
  form.id ? await updateNotice(form.id, form) : await createNotice(form)
  ElMessage.success(form.id ? '保存成功' : '发布成功')
  visible.value = false
  load()
}

const setPinned = async id => {
  await pinNotice(id)
  ElMessage.success('已置顶')
  load()
}

const setNormal = async id => {
  await normalNotice(id)
  ElMessage.success('状态已更新')
  load()
}

const setOffline = async id => {
  await offlineNotice(id)
  ElMessage.success('已下架')
  load()
}

const remove = async id => {
  await ElMessageBox.confirm('确定删除这条公告吗？', '删除确认', { type: 'warning' })
  await deleteNotice(id)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>

<style scoped>
:deep(.notice-row) {
  cursor: pointer;
}

:deep(.notice-row:hover .notice-content) {
  color: #1d4ed8;
}

.notice-content {
  display: -webkit-box;
  overflow: hidden;
  line-height: 1.6;
  text-overflow: ellipsis;
  white-space: normal;
  word-break: break-word;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
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

.notice-detail-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
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
</style>
