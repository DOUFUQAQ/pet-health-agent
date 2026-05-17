// pages/chat/chat.js
const api = require('../../utils/api')
const { requireLogin } = require('../../utils/auth')

const SUGGESTIONS = [
  '我家狗最近不爱吃饭怎么办？',
  '猫咪呕吐黄水是什么原因？',
  '小狗多大可以洗澡？',
  '宠物体温多高算发烧？',
]

const newSessionId = () => 'sess_' + Date.now() + '_' + Math.floor(Math.random() * 1000)

Page({
  data: {
    pets: [],
    petIndex: 0,
    sessionId: '',
    messages: [],
    input: '',
    sending: false,
    suggestions: SUGGESTIONS,
    scrollToView: '',
    // 上传进度（0-100，null 表示未在上传）
    uploadProgress: null,
    uploadType: '',   // 'image' | 'video'
  },

  async onLoad() {
    if (!requireLogin()) return
    this.setData({ sessionId: newSessionId() })
    try {
      const list = await api.pet.list()
      this.setData({ pets: list || [] })
    } catch (e) {}
    this.appendMessage({
      role: 'ai',
      text: '你好，我是基于通义千问的 AI 宠物医生。可以问我宠物饮食、行为、健康、用药等问题，也可以发送图片或视频让我分析宠物行为。',
    })
  },

  onPickPet(e) {
    this.setData({ petIndex: Number(e.detail.value) })
  },

  onInput(e) {
    this.setData({ input: e.detail.value })
  },

  appendMessage(msg) {
    const id = 'msg_' + Date.now() + '_' + Math.floor(Math.random() * 1000)
    const item = { id, ...msg, time: new Date().toLocaleTimeString().slice(0, 5) }
    const messages = this.data.messages.concat(item)
    this.setData({ messages, scrollToView: id })
  },

  pickSuggestion(e) {
    const text = e.currentTarget.dataset.text
    this.setData({ input: text })
  },

  startNewSession() {
    this.setData({ sessionId: newSessionId(), messages: [] })
    this.appendMessage({
      role: 'ai',
      text: '已开启新对话。你可以问我任何宠物相关的问题，或发送图片/视频。',
    })
  },

  // ── 文字发送 ──────────────────────────────────────────────────────────
  async onSend() {
    const text = (this.data.input || '').trim()
    if (!text || this.data.sending) return

    this.appendMessage({ role: 'user', text })
    this.setData({ input: '', sending: true })
    this.appendMessage({ role: 'ai', text: '思考中…', placeholder: true })
    const placeholderId = this.data.messages[this.data.messages.length - 1].id

    const pet = this.data.pets[this.data.petIndex]
    const payload = {
      sessionId: this.data.sessionId,
      petId: pet ? pet.id : null,
      messageType: 'text',
      message: text,
    }

    try {
      const resp = await api.chat.send(payload)
      this._replacePlaceholder(placeholderId, resp && resp.aiResponse ? resp.aiResponse : '（AI 暂无回复）')
    } catch (e) {
      this._replacePlaceholder(placeholderId, '请求失败，请稍后再试', true)
    } finally {
      this.setData({ sending: false })
    }
  },

  // ── 选择图片 ──────────────────────────────────────────────────────────
  onPickImage() {
    if (this.data.sending) return
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const file = res.tempFiles[0]
        this._uploadAndAnalyze('image', file.tempFilePath, file.size)
      },
    })
  },

  // ── 选择视频 ──────────────────────────────────────────────────────────
  onPickVideo() {
    if (this.data.sending) return
    wx.chooseMedia({
      count: 1,
      mediaType: ['video'],
      sourceType: ['album', 'camera'],
      maxDuration: 60,
      success: (res) => {
        const file = res.tempFiles[0]
        this._uploadAndAnalyze('video', file.tempFilePath, file.size)
      },
    })
  },

  // ── 上传 + 分析（图片/视频共用） ──────────────────────────────────────
  async _uploadAndAnalyze(type, filePath, fileSize) {
    const isVideo = type === 'video'

    // 大小预检（图片 20MB，视频 100MB）
    const maxSize = isVideo ? 100 * 1024 * 1024 : 20 * 1024 * 1024
    if (fileSize && fileSize > maxSize) {
      wx.showToast({ title: `文件超过 ${isVideo ? '100' : '20'} MB 限制`, icon: 'none' })
      return
    }

    this.setData({ sending: true, uploadProgress: 0, uploadType: type })

    // 用户侧气泡：显示缩略图或视频标记
    const userBubble = isVideo
      ? { role: 'user', text: '[视频]', mediaType: 'video', mediaPath: filePath }
      : { role: 'user', text: '', mediaType: 'image', mediaPath: filePath }
    this.appendMessage(userBubble)

    // AI 占位
    this.appendMessage({ role: 'ai', text: isVideo ? '正在分析视频行为，请稍候…' : '正在分析图片，请稍候…', placeholder: true })
    const placeholderId = this.data.messages[this.data.messages.length - 1].id

    try {
      // 1. 上传文件，获取服务端 URL
      const uploadResult = await api.upload[type](filePath, (pct) => {
        this.setData({ uploadProgress: pct })
      })
      this.setData({ uploadProgress: null })

      const mediaUrl = uploadResult.url
      const pet = this.data.pets[this.data.petIndex]

      let aiResponse
      if (isVideo) {
        // 视频走专用行为分析接口
        const resp = await api.chat.analyzeVideo({
          sessionId: this.data.sessionId,
          petId: pet ? pet.id : null,
          videoUrl: mediaUrl,
        })
        aiResponse = resp && resp.aiResponse ? resp.aiResponse : '（AI 暂无回复）'
      } else {
        // 图片走 /chat/send（messageType=image）
        const resp = await api.chat.send({
          sessionId: this.data.sessionId,
          petId: pet ? pet.id : null,
          messageType: 'image',
          message: '请分析这张宠物图片，描述宠物的状态和行为，并给出健康建议。',
          mediaUrl,
        })
        aiResponse = resp && resp.aiResponse ? resp.aiResponse : '（AI 暂无回复）'
      }

      this._replacePlaceholder(placeholderId, aiResponse)
    } catch (e) {
      this.setData({ uploadProgress: null })
      this._replacePlaceholder(placeholderId, '分析失败，请稍后再试', true)
    } finally {
      this.setData({ sending: false })
    }
  },

  // ── 工具：替换占位气泡 ────────────────────────────────────────────────
  _replacePlaceholder(id, text, isError = false) {
    const messages = this.data.messages.map((m) =>
      m.id === id ? { ...m, text, placeholder: false, isError } : m
    )
    this.setData({ messages, scrollToView: id })
  },
})
