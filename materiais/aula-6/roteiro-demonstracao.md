# Aula 6 — Roteiro de Demonstração (INSTRUTOR)

Guia **pronto para demonstrar ao vivo**. Padrão: **falha no `baseline` → `hardened` → provar → mostrar a config/código.**
Soluções na tag `aula-6-hardened` (Dockerfile, `SecurityConfig`, `application.yml`, `pom.xml`).

> **Docker:** as VMs dos alunos não têm. Actuator (6.1) e SCA (6.2) **não precisam** de Docker. Para o Trivy (6.3), use o **binário nativo** (`trivy config`/`trivy fs`); o build+scan da imagem é bônus, na sua máquina.

## Preparação
```bash
git checkout aula-6-baseline
./scripts/start.sh --lab 6 --no-docker
```

---

## DEMO 1 — Actuator vaza segredos  · ~6 min

**O que falar:** "O Actuator ajuda a operar a app — e, aberto, entrega a planta dela para qualquer um."

### 1a) Exposto (baseline)
```bash
curl -s -o /dev/null -w 'env -> %{http_code}\n' http://localhost:8080/actuator/env
curl -s http://localhost:8080/actuator/env | head -c 300
```
- **Esperado:** `env -> 200` e um dump de configuração/variáveis.
- **O que dizer:** "Isso lista props e, muitas vezes, segredos. Reconhecimento de graça."

### 1b) Fechado (hardened)
```bash
git checkout aula-6-hardened && ./scripts/start.sh --lab 6 --no-docker
curl -s -o /dev/null -w 'env -> %{http_code}\n'    http://localhost:8080/actuator/env      # 401/404
curl -s -o /dev/null -w 'health -> %{http_code}\n' http://localhost:8080/actuator/health   # 200
```
- **Esperado:** `env` bloqueado, `health` ok. `/h2-console` indisponível.
- **Mostre** no `application.yml`: `exposure.include: health,info` e `h2.console.enabled: false`.
- **Conceito:** superfície mínima em produção.

---

## DEMO 2 — SCA: a dependência com CVE (Text4Shell)  · ~7 min

**O que falar:** "A maior parte do código que entregamos não é nossa. Uma lib com CVE é uma porta que você nem escreveu."

### 2a) Detectar
```bash
./mvnw -Psecurity verify -DnvdApiKey=SUA_CHAVE      # abre target/dependency-check-report.html
```
- **Esperado:** finding para **`commons-text` 1.9 → CVE-2022-42889 (Text4Shell)**.
- **O que dizer:** "CVSS alto, exploração conhecida. Entrou como dependência — talvez transitiva."

### 2b) Corrigir
- No `pom.xml`, atualize `commons-text` para `1.10.0+` (mostre o diff baseline→hardened).
- Rode de novo: o CVE **some** (ou o gate deixa de falhar).
- **Conceito:** SCA + atualização; ideal falhar o build no crítico.

> Dica: prepare o relatório **antes** da aula (a 1ª execução baixa a base do NVD e demora). Peça a NVD API key.

---

## DEMO 3 — Imagem de container segura (Trivy)  · ~6 min

**O que falar:** "Como empacotamos importa: root, segredo em layer e imagem gorda ampliam qualquer falha."

### 3a) Sem Docker (o que o aluno faz) — Trivy nativo
```bash
trivy config .     # analisa o Dockerfile: root? sem HEALTHCHECK? más práticas?
trivy fs .         # analisa dependências/arquivos
```
- **Esperado (baseline):** alertas de config (roda como root etc.) e CVEs.
- **Mostre** o `Dockerfile` hardened: **multi-stage**, `USER` não-root, base `eclipse-temurin:17-jre`, sem segredo.

### 3b) Com Docker (bônus, sua máquina)
```bash
docker build -t portal-pedidos:hardened .
docker run --rm aquasec/trivy:latest image portal-pedidos:hardened
docker history portal-pedidos:hardened      # sem segredo em layer
```
- **Conceito:** multi-stage + não-root + sem segredo = menor superfície e menor privilégio.

---

## DEMO 4 — Gate de segurança no CI (shift-left)  · ~5 min

**O que falar:** "Achar em produção é caro; achar no PR é barato. Vamos deixar o crítico **não passar**."

- Abra o workflow (GitHub Actions/GitLab CI) com 3 gates: **build+test**, **SAST**, **SCA**.
- Mostre o gate de SCA configurado para **falhar** em CVSS alto (`failBuildOnCVSS`).
- **Demonstre a falha:** reintroduza `commons-text` 1.9 num commit e rode o pipeline (ou o comando localmente):
```bash
./mvnw -Psecurity verify     # com a lib vulnerável de volta -> BUILD FAILURE no gate de SCA
```
- **Esperado:** o build **falha** no estágio de SCA.
- **Conceito:** shift-left — proteção automática, não depende de alguém lembrar.

---

## Encerramento (30s)
Amarre o ciclo completo: **código seguro (Aulas 2–5) → operação segura (Actuator) → dependências (SCA) → empacotamento (imagem) → pipeline (gates)**. É o "ponta a ponta". Em seguida: **CTF** e **Simulado final**.

## Erros comuns na hora da demo
- **Dependency-Check lento/timeout:** falta a NVD API key; rode uma vez antes da aula.
- **`trivy` ausente:** instale o binário nativo (não precisa de Docker para `config`/`fs`).
- **`/actuator/health` bloqueou:** você fechou demais; mantenha `health,info`.
- **CVE persiste após atualizar:** a versão antiga é transitiva; force em `dependencyManagement`.
