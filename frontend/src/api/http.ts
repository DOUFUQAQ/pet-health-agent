import axios, { type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'

interface ApiResult<T> {
  code: number
  message: string
  data: T
  timestamp: number
}

const instance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  timeout: 20000,
})

instance.interceptors.request.use((config) => {
  const token = localStorage.getItem('pet-health-token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

instance.interceptors.response.use(
  (response) => {
    const result = response.data as ApiResult<unknown>
    if (result && typeof result.code === 'number') {
      if (result.code === 200) {
        return result.data
      }
      ElMessage.error(result.message || 'Request failed')
      return Promise.reject(new Error(result.message || 'Request failed'))
    }
    return response.data
  },
  (error) => {
    const status = error?.response?.status
    if (status === 401) {
      localStorage.removeItem('pet-health-token')
      localStorage.removeItem('pet-health-user')
      ElMessage.error('登录已过期，请重新登录')
      if (!location.pathname.includes('/login')) {
        location.href = '/login'
      }
    } else {
      ElMessage.error(error?.response?.data?.message || error?.message || '网络错误')
    }
    return Promise.reject(error)
  },
)

export const http = {
  get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return instance.get(url, config) as unknown as Promise<T>
  },
  post<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
    return instance.post(url, data, config) as unknown as Promise<T>
  },
  put<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
    return instance.put(url, data, config) as unknown as Promise<T>
  },
  delete<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return instance.delete(url, config) as unknown as Promise<T>
  },
}
