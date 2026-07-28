import { NavLink } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'

export default function Layout({ children }) {
  const { usuario, logout } = useAuth()
  const ehAdmin = usuario?.perfil === 'ADMIN'

  const linkClasse = ({ isActive }) =>
    `block px-3 py-2 rounded-lg text-sm transition-colors ${
      isActive
        ? 'bg-indigo-50 text-indigo-700 font-medium'
        : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900'
    }`

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="h-13 bg-white border-b border-gray-200 flex items-center justify-between px-5 sticky top-0 z-20" style={{ height: 52 }}>
        <span className="text-[15px] font-semibold">
          SISU<span className="text-indigo-600">MONI</span>
        </span>
        <div className="flex items-center gap-3 text-sm text-gray-600">
          {ehAdmin && (
            <span className="text-[11px] font-medium bg-gray-900 text-white px-2 py-0.5 rounded-full">
              Administrador
            </span>
          )}
          <span>{usuario?.nome}</span>
          <button onClick={logout} className="text-gray-500 hover:text-gray-900">
            Sair
          </button>
        </div>
      </header>

      <div className="grid" style={{ gridTemplateColumns: '196px 1fr', minHeight: 'calc(100vh - 52px)' }}>
        <nav className="bg-white border-r border-gray-200 p-3 flex flex-col gap-1">
          <p className="text-[10px] uppercase tracking-wider text-gray-400 px-3 pt-2 pb-1">
            Período 2026.1
          </p>
          <NavLink to="/classificacao" className={linkClasse}>Classificação</NavLink>
          <NavLink to="/estudantes" className={linkClasse}>Estudantes</NavLink>

          {ehAdmin && (
            <>
              <p className="text-[10px] uppercase tracking-wider text-gray-400 px-3 pt-3 pb-1">
                Cadastros
              </p>
              <NavLink to="/vagas" className={linkClasse}>Vagas</NavLink>
              <NavLink to="/gestao" className={linkClasse}>Turmas e deptos.</NavLink>
              <NavLink to="/operadores" className={linkClasse}>Operadores</NavLink>
            </>
          )}
        </nav>

        <main className="p-6 max-w-5xl">{children}</main>
      </div>
    </div>
  )
}