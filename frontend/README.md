# SISUMONI — frontend

Interface web do SISUMONI: React 19 + Vite, React Router, TanStack Query e Tailwind CSS.

```bash
npm install
npm run dev      # http://localhost:5173 (espera o backend em http://localhost:8080)
npm run lint
npm run build
```

Organização de `src/`:

| Pasta | Conteúdo |
| --- | --- |
| `pages/` | Uma página por rota: login, troca de senha, classificação, estudantes, vagas, gestão, operadores |
| `components/` | `Layout` — casca com navegação e controle de tema |
| `contexts/` | `AuthContext` (sessão e JWT) e `ThemeContext` (claro/escuro) |
| `hooks/` | `useApi` — wrappers de React Query sobre a API |
| `lib/` | Cliente Axios, formatação de números no padrão pt-BR e download de PDFs |

A documentação completa do projeto — arquitetura, algoritmo de classificação, API e
deploy — está no [README da raiz](../README.md).
