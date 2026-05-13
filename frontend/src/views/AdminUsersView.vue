<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api/services'
import type { AdminUserEntity } from '../types'

const loading = ref(false)
const users = ref<AdminUserEntity[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', status: undefined as number | undefined, role: undefined as number | undefined })

const load = async () => {
  loading.value = true
  try {
    const page = await api.admin.users(query)
    users.value = page.records
    total.value = page.total
  } finally {
    loading.value = false
  }
}

const toggleStatus = async (row: AdminUserEntity) => {
  const next = row.status === 0 ? 1 : 0
  await api.admin.updateUserStatus(row.id, next)
  ElMessage.success('状态已更新')
  await load()
}

onMounted(load)
</script>

<template>
  <el-card>
    <template #header>
      <el-space>
        <el-input v-model="query.keyword" placeholder="用户名/手机号" style="width:220px" @keyup.enter="load" />
        <el-select v-model="query.status" clearable placeholder="状态" style="width:120px" @change="load">
          <el-option label="正常" :value="0" /><el-option label="冻结" :value="1" />
        </el-select>
        <el-button type="primary" @click="load">查询</el-button>
      </el-space>
    </template>
    <el-table :data="users" v-loading="loading" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" width="140" />
      <el-table-column prop="nickname" label="昵称" width="140" />
      <el-table-column prop="phone" label="手机号" width="150" />
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column label="角色" width="100"><template #default="{row}">{{ row.role === 2 ? '管理员' : '用户' }}</template></el-table-column>
      <el-table-column label="状态" width="90"><template #default="{row}">{{ row.status === 0 ? '正常' : '冻结' }}</template></el-table-column>
      <el-table-column label="操作" width="140"><template #default="{row}"><el-button size="small" @click="toggleStatus(row)">{{ row.status===0?'冻结':'解冻' }}</el-button></template></el-table-column>
    </el-table>
    <div style="margin-top:12px;display:flex;justify-content:flex-end">
      <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" layout="total, prev, pager, next" :total="total" @current-change="load"/>
    </div>
  </el-card>
</template>
