# Guia de Setup - SISUMONI

## Pré-requisitos

- Java 21 ou superior
- Maven 3.8.1+
- PostgreSQL 12+
- Git

## Instalação do PostgreSQL

### Linux (Ubuntu/Debian)
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
```

### macOS
```bash
brew install postgresql
brew services start postgresql
```

### Windows
Baixe em: https://www.postgresql.org/download/windows/

## Configuração do Banco de Dados

1. Acesse o PostgreSQL:
```bash
sudo -u postgres psql
```

2. Crie o banco de dados:
```sql
CREATE DATABASE sisumoni_db;
CREATE USER sisumoni_user WITH PASSWORD 'sisumoni_pass';
ALTER ROLE sisumoni_user SET client_encoding TO 'utf8';
ALTER ROLE sisumoni_user SET default_transaction_isolation TO 'read committed';
ALTER ROLE sisumoni_user SET default_transaction_deferrable TO on;
ALTER ROLE sisumoni_user SET default_transaction_read_only TO off;
GRANT ALL PRIVILEGES ON DATABASE sisumoni_db TO sisumoni_user;
\q
```

## Configuração da Aplicação

### Opção 1: Usando as credenciais padrão

Se você usou as credenciais acima, o arquivo `application.properties` já está configurado.

### Opção 2: Usando credenciais customizadas

Edite o arquivo `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/seu_db
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
```

## Instalando Dependências

```bash
mvn clean install
```

## Rodando a Aplicação

```bash
mvn spring-boot:run
```

A aplicação estará disponível em: `http://localhost:8080`

## Endpoints da API

### Estudantes
- `GET /api/estudantes` - Listar todos
- `GET /api/estudantes/{id}` - Obter por ID
- `GET /api/estudantes/matricula/{matricula}` - Obter por matrícula
- `POST /api/estudantes` - Criar novo
- `PUT /api/estudantes/{id}` - Atualizar
- `DELETE /api/estudantes/{id}` - Deletar

### Professores
- `GET /api/professores` - Listar todos
- `GET /api/professores/ativos` - Listar ativos
- `GET /api/professores/{id}` - Obter por ID
- `POST /api/professores` - Criar novo
- `PUT /api/professores/{id}` - Atualizar
- `DELETE /api/professores/{id}` - Deletar

### Vagas de Monitoria
- `GET /api/vagas` - Listar todas
- `GET /api/vagas/abertas` - Listar vagas abertas
- `GET /api/vagas/{id}` - Obter por ID
- `GET /api/vagas/professor/{professorId}` - Listar por professor
- `GET /api/vagas/buscar?disciplina=...` - Buscar por disciplina
- `POST /api/vagas` - Criar nova
- `PUT /api/vagas/{id}` - Atualizar
- `PUT /api/vagas/{id}/status` - Atualizar status
- `DELETE /api/vagas/{id}` - Deletar

### Aplicações (Candidaturas)
- `POST /api/aplicacoes/candidatar` - Candidatar a uma vaga
- `GET /api/aplicacoes/{id}` - Obter por ID
- `GET /api/aplicacoes/estudante/{estudanteId}` - Listar por estudante
- `GET /api/aplicacoes/vaga/{vagaId}` - Listar por vaga
- `GET /api/aplicacoes/pendentes` - Listar pendentes
- `PUT /api/aplicacoes/{id}/aprovar` - Aprovar candidato
- `PUT /api/aplicacoes/{id}/rejeitar` - Rejeitar candidato
- `DELETE /api/aplicacoes/{id}` - Deletar

## Exemplos de Uso com cURL

### Criar um Estudante
```bash
curl -X POST http://localhost:8080/api/estudantes \
  -H "Content-Type: application/json" \
  -d '{
    "matricula": "2022001",
    "nome": "João Silva",
    "email": "joao@example.com",
    "mediaGeral": 8.5,
    "periodoAtual": 4
  }'
```

### Criar um Professor
```bash
curl -X POST http://localhost:8080/api/professores \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Prof. Maria",
    "email": "maria@example.com",
    "departamento": "Computação",
    "telefone": "1234567890",
    "ativo": true
  }'
```

### Criar uma Vaga
```bash
curl -X POST http://localhost:8080/api/vagas \
  -H "Content-Type: application/json" \
  -d '{
    "disciplina": "Algoritmos",
    "descricao": "Monitor para disciplina de Algoritmos",
    "quantidadeVagas": 2,
    "salarioHora": 15.50,
    "cargaHoraria": 20,
    "professorId": 1
  }'
```

### Candidatar a uma Vaga
```bash
curl -X POST http://localhost:8080/api/aplicacoes/candidatar \
  -H "Content-Type: application/json" \
  -d '{
    "estudanteId": 1,
    "vagaId": 1
  }'
```

## Estrutura do Projeto

```
SISUMONI/
├── src/
│   ├── main/
│   │   ├── java/br/com/sisumoni/
│   │   │   ├── controller/        # REST Controllers
│   │   │   ├── model/             # Entidades JPA
│   │   │   ├── repository/        # Spring Data Repositories
│   │   │   ├── service/           # Lógica de negócio
│   │   │   └── SisumoniApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── pom.xml
└── README.md
```

## Tecnologias Utilizadas

- **Spring Boot 3.2.0** - Framework web
- **Spring Data JPA** - ORM
- **PostgreSQL** - Banco de dados
- **Maven** - Gerenciador de dependências
- **Lombok** - Redução de boilerplate
- **Java 21** - Linguagem

## Próximos Passos

1. Criar testes unitários e de integração
2. Implementar autenticação e autorização (Spring Security)
3. Adicionar validações mais robustas
4. Implementar paginação
5. Criar um frontend em React/Vue
6. Configurar CI/CD
7. Implementar cache
8. Adicionar documentação Swagger/OpenAPI
