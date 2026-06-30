<template>
  <el-container class="layout">
    <el-aside width="232px" class="aside">
      <div class="brand">实验室预约系统</div>
      <el-menu router :default-active="$route.path" background-color="#ffffff" @select="handleMenuSelect">
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          <el-icon class="menu-icon">
            <component :is="item.icon" />
          </el-icon>
          <span class="menu-title">{{ item.title }}</span>
          <span v-if="menuBadge(item.path)" class="menu-badge">{{ formatBadge(menuBadge(item.path)) }}</span>
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
      <el-main class="main-area">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Bell,
  Calendar,
  DataAnalysis,
  Document,
  House,
  Memo,
  Monitor,
  Operation,
  Setting,
  Tools,
  User,
  UserFilled
} from '@element-plus/icons-vue'
import { listEquipmentBorrows, listLabReservations, unreadMessageCount } from '../api/modules'
import { useUserStore } from '../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const reservationReminder = ref(0)
const borrowReminder = ref(0)
const reservationReminderSignature = ref('')
const borrowReminderSignature = ref('')
const reminderViewVersion = ref(0)
const messageUnread = ref(0)
const icons = { Bell, Calendar, DataAnalysis, Document, House, Memo, Monitor, Operation, Setting, Tools, User, UserFilled }

const menuMap = {
  ADMIN: [
    { path: '/dashboard', title: '首页统计', icon: icons.DataAnalysis },
    { path: '/labs', title: '实验室管理', icon: icons.House },
    { path: '/equipment', title: '设备管理', icon: icons.Monitor },
    { path: '/lab-reservations', title: '预约审核', icon: icons.Calendar },
    { path: '/equipment-borrows', title: '借用审核', icon: icons.Tools },
    { path: '/reservation-rules', title: '预约规则', icon: icons.Setting },
    { path: '/notices', title: '公告管理', icon: icons.Document },
    { path: '/messages', title: '消息中心', icon: icons.Bell },
    { path: '/users', title: '用户管理', icon: icons.UserFilled },
    { path: '/operation-logs', title: '操作日志', icon: icons.Memo },
    { path: '/profile', title: '个人中心', icon: icons.User }
  ],
  TEACHER: [
    { path: '/dashboard', title: '首页统计', icon: icons.DataAnalysis },
    { path: '/lab-reservations', title: '预约审核', icon: icons.Calendar },
    { path: '/equipment-borrows', title: '借用审核', icon: icons.Tools },
    { path: '/notices', title: '公告查看', icon: icons.Document },
    { path: '/messages', title: '消息中心', icon: icons.Bell },
    { path: '/profile', title: '个人中心', icon: icons.User }
  ],
  STUDENT: [
    { path: '/dashboard', title: '首页统计', icon: icons.DataAnalysis },
    { path: '/labs', title: '实验室信息', icon: icons.House },
    { path: '/equipment', title: '设备信息', icon: icons.Monitor },
    { path: '/lab-reservations', title: '我的预约', icon: icons.Calendar },
    { path: '/equipment-borrows', title: '我的借用', icon: icons.Tools },
    { path: '/notices', title: '公告查看', icon: icons.Document },
    { path: '/messages', title: '消息中心', icon: icons.Bell },
    { path: '/profile', title: '个人中心', icon: icons.User }
  ]
}

const menus = computed(() => menuMap[userStore.user?.role] || [])
const roleName = computed(() => ({ ADMIN: '管理员', TEACHER: '教师审核员', STUDENT: '学生' }[userStore.user?.role] || '未知角色'))
const menuBadge = path => {
  reminderViewVersion.value
  if (path === '/lab-reservations') return isReminderViewed(path, reservationReminderSignature.value) ? 0 : reservationReminder.value
  if (path === '/equipment-borrows') return isReminderViewed(path, borrowReminderSignature.value) ? 0 : borrowReminder.value
  if (path === '/messages') return messageUnread.value
  return 0
}

const formatBadge = value => value > 99 ? '99+' : value

const reminderStorageKey = path => `lab-reservation:menu-reminder:${userStore.user?.id || userStore.user?.username || 'guest'}:${path}`

const isReminderViewed = (path, signature) => {
  if (!signature) return false
  return localStorage.getItem(reminderStorageKey(path)) === signature
}

const markReminderViewed = path => {
  let changed = false
  if (path === '/lab-reservations' && reservationReminderSignature.value) {
    localStorage.setItem(reminderStorageKey(path), reservationReminderSignature.value)
    changed = true
  }
  if (path === '/equipment-borrows' && borrowReminderSignature.value) {
    localStorage.setItem(reminderStorageKey(path), borrowReminderSignature.value)
    changed = true
  }
  if (changed) reminderViewVersion.value += 1
}

const buildReminderSignature = items => items
  .map(item => `${item.id}-${item.status}-${item.updateTime || item.reviewTime || item.createTime || ''}`)
  .sort()
  .join('|')

const loadReminders = async () => {
  if (!userStore.user?.role) return
  const [reservations, borrows, unread] = await Promise.all([listLabReservations(), listEquipmentBorrows(), unreadMessageCount()])
  messageUnread.value = unread
  if (userStore.user.role === 'STUDENT') {
    const reservationResults = reservations.filter(item => ['APPROVED', 'REJECTED'].includes(item.status))
    const borrowResults = borrows.filter(item => ['APPROVED', 'REJECTED', 'RETURNED'].includes(item.status))
    reservationReminder.value = reservationResults.length
    borrowReminder.value = borrowResults.length
    reservationReminderSignature.value = buildReminderSignature(reservationResults)
    borrowReminderSignature.value = buildReminderSignature(borrowResults)
  } else {
    const pendingReservations = reservations.filter(item => item.status === 'PENDING')
    const pendingBorrows = borrows.filter(item => item.status === 'PENDING')
    reservationReminder.value = pendingReservations.length
    borrowReminder.value = pendingBorrows.length
    reservationReminderSignature.value = buildReminderSignature(pendingReservations)
    borrowReminderSignature.value = buildReminderSignature(pendingBorrows)
  }
}

const handleMenuSelect = path => {
  markReminderViewed(path)
}

const logout = () => {
  userStore.logout()
  router.push('/login')
}

const refreshReminders = async () => {
  await loadReminders()
  markReminderViewed(route.path)
}

onMounted(refreshReminders)
watch(() => route.fullPath, refreshReminders)
</script>

<style scoped>
.layout {
  min-height: 100vh;
  background: #eef4fb;
}

.aside {
  background: #fbfdff;
  border-right: 1px solid #bfd3ec;
  box-shadow: 4px 0 18px rgba(37, 99, 235, 0.05);
}

.brand {
  height: 64px;
  padding: 20px 18px;
  color: #111827;
  font-size: 18px;
  font-weight: 700;
  border-bottom: 1px solid #c8d9ef;
}

.header {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 14px;
  background: #fbfdff;
  border-bottom: 1px solid #bfd3ec;
  box-shadow: 0 4px 18px rgba(37, 99, 235, 0.05);
}

.main-area {
  background: #f5f7fb;
}

:deep(.el-menu-item) {
  position: relative;
  gap: 10px;
  height: 54px;
  margin: 4px 10px;
  border-radius: 8px;
}

:deep(.el-menu) {
  border-right: 0;
}

:deep(.el-menu-item.is-active) {
  color: #1d4ed8;
  background: #eaf2ff;
}

.menu-icon {
  width: 18px;
  color: #64748b;
  font-size: 18px;
}

:deep(.el-menu-item.is-active) .menu-icon {
  color: #1d4ed8;
}

.menu-title {
  display: block;
  max-width: 154px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.menu-badge {
  position: absolute;
  top: 9px;
  right: 18px;
  min-width: 8px;
  height: 8px;
  padding: 0;
  overflow: hidden;
  color: transparent;
  line-height: 8px;
  text-align: center;
  background: #f56c6c;
  border: 2px solid #ffffff;
  border-radius: 999px;
  box-shadow: 0 1px 4px rgba(245, 108, 108, 0.35);
}

.menu-badge:not(:empty) {
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  color: #ffffff;
  font-size: 11px;
  font-weight: 700;
  line-height: 16px;
}
</style>
