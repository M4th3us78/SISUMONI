import { useState } from 'react'
import {
  useVagas, useCriarVaga, useDeletarVaga,
  useDepartamentos, useTurmas,
} from '../hooks/useApi'

export default function Vagas() {
  const { data: vagas, isLoading } = useVagas()
  const { data: departamentos } = useDepartamentos()
  const { data: turmas } = useTurmas()
  const criar = useCriarVaga()
  const deletar = useDeletarVaga()

  const [modalAberto, setModalAberto] = useState(false)

  return (
    <div>
      <div className="flex items-start justify-between mb-4">
        <div>
          <h1 className="text-xl font-semibold">Vagas de monitoria</h1>
          <p className="text-gray-500 text-sm mt-0.5">
            {vagas?.length ?? 0} vagas cadastradas
          </p>
        </div>
        <button
          onClick={() => setModalAberto(true)}
          className="bg-indigo-600 text-white rounded-lg px-4 py-2 text-sm font-medium hover:bg-indigo-700"
        >
          Nova vaga
        </button>
      </div>

      <div className="bg-white border border-gray-200 rounded-xl overflow-hidden">
        {isLoading ? (
          <p className="text-gray-500 text-sm p-4">Carregando...</p>
        ) : !vagas?.length ? (
          <p className="text-gray-500 text-sm p-4">
            Nenhuma vaga cadastrada. Clique em "Nova vaga" para começar.
          </p>
        ) : (
          <table className="w-full">
            <thead>
              <tr className="border-b border-gray-200">
                <th className="text-left text-[10px] uppercase tracking-wide text-gray-400 font-medium px-4 py-2">Disciplina</th>
                <th className="text-left text-[10px] uppercase tracking-wide text-gray-400 font-medium px-4 py-2 w-44">Departamento</th>
                <th className="text-left text-[10px] uppercase tracking-wide text-gray-400 font-medium px-4 py-2 w-32">Vagas</th>
                <th className="text-left text-[10px] uppercase tracking-wide text-gray-400 font-medium px-4 py-2 w-16"></th>
              </tr>
            </thead>
            <tbody>
              {vagas.map((v) => (
                <tr key={v.id} className="border-b border-gray-100 last:border-0">
                  <td className="px-4 py-2.5">
                    <div className="text-sm font-medium">{v.disciplina}</div>
                    <div className="text-xs text-gray-400">{v.professor}</div>
                  </td>
                  <td className="px-4 py-2.5 text-sm text-gray-600">
                    {v.departamento?.nome}
                  </td>
                  <td className="px-4 py-2.5 text-xs text-gray-600 tabular-nums">
                    {v.qtdBolsistas}B · {v.qtdVoluntarios}V · {v.qtdListaEspera}E
                  </td>
                  <td className="px-4 py-2.5 text-right">
                    <button
                      onClick={() => {
                        if (confirm(`Remover a vaga "${v.disciplina}"?`)) {
                          deletar.mutate(v.id, {
                            onError: (err) => alert(err.response?.data?.mensagem || 'Erro ao remover'),
                          })
                        }
                      }}
                      className="text-xs text-rose-600 hover:text-rose-800"
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
        <ModalVaga
          departamentos={departamentos || []}
          turmas={turmas || []}
          onFechar={() => setModalAberto(false)}
          onSalvar={(dados) => {
            criar.mutate(dados, {
              onSuccess: () => setModalAberto(false),
              onError: (err) => alert(err.response?.data?.mensagem || 'Erro ao cadastrar vaga'),
            })
          }}
          salvando={criar.isPending}
        />
      )}
    </div>
  )
}

// ─── Modal de cadastro de vaga ───────────────────────────
function ModalVaga({ departamentos, turmas, onFechar, onSalvar, salvando }) {
  const [form, setForm] = useState({
    disciplina: '', professor: '', departamentoId: '',
    qtdBolsistas: '1', qtdVoluntarios: '2', qtdListaEspera: '1',
  })
  const [turmasSelecionadas, setTurmasSelecionadas] = useState([])

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
    const esp = parseInt(form.qtdListaEspera) || 0

    if (bols + vol + esp === 0) {
      alert('A vaga precisa ter ao menos uma posição (bolsista, voluntário ou espera).')
      return
    }

    onSalvar({
      disciplina: form.disciplina.trim(),
      professor: form.professor.trim(),
      departamentoId: form.departamentoId,
      qtdBolsistas: bols,
      qtdVoluntarios: vol,
      qtdListaEspera: esp,
      turmasIds: turmasSelecionadas,
    })
  }

  const campo = "border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 w-full"
  const label = "text-xs font-medium text-gray-600 mb-1 block"

  return (
    <div className="fixed inset-0 bg-black/40 grid place-items-center p-6 z-50" onClick={onFechar}>
      <div
        className="bg-white rounded-2xl w-full max-w-lg max-h-[90vh] overflow-y-auto"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="px-5 py-4 border-b border-gray-200">
          <h2 className="text-sm font-semibold">Cadastrar vaga</h2>
        </div>

        <div className="p-5">
          <div className="grid grid-cols-2 gap-3 mb-3">
            <div>
              <label className={label}>Disciplina</label>
              <input className={campo} value={form.disciplina} onChange={(e) => set('disciplina', e.target.value)} />
            </div>
            <div>
              <label className={label}>Professor responsável</label>
              <input className={campo} value={form.professor} onChange={(e) => set('professor', e.target.value)} />
            </div>
          </div>

          <div className="mb-3">
            <label className={label}>Departamento</label>
            <select className={campo} value={form.departamentoId} onChange={(e) => set('departamentoId', e.target.value)}>
              <option value="">Selecione</option>
              {departamentos.map((d) => <option key={d.id} value={d.id}>{d.nome}</option>)}
            </select>
          </div>

          <p className="text-[10px] uppercase tracking-wide text-gray-400 font-medium mb-2 mt-4">Quantidade de posições</p>
          <div className="grid grid-cols-3 gap-3">
            <div>
              <label className={label}>Bolsistas</label>
              <input type="text" inputMode="numeric" className={campo} value={form.qtdBolsistas} onChange={(e) => set('qtdBolsistas', e.target.value.replace(/\D/g, ''))} />
            </div>
            <div>
              <label className={label}>Voluntários</label>
              <input type="text" inputMode="numeric" className={campo} value={form.qtdVoluntarios} onChange={(e) => set('qtdVoluntarios', e.target.value.replace(/\D/g, ''))} />
            </div>
            <div>
              <label className={label}>Lista de espera</label>
              <input type="text" inputMode="numeric" className={campo} value={form.qtdListaEspera} onChange={(e) => set('qtdListaEspera', e.target.value.replace(/\D/g, ''))} />
            </div>
          </div>

          <p className="text-[10px] uppercase tracking-wide text-gray-400 font-medium mb-2 mt-4">Turmas que podem se candidatar</p>
          {turmas.length === 0 ? (
            <p className="text-xs text-gray-400">Cadastre turmas primeiro.</p>
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
                        ? 'border-indigo-600 bg-indigo-50 text-indigo-700 font-medium'
                        : 'border-gray-300 text-gray-600 hover:border-gray-400'
                    }`}
                  >
                    {t.nome}
                  </button>
                )
              })}
            </div>
          )}
        </div>

        <div className="px-5 py-4 border-t border-gray-200 flex justify-end gap-2">
          <button onClick={onFechar} className="border border-gray-300 rounded-lg px-4 py-2 text-sm hover:bg-gray-50">
            Cancelar
          </button>
          <button
            onClick={submeter}
            disabled={salvando}
            className="bg-indigo-600 text-white rounded-lg px-4 py-2 text-sm font-medium hover:bg-indigo-700 disabled:opacity-50"
          >
            {salvando ? 'Salvando...' : 'Salvar vaga'}
          </button>
        </div>
      </div>
    </div>
  )
}