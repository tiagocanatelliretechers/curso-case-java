# Aula 6 — Guia de Laboratório final (aluno)
## Deploy Seguro Ponta a Ponta

**Ponto de partida:** `aula-6-baseline` (app já corrigida nas Aulas 2–5, mas com: Actuator/H2 abertos, dependência com CVE, Dockerfile inseguro, CI sem gates).
**Ambiente:** JDK 17 + IDE. Subir o app: `./scripts/start.sh --lab 6 --no-docker` (ou `-Lab 6 -NoDocker`).

> **Docker:** não é necessário para o aluno. Dependency-Check roda pelo Maven; o Trivy escaneia por **binário nativo** (`trivy fs` / `trivy config`). O build+scan da imagem (`docker build` + `trivy image`) é **opcional/instrutor**.

---

## Como usar este guia (leia antes de começar)

Esta é a aula de **colocar em produção sem abrir brechas**. O objetivo não é "rodar o comando" — é entender *que risco cada configuração fecha*. Se não conseguir explicar o porquê, **releia o "Conceito em 1 minuto" ou chame o instrutor**.

Cada exercício tem: **Por que fazemos isto** · **Conceito em 1 minuto** · **Passo a passo** (com *o que observar* / *o que significa*) · **Ponto de entendimento** · **Se der errado** · **Conecte com a teoria**.

## Mapa: conceito → laboratório

| Conceito (slides) | Risco no deploy | Lab | O que você vai provar |
|-------------------|-----------------|-----|-----------------------|
| Superfície de exposição em produção | Actuator/H2 abertos vazam segredos | 6.1 | Que `/actuator/env` entrega segredos — e como fechar. |
| SCA (dependências vulneráveis) | `commons-text` 1.9 (Text4Shell) | 6.2 | Que uma lib com CVE entra pela porta dos fundos — e como detectar/atualizar. |
| Imagem de container segura | Dockerfile root, com segredo, "gordo" | 6.3 | Que a imagem tem riscos — e como fazer multi-stage/não-root. |
| Segurança no pipeline (shift-left) | CI sem gates | 6.4 | Que dá para barrar o crítico **antes** do merge. |

## Objetivos de aprendizagem
Ao final você deve conseguir **explicar**:
1. Por que endpoints de gestão (Actuator) e o H2 console não podem ficar abertos em produção.
2. O que é **SCA** e por que uma dependência vulnerável é tão perigosa quanto um bug seu.
3. O que torna uma imagem de container mais segura (multi-stage, não-root, sem segredo em layer).
4. O que é um **gate de segurança** no CI e por que "shift-left" reduz custo de correção.

---

## Lab 6.1 — Proteger o Actuator (e o H2 console) (20 min)

**Por que fazemos isto:** o Spring Boot Actuator é ótimo para operar a app — e perigoso se exposto. `/actuator/env` lista **variáveis de ambiente e configs** (incluindo segredos). Em produção, isso é um vazamento pronto. O H2 console aberto é acesso direto ao banco.

**Conceito em 1 minuto:** em produção, **exponha o mínimo**: só `health`/`info`, sem detalhes; tudo mais fechado ou atrás de autenticação. É o princípio de **superfície mínima de ataque** aplicado à operação. O H2 console é ferramenta de dev — nunca vai para produção.

**Passo 0 — reproduza (baseline):**
```bash
curl -s -o /dev/null -w '%{http_code}\n' http://localhost:8080/actuator/env    # 200 = exposto
curl -s http://localhost:8080/actuator/env | head -c 300                        # veja o que vaza
```
- *O que observar:* 200 e um dump de configuração/variáveis.
- *O que isso significa:* qualquer um lê a "planta" da aplicação e possivelmente segredos.

**Passo a passo (correção):**
1. Restrinja a exposição no `application.yml`:
   ```yaml
   management.endpoints.web.exposure.include: health,info
   management.endpoint.health.show-details: never
   ```
2. Proteja o que sobrar com autenticação e desabilite o H2 console:
   ```yaml
   spring.h2.console.enabled: false
   ```
   ```java
   .requestMatchers("/actuator/**").hasRole("ADMIN")   // no SecurityConfig
   ```
3. Reteste:
   - *O que observar:* `/actuator/env` → **401/404**; `/actuator/health` → ok; `/h2-console` indisponível.
   - *O que isso significa:* a superfície de exposição encolheu para o essencial.

**Ponto de entendimento:** *Por que `/actuator/env` é especialmente perigoso?*
> Resposta esperada: lista variáveis/props de configuração — inclusive segredos e detalhes de infraestrutura — dando reconhecimento e, às vezes, credenciais.

**Se der errado:**
- `/actuator/health` também bloqueou → você fechou demais; mantenha `health,info` no `include`.
- Ainda expõe → o `exposure.include` antigo (`*`) continua no perfil ativo.

**Conecte com a teoria:** slides "Superfície mínima em produção" e "Actuator/H2 seguros".

---

## Lab 6.2 — SCA: Dependency-Check e a dependência vulnerável (25 min)

**Por que fazemos isto:** a maior parte do código que você entrega **não é seu** — são bibliotecas. Uma lib com CVE conhecido é uma porta aberta que você nem escreveu. SCA (*Software Composition Analysis*) encontra isso automaticamente.

**Conceito em 1 minuto:** SCA lê suas dependências (o "bill of materials") e cruza com bancos de vulnerabilidades (NVD). Se uma versão tem CVE conhecido, ele aponta. O Portal usa **`commons-text` 1.9**, afetada pelo **CVE-2022-42889 ("Text4Shell")**. Corrigir é atualizar para uma versão sã — e, idealmente, **falhar o build** se aparecer algo crítico.

**Passo a passo:**
1. Rode o Dependency-Check (perfil `security` já no `pom.xml`):
   ```bash
   ./mvnw -Psecurity verify -DnvdApiKey=SUA_CHAVE
   ```
   > A chave da NVD (grátis) acelera muito: <https://nvd.nist.gov/developers/request-an-api-key>. Sem ela, o primeiro download da base é lento.
   Abra `target/dependency-check-report.html`.
   - *O que observar:* um finding para `commons-text` 1.9 (CVE-2022-42889).
   - *O que isso significa:* uma dependência transitiva/direta traz um risco crítico conhecido.
2. Atualize no `pom.xml` para uma versão corrigida (ex.: `1.10.0`+).
3. Rode de novo:
   - *O que observar:* o CVE não aparece mais (ou o build deixa de falhar no gate de CVSS).

**Ponto de entendimento:** *Por que uma dependência vulnerável é tão grave quanto um bug no seu próprio código?*
> Resposta esperada: ela roda com os mesmos privilégios da sua aplicação; um CVE explorável nela compromete o sistema igual a uma falha sua — e você pode nem saber que a incluiu (transitiva).

**Se der errado:**
- Download eterno / timeout → falta a `nvdApiKey` (a base do NVD é grande); peça a chave.
- CVE persiste após atualizar → a versão antiga entrou como **transitiva** de outra lib; force a versão em `dependencyManagement`.

**Conecte com a teoria:** slides "SCA e o supply chain", "CVE/CVSS" e "Text4Shell (CVE-2022-42889)".

---

## Lab 6.3 — Hardening do Dockerfile + Trivy (25 min)

**Por que fazemos isto:** a forma como você empacota a app importa. Uma imagem que roda como **root**, carrega **segredos em layer** ou traz um SO inteiro amplia muito o estrago de qualquer falha.

**Conceito em 1 minuto:** boas práticas de imagem: **multi-stage** (compila num stage, a imagem final leva só o JRE + o jar — menor e sem ferramentas de build), **usuário não-root**, **nenhum segredo em `ENV`/layer** (passe em runtime), e **base mínima** (ex.: `eclipse-temurin:17-jre`). O **Trivy** escaneia imagem, filesystem e o próprio Dockerfile em busca de CVEs e más configurações.

**Passo a passo:**
1. Revise/reescreva o `Dockerfile` (compare com o hardened):
   - multi-stage (build → runtime só com JRE + jar);
   - `USER` não-root;
   - sem segredo em `ENV`;
   - base mínima (`eclipse-temurin:17-jre`).
   - *O que isso significa:* menor superfície, menos privilégio, sem segredo vazado no histórico de layers.

2. **Sem Docker (nas VMs)** — escaneie com o **Trivy nativo** (`choco install trivy` / binário do site):
   ```bash
   trivy config .        # analisa o Dockerfile em busca de más configurações
   trivy fs .            # analisa dependências/arquivos do projeto
   ```
   - *O que observar:* alertas de config (ex.: roda como root, sem HEALTHCHECK) e CVEs de dependência.

3. **Com Docker (opcional/instrutor):** build + scan da imagem:
   ```bash
   docker build -t portal-pedidos:hardened .
   docker run --rm aquasec/trivy:latest image portal-pedidos:hardened
   docker history portal-pedidos:hardened   # confirme que não há segredo em layer
   ```

**Ponto de entendimento:** *Por que rodar o container como não-root reduz o risco?*
> Resposta esperada: se um atacante executar código dentro do container, começa sem privilégios de root — dificultando escapar do container ou tocar em recursos sensíveis.

**Se der errado:**
- `trivy` não encontrado → instale o binário nativo (não precisa de Docker para `config`/`fs`).
- Muitos CVEs na base → foque nos **críticos/altos evitáveis**; nem todo CVE tem correção aplicável.

**Conecte com a teoria:** slides "Imagem de container segura (multi-stage, não-root)" e "Trivy — image/fs/config".

---

## Lab 6.4 — Pipeline CI com gates de segurança (20 min)

**Por que fazemos isto:** achar a falha em produção é caro; achar no **pull request** é barato. "Shift-left" é mover a segurança para cedo no ciclo, automatizada, de forma que um crítico **não passe** do merge.

**Conceito em 1 minuto:** um pipeline de CI roda a cada commit/PR. Adicionamos **gates**: build+test, **SAST** (Aula 5) e **SCA** (Dependency-Check). Configuramos o pipeline para **falhar** diante de um finding crítico — assim a proteção não depende de alguém lembrar de rodar.

**Passo a passo:**
1. Crie um workflow (GitHub Actions **ou** GitLab CI) com 3 estágios:
   ```yaml
   # exemplo (GitHub Actions, resumido)
   - run: ./mvnw verify                 # build + testes
   - run: ./mvnw -Psecurity verify      # SCA (Dependency-Check) — falha em CVSS alto
   # + passo de SAST (SonarQube/Semgrep) da Aula 5
   ```
2. Configure o gate para **falhar** o build em finding crítico (ex.: `failBuildOnCVSS`).
3. Faça um commit que reintroduza uma dependência vulnerável (ex.: voltar `commons-text` para 1.9).
   - *O que observar:* o pipeline **falha** no estágio de SCA.
   - *O que isso significa:* o crítico foi barrado **antes** de chegar à main — sem depender de disciplina humana.

**Ponto de entendimento:** *Por que "shift-left" reduz o custo de corrigir?*
> Resposta esperada: quanto mais cedo o problema é pego (no PR, não em produção), menor o retrabalho, o raio de impacto e o custo — e a correção ainda está fresca na cabeça de quem escreveu.

**Se der errado:**
- Pipeline passa mesmo com CVE → o gate (`failBuildOnCVSS`) não está configurado para falhar.
- Não tem CI real disponível → simule localmente rodando os mesmos comandos numa ordem e tratando a falha do Maven como "gate".

**Conecte com a teoria:** slides "Shift-left & DevSecOps" e "Gates de segurança no CI".

---

## Autoavaliação (responda sem olhar o gabarito)
1. Por que `/actuator/env` aberto é um risco sério em produção?
2. O que é SCA e por que dependência vulnerável é tão grave quanto bug próprio?
3. Cite três práticas que tornam uma imagem de container mais segura.
4. O que é um gate de segurança no CI e por que "shift-left" compensa?

## Entrega
- Actuator/H2 protegidos; dependência atualizada (sem o CVE); Dockerfile hardened + scan (Trivy); pipeline com gates.
- Uma frase por lab respondendo ao "Ponto de entendimento".
- Em seguida: **CTF** (`anexos/ctf.md`) e **Simulado final** (`../simulado-final.md`).

## Onde pedir ajuda / erros comuns gerais
- **Sem Docker?** É o caso das VMs — `trivy config`/`trivy fs` (nativo) cobrem o essencial; o build da imagem é opcional/instrutor.
- **Dependency-Check lento?** Peça a NVD API key.
- **Comparar com a solução:** `git checkout aula-6-hardened`.
- **Travou num conceito?** Não avance "só para entregar" — chame o instrutor.
