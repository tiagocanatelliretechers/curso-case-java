# Aula 3 — Guia de Laboratório (aluno)
## Autenticação e Autorização

**Duração:** ~80 min · **Ponto de partida:** `aula-3-baseline` (SQLi/validação/upload já corrigidos na Aula 2).
**Ambiente:** JDK 17 + IDE + navegador (+ `curl`/Postman opcional).
Subir o app: `./scripts/start.sh --lab 3 --no-docker` (ou `.\scripts\start.ps1 -Lab 3 -NoDocker`).

---

## Como usar este guia (leia antes de começar)

O objetivo **não é só concluir os passos** — é entender *por que* cada falha existe e *por que* a correção funciona. Se ao final de um exercício você não consegue explicar isso com suas palavras, **pare e releia o bloco "Conceito em 1 minuto" ou chame o instrutor** antes de avançar. Um lab bem-feito é aquele que você conseguiria reexplicar para um colega.

Cada exercício segue sempre a mesma estrutura:

| Seção | Para que serve |
|-------|----------------|
| **Por que fazemos isto** | O contexto e o risco real — liga o exercício ao mundo. |
| **Conceito em 1 minuto** | O fundamento teórico, direto ao ponto. |
| **Passo a passo** | A ação, com **o que observar** e **o que isso significa** em cada passo. |
| **Ponto de entendimento** | Uma pergunta que você deve saber responder (testa compreensão, não entrega). |
| **Se der errado** | Erros comuns e como sair deles sem travar. |
| **Conecte com a teoria** | Onde isto apareceu nos slides. |

> Dica de ritmo: reserve os 2 primeiros minutos de cada lab só para ler "Por que fazemos isto" e "Conceito em 1 minuto". Não comece a digitar antes disso.

---

## Mapa: conceito → laboratório

Este mapa mostra como a teoria da aula vira prática. Use-o para não se perder.

| Conceito (slides) | Vulnerabilidade no Portal | Lab | O que você vai provar |
|-------------------|---------------------------|-----|-----------------------|
| Autorização por objeto (ReBAC/posse) | IDOR em `/pedidos/{id}` | 3.1 → 3.2 | Que dá para ver o pedido de outro cliente — e como bloquear. |
| Hashing de senha (MD5 × BCrypt) | Senhas em MD5 sem sal | 3.3 | Que MD5 é quebrável e como migrar sem quebrar o login. |
| Brute force / política de senha | Login sem limite de tentativas | 3.4 | Que dá para tentar infinitas senhas — e como frear. |
| RBAC por papel | `/admin` só exige "estar logado" | 3.5 | Que um cliente comum entra no admin — e como exigir o papel. |

## Objetivos de aprendizagem

Ao final desta aula você deve conseguir **explicar**:
1. A diferença entre "estar autenticado" e "estar autorizado", com um exemplo do Portal.
2. Por que checar `findById(id)` sem comparar o dono é um IDOR — e onde a posse deve ser verificada.
3. Por que BCrypt é adequado para senha e MD5 não, citando "sal" e "work factor".
4. Como migrar hashes legados sem forçar todos a redefinir a senha.
5. Por que `hasRole('ADMIN')` é diferente de `authenticated()`.

---

## Lab 3.1 — Explorar o IDOR (10 min)

**Por que fazemos isto:** antes de corrigir, você precisa *ver a falha com os próprios olhos*. IDOR (Insecure Direct Object Reference) é a manifestação mais comum de Broken Access Control (OWASP **A01**, a categoria nº 1). Comprovar o problema com evidência é o que um pentester faz — e é o que justifica a correção.

**Conceito em 1 minuto:** o Portal identifica um pedido por um número (`/pedidos/2`). O código busca esse pedido **só pelo id** e o devolve, sem perguntar *"este pedido é de quem está pedindo?"*. Como o id é sequencial e aparece na URL, basta trocá-lo para ler dados de outro cliente. A aplicação confundiu **autenticado** (João tem login) com **autorizado** (João pode ver *aquele* pedido).

**Passo a passo:**

1. Faça login como `joao@acme.com` / `senha123` (cliente ACME, id 1).
   - *O que observar:* você cai em "Meus Pedidos".
   - *O que isso significa:* você está **autenticado** — o crachá está validado.

2. Acesse `/pedidos` e anote os ids que aparecem (são os pedidos da ACME).
   - *O que observar:* todos pertencem a você.

3. Na URL, troque o id para um que **não** é seu, ex.: `/pedidos/2` (da Globex).
   - *O que observar:* você **vê o pedido de outro cliente**.
   - *O que isso significa:* a autorização por objeto **não existe** — o servidor não checou a posse. Isso é o IDOR.

4. Capture a evidência (print da tela + a URL). Essa é a "prova" antes da correção.

5. Repita na **API** (mostra que o problema não é só da tela):
   ```bash
   TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login -H 'Content-Type: application/json' \
     -d '{"email":"joao@acme.com","senha":"senha123"}' | jq -r .token)
   curl -s http://localhost:8080/api/pedidos/2 -H "Authorization: Bearer $TOKEN"
   ```
   - *O que observar:* a API retorna o JSON do pedido 2 (que não é do João).
   - *O que isso significa:* a falha está na **regra de acesso ao recurso**, não na interface. Esconder na tela não resolveria.

**Ponto de entendimento:** *Se removêssemos o link para `/pedidos/2` da tela, o ataque ainda funcionaria?*
> Resposta esperada: **sim** — o acesso é feito direto pela URL/API; a UI não é controle de acesso.

**Se der errado:**
- `jq: command not found` → instale o `jq`, ou remova o `| jq -r .token` e copie o token manualmente do JSON.
- Login não entra / volta pro login → confira que subiu com `-NoDocker` (H2) e use as contas de teste da tela.
- `/pedidos/2` dá 404 → tente outro id (1..5); confirme no console H2 quais pedidos existem.

**Conecte com a teoria:** slides "Broken Access Control", "IDOR — Antes" e "Object-level authorization".

---

## Lab 3.2 — Corrigir o IDOR (25 min)

**Por que fazemos isto:** encontrar a falha é metade; a outra metade é **corrigir no lugar certo**. A tentação é "esconder na tela" — mas o controle tem que estar no **servidor, no ponto de acesso ao recurso**, e valer para web *e* API de uma vez só.

**Conceito em 1 minuto:** autorização por objeto = comparar o **dono do recurso** com o **usuário autenticado**. A regra `pedido.clienteId == clienteLogado` é a pergunta que faltava. Colocando essa checagem **no Service**, tanto a web quanto a API passam por ela — uma única fonte de verdade, impossível de "esquecer" em uma das pontas.

**Passo a passo:**

1. Adicione a checagem de **posse** no `PedidoService` (a regra num só lugar):
   ```java
   public Pedido porIdDoCliente(Long id, Long clienteId) {
       Pedido pedido = pedidoRepository.findById(id)
           .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
       if (!pedido.getClienteId().equals(clienteId))
           throw new AccessDeniedException("Pedido nao pertence ao cliente autenticado");
       return pedido;
   }
   ```
   - *O que observar:* o método agora **precisa** do `clienteId` de quem pede.
   - *O que isso significa:* é impossível buscar um pedido sem informar (e comparar) o dono.

2. Ajuste os **controllers** (web e API) para obter o cliente autenticado e **delegar ao Service** (não repita a regra):
   ```java
   @GetMapping("/{id}")
   public String ver(@PathVariable Long id, Principal principal, Model model) {
       Long clienteId = clienteIdDe(principal);          // do usuário logado
       model.addAttribute("pedido", pedidoService.porIdDoCliente(id, clienteId));
       return "pedidos/detalhe";
   }
   ```
   - *O que isso significa:* o controller confia no Service para autorizar — a regra não se espalha.

3. (Opcional/reforço) Habilite `@EnableMethodSecurity` e anote a API com `@PostAuthorize` para validar o objeto retornado:
   ```java
   @PostAuthorize("returnObject.body == null or returnObject.body.clienteId == @usuarioService.clienteIdDoEmail(authentication.name)")
   ```
   - *O que isso significa:* uma segunda barreira, agora a nível de método. Prefira concentrar a regra no Service e usar isto como reforço.

4. Garanta que `AccessDeniedException` vire **403** (é o padrão do Spring Security).

5. Escreva os testes que **provam** a correção:
   ```java
   @Test @WithUserDetails("joao@acme.com")
   void naoAcessaPedidoDeOutro() throws Exception {
       mvc.perform(get("/api/pedidos/2")).andExpect(status().isForbidden()); // 403
   }
   @Test @WithUserDetails("joao@acme.com")
   void acessaProprioPedido() throws Exception {
       mvc.perform(get("/api/pedidos/1")).andExpect(status().isOk());        // 200
   }
   ```

6. Repita o ataque do Lab 3.1.
   - *O que observar:* `/pedidos/2` e `/api/pedidos/2` agora devolvem **403**; `/pedidos/1` (seu) continua **200**.
   - *O que isso significa:* o IDOR foi fechado sem quebrar o acesso legítimo.

**Ponto de entendimento:** *Por que colocamos a checagem no Service e não no controller da API?*
> Resposta esperada: para ter **uma única regra** que vale para web e API; duplicar no controller convida ao erro de esquecer uma das pontas.

**Nota de projeto (decisão real):** alguns times retornam **404** em vez de 403 para não confirmar que o recurso existe. Aqui usamos 403 por clareza didática — o importante é *negar*. Alinhe com o seu time.

**Se der errado:**
- Continua 200 no pedido alheio → a checagem ficou só no template/controller, não no Service; ou o controller não está chamando `porIdDoCliente`.
- 403 até no próprio pedido → o `clienteId` do usuário logado está vindo errado (confira `clienteIdDe(principal)`).
- Teste não compila → falta `@WithUserDetails`/config do MockMvc; veja o import.

**Conecte com a teoria:** slides "IDOR — Depois", "Object-level authorization" e "Posse no Service".

---

## Lab 3.3 — Migrar MD5 → BCrypt (20 min)

**Por que fazemos isto:** quando o banco vazar (e um dia vaza), a forma como a senha foi guardada decide o estrago. MD5 é rápido demais e sem sal — quebrável com tabelas prontas. Trocar o algoritmo **sem** forçar todo mundo a redefinir a senha é um problema real de produção.

**Conceito em 1 minuto:** BCrypt é **lento de propósito** (work factor ajustável) e usa um **sal único por senha** — duas senhas iguais viram hashes diferentes, o que anula rainbow tables. A migração "rehash on login": no primeiro login bem-sucedido de um usuário antigo, você confere pelo MD5 e **regrava** o hash em BCrypt. Assim os usuários migram sozinhos, sem atrito.

**Passo a passo:**

1. Troque o `PasswordEncoder` no `SecurityConfig`:
   ```java
   @Bean
   public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); } // custo padrão 10
   ```
   - *O que isso significa:* novos hashes já saem em BCrypt (`$2a$...`).

2. Implemente a migração no fluxo de login (rehash on login):
   ```java
   String hash = u.getSenha();
   if (hash.startsWith("$2")) ok = bcrypt.matches(senha, hash);   // já é bcrypt
   else {                                                          // formato antigo MD5
       ok = md5(senha).equals(hash);
       if (ok) { u.setSenha(bcrypt.encode(senha)); repo.save(u); }// migra no login
   }
   ```
   - *O que observar:* o `if` distingue o formato do hash pelo prefixo.
   - *O que isso significa:* usuário antigo entra normalmente **e** sai do banco já como BCrypt.

3. Cadastre um usuário novo e confira o hash no console H2 (`/h2-console`, `SELECT email, senha FROM usuario`).
   - *O que observar:* começa com `$2a$` (BCrypt).

4. Faça login com um usuário **antigo** (MD5) e recarregue o `SELECT`.
   - *O que observar:* o hash daquele usuário **mudou** de 32 hex (MD5) para `$2a$...` (BCrypt).
   - *O que isso significa:* a migração aconteceu no login, sem redefinição de senha.

**Ponto de entendimento:** *Por que dois usuários com a senha "senha123" tinham o mesmo hash em MD5, mas passam a ter hashes diferentes em BCrypt?*
> Resposta esperada: BCrypt usa um **sal único por senha** embutido no hash; MD5 puro não usa sal.

**Se der errado:**
- Usuário antigo não loga → a comparação MD5 do ramo legado não está sendo feita antes de migrar.
- Hash não muda → faltou `repo.save(u)` após o `encode`.
- Tudo virou 401 → confira que o `matches` do BCrypt roda para hashes que já começam com `$2`.

**Conecte com a teoria:** slides "MD5 × BCrypt", "Anatomia do hash BCrypt" e "Migração rehash on login".

---

## Lab 3.4 — Rate limiting no login + política de senha (15 min)

**Por que fazemos isto:** hashing forte protege a senha *guardada*; mas sem limite de tentativas, um atacante simplesmente testa milhares de senhas no login. Rate limiting é a "catraca que trava" após algumas tentativas.

**Conceito em 1 minuto:** um *bucket* de N fichas por janela de tempo (ex.: 5/min por IP). Cada tentativa consome uma ficha; esgotou, bloqueia temporariamente com **HTTP 429**. Some a isso uma política mínima de senha (priorize **comprimento**, não símbolos) para barrar as senhas óbvias.

**Passo a passo:**

1. Implemente o rate limiting no login (ex.: bucket4j):
   ```java
   Bucket b = buckets.computeIfAbsent(ip, k -> Bucket.builder()
       .addLimit(Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1)))).build());
   if (!b.tryConsume(1)) return ResponseEntity.status(429).body("muitas tentativas");
   ```
   - *O que isso significa:* a 6ª tentativa dentro do minuto é barrada.

2. Aplique política mínima de senha no cadastro (ex.: `@Size(min = 10)`).

3. Teste o bloqueio: erre a senha 5 vezes seguidas e tente a 6ª.
   - *O que observar:* a 6ª retorna **429** (ou erro de "muitas tentativas").
   - *O que isso significa:* brute force fica inviável no tempo.

4. Teste a política: tente cadastrar com senha curta (ex.: `123`).
   - *O que observar:* o cadastro é **recusado**.

**Ponto de entendimento:** *Por que limitamos por IP/usuário e não simplesmente bloqueamos a conta após 3 erros?*
> Resposta esperada: bloqueio permanente vira **negação de serviço** (um atacante bloqueia a conta de qualquer um de propósito); rate limiting freia sem trancar a vítima para sempre.

**Se der errado:**
- Nunca bloqueia → o bucket está sendo recriado a cada request (verifique o `computeIfAbsent` por chave estável).
- Bloqueia geral → a chave do bucket está fixa; use IP ou e-mail como chave.

**Conecte com a teoria:** slides "Rate limiting no login" e "Política de senha (NIST)".

---

## Lab 3.5 — Proteger `/admin` (10 min — faça se sobrar tempo)

**Por que fazemos isto:** é o exemplo clássico de **escalada vertical**: qualquer usuário logado abrindo o painel de admin. Mostra a diferença prática entre "estar logado" e "ter o papel".

**Conceito em 1 minuto:** `authenticated()` só exige login; `hasRole('ADMIN')` exige a *authority* `ROLE_ADMIN`. No baseline, `/admin` está como `authenticated()` — por isso o João (cliente comum) entra.

**Passo a passo:**

1. Antes de corrigir, logue como `joao@acme.com` e acesse `/admin`.
   - *O que observar:* ele entra (não deveria!). Essa é a evidência da escalada vertical.

2. Ajuste o `SecurityConfig`:
   ```java
   .requestMatchers("/admin/**").hasRole("ADMIN")   // era authenticated()
   ```
   - *O que isso significa:* agora só `ROLE_ADMIN` passa.

3. Reteste: `joao@acme.com` → **403**; `admin@portal.com` → **200**.

4. Escreva o teste:
   ```java
   @Test @WithMockUser(roles = "USER")  void comum403() throws Exception { mvc.perform(get("/admin")).andExpect(status().isForbidden()); }
   @Test @WithMockUser(roles = "ADMIN") void admin200()  throws Exception { mvc.perform(get("/admin")).andExpect(status().isOk()); }
   ```

**Ponto de entendimento:** *Por que `hasRole('ADMIN')` casa com a authority `ROLE_ADMIN` sem o prefixo?*
> Resposta esperada: o Spring adiciona o prefixo `ROLE_` automaticamente em `hasRole`.

**Se der errado:**
- Admin também toma 403 → o papel no banco/usuário não é exatamente `ROLE_ADMIN`, ou você usou `hasAuthority("ADMIN")` (sem prefixo) por engano.

**Conecte com a teoria:** slides "RBAC no Portal", "Escalada de privilégio" e "RBAC por rota na SecurityFilterChain".

---

## Autoavaliação (responda sem olhar o gabarito)

1. Explique o IDOR do Portal e onde a correção deve morar.
2. O que muda no banco quando um usuário MD5 faz o primeiro login após o Lab 3.3?
3. Por que rate limiting é preferível a bloqueio permanente de conta?
4. Qual a diferença entre `authenticated()` e `hasRole('ADMIN')`?

> Se travar em alguma, volte ao "Conceito em 1 minuto" do lab correspondente **antes** de seguir para a Aula 4.

## Entrega
- Código corrigido (Labs 3.2–3.5) + testes (IDOR e admin).
- Evidência do IDOR (Lab 3.1) **antes** da correção (print + URL).
- Uma frase por lab respondendo ao respectivo "Ponto de entendimento".

## Onde pedir ajuda / erros comuns gerais
- **App não sobe / login volta pro login:** rode com `-NoDocker` (H2) e confira `same-site: lax` no `application.yml`.
- **Comparar antes/depois:** a solução de referência está na tag `aula-3-hardened` (`git checkout aula-3-hardened`).
- **Travou num conceito?** Não avance "só para entregar" — chame o instrutor. O objetivo é entender, não concluir.
