# Aula 6 — Guia de Laboratório final (aluno)
## Deploy Seguro Ponta a Ponta

**Ponto de partida:** `aula-6-baseline` (app já corrigida nas Aulas 2–5, mas com: Actuator/H2
abertos, dependência com CVE, Dockerfile inseguro, CI sem gates).
**Ambiente:** JDK 17 + Docker + conta GitHub/GitLab (para o Lab 6.4, pode ser simulado localmente).

---

## Lab 6.1 — Proteger o Actuator (e H2 console) em produção (15 min)

**Passo 0 — Reproduza:**
```bash
curl -s -o /dev/null -w '%{http_code}\n' http://localhost:8080/actuator/env   # 200 = exposto
```

**Passo a passo:**
1. Restrinja os endpoints do Actuator em produção:
   ```yaml
   management.endpoints.web.exposure.include: health,info
   management.endpoint.health.show-details: never
   management.endpoint.env.show-values: never
   ```
2. Proteja o que sobrar com autenticação (ex.: `hasRole('ADMIN')` para `/actuator/**`) e desabilite o H2 console em prod (`spring.h2.console.enabled: false`, ou perfil `postgres`).
3. Teste: `/actuator/env` → 401/404; `/actuator/health` → ok.

**Checkpoint:** `/actuator/env` deixa de expor segredos; `/h2-console` indisponível em prod.

---

## Lab 6.2 — Dependency-Check e atualização da dependência vulnerável (20 min)

**Passo a passo:**
1. Rode a análise de dependências (perfil `security` já configurado no `pom.xml`):
   ```bash
   ./mvnw -Psecurity verify
   ```
   Abra `target/dependency-check-report.html`.
2. Identifique a dependência com CVE (**`commons-text` 1.9** → CVE-2022-42889, "Text4Shell").
3. Atualize para uma versão corrigida (ex.: `1.10.0`+) no `pom.xml`.
4. Rode de novo → o CVE não deve mais aparecer (ou o build não falha mais no gate de CVSS).

**Checkpoint:** o relatório aponta o `commons-text` 1.9; após atualizar, o finding some.

---

## Lab 6.3 — Hardening do Dockerfile + Trivy (20 min)

**Passo a passo:**
1. Reescreva o `Dockerfile` com:
   - **multi-stage** (stage de build com Maven; stage final só com o JRE + o jar);
   - usuário **não-root**;
   - **sem** segredos em `ENV` (passe em runtime via ambiente);
   - imagem base mínima (ex.: `eclipse-temurin:17-jre`).
2. Faça o build e escaneie a imagem:
   ```bash
   docker build -t portal-pedidos:hardened .
   docker run --rm aquasec/trivy:latest image portal-pedidos:hardened
   ```
3. Confirme que não há segredo embutido (`docker history portal-pedidos:hardened`) e que roda como não-root.

**Checkpoint:** imagem multi-stage, não-root, sem segredo em layer; Trivy sem críticos evitáveis.

---

## Lab 6.4 — Pipeline CI com gates de segurança (15 min)

**Passo a passo:**
1. Crie um workflow (GitHub Actions **ou** GitLab CI — escolha um) com 3 estágios:
   - **build+test:** `./mvnw verify`
   - **SAST:** SonarQube/Semgrep (da Aula 5)
   - **SCA:** `./mvnw -Psecurity verify` (Dependency-Check)
2. Configure o pipeline para **falhar** se houver finding crítico (ex.: `failBuildOnCVSS` alto).
3. Faça um commit que reintroduza uma dependência vulnerável e observe o pipeline **falhar** no gate de SCA.

**Checkpoint:** o pipeline roda os 3 gates e falha o build diante de um crítico.

---

## Entrega
- Actuator/H2 protegidos; dependência atualizada; Dockerfile hardened + scan; pipeline com gates.
- Em seguida: **CTF** (`anexos/ctf.md`) e **Simulado final** (`../simulado-final.md`).
