// utils/request.js - 网络请求封装，自动带 JWT，统一处理 Result<T> 包装
const app = getApp()

const request = (url, options = {}) => {
  return new Promise((resolve, reject) => {
    const token = app.globalData.token || wx.getStorageSync('pet-health-token')
    const header = options.header || {}
    if (token) header.Authorization = `Bearer ${token}`
    if (!header['Content-Type']) header['Content-Type'] = 'application/json'

    wx.request({
      url: app.globalData.baseUrl + url,
      method: options.method || 'GET',
      data: options.data,
      header,
      timeout: options.timeout || 8000,
      success: (res) => {
        if (res.statusCode === 401) {
          wx.removeStorageSync('pet-health-token')
          wx.removeStorageSync('pet-health-user')
          app.globalData.token = ''
          app.globalData.user = null
          wx.reLaunch({ url: '/pages/login/login' })
          reject(new Error('登录已过期'))
          return
        }
        const body = res.data
        if (body && typeof body.code === 'number') {
          if (body.code === 200) {
            resolve(body.data)
          } else {
            wx.showToast({ title: body.message || '请求失败', icon: 'none' })
            reject(new Error(body.message || 'biz_error'))
          }
        } else {
          resolve(body)
        }
      },
      fail: (err) => {
        const msg = err.errMsg || ''
        let tip = '网络异常'
        if (msg.includes('timeout')) {
          tip = `请求超时 — 请检查 baseUrl(${app.globalData.baseUrl}) 是否可达 / 后端是否在跑 / 防火墙`
        } else if (msg.includes('fail')) {
          tip = '连接失败：' + msg
        }
        console.error('[request fail]', url, msg)
        wx.showToast({ title: tip, icon: 'none', duration: 3000 })
        reject(err)
      },
    })
  })
}

const get = (url, params) => {
  if (params && Object.keys(params).length) {
    const qs = Object.entries(params)
      .filter(([, v]) => v !== undefined && v !== null && v !== '')
      .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
      .join('&')
    if (qs) url = `${url}?${qs}`
  }
  return request(url, { method: 'GET' })
}

const post = (url, data) => request(url, { method: 'POST', data })
const put = (url, data) => request(url, { method: 'PUT', data })
const del = (url) => request(url, { method: 'DELETE' })

module.exports = { request, get, post, put, del }
