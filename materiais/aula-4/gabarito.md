# Aula 4 — Gabarito / Solução comentada (SOMENTE INSTRUTOR)

Corresponde à branch `aula-4-hardened`.

## Lab 4.1 — AES-256-GCM

**`CryptoService` corrigido:**
```java
@Service
public class CryptoService {
    private static final int IV_LEN = 12;      // 96 bits (recomendado p/ GCM)
    private static final int TAG_BITS = 128;
    private final SecretKey key;
    private final SecureRandom random = new SecureRandom();

    public CryptoService(@Value("${PORTAL_CRYPTO_KEY}") String base64Key) {
        byte[] k = Base64.getDecoder().decode(base64Key);   // 32 bytes = AES-256
        this.key = new SecretKeySpec(k, "AES");
    }

    public String proteger(String textoClaro) throws Exception {
        if (textoClaro == null) return null;
        byte[] iv = new byte[IV_LEN];
        random.nextBytes(iv);                               // IV unico por operacao
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
        byte[] ct = c.doFinal(textoClaro.getBytes(StandardCharsets.UTF_8));
        byte[] out = ByteBuffer.allocate(iv.length + ct.length).put(iv).put(ct).array();
        return Base64.getEncoder().encodeToString(out);     // Base64 = so transporte
    }

    public String revelar(String protegido) throws Exception {
        if (protegido == null) return null;
        byte[] all = Base64.getDecoder().decode(protegido);
        byte[] iv = Arrays.copyOfRange(all, 0, IV_LEN);
        byte[] ct = Arrays.copyOfRange(all, IV_LEN, all.length);
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        c.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
        return new String(c.doFinal(ct), StandardCharsets.UTF_8); // tag verificada aqui
    }
}
```
**Pontos didáticos:**
- Chave de 256 bits **fora do código** (env `PORTAL_CRYPTO_KEY`, Base64 de 32 bytes).
- IV aleatório por operação com `SecureRandom`; **nunca** reutilizar IV com a mesma chave.
- GCM já autentica (a tag é verificada no `doFinal` do decrypt → adultério é detectado).
- Base64 aqui transporta o **ciphertext**, que já está cifrado — diferente do baseline.

> Gere a chave: `head -c 32 /dev/urandom | base64`. Em produção: KMS/Vault.

## Lab 4.2 — JWT com verificação de assinatura

**`JwtService` corrigido:**
```java
public JwtService(@Value("${PORTAL_JWT_SECRET}") String secret,
                  @Value("${portal.jwt.expiration-ms:900000}") long expirationMs) {
    byte[] bytes = Base64.getDecoder().decode(secret);      // >= 32 bytes
    this.key = Keys.hmacShaKeyFor(bytes);
    this.expirationMs = expirationMs;                        // 15 min
}

public Jws<Claims> validar(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token);   // lanca excecao se assinatura/alg/exp invalidos
}

public String subject(String token) { return validar(token).getBody().getSubject(); }
public String role(String token)    { return validar(token).getBody().get("role", String.class); }
```

**Filtro** passa a usar `validar(...)`; em exceção → 401 e **não** autentica:
```java
try {
    Jws<Claims> jws = jwtService.validar(token);
    var auth = new UsernamePasswordAuthenticationToken(
        jws.getBody().getSubject(), null,
        List.of(new SimpleGrantedAuthority(jws.getBody().get("role", String.class))));
    SecurityContextHolder.getContext().setAuthentication(auth);
} catch (JwtException e) {
    // token invalido/forjado/expirado -> segue sem autenticar (=> 401 nas rotas protegidas)
}
```
**Por que barra `alg:none`:** `parseClaimsJws` exige uma assinatura válida (JWS) com a chave e o
algoritmo esperados; tokens sem assinatura (`alg:none` = JWT unsecured) ou com assinatura errada
lançam exceção. Remover `portal.jwt.secret` do `application.yml`; usar env `PORTAL_JWT_SECRET`.

## Lab 4.3 — Cookie + regeneração de sessão

**`application.yml`:**
```yaml
server:
  servlet:
    session:
      cookie:
        http-only: true
        secure: true       # em dev sem HTTPS pode ficar false (documentar!)
        same-site: lax
      timeout: 15m
```
**`SecurityConfig`:** remover `sessionFixation().none()` (default = `changeSessionId()`):
```java
.sessionManagement(s -> s
    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
    .sessionFixation(f -> f.changeSessionId())   // regenera ID no login
    .maximumSessions(1))                          // concurrent session control (opcional)
```
**Verificação:** `JSESSIONID` antes do login ≠ depois do login; `Set-Cookie` com `HttpOnly`/`SameSite`.

## Lab 4.4 — CSRF

**`SecurityConfig`:** deixar o CSRF **habilitado** (default) para a web; isentar só a API stateless:
```java
.csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
// (remover o AbstractHttpConfigurer::disable do baseline)
```
- Formulários Thymeleaf com `th:action` recebem o `_csrf` automaticamente.
- Reabilitar `frameOptions` também (o H2 console será desabilitado na Aula 6).

**Teste (CSRF ativo):**
```java
@Test @WithMockUser
void postSemTokenCsrfEhBloqueado() throws Exception {
    mvc.perform(post("/pedidos").param("produtoId","1"))
       .andExpect(status().isForbidden());
}
@Test @WithMockUser
void postComTokenCsrfFunciona() throws Exception {
    mvc.perform(post("/pedidos").param("produtoId","1").with(csrf()))
       .andExpect(status().is3xxRedirection());
}
```

## Quiz — ver `quiz.md`.
