# Aula 4 — Guia de Laboratório (aluno)
## Criptografia + Gestão de Sessão

**Duração:** 40 min (cripto) + 60 min (sessão) · **Ponto de partida:** `aula-4-baseline`.
**Ambiente:** JDK 17 + IDE + Postman/navegador (+ `jq`, `base64` no terminal).

---

## Lab 4.1 — De Base64 para AES-256-GCM (20 min)

**Objetivo:** provar que a "proteção" atual é falsa e implementar cifra real.

**Passo 0 — Prove que Base64 não protege:**
```bash
# valor de dados_pagamento vindo do banco (ex.: via /admin ou H2)
echo "VklTQSA0MTExIDExMTEgMTExMSAxMTExIHZhbCAxMi8yNyBjdnYgMTIz" | base64 -d
```
Confirme que o "dado protegido" volta em texto claro sem nenhuma chave.

**Passo a passo:**
1. Reescreva `CryptoService` para cifrar/decifrar com **AES-256-GCM**:
   - chave de 256 bits vinda de **variável de ambiente** (ex.: `PORTAL_CRYPTO_KEY`, Base64), não do código;
   - **IV/nonce** de 12 bytes gerado com `SecureRandom`, único por operação;
   - persistir `IV || ciphertext || tag` (concatenados) em Base64 (Base64 aqui é só transporte).
2. Gere uma chave para o ambiente:
   ```bash
   export PORTAL_CRYPTO_KEY=$(head -c 32 /dev/urandom | base64)
   ```
3. Regrave os dados de pagamento cifrados (no seed ou via serviço) e confirme que, sem a chave,
   o valor no banco é indecifrável; com a chave, o serviço recupera o original.

**Resultado esperado:** dado em repouso cifrado; sem a chave não se recupera; chave fora do código.

**Checkpoint (instrutor):**
- [ ] A chave vem de variável de ambiente (não hardcoded)?
- [ ] O IV é gerado por `SecureRandom` e é único por operação?
- [ ] O algoritmo é `AES/GCM/NoPadding` com chave de 256 bits?

---

## Lab 4.2 — Corrigir o JWT (20 min)

**Objetivo:** passar a **verificar a assinatura** e demonstrar o ataque antes.

**Passo 0 — Demonstre o ataque `alg:none` (baseline):**
```bash
H=$(printf '{"alg":"none"}' | base64 | tr '+/' '-_' | tr -d '=')
P=$(printf '{"sub":"joao@acme.com","role":"ROLE_ADMIN"}' | base64 | tr '+/' '-_' | tr -d '=')
curl -s http://localhost:8080/api/pedidos -H "Authorization: Bearer $H.$P."
```
Confirme que o token forjado é aceito (role ADMIN sem senha).

**Passo a passo:**
1. Em `JwtService`, troque a leitura sem verificação por `Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token)`.
2. Use uma **chave forte** (≥ 256 bits) vinda de variável de ambiente; remova o segredo do `application.yml`.
3. Reduza a expiração (ex.: 15 min) e valide `exp`.
4. Reinicie e **repita o Passo 0** → o token `alg:none`/forjado deve ser **rejeitado** (401).
5. Confirme que um token legítimo (via `/api/auth/login`) continua funcionando.

**Resultado esperado:** apenas tokens assinados com a chave correta são aceitos; forjados são rejeitados.

**Checkpoint (instrutor):**
- [ ] `alg:none`/token forjado agora retorna 401?
- [ ] A chave saiu do `application.yml` para variável de ambiente?
- [ ] Token legítimo ainda autentica?

---

## Lab 4.3 — Cookie seguro + regeneração de sessão (30 min)

**Objetivo:** blindar o cookie de sessão e prevenir session fixation.

**Passo a passo:**
1. No `application.yml`, ligue os flags do cookie:
   ```yaml
   server.servlet.session.cookie.http-only: true
   server.servlet.session.cookie.secure: true
   server.servlet.session.cookie.same-site: lax
   ```
   (para testar sem HTTPS local, `secure` pode ficar `false` **apenas** em dev — documente.)
2. No `SecurityConfig`, remova `sessionFixation().none()` (ou troque por `changeSessionId()`, o default seguro).
3. Teste: capture o `JSESSIONID` **antes** do login; após o login, o ID deve **mudar**.
4. Inspecione o `Set-Cookie` e confirme `HttpOnly`/`SameSite`.

**Resultado esperado:** cookie com flags seguros; ID de sessão regenerado no login.

**Checkpoint (instrutor):**
- [ ] `Set-Cookie` mostra `HttpOnly` e `SameSite`?
- [ ] O `JSESSIONID` muda após o login (anti fixation)?

---

## Lab 4.4 — Reativar CSRF (30 min)

**Objetivo:** proteger os fluxos baseados em cookie contra CSRF.

**Passo a passo:**
1. No `SecurityConfig`, **remova** o `.csrf(disable)` para os fluxos web (mantendo stateless a API
   com token em header — CSRF não se aplica a ela).
2. Garanta que os formulários Thymeleaf enviem o token CSRF (o Spring injeta em forms com `th:action`).
3. Teste a proteção: monte uma requisição POST forjada **sem** o token (ex.: um HTML externo/curl) para
   um endpoint que altera estado (ex.: `/pedidos`) → deve ser **bloqueada** (403).
4. Confirme que o fluxo normal (com token) continua funcionando.

**Resultado esperado:** requisição forjada sem token CSRF é bloqueada; fluxo legítimo funciona.

**Checkpoint (instrutor):**
- [ ] POST sem token CSRF a um endpoint de escrita retorna 403?
- [ ] Formulário normal (com token) continua funcionando?
- [ ] A API stateless (token em header) segue operando sem CSRF?

---

## Entrega
- `CryptoService` com AES-GCM; `JwtService` com verificação de assinatura.
- Config de cookie + regeneração de sessão; CSRF reativado para a web.
- Evidências: Base64 decodificado (4.1) e token `alg:none` rejeitado (4.2).
