import { NavLink } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { useTheme } from '../contexts/ThemeContext'
import logoCompleta from '../assets/MONI (7).png'

export default function Layout({ children }) {
  const { usuario, logout } = useAuth()
  const { tema, alternarTema } = useTheme()
  const ehAdmin = usuario?.perfil === 'ADMIN'

  const linkClasse = ({ isActive }) =>
    `block px-3 py-2 rounded-lg text-sm transition-colors ${
      isActive
        ? 'bg-gold-dim text-gold-light font-medium'
        : 'text-text2 hover:bg-surface2 hover:text-text1'
    }`

  return (
    <div className="min-h-screen bg-bg">
      <header className="bg-surface border-b border-border flex items-center justify-between px-5 sticky top-0 z-20" style={{ height: 72 }}>
        <img src={logoCompleta} alt="SisuMoni" className="h-14 w-auto" />
        <div className="flex items-center gap-3 text-sm text-text2">
          {ehAdmin && (
            <span className="badge badge-gold">
              Administrador
            </span>
          )}
          <span>{usuario?.nome}</span>
          <button onClick={alternarTema} className="btn-chip btn-chip-neutral">
            {tema === 'dark' ? 'Tema claro' : 'Tema escuro'}
          </button>
          <button onClick={logout} className="btn-chip btn-chip-neutral">
            Sair
          </button>
        </div>
      </header>

      <div className="grid" style={{ gridTemplateColumns: '196px 1fr', minHeight: 'calc(100vh - 72px)' }}>
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
