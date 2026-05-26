// utils/api.js - 业务接口集合
const { request, get, post, put, del } = require('./request')
const app = getApp()

/**
 * 文件上传（图片或视频），返回 Promise<{ url: string }>
 * @param {'image'|'video'} type
 * @param {string} filePath  wx.chooseMedia 返回的 tempFilePath
 * @param {function} [onProgress]  进度回调 (percent: number) => void
 */
const uploadFile = (type, filePath, onProgress) => {
  return new Promise((resolve, reject) => {
    const token = app.globalData.token || wx.getStorageSync('pet-health-token')
    const task = wx.uploadFile({
      url: app.globalData.baseUrl + '/upload/' + type,
      filePath,
      name: 'file',
      header: token ? { Authorization: 'Bearer ' + token } : {},
      success: (res) => {
        try {
          const body = typeof res.data === 'string' ? JSON.parse(res.data) : res.data
          if (body && body.code === 200) {
            resolve(body.data)
          } else {
            wx.showToast({ title: (body && body.message) || '上传失败', icon: 'none' })
            reject(new Error((body && body.message) || 'upload_error'))
          }
        } catch (e) {
          reject(e)
        }
      },
      fail: (err) => {
        wx.showToast({ title: '上传失败: ' + (err.errMsg || ''), icon: 'none' })
        reject(err)
      },
    })
    if (onProgress && task) {
      task.onProgressUpdate((p) => onProgress(p.progress))
    }
  })
}

const api = {
  auth: {
    login: (data) => post('/auth/login', data),
    register: (data) => post('/auth/register', data),
    logout: () => post('/auth/logout', {}),
  },
  user: {
    info: () => get('/user/info'),
    updateInfo: (data) => put('/user/info', data),
    updatePassword: (data) => put('/user/password', data),
  },
  pet: {
    list: () => get('/pet/list'),
    detail: (id) => get(`/pet/${id}`),
    add: (data) => post('/pet', data),
    update: (data) => put('/pet', data),
    remove: (id) => del(`/pet/${id}`),
  },
  device: {
    list: () => get('/device/list'),
    bind: (data) => post('/device/bind', data),
    unbind: (id) => post(`/device/unbind/${id}`, {}),
    control: (id, payload) => post(`/device/control/${id}`, payload),
  },
  health: {
    dashboard: (petId) => get(`/health/dashboard/${petId}`),
    track: (petId, hours) => get(`/health/track/${petId}`, { hours }),
  },
  alert: {
    list: (params) => get('/alert/list', params),
    unreadCount: () => get('/alert/unread/count'),
    read: (id) => put(`/alert/${id}/read`),
    handle: (id) => put(`/alert/${id}/handle`),
  },
  chat: {
    // AI 调用耗时较长（尤其多模态），统一设置 120s 超时
    send: (data) => request('/chat/send', { method: 'POST', data, timeout: 120000 }),
    // 视频行为分析耗时较长，单独设置 150s 超时
    analyzeVideo: (data) => request('/chat/analyze/video', { method: 'POST', data, timeout: 150000 }),
    sessions: () => get('/chat/sessions'),
    sessionHistory: (sessionId) => get(`/chat/session/${sessionId}`),
    deleteSession: (sessionId) => del(`/chat/session/${sessionId}`),
  },
  upload: {
    image: (filePath, onProgress) => uploadFile('image', filePath, onProgress),
    video: (filePath, onProgress) => uploadFile('video', filePath, onProgress),
  },
  carelog: {
    list: (params) => get('/carelog/list', params),
    add: (data) => post('/carelog', data),
    update: (data) => put('/carelog', data),
    remove: (id) => del(`/carelog/${id}`),
  },
}

module.exports = api
