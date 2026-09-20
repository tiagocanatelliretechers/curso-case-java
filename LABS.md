# Laboratórios — Mapa de Execução

Como os laboratórios estão organizados, onde ficam os codebases e como subir o ambiente
com **um único script** (Linux/macOS e Windows).

> Pré-requisitos e instalação por SO: ver **[materiais/MANUAL-AMBIENTE-ALUNO.md](materiais/MANUAL-AMBIENTE-ALUNO.md)**.

---

## 0. Instalar os pré-requisitos (uma vez, na máquina do aluno)

Máquina zerada? Um script instala **Git, Java 17, Maven, Docker e o Eclipse (Enterprise Java)**:

**Windows** (usa o `winget`, que já vem no Windows 10/11 — de preferência num PowerShell "como Administrador"):
```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass   # se necessário
.\scripts\setup.ps1
```

**macOS / Linux** (macOS usa Homebrew; Linux usa apt + sudo):
```bash
chmod +x scripts/setup.sh
./scripts/setup.sh
```

Flags: `--skip-docker`, `--skip-eclipse`, `--skip-maven`, `--java 21` (PowerShell: `-SkipDocker`, `-SkipEclipse`, `-SkipMaven`, `-JavaVersion 21`).
Depois **feche e reabra o terminal** (PATH/JAVA_HOME) e abra o Docker uma vez até "Engine running".

---

## 1. Onde está cada coisa

| O quê | Onde |
|-------|------|
| Aplicação (código) | `portal-pedidos/` (Spring Boot 3, Java 17) |
| Guia do lab (aluno) | `materiais/aula-N/lab.md` |
| Slides do lab | `materiais/aula-N/lab-aula-N.pptx` |
| Gabarito (instrutor) | `materiais/aula-N/gabarito.md` |
| Anexos (fichas, CTF, checklist) | `materiais/aula-N/anexos/` |
| Infra (Docker) | `infra/docker-compose.yml` |
| Scripts de setup (instalar tudo) | `scripts/setup.sh` (macOS/Linux) · `scripts/setup.ps1` (Windows) |
| Scripts de ambiente | `scripts/start.sh` (Linux/macOS) · `scripts/start.ps1` (Windows) |

## 2. Os codebases dos labs são TAGS do Git

A aplicação é única; cada lab é um ponto na história do Git:

| Tag | Uso |
|-----|-----|
| `aula-1-baseline` … `aula-6-baseline` | **ponto de partida** de cada lab (já com as correções das aulas anteriores) |
| `aula-2-hardened` … `aula-6-hardened` | **solução de referência** de cada aula |

Regra: `aula-N-baseline` == `aula-(N-1)-hardened`.

## 3. Subir o ambiente — script único

**Linux/macOS**
```bash
chmod +x scripts/start.sh          # (só na primeira vez)
./scripts/start.sh                 # app + banco (Docker) -> http://localhost:8080
./scripts/start.sh --lab 3         # checkout de aula-3-baseline e sobe
./scripts/start.sh --sonar         # sobe também o SonarQube (Aula 5)
./scripts/start.sh --dev           # só o banco no Docker; app via ./mvnw (editar/debug)
./scripts/start.sh --check         # só verifica pré-requisitos
./scripts/start.sh --stop          # derruba tudo
```

**Windows (PowerShell)**
```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass   # se necessário
.\scripts\start.ps1                 # app + banco (Docker)
.\scripts\start.ps1 -Lab 3          # checkout de aula-3-baseline e sobe
.\scripts\start.ps1 -Sonar          # sobe também o SonarQube
.\scripts\start.ps1 -Dev            # só o banco; app via .\mvnw.cmd
.\scripts\start.ps1 -Check          # só verifica pré-requisitos
.\scripts\start.ps1 -Stop           # derruba tudo
```

- **Modo padrão** (Docker): sobe app + Postgres. Melhor para "só rodar".
- **Modo `--dev`/`-Dev`**: sobe só o banco e roda a app via wrapper Maven — melhor para
  **editar e depurar** no laboratório (hot reload, breakpoints na IDE).

Contas de teste: `admin@portal.com/admin123` · `joao@acme.com/senha123` · `maria@globex.com/senha123`.

## 4. Dinâmica de um laboratório

```
1) ./scripts/start.sh --lab N        # pega o codebase (aula-N-baseline) e sobe a infra
2) Siga materiais/aula-N/lab.md       # explore a falha e aplique a correção
3) Compare com materiais/aula-N/gabarito.md  (ou: git checkout aula-N-hardened)
4) ./scripts/start.sh --stop          # ao terminar
```

## 5. Ferramentas por aula

| Aula | Sobe com | Ferramentas extras |
|------|----------|--------------------|
| 1–4  | `start.sh --lab N` | nenhuma além de app + banco |
| 5    | `start.sh --lab 5 --sonar` | **SonarQube** (:9000, já no compose) + **OWASP ZAP** via Docker |
| 6    | `start.sh --lab 6` | **Dependency-Check** (`./mvnw -Psecurity verify`, requer NVD API key) + **Trivy** via Docker |

Comandos das ferramentas (Aula 5/6):
```bash
# OWASP ZAP (DAST) — Linux
docker run --rm --network=host -t ghcr.io/zaproxy/zaproxy:stable zap-baseline.py -t http://localhost:8080 -r zap.html
# OWASP ZAP — Windows/macOS (use host.docker.internal)
docker run --rm -t ghcr.io/zaproxy/zaproxy:stable zap-baseline.py -t http://host.docker.internal:8080 -r zap.html

# Dependency-Check (SCA) — Aula 6
cd portal-pedidos && ./mvnw -Psecurity verify -DnvdApiKey=SUA_CHAVE   # chave: https://nvd.nist.gov/developers/request-an-api-key

# Trivy (scan de imagem) — Aula 6
docker build -t portal-pedidos:hardened portal-pedidos
docker run --rm aquasec/trivy:latest image portal-pedidos:hardened
```

## 6. Solução de problemas (resumo)

- **Porta 8080/5432/9000 ocupada:** encerre o processo ou pare outros containers.
- **Docker daemon off:** abra o Docker Desktop e aguarde ficar "running".
- **SonarQube não sobe (Linux):** `sudo sysctl -w vm.max_map_count=262144`.
- **Disco cheio:** `docker system prune -a --volumes` e, no macOS, "Clean/Purge data" no Docker Desktop.
- **1º build lento:** normal — baixa dependências/imagens; as próximas execuções usam cache.

Detalhes completos de troubleshooting: `materiais/MANUAL-AMBIENTE-ALUNO.md`.
