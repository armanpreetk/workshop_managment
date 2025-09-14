import { Route, Routes, Navigate } from 'react-router-dom'
import LoginPage from './pages/LoginPage'
import AdminDashboard from './pages/AdminDashboard'
import ManagerDashboard from './pages/ManagerDashboard'
import EmployeeDashboard from './pages/EmployeeDashboard'
import { AuthProvider, useAuth } from './auth/AuthContext'

function RequireRole({ role, children }: { role: 'ADMIN'|'MANAGER'|'EMPLOYEE', children: JSX.Element }) {
  const { user } = useAuth()
  if (!user) return <Navigate to="/" replace />
  if (user.type !== role) return <Navigate to="/" replace />
  return children
}

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/" element={<LoginPage />} />
        <Route path="/admin" element={<RequireRole role="ADMIN"><AdminDashboard /></RequireRole>} />
        <Route path="/manager" element={<RequireRole role="MANAGER"><ManagerDashboard /></RequireRole>} />
        <Route path="/employee" element={<RequireRole role="EMPLOYEE"><EmployeeDashboard /></RequireRole>} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </AuthProvider>
  )
}
