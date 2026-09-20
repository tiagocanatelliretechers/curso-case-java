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

## Branches e tags da aplicação

- `master` — materiais das 6 aulas (slides `.md` + `.pptx`, labs, gabaritos, quizzes) +
  aplicação no estado **baseline**.
- branch `curso-progressivo` — progressão linear com uma correção por aula (compila em cada etapa).
- branch `solucao-hardened` — solução final + testes JUnit (`mvn test` = 9 verdes).

Tags (cada `aula-N-baseline` == `aula-(N-1)-hardened`, para o lab começar do estado corrigido anterior):

| Tag | Estado |
|-----|--------|
| `aula-1-baseline` = `aula-2-baseline` | app vulnerável (Aula 1 não altera código) |
| `aula-2-hardened` = `aula-3-baseline` | + SQLi parametrizado, Bean Validation/@CNPJ, upload seguro |
| `aula-3-hardened` = `aula-4-baseline` | + IDOR, BCrypt+migração, rate limiting, /admin ADMIN |
| `aula-4-hardened` = `aula-5-baseline` | + AES-256-GCM, JWT verificado, cookie/sessão, CSRF |
| `aula-5-hardened` = `aula-6-baseline` | + error handling, log injection, headers, SSRF allowlist |
| `aula-6-hardened` | + Actuator restrito, H2 off, commons-text atualizado, Dockerfile hardened, testes |

Todas as correções foram **verificadas em execução** (ataques falham, uso legítimo funciona)
e os testes JUnit passam em `aula-6-hardened`.

> Dica: `git checkout aula-1-baseline` (ponto de partida) · `git checkout aula-3-baseline`
> (iniciar a Aula 3) · `git checkout solucao-hardened` (solução + testes).
> Para rodar a solução, defina `PORTAL_JWT_SECRET` e `PORTAL_CRYPTO_KEY` (há defaults DEV no `application.yml`).

> ⚠️ A aplicação é um ambiente de treinamento e contém vulnerabilidades reais de
> propósito. Não a exponha na internet.
