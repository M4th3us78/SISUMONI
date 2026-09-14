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
    const dadosUsuario = {
      nome: data.nome,
      email: data.email,
      perfil: data.perfil,
      senhaProvisoria: data.senhaProvisoria,
    }
    localStorage.setItem('token', data.token)
    localStorage.setItem('usuario', JSON.stringify(dadosUsuario))
    setUsuario(dadosUsuario)
    navigate(data.senhaProvisoria ? '/trocar-senha' : '/classificacao')
  }

  function logout() {
    localStorage.removeItem('token')
    localStorage.removeItem('usuario')
    setUsuario(null)
    navigate('/login')
  }

  // Chamado depois que o usuário troca a senha provisória com sucesso,
  // pra liberar o resto do sistema sem precisar logar de novo
  function senhaAlterada() {
    setUsuario((atual) => {
      const novo = { ...atual, senhaProvisoria: false }
      localStorage.setItem('usuario', JSON.stringify(novo))
      return novo
    })
  }

  return (
    <AuthContext.Provider value={{ usuario, login, logout, senhaAlterada }}>
      {children}
    </AuthContext.Provider>
  )
}

// eslint-disable-next-line react-refresh/only-export-components -- hook de consumo do contexto mora junto do provider
export function useAuth() {
  return useContext(AuthContext)
}