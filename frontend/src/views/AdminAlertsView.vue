<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { api } from '../api/services'
import type { AlertEntity } from '../types'

const loading = ref(false)
const list = ref<AlertEntity[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, status: undefined as number | undefined, level: undefined as number | undefined })

const load = async () => {
  loading.value = true
  try {
    const page = await api.admin.alerts(query)
    list.value = page.records
    total.value = page.total
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <el-card>
    <template #header>
      <el-space>
        <el-select v-model="query.status" clearable placeholder="状态" style="width:120px" @change="load">
          <el-option label="未读" :value="0" /><el-option label="已读" :value="1" /><el-option label="已处理" :value="2" />
        </el-select>
        <el-select v-model="query.level" clearable placeholder="等级" style="width:120px" @change="load">
          <el-option label="信息" :value="0" /><el-option label="警告" :value="1" /><el-option label="错误" :value="2" /><el-option label="严重" :value="3" />
        </el-select>
        <el-button type="primary" @click="load">查询</el-button>
      </el-space>
    </template>
    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="petId" label="宠物ID" width="100" />
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column prop="alertTitle" label="标题" min-width="180" />
      <el-table-column prop="alertType" label="类型" width="110" />
      <el-table-column prop="alertLevel" label="等级" width="80" />
      <el-table-column prop="status" label="状态" width="80" />
      <el-table-column prop="alertContent" label="内容" min-width="240" />
      <el-table-column prop="alertTime" label="时间" min-width="180" />
    </el-table>
    <div style="margin-top:12px;display:flex;justify-content:flex-end">
      <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" layout="total, prev, pager, next" :total="total" @current-change="load"/>
    </div>
  </el-card>
</template>
