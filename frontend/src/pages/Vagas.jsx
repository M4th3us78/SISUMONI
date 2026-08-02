import { useState, useMemo } from 'react'
import {
  useVagas, useCriarVaga, useAtualizarVaga, useDeletarVaga,
  useDepartamentos, useTurmas,
} from '../hooks/useApi'

export default function Vagas() {
  const { data: vagas, isLoading } = useVagas()
  const { data: departamentos } = useDepartamentos()
  const { data: turmas } = useTurmas()
  const criar = useCriarVaga()
  const atualizar = useAtualizarVaga()
  const deletar = useDeletarVaga()

  const [modalVaga, setModalVaga] = useState(null)
  const [departamentoId, setDepartamentoId] = useState('')

  const vagasFiltradas = useMemo(
    () => (vagas?.filter(v => !departamentoId || v.departamento.id === departamentoId) ?? [])
      .sort((a, b) => a.disciplina.localeCompare(b.disciplina, 'pt-BR')),
    [vagas, departamentoId]
  )

  return (
    <div>
      <div className="flex items-start justify-between mb-4 gap-3">
        <div>
          <h1 className="text-xl font-display font-bold text-text1">Vagas de monitoria</h1>
          <p className="text-text2 text-sm mt-0.5">
            {vagasFiltradas.length} {departamentoId ? 'neste departamento' : 'vagas cadastradas'}
          </p>
        </div>
        <div className="flex items-center gap-2">
          <select
            value={departamentoId}
            onChange={(e) => setDepartamentoId(e.target.value)}
            className="field w-auto py-1.5"
          >
            <option value="">Todos os departamentos</option>
            {departamentos?.map(d => <option key={d.id} value={d.id}>{d.nome}</option>)}
          </select>
          <button
            onClick={() => setModalVaga({})}
            className="bg-gold text-bg rounded-lg px-4 py-2 text-sm font-semibold hover:bg-gold-light transition-colors"
          >
            Nova vaga
          </button>
        </div>
      </div>

      <div className="card overflow-hidden">
        {isLoading ? (
          <p className="text-text2 text-sm p-4">Carregando...</p>
        ) : !vagasFiltradas.length ? (
          <p className="text-text2 text-sm p-4">
            {departamentoId ? 'Nenhuma vaga neste departamento.' : 'Nenhuma vaga cadastrada. Clique em "Nova vaga" para começar.'}
          </p>
        ) : (
          <table className="w-full">
            <thead>
              <tr className="border-b-2 border-border">
                <th className="text-left text-[10px] uppercase tracking-wide text-text3 font-mono font-medium px-4 py-2">Disciplina</th>
                <th className="text-left text-[10px] uppercase tracking-wide text-text3 font-mono font-medium px-4 py-2 w-48">Professor</th>
                <th className="text-left text-[10px] uppercase tracking-wide text-text3 font-mono font-medium px-4 py-2 w-44">Departamento</th>
                <th className="text-left text-[10px] uppercase tracking-wide text-text3 font-mono font-medium px-4 py-2 w-32">Vagas</th>
                <th className="text-left text-[10px] uppercase tracking-wide text-text3 font-mono font-medium px-4 py-2 w-36"></th>
              </tr>
            </thead>
            <tbody>
              {vagasFiltradas.map((v) => (
                <tr key={v.id} className="border-b-2 border-border2 last:border-0 hover:bg-surface2">
                  <td className="px-4 py-2.5">
                    <div className="text-sm font-medium text-text1">{v.disciplina}</div>
                  </td>
                  <td className="px-4 py-2.5 text-sm font-medium text-gold-light">
                    {v.professor}
                  </td>
                  <td className="px-4 py-2.5 text-sm text-text2">
                    {v.departamento?.nome}
                  </td>
                  <td className="px-4 py-2.5 text-xs text-text2 tabular-nums font-mono">
                    {v.qtdBolsistas}B · {v.qtdVoluntarios}V · {v.qtdListaEspera}E
                  </td>
                  <td className="px-4 py-2.5 text-right">
                    <div className="flex items-center justify-end gap-2">
                      <button
                        onClick={() => setModalVaga(v)}
                        className="btn-chip btn-chip-accent"
                      >
                        Editar
                      </button>
                      <button
                        onClick={() => {
                          if (confirm(`Remover a vaga "${v.disciplina}"?`)) {
                            deletar.mutate(v.id, {
                              onError: (err) => alert(err.response?.data?.mensagem || 'Erro ao remover'),
                            })
                          }
                        }}
                        className="btn-chip btn-chip-danger"
                      >
                        Remover
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {modalVaga !== null && (
        <ModalVaga
          departamentos={departamentos || []}
          turmas={turmas || []}
          vaga={modalVaga}
          onFechar={() => setModalVaga(null)}
          onSalvar={(dados) => {
            const opts = {
              onSuccess: () => setModalVaga(null),
              onError: (err) => alert(err.response?.data?.mensagem || 'Erro ao salvar vaga'),
            }
            if (modalVaga?.id) {
              atualizar.mutate({ id: modalVaga.id, dados }, opts)
            } else {
              criar.mutate(dados, opts)
            }
          }}
          salvando={modalVaga?.id ? atualizar.isPending : criar.isPending}
        />
      )}
    </div>
  )
}

// ─── Modal de cadastro/edição de vaga ────────────────────
function ModalVaga({ departamentos, turmas, vaga, onFechar, onSalvar, salvando }) {
  const editando = !!vaga?.id
  const [form, setForm] = useState({
    disciplina: vaga?.disciplina ?? '',
    professor: vaga?.professor ?? '',
    departamentoId: vaga?.departamento?.id ?? '',
    qtdBolsistas: vaga?.qtdBolsistas != null ? String(vaga.qtdBolsistas) : '1',
    qtdVoluntarios: vaga?.qtdVoluntarios != null ? String(vaga.qtdVoluntarios) : '2',
  })
  const [turmasSelecionadas, setTurmasSelecionadas] = useState(
    vaga?.turmas?.map((t) => t.id) ?? []
  )

  const set = (campo, valor) => setForm((f) => ({ ...f, [campo]: valor }))

  function alternarTurma(id) {
    setTurmasSelecionadas((atual) =>
      atual.includes(id) ? atual.filter((t) => t !== id) : [...atual, id]
    )
  }

  function submeter() {
    if (!form.disciplina.trim() || !form.professor.trim() || !form.departamentoId) {
      alert('Preencha disciplina, professor e departamento.')
      return
    }
    if (turmasSelecionadas.length === 0) {
      alert('Selecione ao menos uma turma.')
      return
    }

    const bols = parseInt(form.qtdBolsistas) || 0
    const vol = parseInt(form.qtdVoluntarios) || 0

    onSalvar({
      disciplina: form.disciplina.trim(),
      professor: form.professor.trim(),
      departamentoId: form.departamentoId,
      qtdBolsistas: bols,
      qtdVoluntarios: vol,
      qtdListaEspera: 1, // fixo — sempre 1 vaga de lista de espera
      turmasIds: turmasSelecionadas,
    })
  }

  return (
    <div className="fixed inset-0 bg-black/60 backdrop-blur-sm grid place-items-center p-6 z-50" onClick={onFechar}>
      <div
        className="card w-full max-w-lg max-h-[90vh] overflow-y-auto"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="px-5 py-4 border-b-2 border-border">
          <h2 className="text-sm font-semibold text-text1">{editando ? 'Editar vaga' : 'Cadastrar vaga'}</h2>
        </div>

        <div className="p-5">
          <div className="grid grid-cols-2 gap-3 mb-3">
            <div>
              <label className="field-label">Disciplina</label>
              <input className="field" value={form.disciplina} onChange={(e) => set('disciplina', e.target.value)} />
            </div>
            <div>
              <label className="field-label">Professor responsável</label>
              <input className="field" value={form.professor} onChange={(e) => set('professor', e.target.value)} />
            </div>
          </div>

          <div className="mb-3">
            <label className="field-label">Departamento</label>
            <select className="field" value={form.departamentoId} onChange={(e) => set('departamentoId', e.target.value)}>
              <option value="">Selecione</option>
              {departamentos.map((d) => <option key={d.id} value={d.id}>{d.nome}</option>)}
            </select>
          </div>

          <p className="text-[10px] uppercase tracking-wide text-text3 font-mono font-medium mb-2 mt-4">Quantidade de posições</p>
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="field-label">Bolsistas</label>
              <input type="text" inputMode="numeric" className="field" value={form.qtdBolsistas} onChange={(e) => set('qtdBolsistas', e.target.value.replace(/\D/g, ''))} />
            </div>
            <div>
              <label className="field-label">Voluntários</label>
              <input type="text" inputMode="numeric" className="field" value={form.qtdVoluntarios} onChange={(e) => set('qtdVoluntarios', e.target.value.replace(/\D/g, ''))} />
            </div>
          </div>
          <p className="text-[11px] text-text3 mt-1">A lista de espera é fixa em 1 vaga.</p>

          <div className="flex items-center justify-between mb-2 mt-4">
            <p className="text-[10px] uppercase tracking-wide text-text3 font-mono font-medium">Turmas que podem se candidatar</p>
            {turmas.length > 0 && (
              <button
                type="button"
                onClick={() => setTurmasSelecionadas(
                  turmasSelecionadas.length === turmas.length ? [] : turmas.map((t) => t.id)
                )}
                className="text-xs text-gold hover:text-gold-light font-medium"
              >
                {turmasSelecionadas.length === turmas.length ? 'Desmarcar todas' : 'Selecionar todas'}
              </button>
            )}
          </div>
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
                    className={`px-3 py-1.5 rounded-full text-xs border-2 transition-colors ${
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

        <div className="px-5 py-4 border-t-2 border-border flex justify-end gap-2">
          <button onClick={onFechar} className="border-2 border-border2 text-text2 rounded-lg px-4 py-2 text-sm hover:bg-surface2">
            Cancelar
          </button>
          <button
            onClick={submeter}
            disabled={salvando}
            className="bg-gold text-bg rounded-lg px-4 py-2 text-sm font-semibold hover:bg-gold-light disabled:opacity-50 transition-colors"
          >
            {salvando ? 'Salvando...' : editando ? 'Salvar alterações' : 'Salvar vaga'}
          </button>
        </div>
      </div>
    </div>
  )
}
