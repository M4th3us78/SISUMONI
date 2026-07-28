import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from './contexts/AuthContext'
import Layout from './components/Layout'
import Login from './pages/Login'
import Classificacao from './pages/Classificacao'
import Estudantes from './pages/Estudantes'
import Vagas from './pages/Vagas'
import Gestao from './pages/Gestao'
import Operadores from './pages/Operadores'

// Rota que exige login
function RotaProtegida({ children }) {
  const { usuario } = useAuth()
  return usuario ? children : <Navigate to="/login" replace />
}

// Rota que exige perfil ADMIN
function RotaAdmin({ children }) {
  const { usuario } = useAuth()
  if (!usuario) return <Navigate to="/login" replace />
  if (usuario.perfil !== 'ADMIN') return <Navigate to="/classificacao" replace />
  return children
}

function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />

      <Route path="/classificacao" element={
        <RotaProtegida><Layout><Classificacao /></Layout></RotaProtegida>
      } />
      <Route path="/estudantes" element={
        <RotaProtegida><Layout><Estudantes /></Layout></RotaProtegida>
      } />

      {/* Rotas só de admin */}
      <Route path="/vagas" element={
        <RotaAdmin><Layout><Vagas /></Layout></RotaAdmin>
      } />
      <Route path="/gestao" element={
        <RotaAdmin><Layout><Gestao /></Layout></RotaAdmin>
      } />
      <Route path="/operadores" element={
        <RotaAdmin><Layout><Operadores /></Layout></RotaAdmin>
      } />

      <Route path="*" element={<Navigate to="/classificacao" replace />} />
    </Routes>
  )
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </BrowserRouter>
  )
}