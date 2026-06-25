<template>
  <div class="login-page" :style="pageStyle">
    <header class="login-header">
      <div class="school-mark">GLUT</div>
      <div class="school-copy">
        <div class="school-name">桂林理工大学</div>
        <div class="system-name">实验室预约申请与设备管理系统</div>
      </div>
    </header>

    <main class="login-main">
      <section class="login-panel">
        <h1>欢迎登录</h1>
        <div class="tab-line">账号登录</div>
        <el-form :model="form" label-position="top" @keyup.enter="submit">
          <el-form-item>
            <el-input v-model.trim="form.username" size="large" placeholder="请输入账号" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="form.password" size="large" type="password" placeholder="请输入密码" show-password />
          </el-form-item>
          <div class="login-options">
            <el-checkbox v-model="remember">记住账号</el-checkbox>
            <button type="button" class="forgot-link" @click="openResetDialog">忘记密码</button>
          </div>
          <el-button type="danger" size="large" class="login-btn" :loading="loading" @click="submit">登 录</el-button>
        </el-form>
      </section>
    </main>

    <footer class="login-footer">© 2026 桂林理工大学实验室预约申请与设备管理系统</footer>

    <el-dialog v-model="resetVisible" title="邮箱验证码重置密码" width="460px">
      <el-form :model="resetForm" label-width="88px">
        <el-form-item label="账号">
          <el-input v-model.trim="resetForm.username" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model.trim="resetForm.email" placeholder="请输入该账号绑定的邮箱" />
        </el-form-item>
        <el-form-item label="验证码">
          <div class="code-row">
            <el-input v-model.trim="resetForm.code" placeholder="请输入 6 位验证码" />
            <el-button :disabled="countdown > 0" :loading="codeLoading" @click="sendCode">
              {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="resetForm.newPassword" type="password" show-password placeholder="至少 8 位，包含大小写字母、数字和特殊字符" />
          <div class="password-tip">密码至少 8 位，需包含大写字母、小写字母、数字和特殊字符，也不能与旧密码相同。</div>
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="resetForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetVisible = false">取消</el-button>
        <el-button type="primary" :loading="resetLoading" @click="submitReset">重置密码</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, resetPassword, sendResetCode } from '../api/modules'
import { useUserStore } from '../stores/user'
import campusBg from '../assets/login-campus.jpg'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const remember = ref(false)
const resetVisible = ref(false)
const codeLoading = ref(false)
const resetLoading = ref(false)
const countdown = ref(0)
let timer = null

const form = reactive({
  username: '',
  password: ''
})
const resetForm = reactive({
  username: '',
  email: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})
const pageStyle = {
  backgroundImage: `linear-gradient(rgba(20, 37, 70, 0.08), rgba(20, 37, 70, 0.22)), url(${campusBg})`
}

onMounted(() => {
  const savedUsername = localStorage.getItem('rememberedUsername')
  if (savedUsername) {
    remember.value = true
    form.username = savedUsername
  }
})

const submit = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  loading.value = true
  try {
    const data = await login(form)
    if (remember.value) {
      localStorage.setItem('rememberedUsername', form.username)
    } else {
      localStorage.removeItem('rememberedUsername')
    }
    userStore.setLoginInfo(data)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } finally {
    loading.value = false
  }
}

const openResetDialog = () => {
  Object.assign(resetForm, {
    username: form.username || '',
    email: '',
    code: '',
    newPassword: '',
    confirmPassword: ''
  })
  resetVisible.value = true
}

const sendCode = async () => {
  if (!resetForm.username || !resetForm.email) {
    ElMessage.warning('请先填写账号和邮箱')
    return
  }
  codeLoading.value = true
  try {
    await sendResetCode({ username: resetForm.username, email: resetForm.email })
    ElMessage.success('验证码已发送，请查收邮箱')
    startCountdown()
  } finally {
    codeLoading.value = false
  }
}

const submitReset = async () => {
  if (!resetForm.username || !resetForm.email || !resetForm.code || !resetForm.newPassword) {
    ElMessage.warning('请完整填写重置信息')
    return
  }
  if (!isStrongPassword(resetForm.newPassword)) {
    ElMessage.warning('密码至少 8 位，需包含大写字母、小写字母、数字和特殊字符')
    return
  }
  if (resetForm.newPassword !== resetForm.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  resetLoading.value = true
  try {
    await resetPassword({
      username: resetForm.username,
      email: resetForm.email,
      code: resetForm.code,
      newPassword: resetForm.newPassword
    })
    ElMessage.success('密码重置成功，请使用新密码登录')
    resetVisible.value = false
  } finally {
    resetLoading.value = false
  }
}

const isStrongPassword = password =>
  Boolean(password)
  && password.length >= 8
  && /[A-Z]/.test(password)
  && /[a-z]/.test(password)
  && /\d/.test(password)
  && /[^A-Za-z0-9]/.test(password)

const startCountdown = () => {
  countdown.value = 60
  if (timer) clearInterval(timer)
  timer = setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0) {
      clearInterval(timer)
      timer = null
    }
  }, 1000)
}
</script>

<style scoped>
.login-page {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  background-position: center bottom;
  background-size: cover;
}

.login-header {
  display: flex;
  align-items: center;
  gap: 18px;
  height: 116px;
  padding: 0 12vw;
  color: #ffffff;
  background: rgba(47, 79, 163, 0.96);
  box-shadow: 0 2px 14px rgba(15, 23, 42, 0.16);
}

.school-mark {
  display: grid;
  width: 72px;
  height: 72px;
  flex: 0 0 72px;
  place-items: center;
  color: #2f4fa3;
  font-size: 20px;
  font-weight: 800;
  background: #ffffff;
  border: 2px solid rgba(255, 255, 255, 0.9);
  border-radius: 50%;
}

.school-copy {
  min-width: 0;
}

.school-name {
  font-size: 30px;
  font-weight: 700;
  line-height: 1.2;
}

.system-name {
  margin-top: 8px;
  font-size: 24px;
  letter-spacing: 0;
}

.login-main {
  display: flex;
  min-height: calc(100vh - 116px);
  align-items: center;
  justify-content: flex-end;
  padding: 48px 12vw 72px;
}

.login-panel {
  width: min(430px, calc(100vw - 32px));
  padding: 40px 48px 42px;
  color: #ffffff;
  background: rgba(44, 70, 125, 0.9);
  box-shadow: 0 22px 58px rgba(15, 23, 42, 0.28);
}

h1 {
  margin: 0 0 28px;
  font-size: 32px;
  font-weight: 700;
  text-align: center;
}

.tab-line {
  position: relative;
  margin-bottom: 18px;
  padding-bottom: 10px;
  font-size: 18px;
  border-bottom: 3px solid rgba(255, 255, 255, 0.45);
}

.tab-line::after {
  position: absolute;
  left: 0;
  bottom: -3px;
  width: 110px;
  height: 3px;
  content: "";
  background: #e60012;
}

.login-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 2px 0 16px;
  color: rgba(255, 255, 255, 0.86);
  font-size: 14px;
}

.login-options :deep(.el-checkbox__label) {
  color: rgba(255, 255, 255, 0.9);
}

.forgot-link {
  padding: 0;
  color: rgba(255, 255, 255, 0.92);
  font: inherit;
  cursor: pointer;
  background: transparent;
  border: none;
}

.forgot-link:hover {
  color: #ffffff;
  text-decoration: underline;
}

.login-btn {
  width: 100%;
  border: none;
  background: #e60012;
}

.login-footer {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 18px;
  color: rgba(255, 255, 255, 0.86);
  font-size: 13px;
  text-align: center;
  text-shadow: 0 1px 8px rgba(15, 23, 42, 0.45);
}

.code-row {
  display: grid;
  width: 100%;
  grid-template-columns: 1fr 112px;
  gap: 8px;
}

.password-tip {
  margin-top: 6px;
  color: #909399;
  font-size: 12px;
  line-height: 1.4;
}

:deep(.el-input__wrapper) {
  min-height: 50px;
  border-radius: 8px;
  box-shadow: none;
}

@media (max-width: 900px) {
  .login-header {
    height: auto;
    min-height: 98px;
    padding: 18px 24px;
  }

  .school-name {
    font-size: 24px;
  }

  .system-name {
    font-size: 18px;
  }

  .login-main {
    justify-content: center;
    min-height: calc(100vh - 98px);
    padding: 32px 16px 72px;
  }

  .login-panel {
    padding: 34px 28px;
  }
}
</style>
