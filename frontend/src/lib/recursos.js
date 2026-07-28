import api from './api'

// ── Turmas ──────────────────────────────────────────────
export const turmasApi = {
  listar: () => api.get('/turmas').then(r => r.data),
  criar: (nome) => api.post('/turmas', { nome }).then(r => r.data),
  deletar: (id) => api.delete(`/turmas/${id}`),
}

// ── Departamentos ───────────────────────────────────────
export const departamentosApi = {
  listar: () => api.get('/departamentos').then(r => r.data),
  criar: (nome) => api.post('/departamentos', { nome }).then(r => r.data),
  deletar: (id) => api.delete(`/departamentos/${id}`),
}

// ── Vagas ───────────────────────────────────────────────
export const vagasApi = {
  listar: () => api.get('/vagas').then(r => r.data),
  criar: (dados) => api.post('/vagas', dados).then(r => r.data),
  deletar: (id) => api.delete(`/vagas/${id}`),
  classificacao: (id) => api.get(`/vagas/${id}/classificacao`).then(r => r.data),
}

// ── Estudantes ──────────────────────────────────────────
export const estudantesApi = {
  listar: () => api.get('/estudantes').then(r => r.data),
  buscar: (id) => api.get(`/estudantes/${id}`).then(r => r.data),
  criar: (dados) => api.post('/estudantes', dados).then(r => r.data),
  atualizar: (id, dados) => api.put(`/estudantes/${id}`, dados).then(r => r.data),
  deletar: (id) => api.delete(`/estudantes/${id}`),
}

// ── Operadores ──────────────────────────────────────────
export const operadoresApi = {
  listar: () => api.get('/operadores').then(r => r.data),
  criar: (dados) => api.post('/operadores', dados).then(r => r.data),
  deletar: (id) => api.delete(`/operadores/${id}`),
}