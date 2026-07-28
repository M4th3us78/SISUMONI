import { useState } from 'react'
import { useOperadores, useCriarOperador, useDeletarOperador, useTurmas } from '../hooks/useApi'

export default function Operadores() {
  const { data: operadores, isLoading } = useOperadores()
  const { data: turmas } = useTurmas()
  const criar = useCriarOperador()
  const deletar = useDeletarOperador()

  const [modalAberto, setModalAberto] = useState(false)

  return (
    <div>
      <div className="flex items-start justify-between mb-4">
        <div>
          <h1 className="text-xl font-display font-bold text-text1">Operadores</h1>
          <p className="text-text2 text-sm mt-0.5">
            Quem cadastra estudantes e resolve empates em cada turma
          </p>
        </div>
        <button
          onClick={() => setModalAberto(true)}
          className="bg-gold text-bg rounded-lg px-4 py-2 text-sm font-semibold hover:bg-gold-light transition-colors"
        >
          Novo operador
        </button>
      </div>

      <div className="card overflow-hidden">
        {isLoading ? (
          <p className="text-text2 text-sm p-4">Carregando...</p>
        ) : !operadores?.length ? (
          <p className="text-text2 text-sm p-4">
            Nenhum operador cadastrado. Clique em "Novo operador" para começar.
          </p>
        ) : (
          <table className="w-full">
            <thead>
              <tr className="border-b border-border">
                <th className="text-left text-[10px] uppercase tracking-wide text-text3 font-mono font-medium px-4 py-2">Nome</th>
                <th className="text-left text-[10px] uppercase tracking-wide text-text3 font-mono font-medium px-4 py-2 w-52">E-mail</th>
                <th className="text-left text-[10px] uppercase tracking-wide text-text3 font-mono font-medium px-4 py-2 w-48">Turmas</th>
                <th className="text-left text-[10px] uppercase tracking-wide text-text3 font-mono font-medium px-4 py-2 w-16"></th>
              </tr>
            </thead>
            <tbody>
              {operadores.map((o) => (
                <tr key={o.id} className="border-b border-border2 last:border-0 hover:bg-surface2">
                  <td className="px-4 py-2.5 text-sm font-medium text-text1">{o.nome}</td>
                  <td className="px-4 py-2.5 text-sm text-text2">{o.email}</td>
                  <td className="px-4 py-2.5">
                    <div className="flex gap-1 flex-wrap">
                      {o.turmas?.length
                        ? o.turmas.map((t, i) => (
                            <span key={i} className="badge badge-neutral">{t}</span>
                          ))
                        : <span className="text-xs text-text3">—</span>}
                    </div>
                  </td>
                  <td className="px-4 py-2.5 text-right">
                    <button
                      onClick={() => {
                        if (confirm(`Remover o operador ${o.nome}? Ele perderá o acesso ao sistema.`)) {
                          deletar.mutate(o.id, {
                            onError: (err) => alert(err.response?.data?.mensagem || 'Erro ao remover'),
                          })
                        }
                      }}
                      className="text-xs text-danger hover:text-danger/80"
                    >
                      Remover
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {modalAberto && (
        <ModalOperador
          turmas={turmas || []}
          onFechar={() => setModalAberto(false)}
          onSalvar={(dados) => {
            criar.mutate(dados, {
              onSuccess: () => setModalAberto(false),
              onError: (err) => alert(err.response?.data?.mensagem || 'Erro ao cadastrar operador'),
            })
          }}
          salvando={criar.isPending}
        />
      )}
    </div>
  )
}

// ─── Modal de cadastro de operador ───────────────────────
function ModalOperador({ turmas, onFechar, onSalvar, salvando }) {
  const [form, setForm] = useState({ nome: '', email: '', senha: '' })
  const [turmasSelecionadas, setTurmasSelecionadas] = useState([])

  const set = (campo, valor) => setForm((f) => ({ ...f, [campo]: valor }))

  function alternarTurma(id) {
    setTurmasSelecionadas((atual) =>
      atual.includes(id) ? atual.filter((t) => t !== id) : [...atual, id]
    )
  }

  function submeter() {
    if (!form.nome.trim() || !form.email.trim() || !form.senha) {
      alert('Preencha nome, e-mail e senha.')
      return
    }
    if (form.senha.length < 6) {
      alert('A senha deve ter no mínimo 6 caracteres.')
      return
    }
    if (turmasSelecionadas.length === 0) {
      alert('Selecione ao menos uma turma.')
      return
    }
    onSalvar({
      nome: form.nome.trim(),
      email: form.email.trim(),
      senha: form.senha,
      turmasIds: turmasSelecionadas,
    })
  }

  return (
    <div className="fixed inset-0 bg-black/60 backdrop-blur-sm grid place-items-center p-6 z-50" onClick={onFechar}>
      <div className="card w-full max-w-md max-h-[90vh] overflow-y-auto" onClick={(e) => e.stopPropagation()}>
        <div className="px-5 py-4 border-b border-border">
          <h2 className="text-sm font-semibold text-text1">Cadastrar operador</h2>
        </div>

        <div className="p-5">
          <div className="mb-3">
            <label className="field-label">Nome</label>
            <input className="field" value={form.nome} onChange={(e) => set('nome', e.target.value)} />
          </div>
          <div className="mb-3">
            <label className="field-label">E-mail</label>
            <input type="email" className="field" value={form.email} onChange={(e) => set('email', e.target.value)} />
          </div>
          <div className="mb-1">
            <label className="field-label">Senha provisória</label>
            <input type="text" className="field" value={form.senha} onChange={(e) => set('senha', e.target.value)} placeholder="Mínimo 6 caracteres" />
            <p className="text-[11px] text-text3 mt-1">O operador poderá trocar depois. Deixe visível para poder comunicá-la.</p>
          </div>

          <p className="text-[10px] uppercase tracking-wide text-text3 font-mono font-medium mb-2 mt-4">Turmas sob responsabilidade</p>
          {turmas.length === 0 ? (
            <p className="text-xs text-text3">Cadastre turmas primeiro.</p>
          ) : (
            <div className="flex gap-2 flex-wrap">
              {turmas.map((t) => {
                const ativa = turmasSelecionadas.includes(t.id)
                return (
                  <button
                    key={t.id}
                    onClick={() => alternarTurma(t.id)}
                    className={`px-3 py-1.5 rounded-full text-xs border transition-colors ${
                      ativa
                        ? 'border-gold bg-gold text-bg font-medium'
                        : 'border-border2 text-text2 hover:border-border'
                    }`}
                  >
                    {t.nome}
                  </button>
                )
              })}
            </div>
          )}
        </div>

        <div className="px-5 py-4 border-t border-border flex justify-end gap-2">
          <button onClick={onFechar} className="border border-border2 text-text2 rounded-lg px-4 py-2 text-sm hover:bg-surface2">
            Cancelar
          </button>
          <button
            onClick={submeter}
            disabled={salvando}
            className="bg-gold text-bg rounded-lg px-4 py-2 text-sm font-semibold hover:bg-gold-light disabled:opacity-50 transition-colors"
          >
            {salvando ? 'Salvando...' : 'Salvar operador'}
          </button>
        </div>
      </div>
    </div>
  )
}
