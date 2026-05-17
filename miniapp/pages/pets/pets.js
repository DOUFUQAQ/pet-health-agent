// pages/pets/pets.js
const api = require('../../utils/api')
const { requireLogin } = require('../../utils/auth')

const SPECIES_LABEL = { dog: '狗狗', cat: '猫咪' }
const GENDER_LABEL = { 0: '母', 1: '公' }

Page({
  data: { list: [] },

  async onShow() {
    if (!requireLogin()) return
    await this.load()
  },

  async load() {
    try {
      const list = await api.pet.list()
      this.setData({
        list: (list || []).map((p) => ({
          ...p,
          speciesLabel: SPECIES_LABEL[p.species] || p.species,
          genderLabel: GENDER_LABEL[p.gender] || '-',
        })),
      })
    } catch (e) {}
  },

  goAdd() {
    wx.navigateTo({ url: '/pages/pet-edit/pet-edit' })
  },

  goEdit(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: `/pages/pet-edit/pet-edit?id=${id}` })
  },

  onRemove(e) {
    const { id, name } = e.currentTarget.dataset
    wx.showModal({
      title: '删除宠物',
      content: `确认删除「${name}」？相关健康数据仍会保留，但无法再绑定设备。`,
      confirmColor: '#ef4444',
      success: async (res) => {
        if (!res.confirm) return
        try {
          await api.pet.remove(id)
          wx.showToast({ title: '已删除', icon: 'success' })
          this.load()
        } catch (e) {}
      },
    })
  },
})
