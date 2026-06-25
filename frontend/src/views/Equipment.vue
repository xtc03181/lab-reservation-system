<template>
  <div>
    <div class="toolbar">
      <h2 class="page-title">设备信息</h2>
      <div class="toolbar-actions">
        <el-input v-model="keyword" placeholder="搜索设备名称/编号" clearable style="width: 240px" />
        <el-button v-if="isAdmin" type="primary" @click="openDialog()">新增设备</el-button>
      </div>
    </div>
    <div class="panel">
      <el-table :data="pagedRows" border>
        <el-table-column prop="name" label="设备名称" />
        <el-table-column prop="code" label="编号" />
        <el-table-column prop="labId" label="实验室ID" />
        <el-table-column prop="model" label="型号" />
        <el-table-column prop="totalCount" label="总数" />
        <el-table-column prop="availableCount" label="可借" />
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
    <el-dialog v-model="visible" title="设备信息" width="520px">
      <el-form :model="form" label-width="92px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="编号"><el-input v-model="form.code" /></el-form-item>
        <el-form-item label="实验室ID"><el-input-number v-model="form.labId" :min="1" /></el-form-item>
        <el-form-item label="型号"><el-input v-model="form.model" /></el-form-item>
        <el-form-item label="总数"><el-input-number v-model="form.totalCount" :min="1" /></el-form-item>
        <el-form-item label="可借数量"><el-input-number v-model="form.availableCount" :min="0" /></el-form-item>
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
import { createEquipment, deleteEquipment, listEquipment, updateEquipment } from '../api/modules'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()
const isAdmin = computed(() => userStore.user?.role === 'ADMIN')
const rows = ref([])
const keyword = ref('')
const page = ref(1)
const pageSize = ref(8)
const visible = ref(false)
const form = reactive({ id: null, name: '', code: '', labId: 1, model: '', totalCount: 1, availableCount: 1, status: 1 })

const filteredRows = computed(() => {
  const value = keyword.value.trim()
  if (!value) return rows.value
  return rows.value.filter(row => `${row.name || ''}${row.code || ''}${row.model || ''}`.includes(value))
})
const pagedRows = computed(() => filteredRows.value.slice((page.value - 1) * pageSize.value, page.value * pageSize.value))

const load = async () => {
  rows.value = await listEquipment()
}

const openDialog = row => {
  Object.assign(form, row || { id: null, name: '', code: '', labId: 1, model: '', totalCount: 1, availableCount: 1, status: 1 })
  visible.value = true
}

const save = async () => {
  if (!form.name || !form.code || !form.labId) {
    ElMessage.warning('请填写名称、编号和实验室')
    return
  }
  if (form.availableCount > form.totalCount) {
    ElMessage.warning('可借数量不能大于总数')
    return
  }
  form.id ? await updateEquipment(form.id, form) : await createEquipment(form)
  ElMessage.success('保存成功')
  visible.value = false
  load()
}

const remove = async id => {
  await deleteEquipment(id)
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
