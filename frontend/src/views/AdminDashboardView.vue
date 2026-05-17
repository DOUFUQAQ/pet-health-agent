<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'
import {
  User,
  House,
  Cpu,
  Connection,
  Bell,
  Calendar,
} from '@element-plus/icons-vue'
import { api } from '../api/services'
import type { AdminOverview, DeviceEntity, AlertEntity } from '../types'

const loading = ref(false)
const data = ref<AdminOverview | null>(null)
const recentAlerts = ref<AlertEntity[]>([])
const devicesAll = ref<DeviceEntity[]>([])

const deviceChartRef = ref<HTMLDivElement>()
const alertChartRef = ref<HTMLDivElement>()
let deviceChart: echarts.ECharts | null = null
let alertChart: echarts.ECharts | null = null

const kpis = [
  { key: 'totalUsers', title: '注册用户', icon: User, color: '#3b82f6', bg: 'linear-gradient(135deg, #dbeafe, #eff6ff)' },
  { key: 'totalPets', title: '宠物档案', icon: House, color: '#10b981', bg: 'linear-gradient(135deg, #d1fae5, #f0fdf4)' },
  { key: 'totalDevices', title: '设备总数', icon: Cpu, color: '#8b5cf6', bg: 'linear-gradient(135deg, #ede9fe, #f5f3ff)' },
  { key: 'onlineDevices', title: '在线设备', icon: Connection, color: '#22c55e', bg: 'linear-gradient(135deg, #dcfce7, #f0fdf4)' },
  { key: 'unreadAlerts', title: '未读预警', icon: Bell, color: '#f59e0b', bg: 'linear-gradient(135deg, #fef3c7, #fffbeb)' },
  { key: 'todayAlerts', title: '今日预警', icon: Calendar, color: '#ef4444', bg: 'linear-gradient(135deg, #fee2e2, #fef2f2)' },
] as const

const renderDeviceChart = () => {
  if (!deviceChartRef.value) return
  if (!deviceChart) deviceChart = echarts.init(deviceChartRef.value)
  const total = devicesAll.value.length || 1
  const online = devicesAll.value.filter((d) => d.status === 1).length
  const offline = devicesAll.value.filter((d) => d.status === 0).length
  const fault = devicesAll.value.filter((d) => d.status === 2).length
  deviceChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, icon: 'circle' },
    series: [
      {
        name: '设备状态',
        type: 'pie',
        radius: ['50%', '72%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: false,
        label: { show: true, formatter: '{b}\n{d}%', fontSize: 12 },
        data: [
          { value: online, name: '在线', itemStyle: { color: '#22c55e' } },
          { value: offline, name: '离线', itemStyle: { color: '#94a3b8' } },
          { value: fault, name: '故障', itemStyle: { color: '#ef4444' } },
        ],
      },
    ],
    graphic: total === 0
      ? [{ type: 'text', left: 'center', top: '40%', style: { text: '暂无数据', fill: '#94a3b8' } }]
      : undefined,
  })
}

const renderAlertChart = () => {
  if (!alertChartRef.value) return
  if (!alertChart) alertChart = echarts.init(alertChartRef.value)
  const counts = [0, 0, 0, 0]
  for (const a of recentAlerts.value) {
    if (a.alertLevel >= 0 && a.alertLevel <= 3) counts[a.alertLevel]++
  }
  alertChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 30, right: 20, top: 20, bottom: 30, containLabel: true },
    xAxis: {
      type: 'category',
      data: ['信息', '警告', '错误', '严重'],
      axisTick: { show: false },
      axisLine: { lineStyle: { color: '#cbd5e1' } },
    },
    yAxis: { type: 'value', splitLine: { lineStyle: { type: 'dashed', color: '#e2e8f0' } } },
    series: [
      {
        type: 'bar',
        barWidth: 32,
        data: [
          { value: counts[0], itemStyle: { color: '#60a5fa', borderRadius: [6, 6, 0, 0] } },
          { value: counts[1], itemStyle: { color: '#facc15', borderRadius: [6, 6, 0, 0] } },
          { value: counts[2], itemStyle: { color: '#fb923c', borderRadius: [6, 6, 0, 0] } },
          { value: counts[3], itemStyle: { color: '#ef4444', borderRadius: [6, 6, 0, 0] } },
        ],
      },
    ],
  })
}

const load = async () => {
  loading.value = true
  try {
    const [overview, devicePage, alertPage] = await Promise.all([
      api.admin.overview(),
      api.admin.devices({ pageNum: 1, pageSize: 100 }),
      api.admin.alerts({ pageNum: 1, pageSize: 50 }),
    ])
    data.value = overview
    devicesAll.value = devicePage.records
    recentAlerts.value = alertPage.records
  } finally {
    loading.value = false
  }
}

watch([devicesAll, recentAlerts], () => {
  renderDeviceChart()
  renderAlertChart()
})

const levelTagType = (level: number) => {
  if (level >= 3) return 'danger'
  if (level === 2) return 'warning'
  if (level === 1) return 'warning'
  return 'info'
}

const levelLabel = (level: number) => ['信息', '警告', '错误', '严重'][level] || '未知'

onMounted(load)
</script>

<template>
  <el-space direction="vertical" fill size="20" class="dashboard">
    <!-- KPI 卡片 -->
    <el-row :gutter="16">
      <el-col v-for="k in kpis" :key="k.key" :xs="12" :sm="8" :md="4">
        <div class="kpi-card" :style="{ background: k.bg }">
          <div class="kpi-icon" :style="{ color: k.color }">
            <el-icon :size="22"><component :is="k.icon" /></el-icon>
          </div>
          <p class="kpi-title">{{ k.title }}</p>
          <h2 class="kpi-value">{{ data?.[k.key] ?? '-' }}</h2>
        </div>
      </el-col>
    </el-row>

    <!-- 图表 -->
    <el-row :gutter="16">
      <el-col :md="10" :xs="24">
        <el-card class="chart-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>设备状态分布</span>
              <span class="card-sub">实时</span>
            </div>
          </template>
          <div ref="deviceChartRef" class="chart" />
        </el-card>
      </el-col>

      <el-col :md="14" :xs="24">
        <el-card class="chart-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>预警等级分布</span>
              <span class="card-sub">最近 50 条</span>
            </div>
          </template>
          <div ref="alertChartRef" class="chart" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 最新预警表 -->
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>最近预警</span>
          <el-button type="primary" link @click="$router.push('/admin/alerts')">查看全部 →</el-button>
        </div>
      </template>
      <el-table :data="recentAlerts.slice(0, 8)" v-loading="loading" :show-header="true">
        <el-table-column prop="alertTitle" label="标题" min-width="160" />
        <el-table-column label="等级" width="100">
          <template #default="{ row }">
            <el-tag :type="levelTagType(row.alertLevel)" effect="light" round>{{ levelLabel(row.alertLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alertType" label="类型" width="120" />
        <el-table-column prop="alertContent" label="内容" min-width="240" show-overflow-tooltip />
        <el-table-column prop="alertTime" label="时间" width="180" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 0 ? 'danger' : row.status === 1 ? 'warning' : 'success'">
              {{ row.status === 0 ? '未读' : row.status === 1 ? '已读' : '已处理' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </el-space>
</template>

<style scoped>
.dashboard :deep(.el-card__header) {
  padding: 14px 18px;
}

.kpi-card {
  border-radius: 14px;
  padding: 18px 20px;
  position: relative;
  overflow: hidden;
  transition: transform 0.18s ease;
}
.kpi-card:hover {
  transform: translateY(-2px);
}

.kpi-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 14px;
}

.kpi-title {
  margin: 0;
  color: #475569;
  font-size: 13px;
}

.kpi-value {
  margin: 8px 0 0;
  font-size: 30px;
  color: #0f172a;
  font-weight: 700;
}

.chart-card {
  border-radius: 12px;
  border: none;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #0f172a;
}

.card-sub {
  font-weight: 400;
  font-size: 12px;
  color: #94a3b8;
}

.chart {
  height: 300px;
}
</style>
