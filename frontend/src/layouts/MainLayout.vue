<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  Histogram,
  User,
  DataLine,
  Cpu,
  Bell,
  ArrowDown,
  SwitchButton,
} from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

const adminMenu = [
  { path: '/admin/dashboard', title: '运营总览', icon: Histogram },
  { path: '/admin/users', title: '用户管理', icon: User },
  { path: '/admin/pets', title: '宠物档案', icon: DataLine },
  { path: '/admin/devices', title: '设备管理', icon: Cpu },
  { path: '/admin/alerts', title: '预警审核', icon: Bell },
]

const activePath = computed(() => route.path)
const displayName = computed(() => authStore.user?.nickname || authStore.user?.username || '管理员')
const currentTitle = computed(() => adminMenu.find((m) => m.path === activePath.value)?.title || '管理后台')

const goTo = (path: string) => router.push(path)

const onLogout = async () => {
  await ElMessageBox.confirm('确认退出当前账号吗？', '提示', { type: 'warning' })
  await authStore.logout()
  router.replace('/login')
}
</script>

<template>
  <el-container class="layout-root">
    <el-aside class="sidebar" width="232px">
      <div class="brand">
        <div class="logo-dot" />
        <div class="brand-text">
          <p class="brand-title">PetHealth</p>
          <p class="brand-sub">管理后台</p>
        </div>
      </div>
      <el-menu
        :default-active="activePath"
        class="side-menu"
        background-color="transparent"
        text-color="#cbd5e1"
        active-text-color="#fff"
      >
        <el-menu-item
          v-for="item in adminMenu"
          :key="item.path"
          :index="item.path"
          @click="goTo(item.path)"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
      <div class="footer">
        <span>v1.0 · {{ new Date().getFullYear() }}</span>
      </div>
    </el-aside>

    <el-container>
      <el-header class="header-bar" height="60px">
        <div>
          <div class="crumb">PetHealth / 管理后台</div>
          <div class="page-title">{{ currentTitle }}</div>
        </div>
        <el-dropdown trigger="click">
          <div class="user-chip">
            <el-avatar :size="34" class="user-avatar">{{ displayName.slice(0, 1).toUpperCase() }}</el-avatar>
            <div class="user-text">
              <span class="user-name">{{ displayName }}</span>
              <el-tag size="small" type="danger" effect="dark" round>管理员</el-tag>
            </div>
            <el-icon><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="router.push('/admin/dashboard')">返回总览</el-dropdown-item>
              <el-dropdown-item divided @click="onLogout">
                <el-icon><SwitchButton /></el-icon>退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <el-main class="page-container">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout-root {
  min-height: 100vh;
  background: #f1f5f9;
}

.sidebar {
  background: linear-gradient(180deg, #0f172a 0%, #1e293b 100%);
  display: flex;
  flex-direction: column;
  border-right: 1px solid rgba(255, 255, 255, 0.05);
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.logo-dot {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  background: linear-gradient(135deg, #7dd3fc, #22c55e);
  box-shadow: 0 6px 16px rgba(125, 211, 252, 0.35);
}

.brand-title {
  margin: 0;
  font-size: 17px;
  color: #fff;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.brand-sub {
  margin: 2px 0 0;
  font-size: 12px;
  color: #64748b;
}

.side-menu {
  flex: 1;
  margin-top: 14px;
  border-right: none;
}

.side-menu :deep(.el-menu-item) {
  height: 46px;
  margin: 4px 12px;
  border-radius: 8px;
  font-size: 14px;
  padding-left: 16px !important;
}

.side-menu :deep(.el-menu-item:hover) {
  background: rgba(125, 211, 252, 0.08);
}

.side-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, rgba(56, 189, 248, 0.25), rgba(34, 197, 94, 0.18));
  color: #fff !important;
}

.side-menu :deep(.el-menu-item .el-icon) {
  font-size: 17px;
  margin-right: 10px;
}

.footer {
  padding: 14px 22px 18px;
  color: #475569;
  font-size: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
}

.header-bar {
  background: #fff;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
  box-shadow: 0 1px 0 rgba(15, 23, 42, 0.02);
}

.crumb {
  color: #94a3b8;
  font-size: 12px;
}

.page-title {
  margin-top: 2px;
  color: #0f172a;
  font-size: 17px;
  font-weight: 600;
}

.user-chip {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 999px;
  transition: background 0.15s;
}

.user-chip:hover {
  background: #f1f5f9;
}

.user-avatar {
  background: linear-gradient(135deg, #6366f1, #ec4899);
  color: #fff;
  font-weight: 600;
}

.user-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name {
  color: #0f172a;
  font-size: 14px;
  font-weight: 500;
  line-height: 1.2;
}

.page-container {
  padding: 24px 28px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.18s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
