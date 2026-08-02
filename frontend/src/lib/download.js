// Dispara o download de um Blob no navegador via link temporário
export function baixarArquivo(blob, nomeArquivo) {
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = nomeArquivo
  document.body.appendChild(a)
  a.click()
  a.remove()
  URL.revokeObjectURL(url)
}

// Requisições com responseType: 'blob' entregam o corpo de erro (ex.: 409)
// como Blob em vez de JSON já parseado — precisa ler e parsear manualmente
// para extrair a mensagem no mesmo formato usado pelo resto do app.
export async function extrairMensagemErroBlob(err) {
  const data = err.response?.data
  if (data instanceof Blob) {
    try {
      const texto = await data.text()
      return JSON.parse(texto).mensagem
    } catch {
      return null
    }
  }
  return data?.mensagem
}
