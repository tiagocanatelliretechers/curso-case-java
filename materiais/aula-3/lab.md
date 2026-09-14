# Aula 3 — Guia de Laboratório (aluno)
## Autenticação e Autorização

**Duração:** 80 min · **Ponto de partida:** `aula-3-baseline` (SQLi/validação/upload já corrigidos na Aula 2).
**Ambiente:** JDK 17 + IDE + Postman/navegador.

---

## Lab 3.1 — Explorar o IDOR (10 min)

**Objetivo:** comprovar e documentar o IDOR antes de corrigir (postura ofensiva).

**Passo a passo:**
1. Faça login como `joao@acme.com` / `senha123` (cliente ACME, id 1).
2. Acesse seus pedidos em `/pedidos` e anote os ids que aparecem.
3. Na URL, troque o id para um pedido que **não** é seu (ex.: `/pedidos/2`, da Globex).
4. Confirme que você vê o pedido de outro cliente. Capture evidência (print/URL).
5. Repita na API:
   ```bash
   TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login -H 'Content-Type: application/json' \
     -d '{"email":"joao@acme.com","senha":"senha123"}' | jq -r .token)
   curl -s http://localhost:8080/api/pedidos/2 -H "Authorization: Bearer $TOKEN"
   ```

**Resultado esperado:** acesso indevido comprovado na web e na API.

**Checkpoint (instrutor):**
- [ ] O aluno documentou a evidência do acesso ao pedido alheio (web e API)?

---

## Lab 3.2 — Corrigir o IDOR (25 min)

**Objetivo:** impedir acesso a pedidos de outros clientes, com teste que prova.

**Passo a passo:**
1. Adicione a checagem de **posse** no `PedidoService` (o pedido pertence ao cliente autenticado?).
   Lance `AccessDeniedException` quando não pertencer.
2. Habilite method security (`@EnableMethodSecurity`) e/ou use `@PostAuthorize` no método que retorna o pedido.
3. Ajuste os controllers (web e API) para obter o cliente autenticado e delegar a checagem ao Service.
4. Escreva testes:
   - dono acessa o próprio pedido → 200;
   - cliente acessa pedido de outro → 403.
5. Repita o ataque do Lab 3.1 → deve retornar **403**.

**Resultado esperado:** IDOR eliminado na web e na API; testes passam.

**Checkpoint (instrutor):**
- [ ] A checagem de posse está no Service (não só no template)?
- [ ] `/pedidos/2` como João retorna 403?
- [ ] `/api/pedidos/2` como João retorna 403?
- [ ] Há teste automatizado para os dois casos?

---

## Lab 3.3 — Migrar MD5 → BCrypt (20 min)

**Objetivo:** substituir o hashing inseguro e migrar senhas existentes sem quebrar o login.

**Passo a passo:**
1. Troque o `PasswordEncoder` para `BCryptPasswordEncoder` (bean no `SecurityConfig`).
2. Estratégia de migração (rehash on login):
   - No autenticador, se a senha armazenada for MD5 (formato antigo) e a senha digitada conferir por MD5,
     regrave o hash em bcrypt e prossiga o login.
   - Alternativa: `DelegatingPasswordEncoder` com prefixo `{bcrypt}`/`{MD5}`.
3. Cadastre um novo usuário e confirme que o hash gravado é bcrypt (`$2...`).
4. Faça login com um usuário antigo (MD5) e confirme que o hash **migra** para bcrypt.

**Resultado esperado:** novos hashes em bcrypt; usuários antigos migram no login.

**Checkpoint (instrutor):**
- [ ] Novo cadastro grava bcrypt?
- [ ] Usuário antigo continua logando e tem o hash migrado?
- [ ] Nenhuma senha em texto claro/MD5 permanece após o primeiro login?

---

## Lab 3.4 — Rate limiting no login + política de senha (15 min)

**Objetivo:** dificultar brute force e senhas fracas.

**Passo a passo:**
1. Implemente rate limiting no login (ex.: bucket4j, ou contador por IP/usuário em memória/Redis):
   após N tentativas falhas em janela T, bloquear temporariamente.
2. Aplique política mínima de senha no cadastro (tamanho mínimo; opcional: checagem de senha vazada).
3. Teste: N+1 tentativas erradas → bloqueio temporário; senha curta → rejeitada.

**Resultado esperado:** brute force é freado; senhas fracas são recusadas.

**Checkpoint (instrutor):**
- [ ] Após exceder o limite, novas tentativas são bloqueadas (429/erro)?
- [ ] Senha abaixo do mínimo é rejeitada?

---

## Lab 3.5 — Proteger `/admin` (10 min, opcional se sobrar tempo)

**Objetivo:** exigir `ROLE_ADMIN` no painel administrativo.

**Passo a passo:**
1. Ajuste o `SecurityConfig` (`/admin/**` → `hasRole('ADMIN')`) e/ou `@PreAuthorize("hasRole('ADMIN')")` no controller.
2. Escreva um teste com usuário comum → **403**; com admin → **200**.

**Resultado esperado:** cliente comum recebe 403 em `/admin`; admin acessa.

**Checkpoint (instrutor):**
- [ ] `joao@acme.com` recebe 403 em `/admin`?
- [ ] `admin@portal.com` acessa normalmente?
- [ ] Existe teste automatizado?

---

## Entrega
- Código corrigido (3.2–3.5) + testes (IDOR e admin).
- Evidência do IDOR (Lab 3.1) antes da correção.
