<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { api } from '../api/services'
import type { PetVO } from '../types'

const loading = ref(false)
const list = ref<PetVO[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })

const load = async () => {
  loading.value = true
  try {
    const page = await api.admin.pets(query)
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
        <el-input v-model="query.keyword" placeholder="宠物名/品种" style="width:220px" @keyup.enter="load" />
        <el-button type="primary" @click="load">查询</el-button>
      </el-space>
    </template>
    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column prop="name" label="宠物名" width="140" />
      <el-table-column prop="species" label="物种" width="110" />
      <el-table-column prop="breed" label="品种" width="140" />
      <el-table-column prop="weight" label="体重" width="100" />
      <el-table-column prop="status" label="状态" width="90" />
      <el-table-column prop="createTime" label="创建时间" min-width="180" />
    </el-table>
    <div style="margin-top:12px;display:flex;justify-content:flex-end">
      <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" layout="total, prev, pager, next" :total="total" @current-change="load"/>
    </div>
  </el-card>
</template>
