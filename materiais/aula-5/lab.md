# Aula 5 — Guia de Laboratório (aluno)
## Error Handling + SAST & DAST

**Ponto de partida:** `aula-5-baseline` (cripto/sessão corrigidos na Aula 4).
**Ambiente:** JDK 17 + IDE + **Docker** (para SonarQube e OWASP ZAP).

> Duas trilhas: **Trilha B** (open-source) roda sempre. **Trilha A** (comercial) é opcional,
> só se a turma tiver licença/trial de Fortify/AppScan/WebInspect.

---

## Lab 5.1 — Error Handling seguro (20 min)

**Objetivo:** parar de vazar stack trace e corrigir log injection.

**Passo 0 — Reproduza o vazamento (baseline):**
```bash
curl -s "http://localhost:8080/pedidos/abc"   # id invalido -> stack trace/mensagem
```

**Passo a passo:**
1. Crie um `@ControllerAdvice` global com `@ExceptionHandler` que:
   - retorna resposta **genérica** ao cliente (ex.: "Ocorreu um erro" / JSON `{ "erro": "..." }`);
   - registra o detalhe (exceção + traceId) **apenas no log do servidor**.
2. No `application.yml`, desligue a exposição:
   ```yaml
   server.error.include-message: never
   server.error.include-stacktrace: never
   server.error.include-exception: false
   ```
3. Corrija a **log injection** em `ImportController`: neutralize quebras de linha/《caracteres de controle》
   do input antes de logar (ex.: substituir `\r`/`\n`), e prefira logar com placeholders (`log.info("... {}", valor)`).
4. Teste: `/pedidos/abc` não revela mais stack trace; a URL do import não injeta linhas falsas no log.

**Resultado esperado:** erros genéricos ao cliente; detalhe só no servidor; log sem injeção.

**Checkpoint (instrutor):**
- [ ] `/pedidos/abc` retorna erro genérico (sem stack trace)?
- [ ] O detalhe da exceção aparece no log do servidor (não na resposta)?
- [ ] A URL com `\n` não cria linhas de log forjadas?

---

## Lab 5.2 — SAST

### Trilha B (open-source) — SonarQube (50 min)

**Pré-requisitos:** Docker; ~4 GB de RAM livres.

**Passo a passo:**
1. Suba o SonarQube:
   ```bash
   docker run -d --name sonarqube -p 9000:9000 sonarqube:10-community
   ```
   Acesse <http://localhost:9000> (admin/admin; troque a senha). Crie um token em *My Account → Security*.
2. Rode o scanner no projeto:
   ```bash
   ./mvnw -q sonar:sonar \
     -Dsonar.host.url=http://localhost:9000 \
     -Dsonar.login=SEU_TOKEN \
     -Dsonar.projectKey=portal-pedidos
   ```
3. No painel, abra a aba **Security Hotspots / Issues**. Triar **≥ 5 findings**:
   para cada um, anote: regra, severidade, se é verdadeiro/falso positivo, ação (corrigir/suprimir com justificativa).
4. Observe que vários findings das Aulas 2–4 **já não aparecem** (SQLi, MD5, etc.) — reforço.
5. **Corrija 1 finding novo de severidade alta** ainda presente e rode o scan de novo para confirmar que sumiu.

**Resultado esperado:** análise executada; ≥ 5 findings triados; 1 alto corrigido e confirmado.

**Checkpoint (instrutor):**
- [ ] O scan concluiu e apareceu no painel?
- [ ] O aluno triou ≥ 5 findings distinguindo VP/FP?
- [ ] 1 finding alto foi corrigido e desapareceu no re-scan?

### Trilha A (opcional) — Fortify SCA / AppScan Source

1. Traduza/analise o projeto com a ferramenta disponível (ex.: `sourceanalyzer` do Fortify, ou AppScan Source).
2. Abra os resultados (AWB/relatório), triar ≥ 5 findings equivalentes.
3. Corrija 1 finding alto e re-analise.

**Checkpoint:** mesmos critérios da Trilha B.

---

## Lab 5.3 — DAST

### Trilha B (open-source) — OWASP ZAP (40 min)

**Pré-requisitos:** app rodando (`aula-5-baseline`); Docker.

**Passo a passo:**
1. **Baseline scan** (não autenticado):
   ```bash
   docker run --rm --network=host -t ghcr.io/zaproxy/zaproxy:stable \
     zap-baseline.py -t http://localhost:8080 -r zap-baseline.html
   ```
   (em macOS/Windows, se `--network=host` não funcionar, use `-t http://host.docker.internal:8080`.)
2. Abra `zap-baseline.html` e revise os alertas (headers ausentes, cookies, etc.).
3. **Scan autenticado:** configure um contexto no ZAP (GUI) com as credenciais de teste
   (`joao@acme.com`/`senha123`) para cobrir rotas logadas; rode o *Active Scan* nas rotas de pedidos.
4. Escolha **1 finding** e valide **manualmente** (reproduza a requisição e confirme).

**Resultado esperado:** relatório gerado; alertas revisados; 1 finding validado manualmente.

**Checkpoint (instrutor):**
- [ ] O relatório do ZAP foi gerado e revisado?
- [ ] Houve pelo menos um scan cobrindo rota autenticada?
- [ ] O aluno validou manualmente 1 finding (não só confiou no relatório)?

### Trilha A (opcional) — WebInspect / AppScan Standard

1. Configure o scan (URL alvo + autenticação gravada) contra o Portal.
2. Rode crawling + ataque; exporte o relatório.
3. Valide manualmente 1 finding.

**Checkpoint:** mesmos critérios da Trilha B.

---

## Entrega
- Correções do Lab 5.1 (handler global + log seguro).
- Notas de triagem SAST (≥ 5) + 1 correção confirmada.
- Relatório DAST + 1 finding validado manualmente.
