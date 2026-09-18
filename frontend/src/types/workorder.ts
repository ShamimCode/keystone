export type WorkOrderStatus =
  | 'NEW' | 'ASSIGNED' | 'IN_PROGRESS' | 'ON_HOLD' | 'COMPLETED' | 'CLOSED' | 'CANCELLED'

export type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'

export interface WorkOrder {
  id: string
  code: string
  title: string
  description: string | null
  priority: Priority
  status: WorkOrderStatus
  customerId: string
  customerName: string
  siteId: string
  siteName: string
  assignedToId: string | null
  assignedToName: string | null
  slaDueAt: string | null
  createdAt: string
  updatedAt: string
}

export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
}

export interface Customer {
  id: string
  name: string
  contactEmail: string | null
  createdAt: string
}

export interface Site {
  id: string
  customerId: string
  customerName: string
  name: string
  address: string
  createdAt: string
}