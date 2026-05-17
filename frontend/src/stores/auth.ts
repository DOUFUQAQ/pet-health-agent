import { ref } from 'vue'
import { defineStore } from 'pinia'
import { api } from '../api/services'

export interface UserInfo {
  id: number
  username: string
  phone?: string
  email?: string
  nickname?: string
  avatar?: string
  role?: number
  status?: number
  token?: string
}

const TOKEN_KEY = 'pet-health-token'
const USER_KEY = 'pet-health-user'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const user = ref<UserInfo | null>(
    localStorage.getItem(USER_KEY) ? JSON.parse(localStorage.getItem(USER_KEY) || '{}') : null,
  )

  const setAuth = (nextToken: string, nextUser: UserInfo | null) => {
    token.value = nextToken
    user.value = nextUser

    if (nextToken) {
      localStorage.setItem(TOKEN_KEY, nextToken)
    } else {
      localStorage.removeItem(TOKEN_KEY)
    }

    if (nextUser) {
      localStorage.setItem(USER_KEY, JSON.stringify(nextUser))
    } else {
      localStorage.removeItem(USER_KEY)
    }
  }

  const login = async (username: string, password: string) => {
    const data = await api.auth.login({ username, password })
    setAuth(data.token || '', data)
  }

  const fetchUserInfo = async () => {
    const data = await api.user.info()
    user.value = data
    localStorage.setItem(USER_KEY, JSON.stringify(data))
    return data
  }

  const logout = async () => {
    try {
      if (token.value) {
        await api.auth.logout()
      }
    } finally {
      setAuth('', null)
    }
  }

  return {
    token,
    user,
    login,
    fetchUserInfo,
    logout,
  }
})
