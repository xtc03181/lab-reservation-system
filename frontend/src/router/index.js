import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
import Login from '../views/Login.vue'
import Layout from '../views/Layout.vue'
import Dashboard from '../views/Dashboard.vue'
import Labs from '../views/Labs.vue'
import Equipment from '../views/Equipment.vue'
import LabReservations from '../views/LabReservations.vue'
import EquipmentBorrows from '../views/EquipmentBorrows.vue'
import Notices from '../views/Notices.vue'
import Users from '../views/Users.vue'
import Profile from '../views/Profile.vue'
import OperationLogs from '../views/OperationLogs.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: Login },
    {
      path: '/',
      component: Layout,
      children: [
        { path: '', redirect: '/dashboard' },
        { path: 'dashboard', component: Dashboard, meta: { roles: ['ADMIN', 'TEACHER', 'STUDENT'] } },
        { path: 'labs', component: Labs, meta: { roles: ['ADMIN', 'STUDENT'] } },
        { path: 'equipment', component: Equipment, meta: { roles: ['ADMIN', 'STUDENT'] } },
        { path: 'lab-reservations', component: LabReservations, meta: { roles: ['ADMIN', 'TEACHER', 'STUDENT'] } },
        { path: 'equipment-borrows', component: EquipmentBorrows, meta: { roles: ['ADMIN', 'TEACHER', 'STUDENT'] } },
        { path: 'notices', component: Notices, meta: { roles: ['ADMIN', 'TEACHER', 'STUDENT'] } },
        { path: 'users', component: Users, meta: { roles: ['ADMIN'] } },
        { path: 'profile', component: Profile, meta: { roles: ['ADMIN', 'TEACHER', 'STUDENT'] } },
        { path: 'operation-logs', component: OperationLogs, meta: { roles: ['ADMIN'] } }
      ]
    }
  ]
})

router.beforeEach(to => {
  const userStore = useUserStore()
  if (to.path !== '/login' && !userStore.token) {
    return '/login'
  }
  if (to.path === '/login' && userStore.token) {
    return '/dashboard'
  }
  const roles = to.meta.roles
  if (roles && userStore.user?.role && !roles.includes(userStore.user.role)) {
    return '/dashboard'
  }
})

export default router
