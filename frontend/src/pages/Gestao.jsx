import { useState } from 'react'
import {
  useTurmas, useCriarTurma, useDeletarTurma,
  useDepartamentos, useCriarDepartamento, useDeletarDepartamento,
} from '../hooks/useApi'

export default function Gestao() {
  const [aba, setAba] = useState('turmas')

  return (
    <div>
      <div className="mb-4">
        <h1 className="text-xl font-semibold">Turmas e departamentos</h1>
        <p className="text-gray-500 text-sm mt-0.5">
          Cadastros base do sistema
        </p>
      </div>

      <div className="flex gap-1 mb-4 border-b border-gray-200">
        <button
          onClick={() => setAba('turmas')}
          className={`px-4 py-2 text-sm font-medium border-b-2 -mb-px transition-colors ${
            aba === 'turmas'
              ? 'border-indigo-600 text-indigo-700'
              : 'border-transparent text-gray-500 hover:text-gray-800'
          }`}
        >
          Turmas
        </button>
        <button
          onClick={() => setAba('departamentos')}
          className={`px-4 py-2 text-sm font-medium border-b-2 -mb-px transition-colors ${
            aba === 'departamentos'
              ? 'border-indigo-600 text-indigo-700'
              : 'border-transparent text-gray-500 hover:text-gray-800'
          }`}
        >
          Departamentos
        </button>
      </div>

      {aba === 'turmas' ? <PainelTurmas /> : <PainelDepartamentos />}
    </div>
  )
}

// ─── Painel de turmas ────────────────────────────────────
function PainelTurmas() {
  const { data: turmas, isLoading } = useTurmas()
  const criar = useCriarTurma()
  const deletar = useDeletarTurma()
  const [nome, setNome] = useState('')

  function adicionar() {
    if (!nome.trim()) return
    criar.mutate(nome.trim(), {
      onSuccess: () => setNome(''),
      onError: (err) => alert(err.response?.data?.mensagem || 'Erro ao criar turma'),
    })
  }

  return (
    <CrudSimples
      itens={turmas}
      isLoading={isLoading}
      nome={nome}
      setNome={setNome}
      onAdicionar={adicionar}
      salvando={criar.isPending}
      onDeletar={(id, label) => {
        if (confirm(`Remover a turma "${label}"?`)) {
          deletar.mutate(id, {
            onError: (err) => alert(err.response?.data?.mensagem || 'Erro ao remover'),
          })
        }
      }}
      placeholder="Ex: Medicina 3º período"
      vazio="Nenhuma turma cadastrada."
    />
  )
}

// ─── Painel de departamentos ─────────────────────────────
function PainelDepartamentos() {
  const { data: departamentos, isLoading } = useDepartamentos()
  const criar = useCriarDepartamento()
  const deletar = useDeletarDepartamento()
  const [nome, setNome] = useState('')

  function adicionar() {
    if (!nome.trim()) return
    criar.mutate(nome.trim(), {
      onSuccess: () => setNome(''),
      onError: (err) => alert(err.response?.data?.mensagem || 'Erro ao criar departamento'),
    })
  }

  return (
    <CrudSimples
      itens={departamentos}
      isLoading={isLoading}
      nome={nome}
      setNome={setNome}
      onAdicionar={adicionar}
      salvando={criar.isPending}
      onDeletar={(id, label) => {
        if (confirm(`Remover o departamento "${label}"?`)) {
          deletar.mutate(id, {
            onError: (err) => alert(err.response?.data?.mensagem || 'Erro ao remover'),
          })
        }
      }}
      placeholder="Ex: Morfologia"
      vazio="Nenhum departamento cadastrado."
    />
  )
}

// ─── Componente reutilizável de CRUD simples ─────────────
function CrudSimples({ itens, isLoading, nome, setNome, onAdicionar, salvando, onDeletar, placeholder, vazio }) {
  return (
    <div>
      <div className="flex gap-2 mb-4">
        <input
          value={nome}
          onChange={(e) => setNome(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && onAdicionar()}
          placeholder={placeholder}
          className="border border-gray-300 rounded-lg px-3 py-2 text-sm flex-1 focus:outline-none focus:ring-2 focus:ring-indigo-500"
        />
        <button
          onClick={onAdicionar}
          disabled={salvando}
          className="bg-indigo-600 text-white rounded-lg px-4 py-2 text-sm font-medium hover:bg-indigo-700 disabled:opacity-50"
        >
          Adicionar
        </button>
      </div>

      <div className="bg-white border border-gray-200 rounded-xl overflow-hidden">
        {isLoading ? (
          <p className="text-gray-500 text-sm p-4">Carregando...</p>
        ) : !itens?.length ? (
          <p className="text-gray-500 text-sm p-4">{vazio}</p>
        ) : (
          <ul>
            {itens.map((item) => (
              <li key={item.id} className="flex items-center justify-between px-4 py-3 border-b border-gray-100 last:border-0">
                <span className="text-sm">{item.nome}</span>
                <button
                  onClick={() => onDeletar(item.id, item.nome)}
                  className="text-xs text-rose-600 hover:text-rose-800"
                >
                  Remover
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  )
}