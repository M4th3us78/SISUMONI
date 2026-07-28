import { createContext, useContext, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../lib/api'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(() => {
    const salvo = localStorage.getItem('usuario')
    return salvo ? JSON.parse(salvo) : null
  })
  const navigate = useNavigate()

  async function login(email, senha) {
    const { data } = await api.post('/auth/login', { email, senha })
    localStorage.setItem('token', data.token)
    localStorage.setItem('usuario', JSON.stringify({
      nome: data.nome,
      email: data.email,
      perfil: data.perfil,
    }))
    setUsuario({ nome: data.nome, email: data.email, perfil: data.perfil })
    navigate('/classificacao')
  }

  function logout() {
    localStorage.removeItem('token')
    localStorage.removeItem('usuario')
    setUsuario(null)
    navigate('/login')
  }

  return (
    <AuthContext.Provider value={{ usuario, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}