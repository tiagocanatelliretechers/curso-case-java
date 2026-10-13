# Aula 6 — Guia de Laboratório final (aluno)
## Deploy Seguro Ponta a Ponta

**Ponto de partida:** `aula-6-baseline` (app já corrigida nas Aulas 2–5, mas com: Actuator/H2 abertos, dependência com CVE, Dockerfile inseguro, CI sem gates).
**Ambiente:** JDK 17 + IDE. Subir o app: `./scripts/start.sh --lab 6 --no-docker` (ou `-Lab 6 -NoDocker`).

> **Docker:** não é necessário. Em sala, o **Lab 6.2** (SCA) roda pelo Maven (Dependency-Check, só precisa da NVD key). Os **Labs 6.3 e 6.4 saíram da máquina**: viram **Extra na nuvem** (GitHub Actions + Dependabot + Trivy), feitos por fork — sem Docker e sem Trivy local.

---

## Como usar este guia (leia antes de começar)

Esta é a aula de **colocar em produção sem abrir brechas**. O objetivo não é "rodar o comando" — é entender *que risco cada configuração fecha*. Se não conseguir explicar o porquê, **releia o "Conceito em 1 minuto" ou chame o instrutor**.

Cada exercício tem: **Por que fazemos isto** · **Conceito em 1 minuto** · **Passo a passo** (com *o que observar* / *o que significa*) · **Ponto de entendimento** · **Se der errado** · **Conecte com a teoria**.

## Mapa: conceito → laboratório

| Conceito (slides) | Risco no deploy | Lab | O que você vai provar |
|-------------------|-----------------|-----|-----------------------|
| Superfície de exposição em produção | Actuator/H2 abertos vazam segredos | 6.1 · **sala** | Que `/actuator/env` entrega segredos — e como fechar. |
| SCA (dependências vulneráveis) | `commons-text` 1.9 (Text4Shell) | 6.2 · **sala** | Que uma lib com CVE entra pela porta dos fundos — e como detectar/atualizar. |
| Imagem de container segura | Dockerfile root, com segredo, "gordo" | 6.3 · **Extra (nuvem)** | Que a imagem tem riscos — e como fazer multi-stage/não-root. |
| Segurança no pipeline (shift-left) | CI sem gates | 6.4 · **Extra (nuvem)** | Que dá para barrar o crítico **antes** do merge. |

> **Estrutura desta aula:** os Labs **6.1 e 6.2 são hands-on em sala** (na sua máquina, com a app rodando). Os Labs **6.3 e 6.4 são "Extra — pós-aula", feitos na NUVEM** (GitHub Actions + Dependabot + Trivy), sem instalar nada. Em sala eles entram como **demonstração do instrutor**; você refaz depois, no seu ritmo, por **fork** do repositório (passo a passo na seção Extra, abaixo, e em `DEMO-NUVEM.md`).

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
   ./mvnw -Psecurity verify -DnvdApiKey=COLE_SUA_CHAVE
   ```
   > **A chave é pessoal e gratuita** — cada um gera a sua (atrelada ao seu e-mail) em
   > <https://nvd.nist.gov/developers/request-an-api-key>. **Não há "chave do curso".**
   > Peça a chave **antes da aula** e, se possível, **rode uma vez** para já baixar a base
   > do NVD (a 1ª execução sem cache é lenta). Sem chave funciona, mas demora bem mais.
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

## ⭐ EXTRA (pós-aula, na NUVEM) — Labs 6.3 e 6.4 sem instalar nada

> Em sala, estes dois você verá como **demonstração** (o instrutor na tela). **Refaça depois**,
> no seu ritmo, pelo seu **fork** no GitHub — roda tudo nos servidores do GitHub (Actions),
> **sem Trivy, sem Docker, sem NVD key na sua máquina**. Roteiro detalhado em `DEMO-NUVEM.md`.

**Como preparar seu fork (uma vez, ~2 min):**
1. Abra o repositório da aula no GitHub → botão **Fork** (precisa de conta GitHub, grátis).
2. No seu fork: aba **Actions** → **"I understand my workflows, go ahead and enable them"**.
3. (Para o 6.3-Extra ver as CVEs) *Settings → Advanced Security* → ligue **Dependabot alerts**.

### Lab 6.3-Extra — Imagem/dependências seguras (Trivy na nuvem)

**Por que fazemos isto:** a forma como você empacota a app importa — rodar como **root**,
**segredo em layer** ou imagem "gorda" amplia qualquer falha. E dá para escanear **sem instalar nada**.

**Conceito em 1 minuto:** boas práticas de imagem: **multi-stage**, **usuário não-root**,
**nenhum segredo em `ENV`/layer**, **base mínima** (`eclipse-temurin:17-jre`). O **Trivy**
(rodando no pipeline da nuvem) escaneia dependências e o próprio Dockerfile em busca de CVEs e más configs.

**Passo a passo (no navegador):**
1. No seu fork, aba **Actions** → workflow **security-gate** → execução da branch `main`.
2. Abra o job **"Trivy — dependências + Dockerfile"** → passo **"relatório completo"**. Observe:
   - `commons-text 1.9 · CVE-2022-42889 (Text4Shell) · CRITICAL`;
   - `Dockerfile · AVD-DS-0002 (HIGH)` → *"sem `USER` = roda como root"*.
3. Compare com a branch **`hardened`** (o mesmo job fica **verde**) e abra o **`portal-pedidos/Dockerfile`**:
   multi-stage, `USER appuser`, base `-jre`, sem segredo.
   - *O que isso significa:* menor superfície, menos privilégio, sem segredo no histórico de layers.
4. **SCA nativo:** aba **Security → Dependabot** mostra a Text4Shell sozinha; em **Pull requests**
   está o PR automático que o GitHub abriu para subir o `commons-text`.

**Ponto de entendimento:** *Por que rodar o container como não-root reduz o risco?*
> Resposta esperada: se um atacante executar código no container, começa **sem** privilégios de root — dificultando escapar do container ou tocar em recursos sensíveis.

### Lab 6.4-Extra — Gate de segurança no CI (shift-left)

**Por que fazemos isto:** achar a falha em produção é caro; achar no **pull request** é barato.
"Shift-left" é mover a segurança para cedo, **automatizada**, para o crítico **não passar** do merge.

**Conceito em 1 minuto:** o pipeline roda a cada push/PR. O workflow `security.yml` tem um **gate**
configurado para **falhar** em `HIGH/CRITICAL` (`--exit-code 1`). Assim a proteção não depende de disciplina humana.

**Passo a passo (no navegador):**
1. Na aba **Actions** do seu fork, veja **lado a lado**: ✘ vermelho no `main`, ✔ verde no `hardened`.
   *"Mesmo pipeline; a diferença é só o código."*
2. Abra o passo **"Gate — reprova em HIGH/CRITICAL"** do `main`: o build **falhou** por causa do crítico.
   Veja no `.github/workflows/security.yml` a linha `severity: HIGH,CRITICAL` + `--exit-code 1`.
3. **Prova viva:** abra um **Pull Request** no seu fork (ex.: `hardened` → `main`) e acompanhe o
   **check do security-gate** rodando no PR — vermelho enquanto houver o crítico, verde depois.
   - *O que isso significa:* o crítico é barrado **antes** do merge, sem depender de ninguém lembrar.

**Ponto de entendimento:** *Por que "shift-left" reduz o custo de corrigir?*
> Resposta esperada: quanto mais cedo o problema é pego (no PR, não em produção), menor o retrabalho, o raio de impacto e o custo — e a correção ainda está fresca na cabeça de quem escreveu.

**Se der errado (Extra):**
- Actions não roda no fork → faltou o passo 2 (habilitar workflows no fork).
- Aba Security vazia → ligue os **Dependabot alerts** em *Settings → Advanced Security*.
- Quer o caminho local (opcional, com Trivy na máquina)? Está em `roteiro-demonstracao.md` (seção "LOCAL").

**Conecte com a teoria:** slides "Imagem de container segura", "Trivy", "Shift-left & DevSecOps" e "Gates de segurança no CI".

---

## Autoavaliação (responda sem olhar o gabarito)
1. Por que `/actuator/env` aberto é um risco sério em produção?
2. O que é SCA e por que dependência vulnerável é tão grave quanto bug próprio?
3. Cite três práticas que tornam uma imagem de container mais segura.
4. O que é um gate de segurança no CI e por que "shift-left" compensa?

## Entrega
- **Em sala (6.1 e 6.2):** Actuator/H2 protegidos e dependência atualizada (sem o CVE), com uma
  frase por lab respondendo ao "Ponto de entendimento".
- **Extra pós-aula (6.3 e 6.4, na nuvem):** print do seu fork mostrando o **gate vermelho no `main`
  e verde no `hardened`**, e um print da aba **Security/Dependabot** com a Text4Shell. (Opcional, mas recomendado.)
- Em seguida: **CTF** (`anexos/ctf.md`) e **Simulado final** (`../simulado-final.md`).

## Onde pedir ajuda / erros comuns gerais
- **NVD key (Lab 6.2):** é **pessoal e gratuita** — gere a sua e, se possível, pré-rode uma vez (a base do NVD é grande).
- **Extra na nuvem (6.3/6.4):** não precisa de Docker/Trivy local — roda no Actions do seu fork; lembre de **habilitar os workflows** no fork.
- **Comparar com a solução:** `git checkout aula-6-hardened`.
- **Travou num conceito?** Não avance "só para entregar" — chame o instrutor.
