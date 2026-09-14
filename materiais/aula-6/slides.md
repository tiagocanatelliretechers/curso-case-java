# Aula 6 — Slides (outline)
## Secure Deployment & Maintenance + Revisão Geral e Simulado

> 240 min (2 intervalos de 10 min). Última aula: fecha o SDLC e integra tudo.

---

### BLOCO 1 (50 min) — Secure Deployment

**Slide 1 — Objetivos**
- Endurecer (hardening) o deploy e a configuração.
- Gerenciar segredos e dependências no pipeline.
- Montar CI/CD com gates de segurança.

**Slide 2 — Security Misconfiguration na prática**
- Diferenças entre dev/staging/prod (nunca subir config de dev em prod).
- Hardening do servidor de aplicação: desabilitar o que não usa.
- Remover banners/versões; endpoints de management (Actuator) protegidos ou desabilitados em prod.
- **Nota:** No Portal, Actuator e H2 console estão abertos — Lab 6.1.

**Slide 3 — Gestão de segredos em deploy**
- Variáveis de ambiente > arquivo no repo; melhor ainda: secrets manager (Vault, AWS/GCP Secrets).
- **12-factor app:** config no ambiente, não no código.
- Nunca colocar segredo em imagem Docker (fica nas layers).
- **Nota:** Ligue com o Dockerfile do baseline (secret em ENV) — Lab 6.3.

**Slide 4 — Software & Data Integrity Failures**
- Assinar e verificar artefatos; **SBOM** (Software Bill of Materials).
- Dependency confusion; fixar versões e repositórios confiáveis.
- Vulnerable & Outdated Components (A06): dependências desatualizadas com CVE.
- **Nota:** No Portal, `commons-text` 1.9 tem CVE — Lab 6.2.

**Slide 5 — Verificação de dependências (SCA)**
- OWASP **Dependency-Check**, **Snyk**, **GitHub Dependabot**.
- Rodar no build; falhar em CVSS alto; gerar relatório/SBOM.
- **Nota:** SCA complementa SAST/DAST (Aula 5).

**Slide 6 — Segurança em CI/CD (shift-left prático)**
- Gates: SAST no PR, SCA no build, DAST em staging antes do deploy.
- Falhar o build quando encontrar crítico; não permitir merge sem passar.
- **Nota:** Lab 6.4 monta um pipeline com 3 gates.

**Slide 7 — IaC e segurança de containers**
- Dockerfile hardening: usuário **não-root**, imagem **mínima**, **multi-stage**, sem segredos em layer.
- Scan de imagem: **Trivy**/**Grype**.
- **Nota:** Mostre um Dockerfile inseguro vs. hardened.

---

### BLOCO 2 (40 min) — Secure Maintenance & Code Review

**Slide 8 — Patch management**
- Dependências desatualizadas = causa comum de incidente.
- Monitoramento contínuo de CVEs; processo de atualização priorizado por risco.
- **Nota:** Automatize (Dependabot/renovate) + triagem.

**Slide 9 — Monitoramento e resposta**
- WAF, logging centralizado, alertas de anomalia.
- **RASP** (Runtime Application Self-Protection) — menção conceitual.
- Plano de resposta a incidente e runbooks.
- **Nota:** Ligue com logging seguro da Aula 5.

**Slide 10 — Code review de segurança (manual)**
- Checklist do revisor: authn, authz (posse!), input validation, dados sensíveis, dependências novas, tratamento de erro/log.
- Revisar o **diff**, não só o arquivo; questionar dados que cruzam trust boundaries.
- **Nota:** Distribua o checklist (`anexos/checklist-code-review.md`).

**Slide 11 — Segurança no SDLC/Agile/CI-CD**
- SAST/SCA como **gate obrigatório** em Pull Requests.
- **Security Champions** dentro dos squads: elo entre dev e AppSec.
- Threat modeling revisitado a cada mudança relevante de arquitetura.
- **Nota:** Cultura > ferramenta; ferramenta habilita a cultura.

**Slide 12 — Métricas de um programa de AppSec**
- MTTR de vulnerabilidade (tempo médio de correção).
- % de findings corrigidos vs. aceitos como risco.
- Cobertura de scans por repositório; densidade de findings por KLOC.
- **Nota:** Métricas mostram tendência e priorizam investimento.

---

### INTERVALO (10 min)

---

### BLOCO 3 (70 min) — Laboratório final (pipeline de deploy seguro)

**Slide 13 — Laboratório final**
- Lab 6.1: proteger/desabilitar Actuator (e H2 console) em prod.
- Lab 6.2: Dependency-Check → achar e atualizar a dependência vulnerável.
- Lab 6.3: hardening do Dockerfile (multi-stage, não-root, sem segredo) + Trivy.
- Lab 6.4: pipeline CI (GitHub Actions/GitLab CI) com 3 gates (build+test, SAST, SCA) que falha em crítico.
- **Nota:** Distribua `lab.md`. Priorize 6.1 e 6.2 se o tempo apertar.

---

### INTERVALO (10 min)

---

### BLOCO 4 (50 min) — Revisão integrada + CTF

**Slide 14 — Revisão integrada das 6 aulas**
- Percorrer o `mapa-rastreabilidade.md`: módulo × aula × lab × OWASP.
- O que foi corrigido na aplicação, aula a aula.
- **Nota:** Use o mapa como âncora visual do curso inteiro.

**Slide 15 — Capture the Flag (curto)**
- 4–6 "flags" sobre a versão final da app, cada uma remetendo a um módulo diferente.
- Ver `anexos/ctf.md` (enunciado do aluno) e gabarito no `gabarito.md`.
- **Nota:** Individual ou em duplas; 25–30 min; discuta as soluções.

---

### BLOCO 5 (20 min) — Simulado final + encerramento

**Slide 16 — Simulado de certificação**
- 20 questões cobrindo os 10 módulos (ver `../simulado-final.md`).
- Cronometrado se possível.
- **Nota:** Corrija ao vivo; revise os pontos fracos da turma.

**Slide 17 — Encerramento**
- Do requisito ao deploy: segurança em todo o SDLC.
- Próximos passos: ASVS como padrão, Security Champions, automação de gates.
- **Nota:** Agradeça, colete feedback, aponte trilha de estudo (OWASP, ASVS, cheat sheets).
