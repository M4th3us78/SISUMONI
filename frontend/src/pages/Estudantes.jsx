import { useState, useMemo } from 'react'
import {
  useEstudantes, useCriarEstudante, useDeletarEstudante,
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
  const deletar = useDeletarEstudante()

  const [modalAberto, setModalAberto] = useState(false)
  const [turmaId, setTurmaId] = useState('')

  const estudantesFiltrados = useMemo(
    () => estudantes?.filter(e => !turmaId || e.turma.id === turmaId) ?? [],
    [estudantes, turmaId]
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
            {turmas?.map(t => <option key={t.id} value={t.id}>{t.nome}</option>)}
          </select>
          <button
            onClick={() => setModalAberto(true)}
            className="bg-gold text-bg rounded-lg px-4 py-2 text-sm font-semibold hover:bg-gold-light transition-colors"
          >
            Novo estudante
          </button>
        </div>
      </div>

      <div className="card overflow-hidden">
        {isLoading ? (
          <p className="text-text2 text-sm p-4">Carregando...</p>
        ) : !estudantesFiltrados.length ? (
          <p className="text-text2 text-sm p-4">
            {turmaId
              ? 'Nenhum estudante cadastrado nesta turma.'
              : 'Nenhum estudante cadastrado. Clique em "Novo estudante" para começar.'}
          </p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b border-border">
                  {[
                    'Nome', 'Matrícula', 'IRA',
                    '1ª opção', 'Média', 'Pontuação', 'Situação',
                    '2ª opção', 'Média', 'Pontuação', 'Situação',
                    '',
                  ].map((titulo, i) => (
                    <th key={i} className="text-left text-[10px] uppercase tracking-wide text-text3 font-mono font-medium px-4 py-2 whitespace-nowrap">
                      {titulo}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {estudantesFiltrados.map((e) => {
                  const c1 = e.opcao1 ? mapaClassificacao.get(`${e.opcao1.id}_${e.id}`) : null
                  const c2 = e.opcao2 ? mapaClassificacao.get(`${e.opcao2.id}_${e.id}`) : null
                  const badge1 = tipoBadge(c1?.tipo, c1?.empate)
                  const badge2 = tipoBadge(c2?.tipo, c2?.empate)

                  return (
                    <tr key={e.id} className="border-b border-border2 last:border-0 hover:bg-surface2">
                      <td className="px-4 py-2.5">
                        <div className="text-sm font-medium text-text1">{e.nome}</div>
                        <div className="text-xs text-text3">{e.nomeFantasia}</div>
                      </td>
                      <td className="px-4 py-2.5 text-sm text-text2 tabular-nums font-mono whitespace-nowrap">{e.matricula}</td>
                      <td className="px-4 py-2.5 text-sm tabular-nums text-text1 font-mono whitespace-nowrap">{fmtNota(e.ira)}</td>

                      <td className="px-4 py-2.5 text-sm text-text1 whitespace-nowrap">{e.opcao1?.disciplina ?? '—'}</td>
                      <td className="px-4 py-2.5 text-sm tabular-nums text-text2 font-mono whitespace-nowrap">{fmtNota(e.mediaOpcao1)}</td>
                      <td className="px-4 py-2.5 text-sm tabular-nums text-text1 font-mono whitespace-nowrap">{fmtNota(c1?.pontuacao)}</td>
                      <td className="px-4 py-2.5 whitespace-nowrap">
                        {e.opcao1 ? <span className={`badge ${badge1.classe}`}>{badge1.texto}</span> : '—'}
                      </td>

                      <td className="px-4 py-2.5 text-sm text-text1 whitespace-nowrap">{e.opcao2?.disciplina ?? '—'}</td>
                      <td className="px-4 py-2.5 text-sm tabular-nums text-text2 font-mono whitespace-nowrap">{fmtNota(e.mediaOpcao2)}</td>
                      <td className="px-4 py-2.5 text-sm tabular-nums text-text1 font-mono whitespace-nowrap">{fmtNota(c2?.pontuacao)}</td>
                      <td className="px-4 py-2.5 whitespace-nowrap">
                        {e.opcao2 ? <span className={`badge ${badge2.classe}`}>{badge2.texto}</span> : '—'}
                      </td>

                      <td className="px-4 py-2.5 text-right whitespace-nowrap">
                        <button
                          onClick={() => {
                            if (confirm(`Remover ${e.nome}?`)) deletar.mutate(e.id)
                          }}
                          className="text-xs text-danger hover:text-danger/80"
                        >
                          Remover
                        </button>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {modalAberto && (
        <ModalCadastro
          turmas={turmas || []}
          vagas={vagas || []}
          onFechar={() => setModalAberto(false)}
          onSalvar={(dados) => {
            criar.mutate(dados, {
              onSuccess: () => setModalAberto(false),
              onError: (err) => {
                alert(err.response?.data?.mensagem || 'Erro ao cadastrar estudante')
              },
            })
          }}
          salvando={criar.isPending}
        />
      )}
    </div>
  )
}

// ─── Modal de cadastro com cálculo ao vivo ───────────────
function ModalCadastro({ turmas, vagas, onFechar, onSalvar, salvando }) {
  const [form, setForm] = useState({
    nome: '', matricula: '', nomeFantasia: '', turmaId: '',
    ira: '', opcao1Id: '', mediaOpcao1: '', opcao2Id: '', mediaOpcao2: '',
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

  // Vagas elegíveis para a turma selecionada
  const vagasElegiveis = form.turmaId
  ? vagas.filter((v) => v.turmas?.some((t) => t.id === form.turmaId))
  : []

  const fmt = (n) => n.toFixed(4).replace('.', ',')

  function submeter() {
  // Campos de texto obrigatórios
  if (!form.nome.trim() || !form.matricula.trim() || !form.nomeFantasia.trim() || !form.turmaId) {
    alert('Preencha nome, nome fantasia, matrícula e turma.')
    return
  }

  // 1ª opção obrigatória
  if (!form.opcao1Id) {
    alert('Selecione a vaga da 1ª opção.')
    return
  }

  // 2ª opção obrigatória
  if (!form.opcao2Id) {
    alert('Selecione a vaga da 2ª opção.')
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

  // Validação numérica da média da 2ª opção
  const media2Num = paraNumero(form.mediaOpcao2)
  if (isNaN(media2Num) || media2Num < 0 || media2Num > 10) {
    alert('A média da 2ª opção deve ser um número entre 0 e 10.')
    return
  }

  const dados = {
    nome: form.nome.trim(),
    matricula: form.matricula.trim(),
    nomeFantasia: form.nomeFantasia.trim(),
    turmaId: form.turmaId,
    ira: iraNum,
    opcao1Id: form.opcao1Id,
    mediaOpcao1: media1Num,
    opcao2Id: form.opcao2Id,
    mediaOpcao2: media2Num,
  }
  onSalvar(dados)
}

  return (
    <div className="fixed inset-0 bg-black/60 backdrop-blur-sm grid place-items-center p-6 z-50" onClick={onFechar}>
      <div
        className="card w-full max-w-lg max-h-[90vh] overflow-y-auto"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="px-5 py-4 border-b border-border">
          <h2 className="text-sm font-semibold text-text1">Cadastrar estudante</h2>
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
              <label className="field-label">Matrícula</label>
              <input className="field" value={form.matricula} onChange={(e) => set('matricula', e.target.value)} />
            </div>
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
                  <option key={v.id} value={v.id}>{v.disciplina}</option>
                ))}
              </select>
            </div>
            <div>
              <label className="field-label">Média na 1ª opção</label>
              <input type="text" inputMode="decimal" placeholder="9,5" className="field" value={form.mediaOpcao1} onChange={(e) => set('mediaOpcao1', e.target.value)} />
            </div>
          </div>

          {/* Cálculo ao vivo */}
          <div className="bg-surface2 border border-border2 rounded-lg px-4 py-3 flex items-center justify-between mt-4">
            <div className="text-xs text-text2">
              {pontuacao1 != null
                ? <>Pontuação = <span className="tabular-nums font-mono">{fmt(ira)}</span> + <span className="tabular-nums font-mono">{fmt(media1)}</span></>
                : 'Preencha IRA e média para ver a pontuação'}
            </div>
            <div className={`text-2xl font-display font-bold tabular-nums ${pontuacao1 != null ? 'text-gold-light' : 'text-text3'}`}>
              {pontuacao1 != null ? fmt(pontuacao1) : '—'}
            </div>
          </div>

          <p className="text-[10px] uppercase tracking-wide text-text3 font-mono font-medium mb-2 mt-4">2ª opção</p>
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
                    <option key={v.id} value={v.id}>{v.disciplina}</option>
                  ))}
              </select>
            </div>
            <div>
              <label className="field-label">Média na 2ª opção</label>
              <input type="text" inputMode="decimal" placeholder="8,0" className="field" value={form.mediaOpcao2} onChange={(e) => set('mediaOpcao2', e.target.value)} />
            </div>
          </div>
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
            {salvando ? 'Salvando...' : 'Salvar estudante'}
          </button>
        </div>
      </div>
    </div>
  )
}
