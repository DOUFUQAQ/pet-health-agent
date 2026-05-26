// pages/camera/camera.js
//
// 微信小程序 <image> 不支持 MJPEG 多帧流，采用拉取单帧 JPG 的方式：
// ESP32-CAM CameraWebServer 默认在 80 端口提供 /capture 接口，每次返回一张 JPG。
// 定时刷新 src（带时间戳防缓存），即可形成伪实时视频流，约 200-500ms 一帧。
//
// 由于 ESP32-CAM 不能直接走 https 域名校验，开发者工具需勾「不校验合法域名」。

const STORAGE_KEY = 'esp32cam-ip'

Page({
  data: {
    ip: '',
    streaming: false,
    src: '',
    fps: 0,
    error: '',
  },

  onLoad() {
    const saved = wx.getStorageSync(STORAGE_KEY)
    if (saved) this.setData({ ip: saved })
  },

  onUnload() {
    this.stopStream()
  },

  onIpInput(e) {
    this.setData({ ip: e.detail.value })
  },

  startStream() {
    const ip = (this.data.ip || '').replace(/^https?:\/\//, '').replace(/\/$/, '')
    if (!ip) {
      wx.showToast({ title: '请先填写 CAM 的 IP', icon: 'none' })
      return
    }
    wx.setStorageSync(STORAGE_KEY, ip)
    this.setData({ ip, streaming: true, error: '', src: '' })
    this.lastFrameAt = Date.now()
    this.frameCount = 0
    this.tickFrame()
  },

  stopStream() {
    this.setData({ streaming: false })
    if (this.timer) {
      clearTimeout(this.timer)
      this.timer = null
    }
  },

  tickFrame() {
    if (!this.data.streaming) return
    const ts = Date.now()
    const url = `http://${this.data.ip}/capture?_=${ts}`
    this.setData({ src: url })
  },

  onFrameLoad() {
    if (!this.data.streaming) return
    this.frameCount = (this.frameCount || 0) + 1
    const now = Date.now()
    const delta = now - (this.lastFrameAt || now)
    if (delta >= 1000) {
      const fps = Math.round((this.frameCount * 1000) / delta)
      this.setData({ fps, error: '' })
      this.frameCount = 0
      this.lastFrameAt = now
    }
    // 帧加载完成后立即拉下一帧，不额外等待
    this.timer = setTimeout(() => this.tickFrame(), 0)
  },

  onFrameError() {
    if (!this.data.streaming) return
    this.setData({
      error: '视频帧加载失败：检查 CAM 是否上电、IP 是否在同一网段、是否已烧 CameraWebServer',
      fps: 0,
    })
    // 失败后 1.5s 重试
    this.timer = setTimeout(() => this.tickFrame(), 1500)
  },
})
