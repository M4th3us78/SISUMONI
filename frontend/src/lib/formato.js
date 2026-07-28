// Formata número com 4 casas decimais e vírgula (padrão BR)
export const fmtNota = (n) => (n == null ? '—' : Number(n).toFixed(4).replace('.', ','))

// Mapeia tipo/empate de uma classificação para {texto, classe} de badge
export function tipoBadge(tipo, empate) {
  if (empate) return { texto: 'Empate', classe: 'badge-yellow' }
  if (tipo === 'BOLSISTA') return { texto: 'Bolsista', classe: 'badge-green' }
  if (tipo === 'VOLUNTARIO') return { texto: 'Voluntário', classe: 'badge-accent' }
  if (tipo === 'LISTA_ESPERA') return { texto: 'Lista de espera', classe: 'badge-neutral' }
  return { texto: 'Não classificado', classe: 'badge-neutral' }
}
