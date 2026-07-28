import { useState, useEffect } from 'react'
import { useVagas, useClassificacao, useResolverEmpate } from '../hooks/useApi'

export default function Classificacao() {
  const { data: vagas, isLoading: carregandoVagas } = useVagas()
  const [vagaId, setVagaId] = useState(null)
  const [modalEmpate, setModalEmpate] = useState(false)

  useEffect(() => {
    if (vagas?.length && !vagaId) setVagaId(vagas[0].id)
  }, [vagas, vagaId])

  const { data: classificacao, isLoading: carregandoClass } = useClassificacao(vagaId)

  const vagaAtual = vagas?.find(v => v.id === vagaId)
  const empatados = classificacao?.filter(c => c.empate) || []
  const temEmpate = empatados.length > 0

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

  const fmt = (n) => Number(n).toFixed(4).replace('.', ',')

  const tipoTag = (tipo, empate) => {
    if (empate) return <span className="badge badge-yellow">Empate</span>
    if (tipo === 'BOLSISTA') return <span className="badge badge-green">Bolsista</span>
    if (tipo === 'VOLUNTARIO') return <span className="badge badge-accent">Voluntário</span>
    if (tipo === 'LISTA_ESPERA') return <span className="badge badge-neutral">Lista de espera</span>
    return <span className="badge badge-neutral">Fora</span>
  }

  return (
    <div>
      <div className="mb-4">
        <h1 className="text-xl font-display font-bold text-text1">Classificação</h1>
        <p className="text-text2 text-sm mt-0.5">
          A lista é recalculada automaticamente a cada cadastro
        </p>
      </div>

      {temEmpate && (
        <div className="bg-warning-dim border border-warning/30 rounded-xl px-4 py-3 mb-4 flex items-center gap-3">
          <span className="w-2 h-2 rounded-full bg-warning shrink-0"></span>
          <p className="text-sm text-warning flex-1">
            Empate na fronteira desta vaga. A posição provisória precisa ser decidida.
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
        <div className="px-4 py-3 border-b border-border flex items-center justify-between gap-3">
          <div>
            <p className="text-sm font-semibold text-text1">{vagaAtual?.disciplina}</p>
            <p className="text-xs text-text2 mt-0.5">
              {vagaAtual?.qtdBolsistas} bolsistas · {vagaAtual?.qtdVoluntarios} voluntários · {vagaAtual?.qtdListaEspera} em espera
            </p>
          </div>
          <select
            value={vagaId || ''}
            onChange={(e) => setVagaId(e.target.value)}
            className="field w-auto py-1.5"
          >
            {vagas.map(v => <option key={v.id} value={v.id}>{v.disciplina}</option>)}
          </select>
        </div>

        {carregandoClass ? (
          <p className="text-text2 text-sm p-4">Carregando classificação...</p>
        ) : !classificacao?.length ? (
          <p className="text-text2 text-sm p-4">Nenhum estudante inscrito nesta vaga ainda.</p>
        ) : (
          <table className="w-full">
            <thead>
              <tr className="border-b border-border">
                <th className="text-left text-[10px] uppercase tracking-wide text-text3 font-mono font-medium px-4 py-2 w-14">Pos.</th>
                <th className="text-left text-[10px] uppercase tracking-wide text-text3 font-mono font-medium px-4 py-2">Estudante</th>
                <th className="text-left text-[10px] uppercase tracking-wide text-text3 font-mono font-medium px-4 py-2 w-36">Pontuação</th>
                <th className="text-left text-[10px] uppercase tracking-wide text-text3 font-mono font-medium px-4 py-2 w-32">Resultado</th>
              </tr>
            </thead>
            <tbody>
              {classificacao.map((c) => (
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
                  <td className="px-4 py-2.5 text-sm font-medium tabular-nums text-text1 font-mono">{fmt(c.pontuacao)}</td>
                  <td className="px-4 py-2.5">{tipoTag(c.tipo, c.empate)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {modalEmpate && (
        <ModalEmpate
          vagaId={vagaId}
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

  const fmt = (n) => Number(n).toFixed(4).replace('.', ',')

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
            Pontuação {fmt(c.pontuacao)}
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
