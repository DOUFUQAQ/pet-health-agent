// pages/login/login.js
const api = require('../../utils/api')
const { setAuth } = require('../../utils/auth')

Page({
  data: {
    username: '',
    password: '',
    loading: false,
  },
  onUsernameInput(e) { this.setData({ username: e.detail.value }) },
  onPasswordInput(e) { this.setData({ password: e.detail.value }) },
  async onLogin() {
    const { username, password } = this.data
    if (!username || !password) {
      wx.showToast({ title: '请输入账号密码', icon: 'none' })
      return
    }
    this.setData({ loading: true })
    try {
      const data = await api.auth.login({ username, password })
      if (data.role === 2) {
        wx.showToast({ title: '管理员请用 Web 后台登录', icon: 'none' })
        return
      }
      setAuth(data.token || '', data)
      wx.switchTab({ url: '/pages/dashboard/dashboard' })
    } catch (e) {
      // toast 已由 request 拦截器抛出
    } finally {
      this.setData({ loading: false })
    }
  },
  goRegister() {
    wx.navigateTo({ url: '/pages/register/register' })
  },
})
