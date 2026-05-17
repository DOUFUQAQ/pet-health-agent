import { http } from './http'
import type {
  AdminOverview,
  AdminUserEntity,
  AlertEntity,
  DeviceEntity,
  PetVO,
} from '../types'
import type { UserInfo } from '../stores/auth'

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
}

export const api = {
  auth: {
    login: (payload: { username: string; password: string }) => http.post<UserInfo>('/auth/login', payload),
    logout: () => http.post<void>('/auth/logout'),
  },
  user: {
    info: () => http.get<UserInfo>('/user/info'),
  },
  admin: {
    overview: () => http.get<AdminOverview>('/admin/overview'),
    users: (params: { pageNum?: number; pageSize?: number; keyword?: string; status?: number; role?: number }) =>
      http.get<PageResult<AdminUserEntity>>('/admin/users', { params }),
    updateUserStatus: (id: number, status: number) =>
      http.put<void>(`/admin/users/${id}/status`, { status }),
    pets: (params: { pageNum?: number; pageSize?: number; keyword?: string }) =>
      http.get<PageResult<PetVO>>('/admin/pets', { params }),
    devices: (params: { pageNum?: number; pageSize?: number; keyword?: string; status?: number }) =>
      http.get<PageResult<DeviceEntity>>('/admin/devices', { params }),
    alerts: (params: { pageNum?: number; pageSize?: number; status?: number; level?: number }) =>
      http.get<PageResult<AlertEntity>>('/admin/alerts', { params }),
  },
}
