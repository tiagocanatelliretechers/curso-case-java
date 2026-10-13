# Curso CASE Java — Certified Application Security Engineer

Material completo do curso **Application Security em Java** (24h, 6 aulas de 4h),
formato CASE, para desenvolvedores Java com 2+ anos de experiência.

## Estrutura do repositório

```
portal-pedidos/     Aplicação de referência (Spring Boot, propositalmente vulnerável)
                    -> os laboratórios de todas as aulas evoluem este projeto
infra/              docker-compose da infra (app + Postgres + SonarQube)
scripts/            start.sh (Linux/macOS) e start.ps1 (Windows) — sobem tudo
materiais/
  aula-1/ .. aula-6/  slides, conteúdo, guia de lab, gabarito, quiz, anexos
  MANUAL-AMBIENTE-ALUNO.md
  mapa-rastreabilidade.md
  simulado-final.md
LABS.md             mapa de execução dos labs (onde, como, com um comando)
```

## Instalar os pré-requisitos (máquina zerada)

Um script instala Git, Java 17, Maven, Docker e o Eclipse:

```powershell
.\scripts\setup.ps1            # Windows (winget)
```
```bash
./scripts/setup.sh             # macOS (Homebrew) / Linux (apt)
```

## Subir o ambiente com um comando

```bash
./scripts/start.sh --lab 1     # Linux/macOS: checkout do lab + sobe app+banco
```
```powershell
.\scripts\start.ps1 -Lab 1     # Windows (PowerShell)
```

Acesse <http://localhost:8080>. Detalhes, flags (`--sonar`, `--dev`, `--stop`) e a
dinâmica dos laboratórios: ver **[LABS.md](LABS.md)**.

## As 6 aulas

| Aula | Tema | Módulos CASE |
|------|------|--------------|
| 1 | AppSec, ameaças e ataques + Levantamento de requisitos de segurança | 1, 2 |
| 2 | Design/arquitetura seguros + Validação de entrada | 3, 4 |
| 3 | Autenticação e autorização | 5 |
| 4 | Criptografia + Gestão de sessão | 6, 7 |
| 5 | Tratamento de erros + SAST & DAST | 8, 9 |
| 6 | Deploy e manutenção seguros + Revisão e simulado | 10 |

## Como usar

1. Suba a aplicação de referência (`portal-pedidos/`) seguindo o seu README.
2. Cada aula tem, em `materiais/aula-N/`:
   - `slides.md` — outline slide a slide (título + bullets + nota do apresentador)
   - `lab.md` — guia de laboratório (entregue ao aluno)
   - `gabarito.md` — soluções comentadas (**somente instrutor**)
   - `quiz.md` — quiz de fechamento com gabarito
   - `anexos/` — templates e fichas
3. Os `.md` estão prontos para conversão em PPTX/DOCX/PDF.

## 🎓 Todos os laboratórios em um só repositório (crus + gabarito)

Este repositório é **autocontido**: traz o app, os scripts, os materiais e **todos os estados
de laboratório das 6 aulas como branches** — cada falha "crua" para você praticar e cada
solução pronta como **gabarito**. Ideal para **exercitar depois do curso**.

- **`master`** — materiais das 6 aulas (slides, labs, gabaritos, quizzes, deck de revisão) +
  app no estado inicial.
- **Branch `aula-N-baseline`** — o lab **cru**: o estado vulnerável onde você *pratica* (já com as
  correções das aulas anteriores aplicadas).
- **Branch `aula-N-hardened`** — o **gabarito**: a mesma falha já corrigida, para conferir a solução.

> As mesmas referências existem também como **tags** de mesmo nome (`git checkout` funciona nos dois).

### Índice dos laboratórios

| Lab | Branch crua (pratique) | Branch gabarito (confira) | O que você corrige |
|-----|------------------------|---------------------------|--------------------|
| Aula 1 | `aula-1-baseline` | *(sem correção — reconhecimento)* | Mapear ameaças e levantar requisitos |
| Aula 2 | `aula-2-baseline` | `aula-2-hardened` | SQLi parametrizado, Bean Validation, upload seguro |
| Aula 3 | `aula-3-baseline` | `aula-3-hardened` | IDOR, BCrypt+migração, rate limiting, `/admin` por papel |
| Aula 4 | `aula-4-baseline` | `aula-4-hardened` | Base64→AES-256-GCM, JWT assinado, cookie/sessão, CSRF |
| Aula 5 | `aula-5-baseline` | `aula-5-hardened` | Error handling, log injection, headers, SonarLint/ZAP |
| Aula 6 | `aula-6-baseline` | `aula-6-hardened` | Actuator/H2, SCA (Text4Shell), Dockerfile, gate de CI |

Invariante: **`aula-N-baseline` == `aula-(N-1)-hardened`** (o corrigido acumula de uma aula para a outra).
Todas as correções foram **verificadas em execução** (ataques falham, uso legítimo funciona).

### Como praticar (pós-curso)

```bash
git clone https://github.com/tiagocanatelliretechers/curso-case-java.git
cd curso-case-java

git checkout aula-3-baseline              # começa a Aula 3 (estado cru)
./scripts/start.sh --no-docker            # Windows: .\scripts\start.ps1 -NoDocker
# -> siga materiais/aula-3/lab.md, aplique a correção

git checkout aula-3-hardened              # compare com o gabarito
```

Acesse <http://localhost:8080> · contas: `admin@portal.com/admin123` ·
`joao@acme.com/senha123` · `maria@globex.com/senha123`.
Para a solução, defina `PORTAL_JWT_SECRET` e `PORTAL_CRYPTO_KEY` (há defaults DEV no `application.yml`).

### Extra na nuvem (Aula 6 — SCA & gate de CI, sem instalar nada)

As branches **`aula-6-baseline`** e **`aula-6-hardened`** já trazem **`.github/`** configurado
(**Trivy** no GitHub Actions + **Dependabot**). Dê **fork**, habilite os workflows na aba **Actions**
e veja o **gate** ficar 🔴 vermelho no baseline (Text4Shell + Dockerfile root) e 🟢 verde no hardened.
Passo a passo: `materiais/aula-6/DEMO-NUVEM.md`.

> ⚠️ A aplicação é um ambiente de treinamento e contém vulnerabilidades reais de
> propósito. Não a exponha na internet.
