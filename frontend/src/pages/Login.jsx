import { useState } from 'react'
import { useAuth } from '../contexts/AuthContext'
import logoCompleta from '../assets/MONI (7).png'

export default function Login() {
  const { login } = useAuth()
  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [erro, setErro] = useState('')
  const [carregando, setCarregando] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setErro('')
    setCarregando(true)
    try {
      await login(email, senha)
    } catch (err) {
      setErro('Email ou senha incorretos')
    } finally {
      setCarregando(false)
    }
  }

  return (
    <div className="min-h-screen bg-bg flex items-center justify-center">
      <div className="card p-8 w-full max-w-sm">

        <div className="mb-8 text-center">
          <img src={logoCompleta} alt="SisuMoni" className="h-20 w-auto mx-auto" />
          <p className="text-sm text-text2 mt-1">Sistema de Monitoria — K0</p>
        </div>

        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <div className="flex flex-col gap-1">
            <label className="field-label">E-mail</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="seu@email.com"
              required
              className="field"
            />
          </div>

          <div className="flex flex-col gap-1">
            <label className="field-label">Senha</label>
            <input
              type="password"
              value={senha}
              onChange={(e) => setSenha(e.target.value)}
              placeholder="••••••••"
              required
              className="field"
            />
          </div>

          {erro && (
            <p className="text-sm text-danger bg-danger-dim border border-danger/30 rounded-lg px-3 py-2">
              {erro}
            </p>
          )}

          <button
            type="submit"
            disabled={carregando}
            className="bg-gold text-bg rounded-lg py-2 text-sm font-semibold hover:bg-gold-light disabled:opacity-50 disabled:cursor-not-allowed mt-2 transition-colors"
          >
            {carregando ? 'Entrando...' : 'Entrar'}
          </button>
        </form>
      </div>
    </div>
  )
}
