import { useAuth } from '../contexts/AuthContext'

export default function Dashboard() {
  const { usuario, logout } = useAuth()

  return (
    <div className="min-h-screen bg-gray-50 p-8">
      <div className="max-w-4xl mx-auto">
        <div className="flex items-center justify-between mb-8">
          <h1 className="text-xl font-semibold text-gray-900">
            SISU<span className="text-indigo-600">MONI</span>
          </h1>
          <div className="flex items-center gap-4">
            <span className="text-sm text-gray-600">{usuario?.nome}</span>
            <button
              onClick={logout}
              className="text-sm text-gray-500 hover:text-gray-900"
            >
              Sair
            </button>
          </div>
        </div>
        <div className="bg-white rounded-2xl border border-gray-200 p-6">
          <p className="text-gray-600">
            ✅ Login funcionando! Bem-vindo, <strong>{usuario?.nome}</strong>.
          </p>
          <p className="text-sm text-gray-400 mt-1">Perfil: {usuario?.perfil}</p>
        </div>
      </div>
    </div>
  )
}