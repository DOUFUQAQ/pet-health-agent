// pages/alerts/alerts.js
const api = require('../../utils/api')
const { requireLogin } = require('../../utils/auth')

const LEVEL_LABEL = ['信息', '警告', '错误', '严重']
const LEVEL_TAG = ['tag-info', 'tag-warn', 'tag-warn', 'tag-danger']

Page({
  data: {
    list: [],
    statusFilter: '',
    statusOptions: ['全部', '未读', '已读', '已处理'],
    statusIndex: 0,
  },

  async onShow() {
    if (!requireLogin()) return
    await this.load()
  },

  async onPullDownRefresh() {
    await this.load()
    wx.stopPullDownRefresh()
  },

  async load() {
    const { statusIndex } = this.data
    const params = { pageNum: 1, pageSize: 50 }
    if (statusIndex > 0) params.status = statusIndex - 1
    try {
      const page = await api.alert.list(params)
      const list = (page.records || []).map((a) => ({
        ...a,
        levelLabel: LEVEL_LABEL[a.alertLevel] || '未知',
        levelTag: LEVEL_TAG[a.alertLevel] || 'tag-info',
      }))
      this.setData({ list })
    } catch (e) {}
  },

  onPickStatus(e) {
    this.setData({ statusIndex: Number(e.detail.value) })
    this.load()
  },

  async onMarkRead(e) {
    const { id } = e.currentTarget.dataset
    try {
      await api.alert.read(id)
      wx.showToast({ title: '已标为已读', icon: 'success' })
      this.load()
    } catch (e) {}
  },
})
