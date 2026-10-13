# Aula 6 — Roteiro de Demonstração (INSTRUTOR)

Guia **pronto para demonstrar ao vivo**. Padrão: **falha no `baseline` → `hardened` → provar → mostrar a config/código.**
Soluções na tag `aula-6-hardened` (Dockerfile, `SecurityConfig`, `application.yml`, `pom.xml`).

> **Docker:** as VMs dos alunos não têm. Actuator (6.1) e SCA (6.2) **não precisam** de Docker.
>
> ⭐ **Demos 3 e 4 agora rodam na NUVEM (GitHub Actions) — recomendado.** Nada para instalar
> (sem Trivy local, sem NVD key, sem Defender bloqueando). Passo a passo completo em
> **`DEMO-NUVEM.md`**. O caminho local (Trivy na máquina) fica como **alternativa opcional**
> ao final de cada demo.

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

## DEMO 3 — Imagem/dependências seguras (Trivy na NUVEM)  · ~6 min

**O que falar:** "Como empacotamos importa: root, segredo em layer e imagem gorda ampliam qualquer falha. E eu não precisei instalar scanner nenhum."

### 3·NUVEM — GitHub Actions ⭐ recomendado (passo a passo em `DEMO-NUVEM.md`)
No repo `curso-case-java-aula-6-deploy-seguro`, aba **Actions → security-gate**:
- **`main` (baseline) → 🔴 vermelho:** abra o job "Trivy" → passo "relatório completo" e mostre
  `commons-text 1.9 · CVE-2022-42889 (Text4Shell) · CRITICAL` **e** `Dockerfile · AVD-DS-0002 (HIGH)` (roda como root).
- **`hardened` → 🟢 verde:** mesmo job, nada HIGH/CRITICAL.
- **Mostre** o `Dockerfile` hardened: **multi-stage**, `USER appuser`, base `eclipse-temurin:17-jre`, sem segredo.
- **SCA nativo:** aba **Security → Dependabot** mostra a Text4Shell sozinha; em **Pull requests** está o PR automático "Bump commons-text 1.9 → …".

### 3·LOCAL (opcional) — Trivy na máquina
Na sua máquina de instrutor (ou Windows com `trivy.exe`), mesmos achados offline:
```bash
trivy config .     # Dockerfile: root? sem HEALTHCHECK? más práticas?
trivy fs .         # dependências/arquivos (CVEs)
```
- Windows (PATH na sessão): `$env:Path += ";C:\trivy\windows"` e então `trivy config .`.
- Com Docker (bônus): `docker build -t portal-pedidos:hardened .` → `docker run --rm aquasec/trivy:latest image portal-pedidos:hardened`.
- **Conceito:** multi-stage + não-root + sem segredo = menor superfície e menor privilégio.

---

## DEMO 4 — Gate de segurança no CI (shift-left)  · ~5 min

**O que falar:** "Achar em produção é caro; achar no PR é barato. O inseguro **não passa sozinho**."

### 4·NUVEM — o gate que reprova ⭐ recomendado
- Na aba **Actions**, mostre **lado a lado**: ✔ verde no `hardened` e ✘ vermelho no `main`.
  "Mesmo pipeline. A diferença é só o código."
- Abra o passo **"Gate — reprova em HIGH/CRITICAL"** do `main`: o build falhou (`exit-code 1`).
  Mostre a linha do YAML (`severity: HIGH,CRITICAL` + `--exit-code 1`) — "é aqui que o crítico vira barreira."
- **Prova viva:** abra um **Pull Request** → a turma vê o **check do security-gate** rodando no PR
  (vermelho com a lib vulnerável, verde depois de corrigida).
- **Conceito:** shift-left — proteção automática no PR, não depende de alguém lembrar.

### 4·LOCAL (opcional) — falhar o build na máquina
```bash
git checkout aula-6-baseline        # commons-text 1.9 de volta
./mvnw -Psecurity verify -DnvdApiKey=SUA_CHAVE   # -> BUILD FAILURE no gate de SCA
```
- **Esperado:** o build **falha** no estágio de SCA (precisa da NVD key pré-rodada).

---

## Encerramento (30s)
Amarre o ciclo completo: **código seguro (Aulas 2–5) → operação segura (Actuator) → dependências (SCA) → empacotamento (imagem) → pipeline (gates)**. É o "ponta a ponta". Em seguida: **CTF** e **Simulado final**.

## Erros comuns na hora da demo
- **Dependency-Check lento/timeout:** falta a NVD API key; rode uma vez antes da aula.
- **`trivy` ausente:** instale o binário nativo (não precisa de Docker para `config`/`fs`).
- **`/actuator/health` bloqueou:** você fechou demais; mantenha `health,info`.
- **CVE persiste após atualizar:** a versão antiga é transitiva; force em `dependencyManagement`.
