# 📋 Padrões e Convenções - SISUMONI

Este documento define os padrões de codificação e convenções utilizadas no projeto.

## Estrutura de Pastas

```
sisumoni/
├── src/
│   ├── main/
│   │   ├── java/br/com/sisumoni/
│   │   │   ├── controller/        # REST Controllers
│   │   │   │   ├── EstudanteController.java
│   │   │   │   ├── ProfessorController.java
│   │   │   │   ├── VagaMonitoriaController.java
│   │   │   │   └── AplicacaoController.java
│   │   │   │
│   │   │   ├── model/             # Entidades JPA
│   │   │   │   ├── Estudante.java
│   │   │   │   ├── Professor.java
│   │   │   │   ├── VagaMonitoria.java
│   │   │   │   ├── Aplicacao.java
│   │   │   │   └── enums/
│   │   │   │       ├── StatusAplicacao.java
│   │   │   │       └── StatusVaga.java
│   │   │   │
│   │   │   ├── repository/       # Spring Data Repositories
│   │   │   │   ├── EstudanteRepository.java
│   │   │   │   ├── ProfessorRepository.java
│   │   │   │   ├── VagaMonitoriaRepository.java
│   │   │   │   └── AplicacaoRepository.java
│   │   │   │
│   │   │   ├── service/          # Lógica de Negócio
│   │   │   │   ├── EstudanteService.java
│   │   │   │   ├── ProfessorService.java
│   │   │   │   ├── VagaMonitoriaService.java
│   │   │   │   └── AplicacaoService.java
│   │   │   │
│   │   │   └── SisumoniApplication.java  # Classe Principal
│   │   │
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/
│       └── java/br/com/sisumoni/
│           └── service/
│               └── EstudanteServiceTest.java
│
├── pom.xml
├── README.md
├── SETUP.md
├── APRENDIZADO.md
├── PATTERNS.md (este arquivo)
└── .gitignore
```

## Convenções de Nomenclatura

### Classes
- **Controllers**: `<Entidade>Controller` (Ex: `EstudanteController`)
- **Services**: `<Entidade>Service` (Ex: `EstudanteService`)
- **Repositories**: `<Entidade>Repository` (Ex: `EstudanteRepository`)
- **Entidades**: Nome singular (Ex: `Estudante`, não `Estudantes`)
- **Enums**: PascalCase (Ex: `StatusAplicacao`)

### Métodos
- **Getters**: `get<AtributoQuePega>()` (Ex: `getEstudante()`)
- **Setters**: `set<AtributoQueConfigura>()` (Ex: `setNome()`)
- **Query Methods**: `findBy<Campo>()` (Ex: `findByMatricula()`)
- **Validadores**: `validar<O>()` ou `is<Condicao>()` (Ex: `validarEmail()`, `isAtivo()`)
- **Ações**: verbo + objeto (Ex: `aprovarAplicacao()`, `deletarEstudante()`)

### Variáveis
- camelCase para variáveis locais e atributos (Ex: `nomeEstudante`, `mediaGeral`)
- UPPER_SNAKE_CASE para constantes (Ex: `MAX_VAGAS`, `DEFAULT_PERIODO`)

### Pacotes
```
br.com.sisumoni.controller
br.com.sisumoni.model
br.com.sisumoni.model.enums
br.com.sisumoni.repository
br.com.sisumoni.service
```

## Padrões de Código

### 1. Controllers

**Padrão:**
```java
@RestController
@RequestMapping("/api/<recurso>")
@CrossOrigin(origins = "*")
public class <Entidade>Controller {
    @Autowired
    private <Entidade>Service service;
    
    @GetMapping
    public ResponseEntity<List<<Entidade>>> listarTodos() { }
    
    @GetMapping("/{id}")
    public ResponseEntity<<Entidade>> obter(@PathVariable Long id) { }
    
    @PostMapping
    public ResponseEntity<<Entidade>> criar(@RequestBody <Entidade> entidade) { }
    
    @PutMapping("/{id}")
    public ResponseEntity<<Entidade>> atualizar(@PathVariable Long id, 
                                                @RequestBody <Entidade> entidade) { }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) { }
}
```

**Exemplo Prático:**
```java
@GetMapping("/{id}")
public ResponseEntity<Estudante> obter(@PathVariable Long id) {
    Optional<Estudante> estudante = estudanteService.obterEstudante(id);
    return estudante.map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
}
```

### 2. Services

**Padrão:**
```java
@Service
public class <Entidade>Service {
    @Autowired
    private <Entidade>Repository repository;
    
    public <Entidade> criar(<Entidade> entidade) { }
    public Optional<<Entidade>> obter(Long id) { }
    public List<<Entidade>> listarTodos() { }
    public <Entidade> atualizar(Long id, <Entidade> entidade) { }
    public void deletar(Long id) { }
}
```

### 3. Repositories

**Padrão:**
```java
@Repository
public interface <Entidade>Repository extends JpaRepository<<Entidade>, Long> {
    Optional<<Entidade>> findBy<Campo>(<TipoDoCampo> valor);
    List<<Entidade>> findBy<Condicao>(<Parametros>);
}
```

### 4. Entidades

**Padrão:**
```java
@Entity
@Table(name = "<tabela>")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class <Entidade> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String campo;
    
    @Enumerated(EnumType.STRING)
    private StatusEnum status;
    
    @ManyToOne
    @JoinColumn(name = "<tabela_id>")
    private OutraEntidade entidade;
    
    @Column(updatable = false)
    private LocalDateTime dataCriacao = LocalDateTime.now();
    
    @PreUpdate
    protected void onUpdate() {
        this.dataAtualizacao = LocalDateTime.now();
    }
}
```

## HTTP Status Codes

| Código | Situação | Exemplo |
|--------|----------|---------|
| 200 | OK - Sucesso | GET bem-sucedido |
| 201 | Created - Recurso criado | POST bem-sucedido |
| 204 | No Content - Sucesso sem retorno | DELETE bem-sucedido |
| 400 | Bad Request - Dados inválidos | Email inválido |
| 404 | Not Found - Recurso não existe | Estudante não encontrado |
| 500 | Internal Server Error - Erro do servidor | Exceção não tratada |

## Validação

### Bean Validation (jakarta.validation)

```java
@Entity
public class Estudante {
    @NotNull
    @NotBlank
    private String nome;
    
    @Email
    private String email;
    
    @Min(0) @Max(100)
    private Double mediaGeral;
    
    @Positive
    private Integer periodoAtual;
}
```

## Relacionamentos entre Entidades

### Um para Muitos (1-N)
```java
// Professor tem muitas Vagas
@Entity
public class Professor {
    @OneToMany(mappedBy = "professor")
    private List<VagaMonitoria> vagas;
}

@Entity
public class VagaMonitoria {
    @ManyToOne
    @JoinColumn(name = "professor_id")
    private Professor professor;
}
```

### Muitos para Um (N-1)
```java
// Muitos Aplicações para Um Estudante
@Entity
public class Aplicacao {
    @ManyToOne
    @JoinColumn(name = "estudante_id")
    private Estudante estudante;
}
```

## Tratamento de Erros

### Padrão no Service
```java
public Aplicacao aprovarAplicacao(Long id) {
    Optional<Aplicacao> aplicacao = repository.findById(id);
    if (aplicacao.isPresent()) {
        Aplicacao a = aplicacao.get();
        // lógica
        return repository.save(a);
    }
    throw new IllegalArgumentException("Aplicação não encontrada");
}
```

### Padrão no Controller
```java
@GetMapping("/{id}")
public ResponseEntity<Estudante> obter(@PathVariable Long id) {
    try {
        Optional<Estudante> estudante = service.obter(id);
        return estudante.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
```

## Testes

### Convenção de Nomes
- `<ClasseATestado>Test` (Ex: `EstudanteServiceTest`)
- Localização: `src/test/java/<mesma-estrutura>`

### Padrão de Teste
```java
@ExtendWith(MockitoExtension.class)
class <Classe>Test {
    @Mock
    private Dependency dependency;
    
    @InjectMocks
    private ClasseATestar service;
    
    @BeforeEach
    void setup() {
        // Setup comum
    }
    
    @Test
    void testOperacaoEsperado() {
        // Arrange
        // Act
        // Assert
    }
}
```

## Commits Git

### Padrão
```
<tipo>(<escopo>): <descrição curta>

<descrição longa (opcional)>

<referência a issues (opcional)>
```

### Tipos
- `feat`: Nova feature
- `fix`: Correção de bug
- `docs`: Documentação
- `style`: Formatação
- `refactor`: Refatoração
- `test`: Testes
- `chore`: Manutenção

### Exemplos
```
feat(estudante): adicionar validação de email
fix(vaga): corrigir cálculo de vagas disponíveis
docs(readme): atualizar instruções de setup
```

## Checklist antes de Commit

- [ ] Código segue as convenções
- [ ] Testes passam
- [ ] Sem código comentado desnecessário
- [ ] Sem `System.out.println()` (usar logger)
- [ ] Documentação atualizada
- [ ] Sem merge conflicts

## Recursos Úteis

- [Java Naming Conventions](https://www.oracle.com/java/technologies/javase/codeconventions-136091.html)
- [Spring Boot Best Practices](https://spring.io/guides/gs/spring-boot/)
- [Clean Code in Java](https://en.wikipedia.org/wiki/Robert_C._Martin)

---

**Mantenha o código limpo, legível e bem documentado! 🎯**
