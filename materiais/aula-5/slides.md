# Aula 5 — Slides (outline)
## Secure Coding para Error Handling + SAST & DAST

> 240 min (2 intervalos de 10 min).
> Ferramentas comerciais (AppScan, Fortify, WebInspect) são cobertas na teoria e no lab em
> **Trilha A** (se houver licença/trial); a **Trilha B** (open-source: SonarQube/Semgrep + OWASP ZAP)
> está sempre disponível. O laboratório roda a Trilha B por padrão.

---

### BLOCO 1 (30 min) — Error Handling seguro

**Slide 1 — Objetivos**
- Tratar erros sem vazar informação.
- Fail securely e logging seguro.
- Preparar a app para os scans (SAST/DAST) de hoje.

**Slide 2 — Mensagens de erro são vazamento**
- Stack trace, versão de framework, estrutura de banco → mapa para o atacante.
- No baseline: `server.error.include-stacktrace=always` e erros SQL ao cliente.
- **Nota:** Retome o Lab 1.2 (o aluno já viu o stack trace no reconhecimento).

**Slide 3 — Fail securely**
- Estado padrão em erro = **negar**.
- Exceção não tratada não pode "abrir" acesso/autorização.
- Respostas de erro consistentes (não revelar existência de recursos por diferença de mensagem).
- **Nota:** Ligue com anti user-enumeration (Aulas 1/3).

**Slide 4 — Tratamento de exceções no Spring**
- `@ControllerAdvice` + `@ExceptionHandler` → resposta genérica ao cliente.
- Detalhe fica no **log do servidor**, com correlação (traceId), não na resposta.
- Desligar `include-stacktrace`/`include-exception`.
- **Nota:** Mostre o handler global padronizando 4xx/5xx.

**Slide 5 — Logging seguro (gancho A09)**
- **Logar:** autenticação (sucesso/falha), mudanças de permissão, falhas de validação, acesso negado.
- **Nunca logar:** senha, token, dados de cartão, PII desnecessária.
- **Log injection (CWE-117):** sanitizar/《neutralizar》 quebras de linha do input antes de logar.
- **Nota:** No baseline, a URL do import é logada crua — permite forjar linhas de log.

**Slide 6 — Boas práticas de logging**
- Formato estruturado (JSON), níveis adequados, correlação de requisição.
- Centralização (ELK/《SIEM》) e retenção; alertas para eventos de segurança.
- **Nota:** Antecipa monitoramento da Aula 6.

---

### BLOCO 2 (20 min) — Laboratório rápido: Error Handling

**Slide 7 — Lab 5.1**
- Implementar `@ControllerAdvice` global; desligar stack trace ao cliente; corrigir log injection.
- **Nota:** Distribua `lab.md`. Curto e de alto impacto.

---

### INTERVALO (10 min)

---

### BLOCO 3 (40 min) — SAST

**Slide 8 — O que é SAST**
- Static Application Security Testing: analisa **código-fonte/bytecode** sem executar.
- Acha: Injection, cripto fraca, hardcoded secrets, padrões inseguros.
- "White-box", cedo no ciclo (shift-left).
- **Nota:** SAST vê o código; DAST vê o comportamento — complementares.

**Slide 9 — Ferramentas SAST (panorama)**
- **Enterprise:** **Fortify SCA** (OpenText), **AppScan Source** (HCL) — rulepacks amplos,
  integração em pipeline, triagem corporativa, dashboards.
- **Open-source/dev:** **SonarQube** (com regras de segurança) e **Semgrep** (regras em YAML, rápido).
- **Nota:** Enterprise = cobertura/《governança》; OSS = agilidade/custo. Muitos times combinam.

**Slide 10 — Forças e limitações do SAST**
- Forças: acha classes conhecidas cedo; roda sem ambiente; bom para CI.
- Limitações: **falsos positivos**; não vê problemas de runtime/config/ambiente; precisa de triagem.
- **Nota:** "SAST aponta onde olhar; o humano confirma."

**Slide 11 — Interpretar e triar resultados**
- Severidade × confiança; priorizar exploráveis.
- Supressão **justificada** (com comentário/rastreio) vs. correção.
- Baseline de findings e evitar regressão (gate no PR).
- **Nota:** Mostre findings que já sumiram (foram corrigidos nas Aulas 2–4) como reforço.

---

### BLOCO 4 (50 min) — Laboratório SAST

**Slide 12 — Lab 5.2**
- Trilha B: SonarQube (Docker) + Sonar Scanner no Portal; triar ≥ 5 findings; corrigir 1 alto.
- Trilha A (se houver): Fortify/AppScan Source equivalente.
- **Nota:** Suba o SonarQube antes da aula (imagem pesada).

---

### INTERVALO (10 min)

---

### BLOCO 5 (40 min) — DAST

**Slide 13 — O que é DAST**
- Dynamic Application Security Testing: testa a aplicação **em execução**, como um atacante externo.
- "Black-box", sem acesso ao código.
- Acha: config insegura, headers ausentes, comportamento real (XSS refletido, etc.).
- **Nota:** Complementa o SAST — pega o que só aparece rodando.

**Slide 14 — Ferramentas DAST (panorama)**
- **Enterprise:** **AppScan Standard** (HCL), **WebInspect** (OpenText) — crawling, ataque
  automatizado, scans autenticados, relatórios corporativos.
- **Open-source:** **OWASP ZAP** — proxy/scanner completo, baseline scan e scan autenticado.
- **Nota:** Todos precisam de **tuning** para cobrir fluxos autenticados/complexos.

**Slide 15 — Forças e limitações do DAST**
- Forças: acha problemas de config/runtime; poucos falsos positivos em certas classes.
- Limitações: cobertura depende do crawling; difícil em SPAs/fluxos complexos sem configurar.
- **Nota:** Combine com testes autenticados e casos de negócio.

**Slide 16 — SAST × DAST × IAST**
- SAST (código) + DAST (execução) + **IAST** (instrumentação em runtime, dentro da app durante testes).
- Processo maduro usa mais de um tipo + SCA (dependências, Aula 6).
- **Nota:** Nenhuma ferramenta sozinha cobre tudo; segurança é processo.

---

### BLOCO 6 (40 min) — Laboratório DAST + fechamento

**Slide 17 — Lab 5.3**
- Trilha B: OWASP ZAP baseline + scan autenticado contra o Portal; analisar relatório; validar 1 finding.
- Trilha A (se houver): WebInspect/AppScan Standard equivalente.
- **Nota:** Rode o ZAP via Docker apontando para o host da app.

**Slide 18 — Debrief + Quiz + ponte para a Aula 6**
- Comparar findings SAST vs DAST: o que só um dos dois achou?
- Quiz (`quiz.md`).
- Aula 6: deploy/manutenção seguros e pipeline com esses scans como gates.
- **Nota:** Aula 6 parte de `aula-6-baseline` (error handling corrigido).
