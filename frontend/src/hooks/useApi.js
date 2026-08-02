import { useQuery, useQueries, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  turmasApi, departamentosApi, vagasApi, estudantesApi, operadoresApi, relatoriosApi,
} from '../lib/recursos'
import { baixarArquivo } from '../lib/download'

// ── Turmas ──────────────────────────────────────────────
export function useTurmas() {
  return useQuery({ queryKey: ['turmas'], queryFn: turmasApi.listar })
}
export function useCriarTurma() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: turmasApi.criar,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['turmas'] }),
  })
}
export function useDeletarTurma() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: turmasApi.deletar,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['turmas'] }),
  })
}

// ── Departamentos ───────────────────────────────────────
export function useDepartamentos() {
  return useQuery({ queryKey: ['departamentos'], queryFn: departamentosApi.listar })
}
export function useCriarDepartamento() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: departamentosApi.criar,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['departamentos'] }),
  })
}
export function useDeletarDepartamento() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: departamentosApi.deletar,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['departamentos'] }),
  })
}

// ── Vagas ───────────────────────────────────────────────
export function useVagas() {
  return useQuery({ queryKey: ['vagas'], queryFn: vagasApi.listar })
}
export function useCriarVaga() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: vagasApi.criar,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['vagas'] }),
  })
}
export function useAtualizarVaga() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, dados }) => vagasApi.atualizar(id, dados),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['vagas'] })
      qc.invalidateQueries({ queryKey: ['classificacao'] })
    },
  })
}
export function useDeletarVaga() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: vagasApi.deletar,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['vagas'] }),
  })
}
export function useClassificacao(vagaId) {
  return useQuery({
    queryKey: ['classificacao', vagaId],
    queryFn: () => vagasApi.classificacao(vagaId),
    enabled: !!vagaId, // só busca se tiver vagaId
  })
}

// Busca a classificação de várias vagas em paralelo (mesma queryKey do
// useClassificacao, então o cache é compartilhado entre os dois hooks)
export function useClassificacoesPorVagas(vagaIds) {
  return useQueries({
    queries: (vagaIds || []).map((id) => ({
      queryKey: ['classificacao', id],
      queryFn: () => vagasApi.classificacao(id),
      enabled: !!id,
    })),
  })
}

// ── Estudantes ──────────────────────────────────────────
export function useEstudantes() {
  return useQuery({ queryKey: ['estudantes'], queryFn: estudantesApi.listar })
}
export function useCriarEstudante() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: estudantesApi.criar,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['estudantes'] })
      qc.invalidateQueries({ queryKey: ['classificacao'] })
    },
  })
}
export function useAtualizarEstudante() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, dados }) => estudantesApi.atualizar(id, dados),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['estudantes'] })
      qc.invalidateQueries({ queryKey: ['classificacao'] })
    },
  })
}
export function useDeletarEstudante() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: estudantesApi.deletar,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['estudantes'] })
      qc.invalidateQueries({ queryKey: ['classificacao'] })
    },
  })
}

// ── Operadores ──────────────────────────────────────────
export function useOperadores() {
  return useQuery({ queryKey: ['operadores'], queryFn: operadoresApi.listar })
}
export function useCriarOperador() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: operadoresApi.criar,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['operadores'] }),
  })
}
export function useAtualizarOperador() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, dados }) => operadoresApi.atualizar(id, dados),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['operadores'] }),
  })
}
export function useDeletarOperador() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: operadoresApi.deletar,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['operadores'] }),
  })
}

// ── Relatórios ────────────────────────────────────────────
export function useRelatorioNomesFantasia() {
  return useMutation({
    mutationFn: async () => {
      const blob = await relatoriosApi.nomesFantasia()
      baixarArquivo(blob, 'classificacao-nomes-fantasia.pdf')
    },
  })
}
export function useRelatorioNomesReais() {
  return useMutation({
    mutationFn: async () => {
      const blob = await relatoriosApi.nomesReais()
      baixarArquivo(blob, 'classificacao-nomes-reais.pdf')
    },
  })
}

export function useResolverEmpate() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ vagaId, vencedorId, perdedorId }) =>
      vagasApi.resolverEmpate(vagaId, { vencedorId, perdedorId }),
    onSuccess: (_, { vagaId }) => {
      qc.invalidateQueries({ queryKey: ['classificacao', vagaId] })
    },
  })
}