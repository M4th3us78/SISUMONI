import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { authApi } from '../lib/recursos'
import logoCompleta from '../assets/MONI (7).png'

export default function TrocarSenha() {
  const { logout, senhaAlterada } = useAuth()
  const navigate = useNavigate()
  const [senhaAtual, setSenhaAtual] = useState('')
  const [senhaNova, setSenhaNova] = useState('')
  const [confirmacao, setConfirmacao] = useState('')
  const [erro, setErro] = useState('')
  const [salvando, setSalvando] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setErro('')

    if (senhaNova.length < 6) {
      setErro('A nova senha deve ter pelo menos 6 caracteres.')
      return
    }
    if (senhaNova !== confirmacao) {
      setErro('A confirmação não bate com a nova senha.')
      return
    }

    setSalvando(true)
    try {
      await authApi.alterarSenha({ senhaAtual, senhaNova })
      senhaAlterada()
      navigate('/classificacao')
    } catch (err) {
      setErro(err.response?.data?.mensagem || 'Erro ao trocar a senha')
    } finally {
      setSalvando(false)
    }
  }

  return (
    <div className="min-h-screen bg-bg flex items-center justify-center">
      <div className="card p-8 w-full max-w-sm">

        <div className="mb-6 text-center">
          <img src={logoCompleta} alt="SisuMoni" className="h-16 w-auto mx-auto" />
          <p className="text-sm text-text1 font-medium mt-3">Troque sua senha para continuar</p>
          <p className="text-xs text-text2 mt-1">
            Sua senha atual é provisória. Defina uma senha só sua antes de acessar o sistema.
          </p>
        </div>

        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <div className="flex flex-col gap-1">
            <label className="field-label">Senha provisória (atual)</label>
            <input
              type="password"
              value={senhaAtual}
              onChange={(e) => setSenhaAtual(e.target.value)}
              placeholder="••••••••"
              required
              className="field"
            />
          </div>

          <div className="flex flex-col gap-1">
            <label className="field-label">Nova senha</label>
            <input
              type="password"
              value={senhaNova}
              onChange={(e) => setSenhaNova(e.target.value)}
              placeholder="Mínimo 6 caracteres"
              required
              className="field"
            />
          </div>

          <div className="flex flex-col gap-1">
            <label className="field-label">Confirmar nova senha</label>
            <input
              type="password"
              value={confirmacao}
              onChange={(e) => setConfirmacao(e.target.value)}
              placeholder="Repita a nova senha"
              required
              className="field"
            />
          </div>

          {erro && (
            <p className="text-sm text-danger bg-danger-dim border-2 border-danger/30 rounded-lg px-3 py-2">
              {erro}
            </p>
          )}

          <button
            type="submit"
            disabled={salvando}
            className="bg-gold text-bg rounded-lg py-2 text-sm font-semibold hover:bg-gold-light disabled:opacity-50 disabled:cursor-not-allowed mt-2 transition-colors"
          >
            {salvando ? 'Salvando...' : 'Trocar senha'}
          </button>

          <button
            type="button"
            onClick={logout}
            className="text-xs text-text3 hover:text-text1 text-center"
          >
            Sair sem trocar
          </button>
        </form>
      </div>
    </div>
  )
}
