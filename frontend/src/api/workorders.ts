import { api } from './client'
import type { Page, WorkOrder, Priority } from '../types/workorder'

export async function listWorkOrders(): Promise<Page<WorkOrder>> {
  const { data } = await api.get<Page<WorkOrder>>('/work-orders', { params: { size: 50 } })
  return data
}

export async function createWorkOrder(payload: {
  customerId: string
  siteId: string
  title: string
  description: string
  priority: Priority
}): Promise<WorkOrder> {
  const { data } = await api.post<WorkOrder>('/work-orders', payload)
  return data
}

export async function assignWorkOrder(id: string, technicianId: string): Promise<WorkOrder> {
  const { data } = await api.post<WorkOrder>(`/work-orders/${id}/assign`, { technicianId })
  return data
}

export async function changeStatus(id: string, toStatus: string, note?: string): Promise<WorkOrder> {
  const { data } = await api.post<WorkOrder>(`/work-orders/${id}/status`, { toStatus, note })
  return data
}

export async function logPartUsage(id: string, partId: string, qtyUsed: number): Promise<void> {
  await api.post(`/work-orders/${id}/parts`, { partId, qtyUsed })
}

export async function logTime(id: string, minutes: number, note?: string): Promise<void> {
  await api.post(`/work-orders/${id}/time`, { minutes, note })
}