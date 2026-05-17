// pages/register/register.js
const api = require('../../utils/api')
const { setAuth } = require('../../utils/auth')

Page({
  data: {
    form: {
      username: '',
      password: '',
      confirmPassword: '',
      phone: '',
      email: '',
      nickname: '',
    },
    loading: false,
  },
  onInput(e) {
    const key = e.currentTarget.dataset.key
    this.setData({ [`form.${key}`]: e.detail.value })
  },
  validate() {
    const f = this.data.form
    if (!f.username || f.username.length < 3) return '用户名至少 3 位'
    if (!f.password || f.password.length < 6) return '密码至少 6 位'
    if (f.password !== f.confirmPassword) return '两次密码不一致'
    if (!/^1[3-9]\d{9}$/.test(f.phone)) return '手机号格式不正确'
    if (f.email && !/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(f.email)) return '邮箱格式不正确'
    return null
  },
  async onSubmit() {
    const err = this.validate()
    if (err) {
      wx.showToast({ title: err, icon: 'none' })
      return
    }
    this.setData({ loading: true })
    try {
      const data = await api.auth.register(this.data.form)
      setAuth(data.token || '', data)
      wx.showToast({ title: '注册成功', icon: 'success' })
      setTimeout(() => wx.switchTab({ url: '/pages/dashboard/dashboard' }), 600)
    } catch (e) {
      // toast 已由 request 提示
    } finally {
      this.setData({ loading: false })
    }
  },
  goLogin() {
    wx.navigateBack()
  },
})
