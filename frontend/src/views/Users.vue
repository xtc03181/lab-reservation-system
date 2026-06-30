<template>
  <div>
    <div class="toolbar">
      <h2 class="page-title">用户管理</h2>
      <div class="toolbar-actions">
        <el-input v-model="keyword" placeholder="搜索账号/姓名/电话/邮箱" clearable style="width: 260px" />
        <el-button type="primary" @click="openDialog()">新增用户</el-button>
      </div>
    </div>
    <div class="panel">
      <div class="table-summary">
        <span>共 <strong>{{ filteredRows.length }}</strong> 个用户</span>
        <span>启用 <strong>{{ filteredRows.filter(row => row.status === 1).length }}</strong> 个</span>
        <span>教师 <strong>{{ filteredRows.filter(row => row.role === 'TEACHER').length }}</strong> 个</span>
        <span>学生 <strong>{{ filteredRows.filter(row => row.role === 'STUDENT').length }}</strong> 个</span>
      </div>
      <el-table :data="pagedRows" border stripe empty-text="暂无用户数据">
        <el-table-column prop="username" label="账号" min-width="130" show-overflow-tooltip />
        <el-table-column prop="realName" label="姓名" min-width="110" show-overflow-tooltip />
        <el-table-column prop="phone" label="电话" min-width="130" show-overflow-tooltip />
        <el-table-column prop="email" label="邮箱" min-width="190" show-overflow-tooltip />
        <el-table-column label="角色" width="130">
          <template #default="{ row }">
            <el-tag :type="roleTagType(row.role)">{{ roleText(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="170">
          <template #default="{ row }">
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
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

    <el-dialog v-model="visible" :title="form.id ? '编辑用户' : '新增用户'" width="600px">
      <el-form :model="form" label-width="84px">
        <el-form-item label="账号">
          <el-input v-model.trim="form.username" :disabled="Boolean(form.id)" />
        </el-form-item>
        <el-form-item label="姓名"><el-input v-model.trim="form.realName" /></el-form-item>
        <el-form-item label="电话"><el-input v-model.trim="form.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model.trim="form.email" placeholder="用于忘记密码验证码" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role" style="width: 100%">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="教师审核员" value="TEACHER" />
            <el-option label="学生" value="STUDENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="停用" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            :placeholder="form.id ? '不填写则不修改；需包含大小写、数字和特殊字符' : '不填写默认 Abc12345!'"
          />
          <div class="password-tip">设置密码时必须至少 8 位，需包含大写字母、小写字母、数字和特殊字符。</div>
        </el-form-item>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { createUser, deleteUser, listUsers, updateUser } from '../api/modules'

const rows = ref([])
const keyword = ref('')
const page = ref(1)
const pageSize = ref(8)
const visible = ref(false)
const form = reactive({ id: null, username: '', realName: '', phone: '', email: '', role: 'STUDENT', status: 1, password: '' })

const roleText = role => ({ ADMIN: '管理员', TEACHER: '教师审核员', STUDENT: '学生' }[role] || role)
const roleTagType = role => ({ ADMIN: 'danger', TEACHER: 'warning', STUDENT: 'primary' }[role] || 'info')
const isStrongPassword = password =>
  Boolean(password)
  && password.length >= 8
  && /[A-Z]/.test(password)
  && /[a-z]/.test(password)
  && /\d/.test(password)
  && /[^A-Za-z0-9]/.test(password)
const filteredRows = computed(() => {
  const value = keyword.value.trim()
  if (!value) return rows.value
  return rows.value.filter(row => `${row.username || ''}${row.realName || ''}${row.phone || ''}${row.email || ''}`.includes(value))
})
const pagedRows = computed(() => filteredRows.value.slice((page.value - 1) * pageSize.value, page.value * pageSize.value))

const load = async () => {
  rows.value = await listUsers()
}

const openDialog = row => {
  Object.assign(form, row ? { ...row, password: '' } : { id: null, username: '', realName: '', phone: '', email: '', role: 'STUDENT', status: 1, password: '' })
  visible.value = true
}

const save = async () => {
  if (!form.username || !form.role) {
    ElMessage.warning('请填写账号并选择角色')
    return
  }
  if (form.password && !isStrongPassword(form.password)) {
    ElMessage.warning('密码至少 8 位，需包含大写字母、小写字母、数字和特殊字符')
    return
  }
  form.id ? await updateUser(form.id, form) : await createUser(form)
  ElMessage.success('保存成功')
  visible.value = false
  load()
}

const remove = async row => {
  await ElMessageBox.confirm(`确定删除用户“${row.username}”吗？`, '删除确认', { type: 'warning' })
  await deleteUser(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>

<style scoped>
.password-tip {
  width: 100%;
  margin-top: 6px;
  color: #6b7280;
  font-size: 13px;
  line-height: 1.5;
}
</style>
