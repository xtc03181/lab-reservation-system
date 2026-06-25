<template>
  <div>
    <div class="toolbar">
      <h2 class="page-title">个人中心</h2>
    </div>
    <div class="profile-grid">
      <div class="panel">
        <h3>基本资料</h3>
        <el-form :model="profile" label-width="84px">
          <el-form-item label="账号"><el-input v-model="profile.username" disabled /></el-form-item>
          <el-form-item label="角色"><el-input :model-value="roleText(profile.role)" disabled /></el-form-item>
          <el-form-item label="姓名"><el-input v-model.trim="profile.realName" /></el-form-item>
          <el-form-item label="电话"><el-input v-model.trim="profile.phone" /></el-form-item>
          <el-form-item label="邮箱"><el-input v-model.trim="profile.email" placeholder="用于忘记密码验证码" /></el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveProfile">保存资料</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="panel">
        <h3>修改密码</h3>
        <el-form :model="passwordForm" label-width="84px">
          <el-form-item label="原密码"><el-input v-model="passwordForm.oldPassword" type="password" show-password /></el-form-item>
          <el-form-item label="新密码">
            <el-input
              v-model="passwordForm.newPassword"
              type="password"
              show-password
              placeholder="至少 8 位，包含大小写字母、数字和特殊字符"
            />
            <div class="password-tip">密码至少 8 位，需包含大写字母、小写字母、数字和特殊字符，也不能与旧密码相同。</div>
          </el-form-item>
          <el-form-item label="确认密码"><el-input v-model="passwordForm.confirmPassword" type="password" show-password /></el-form-item>
          <el-form-item>
            <el-button type="primary" @click="savePassword">修改密码</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { changePassword, getMe, updateMe } from '../api/modules'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()
const profile = reactive({ id: null, username: '', realName: '', phone: '', email: '', role: '' })
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const roleText = role => ({ ADMIN: '管理员', TEACHER: '教师审核员', STUDENT: '学生' }[role] || role)
const isStrongPassword = password =>
  Boolean(password)
  && password.length >= 8
  && /[A-Z]/.test(password)
  && /[a-z]/.test(password)
  && /\d/.test(password)
  && /[^A-Za-z0-9]/.test(password)

const load = async () => {
  Object.assign(profile, await getMe())
}

const saveProfile = async () => {
  await updateMe({ realName: profile.realName, phone: profile.phone, email: profile.email })
  userStore.user = { ...userStore.user, realName: profile.realName, phone: profile.phone, email: profile.email }
  localStorage.setItem('user', JSON.stringify(userStore.user))
  ElMessage.success('资料已保存')
}

const savePassword = async () => {
  if (!passwordForm.oldPassword || !passwordForm.newPassword) {
    ElMessage.warning('请填写原密码和新密码')
    return
  }
  if (!isStrongPassword(passwordForm.newPassword)) {
    ElMessage.warning('密码至少 8 位，需包含大写字母、小写字母、数字和特殊字符')
    return
  }
  if (passwordForm.newPassword === passwordForm.oldPassword) {
    ElMessage.warning('新密码不能与旧密码相同')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  await changePassword({ oldPassword: passwordForm.oldPassword, newPassword: passwordForm.newPassword })
  Object.assign(passwordForm, { oldPassword: '', newPassword: '', confirmPassword: '' })
  ElMessage.success('密码已修改')
}

onMounted(load)
</script>

<style scoped>
.profile-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

h3 {
  margin: 0 0 18px;
  color: #111827;
}

.password-tip {
  width: 100%;
  margin-top: 6px;
  color: #6b7280;
  font-size: 13px;
  line-height: 1.5;
}

@media (max-width: 900px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }
}
</style>
