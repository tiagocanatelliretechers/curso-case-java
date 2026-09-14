# Curso CASE Java — Certified Application Security Engineer

Material completo do curso **Application Security em Java** (24h, 6 aulas de 4h),
formato CASE, para desenvolvedores Java com 2+ anos de experiência.

## Estrutura do repositório

```
portal-pedidos/     Aplicação de referência (Spring Boot, propositalmente vulnerável)
                    -> os laboratórios de todas as aulas evoluem este projeto
materiais/
  aula-1/ .. aula-6/  slides (outline), guia de lab, gabarito, quiz, anexos
  mapa-rastreabilidade.md
  simulado-final.md
```

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

Estado atual do repositório:

- `master` — materiais das 6 aulas + aplicação no estado **baseline** (ponto de partida).
- tag `aula-1-baseline` — aplicação vulnerável (ponto de partida dos laboratórios).
- branch `solucao-hardened` / tag `aula-6-hardened` — **solução de referência completa**
  (correções das Aulas 2–6 aplicadas e **verificadas em execução**: SQLi, IDOR, JWT,
  BCrypt, AES-GCM, CSRF, Actuator, SSRF, rate limiting).

Convenção pedagógica sugerida (gerar sob demanda a partir dos gabaritos, que já contêm
o código corrigido de cada lab): `aula-N-baseline` (estado corrigido acumulado até a aula
anterior) e `aula-N-hardened` (solução daquela aula).

> Dica: `git checkout aula-1-baseline` para o ponto de partida; `git checkout solucao-hardened`
> para a solução final. Para rodar a solução, defina `PORTAL_JWT_SECRET` e `PORTAL_CRYPTO_KEY`
> (há defaults DEV no `application.yml`).

> ⚠️ A aplicação é um ambiente de treinamento e contém vulnerabilidades reais de
> propósito. Não a exponha na internet.
