import { useState, useMemo } from 'react'
import {
  useEstudantes, useCriarEstudante, useAtualizarEstudante, useDeletarEstudante,
  useTurmas, useVagas, useClassificacoesPorVagas,
} from '../hooks/useApi'
import { useAuth } from '../contexts/AuthContext'
import { fmtNota, tipoBadge } from '../lib/formato'

export default function Estudantes() {
  const { usuario } = useAuth()
  const ehAdmin = usuario?.perfil === 'ADMIN'

  const { data: estudantes, isLoading } = useEstudantes()
  const { data: turmas } = useTurmas()
  const { data: vagas } = useVagas()
  const criar = useCriarEstudante()
  const atualizar = useAtualizarEstudante()
  const deletar = useDeletarEstudante()

  const [modalEstudante, setModalEstudante] = useState(null)
  const [turmaId, setTurmaId] = useState('')
  const [expandidos, setExpandidos] = useState(new Set())

  function alternarExpandido(id) {
    setExpandidos((atual) => {
      const novo = new Set(atual)
      novo.has(id) ? novo.delete(id) : novo.add(id)
      return novo
    })
  }

  const estudantesFiltrados = useMemo(
    () => (estudantes?.filter(e => !turmaId || e.turma.id === turmaId) ?? [])
      .sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR')),
    [estudantes, turmaId]
  )

  const turmasOrdenadas = useMemo(
    () => [...(turmas ?? [])].sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR')),
    [turmas]
  )

  // Ids únicos das vagas escolhidas (1ª ou 2ª opção) pelos estudantes visíveis
  const vagaIds = useMemo(() => {
    const ids = new Set()
    estudantesFiltrados.forEach(e => {
      if (e.opcao1) ids.add(e.opcao1.id)
      if (e.opcao2) ids.add(e.opcao2.id)
    })
    return [...ids]
  }, [estudantesFiltrados])

  const resultadosClassificacao = useClassificacoesPorVagas(vagaIds)

  // Mapa `${vagaId}_${estudanteId}` -> { tipo, posicao, pontuacao, empate }
  const mapaClassificacao = useMemo(() => {
    const mapa = new Map()
    resultadosClassificacao.forEach((r, i) => {
      const vagaId = vagaIds[i]
      r.data?.forEach(c => mapa.set(`${vagaId}_${c.estudanteId}`, c))
    })
    return mapa
  }, [resultadosClassificacao, vagaIds])

  return (
    <div>
      <div className="flex items-start justify-between mb-4 gap-3">
        <div>
          <h1 className="text-xl font-display font-bold text-text1">Estudantes</h1>
          <p className="text-text2 text-sm mt-0.5">
            {estudantesFiltrados.length} {turmaId ? 'nesta turma' : 'cadastrados'}
          </p>
        </div>
        <div className="flex items-center gap-2">
          <select
            value={turmaId}
            onChange={(e) => setTurmaId(e.target.value)}
            className="field w-auto py-1.5"
          >
            <option value="">Todas as turmas</option>
            {turmasOrdenadas.map(t => <option key={t.id} value={t.id}>{t.nome}</option>)}
          </select>
          <button
            onClick={() => setModalEstudante({})}
            className="bg-gold text-bg rounded-lg px-4 py-2 text-sm font-semibold hover:bg-gold-light transition-colors"
          >
            Novo estudante
          </button>
        </div>
      </div>

      {isLoading ? (
        <div className="card p-4">
          <p className="text-text2 text-sm">Carregando...</p>
        </div>
      ) : !estudantesFiltrados.length ? (
        <div className="card p-4">
          <p className="text-text2 text-sm">
            {turmaId
              ? 'Nenhum estudante cadastrado nesta turma.'
              : 'Nenhum estudante cadastrado. Clique em "Novo estudante" para começar.'}
          </p>
        </div>
      ) : (
        <div className="flex flex-col gap-2">
          {estudantesFiltrados.map((e) => {
            const c1 = e.opcao1 ? mapaClassificacao.get(`${e.opcao1.id}_${e.id}`) : null
            const c2 = e.opcao2 ? mapaClassificacao.get(`${e.opcao2.id}_${e.id}`) : null
            const badge1 = tipoBadge(c1?.tipo, c1?.empate)
            const badge2 = tipoBadge(c2?.tipo, c2?.empate)
            const aberto = expandidos.has(e.id)

            // Vaga em que o estudante está classificado (bolsista, voluntário ou lista de espera), se houver
            const statusAtivo =
              c1?.tipo ? { vaga: e.opcao1, badge: badge1 } :
              c2?.tipo ? { vaga: e.opcao2, badge: badge2 } :
              null

            return (
              <div key={e.id} className="card overflow-hidden">
                <div
                  onClick={() => alternarExpandido(e.id)}
                  className="p-4 flex items-center justify-between gap-3 cursor-pointer hover:bg-surface2"
                >
                  <div className="flex items-center gap-3 min-w-0">
                    <span className={`text-text3 text-xs shrink-0 transition-transform ${aberto ? 'rotate-90' : ''}`}>▸</span>
                    <div className="min-w-0">
                      <div className="text-sm font-medium text-text1 truncate">{e.nome}</div>
                      <div className="text-sm font-medium text-gold-light truncate">{e.nomeFantasia}</div>
                    </div>
                  </div>
                  <div className="flex items-center gap-2 shrink-0">
                    {statusAtivo && (
                      <>
                        <span className="text-sm text-text2 hidden sm:inline">
                          {statusAtivo.vaga.disciplina} <span className="text-text3">— Prof. {statusAtivo.vaga.professor}</span>
                        </span>
                        <span className={`badge ${statusAtivo.badge.classe}`}>{statusAtivo.badge.texto}</span>
                      </>
                    )}
                    <button
                      onClick={(ev) => { ev.stopPropagation(); setModalEstudante(e) }}
                      className="btn-chip btn-chip-accent"
                    >
                      Editar
                    </button>
                    <button
                      onClick={(ev) => {
                        ev.stopPropagation()
                        if (confirm(`Remover ${e.nome}?`)) deletar.mutate(e.id)
                      }}
                      className="btn-chip btn-chip-danger"
                    >
                      Remover
                    </button>
                  </div>
                </div>

                {aberto && (
                  <div className="px-4 pb-4">
                    <div className="text-xs text-text2 tabular-nums font-mono mb-3">IRA {fmtNota(e.ira)}</div>

                    <div className="pt-3 border-t-2 border-border2">
                      <div className="flex items-center justify-between gap-2">
                        <span className="text-[10px] uppercase tracking-wide text-text3 font-mono font-medium">1ª opção</span>
                        {e.opcao1 ? <span className={`badge ${badge1.classe}`}>{badge1.texto}</span> : '—'}
                      </div>
                      <div className="text-sm text-text1 mt-0.5">{e.opcao1?.disciplina ?? '—'}</div>
                      {e.opcao1 && (
                        <>
                          <div className="text-sm font-medium text-gold-light">Prof. {e.opcao1.professor}</div>
                          <div className="text-xs text-text2 tabular-nums font-mono mt-0.5">
                            Média {fmtNota(e.mediaOpcao1)} · Pontuação {fmtNota(c1?.pontuacao)}
                          </div>
                        </>
                      )}
                    </div>

                    {e.opcao2 && (
                      <div className="mt-3 pt-3 border-t-2 border-border2">
                        <div className="flex items-center justify-between gap-2">
                          <span className="text-[10px] uppercase tracking-wide text-text3 font-mono font-medium">2ª opção</span>
                          <span className={`badge ${badge2.classe}`}>{badge2.texto}</span>
                        </div>
                        <div className="text-sm text-text1 mt-0.5">{e.opcao2.disciplina}</div>
                        <div className="text-sm font-medium text-gold-light">Prof. {e.opcao2.professor}</div>
                        <div className="text-xs text-text2 tabular-nums font-mono mt-0.5">
                          Média {fmtNota(e.mediaOpcao2)} · Pontuação {fmtNota(c2?.pontuacao)}
                        </div>
                      </div>
                    )}
                  </div>
                )}
              </div>
            )
          })}
        </div>
      )}

      {modalEstudante !== null && (
        <ModalCadastro
          turmas={turmasOrdenadas}
          vagas={vagas || []}
          estudante={modalEstudante}
          onFechar={() => setModalEstudante(null)}
          onSalvar={(dados) => {
            const opts = {
              onSuccess: () => setModalEstudante(null),
              onError: (err) => {
                alert(err.response?.data?.mensagem || 'Erro ao salvar estudante')
              },
            }
            if (modalEstudante?.id) {
              atualizar.mutate({ id: modalEstudante.id, dados }, opts)
            } else {
              criar.mutate(dados, opts)
            }
          }}
          salvando={modalEstudante?.id ? atualizar.isPending : criar.isPending}
        />
      )}
    </div>
  )
}

// ─── Modal de cadastro com cálculo ao vivo ───────────────
function ModalCadastro({ turmas, vagas, estudante, onFechar, onSalvar, salvando }) {
  const editando = !!estudante?.id
  const [form, setForm] = useState({
    nome: estudante?.nome ?? '',
    nomeFantasia: estudante?.nomeFantasia ?? '',
    turmaId: estudante?.turma?.id ?? '',
    ira: estudante?.ira != null ? fmtNota(estudante.ira) : '',
    opcao1Id: estudante?.opcao1?.id ?? '',
    mediaOpcao1: estudante?.mediaOpcao1 != null ? fmtNota(estudante.mediaOpcao1) : '',
    opcao2Id: estudante?.opcao2?.id ?? '',
    mediaOpcao2: estudante?.mediaOpcao2 != null ? fmtNota(estudante.mediaOpcao2) : '',
  })

  const set = (campo, valor) => {
  setForm((f) => {
    const novo = { ...f, [campo]: valor }
    // Se trocou a turma, limpa as opções de vaga (podem não ser mais elegíveis)
    if (campo === 'turmaId') {
      novo.opcao1Id = ''
      novo.opcao2Id = ''
    }
    return novo
})
    }
  // Converte "9,232" (BR) para número 9.232
  const paraNumero = (texto) => {
    if (!texto) return NaN
    return parseFloat(String(texto).replace(',', '.'))
  }

  // Cálculo ao vivo da pontuação da 1ª opção
  const ira = paraNumero(form.ira)
  const media1 = paraNumero(form.mediaOpcao1)
  const pontuacao1 = (!isNaN(ira) && !isNaN(media1)) ? (ira + media1) : null

  // Cálculo ao vivo da pontuação da 2ª opção
  const media2 = paraNumero(form.mediaOpcao2)
  const pontuacao2 = (!isNaN(ira) && !isNaN(media2)) ? (ira + media2) : null

  // Vagas elegíveis para a turma selecionada
  const vagasElegiveis = form.turmaId
  ? vagas
      .filter((v) => v.turmas?.some((t) => t.id === form.turmaId))
      .sort((a, b) => a.disciplina.localeCompare(b.disciplina, 'pt-BR'))
  : []

  const fmt = (n) => n.toFixed(4).replace('.', ',')

  function submeter() {
  // Campos de texto obrigatórios
  if (!form.nome.trim() || !form.nomeFantasia.trim() || !form.turmaId) {
    alert('Preencha nome, nome fantasia e turma.')
    return
  }

  // 1ª opção obrigatória
  if (!form.opcao1Id) {
    alert('Selecione a vaga da 1ª opção.')
    return
  }

  // Validação numérica do IRA
  const iraNum = paraNumero(form.ira)
  if (isNaN(iraNum) || iraNum < 0 || iraNum > 10) {
    alert('O IRA deve ser um número entre 0 e 10.')
    return
  }

  // Validação numérica da média da 1ª opção
  const media1Num = paraNumero(form.mediaOpcao1)
  if (isNaN(media1Num) || media1Num < 0 || media1Num > 10) {
    alert('A média da 1ª opção deve ser um número entre 0 e 10.')
    return
  }

  // Validação numérica da média da 2ª opção (só se foi selecionada)
  let media2Num = null
  if (form.opcao2Id) {
    media2Num = paraNumero(form.mediaOpcao2)
    if (isNaN(media2Num) || media2Num < 0 || media2Num > 10) {
      alert('A média da 2ª opção deve ser um número entre 0 e 10.')
      return
    }
  }

  const dados = {
    nome: form.nome.trim(),
    nomeFantasia: form.nomeFantasia.trim(),
    turmaId: form.turmaId,
    ira: iraNum,
    opcao1Id: form.opcao1Id,
    mediaOpcao1: media1Num,
    opcao2Id: form.opcao2Id || null,
    mediaOpcao2: form.opcao2Id ? media2Num : null,
  }
  onSalvar(dados)
}

  return (
    <div className="fixed inset-0 bg-black/60 backdrop-blur-sm grid place-items-center p-6 z-50" onClick={onFechar}>
      <div
        className="card w-full max-w-lg max-h-[90vh] overflow-y-auto"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="px-5 py-4 border-b-2 border-border">
          <h2 className="text-sm font-semibold text-text1">{editando ? 'Editar estudante' : 'Cadastrar estudante'}</h2>
        </div>

        <div className="p-5">
          <p className="text-[10px] uppercase tracking-wide text-text3 font-mono font-medium mb-2">Identificação</p>
          <div className="grid grid-cols-2 gap-3 mb-3">
            <div>
              <label className="field-label">Nome completo</label>
              <input className="field" value={form.nome} onChange={(e) => set('nome', e.target.value)} />
            </div>
            <div>
              <label className="field-label">Nome fantasia</label>
              <input className="field" value={form.nomeFantasia} onChange={(e) => set('nomeFantasia', e.target.value)} />
            </div>
          </div>
          <div className="grid grid-cols-2 gap-3 mb-3">
            <div>
              <label className="field-label">Turma</label>
              <select className="field" value={form.turmaId} onChange={(e) => set('turmaId', e.target.value)}>
                <option value="">Selecione</option>
                {turmas.map((t) => <option key={t.id} value={t.id}>{t.nome}</option>)}
              </select>
            </div>
          </div>

          <p className="text-[10px] uppercase tracking-wide text-text3 font-mono font-medium mb-2 mt-4">Notas</p>
          <div className="grid grid-cols-2 gap-3 mb-3">
            <div>
              <label className="field-label">IRA (0 a 10)</label>
              <input type="text" inputMode="decimal" placeholder="8,8951" className="field" value={form.ira} onChange={(e) => set('ira', e.target.value)} />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3 mb-3">
            <div>
              <label className="field-label">1ª opção — vaga</label>
              <select
                className="field"
                value={form.opcao1Id}
                onChange={(e) => set('opcao1Id', e.target.value)}
                disabled={!form.turmaId}
              >
                <option value="">
                  {!form.turmaId ? 'Escolha a turma primeiro' : 'Selecione'}
                </option>
                {vagasElegiveis.map((v) => (
                  <option key={v.id} value={v.id}>{v.disciplina} — Prof. {v.professor}</option>
                ))}
              </select>
            </div>
            <div>
              <label className="field-label">Média na 1ª opção</label>
              <input type="text" inputMode="decimal" placeholder="9,5" className="field" value={form.mediaOpcao1} onChange={(e) => set('mediaOpcao1', e.target.value)} />
            </div>
          </div>

          {/* Cálculo ao vivo */}
          <div className="bg-surface2 border-2 border-border2 rounded-lg px-4 py-3 flex items-center justify-between mt-4">
            <div className="text-xs text-text2">
              {pontuacao1 != null
                ? <>Pontuação = <span className="tabular-nums font-mono">{fmt(ira)}</span> + <span className="tabular-nums font-mono">{fmt(media1)}</span></>
                : 'Preencha IRA e média para ver a pontuação'}
            </div>
            <div className={`text-2xl font-display font-bold tabular-nums ${pontuacao1 != null ? 'text-gold-light' : 'text-text3'}`}>
              {pontuacao1 != null ? fmt(pontuacao1) : '—'}
            </div>
          </div>

          <p className="text-[10px] uppercase tracking-wide text-text3 font-mono font-medium mb-2 mt-4">2ª opção (opcional)</p>
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="field-label">2ª opção - vaga</label>
              <select
                className="field"
                value={form.opcao2Id}
                onChange={(e) => set('opcao2Id', e.target.value)}
                disabled={!form.turmaId}
              >
                <option value="">
                  {!form.turmaId ? 'Escolha a turma primeiro' : 'Selecione'}
                </option>
                {vagasElegiveis
                  .filter((v) => v.id !== form.opcao1Id)
                  .map((v) => (
                    <option key={v.id} value={v.id}>{v.disciplina} — Prof. {v.professor}</option>
                  ))}
              </select>
            </div>
            <div>
              <label className="field-label">Média na 2ª opção</label>
              <input type="text" inputMode="decimal" placeholder="8,0" className="field" value={form.mediaOpcao2} onChange={(e) => set('mediaOpcao2', e.target.value)} />
            </div>
          </div>

          {form.opcao2Id && (
            <div className="bg-surface2 border-2 border-border2 rounded-lg px-4 py-3 flex items-center justify-between mt-4">
              <div className="text-xs text-text2">
                {pontuacao2 != null
                  ? <>Pontuação = <span className="tabular-nums font-mono">{fmt(ira)}</span> + <span className="tabular-nums font-mono">{fmt(media2)}</span></>
                  : 'Preencha a média para ver a pontuação'}
              </div>
              <div className={`text-2xl font-display font-bold tabular-nums ${pontuacao2 != null ? 'text-gold-light' : 'text-text3'}`}>
                {pontuacao2 != null ? fmt(pontuacao2) : '—'}
              </div>
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
            {salvando ? 'Salvando...' : editando ? 'Salvar alterações' : 'Salvar estudante'}
          </button>
        </div>
      </div>
    </div>
  )
}
