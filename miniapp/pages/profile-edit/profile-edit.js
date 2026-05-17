// pages/profile-edit/profile-edit.js
const api = require('../../utils/api')
const { setAuth, requireLogin } = require('../../utils/auth')

Page({
  data: {
    form: { nickname: '', phone: '', email: '' },
    pwdForm: { oldPassword: '', newPassword: '', confirmPassword: '' },
    saving: false,
    pwdSaving: false,
  },

  async onLoad() {
    if (!requireLogin()) return
    const app = getApp()
    let user = app.globalData.user
    if (!user) {
      try { user = await api.user.info() } catch (e) {}
    }
    if (user) {
      this.setData({
        form: {
          nickname: user.nickname || '',
          phone: user.phone || '',
          email: user.email || '',
        },
      })
    }
  },

  onInput(e) {
    const key = e.currentTarget.dataset.key
    this.setData({ [`form.${key}`]: e.detail.value })
  },

  onPwdInput(e) {
    const key = e.currentTarget.dataset.key
    this.setData({ [`pwdForm.${key}`]: e.detail.value })
  },

  async onSaveInfo() {
    const f = this.data.form
    if (f.phone && !/^1[3-9]\d{9}$/.test(f.phone)) {
      wx.showToast({ title: '手机号格式不正确', icon: 'none' })
      return
    }
    if (f.email && !/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(f.email)) {
      wx.showToast({ title: '邮箱格式不正确', icon: 'none' })
      return
    }
    this.setData({ saving: true })
    try {
      const updated = await api.user.updateInfo(f)
      setAuth(getApp().globalData.token, updated)
      wx.showToast({ title: '资料已更新', icon: 'success' })
    } catch (e) {
    } finally {
      this.setData({ saving: false })
    }
  },

  async onChangePwd() {
    const p = this.data.pwdForm
    if (!p.oldPassword || !p.newPassword) {
      wx.showToast({ title: '请填写完整密码', icon: 'none' })
      return
    }
    if (p.newPassword.length < 6) {
      wx.showToast({ title: '新密码至少 6 位', icon: 'none' })
      return
    }
    if (p.newPassword !== p.confirmPassword) {
      wx.showToast({ title: '两次新密码不一致', icon: 'none' })
      return
    }
    this.setData({ pwdSaving: true })
    try {
      await api.user.updatePassword({
        oldPassword: p.oldPassword,
        newPassword: p.newPassword,
      })
      wx.showToast({ title: '密码已修改', icon: 'success' })
      this.setData({ pwdForm: { oldPassword: '', newPassword: '', confirmPassword: '' } })
    } catch (e) {
    } finally {
      this.setData({ pwdSaving: false })
    }
  },
})
