import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
    },
    {
      path: '/',
      component: () => import('../layouts/MainLayout.vue'),
      children: [
        { path: '', redirect: '/admin/dashboard' },
        {
          path: 'admin/dashboard',
          name: 'admin-dashboard',
          component: () => import('../views/AdminDashboardView.vue'),
        },
        {
          path: 'admin/users',
          name: 'admin-users',
          component: () => import('../views/AdminUsersView.vue'),
        },
        {
          path: 'admin/pets',
          name: 'admin-pets',
          component: () => import('../views/AdminPetsView.vue'),
        },
        {
          path: 'admin/devices',
          name: 'admin-devices',
          component: () => import('../views/AdminDevicesView.vue'),
        },
        {
          path: 'admin/alerts',
          name: 'admin-alerts',
          component: () => import('../views/AdminAlertsView.vue'),
        },
      ],
    },
  ],
})

router.beforeEach(async (to) => {
  const authStore = useAuthStore()
  if (to.path === '/login') {
    if (authStore.token) return '/admin/dashboard'
    return true
  }

  if (!authStore.token) return '/login'

  if (!authStore.user) {
    try {
      await authStore.fetchUserInfo()
    } catch (error) {
      authStore.logout()
      return '/login'
    }
  }

  // 非管理员（普通用户）禁止进入 web 管理后台，改用微信小程序
  if (authStore.user?.role !== 2) {
    authStore.logout()
    return '/login'
  }
  return true
})

export default router
