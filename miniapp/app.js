// app.js
const config = require('./utils/config')

App({
  onLaunch() {
    const token = wx.getStorageSync('pet-health-token')
    const user = wx.getStorageSync('pet-health-user')
    this.globalData.token = token || ''
    this.globalData.user = user || null
    console.log(`[app] env=${config.ENV} baseUrl=${config.BASE_URL}`)
  },
  globalData: {
    // 后端 API 基址 — 由 utils/config.js 集中管理，移植部署只改 config.js
    baseUrl: config.BASE_URL,
    token: '',
    user: null,
  },
})
