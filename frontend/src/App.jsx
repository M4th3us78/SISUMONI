import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from './contexts/AuthContext'
import Layout from './components/Layout'
import Login from './pages/Login'
import TrocarSenha from './pages/TrocarSenha'
import Classificacao from './pages/Classificacao'
import Estudantes from './pages/Estudantes'
import Vagas from './pages/Vagas'
import Gestao from './pages/Gestao'
import Operadores from './pages/Operadores'

// Rota que exige login
function RotaProtegida({ children }) {
  const { usuario } = useAuth()
  if (!usuario) return <Navigate to="/login" replace />
  if (usuario.senhaProvisoria) return <Navigate to="/trocar-senha" replace />
  return children
}

// Rota que exige perfil ADMIN
function RotaAdmin({ children }) {
  const { usuario } = useAuth()
  if (!usuario) return <Navigate to="/login" replace />
  if (usuario.senhaProvisoria) return <Navigate to="/trocar-senha" replace />
  if (usuario.perfil !== 'ADMIN') return <Navigate to="/classificacao" replace />
  return children
}

// Rota da troca obrigatória de senha: só exige estar logado (não checa
// senhaProvisoria, senão vira loop de redirecionamento)
function RotaTrocarSenha({ children }) {
  const { usuario } = useAuth()
  return usuario ? children : <Navigate to="/login" replace />
}

function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/trocar-senha" element={
        <RotaTrocarSenha><TrocarSenha /></RotaTrocarSenha>
      } />

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