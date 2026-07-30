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
        <h1 className="text-xl font-display font-bold text-text1">Turmas e departamentos</h1>
        <p className="text-text2 text-sm mt-0.5">
          Cadastros base do sistema
        </p>
      </div>

      <div className="flex gap-1 mb-4 border-b border-border">
        <button
          onClick={() => setAba('turmas')}
          className={`px-4 py-2 text-sm font-medium border-b-2 -mb-px transition-colors ${
            aba === 'turmas'
              ? 'border-gold text-gold'
              : 'border-transparent text-text2 hover:text-text1'
          }`}
        >
          Turmas
        </button>
        <button
          onClick={() => setAba('departamentos')}
          className={`px-4 py-2 text-sm font-medium border-b-2 -mb-px transition-colors ${
            aba === 'departamentos'
              ? 'border-gold text-gold'
              : 'border-transparent text-text2 hover:text-text1'
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
          className="field flex-1"
        />
        <button
          onClick={onAdicionar}
          disabled={salvando}
          className="bg-gold text-bg rounded-lg px-4 py-2 text-sm font-semibold hover:bg-gold-light disabled:opacity-50 transition-colors"
        >
          Adicionar
        </button>
      </div>

      <div className="card overflow-hidden">
        {isLoading ? (
          <p className="text-text2 text-sm p-4">Carregando...</p>
        ) : !itens?.length ? (
          <p className="text-text2 text-sm p-4">{vazio}</p>
        ) : (
          <ul>
            {itens.map((item) => (
              <li key={item.id} className="flex items-center justify-between px-4 py-3 border-b border-border2 last:border-0 hover:bg-surface2">
                <span className="text-sm text-text1">{item.nome}</span>
                <button
                  onClick={() => onDeletar(item.id, item.nome)}
                  className="btn-chip btn-chip-danger"
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
