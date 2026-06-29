<template>
  <el-container class="layout">
    <el-aside width="232px" class="aside">
      <div class="brand">实验室预约系统</div>
      <el-menu router :default-active="$route.path" background-color="#ffffff">
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          <span>{{ item.title }}</span>
          <el-badge
            v-if="menuBadge(item.path)"
            class="menu-badge"
            :value="menuBadge(item.path)"
            :max="99"
          />
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span>{{ userStore.user?.realName || userStore.user?.username }}（{{ roleName }}）</span>
        <el-badge :value="messageUnread" :max="99" :hidden="!messageUnread">
          <el-button @click="$router.push('/messages')">消息中心</el-button>
        </el-badge>
        <el-button @click="$router.push('/profile')">个人中心</el-button>
        <el-button @click="logout">退出</el-button>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { listEquipmentBorrows, listLabReservations, unreadMessageCount } from '../api/modules'
import { useUserStore } from '../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const reservationReminder = ref(0)
const borrowReminder = ref(0)
const messageUnread = ref(0)

const menuMap = {
  ADMIN: [
    { path: '/dashboard', title: '首页统计' },
    { path: '/labs', title: '实验室管理' },
    { path: '/equipment', title: '设备管理' },
    { path: '/lab-reservations', title: '预约审核' },
    { path: '/equipment-borrows', title: '借用审核' },
    { path: '/reservation-rules', title: '预约规则' },
    { path: '/notices', title: '公告管理' },
    { path: '/messages', title: '消息中心' },
    { path: '/users', title: '用户管理' },
    { path: '/operation-logs', title: '操作日志' },
    { path: '/profile', title: '个人中心' }
  ],
  TEACHER: [
    { path: '/dashboard', title: '首页统计' },
    { path: '/lab-reservations', title: '预约审核' },
    { path: '/equipment-borrows', title: '借用审核' },
    { path: '/notices', title: '公告查看' },
    { path: '/messages', title: '消息中心' },
    { path: '/profile', title: '个人中心' }
  ],
  STUDENT: [
    { path: '/dashboard', title: '首页统计' },
    { path: '/labs', title: '实验室信息' },
    { path: '/equipment', title: '设备信息' },
    { path: '/lab-reservations', title: '我的预约' },
    { path: '/equipment-borrows', title: '我的借用' },
    { path: '/notices', title: '公告查看' },
    { path: '/messages', title: '消息中心' },
    { path: '/profile', title: '个人中心' }
  ]
}

const menus = computed(() => menuMap[userStore.user?.role] || [])
const roleName = computed(() => ({ ADMIN: '管理员', TEACHER: '教师审核员', STUDENT: '学生' }[userStore.user?.role] || '未知角色'))
const menuBadge = path => {
  if (path === '/lab-reservations') return reservationReminder.value
  if (path === '/equipment-borrows') return borrowReminder.value
  if (path === '/messages') return messageUnread.value
  return 0
}

const loadReminders = async () => {
  if (!userStore.user?.role) return
  const [reservations, borrows, unread] = await Promise.all([listLabReservations(), listEquipmentBorrows(), unreadMessageCount()])
  messageUnread.value = unread
  if (userStore.user.role === 'STUDENT') {
    reservationReminder.value = reservations.filter(item => ['APPROVED', 'REJECTED'].includes(item.status)).length
    borrowReminder.value = borrows.filter(item => ['APPROVED', 'REJECTED', 'RETURNED'].includes(item.status)).length
  } else {
    reservationReminder.value = reservations.filter(item => item.status === 'PENDING').length
    borrowReminder.value = borrows.filter(item => item.status === 'PENDING').length
  }
}

const logout = () => {
  userStore.logout()
  router.push('/login')
}

onMounted(loadReminders)
watch(() => route.fullPath, loadReminders)
</script>

<style scoped>
.layout {
  min-height: 100vh;
}

.aside {
  background: #ffffff;
  border-right: 1px solid #e5e7eb;
}

.brand {
  height: 64px;
  padding: 20px 18px;
  color: #111827;
  font-size: 18px;
  font-weight: 700;
  border-bottom: 1px solid #e5e7eb;
}

.header {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 14px;
  background: #ffffff;
  border-bottom: 1px solid #e5e7eb;
}

.menu-badge {
  margin-left: auto;
}

:deep(.el-menu-item) {
  display: flex;
  gap: 8px;
}
</style>
