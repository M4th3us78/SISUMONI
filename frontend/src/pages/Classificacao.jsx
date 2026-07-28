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

  if (carregandoVagas) return <p className="text-gray-500">Carregando vagas...</p>

  if (!vagas?.length) {
    return (
      <div>
        <h1 className="text-xl font-semibold mb-1">Classificação</h1>
        <p className="text-gray-500 text-sm">
          Nenhuma vaga cadastrada ainda. Peça ao administrador para cadastrar vagas.
        </p>
      </div>
    )
  }

  const fmt = (n) => Number(n).toFixed(4).replace('.', ',')

  const tipoTag = (tipo, empate) => {
    if (empate) return <span className="tag bg-amber-100 text-amber-800">Empate</span>
    if (tipo === 'BOLSISTA') return <span className="tag bg-teal-100 text-teal-800">Bolsista</span>
    if (tipo === 'VOLUNTARIO') return <span className="tag bg-indigo-100 text-indigo-800">Voluntário</span>
    if (tipo === 'LISTA_ESPERA') return <span className="tag bg-gray-100 text-gray-600">Lista de espera</span>
    return <span className="tag bg-gray-100 text-gray-500">Fora</span>
  }

  return (
    <div>
      <div className="mb-4">
        <h1 className="text-xl font-semibold">Classificação</h1>
        <p className="text-gray-500 text-sm mt-0.5">
          A lista é recalculada automaticamente a cada cadastro
        </p>
      </div>

      {temEmpate && (
        <div className="bg-amber-50 border border-amber-200 rounded-xl px-4 py-3 mb-4 flex items-center gap-3">
          <span className="w-2 h-2 rounded-full bg-amber-500 shrink-0"></span>
          <p className="text-sm text-amber-900 flex-1">
            Empate na fronteira desta vaga. A posição provisória precisa ser decidida.
          </p>
          <button
            onClick={() => setModalEmpate(true)}
            className="bg-white border border-amber-300 text-amber-800 rounded-lg px-3 py-1.5 text-sm font-medium hover:bg-amber-100"
          >
            Resolver empate
          </button>
        </div>
      )}

      <div className="bg-white border border-gray-200 rounded-xl overflow-hidden">
        <div className="px-4 py-3 border-b border-gray-200 flex items-center justify-between gap-3">
          <div>
            <p className="text-sm font-semibold">{vagaAtual?.disciplina}</p>
            <p className="text-xs text-gray-500 mt-0.5">
              {vagaAtual?.qtdBolsistas} bolsistas · {vagaAtual?.qtdVoluntarios} voluntários · {vagaAtual?.qtdListaEspera} em espera
            </p>
          </div>
          <select
            value={vagaId || ''}
            onChange={(e) => setVagaId(e.target.value)}
            className="border border-gray-300 rounded-lg px-3 py-1.5 text-sm"
          >
            {vagas.map(v => <option key={v.id} value={v.id}>{v.disciplina}</option>)}
          </select>
        </div>

        {carregandoClass ? (
          <p className="text-gray-500 text-sm p-4">Carregando classificação...</p>
        ) : !classificacao?.length ? (
          <p className="text-gray-500 text-sm p-4">Nenhum estudante inscrito nesta vaga ainda.</p>
        ) : (
          <table className="w-full">
            <thead>
              <tr className="border-b border-gray-200">
                <th className="text-left text-[10px] uppercase tracking-wide text-gray-400 font-medium px-4 py-2 w-14">Pos.</th>
                <th className="text-left text-[10px] uppercase tracking-wide text-gray-400 font-medium px-4 py-2">Estudante</th>
                <th className="text-left text-[10px] uppercase tracking-wide text-gray-400 font-medium px-4 py-2 w-36">Pontuação</th>
                <th className="text-left text-[10px] uppercase tracking-wide text-gray-400 font-medium px-4 py-2 w-32">Resultado</th>
              </tr>
            </thead>
            <tbody>
              {classificacao.map((c) => (
                <tr key={c.estudanteId} className={`border-b border-gray-100 last:border-0 ${c.empate ? 'bg-amber-50' : ''}`}>
                  <td className="px-4 py-2.5">
                    <span className={`inline-grid place-items-center w-6 h-6 rounded-md text-xs font-medium ${
                      c.tipo === 'BOLSISTA' ? 'bg-teal-50 text-teal-700' :
                      (c.tipo === 'LISTA_ESPERA' || !c.tipo) ? 'text-gray-400' : 'bg-indigo-50 text-indigo-700'
                    }`}>
                      {c.tipo === 'LISTA_ESPERA' || !c.tipo ? '–' : c.posicao}
                    </span>
                  </td>
                  <td className="px-4 py-2.5">
                    <div className="text-sm font-medium">{c.nome || c.nomeFantasia}</div>
                    {c.nome && <div className="text-xs text-gray-400">{c.nomeFantasia}</div>}
                  </td>
                  <td className="px-4 py-2.5 text-sm font-medium tabular-nums">{fmt(c.pontuacao)}</td>
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
          escolhido ? 'border-indigo-600 bg-indigo-50' : 'border-gray-200 hover:border-gray-300'
        }`}
      >
        <span className={`w-4 h-4 rounded-full border-2 grid place-items-center shrink-0 ${
          escolhido ? 'border-indigo-600' : 'border-gray-300'
        }`}>
          {escolhido && <span className="w-1.5 h-1.5 rounded-full bg-indigo-600"></span>}
        </span>
        <span>
          <span className="text-sm font-medium block">{c.nome || c.nomeFantasia}</span>
          <span className="text-xs text-gray-500 tabular-nums">
            Pontuação {fmt(c.pontuacao)}
          </span>
        </span>
      </button>
    )
  }

  return (
    <div className="fixed inset-0 bg-black/40 grid place-items-center p-6 z-50" onClick={onFechar}>
      <div className="bg-white rounded-2xl w-full max-w-md" onClick={(e) => e.stopPropagation()}>
        <div className="px-5 py-4 border-b border-gray-200 flex items-start gap-3">
          <div className="w-8 h-8 rounded-lg bg-amber-100 text-amber-700 grid place-items-center text-base shrink-0">!</div>
          <div>
            <h2 className="text-sm font-semibold">Resolver empate</h2>
            <p className="text-xs text-gray-500 mt-0.5">
              Os dois empataram na mesma pontuação disputando a última posição
            </p>
          </div>
        </div>

        <div className="p-5">
          <div className="bg-amber-50 border border-amber-200 rounded-lg px-3 py-2 text-xs text-amber-900 mb-3">
            Quem você escolher fica com a posição de dentro. O outro vai para a posição seguinte.
            A decisão será registrada com seu nome.
          </div>

          <p className="text-xs font-medium text-gray-600 mb-1">Quem fica com a posição?</p>
          {a && opcao(a)}
          {b && opcao(b)}
        </div>

        <div className="px-5 py-4 border-t border-gray-200 flex justify-end gap-2">
          <button onClick={onFechar} className="border border-gray-300 rounded-lg px-4 py-2 text-sm hover:bg-gray-50">
            Cancelar
          </button>
          <button
            onClick={confirmar}
            disabled={!vencedorId || resolver.isPending}
            className="bg-indigo-600 text-white rounded-lg px-4 py-2 text-sm font-medium hover:bg-indigo-700 disabled:opacity-40 disabled:cursor-not-allowed"
          >
            {resolver.isPending ? 'Resolvendo...' : 'Confirmar'}
          </button>
        </div>
      </div>
    </div>
  )
}