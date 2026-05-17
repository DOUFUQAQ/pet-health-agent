// pages/devices/devices.js
const api = require('../../utils/api')
const { requireLogin } = require('../../utils/auth')

const TYPES = [
  { value: 'collar', label: '智能项圈' },
  { value: 'camera', label: '摄像头' },
  { value: 'sensor', label: '传感器' },
  { value: 'feeder', label: '喂食器' },
]

Page({
  data: {
    devices: [],
    pets: [],
    typeOptions: TYPES.map((t) => t.label),
    showBindModal: false,
    binding: false,
    bindForm: {
      deviceId: '',
      deviceName: '',
      typeIndex: 0,
      petIndex: 0,
    },
  },

  async onShow() {
    if (!requireLogin()) return
    await this.load()
  },

  async load() {
    try {
      const [devices, pets] = await Promise.all([api.device.list(), api.pet.list()])
      this.setData({ devices: devices || [], pets: pets || [] })
    } catch (e) {}
  },

  // ---- 控制按钮 ----
  async onControl(e) {
    const { id, command } = e.currentTarget.dataset
    const tipMap = { feed: '喂食指令已下发', restart: '重启指令已下发', sync: '同步指令已下发' }
    try {
      await api.device.control(id, { command, params: {} })
      wx.showToast({ title: tipMap[command] || '指令已发送', icon: 'success' })
    } catch (e) {}
  },

  // ---- 解绑 ----
  onUnbind(e) {
    const { id, name } = e.currentTarget.dataset
    wx.showModal({
      title: '解绑设备',
      content: `确认解绑「${name}」吗？解绑后设备数据将不再归属当前宠物。`,
      success: async (res) => {
        if (!res.confirm) return
        try {
          await api.device.unbind(id)
          wx.showToast({ title: '已解绑', icon: 'success' })
          this.load()
        } catch (e) {}
      },
    })
  },

  // ---- 打开 / 关闭绑定弹窗 ----
  openBind() {
    if (!this.data.pets.length) {
      wx.showToast({ title: '请先在「宠物档案」添加宠物', icon: 'none' })
      return
    }
    this.setData({
      showBindModal: true,
      bindForm: { deviceId: '', deviceName: '', typeIndex: 0, petIndex: 0 },
    })
  },
  closeBind() {
    this.setData({ showBindModal: false })
  },

  onBindInput(e) {
    const key = e.currentTarget.dataset.key
    this.setData({ [`bindForm.${key}`]: e.detail.value })
  },
  onPickType(e) {
    this.setData({ 'bindForm.typeIndex': Number(e.detail.value) })
  },
  onPickPet(e) {
    this.setData({ 'bindForm.petIndex': Number(e.detail.value) })
  },

  // ---- 扫码自动填充 deviceId ----
  scanDevice() {
    wx.scanCode({
      scanType: ['qrCode', 'barCode'],
      success: (res) => {
        // 设备贴的二维码内容直接是 deviceId 字符串，比如 "esp32-demo-001"
        // 也可以是 JSON：{"deviceId":"...","deviceName":"...","deviceType":"collar"}
        let v = res.result || ''
        try {
          const obj = JSON.parse(v)
          if (obj && typeof obj === 'object') {
            this.setData({
              'bindForm.deviceId': obj.deviceId || '',
              'bindForm.deviceName': obj.deviceName || this.data.bindForm.deviceName,
            })
            if (obj.deviceType) {
              const idx = TYPES.findIndex((t) => t.value === obj.deviceType)
              if (idx >= 0) this.setData({ 'bindForm.typeIndex': idx })
            }
            return
          }
        } catch (e) { /* not json, treat as plain id */ }
        this.setData({ 'bindForm.deviceId': v })
      },
      fail: () => {
        wx.showToast({ title: '已取消扫码', icon: 'none' })
      },
    })
  },

  async onSubmitBind() {
    const f = this.data.bindForm
    if (!f.deviceId) { wx.showToast({ title: '请填写或扫描设备 ID', icon: 'none' }); return }
    if (!f.deviceName) { wx.showToast({ title: '请填写设备名称', icon: 'none' }); return }
    const pet = this.data.pets[f.petIndex]
    if (!pet) { wx.showToast({ title: '请选择宠物', icon: 'none' }); return }

    this.setData({ binding: true })
    try {
      await api.device.bind({
        deviceId: f.deviceId.trim(),
        deviceName: f.deviceName.trim(),
        deviceType: TYPES[f.typeIndex].value,
        petId: pet.id,
      })
      wx.showToast({ title: '绑定成功', icon: 'success' })
      this.setData({ showBindModal: false })
      this.load()
    } catch (e) {
    } finally {
      this.setData({ binding: false })
    }
  },
})
