# 📚 Guia de Aprendizado - SISUMONI

Este documento ajuda a entender os conceitos Java e Spring utilizados no projeto.

## 📖 Índice
1. [Anotações Spring](#anotações-spring)
2. [JPA e Hibernate](#jpa-e-hibernate)
3. [Padrão MVC](#padrão-mvc)
4. [DTOs vs Entidades](#dtos-vs-entidades)
5. [Recurso Recomendado](#recurso-recomendado)

---

## Anotações Spring

### @SpringBootApplication
Marca a classe como ponto de entrada da aplicação Spring Boot.

```java
@SpringBootApplication
public class SisumoniApplication {
    public static void main(String[] args) {
        SpringApplication.run(SisumoniApplication.class, args);
    }
}
```

### @RestController
Marca uma classe como um controlador REST. Combina `@Controller` e `@ResponseBody`.

```java
@RestController
@RequestMapping("/api/estudantes")
public class EstudanteController {
    // Todos os métodos retornam JSON automaticamente
}
```

### @RequestMapping
Define a rota base para o controlador.

```java
@RequestMapping("/api/estudantes")  // Base: /api/estudantes
```

### @GetMapping, @PostMapping, @PutMapping, @DeleteMapping
Mapeiam métodos HTTP específicos.

```java
@GetMapping("/{id}")        // GET /api/estudantes/{id}
@PostMapping              // POST /api/estudantes
@PutMapping("/{id}")       // PUT /api/estudantes/{id}
@DeleteMapping("/{id}")    // DELETE /api/estudantes/{id}
```

### @Service
Marca uma classe como serviço (camada de negócio).

```java
@Service
public class EstudanteService {
    // Contém lógica de negócio
}
```

### @Repository
Marca uma interface como repositório (acesso a dados).

```java
@Repository
public interface EstudanteRepository extends JpaRepository<Estudante, Long> {
    Optional<Estudante> findByMatricula(String matricula);
}
```

### @Autowired
Injeta dependências automaticamente.

```java
@Service
public class EstudanteService {
    @Autowired
    private EstudanteRepository repository;
    // Repository é injetado automaticamente
}
```

### @CrossOrigin
Permite requisições de outros domínios (CORS).

```java
@RestController
@CrossOrigin(origins = "*")  // Permite requisições de qualquer origem
public class EstudanteController {
}
```

---

## JPA e Hibernate

### O que é JPA?
Java Persistence API (JPA) é um padrão para mapeamento objeto-relacional.

### @Entity
Marca uma classe como entidade do banco de dados.

```java
@Entity
@Table(name = "estudantes")
public class Estudante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
```

### @Column
Define configurações de uma coluna do banco.

```java
@Column(nullable = false, unique = true)
private String matricula;

@Column(name = "data_criacao", updatable = false)
private LocalDateTime dataCriacao;
```

### @ManyToOne
Define relacionamento de muitos para um.

```java
@Entity
public class Aplicacao {
    @ManyToOne
    @JoinColumn(name = "estudante_id")
    private Estudante estudante;  // Muitas aplicações, um estudante
}
```

### @Enumerated
Mapeia um enum para o banco de dados.

```java
@Enumerated(EnumType.STRING)
private StatusAplicacao status;  // STRING = armazena o nome do enum
```

### CrudRepository vs JpaRepository
```java
// CrudRepository: Operações básicas
public interface EstudanteRepository extends CrudRepository<Estudante, Long>

// JpaRepository: Estende CrudRepository + paginação e sorting
public interface EstudanteRepository extends JpaRepository<Estudante, Long>
```

### Query Methods
Spring Data JPA gera queries automaticamente pelo nome do método.

```java
Optional<Estudante> findByMatricula(String matricula);
// Gera: SELECT * FROM estudantes WHERE matricula = ?

List<Estudante> findByNomeContainingIgnoreCase(String nome);
// Gera: SELECT * FROM estudantes WHERE UPPER(nome) LIKE UPPER(CONCAT('%', ?, '%'))

List<Estudante> findByMediaGeralGreaterThan(Double media);
// Gera: SELECT * FROM estudantes WHERE media_geral > ?
```

---

## Padrão MVC

SISUMONI segue o padrão MVC (Model-View-Controller):

### Camadas

```
┌─────────────────────────────────┐
│   Controller (Apresentação)     │  Recebe requisições HTTP
│   (EstudanteController)         │  Valida entrada
│   ↓↑                           │
├─────────────────────────────────┤
│   Service (Negócio)             │  Lógica de negócio
│   (EstudanteService)            │  Orquestração
│   ↓↑                           │
├─────────────────────────────────┤
│   Repository (Persistência)     │  Acesso a dados
│   (EstudanteRepository)         │  Operações CRUD
│   ↓↑                           │
├─────────────────────────────────┤
│   Database (PostgreSQL)         │  Armazena dados
└─────────────────────────────────┘
```

### Fluxo de uma Requisição

```
1. Cliente faz requisição HTTP
   GET /api/estudantes/1

2. Controller recebe e chama Service
   estudanteService.obterEstudante(1)

3. Service executa lógica de negócio
   validar permissões
   aplicar regras de negócio

4. Service chama Repository
   estudanteRepository.findById(1)

5. Repository acessa banco de dados
   SQL: SELECT * FROM estudantes WHERE id = 1

6. Banco retorna dados
   → Repository
   → Service
   → Controller
   → JSON
   → Cliente
```

---

## DTOs vs Entidades

### Entidades
- Mapeadas ao banco de dados
- Contêm lógica de persistência
- Relacionamentos com outras entidades

```java
@Entity
@Table(name = "estudantes")
public class Estudante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Universidade universidade;  // Relacionamento
}
```

### DTOs (Data Transfer Objects)
- Transferem dados entre camadas
- Selecionam quais campos expor
- Sem lógica de persistência

```java
public class EstudanteDTO {
    private Long id;
    private String nome;
    private String email;
    // NÃO inclui universidadeId para segurança
}
```

### Quando usar DTO?

**Use DTO quando:**
- ✅ Precisa expor apenas certos campos
- ✅ Quer ocultar relacionamentos sensíveis
- ✅ Precisa combinar dados de múltiplas entidades
- ✅ Quer validar entrada separadamente

**Use Entidade quando:**
- ✅ A API retorna todos os campos
- ✅ Sem questões de segurança
- ✅ Operação simples CRUD

---

## Conceitos Importantes

### Optional<T>
Evita `NullPointerException`. Representa um valor que pode ou não existir.

```java
Optional<Estudante> estudante = estudanteService.obterEstudante(1L);

if (estudante.isPresent()) {
    System.out.println(estudante.get().getNome());
} else {
    System.out.println("Estudante não encontrado");
}

// Ou usando functional style:
estudante
    .map(Estudante::getNome)
    .ifPresentOrElse(
        nome -> System.out.println(nome),
        () -> System.out.println("Estudante não encontrado")
    );
```

### ResponseEntity<T>
Wrapper para controlar completamente a resposta HTTP.

```java
@GetMapping("/{id}")
public ResponseEntity<Estudante> obter(@PathVariable Long id) {
    Optional<Estudante> estudante = service.obterEstudante(id);
    
    return estudante
        .map(ResponseEntity::ok)                    // HTTP 200
        .orElseGet(() -> ResponseEntity.notFound().build());  // HTTP 404
}

// Personalizando:
return ResponseEntity
    .status(HttpStatus.CREATED)                     // HTTP 201
    .header("Location", "/api/estudantes/" + novoId)
    .body(novoEstudante);
```

### @PathVariable
Extrai parametros da URL.

```java
@GetMapping("/{id}")
public ResponseEntity<Estudante> obter(@PathVariable Long id) {
    // id vem da URL: GET /api/estudantes/123
}
```

### @RequestBody
Mapeia JSON da requisição para objeto Java.

```java
@PostMapping
public ResponseEntity<Estudante> criar(@RequestBody Estudante estudante) {
    // estudante é populado automaticamente do JSON
}
```

### @RequestParam
Extrai parâmetros de query string.

```java
@GetMapping("/buscar")
public List<Estudante> buscar(@RequestParam String nome) {
    // GET /api/estudantes/buscar?nome=João
}
```

---

## Recurso Recomendado

Para aprofundar seu conhecimento:
1. **Spring Boot Official Docs**: https://spring.io/projects/spring-boot
2. **Baeldung Spring Tutorials**: https://www.baeldung.com/spring-tutorial-intro-to-basic-exception-handling
3. **Spring Data JPA Guide**: https://spring.io/projects/spring-data-jpa

---

## Próximas Etapas de Aprendizado

### Nível Básico (Concluído ✅)
- ✅ Entidades JPA
- ✅ Repositórios
- ✅ Services
- ✅ Controllers REST

### Nível Intermediário
- [ ] Testes unitários com JUnit 5
- [ ] Validação com Bean Validation
- [ ] Exception Handling
- [ ] Relacionamentos complexos
- [ ] Paginação e Sorting

### Nível Avançado
- [ ] Spring Security (Autenticação/Autorização)
- [ ] Transações (`@Transactional`)
- [ ] Cache com Redis
- [ ] Message Brokers (RabbitMQ/Kafka)
- [ ] Microserviços

---

**Bom aprendizado! 🚀**
