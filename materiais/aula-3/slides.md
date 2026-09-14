# Aula 3 — Slides (outline)
## Secure Coding para Autenticação e Autorização

> 240 min (2 intervalos de 10 min). Módulo único, mais denso — laboratório estendido (80 min).
> Abertura: "Vimos o IDOR no reconhecimento. Hoje vamos explorá-lo de verdade e corrigi-lo."

---

### BLOCO 1 (50 min) — Autenticação segura

**Slide 1 — Abertura e objetivos**
- Autenticação (provar quem é) vs. Autorização (o que pode fazer).
- Hoje: hashing de senha, políticas, MFA, SSO, RBAC/ABAC, IDOR, Spring Security.
- **Nota:** Reforce a distinção authn × authz desde já — é a confusão nº 1.

**Slide 2 — Armazenamento de senha: o que NÃO fazer**
- Texto claro: inaceitável.
- MD5/SHA-1 puro: rápido demais → brute force/rainbow tables triviais.
- Cripto reversível: se dá para descriptografar, o atacante também consegue.
- **Nota:** No Portal, senha é MD5 sem salt — mostre a quebra offline (Aula 1 extraiu o hash via SQLi).

**Slide 3 — Hashing de senha adequado**
- Algoritmos: **bcrypt**, **Argon2**, **PBKDF2** (funções lentas, com custo ajustável).
- **Salt** único por senha (embutido no bcrypt) + **work factor** (custo) calibrado.
- Nunca implemente cripto própria.
- **Nota:** bcrypt inclui salt no próprio hash (`$2a$10$...`). Work factor ≥ 10; recalibrar com hardware.

**Slide 4 — Política de senha moderna (NIST 800-63B)**
- Comprimento > complexidade forçada; permitir frases longas.
- Verificar contra listas de senhas vazadas.
- **Rate limiting** em vez de expiração/rotação forçada e bloqueio permanente.
- Não exigir regras que levem a padrões previsíveis.
- **Nota:** Desmistifique "trocar senha a cada 30 dias" — NIST recomenda o contrário.

**Slide 5 — MFA e step-up authentication**
- MFA (algo que sabe + tem + é): TOTP, WebAuthn/passkeys, push.
- Step-up: exigir 2º fator só em operações sensíveis (checkout, mudança de dados).
- **Nota:** MFA é a defesa mais eficaz contra account takeover por senha vazada.

**Slide 6 — Gestão de credenciais e segredos**
- Nunca hardcode senha/chave/token no código ou repositório.
- Usar secrets manager / variáveis de ambiente / vault.
- Rotação e escopo mínimo.
- **Nota:** No Portal, `portal.jwt.secret` está no `application.yml` e vaza no `/actuator/env` — ligue com Aula 4.

**Slide 7 — Fluxo "esqueci minha senha" seguro**
- Token de uso único, aleatório (CSPRNG), com expiração curta.
- Resposta **genérica** (não revelar se o e-mail existe) → anti user enumeration.
- Invalidar sessões após redefinição.
- **Nota:** No Portal a mensagem revela se o e-mail existe (A07). Discuta a correção.

**Slide 8 — SSO: SAML e OIDC/OAuth2**
- SAML: XML, corporativo/legado. OIDC (sobre OAuth2): moderno, JSON/JWT.
- OAuth2 = autorização/delegação; OIDC adiciona autenticação (id_token).
- Riscos comuns: não validar assinatura, `audience`/`issuer`, `nonce`, redirect_uri.
- **Nota:** Regra de ouro: valide SEMPRE assinatura, issuer e audience do token.

---

### BLOCO 2 (40 min) — Autorização e controle de acesso

**Slide 9 — Modelos de autorização**
- **RBAC** (por papel), **ABAC** (por atributos/contexto), **ReBAC** (por relacionamento — "é dono de").
- Quando usar: RBAC simples/estável; ABAC contexto rico; ReBAC posse/compartilhamento.
- **Nota:** O Portal precisa de RBAC (admin/user) + ReBAC (cliente é dono do pedido).

**Slide 10 — Broken Access Control na prática**
- **IDOR**: referência direta a objeto sem checar posse.
- Escalada **vertical** (user→admin) vs **horizontal** (user→outro user).
- **Forced browsing**: acessar URLs não linkadas.
- **Nota:** No Portal temos os três: `/pedidos/{id}` (horizontal), `/admin` (vertical), forced browsing de endpoints.

**Slide 11 — Autorização em todas as camadas**
- Esconder botão no frontend **não** é controle.
- Verificar no servidor, no ponto de acesso ao recurso.
- **Complete mediation**: cada acesso é verificado.
- **Nota:** Demonstre: remover o link de `/admin` não impede o acesso direto.

**Slide 12 — Object-level authorization (anti-IDOR)**
- Além de "está autenticado?" e "tem o papel?", perguntar: "este objeto é dele?".
- Essencial em APIs REST (a causa nº 1 de vazamento em APIs).
- Implementar no Service (dono do recurso) e/ou `@PostAuthorize`.
- **Nota:** Este é o coração do Lab 3.2.

**Slide 13 — Erros comuns de autorização**
- Confiar em campo do cliente (role no JWT sem verificar assinatura).
- Checar autorização só em algumas rotas.
- Cachear decisão de acesso indevidamente.
- **Nota:** Ligue o "role no JWT" com a Aula 4 (JWT sem verificação).

---

### INTERVALO (10 min)

---

### BLOCO 3 (30 min) — Spring Security aplicado

**Slide 14 — Arquitetura do Spring Security**
- Cadeia de filtros (`SecurityFilterChain`); `Authentication`/`SecurityContext`.
- `UserDetailsService` (carrega usuário) + `PasswordEncoder` (verifica senha).
- **Nota:** Mostre o fluxo de uma requisição autenticada passando pelos filtros.

**Slide 15 — Configuração segura (form login + method security)**
- `authorizeHttpRequests` com regras explícitas; `hasRole`/`hasAuthority`.
- Habilitar method security: `@EnableMethodSecurity`.
- `@PreAuthorize`/`@PostAuthorize` com SpEL.
  ```java
  @PreAuthorize("hasRole('ADMIN')")
  @PostAuthorize("returnObject.clienteId == principal.clienteId")
  ```
- **Nota:** No baseline, `/admin` está como `authenticated()` — deveria ser `hasRole('ADMIN')`.

**Slide 16 — PasswordEncoder e migração**
- `BCryptPasswordEncoder` (ou `DelegatingPasswordEncoder` para múltiplos formatos).
- Migração de MD5 → bcrypt: reidratar no próximo login bem-sucedido.
- **Nota:** Estratégia de migração é o Lab 3.3.

**Slide 17 — Checagem de posse de recurso**
- No Service: `if(!pedido.getClienteId().equals(clienteAtual)) throw AccessDenied`.
- Ou `@PostAuthorize` avaliando o objeto retornado.
- **Nota:** Prefira negar cedo no Service; `@PostAuthorize` complementa.

---

### BLOCO 4 (80 min) — Laboratório

**Slide 18 — Laboratório da Aula 3**
- Lab 3.1: explorar o IDOR (evidência).
- Lab 3.2: corrigir IDOR (Service + @PostAuthorize) + teste.
- Lab 3.3: migrar MD5 → BCrypt (com rehash no login).
- Lab 3.4: rate limiting no login + política de senha.
- Lab 3.5: `@PreAuthorize`/hasRole no `/admin` + teste 403.
- **Nota:** Distribua `lab.md`. Priorize 3.1→3.2→3.3.

---

### INTERVALO (10 min)

---

### BLOCO 5 (20 min) — Debrief + Quiz

**Slide 19 — Debrief**
- Rodar o ataque IDOR contra a versão corrigida (deve dar 403).
- Mostrar hash bcrypt no banco após rehash.
- **Nota:** Conecte com métricas: quantas rotas ainda faltam proteger?

**Slide 20 — Quiz + ponte para a Aula 4**
- Quiz (`quiz.md`).
- Aula 4: criptografia e sessão — inclui o JWT sem verificação e os cookies inseguros.
- **Nota:** Aula 4 parte de `aula-4-baseline` (IDOR/MD5/rate limiting já corrigidos).
