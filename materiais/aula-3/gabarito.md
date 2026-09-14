# Aula 3 — Gabarito / Solução comentada (SOMENTE INSTRUTOR)

Corresponde à branch `aula-3-hardened`.

## Lab 3.1 — Exploração do IDOR
Evidência esperada: logado como João (cliente 1), `/pedidos/2` mostra o pedido da Globex;
`GET /api/pedidos/2` com o token de João retorna o JSON do pedido 2. Ambos deveriam ser 403.

## Lab 3.2 — Correção do IDOR

**Service com checagem de posse:**
```java
public Pedido porIdDoCliente(Long id, Long clienteId) {
    Pedido pedido = pedidoRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    if (!pedido.getClienteId().equals(clienteId)) {
        throw new AccessDeniedException("Pedido nao pertence ao cliente autenticado");
    }
    return pedido;
}
```
> Boas práticas: retornar 404 (não 403) para não confirmar existência do recurso a estranhos é
> uma opção; aqui usamos 403 por clareza didática. Alinhe com o time.

**Controller web:**
```java
@GetMapping("/{id}")
public String ver(@PathVariable Long id, Principal principal, Model model) {
    Long clienteId = clienteIdDe(principal);
    model.addAttribute("pedido", pedidoService.porIdDoCliente(id, clienteId));
    return "pedidos/detalhe";
}
```

**API com @PostAuthorize (alternativa/complemento):**
```java
@EnableMethodSecurity   // na classe de config
...
@PostAuthorize("returnObject.body == null or returnObject.body.clienteId == @usuarioService.clienteIdDoEmail(authentication.name)")
@GetMapping("/{id}")
public ResponseEntity<Pedido> porId(@PathVariable Long id) { ... }
```
> Mais simples e recomendado: delegar ao Service (`porIdDoCliente`) tanto na web quanto na API,
> mantendo a regra em um único lugar.

**Tratamento do 403:** `AccessDeniedException` → 403 (Spring Security). Para web, um
`@ControllerAdvice`/página de erro amigável (ligação com Aula 5).

**Teste (API):**
```java
@SpringBootTest @AutoConfigureMockMvc
class IdorTest {
    @Autowired MockMvc mvc;
    @Test @WithUserDetails("joao@acme.com")
    void naoAcessaPedidoDeOutroCliente() throws Exception {
        mvc.perform(get("/api/pedidos/2")).andExpect(status().isForbidden());
    }
    @Test @WithUserDetails("joao@acme.com")
    void acessaProprioPedido() throws Exception {
        mvc.perform(get("/api/pedidos/1")).andExpect(status().isOk());
    }
}
```

## Lab 3.3 — MD5 → BCrypt com migração

**Encoder:**
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(); // work factor default 10
}
```

**Migração "rehash on login"** — abordagem via `AuthenticationProvider`/serviço de login:
```java
public boolean autenticarEMigrar(String email, String senhaDigitada) {
    Usuario u = usuarioRepository.findByEmail(email).orElse(null);
    if (u == null) return false;
    String hash = u.getSenha();
    boolean ok;
    if (hash.startsWith("$2")) {                     // ja e bcrypt
        ok = bcrypt.matches(senhaDigitada, hash);
    } else {                                          // formato antigo MD5
        ok = md5(senhaDigitada).equals(hash);
        if (ok) {                                     // migra no login bem-sucedido
            u.setSenha(bcrypt.encode(senhaDigitada));
            usuarioRepository.save(u);
        }
    }
    return ok;
}
```
> Alternativa idiomática: `DelegatingPasswordEncoder` com hashes prefixados
> (`{bcrypt}$2a$...`, `{MD5}...`) e `setDefaultPasswordEncoderForMatches`. Para o baseline (hashes MD5
> sem prefixo) a abordagem acima é mais direta.

**Cadastro** passa a gravar `bcrypt.encode(senha)`.

## Lab 3.4 — Rate limiting + política de senha

**Rate limiting simples (bucket4j) no login:**
```java
// dependencia: com.bucket4j:bucket4j-core
private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

private Bucket bucketPara(String chave) {
    return buckets.computeIfAbsent(chave, k -> Bucket.builder()
        .addLimit(Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1))))
        .build());
}

// no endpoint de login (web filter ou controller da API):
if (!bucketPara(ipOuEmail).tryConsume(1)) {
    return ResponseEntity.status(429).body(Map.of("erro", "muitas tentativas"));
}
```
> Em produção, use um store distribuído (Redis) para o rate limiting valer entre instâncias.

**Política de senha:** `@Size(min=10)` no cadastro; opcional: checar contra lista de senhas vazadas
(k-anonymity / Have I Been Pwned) antes de aceitar.

**Anti user enumeration** (ligação Aula 1/3): mensagem única para login e "esqueci senha", tanto para
e-mail existente quanto inexistente.

## Lab 3.5 — Proteger `/admin`

**Config:**
```java
.requestMatchers("/admin/**").hasRole("ADMIN")
```
e/ou:
```java
@PreAuthorize("hasRole('ADMIN')")
@GetMapping public String dashboard(Model model) { ... }
```
> `hasRole('ADMIN')` casa com a authority `ROLE_ADMIN`.

**Teste:**
```java
@Test @WithMockUser(roles = "USER")
void usuarioComumRecebe403NoAdmin() throws Exception {
    mvc.perform(get("/admin")).andExpect(status().isForbidden());
}
@Test @WithMockUser(roles = "ADMIN")
void adminAcessa() throws Exception {
    mvc.perform(get("/admin")).andExpect(status().isOk());
}
```

## Quiz — ver `quiz.md`.
