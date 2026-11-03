<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
})

const onLogin = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    await authStore.login(form.username, form.password)
    if (authStore.user?.role !== 2) {
      // 普通用户拒绝登录管理后台
      authStore.logout()
      ElMessage.error('该账号无管理后台权限，请用微信小程序登录')
      return
    }
    ElMessage.success('登录成功')
    router.replace('/admin/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="left">
      <div class="brand">
        <div class="logo-dot" />
        <span class="brand-name">PetHealth · 管理后台</span>
      </div>
      <h1 class="title">智能宠物健康管家<br />运营管理平台</h1>
      <ul class="features">
        <li>多维度健康数据监控与异常预警</li>
        <li>设备管理与状态实时追踪</li>
        <li>用户/宠物档案统一运营</li>
        <li>大模型驱动的健康分析回查</li>
      </ul>
      <p class="footer-tip">普通宠物主请使用「PetHealth」微信小程序登录</p>
    </div>

    <div class="right">
      <el-card class="auth-card" shadow="never">
        <h2 class="card-title">管理员登录</h2>
        <p class="card-sub">使用具备管理员权限的账号登录</p>
        <el-form @submit.prevent>
          <el-form-item>
            <el-input
              v-model="form.username"
              size="large"
              :prefix-icon="User"
              placeholder="管理员账号"
              @keyup.enter="onLogin"
            />
          </el-form-item>
          <el-form-item>
            <el-input
              v-model="form.password"
              size="large"
              type="password"
              show-password
              :prefix-icon="Lock"
              placeholder="登录密码"
              @keyup.enter="onLogin"
            />
          </el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="submit-btn"
            @click="onLogin"
          >
            进入管理后台
          </el-button>
        </el-form>
        <div class="hint">
          首次使用请先在 <code>miniapp</code> 或后台完成注册，或参考 <code>backend/src/main/resources/db/schema.sql</code> 自行插入账号
        </div>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.15fr 1fr;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 60%, #1e3a8a 100%);
  color: #e2e8f0;
}

.left {
  padding: 64px 64px 56px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.left::before {
  content: '';
  position: absolute;
  inset: -10% -20% auto auto;
  width: 520px;
  height: 520px;
  background: radial-gradient(circle, rgba(125, 211, 252, 0.18), transparent 70%);
  pointer-events: none;
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 48px;
}

.logo-dot {
  width: 36px;
  height: 36px;
  border-radius: 12px;
  background: linear-gradient(135deg, #7dd3fc, #22c55e);
  box-shadow: 0 4px 14px rgba(125, 211, 252, 0.45);
}

.brand-name {
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 0.5px;
  color: #fff;
}

.title {
  font-size: 44px;
  line-height: 1.25;
  margin: 0 0 28px;
  color: #fff;
  font-weight: 700;
}

.features {
  margin: 0;
  padding: 0;
  list-style: none;
  font-size: 15px;
  line-height: 2;
  color: #94a3b8;
  max-width: 520px;
}

.features li::before {
  content: '◆';
  color: #38bdf8;
  margin-right: 10px;
  font-size: 12px;
}

.footer-tip {
  margin: 60px 0 0;
  color: #64748b;
  font-size: 13px;
}

.right {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
}

.auth-card {
  width: 420px;
  border-radius: 16px;
  border: none;
  box-shadow: 0 30px 60px rgba(15, 23, 42, 0.12);
  padding: 12px;
}

.card-title {
  margin: 0;
  color: #0f172a;
  font-size: 24px;
}

.card-sub {
  margin: 8px 0 32px;
  color: #64748b;
  font-size: 14px;
}

.submit-btn {
  width: 100%;
  height: 46px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 1px;
}

.hint {
  margin-top: 24px;
  text-align: center;
  color: #94a3b8;
  font-size: 13px;
}

.hint code {
  background: #f1f5f9;
  color: #0f172a;
  padding: 2px 8px;
  border-radius: 4px;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
}

@media (max-width: 1024px) {
  .login-page {
    grid-template-columns: 1fr;
  }
  .left {
    padding: 36px 24px 24px;
  }
  .title {
    font-size: 28px;
  }
  .features {
    display: none;
  }
}
</style>
