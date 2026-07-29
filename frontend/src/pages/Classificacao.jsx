import { useState, useMemo } from 'react'
import { useVagas, useDepartamentos, useClassificacoesPorVagas, useResolverEmpate } from '../hooks/useApi'
import { useAuth } from '../contexts/AuthContext'
import { fmtNota, tipoBadge } from '../lib/formato'

export default function Classificacao() {
  const { usuario } = useAuth()
  const ehAdmin = usuario?.perfil === 'ADMIN'
  const { data: vagas, isLoading: carregandoVagas } = useVagas()
  const { data: departamentos } = useDepartamentos()
  const [departamentoId, setDepartamentoId] = useState('')

  const vagasFiltradas = useMemo(
    () => vagas?.filter(v => !departamentoId || v.departamento.id === departamentoId) ?? [],
    [vagas, departamentoId]
  )
  const vagaIds = useMemo(() => vagasFiltradas.map(v => v.id), [vagasFiltradas])
  const resultados = useClassificacoesPorVagas(vagaIds)

  if (carregandoVagas) return <p className="text-text2">Carregando vagas...</p>

  if (!vagas?.length) {
    return (
      <div>
        <h1 className="text-xl font-display font-bold text-text1 mb-1">Classificação</h1>
        <p className="text-text2 text-sm">
          Nenhuma vaga cadastrada ainda. Peça ao administrador para cadastrar vagas.
        </p>
      </div>
    )
  }

  return (
    <div>
      <div className="mb-4 flex items-start justify-between gap-3">
        <div>
          <h1 className="text-xl font-display font-bold text-text1">Classificação</h1>
          <p className="text-text2 text-sm mt-0.5">
            A lista é recalculada automaticamente a cada cadastro
          </p>
        </div>
        <select
          value={departamentoId}
          onChange={(e) => setDepartamentoId(e.target.value)}
          className="field w-auto py-1.5"
        >
          <option value="">Todos os departamentos</option>
          {departamentos?.map(d => <option key={d.id} value={d.id}>{d.nome}</option>)}
        </select>
      </div>

      {!vagasFiltradas.length ? (
        <p className="text-text2 text-sm">Nenhuma vaga neste departamento.</p>
      ) : (
        <div className="grid grid-cols-1 2xl:grid-cols-2 gap-4 items-start">
          {vagasFiltradas.map((vaga, i) => (
            <VagaCard
              key={vaga.id}
              vaga={vaga}
              classificacao={resultados[i]?.data}
              isLoading={resultados[i]?.isLoading}
              ehAdmin={ehAdmin}
            />
          ))}
        </div>
      )}
    </div>
  )
}

// ─── Card de uma vaga com sua tabela de classificação ────
function VagaCard({ vaga, classificacao, isLoading, ehAdmin }) {
  const [modalEmpate, setModalEmpate] = useState(false)

  const empatados = classificacao?.filter(c => c.empate) || []
  const temEmpate = empatados.length > 0

  return (
    <div>
      {temEmpate && (
        <div className="bg-warning-dim border border-warning/30 rounded-xl px-4 py-3 mb-2 flex items-center gap-3">
          <span className="w-2 h-2 rounded-full bg-warning shrink-0"></span>
          <p className="text-sm text-warning flex-1">
            Empate na fronteira de <strong>{vaga.disciplina}</strong>. A posição provisória precisa ser decidida.
          </p>
          <button
            onClick={() => setModalEmpate(true)}
            className="bg-surface border border-warning/30 text-warning rounded-lg px-3 py-1.5 text-sm font-medium hover:bg-warning-dim"
          >
            Resolver empate
          </button>
        </div>
      )}

      <div className="card overflow-hidden">
        <div className="px-4 py-3 border-b border-border">
          <p className="text-sm font-semibold text-text1">{vaga.disciplina}</p>
          <p className="text-sm font-medium text-gold-light mt-0.5">Prof. {vaga.professor}</p>
          <p className="text-xs text-text2 mt-0.5">
            {vaga.qtdBolsistas} bolsistas · {vaga.qtdVoluntarios} voluntários · {vaga.qtdListaEspera} em espera
          </p>
        </div>

        {isLoading ? (
          <p className="text-text2 text-sm p-4">Carregando classificação...</p>
        ) : !classificacao?.length ? (
          <p className="text-text2 text-sm p-4">Nenhum estudante inscrito nesta vaga ainda.</p>
        ) : (
          <table className="w-full">
            <thead>
              <tr className="border-b border-border">
                <th className="text-left text-[10px] uppercase tracking-wide text-text3 font-mono font-medium px-4 py-2 w-14">Pos.</th>
                <th className="text-left text-[10px] uppercase tracking-wide text-text3 font-mono font-medium px-4 py-2">Estudante</th>
                {ehAdmin && <th className="text-left text-[10px] uppercase tracking-wide text-text3 font-mono font-medium px-4 py-2">Nome fantasia</th>}
                <th className="text-left text-[10px] uppercase tracking-wide text-text3 font-mono font-medium px-4 py-2 w-36">Pontuação</th>
                <th className="text-left text-[10px] uppercase tracking-wide text-text3 font-mono font-medium px-4 py-2 w-32">Resultado</th>
              </tr>
            </thead>
            <tbody>
              {classificacao.map((c) => {
                const badge = tipoBadge(c.tipo, c.empate)
                return (
                  <tr key={c.estudanteId} className={`border-b border-border2 last:border-0 hover:bg-surface2 ${c.empate ? 'bg-warning-dim/40' : ''}`}>
                    <td className="px-4 py-2.5">
                      <span className={`rank-pos ${
                        c.tipo === 'BOLSISTA' ? 'rank-1' :
                        (c.tipo === 'LISTA_ESPERA' || !c.tipo) ? 'rank-n' : 'rank-2'
                      }`}>
                        {c.tipo === 'LISTA_ESPERA' || !c.tipo ? '–' : c.posicao}
                      </span>
                    </td>
                    <td className="px-4 py-2.5">
                      <div className="text-sm font-medium text-text1">{c.nome || c.nomeFantasia}</div>
                      {c.nome && <div className="text-xs text-text3">{c.nomeFantasia}</div>}
                    </td>
                    {ehAdmin && <td className="px-4 py-2.5 text-sm text-text2">{c.nomeFantasia}</td>}
                    <td className="px-4 py-2.5 text-sm font-medium tabular-nums text-text1 font-mono">{fmtNota(c.pontuacao)}</td>
                    <td className="px-4 py-2.5"><span className={`badge ${badge.classe}`}>{badge.texto}</span></td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        )}
      </div>

      {modalEmpate && (
        <ModalEmpate
          vagaId={vaga.id}
          empatados={empatados}
          onFechar={() => setModalEmpate(false)}
        />
      )}
    </div>
  )
}

// ─── Modal de resolução de empate ────────────────────────
function ModalEmpate({ vagaId, empatados, onFechar }) {
  const resolver = useResolverEmpate()
  const [vencedorId, setVencedorId] = useState(null)

  // Assumindo empate de dois (o múltiplo é pós-MVP)
  const [a, b] = empatados

  function confirmar() {
    if (!vencedorId) return
    const perdedorId = vencedorId === a.estudanteId ? b.estudanteId : a.estudanteId
    resolver.mutate(
      { vagaId, vencedorId, perdedorId },
      {
        onSuccess: () => onFechar(),
        onError: (err) => alert(err.response?.data?.mensagem || 'Erro ao resolver empate'),
      }
    )
  }

  const opcao = (c) => {
    const escolhido = vencedorId === c.estudanteId
    return (
      <button
        key={c.estudanteId}
        onClick={() => setVencedorId(c.estudanteId)}
        className={`w-full text-left border rounded-xl px-4 py-3 flex items-center gap-3 mt-2 transition-colors ${
          escolhido ? 'border-gold bg-gold-dim' : 'border-border2 hover:border-border'
        }`}
      >
        <span className={`w-4 h-4 rounded-full border-2 grid place-items-center shrink-0 ${
          escolhido ? 'border-gold' : 'border-border2'
        }`}>
          {escolhido && <span className="w-1.5 h-1.5 rounded-full bg-gold"></span>}
        </span>
        <span>
          <span className="text-sm font-medium block text-text1">{c.nome || c.nomeFantasia}</span>
          <span className="text-xs text-text2 tabular-nums font-mono">
            Pontuação {fmtNota(c.pontuacao)}
          </span>
        </span>
      </button>
    )
  }

  return (
    <div className="fixed inset-0 bg-black/60 backdrop-blur-sm grid place-items-center p-6 z-50" onClick={onFechar}>
      <div className="card w-full max-w-md" onClick={(e) => e.stopPropagation()}>
        <div className="px-5 py-4 border-b border-border flex items-start gap-3">
          <div className="w-8 h-8 rounded-lg bg-warning-dim text-warning grid place-items-center text-base shrink-0">!</div>
          <div>
            <h2 className="text-sm font-semibold text-text1">Resolver empate</h2>
            <p className="text-xs text-text2 mt-0.5">
              Os dois empataram na mesma pontuação disputando a última posição
            </p>
          </div>
        </div>

        <div className="p-5">
          <div className="bg-warning-dim border border-warning/30 rounded-lg px-3 py-2 text-xs text-warning mb-3">
            Quem você escolher fica com a posição de dentro. O outro vai para a posição seguinte.
            A decisão será registrada com seu nome.
          </div>

          <p className="field-label">Quem fica com a posição?</p>
          {a && opcao(a)}
          {b && opcao(b)}
        </div>

        <div className="px-5 py-4 border-t border-border flex justify-end gap-2">
          <button onClick={onFechar} className="border border-border2 text-text2 rounded-lg px-4 py-2 text-sm hover:bg-surface2">
            Cancelar
          </button>
          <button
            onClick={confirmar}
            disabled={!vencedorId || resolver.isPending}
            className="bg-gold text-bg rounded-lg px-4 py-2 text-sm font-semibold hover:bg-gold-light disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
          >
            {resolver.isPending ? 'Resolvendo...' : 'Confirmar'}
          </button>
        </div>
      </div>
    </div>
  )
}
