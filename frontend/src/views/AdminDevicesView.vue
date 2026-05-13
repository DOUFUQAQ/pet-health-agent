<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { api } from '../api/services'
import type { DeviceEntity } from '../types'

const loading = ref(false)
const list = ref<DeviceEntity[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', status: undefined as number | undefined })

const load = async () => {
  loading.value = true
  try {
    const page = await api.admin.devices(query)
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
        <el-input v-model="query.keyword" placeholder="设备ID/名称" style="width:220px" @keyup.enter="load" />
        <el-select v-model="query.status" clearable placeholder="设备状态" style="width:120px" @change="load">
          <el-option label="离线" :value="0" /><el-option label="在线" :value="1" /><el-option label="故障" :value="2" />
        </el-select>
        <el-button type="primary" @click="load">查询</el-button>
      </el-space>
    </template>
    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="deviceId" label="设备编码" width="170" />
      <el-table-column prop="deviceName" label="设备名称" width="160" />
      <el-table-column prop="deviceType" label="类型" width="110" />
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column prop="petId" label="宠物ID" width="100" />
      <el-table-column prop="status" label="状态" width="90" />
      <el-table-column prop="battery" label="电量" width="90" />
      <el-table-column prop="lastOnlineTime" label="最后在线" min-width="180" />
    </el-table>
    <div style="margin-top:12px;display:flex;justify-content:flex-end">
      <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" layout="total, prev, pager, next" :total="total" @current-change="load"/>
    </div>
  </el-card>
</template>
