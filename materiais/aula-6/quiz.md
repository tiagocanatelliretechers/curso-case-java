# Aula 6 — Quiz de fixação (10 questões)

**1.** Deixar o Actuator totalmente exposto em produção é um exemplo de:
- a) Injection
- b) Security Misconfiguration
- c) SSRF
- d) CSRF

**2.** Segundo o 12-factor app, a configuração (incluindo segredos) deve ficar:
- a) No código-fonte
- b) No ambiente (variáveis/secrets manager)
- c) Na imagem Docker
- d) Em um comentário no repositório

**3.** SBOM (Software Bill of Materials) serve para:
- a) Acelerar o build
- b) Inventariar dependências/componentes e rastrear vulnerabilidades
- c) Criptografar dados
- d) Substituir o SAST

**4.** Ferramentas como OWASP Dependency-Check e Snyk fazem:
- a) DAST
- b) SAST
- c) SCA (análise de componentes/dependências)
- d) Threat modeling

**5.** No hardening de um Dockerfile, uma boa prática é:
- a) Rodar como root para facilitar
- b) Usuário não-root, multi-stage e sem segredos em layer
- c) Embutir a chave da aplicação via ENV
- d) Usar a maior imagem possível

**6.** Colocar um segredo em `ENV` no Dockerfile é problemático porque:
- a) Deixa o build lento
- b) O segredo fica registrado nas layers da imagem
- c) O Docker não suporta ENV
- d) Não é problema

**7.** "Shift-left" em segurança significa:
- a) Testar segurança só depois do deploy
- b) Antecipar atividades de segurança para o início do ciclo
- c) Mover o servidor para a esquerda
- d) Ignorar o pipeline

**8.** Um gate de segurança típico em Pull Request é:
- a) Revisão de UX
- b) SAST/SCA obrigatório que bloqueia merge em finding crítico
- c) Deploy automático em produção
- d) Aumento de logs

**9.** O papel de um **Security Champion** é:
- a) Substituir o time de AppSec
- b) Ser o elo de segurança dentro do squad, disseminando práticas
- c) Aprovar todos os merges sozinho
- d) Only run scans

**10.** (Laboratório) No Dependency-Check do Portal, qual dependência é sinalizada?
- a) Spring Boot
- b) `commons-text` 1.9 (CVE-2022-42889 / Text4Shell)
- c) PostgreSQL driver
- d) H2

---

## Gabarito comentado
1. **b** — Config insegura em produção.
2. **b** — Config no ambiente, não no código/imagem.
3. **b** — Inventário para rastrear CVEs.
4. **c** — SCA analisa dependências.
5. **b** — Não-root, multi-stage, sem segredo.
6. **b** — Segredo persiste nas layers.
7. **b** — Antecipar segurança no ciclo.
8. **b** — Gate que bloqueia merge em crítico.
9. **b** — Elo de segurança no squad.
10. **b** — `commons-text` 1.9 (Text4Shell).
