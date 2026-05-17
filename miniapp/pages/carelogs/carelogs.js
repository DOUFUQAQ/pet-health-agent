// pages/carelogs/carelogs.js
const api = require('../../utils/api')
const { requireLogin } = require('../../utils/auth')

const TYPE_META = {
  feeding:     { label: '喂食',   icon: '🍖', color: 'tag-warn' },
  medication:  { label: '用药',   icon: '💊', color: 'tag-danger' },
  vaccination: { label: '疫苗',   icon: '💉', color: 'tag-info' },
  checkup:     { label: '体检',   icon: '🩺', color: 'tag-success' },
  grooming:    { label: '美容',   icon: '✂️', color: 'tag-info' },
  other:       { label: '其他',   icon: '📌', color: 'tag-info' },
}

const FILTERS = [
  { value: '', label: '全部' },
  { value: 'feeding', label: '喂食' },
  { value: 'medication', label: '用药' },
  { value: 'vaccination', label: '疫苗' },
  { value: 'checkup', label: '体检' },
  { value: 'grooming', label: '美容' },
  { value: 'other', label: '其他' },
]

Page({
  data: {
    pets: [],
    petIndex: 0,
    typeIndex: 0,
    typeOptions: FILTERS.map((f) => f.label),
    list: [],
    pageNum: 1,
    pageSize: 20,
    total: 0,
    loading: false,
    hasMore: true,
  },

  async onShow() {
    if (!requireLogin()) return
    if (!this.data.pets.length) {
      try {
        const list = await api.pet.list()
        this.setData({ pets: list || [] })
      } catch (e) {}
    }
    await this.reload()
  },

  async onPullDownRefresh() {
    await this.reload()
    wx.stopPullDownRefresh()
  },

  onPickPet(e) {
    this.setData({ petIndex: Number(e.detail.value) })
    this.reload()
  },
  onPickType(e) {
    this.setData({ typeIndex: Number(e.detail.value) })
    this.reload()
  },

  async reload() {
    this.setData({ pageNum: 1, list: [], hasMore: true })
    await this.fetch()
  },

  async fetch() {
    const pet = this.data.pets[this.data.petIndex]
    if (!pet) return
    const logType = FILTERS[this.data.typeIndex].value
    this.setData({ loading: true })
    try {
      const page = await api.carelog.list({
        petId: pet.id,
        logType: logType || undefined,
        pageNum: this.data.pageNum,
        pageSize: this.data.pageSize,
      })
      const records = (page.records || []).map((r) => {
        const meta = TYPE_META[r.logType] || TYPE_META.other
        return { ...r, typeLabel: meta.label, typeIcon: meta.icon, typeColor: meta.color }
      })
      const merged = this.data.pageNum === 1 ? records : this.data.list.concat(records)
      this.setData({
        list: merged,
        total: page.total || 0,
        hasMore: merged.length < (page.total || 0),
      })
    } catch (e) {
    } finally {
      this.setData({ loading: false })
    }
  },

  async loadMore() {
    if (!this.data.hasMore || this.data.loading) return
    this.setData({ pageNum: this.data.pageNum + 1 })
    await this.fetch()
  },

  goAdd() {
    if (!this.data.pets.length) {
      wx.showToast({ title: '请先添加宠物档案', icon: 'none' })
      return
    }
    const pet = this.data.pets[this.data.petIndex]
    wx.navigateTo({ url: `/pages/carelog-edit/carelog-edit?petId=${pet.id}` })
  },

  goEdit(e) {
    const id = e.currentTarget.dataset.id
    const pet = this.data.pets[this.data.petIndex]
    wx.navigateTo({ url: `/pages/carelog-edit/carelog-edit?petId=${pet.id}&id=${id}` })
  },

  onRemove(e) {
    const { id, title } = e.currentTarget.dataset
    wx.showModal({
      title: '删除日志',
      content: `确认删除「${title}」？`,
      confirmColor: '#ef4444',
      success: async (res) => {
        if (!res.confirm) return
        try {
          await api.carelog.remove(id)
          wx.showToast({ title: '已删除', icon: 'success' })
          this.reload()
        } catch (e) {}
      },
    })
  },
})
