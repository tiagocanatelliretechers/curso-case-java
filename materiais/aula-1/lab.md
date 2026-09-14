# Aula 1 — Guia de Laboratório (aluno)
## Reconhecimento e Levantamento de Requisitos de Segurança

**Duração:** 60 min · **Modalidade:** individual · **Ambiente:** JDK 17 + Docker + IDE + Postman/navegador
**Ponto de partida:** branch/tag `aula-1-baseline` do projeto `portal-pedidos`.

> ⚠️ Ambiente de treinamento, vulnerável de propósito. Não exponha na rede.

Ao final você terá: a aplicação rodando, uma **Ficha de Reconhecimento** preenchida e um
conjunto de **requisitos de segurança** derivados de abuse cases.

---

## Lab 1.1 — Subir e navegar (15 min)

**Objetivo:** ter o Portal de Pedidos rodando e conhecer as telas.

**Pré-requisitos:** Docker OU JDK 17 + Maven; porta 8080 livre.

**Passo a passo:**
1. Faça checkout do ponto de partida:
   ```bash
   git checkout aula-1-baseline
   ```
2. Suba a aplicação (escolha uma opção):
   - Local (H2): `./mvnw spring-boot:run`
   - Docker (PostgreSQL): `docker compose up --build`
3. Abra <http://localhost:8080>.
4. Faça login com cada conta de teste e observe as diferenças:
   - `admin@portal.com` / `admin123`
   - `joao@acme.com` / `senha123`
   - `maria@globex.com` / `senha123`
5. Navegue por: catálogo (`/produtos`), busca, seus pedidos (`/pedidos`), detalhe de um pedido, `/admin`, `/importar-catalogo`.

**Resultado esperado:** aplicação acessível; você consegue logar e criar um pedido.

**Checkpoint (instrutor):**
- [ ] A aplicação sobe sem erro na porta 8080?
- [ ] O aluno consegue criar um pedido logado como `joao@acme.com`?
- [ ] O aluno percebeu que `/admin` abre mesmo logado como cliente comum?

---

## Lab 1.2 — Reconhecimento manual (25 min)

**Objetivo:** mapear a superfície da aplicação como um atacante faria, **sem** explorar ainda — apenas observar e documentar. Preencha a **Ficha de Reconhecimento** (`anexos/ficha-reconhecimento.md`).

**Pré-requisitos:** aplicação rodando; navegador com DevTools; Postman/curl.

**Passo a passo:**
1. **Endpoints:** navegue e anote todas as rotas que encontrar (páginas e API). Dica: veja os links no HTML e as chamadas na aba Network do DevTools.
2. **Tecnologias expostas:** inspecione os headers de resposta:
   ```bash
   curl -sI http://localhost:8080/
   ```
   Anote `Server`, headers ausentes de segurança (ex.: `X-Frame-Options`, `Content-Security-Policy`).
3. **Mensagens de erro:** provoque um erro e observe o que é revelado:
   ```bash
   curl -s "http://localhost:8080/pedidos/abc"   # id inválido
   ```
   O stack trace/mensagem aparece? O que ele revela (framework, banco, classes)?
4. **Endpoints de gestão:** teste se o Actuator e o H2 console estão abertos:
   ```bash
   curl -s -o /dev/null -w '%{http_code}\n' http://localhost:8080/actuator
   curl -s http://localhost:8080/actuator/mappings | head -c 300
   ```
   Abra `/h2-console` no navegador.
5. **Comentários e artefatos:** veja o HTML/JS retornado (View Source) em busca de comentários, contas de teste, caminhos internos.
6. **API:** obtenha um token e liste rotas da API:
   ```bash
   curl -s -X POST http://localhost:8080/api/auth/login \
     -H 'Content-Type: application/json' \
     -d '{"email":"joao@acme.com","senha":"senha123"}'
   ```
7. Registre tudo na Ficha de Reconhecimento.

**Resultado esperado:** Ficha preenchida com ≥ 6 endpoints, ≥ 2 tecnologias/versões inferidas, ≥ 2 exposições de configuração (ex.: Actuator, H2, stack trace), e observações sobre autenticação.

**Checkpoint (instrutor):**
- [ ] Identificou o Actuator exposto (`/actuator/env`, `/actuator/mappings`)?
- [ ] Notou o stack trace/mensagem de erro detalhada retornada ao cliente?
- [ ] Encontrou o H2 console acessível?
- [ ] Listou os endpoints da API (`/api/auth/login`, `/api/pedidos`)?

---

## Lab 1.3 — Abuse cases e requisitos de segurança (20 min)

**Objetivo:** para **2 funcionalidades** do Portal, escrever abuse cases e derivar
requisitos de segurança **verificáveis** usando o template de Security User Story
(`anexos/template-security-user-story.md`).

**Funcionalidades (escolha estas duas):**
1. **Cadastro de cliente** (`/registrar`).
2. **Upload de comprovante** (`/pedidos/{id}/comprovante`).

**Passo a passo:**
1. Para cada funcionalidade, descreva o fluxo funcional em 1–2 linhas.
2. Escreva **pelo menos 2 abuse cases** por funcionalidade no formato:
   *"As an attacker, I want <ação> so that <ganho>."*
3. Para cada abuse case, derive **pelo menos 1 requisito de segurança verificável**
   (ao todo, no mínimo **3 requisitos por funcionalidade**), cada um com **critério de aceite**.
4. Marque a categoria OWASP e, se souber, o item ASVS relacionado.

**Resultado esperado:** ≥ 2 abuse cases e ≥ 3 requisitos de segurança verificáveis por funcionalidade (total ≥ 6 requisitos), preenchidos no template.

**Checkpoint (instrutor):**
- [ ] Os requisitos são **verificáveis** (têm critério de aceite testável)?
- [ ] Para cadastro, apareceu algo sobre hashing de senha e/ou user enumeration?
- [ ] Para upload, apareceu allowlist de tipo, limite de tamanho e/ou nome gerado pelo servidor?
- [ ] Cada requisito está associado a uma categoria OWASP?

---

## Entrega

Ao final, entregue (por aluno):
- `ficha-reconhecimento.md` preenchida.
- `template-security-user-story.md` preenchido para as 2 funcionalidades.

Esses artefatos serão retomados na Aula 2 (design/validação) — guarde-os.
