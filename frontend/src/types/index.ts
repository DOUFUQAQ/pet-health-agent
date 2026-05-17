export interface PetVO {
  id: number
  userId: number
  name: string
  species: string
  breed?: string
  gender: number
  birthday?: string
  weight?: number
  avatar?: string
  medicalHistory?: string
  allergies?: string
  vaccinationRecords?: string
  status?: number
  age?: number
  healthScore?: number
  deviceCount?: number
}

export interface DeviceEntity {
  id: number
  deviceId: string
  deviceName: string
  deviceType: string
  userId: number
  petId: number
  status: number
  battery?: number
  firmwareVersion?: string
  lastOnlineTime?: string
}

export interface AlertEntity {
  id: number
  petId: number
  userId: number
  alertType: string
  alertLevel: number
  alertTitle: string
  alertContent: string
  suggestion?: string
  status: number
  alertTime: string
}

export interface CareLogEntity {
  id: number
  petId: number
  userId: number
  logType: string
  logTitle: string
  logContent?: string
  logTime: string
  reminderTime?: string
}

export interface ChatHistoryEntity {
  id: number
  petId?: number
  sessionId: string
  messageType: string
  userMessage: string
  mediaUrl?: string
  aiResponse: string
  modelUsed?: string
  createTime: string
}

export interface DashboardTrend {
  date: string
  value: number
}

export interface HealthDashboardVO {
  petId: number
  petName: string
  healthScore: number
  currentTemperature?: number
  currentHeartRate?: number
  currentEnvTemp?: number
  currentHumidity?: number
  todaySteps?: number
  todaySleep?: number
  todayFood?: number
  todayWater?: number
  unreadAlerts?: number
  temperatureTrend?: DashboardTrend[]
  heartRateTrend?: DashboardTrend[]
  activityTrend?: DashboardTrend[]
  aiAnalysis?: string
}

export interface AdminOverview {
  totalUsers: number
  totalPets: number
  totalDevices: number
  onlineDevices: number
  unreadAlerts: number
  todayAlerts: number
}

export interface AdminUserEntity {
  id: number
  username: string
  nickname?: string
  phone?: string
  email?: string
  role: number
  status: number
  createTime?: string
}
