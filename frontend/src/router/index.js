import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const Login = () => import('../views/Login.vue')
const Layout = () => import('../views/Layout.vue')
const Dashboard = () => import('../views/Dashboard.vue')
const Labs = () => import('../views/Labs.vue')
const Equipment = () => import('../views/Equipment.vue')
const LabReservations = () => import('../views/LabReservations.vue')
const EquipmentBorrows = () => import('../views/EquipmentBorrows.vue')
const Notices = () => import('../views/Notices.vue')
const Users = () => import('../views/Users.vue')
const Profile = () => import('../views/Profile.vue')
const OperationLogs = () => import('../views/OperationLogs.vue')
const Messages = () => import('../views/Messages.vue')
const ReservationRules = () => import('../views/ReservationRules.vue')

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
        { path: 'messages', component: Messages, meta: { roles: ['ADMIN', 'TEACHER', 'STUDENT'] } },
        { path: 'reservation-rules', component: ReservationRules, meta: { roles: ['ADMIN'] } },
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
