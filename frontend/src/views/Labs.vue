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
      <el-table :data="pagedRows" border>
        <el-table-column prop="name" label="实验室名称" />
        <el-table-column prop="location" label="位置" />
        <el-table-column prop="capacity" label="容量" />
        <el-table-column prop="manager" label="负责人" />
        <el-table-column prop="description" label="介绍" />
        <el-table-column label="状态">
          <template #default="{ row }">{{ row.status === 1 ? '开放' : '停用' }}</template>
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
    <el-dialog v-model="visible" title="实验室信息" width="520px">
      <el-form :model="form" label-width="92px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="位置"><el-input v-model="form.location" /></el-form-item>
        <el-form-item label="容量"><el-input-number v-model="form.capacity" :min="1" /></el-form-item>
        <el-form-item label="负责人"><el-input v-model="form.manager" /></el-form-item>
        <el-form-item label="介绍"><el-input v-model="form.description" type="textarea" /></el-form-item>
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
const form = reactive({ id: null, name: '', location: '', capacity: 30, manager: '', description: '', status: 1 })

const filteredRows = computed(() => {
  const value = keyword.value.trim()
  if (!value) return rows.value
  return rows.value.filter(row => `${row.name || ''}${row.location || ''}${row.manager || ''}`.includes(value))
})
const pagedRows = computed(() => filteredRows.value.slice((page.value - 1) * pageSize.value, page.value * pageSize.value))

const load = async () => {
  rows.value = await listLabs()
}

const openDialog = row => {
  Object.assign(form, row || { id: null, name: '', location: '', capacity: 30, manager: '', description: '', status: 1 })
  visible.value = true
}

const save = async () => {
  if (!form.name || !form.location || !form.manager) {
    ElMessage.warning('请填写名称、位置和负责人')
    return
  }
  form.id ? await updateLab(form.id, form) : await createLab(form)
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
.toolbar-actions {
  display: flex;
  gap: 10px;
}

.pagination {
  margin-top: 14px;
  justify-content: flex-end;
}
</style>
