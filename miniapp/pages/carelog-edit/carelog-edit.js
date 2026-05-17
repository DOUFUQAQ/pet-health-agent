// pages/carelog-edit/carelog-edit.js
const api = require('../../utils/api')
const { requireLogin } = require('../../utils/auth')

const TYPES = [
  { value: 'feeding', label: '喂食' },
  { value: 'medication', label: '用药' },
  { value: 'vaccination', label: '疫苗' },
  { value: 'checkup', label: '体检' },
  { value: 'grooming', label: '美容' },
  { value: 'other', label: '其他' },
]

// 把 ISO 字符串切成 ['2026-05-17','14:30']
const splitDateTime = (s) => {
  if (!s) return ['', '']
  const t = s.replace('T', ' ')
  const [d, time] = t.split(' ')
  return [d || '', (time || '').slice(0, 5)]
}

Page({
  data: {
    isEdit: false,
    petId: 0,
    logId: null,
    typeOptions: TYPES.map((t) => t.label),
    today: '',
    nowTime: '',
    form: {
      typeIndex: 0,
      logTitle: '',
      logContent: '',
      logDate: '',
      logTime: '',
    },
    saving: false,
  },

  async onLoad(options) {
    if (!requireLogin()) return

    const now = new Date()
    const today = now.toISOString().slice(0, 10)
    const nowTime = now.toTimeString().slice(0, 5)
    this.setData({
      today,
      nowTime,
      'form.logDate': today,
      'form.logTime': nowTime,
      petId: Number(options.petId || 0),
    })

    if (options.id) {
      this.setData({ isEdit: true, logId: Number(options.id) })
      wx.setNavigationBarTitle({ title: '编辑日志' })
      // 通过 list 拉一次回填（后端没有 detail 接口，这里走 petId 列表筛选）
      try {
        const page = await api.carelog.list({ petId: this.data.petId, pageNum: 1, pageSize: 100 })
        const log = (page.records || []).find((r) => r.id === this.data.logId)
        if (log) {
          const [d, t] = splitDateTime(log.logTime)
          this.setData({
            'form.typeIndex': Math.max(0, TYPES.findIndex((x) => x.value === log.logType)),
            'form.logTitle': log.logTitle || '',
            'form.logContent': log.logContent || '',
            'form.logDate': d,
            'form.logTime': t,
          })
        }
      } catch (e) {}
    }
  },

  onInput(e) {
    const key = e.currentTarget.dataset.key
    this.setData({ [`form.${key}`]: e.detail.value })
  },
  onPickType(e) { this.setData({ 'form.typeIndex': Number(e.detail.value) }) },
  onPickDate(e) { this.setData({ 'form.logDate': e.detail.value }) },
  onPickTime(e) { this.setData({ 'form.logTime': e.detail.value }) },

  validate() {
    const f = this.data.form
    if (!f.logTitle) return '请填写标题'
    if (!f.logDate || !f.logTime) return '请选择日期和时间'
    return null
  },

  async onSubmit() {
    const err = this.validate()
    if (err) { wx.showToast({ title: err, icon: 'none' }); return }
    const f = this.data.form
    const payload = {
      petId: this.data.petId,
      logType: TYPES[f.typeIndex].value,
      logTitle: f.logTitle.trim(),
      logContent: f.logContent || null,
      logTime: `${f.logDate}T${f.logTime}:00`,  // ISO 格式
    }
    if (this.data.isEdit) payload.id = this.data.logId

    this.setData({ saving: true })
    try {
      if (this.data.isEdit) await api.carelog.update(payload)
      else await api.carelog.add(payload)
      wx.showToast({ title: this.data.isEdit ? '已保存' : '已添加', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 600)
    } catch (e) {
    } finally {
      this.setData({ saving: false })
    }
  },
})
