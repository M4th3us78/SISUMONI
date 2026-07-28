import { NavLink } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'

export default function Layout({ children }) {
  const { usuario, logout } = useAuth()
  const ehAdmin = usuario?.perfil === 'ADMIN'

  const linkClasse = ({ isActive }) =>
    `block px-3 py-2 rounded-lg text-sm transition-colors ${
      isActive
        ? 'bg-gold-dim text-gold-light font-medium'
        : 'text-text2 hover:bg-surface2 hover:text-text1'
    }`

  return (
    <div className="min-h-screen bg-bg">
      <header className="h-13 bg-surface border-b border-border flex items-center justify-between px-5 sticky top-0 z-20" style={{ height: 52 }}>
        <span className="text-[15px] font-display font-bold">
          SISU<span className="text-gold">MONI</span>
        </span>
        <div className="flex items-center gap-3 text-sm text-text2">
          {ehAdmin && (
            <span className="badge badge-gold">
              Administrador
            </span>
          )}
          <span>{usuario?.nome}</span>
          <button onClick={logout} className="text-text3 hover:text-text1">
            Sair
          </button>
        </div>
      </header>

      <div className="grid" style={{ gridTemplateColumns: '196px 1fr', minHeight: 'calc(100vh - 52px)' }}>
        <nav className="bg-surface border-r border-border p-3 flex flex-col gap-1">
          <p className="text-[10px] uppercase tracking-wider text-text3 px-3 pt-2 pb-1">
            Período 2026.1
          </p>
          <NavLink to="/classificacao" className={linkClasse}>Classificação</NavLink>
          <NavLink to="/estudantes" className={linkClasse}>Estudantes</NavLink>

          {ehAdmin && (
            <>
              <p className="text-[10px] uppercase tracking-wider text-text3 px-3 pt-3 pb-1">
                Cadastros
              </p>
              <NavLink to="/vagas" className={linkClasse}>Vagas</NavLink>
              <NavLink to="/gestao" className={linkClasse}>Turmas e deptos.</NavLink>
              <NavLink to="/operadores" className={linkClasse}>Operadores</NavLink>
            </>
          )}
        </nav>

        <main className="p-6 w-full max-w-[1600px]">{children}</main>
      </div>
    </div>
  )
}
