import { type ReactNode } from 'react'
import { Wrench, LayoutDashboard, LogOut } from 'lucide-react'
import { useAuth } from '../context/AuthContext'

const roleLabel: Record<string, string> = {
  DISPATCHER: 'Dispatcher',
  TECHNICIAN: 'Technician',
  MANAGER: 'Manager',
  CUSTOMER: 'Customer',
}

export default function Layout({ children }: { children: ReactNode }) {
  const { user, logout } = useAuth()

  return (
    <div className="flex h-screen bg-gray-50">
      <aside className="w-64 bg-white border-r border-gray-200 flex flex-col">
        <div className="flex items-center gap-2 px-6 py-5 border-b border-gray-200">
          <div className="bg-indigo-600 text-white rounded-lg p-2">
            <Wrench size={18} />
          </div>
          <span className="font-bold text-lg text-gray-900">KEYSTONE</span>
        </div>

        <nav className="flex-1 px-4 py-4">
          <div className="flex items-center gap-3 px-3 py-2 rounded-lg bg-indigo-50 text-indigo-700 font-medium text-sm">
            <LayoutDashboard size={18} />
            Dashboard
          </div>
        </nav>

        <div className="px-4 py-4 border-t border-gray-200">
          <div className="px-3 py-2 mb-2">
            <p className="text-sm font-medium text-gray-900">{user?.name}</p>
            <p className="text-xs text-gray-500">{user ? roleLabel[user.role] : ''}</p>
          </div>
          <button
            onClick={logout}
            className="flex items-center gap-3 px-3 py-2 rounded-lg text-gray-600 hover:bg-gray-100 text-sm w-full"
          >
            <LogOut size={18} />
            Log out
          </button>
        </div>
      </aside>

      <main className="flex-1 overflow-y-auto">
        <div className="max-w-6xl mx-auto px-8 py-8">{children}</div>
      </main>
    </div>
  )
}