# Aula 5 — Roteiro de Demonstração (INSTRUTOR)

Guia **pronto para demonstrar ao vivo**. Para o Lab 5.1 (código) vale o padrão de sempre:
**falha no `baseline` → `hardened` → provar → mostrar o código.** Para SAST/DAST, a demo é conduzir a
ferramenta e **interpretar** os achados (não é código a corrigir na hora).

Solução do 5.1 na tag `aula-5-hardened` (`GlobalExceptionHandler`, `ImportController`, `application.yml`).

> **Docker:** as VMs dos alunos não têm. Para SAST use **SonarLint** (na IDE) ao vivo; para DAST use **ZAP Desktop**.
> Se a **sua** máquina de instrutor tiver Docker, dá para demonstrar o SonarQube em container como bônus.

## Preparação
```bash
git checkout aula-5-baseline
./scripts/start.sh --lab 5 --no-docker
```

---

## DEMO 1 — Error handling vaza a aplicação  · ~7 min

**O que falar:** "Quando quebra, a aplicação conta demais. Vamos ver o que ela entrega para um estranho."

### 1a) O vazamento (baseline)
```bash
curl -s "http://localhost:8080/pedidos/abc"
```
- **Esperado:** um **stack trace / mensagem de exceção** na resposta.
- **O que dizer:** "Nomes de classe, query, caminho, versão de lib — um mapa da aplicação, de graça."

### 1b) A correção pronta (hardened)
```bash
git checkout aula-5-hardened && ./scripts/start.sh --lab 5 --no-docker
curl -s "http://localhost:8080/pedidos/abc"
```
- **Esperado:** resposta **genérica** (ex.: `{"erro":"Ocorreu um erro","traceId":"..."}`); o detalhe está **no log do servidor** (mostre o console).
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

## DEMO 3 — DAST com OWASP ZAP Desktop (sem Docker)  · ~8 min

**O que falar:** "O SAST viu o código; o DAST vê a aplicação **rodando**, de fora, como um atacante."

### 3a) Scan automático
- Abra o **ZAP Desktop** → *Quick Start → Automated Scan* → alvo `http://localhost:8080` → *Attack*.
- **O que observar:** a aba *Alerts* enche (ex.: headers de segurança ausentes, cookies).
- **O que dizer:** "Ele nem viu o código — achou pela resposta HTTP."

### 3b) Validar 1 finding (o ponto da aula)
- Pegue um alerta (ex.: header ausente) e **valide manualmente**:
```bash
curl -sI http://localhost:8080/ | grep -iE 'content-security-policy|x-frame-options|strict-transport'
```
- **O que dizer:** "Confirma que o header realmente não existe. Nunca confie no relatório sem validar."

### 3c) (Opcional) SAST × DAST
Feche a demo contrastando: **SAST** aponta a linha no código mas erra por falta de contexto; **DAST** confirma em runtime mas não diz onde no código. **Os dois se complementam.**

---

## Encerramento (30s)
Amarre: **erro genérico + log seguro**, **SAST = código (triar VP/FP)**, **DAST = runtime (validar finding)**. Reforce: ferramenta sem interpretação é ruído.

## Erros comuns na hora da demo
- **SonarLint sem avisos:** projeto não importado como Maven, ou nenhum arquivo aberto (a análise é por arquivo aberto).
- **ZAP não acha o alvo:** app precisa estar em `http://localhost:8080`; sem proxy no meio.
- **`/pedidos/abc` já genérico no baseline:** confira que está mesmo no `aula-5-baseline` (não no hardened).
