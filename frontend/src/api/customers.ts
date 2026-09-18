import { api } from './client'
import type { Page, Customer, Site } from '../types/workorder'

export async function listCustomers(): Promise<Page<Customer>> {
  const { data } = await api.get<Page<Customer>>('/customers', { params: { size: 50 } })
  return data
}

export async function listSites(customerId?: string): Promise<Page<Site>> {
  const { data } = await api.get<Page<Site>>('/sites', {
    params: customerId ? { customerId, size: 50 } : { size: 50 },
  })
  return data
}

export async function createCustomer(name: string, contactEmail: string): Promise<Customer> {
  const { data } = await api.post<Customer>('/customers', { name, contactEmail })
  return data
}

export async function createSite(customerId: string, name: string, address: string): Promise<Site> {
  const { data } = await api.post<Site>('/sites', { customerId, name, address })
  return data
}