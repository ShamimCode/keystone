import { useEffect, useState } from 'react'
import { Plus, Wrench, Building2, MapPin } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import Layout from '../components/Layout'
import { Card } from '../components/Card'
import { StatusBadge, PriorityBadge } from '../components/StatusBadge'
import { listWorkOrders, createWorkOrder, assignWorkOrder, changeStatus, logTime } from '../api/workorders'
import { listCustomers, listSites, createCustomer, createSite } from '../api/customers'
import { listTechnicians, type UserSummary } from '../api/users'
import type { WorkOrder, Customer, Site, Priority } from '../types/workorder'

export default function DashboardPage() {
  const { user } = useAuth()
  const [workOrders, setWorkOrders] = useState<WorkOrder[]>([])
  const [customers, setCustomers] = useState<Customer[]>([])
  const [sites, setSites] = useState<Site[]>([])
  const [technicians, setTechnicians] = useState<UserSummary[]>([])
  const [loading, setLoading] = useState(true)
  const [showNewOrder, setShowNewOrder] = useState(false)
  const [showManage, setShowManage] = useState(false)

  async function refresh() {
    const woPage = await listWorkOrders()
    setWorkOrders(woPage.content)
    if (user?.role === 'DISPATCHER' || user?.role === 'MANAGER') {
      const [custPage, sitePage, techs] = await Promise.all([
        listCustomers(),
        listSites(),
        listTechnicians(),
      ])
      setCustomers(custPage.content)
      setSites(sitePage.content)
      setTechnicians(techs)
    }
  }

  useEffect(() => {
    refresh().finally(() => setLoading(false))
  }, [])

  if (loading) {
    return (
      <Layout>
        <p className="text-gray-500">Loading...</p>
      </Layout>
    )
  }

  const isDispatcherOrManager = user?.role === 'DISPATCHER' || user?.role === 'MANAGER'
  const isTechnician = user?.role === 'TECHNICIAN'
  const isCustomer = user?.role === 'CUSTOMER'

  return (
    <Layout>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">
            {isCustomer ? 'Your requests' : isTechnician ? 'Your jobs' : 'Work orders'}
          </h1>
          <p className="text-sm text-gray-500">
            {workOrders.length} {workOrders.length === 1 ? 'order' : 'orders'}
          </p>
        </div>
        {isDispatcherOrManager && (
          <div className="flex gap-2">
            <button
              onClick={() => setShowManage(!showManage)}
              className="flex items-center gap-2 bg-white border border-gray-300 text-gray-700 px-4 py-2 rounded-lg text-sm font-medium hover:bg-gray-50"
            >
              Manage customers/sites
            </button>
            <button
              onClick={() => setShowNewOrder(true)}
              className="flex items-center gap-2 bg-indigo-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-indigo-700"
            >
              <Plus size={16} />
              New work order
            </button>
          </div>
        )}
      </div>

      {showManage && <CustomerSiteManager customers={customers} onChanged={refresh} />}

      {showNewOrder && (
        <NewWorkOrderForm
          customers={customers}
          sites={sites}
          onClose={() => setShowNewOrder(false)}
          onCreated={() => {
            setShowNewOrder(false)
            refresh()
          }}
        />
      )}

      <div className="grid grid-cols-1 gap-4">
        {workOrders.length === 0 && (
          <Card>
            <p className="text-gray-500 text-sm">No work orders yet.</p>
          </Card>
        )}
        {workOrders.map((wo) => (
          <WorkOrderCard
            key={wo.id}
            wo={wo}
            technicians={technicians}
            role={user!.role}
            onChanged={refresh}
          />
        ))}
      </div>
    </Layout>
  )
}

function WorkOrderCard({
  wo,
  technicians,
  role,
  onChanged,
}: {
  wo: WorkOrder
  technicians: UserSummary[]
  role: string
  onChanged: () => void
}) {
  const [busy, setBusy] = useState(false)
  const [selectedTech, setSelectedTech] = useState('')

  async function handleAssign() {
    if (!selectedTech) return
    setBusy(true)
    try {
      await assignWorkOrder(wo.id, selectedTech)
      onChanged()
    } finally {
      setBusy(false)
    }
  }

  async function handleTransition(toStatus: string) {
    setBusy(true)
    try {
      await changeStatus(wo.id, toStatus)
      onChanged()
    } finally {
      setBusy(false)
    }
  }

  return (
    <Card>
      <div className="flex items-start justify-between mb-3">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <span className="text-xs font-mono text-gray-400">{wo.code}</span>
            <StatusBadge status={wo.status} />
            <PriorityBadge priority={wo.priority} />
          </div>
          <h3 className="font-semibold text-gray-900">{wo.title}</h3>
          <p className="text-sm text-gray-500 mt-0.5">{wo.description}</p>
        </div>
      </div>

      <div className="flex items-center gap-4 text-xs text-gray-500 mb-4">
        <span className="flex items-center gap-1">
          <Building2 size={14} /> {wo.customerName}
        </span>
        <span className="flex items-center gap-1">
          <MapPin size={14} /> {wo.siteName}
        </span>
        {wo.assignedToName && (
          <span className="flex items-center gap-1">
            <Wrench size={14} /> {wo.assignedToName}
          </span>
        )}
      </div>

      {(role === 'DISPATCHER' || role === 'MANAGER') && wo.status === 'NEW' && (
        <div className="flex items-center gap-2">
          <select
            value={selectedTech}
            onChange={(e) => setSelectedTech(e.target.value)}
            className="text-sm border border-gray-300 rounded-lg px-2 py-1.5"
          >
            <option value="">Assign to...</option>
            {technicians.map((t) => (
              <option key={t.id} value={t.id}>{t.name}</option>
            ))}
          </select>
          <button
            onClick={handleAssign}
            disabled={busy || !selectedTech}
            className="bg-indigo-600 text-white px-3 py-1.5 rounded-lg text-sm disabled:opacity-50"
          >
            Assign
          </button>
        </div>
      )}

      {role === 'TECHNICIAN' && (
        <div className="flex gap-2 flex-wrap">
          {wo.status === 'ASSIGNED' && (
            <ActionButton onClick={() => handleTransition('IN_PROGRESS')} disabled={busy}>Start</ActionButton>
          )}
          {wo.status === 'IN_PROGRESS' && (
            <>
              <ActionButton onClick={() => handleTransition('ON_HOLD')} disabled={busy}>Hold</ActionButton>
              <ActionButton onClick={() => handleTransition('COMPLETED')} disabled={busy}>Complete</ActionButton>
              <LogTimeForm workOrderId={wo.id} onLogged={onChanged} />
            </>
          )}
          {wo.status === 'ON_HOLD' && (
            <ActionButton onClick={() => handleTransition('IN_PROGRESS')} disabled={busy}>Resume</ActionButton>
          )}
        </div>
      )}

      {role === 'MANAGER' && wo.status === 'COMPLETED' && (
        <ActionButton onClick={() => handleTransition('CLOSED')} disabled={busy}>Close</ActionButton>
      )}
    </Card>
  )
}

function ActionButton({ onClick, disabled, children }: { onClick: () => void; disabled: boolean; children: string }) {
  return (
    <button
      onClick={onClick}
      disabled={disabled}
      className="bg-gray-900 text-white px-3 py-1.5 rounded-lg text-sm disabled:opacity-50"
    >
      {children}
    </button>
  )
}

function LogTimeForm({ workOrderId, onLogged }: { workOrderId: string; onLogged: () => void }) {
  const [minutes, setMinutes] = useState('')

  async function submit() {
    if (!minutes) return
    await logTime(workOrderId, Number(minutes))
    setMinutes('')
    onLogged()
  }

  return (
    <div className="flex items-center gap-1">
      <input
        type="number"
        value={minutes}
        onChange={(e) => setMinutes(e.target.value)}
        placeholder="mins"
        className="w-16 text-sm border border-gray-300 rounded-lg px-2 py-1.5"
      />
      <button onClick={submit} className="bg-gray-100 text-gray-700 px-3 py-1.5 rounded-lg text-sm">
        Log time
      </button>
    </div>
  )
}

function NewWorkOrderForm({
  customers,
  sites,
  onClose,
  onCreated,
}: {
  customers: Customer[]
  sites: Site[]
  onClose: () => void
  onCreated: () => void
}) {
  const [customerId, setCustomerId] = useState('')
  const [siteId, setSiteId] = useState('')
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [priority, setPriority] = useState<Priority>('MEDIUM')
  const [saving, setSaving] = useState(false)

  const filteredSites = sites.filter((s) => s.customerId === customerId)

  async function submit() {
    if (!customerId || !siteId || !title) return
    setSaving(true)
    try {
      await createWorkOrder({ customerId, siteId, title, description, priority })
      onCreated()
    } finally {
      setSaving(false)
    }
  }

  return (
    <Card className="mb-4">
      <h3 className="font-semibold mb-3">New work order</h3>
      <div className="grid grid-cols-2 gap-3 mb-3">
        <select
          value={customerId}
          onChange={(e) => { setCustomerId(e.target.value); setSiteId('') }}
          className="border border-gray-300 rounded-lg px-3 py-2 text-sm"
        >
          <option value="">Select customer</option>
          {customers.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
        <select
          value={siteId}
          onChange={(e) => setSiteId(e.target.value)}
          disabled={!customerId}
          className="border border-gray-300 rounded-lg px-3 py-2 text-sm disabled:opacity-50"
        >
          <option value="">Select site</option>
          {filteredSites.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
        </select>
      </div>
      <input
        value={title}
        onChange={(e) => setTitle(e.target.value)}
        placeholder="Title"
        className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm mb-3"
      />
      <textarea
        value={description}
        onChange={(e) => setDescription(e.target.value)}
        placeholder="Description"
        className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm mb-3"
        rows={2}
      />
      <select
        value={priority}
        onChange={(e) => setPriority(e.target.value as Priority)}
        className="border border-gray-300 rounded-lg px-3 py-2 text-sm mb-4"
      >
        <option value="LOW">Low</option>
        <option value="MEDIUM">Medium</option>
        <option value="HIGH">High</option>
        <option value="CRITICAL">Critical</option>
      </select>
      <div className="flex gap-2">
        <button
          onClick={submit}
          disabled={saving}
          className="bg-indigo-600 text-white px-4 py-2 rounded-lg text-sm disabled:opacity-50"
        >
          Create
        </button>
        <button onClick={onClose} className="text-gray-600 px-4 py-2 text-sm">
          Cancel
        </button>
      </div>
    </Card>
  )
}

function CustomerSiteManager({
  customers,
  onChanged,
}: {
  customers: Customer[]
  onChanged: () => void
}) {
  const [custName, setCustName] = useState('')
  const [custEmail, setCustEmail] = useState('')
  const [siteCustomerId, setSiteCustomerId] = useState('')
  const [siteName, setSiteName] = useState('')
  const [siteAddress, setSiteAddress] = useState('')
  const [saving, setSaving] = useState(false)

  async function submitCustomer() {
    if (!custName) return
    setSaving(true)
    try {
      await createCustomer(custName, custEmail)
      setCustName('')
      setCustEmail('')
      onChanged()
    } finally {
      setSaving(false)
    }
  }

  async function submitSite() {
    if (!siteCustomerId || !siteName || !siteAddress) return
    setSaving(true)
    try {
      await createSite(siteCustomerId, siteName, siteAddress)
      setSiteName('')
      setSiteAddress('')
      onChanged()
    } finally {
      setSaving(false)
    }
  }

  return (
    <Card className="mb-4">
      <div className="grid grid-cols-2 gap-6">
        <div>
          <h4 className="font-semibold text-sm mb-2">New customer</h4>
          <input
            value={custName}
            onChange={(e) => setCustName(e.target.value)}
            placeholder="Customer name"
            className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm mb-2"
          />
          <input
            value={custEmail}
            onChange={(e) => setCustEmail(e.target.value)}
            placeholder="Contact email"
            className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm mb-2"
          />
          <button
            onClick={submitCustomer}
            disabled={saving}
            className="bg-gray-900 text-white px-3 py-1.5 rounded-lg text-sm disabled:opacity-50"
          >
            Add customer
          </button>
        </div>
        <div>
          <h4 className="font-semibold text-sm mb-2">New site</h4>
          <select
            value={siteCustomerId}
            onChange={(e) => setSiteCustomerId(e.target.value)}
            className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm mb-2"
          >
            <option value="">Select customer</option>
            {customers.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
          </select>
          <input
            value={siteName}
            onChange={(e) => setSiteName(e.target.value)}
            placeholder="Site name"
            className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm mb-2"
          />
          <input
            value={siteAddress}
            onChange={(e) => setSiteAddress(e.target.value)}
            placeholder="Address"
            className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm mb-2"
          />
          <button
            onClick={submitSite}
            disabled={saving}
            className="bg-gray-900 text-white px-3 py-1.5 rounded-lg text-sm disabled:opacity-50"
          >
            Add site
          </button>
        </div>
      </div>
    </Card>
  )
}