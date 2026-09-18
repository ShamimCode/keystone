import { api } from './client'

export interface UserSummary {
  id: string
  name: string
  email: string
  role: string
}

export async function listTechnicians(): Promise<UserSummary[]> {
  const { data } = await api.get<UserSummary[]>('/users', { params: { role: 'TECHNICIAN' } })
  return data
}