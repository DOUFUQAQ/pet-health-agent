// pages/profile/profile.js
const api = require('../../utils/api')
const { setAuth, requireLogin } = require('../../utils/auth')

Page({
  data: { user: null, petCount: 0, deviceCount: 0 },

  async onShow() {
    if (!requireLogin()) return
    const app = getApp()
    let user = app.globalData.user
    if (!user) {
      try { user = await api.user.info() } catch (e) {}
    }
    this.setData({ user })
    try {
      const [pets, devices] = await Promise.all([api.pet.list(), api.device.list()])
      this.setData({ petCount: pets.length || 0, deviceCount: devices.length || 0 })
    } catch (e) {}
  },

  goEdit() {
    wx.navigateTo({ url: '/pages/profile-edit/profile-edit' })
  },

  goCamera() {
    wx.navigateTo({ url: '/pages/camera/camera' })
  },

  goLocation() {
    wx.navigateTo({ url: '/pages/location/location' })
  },

  goChat() {
    wx.navigateTo({ url: '/pages/chat/chat' })
  },

  goCareLogs() {
    wx.navigateTo({ url: '/pages/carelogs/carelogs' })
  },

  async onLogout() {
    const ok = await new Promise((resolve) => {
      wx.showModal({ title: '提示', content: '确认退出登录吗？', success: (r) => resolve(r.confirm) })
    })
    if (!ok) return
    try { await api.auth.logout() } catch (e) {}
    setAuth('', null)
    wx.reLaunch({ url: '/pages/login/login' })
  },
})
