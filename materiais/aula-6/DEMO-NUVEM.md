# Aula 6 na NUVEM — Demos 3 e 4 sem instalar nada (GitHub Actions)

Caminho **recomendado** para SCA, imagem/Dockerfile e gate de CI: tudo roda na nuvem do
GitHub. **Ninguém instala Trivy, Docker, Maven ou NVD key.** Você demonstra compartilhando
a tela; quem tiver conta no GitHub pode **dar fork e rodar no próprio** (opcional).

Repositório da aula: **`tiagocanatelliretechers/curso-case-java-aula-6-deploy-seguro`**
(`main` = baseline vulnerável · `hardened` = solução).

O que já está configurado no repo:
- **`.github/workflows/security.yml`** — roda o **Trivy** (via imagem Docker, na nuvem) a cada
  push/PR e **reprova o build em HIGH/CRITICAL**. Validado:
  - `main` (baseline) → 🔴 **vermelho**: `commons-text 1.9 / CVE-2022-42889 (Text4Shell) CRITICAL`
    + Dockerfile `AVD-DS-0002 (HIGH): rodar como root`.
  - `hardened` → 🟢 **verde**: nada HIGH/CRITICAL.
- **`.github/dependabot.yml`** — **SCA nativo**: alertas na aba *Security* + **PR automático**
  de atualização (o GitHub já abriu "Bump commons-text 1.9 → 1.15.0").

---

## DEMO 3 — Imagem/dependências seguras (nuvem) · ~6 min

**O que falar:** "Não precisei instalar scanner nenhum. O pipeline na nuvem leu as
dependências e o Dockerfile e me disse o que é perigoso."

1. Abra o repo no GitHub → aba **Actions** → workflow **security-gate** → a execução do `main`.
2. Abra o job **"Trivy — dependências + Dockerfile"** → passo **"relatório completo"**.
   Mostre na tabela:
   - **`commons-text 1.9 · CVE-2022-42889 · CRITICAL`** (a Text4Shell).
   - **`Dockerfile · AVD-DS-0002 (HIGH)`** — "sem USER = roda como root".
3. Troque para a branch **`hardened`** (seletor de branch ou a execução verde) → o mesmo job
   fica **verde**: `commons-text 1.11.0` e Dockerfile multi-stage/não-root.
4. Abra os arquivos lado a lado para fechar o conceito: **`portal-pedidos/pom.xml`**
   (1.9 → 1.11.0) e **`portal-pedidos/Dockerfile`** (`FROM ...-jdk` single-stage, root →
   multi-stage, `FROM ...-jre`, `USER appuser`).

> **SCA nativo (bônus forte):** aba **Security → Dependabot** do repo. A CVE aparece
> **sozinha**, sem rodar nada. E em **Pull requests** está o PR automático que o GitHub
> abriu para subir o `commons-text`. "O próprio GitHub achou e já te mandou a correção."

---

## DEMO 4 — Gate de segurança no CI (shift-left) · ~5 min

**O que falar:** "Achar em produção é caro; achar no PR é barato. O inseguro **não passa
sozinho**."

1. Na aba **Actions**, mostre o **✔ verde no `hardened`** e o **✘ vermelho no `main`** —
   lado a lado. "Mesmo pipeline. A diferença é só o código."
2. Abra o passo **"Gate — reprova em HIGH/CRITICAL"** do `main`: o build **falhou** porque
   achou CRITICAL/HIGH (`exit-code 1`). Mostre a linha do YAML
   (`severity: HIGH,CRITICAL` + `--exit-code 1`) — "é aqui que o crítico vira barreira."
3. **Prova viva (opcional):** abra um **Pull Request** de `hardened` → `main` (ou um branch
   de teste) e deixe a turma ver o **check do security-gate** rodando no PR:
   vermelho enquanto houver a lib vulnerável, verde depois de corrigida.
4. **Conceito:** shift-left — proteção automática no PR, não depende de alguém lembrar.

---

## Aluno (OPCIONAL) — rodar no próprio fork

Para quem tem conta no GitHub (grátis). Quem não tiver, só assiste — o conceito passa igual.

1. Abrir o repo → botão **Fork** (canto superior direito).
2. No fork, aba **Actions** → botão **"I understand my workflows, go ahead and enable them"**.
3. Rodar de um dos dois jeitos:
   - **Botão:** Actions → *security-gate* → **Run workflow** (está habilitado via
     `workflow_dispatch`); ou
   - **Push:** qualquer commit dispara o workflow.
4. Ver o resultado: 🔴 no fork do baseline, 🟢 depois de trocar para o estado hardened
   (`commons-text` 1.11.0 + Dockerfile endurecido).
5. **SCA nativo no fork:** *Settings → Advanced Security* → confirmar **Dependabot alerts**
   ligado → aba **Security** mostra a Text4Shell.

> Em forks, os workflows vêm **desabilitados por segurança** — por isso o passo 2. É um
> clique. Nada roda na máquina do aluno; tudo nos runners do GitHub.

---

## Por que esse caminho (e não o Trivy local)

| Dor do caminho local | Como a nuvem resolve |
|---|---|
| Windows Defender apaga `trivy.exe`/`zap` (PUA) | Roda no runner Linux do GitHub; nada baixa na máquina |
| NVD API key + 1ª execução lenta do Dependency-Check | Trivy usa banco próprio; Dependabot é nativo — **sem NVD key** |
| VM dos alunos sem Docker | O Docker é do runner, na nuvem |
| "Seguir passo sem entender" | A turma **vê** o vermelho/verde e o porquê, lado a lado |

**Trivy local continua válido como bônus/opcional** (ver `roteiro-demonstracao.md`, seção
"Alternativa local"): na sua máquina de instrutor (`brew install trivy` no macOS) ou no
Windows com `trivy.exe` — mesmos achados, offline.
