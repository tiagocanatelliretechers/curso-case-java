# Manual de Preparação de Ambiente e Execução — Aluno
## Curso CASE Java · Portal de Pedidos B2B

Este guia prepara sua máquina para os laboratórios do curso. **Faça isto antes da Aula 1.**
Funciona em **Linux, Windows e macOS** — os comandos aparecem lado a lado quando mudam.

> ⚠️ A aplicação é um ambiente de treinamento e contém vulnerabilidades reais de propósito.
> **Não a exponha na internet** nem use dados reais.

---

## 0. Em qual sistema operacional rodar?

Tanto faz — escolha o que você já usa:

- **Linux** (Ubuntu/Debian/Fedora/WSL2): caminho mais direto, tudo por linha de comando.
- **Windows 10/11**: funciona com Docker Desktop; comandos no **PowerShell**. (Opcional: usar **WSL2** e seguir as instruções de Linux.)
- **macOS**: igual ao Linux (terminal `bash`/`zsh`).

Há dois jeitos de rodar a aplicação, e você pode usar qualquer um:
- **Opção A — Local (H2):** só precisa de JDK + o wrapper do Maven. Mais rápido.
- **Opção B — Docker (PostgreSQL):** sobe app + banco com um comando. Mais próximo de produção.

---

## 1. Pré-requisitos (todas as aulas)

| Ferramenta | Versão | Para quê |
|---|---|---|
| **JDK** | 17 ou superior | compilar e rodar a aplicação |
| **Git** | recente | baixar o projeto e trocar de branch por lab |
| **Docker + Docker Compose** | atual | subir app+banco e as ferramentas das Aulas 5 e 6 |
| **IDE Java** | IntelliJ / VS Code / Eclipse | editar e depurar |
| **Postman ou Insomnia** (ou `curl`) | — | exercitar a API e reproduzir ataques |

> **Maven não precisa instalar** — o projeto traz `mvnw` (Linux/macOS) e `mvnw.cmd` (Windows).
> Na 1ª execução ele baixa dependências, então é preciso **internet**.

---

## 2. Instalação por sistema operacional

### 2.1 JDK 17 (Eclipse Temurin)

**Windows (PowerShell como administrador):**
```powershell
winget install EclipseAdoptium.Temurin.17.JDK
```
**Linux (Ubuntu/Debian):**
```bash
sudo apt update && sudo apt install -y temurin-17-jdk || sudo apt install -y openjdk-17-jdk
```
**macOS (Homebrew):**
```bash
brew install --cask temurin@17
```
> Alternativa multiplataforma: instalar via [SDKMAN](https://sdkman.io) → `sdk install java 17.0.11-tem`.

### 2.2 Git
- **Windows:** `winget install Git.Git`
- **Linux:** `sudo apt install -y git`
- **macOS:** `brew install git` (ou já vem com o Xcode Command Line Tools)

### 2.3 Docker
- **Windows/macOS:** instale o **Docker Desktop** (site oficial). No Windows, habilite o backend **WSL2**.
- **Linux:** instale o **Docker Engine** + plugin **compose** (`docker compose version` deve funcionar).
- Nos labs pesados (SonarQube), reserve **~4 GB de RAM** para o Docker (Settings → Resources no Desktop).

### 2.4 IDE e cliente de API
- IDE: IntelliJ IDEA (Community serve), VS Code (+ "Extension Pack for Java") ou Eclipse.
- Cliente de API: Postman ou Insomnia. `curl` também é usado nos guias.

---

## 3. Obter o projeto

Se você recebeu um link do repositório:
```bash
git clone <URL-DO-REPOSITORIO>
cd portal-pedidos
```
Se recebeu um arquivo `.zip`, descompacte e entre na pasta `portal-pedidos`.

---

## 4. Verificar o ambiente (rode antes da Aula 1)

**Linux/macOS (bash):**
```bash
java -version        # deve mostrar 17 (ou superior)
git --version
docker --version && docker compose version
```
**Windows (PowerShell):**
```powershell
java -version
git --version
docker --version ; docker compose version
```
Se todos responderem sem erro, seu ambiente está pronto.

---

## 5. Rodar a aplicação

### Opção A — Local com H2 (sem Docker)

**Linux/macOS:**
```bash
./mvnw spring-boot:run
```
**Windows (PowerShell):**
```powershell
.\mvnw.cmd spring-boot:run
```
Acesse: <http://localhost:8080>

### Opção B — Docker Compose (app + PostgreSQL)
```bash
docker compose up --build
```
Acesse: <http://localhost:8080> · Para parar: `Ctrl+C` e depois `docker compose down`.

### Contas de teste

| Papel | E-mail | Senha |
|---|---|---|
| Admin | `admin@portal.com` | `admin123` |
| Cliente (ACME) | `joao@acme.com` | `senha123` |
| Cliente (Globex) | `maria@globex.com` | `senha123` |

---

## 6. Fluxo dos laboratórios (branches/tags)

Cada aula começa de um ponto específico do código. O guia de cada aula indica a tag.
```bash
git checkout aula-1-baseline    # ponto de partida (aplicação vulnerável)
# ... faça o laboratório ...
git checkout aula-3-baseline    # início da Aula 3 (correções anteriores já aplicadas)
```
> Dica: antes de trocar de tag, salve seu trabalho (`git stash` ou um commit numa branch sua).
> Para ver a **solução de referência** de tudo: `git checkout solucao-hardened`.

---

## 7. Segredos (a partir da Aula 4 / branch hardened)

A versão endurecida lê a chave de cifra e o segredo do JWT de **variáveis de ambiente**.
Há valores DEV padrão no `application.yml` (a app sobe sem configurar nada), mas o ideal é definir os seus:

**Linux/macOS (bash):**
```bash
export PORTAL_JWT_SECRET=$(head -c 32 /dev/urandom | base64)
export PORTAL_CRYPTO_KEY=$(head -c 32 /dev/urandom | base64)
```
**Windows (PowerShell):**
```powershell
$bytes = New-Object 'Byte[]' 32; (New-Object Security.Cryptography.RNGCryptoServiceProvider).GetBytes($bytes)
$env:PORTAL_JWT_SECRET = [Convert]::ToBase64String($bytes)
$bytes2 = New-Object 'Byte[]' 32; (New-Object Security.Cryptography.RNGCryptoServiceProvider).GetBytes($bytes2)
$env:PORTAL_CRYPTO_KEY = [Convert]::ToBase64String($bytes2)
```
> A variável vale só para o terminal atual. Rode a aplicação **no mesmo terminal** onde exportou.

---

## 8. Ferramentas específicas por aula

### Aula 5 — SAST e DAST

**SonarQube (SAST) — via Docker:**
```bash
docker run -d --name sonarqube -p 9000:9000 sonarqube:10-community
```
Acesse <http://localhost:9000> (login `admin`/`admin`, troque a senha). Crie um token e rode:
```bash
# Linux/macOS
./mvnw sonar:sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.login=SEU_TOKEN -Dsonar.projectKey=portal-pedidos
```
```powershell
# Windows
.\mvnw.cmd sonar:sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.login=SEU_TOKEN -Dsonar.projectKey=portal-pedidos
```

**OWASP ZAP (DAST) — via Docker:**
```bash
# Linux
docker run --rm --network=host -t ghcr.io/zaproxy/zaproxy:stable zap-baseline.py -t http://localhost:8080 -r zap.html
```
```bash
# Windows/macOS (não use --network=host; aponte para host.docker.internal)
docker run --rm -t ghcr.io/zaproxy/zaproxy:stable zap-baseline.py -t http://host.docker.internal:8080 -r zap.html
```

### Aula 6 — Dependências e container

**OWASP Dependency-Check (SCA):**
```bash
./mvnw -Psecurity verify        # Windows: .\mvnw.cmd -Psecurity verify
```
> ⚠️ **Importante:** o Dependency-Check baixa a base de vulnerabilidades do NVD e, na versão atual,
> **exige uma NVD API key** para não ser limitado por rate. Obtenha uma chave gratuita em
> `https://nvd.nist.gov/developers/request-an-api-key` e rode com:
> ```bash
> ./mvnw -Psecurity verify -DnvdApiKey=SUA_CHAVE
> ```
> A **primeira** execução baixa muitos dados e pode demorar vários minutos (as seguintes usam cache).

**Trivy (scan de imagem) — via Docker:**
```bash
docker build -t portal-pedidos:hardened .
docker run --rm aquasec/trivy:latest image portal-pedidos:hardened
```

---

## 9. Portas usadas

| Porta | Serviço | Quando |
|---|---|---|
| 8080 | Aplicação | todas as aulas |
| 5432 | PostgreSQL | ao usar Docker Compose |
| 9000 | SonarQube | Aula 5 |

Deixe essas portas livres antes de começar.

---

## 10. Solução de problemas (por sintoma)

**"Port 8080 already in use" / porta ocupada**
- Linux/macOS: `lsof -ti:8080 | xargs kill` (descobre e encerra o processo na 8080).
- Windows (PowerShell): `Get-NetTCPConnection -LocalPort 8080` e depois `Stop-Process -Id <PID>`.

**`./mvnw` não executa (Linux/macOS)**
- Dê permissão: `chmod +x mvnw` e rode `./mvnw ...`.

**No Windows o `mvnw.cmd` reclama de quebra de linha / não roda**
- Rode `.\mvnw.cmd ...` no PowerShell (não no Git Bash). Se usar Git Bash, prefira `./mvnw`.

**ZAP não alcança a aplicação**
- No Windows/macOS use `http://host.docker.internal:8080` (não `localhost`, nem `--network=host`).

**SonarQube não sobe / cai sozinho**
- Falta de memória: aumente a RAM do Docker para ~4 GB (Docker Desktop → Settings → Resources).

**Dependency-Check muito lento ou com erro de download**
- Configure a **NVD API key** (seção 8) e tenha paciência no 1º run (usa cache depois).

**Download do Maven falha atrás de proxy corporativo**
- Configure o proxy no `~/.m2/settings.xml` (bloco `<proxies>`), conforme a documentação da sua empresa.

**Docker "permission denied" no Linux**
- Adicione seu usuário ao grupo docker: `sudo usermod -aG docker $USER` e reabra a sessão.

---

## 11. Checklist final (antes da Aula 1)

- [ ] `java -version` mostra 17+
- [ ] `git --version` funciona
- [ ] `docker --version` e `docker compose version` funcionam
- [ ] Consegui subir a aplicação (Opção A **ou** B) e abrir <http://localhost:8080>
- [ ] Consegui fazer login com `joao@acme.com` / `senha123`
- [ ] IDE e Postman/Insomnia instalados

Pronto! Traga suas dúvidas — na Aula 1 você sobe a aplicação e faz seu primeiro reconhecimento.
