// pages/dashboard/dashboard.js
const api = require('../../utils/api')
const { requireLogin } = require('../../utils/auth')

Page({
  data: {
    pets: [],
    petIndex: 0,
    dashboard: null,
    loading: false,
  },

  async onShow() {
    if (!requireLogin()) return
    if (!this.data.pets.length) await this.loadPets()
    await this.loadDashboard()
  },

  async onPullDownRefresh() {
    await this.loadPets()
    await this.loadDashboard()
    wx.stopPullDownRefresh()
  },

  async loadPets() {
    try {
      const list = await api.pet.list()
      this.setData({ pets: list || [] })
    } catch (e) {}
  },

  onPickPet(e) {
    this.setData({ petIndex: Number(e.detail.value) })
    this.loadDashboard()
  },

  async loadDashboard() {
    const pet = this.data.pets[this.data.petIndex]
    if (!pet) return
    this.setData({ loading: true })
    try {
      const d = await api.health.dashboard(pet.id)
      this.setData({ dashboard: d })
    } finally {
      this.setData({ loading: false })
    }
  },

  goChat() {
    wx.navigateTo({ url: '/pages/chat/chat' })
  },
  goCareLogs() {
    wx.navigateTo({ url: '/pages/carelogs/carelogs' })
  },
  goLocation() {
    wx.navigateTo({ url: '/pages/location/location' })
  },
})
