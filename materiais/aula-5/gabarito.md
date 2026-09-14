# Aula 5 — Gabarito / Solução comentada (SOMENTE INSTRUTOR)

Corresponde à branch `aula-5-hardened`.

## Lab 5.1 — Error Handling

**Handler global:**
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,String>> handle(Exception ex) {
        String traceId = UUID.randomUUID().toString();
        log.error("Erro [{}]", traceId, ex);                 // detalhe SO no servidor
        return ResponseEntity.status(500)
            .body(Map.of("erro", "Ocorreu um erro. Ref: " + traceId));  // generico ao cliente
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String,String>> denied(AccessDeniedException ex) {
        return ResponseEntity.status(403).body(Map.of("erro", "Acesso negado"));
    }
}
```
Para telas web, um `@ExceptionHandler` que retorna a view `error` genérica; e páginas
`src/main/resources/templates/error/4xx.html` e `5xx.html`.

**`application.yml`:**
```yaml
server:
  error:
    include-message: never
    include-stacktrace: never
    include-exception: false
    include-binding-errors: never
```

**Log injection corrigido (`ImportController`):**
```java
private static String sanitizar(String s) {
    return s == null ? "" : s.replaceAll("[\\r\\n\\t]", "_");
}
...
log.info("Importacao de catalogo solicitada para URL: {}", sanitizar(url));
```
> Placeholders `{}` + neutralização de CR/LF impedem forjar linhas de log.
> **Não** logar dados sensíveis (senha/token/cartão) em nenhum nível.

## Lab 5.2 — SAST

**Findings típicos que o SonarQube/Semgrep aponta no baseline** (muitos já corrigidos até a Aula 4):
- `java:S2077`/hotspots de SQL dinâmico (SQLi) — corrigido na Aula 2 (não deve mais aparecer).
- `java:S4790` uso de hashing fraco (MD5) — corrigido na Aula 3.
- `java:S6437`/hardcoded secret (`portal.jwt.secret`) — corrigido na Aula 4.
- `java:S5122` CORS/《config》, headers de segurança ausentes.
- `java:S2245` uso de PRNG inseguro (`Random`) — verificar se restou algum.
- Hotspots de CSRF disabled — corrigido na Aula 4.

**Triagem esperada (exemplo de tabela do aluno):**

| Regra | Severidade | VP/FP | Ação |
|-------|-----------|-------|------|
| S4790 (MD5) | Alta | VP (residual em teste?) | Corrigir/remover |
| S6437 (secret) | Alta | VP | Mover p/ env |
| S5122 (headers) | Média | VP | Adicionar headers de segurança |
| S2245 (Random) | Média | VP/FP | SecureRandom onde for segurança |
| Hotspot CSRF | — | Revisar | Confirmar habilitado |

**Correção de 1 alto (exemplo — headers de segurança):**
```java
.headers(h -> h
    .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
    .frameOptions(fo -> fo.sameOrigin())
    .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true)))
```
Re-scan → o finding desaparece.

## Lab 5.3 — DAST

**Alertas típicos do OWASP ZAP no baseline (aula-5):**
- Ausência de `Content-Security-Policy`, `X-Content-Type-Options`, `Strict-Transport-Security`.
- Cookies sem flags adequadas (dependendo do estado — corrigido na Aula 4).
- Exposição de `/actuator/**` (ainda presente — será corrigido na Aula 6).
- Divulgação de informação em mensagens de erro (se o Lab 5.1 não foi aplicado à branch DAST).

**Validação manual de 1 finding (exemplo — Actuator exposto):**
```bash
curl -s -o /dev/null -w '%{http_code}\n' http://localhost:8080/actuator/env   # 200 => exposto
```
Confirma o alerta do ZAP; a correção vem na Aula 6 (Lab 6.1).

**SAST × DAST — pontos de discussão:**
- SAST achou o hardcoded secret e o MD5 (código); DAST achou headers ausentes e Actuator exposto (runtime/config).
- Nenhum sozinho cobre tudo → processo usa ambos + SCA (Aula 6).

## Quiz — ver `quiz.md`.
