// pages/pet-edit/pet-edit.js
const api = require('../../utils/api')
const { requireLogin } = require('../../utils/auth')

const SPECIES = [
  { value: 'dog', label: '狗狗' },
  { value: 'cat', label: '猫咪' },
]
const GENDER = [
  { value: 0, label: '母' },
  { value: 1, label: '公' },
]

Page({
  data: {
    isEdit: false,
    petId: null,
    speciesOptions: SPECIES.map((s) => s.label),
    genderOptions: GENDER.map((g) => g.label),
    today: '',
    form: {
      name: '',
      speciesIndex: 0,
      breed: '',
      genderIndex: 0,
      birthday: '',
      weight: '',
      medicalHistory: '',
      allergies: '',
    },
    saving: false,
  },

  async onLoad(options) {
    if (!requireLogin()) return
    const today = new Date()
    this.setData({ today: today.toISOString().slice(0, 10) })

    if (options.id) {
      this.setData({ isEdit: true, petId: Number(options.id) })
      wx.setNavigationBarTitle({ title: '编辑宠物' })
      try {
        const pet = await api.pet.detail(Number(options.id))
        if (pet) {
          this.setData({
            form: {
              name: pet.name || '',
              speciesIndex: SPECIES.findIndex((s) => s.value === pet.species) >= 0
                ? SPECIES.findIndex((s) => s.value === pet.species) : 0,
              breed: pet.breed || '',
              genderIndex: pet.gender || 0,
              birthday: pet.birthday || '',
              weight: pet.weight ? String(pet.weight) : '',
              medicalHistory: pet.medicalHistory || '',
              allergies: pet.allergies || '',
            },
          })
        }
      } catch (e) {}
    } else {
      wx.setNavigationBarTitle({ title: '添加宠物' })
    }
  },

  onInput(e) {
    const key = e.currentTarget.dataset.key
    this.setData({ [`form.${key}`]: e.detail.value })
  },
  onPickSpecies(e) { this.setData({ 'form.speciesIndex': Number(e.detail.value) }) },
  onPickGender(e) { this.setData({ 'form.genderIndex': Number(e.detail.value) }) },
  onPickBirthday(e) { this.setData({ 'form.birthday': e.detail.value }) },

  validate() {
    const f = this.data.form
    if (!f.name) return '请填写宠物名称'
    if (f.weight && !/^\d+(\.\d{1,2})?$/.test(f.weight)) return '体重格式不正确'
    return null
  },

  async onSubmit() {
    const err = this.validate()
    if (err) { wx.showToast({ title: err, icon: 'none' }); return }
    const f = this.data.form
    const payload = {
      name: f.name.trim(),
      species: SPECIES[f.speciesIndex].value,
      breed: f.breed || null,
      gender: GENDER[f.genderIndex].value,
      birthday: f.birthday || null,
      weight: f.weight ? Number(f.weight) : null,
      medicalHistory: f.medicalHistory || null,
      allergies: f.allergies || null,
    }
    if (this.data.isEdit) payload.id = this.data.petId

    this.setData({ saving: true })
    try {
      if (this.data.isEdit) await api.pet.update(payload)
      else await api.pet.add(payload)
      wx.showToast({ title: this.data.isEdit ? '已保存' : '已添加', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 600)
    } catch (e) {
    } finally {
      this.setData({ saving: false })
    }
  },
})
