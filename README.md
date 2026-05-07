# SISUMONI - Sistema de Automatização de Colocação em Vagas de Monitoria

## 📋 Descrição

**SISUMONI** é um sistema web desenvolvido em **Java com Spring Boot** que automatiza o processo de alocação de alunos em vagas de monitoria de uma universidade. O sistema gerencia estudantes, professores, vagas disponíveis e o processo de candidatura e aprovação de monitores.

## 🎯 Objetivos

- Automatizar o processo de candidatura de alunos a vagas de monitoria
- Otimizar a análise de candidatos para professores
- Manter histórico digitalizado de todas as candidaturas
- Facilitar a comunicação entre professores e alunos
- Garantir transparência no processo de seleção

## 🏗️ Arquitetura

O projeto segue a arquitetura em camadas:

```
┌─────────────────────────────────┐
│      REST APIs (Controllers)    │
├─────────────────────────────────┤
│    Business Logic (Services)    │
├─────────────────────────────────┤
│     Data Access (Repositories)  │
├─────────────────────────────────┤
│      Database (PostgreSQL)      │
└─────────────────────────────────┘
```

## 🚀 Tecnologias

- **Backend**: Java 21 + Spring Boot 3.2
- **Banco de Dados**: PostgreSQL
- **ORM**: Hibernate/JPA
- **Gerenciador de Dependências**: Maven
- **Build Tool**: Maven

## 🔧 Quick Start

### Requisitos Mínimos
- Java 21+
- Maven 3.8.1+
- PostgreSQL 12+

### Instalação Rápida

1. Clone o repositório:
```bash
git clone https://github.com/seu-usuario/sisumoni.git
cd sisumoni
```

2. Configure o PostgreSQL (ver [SETUP.md](SETUP.md) para detalhes)

3. Instale dependências:
```bash
mvn clean install
```

4. Execute a aplicação:
```bash
mvn spring-boot:run
```

5. Acesse em `http://localhost:8080`

## 📚 Estrutura do Projeto

```
src/main/java/br/com/sisumoni/
├── controller/           # REST Endpoints
├── model/               # Entidades JPA
│   └── enums/          # Enumerações
├── repository/         # Spring Data Repositories
├── service/            # Lógica de Negócio
└── SisumoniApplication # Classe Principal
```

## 🔌 API REST

### Recursos Principais

| Recurso | Endpoint | Métodos |
|---------|----------|---------|
| Estudantes | `/api/estudantes` | GET, POST, PUT, DELETE |
| Professores | `/api/professores` | GET, POST, PUT, DELETE |
| Vagas | `/api/vagas` | GET, POST, PUT, DELETE |
| Candidaturas | `/api/aplicacoes` | GET, POST, PUT, DELETE |

Para exemplos completos, consulte [SETUP.md](SETUP.md#exemplos-de-uso-com-curl)

## 📊 Modelos de Dados

### Estudante
- ID, Matrícula, Nome, Email, Média Geral, Período

### Professor
- ID, Nome, Email, Departamento, Telefone, Status

### Vaga de Monitoria
- ID, Disciplina, Descrição, Quantidade de Vagas, Salário/Hora, Carga Horária, Professor, Status

### Aplicação (Candidatura)
- ID, Estudante, Vaga, Status (Pendente/Aprovada/Rejeitada), Data da Candidatura

## 🛣️ Roadmap

- [ ] Autenticação e Autorização (Spring Security)
- [ ] Testes Unitários e de Integração
- [ ] Paginação e Filtros Avançados
- [ ] Frontend em React
- [ ] Documentação Swagger/OpenAPI
- [ ] Deploy em Docker
- [ ] CI/CD com GitHub Actions
- [ ] Sistema de Notificações por Email
- [ ] Dashboard de Estatísticas

## 📖 Documentação

- [SETUP.md](SETUP.md) - Guia completo de configuração e uso
- [API Reference](SETUP.md#endpoints-da-api) - Documentação dos endpoints

## 👨‍💻 Desenvolvimento

### Padrões de Código
- Controllers: Tratam requisições HTTP
- Services: Contêm a lógica de negócio
- Repositories: Acesso ao banco de dados
- Models: Representam entidades do banco

### Convenções
- NomeClasse para classes
- nomePrimetro para atributos e variáveis
- `@RestController` para APIs
- `@Service` para lógica de negócio
- `@Repository` para acesso a dados

## 🤝 Contribuindo

Contribuições são bem-vindas! Por favor:

1. Faça um Fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📝 Licença

Este projeto está sob licença MIT. Veja o arquivo LICENSE para mais detalhes.

## 📧 Suporte

Para dúvidas ou problemas, abra uma issue no repositório.
