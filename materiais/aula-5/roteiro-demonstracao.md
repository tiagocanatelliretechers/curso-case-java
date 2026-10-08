# Aula 5 — Roteiro de Demonstração (INSTRUTOR)

Guia **pronto para demonstrar ao vivo**. Para o Lab 5.1 (código) vale o padrão de sempre:
**falha no `baseline` → `hardened` → provar → mostrar o código.** Para SAST/DAST, a demo é conduzir a
ferramenta e **interpretar** os achados (não é código a corrigir na hora).

Solução do 5.1 na tag `aula-5-hardened` (`GlobalExceptionHandler`, `ImportController`, `application.yml`).

> **Docker:** as VMs dos alunos não têm. Para SAST use **SonarLint** (na IDE) ao vivo; para DAST use **ZAP Desktop**.
> Se a **sua** máquina de instrutor tiver Docker, dá para demonstrar o SonarQube em container como bônus.

## Preparação — o que instalar (uma vez)
- **Todos:** JDK 17 (já das aulas anteriores) + o repo da aula: `git clone https://github.com/tiagocanatelliretechers/curso-case-java-aula-5-sast-dast.git`.
- **SAST (Demo 2):** **SonarLint** na IDE — Eclipse: *Help → Eclipse Marketplace → "SonarQube for IDE"* (ex-SonarLint) → Install → reiniciar. VS Code: extensão *SonarQube for IDE*. **Sem Docker, sem servidor.**
- **DAST (Demo 3):** **OWASP ZAP Desktop** — baixe em <https://www.zaproxy.org/download/> (usa o Java que você já tem). **Sem Docker.**
- **Opcional (só instrutor, se tiver Docker):** SonarQube/ZAP em container (bônus).

```bash
git checkout aula-5-baseline
./scripts/start.sh --lab 5 --no-docker
```
> **Teste antes da aula:** `curl -s http://localhost:8080/api/pedidos/abc` deve vir com stack trace no baseline.

---

## DEMO 1 — Error handling vaza a aplicação  · ~7 min

**O que falar:** "Quando quebra, a aplicação conta demais. Vamos ver o que ela entrega para um estranho."

> Use **`/api/pedidos/abc`** (é público, não precisa de login — ótimo para a tela). O `abc` não é um número, então o endpoint quebra. Na tela compartilhada, basta **abrir essa URL no navegador**.

### 1a) O vazamento (baseline)
```bash
curl -s "http://localhost:8080/api/pedidos/abc"
```
- **Esperado:** HTTP **400** com JSON contendo `"exception"`, **`"trace"`** (stack trace inteira), `"message"`, `"path"`.
- **O que dizer:** "Nomes de classe, caminho, versão de lib — um mapa da aplicação, de graça. E sem precisar estar logado."

### 1b) A correção pronta (hardened)
```bash
git checkout aula-5-hardened && ./scripts/start.sh --lab 5 --no-docker
curl -s "http://localhost:8080/api/pedidos/abc"
```
- **Esperado:** resposta **genérica** — `{"erro":"Ocorreu um erro. Ref: <uuid>"}` (sem `trace`/`exception`); o detalhe vai **para o log do servidor** (mostre o console).
- **Log injection:** mostre que uma URL de import com `\n` não cria mais linhas falsas no log.

### 1c) Mostre o código
Abra **`GlobalExceptionHandler.java`** (resposta genérica + `traceId` no log) e o trecho de limpeza de `\r\n` no **`ImportController`**.
- **Conceito em voz alta:** cliente vê genérico; servidor loga o detalhe com um id de correlação.

---

## DEMO 2 — SAST com SonarLint (sem Docker)  · ~8 min

**O que falar:** "Revisar tudo à mão não escala. O SAST lê o código e aponta padrões perigosos — mas erra. O trabalho é **triar**."

### 2a) Rodando na IDE (ao vivo)
- Com o **SonarLint** instalado, abra alguns arquivos do `portal-pedidos` (ex.: um controller/serviço).
- **O que observar:** avisos na régua lateral / aba *SonarLint*.
- **O que dizer:** "Mesmo motor do SonarQube, rodando local — sem servidor, sem Docker."

### 2b) Triando um finding (o ponto da aula)
Escolha 1 aviso e conduza a triagem em voz alta:
- **Regra** e **severidade**;
- **VP ou FP?** — explique o raciocínio (o caminho é realmente explorável?);
- **Ação:** corrigir agora / suprimir com justificativa / backlog.
Mostre que vários problemas das Aulas 2–4 (SQLi, MD5…) **não aparecem mais** — o código evoluiu.

### 2c) (Bônus, só se você tiver Docker) SonarQube em container
```bash
docker run -d --name sonarqube -p 9000:9000 sonarqube:10-community
./mvnw -q sonar:sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.login=SEU_TOKEN -Dsonar.projectKey=portal-pedidos
```
Mostre o painel (Issues / Security Hotspots) e o conceito de *Quality Gate*.

---

## DEMO 3 — DAST (análise dinâmica)  · ~8 min

**O que falar:** "O SAST viu o código; o DAST vê a aplicação **rodando**, de fora, como um atacante."

> ⚠️ **ZAP bloqueado pelo Windows?** O Defender/SmartScreen costuma marcar o ZAP como "hacktool/PUA" e remover (comum em VM gerenciada). **Não precisa do ZAP** para ensinar o conceito: o DAST acha, na resposta HTTP, coisas como **cabeçalhos de segurança ausentes** — e isso se vê no **navegador (DevTools) ou com curl**. Use o caminho 3a abaixo. O ZAP (3a-alt) fica como opcional/instrutor.

### 3a) DAST manual (sem ZAP) — navegador + curl  ⭐ recomendado
Mostre os achados que um scanner reportaria, direto na resposta HTTP:
- **Na tela (navegador):** F12 → aba **Network** → clique na requisição do documento (`localhost`) → **Response Headers**. Aponte o que **falta**: `Content-Security-Policy`, `X-Frame-Options`, `Strict-Transport-Security`.
- **Ou por linha de comando:**
  - Windows (PowerShell): `curl.exe -sI http://localhost:8080/ | findstr /i "content-security x-frame strict-transport"`
  - Linux/macOS: `curl -sI http://localhost:8080/ | grep -iE 'content-security-policy|x-frame-options|strict-transport'`
- **O que observar:** esses três **não aparecem** → é um achado real de DAST (clickjacking, sem CSP, sem HSTS).
- **O que dizer:** "O scanner automatiza isto; aqui vimos o mesmo na mão. DAST acha pela resposta, sem olhar o código."

### 3a-alt) (Opcional) Scan automático com OWASP ZAP
Se tiver uma máquina onde o ZAP roda (ex.: a sua, de instrutor): **ZAP Desktop** → *Quick Start → Automated Scan* → `http://localhost:8080` → *Attack*. A aba *Alerts* enche com os mesmos achados (e mais). Para instalar sem o instalador bloqueado, use o **pacote ZIP "Cross Platform"** (não o .exe) e rode `zap.bat`.

### 3b) Validar 1 finding (o ponto da aula)
- Já validamos acima com o `curl -I`: o header realmente não existe.
- **O que dizer:** "Nunca confie no relatório sem reproduzir. O valor do analista é triar e validar, não rodar a ferramenta."

### 3c) (Opcional) SAST × DAST
Feche a demo contrastando: **SAST** aponta a linha no código mas erra por falta de contexto; **DAST** confirma em runtime mas não diz onde no código. **Os dois se complementam.**

---

## Encerramento (30s)
Amarre: **erro genérico + log seguro**, **SAST = código (triar VP/FP)**, **DAST = runtime (validar finding)**. Reforce: ferramenta sem interpretação é ruído.

## Erros comuns na hora da demo
- **SonarLint sem avisos:** projeto não importado como Maven, ou nenhum arquivo aberto (a análise é por arquivo aberto).
- **ZAP não acha o alvo:** app precisa estar em `http://localhost:8080`; sem proxy no meio.
- **`/api/pedidos/abc` já genérico no baseline:** confira que está mesmo no `aula-5-baseline` (não no hardened).
- **302 em vez de erro:** você usou `/pedidos/abc` (web, exige login). Use **`/api/pedidos/abc`** (público).
