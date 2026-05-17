// pages/location/location.js
const api = require('../../utils/api')
const { requireLogin } = require('../../utils/auth')

Page({
  data: {
    pets: [],
    petIndex: 0,
    hours: 24,
    centerLat: 39.9087,
    centerLng: 116.3975,
    markers: [],
    polylines: [],
    pointsCount: 0,
    lastTime: '',
    lastSats: '-',
    lastCoord: '-',
  },

  async onShow() {
    if (!requireLogin()) return
    if (!this.data.pets.length) await this.loadPets()
    await this.loadTrack()
  },

  async loadPets() {
    try {
      const list = await api.pet.list()
      this.setData({ pets: list || [] })
    } catch (e) {}
  },

  onPickPet(e) {
    this.setData({ petIndex: Number(e.detail.value) })
    this.loadTrack()
  },

  onPickHours(e) {
    this.setData({ hours: Number(e.detail.value) })
    this.loadTrack()
  },

  async loadTrack() {
    const pet = this.data.pets[this.data.petIndex]
    if (!pet) return
    try {
      const points = await api.health.track(pet.id, this.data.hours)
      if (!points.length) {
        this.setData({
          markers: [], polylines: [], pointsCount: 0,
          lastTime: '—', lastSats: '—', lastCoord: '—',
        })
        return
      }
      const last = points[points.length - 1]
      const polyline = {
        points: points.map((p) => ({ latitude: p.lat, longitude: p.lng })),
        color: '#3b82f6',
        width: 5,
        arrowLine: true,
      }
      const marker = {
        id: 1,
        latitude: last.lat,
        longitude: last.lng,
        iconPath: '/images/marker.png',
        width: 32, height: 32,
        callout: { content: '最后定位', display: 'BYCLICK', color: '#fff', bgColor: '#3b82f6', padding: 6, borderRadius: 6 },
      }
      this.setData({
        centerLat: last.lat,
        centerLng: last.lng,
        markers: [marker],
        polylines: [polyline],
        pointsCount: points.length,
        lastTime: new Date(last.timestamp).toLocaleString(),
        lastSats: last.sats || '-',
        lastCoord: `${last.lat.toFixed(5)}, ${last.lng.toFixed(5)}`,
      })
    } catch (e) {}
  },
})
