# Aula 5 — Guia de Laboratório (aluno)
## Error Handling + SAST & DAST

**Ponto de partida:** `aula-5-baseline` (cripto/sessão corrigidos na Aula 4).
**Ambiente:** JDK 17 + IDE. Subir o app: `./scripts/start.sh --lab 5 --no-docker` (ou `-Lab 5 -NoDocker`).

> **Sobre ferramentas e Docker:** este lab **não exige Docker** para o aluno. Usamos **SonarLint** (plugin da IDE) para SAST e **OWASP ZAP Desktop** (app nativo) para DAST. Se você tiver Docker/servidor, há uma trilha opcional com SonarQube e ZAP em container — mas ela **não é necessária**.

---

## Como usar este guia (leia antes de começar)

O objetivo não é "rodar a ferramenta e entregar o relatório" — é **entender o que a ferramenta encontra e por quê**. Uma ferramenta de segurança sem interpretação é ruído. Se você não consegue explicar um finding (o que é, por que é risco, o que fazer), **pare e releia o "Conceito em 1 minuto" ou chame o instrutor**.

Cada exercício tem: **Por que fazemos isto** · **Conceito em 1 minuto** · **Passo a passo** (com *o que observar* / *o que significa*) · **Ponto de entendimento** · **Se der errado** · **Conecte com a teoria**.

## Mapa: conceito → laboratório

| Conceito (slides) | Vulnerabilidade / risco | Lab | O que você vai provar |
|-------------------|-------------------------|-----|-----------------------|
| Falhas de tratamento de erro | stack trace vazando ao cliente; log injection | 5.1 | Que o erro entrega detalhes ao atacante — e como generalizar. |
| SAST (análise estática) | falhas visíveis no código-fonte | 5.2 | Que a ferramenta acha classes de bug — e como **triar** (VP × FP). |
| DAST (análise dinâmica) | falhas visíveis na aplicação rodando | 5.3 | Que o scan acha problemas em runtime — e como **validar** um finding. |

## Objetivos de aprendizagem
Ao final você deve conseguir **explicar**:
1. Por que vazar stack trace/mensagem de exceção ajuda um atacante — e o que é log injection.
2. A diferença entre **SAST** (lê o código, sem rodar) e **DAST** (ataca a app rodando, sem o código).
3. O que é **triar** um finding: verdadeiro positivo × falso positivo, severidade, ação.
4. Por que nunca se confia num relatório sem **validar** pelo menos um finding manualmente.

---

## Lab 5.1 — Error Handling seguro (25 min)

**Por que fazemos isto:** quando algo quebra, a aplicação **conta demais** — devolve o stack trace e a mensagem da exceção para o cliente. Isso entrega ao atacante nomes de classes, queries, caminhos e versões (um mapa da aplicação). E, no import, um input com quebras de linha **forja linhas no log** (log injection), atrapalhando investigação.

**Conceito em 1 minuto:** o cliente deve ver uma mensagem **genérica** ("ocorreu um erro"); o **detalhe** (exceção + um `traceId` para correlação) vai **só para o log do servidor**. Um `@ControllerAdvice` global centraliza isso. Contra log injection: **neutralize** `\r`/`\n` do input antes de logar e prefira placeholders (`log.info("... {}", valor)`), nunca concatenar input cru no log.

**Passo 0 — reproduza o vazamento (baseline):**
```bash
curl -s "http://localhost:8080/pedidos/abc"     # id inválido
```
- *O que observar:* volta **stack trace / mensagem de exceção**.
- *O que isso significa:* o cliente (qualquer um) recebe detalhes internos — reconhecimento gratuito para o atacante.

**Passo a passo (correção):**
1. Crie um `@ControllerAdvice` global com `@ExceptionHandler`:
   ```java
   @ExceptionHandler(Exception.class)
   public ResponseEntity<?> handle(Exception e) {
       String traceId = UUID.randomUUID().toString();
       log.error("Falha [{}]", traceId, e);                 // detalhe só no log
       return ResponseEntity.status(500).body(Map.of("erro","Ocorreu um erro","traceId",traceId));
   }
   ```
   - *O que isso significa:* o cliente recebe algo genérico + um id para suporte; o detalhe fica no servidor.

2. No `application.yml`, desligue a exposição:
   ```yaml
   server.error.include-message: never
   server.error.include-stacktrace: never
   server.error.include-exception: false
   ```

3. Corrija a **log injection** no `ImportController`/serviço: remova `\r`/`\n` do input e use placeholders:
   ```java
   String limpo = url.replaceAll("[\\r\\n]", "_");
   log.info("Importando catálogo de {}", limpo);
   ```

4. Reteste:
   - *O que observar:* `/pedidos/abc` → erro **genérico**; o log do servidor tem o detalhe + traceId; uma URL com `\n` **não** cria linhas falsas no log.

**Ponto de entendimento:** *Que informação um stack trace entrega a um atacante?*
> Resposta esperada: nomes de classes/pacotes, trechos de query, caminhos de arquivo, versões de libs — pistas para escolher o próximo ataque.

**Se der errado:**
- Ainda vaza detalhe → `include-message/stacktrace` continuam ligados, ou o handler não cobre a exceção lançada (use `Exception.class` como fallback).
- Log ainda injeta → você está concatenando o input direto (`log.info("... " + url)`) em vez de limpar + placeholder.

**Conecte com a teoria:** slides "Vazamento por erro/exceção" e "Log injection".

---

## Lab 5.2 — SAST (análise estática do código)

**Por que fazemos isto:** revisar cada linha à mão não escala. SAST lê o **código-fonte** e aponta classes inteiras de bug (injeção, cripto fraca, secrets, etc.) — mas gera **falsos positivos**. Saber **triar** é a habilidade central.

**Conceito em 1 minuto:** SAST = *Static Application Security Testing*. Analisa o código **sem executá-lo**, seguindo o fluxo de dados (da entrada "source" até o uso perigoso "sink"). Vantagem: acha cedo e mostra a linha exata. Limite: não sabe o contexto de runtime → alguns findings são falsos positivos. Por isso todo finding precisa de **triagem**: é real (VP) ou não (FP)? Qual a severidade? Corrigir ou suprimir com justificativa?

### Caminho principal (sem Docker) — SonarLint na IDE
1. Instale o **SonarLint** (Eclipse: *Help → Eclipse Marketplace → "SonarQube for IDE / SonarLint"*; VS Code: extensão *SonarLint*).
2. Abra o projeto `portal-pedidos`. O SonarLint analisa **ao abrir os arquivos** e marca os problemas na régua lateral (aba *SonarLint / Problems*).
   - *O que observar:* avisos em código com padrões inseguros.
   - *O que isso significa:* é o mesmo motor de regras do SonarQube, rodando **local, sem servidor nem Docker**.
3. **Triar ≥ 5 findings.** Para cada um anote: **regra**, **severidade**, **VP ou FP** (e por quê), **ação**.
4. **Corrija 1 finding real** de severidade alta; reabra o arquivo e confirme que o aviso **sumiu**.

### Caminho opcional (com Docker/servidor) — SonarQube
> Só se houver Docker + ~4 GB RAM. O instrutor pode subir isto e compartilhar a URL da turma.
```bash
docker run -d --name sonarqube -p 9000:9000 sonarqube:10-community   # http://localhost:9000 (admin/admin)
./mvnw -q sonar:sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.login=SEU_TOKEN -Dsonar.projectKey=portal-pedidos
```

**Ponto de entendimento:** *Por que um finding de SAST pode ser um falso positivo?*
> Resposta esperada: a análise não conhece o contexto de runtime (validações externas, dados confiáveis por configuração), então marca um caminho que na prática não é explorável.

**Se der errado:**
- SonarLint não mostra nada → confirme que o projeto foi importado como **Maven** e que os arquivos foram abertos (a análise é por arquivo aberto).
- SonarQube (opcional) não sobe → é a limitação de Docker/RAM; **use o SonarLint**, o resultado didático é o mesmo.

**Conecte com a teoria:** slides "SAST — como funciona (source→sink)" e "Triagem de findings".

---

## Lab 5.3 — DAST (análise dinâmica da app rodando)

**Por que fazemos isto:** SAST vê o código; DAST vê a **aplicação viva** — headers, cookies, respostas, comportamento. Alguns problemas só aparecem em runtime. E, como qualquer scanner, ele **erra**: você precisa **validar** o que ele reporta.

**Conceito em 1 minuto:** DAST = *Dynamic Application Security Testing*. Ataca a app rodando **sem ver o código** (caixa-preta): rastreia as páginas e envia payloads. Acha coisas como headers de segurança ausentes, cookies sem flags, comportamentos inseguros. Limite: não sabe *onde* no código está o problema, e também gera falsos positivos → **valide manualmente** pelo menos um finding reproduzindo a requisição.

### Caminho principal (sem Docker) — OWASP ZAP Desktop
1. Baixe o **OWASP ZAP** (app nativo Java): <https://www.zaproxy.org/download/>. Precisa de Java (você já tem).
2. Com o Portal rodando (`--lab 5 --no-docker`), no ZAP use **Quick Start → Automated Scan**, alvo `http://localhost:8080`.
   - *O que observar:* a aba *Alerts* lista os achados (ex.: headers ausentes, cookies).
   - *O que isso significa:* são problemas visíveis de fora, sem acesso ao código.
3. (Opcional) Configure um **contexto autenticado** (usuário `joao@acme.com`/`senha123`) para cobrir rotas logadas e rode o *Active Scan* nas rotas de pedidos.
4. Escolha **1 finding** e **valide manualmente**: reproduza a requisição (no navegador ou `curl`) e confirme que o problema existe de verdade.

### Caminho opcional (com Docker) — ZAP em container
```bash
docker run --rm -t ghcr.io/zaproxy/zaproxy:stable zap-baseline.py -t http://host.docker.internal:8080 -r zap.html
```

**Ponto de entendimento:** *Por que sempre validar manualmente um finding do DAST?*
> Resposta esperada: scanners geram falsos positivos; reproduzir a requisição confirma que o risco é real antes de gastar tempo corrigindo (ou reportar).

**Se der errado:**
- ZAP não acha o alvo → confirme que o app está em `http://localhost:8080` e que não há proxy bloqueando.
- Scan autenticado não cobre rotas logadas → o contexto/login não foi configurado; comece pelo *Automated Scan* não autenticado.

**Conecte com a teoria:** slides "DAST — caixa-preta" e "SAST × DAST (o que cada um vê)".

---

## Autoavaliação (responda sem olhar o gabarito)
1. Que informações um stack trace vaza — e por que isso ajuda o atacante?
2. Qual a diferença entre o que SAST e DAST enxergam?
3. Como você decide se um finding é verdadeiro ou falso positivo?
4. Por que validar manualmente um finding do DAST?

## Entrega
- Correções do Lab 5.1 (handler global + log seguro), com evidência antes/depois de `/pedidos/abc`.
- Notas de triagem SAST (≥ 5 findings) + 1 correção confirmada.
- Achados do DAST + **1 finding validado manualmente**.
- Uma frase por lab respondendo ao "Ponto de entendimento".

## Onde pedir ajuda / erros comuns gerais
- **Sem Docker?** É o caso das VMs — use **SonarLint** (SAST) e **ZAP Desktop** (DAST). Não precisa dos containers.
- **Comparar com a solução do 5.1:** `git checkout aula-5-hardened`.
- **Travou num conceito?** Não avance "só para entregar" — chame o instrutor.
