import { useState, useEffect } from 'react'
import { useVagas, useClassificacao } from '../hooks/useApi'

export default function Classificacao() {
  const { data: vagas, isLoading: carregandoVagas } = useVagas()
  const [vagaId, setVagaId] = useState(null)

  // Seleciona a primeira vaga automaticamente quando a lista carrega
  useEffect(() => {
    if (vagas?.length && !vagaId) {
      setVagaId(vagas[0].id)
    }
  }, [vagas, vagaId])

  const { data: classificacao, isLoading: carregandoClass } = useClassificacao(vagaId)

  const vagaAtual = vagas?.find(v => v.id === vagaId)
  const temEmpate = classificacao?.some(c => c.empate)

  if (carregandoVagas) {
    return <p className="text-gray-500">Carregando vagas...</p>
  }

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
            Há empate nesta vaga aguardando resolução. A posição na fronteira é provisória.
          </p>
        </div>
      )}

      <div className="bg-white border border-gray-200 rounded-xl overflow-hidden">
        <div className="px-4 py-3 border-b border-gray-200 flex items-center justify-between gap-3">
          <div>
            <p className="text-sm font-semibold">
              {vagaAtual?.disciplina}
            </p>
            <p className="text-xs text-gray-500 mt-0.5">
              {vagaAtual?.qtdBolsistas} bolsistas · {vagaAtual?.qtdVoluntarios} voluntários · {vagaAtual?.qtdListaEspera} em espera
            </p>
          </div>
          <select
            value={vagaId || ''}
            onChange={(e) => setVagaId(e.target.value)}
            className="border border-gray-300 rounded-lg px-3 py-1.5 text-sm"
          >
            {vagas.map(v => (
              <option key={v.id} value={v.id}>{v.disciplina}</option>
            ))}
          </select>
        </div>

        {carregandoClass ? (
          <p className="text-gray-500 text-sm p-4">Carregando classificação...</p>
        ) : !classificacao?.length ? (
          <p className="text-gray-500 text-sm p-4">
            Nenhum estudante inscrito nesta vaga ainda.
          </p>
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
                    <div className="text-sm font-medium">
                      {c.nome || c.nomeFantasia}
                    </div>
                    {c.nome && (
                      <div className="text-xs text-gray-400">{c.nomeFantasia}</div>
                    )}
                  </td>
                  <td className="px-4 py-2.5 text-sm font-medium tabular-nums">
                    {fmt(c.pontuacao)}
                  </td>
                  <td className="px-4 py-2.5">
                    {tipoTag(c.tipo, c.empate)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  )
}