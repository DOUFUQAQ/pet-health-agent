// utils/auth.js - 登录态管理
const app = getApp()

const setAuth = (token, user) => {
  app.globalData.token = token || ''
  app.globalData.user = user || null
  if (token) wx.setStorageSync('pet-health-token', token)
  else wx.removeStorageSync('pet-health-token')
  if (user) wx.setStorageSync('pet-health-user', user)
  else wx.removeStorageSync('pet-health-user')
}

const requireLogin = (redirect = '/pages/login/login') => {
  const token = app.globalData.token || wx.getStorageSync('pet-health-token')
  if (!token) {
    wx.reLaunch({ url: redirect })
    return false
  }
  return true
}

module.exports = { setAuth, requireLogin }
