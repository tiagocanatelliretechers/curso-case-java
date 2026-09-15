# Aula 6 — Deploy e Manutenção Seguros + Fechamento

=== Deploy Seguro

## O que vamos aprender hoje
> Última aula: levamos a segurança até a produção e amarramos o ciclo inteiro.
- Hardening de configuração, gestão de segredos e integridade de artefatos.
- Dependências vulneráveis, segurança de containers e gates de CI/CD.
- Manutenção: patch, monitoramento, revisão de código e métricas.
- Lab final + Capture the Flag + simulado de certificação.
Regra de ouro: a aplicação mais segura do mundo em dev pode ser insegura em produção — o deploy também é código.

## Security Misconfiguration na prática
> A mesma aplicação, mal configurada, vira um alvo fácil.
Teoria: diferencie ambientes (dev/staging/prod); nunca leve config de dev para produção; faça hardening do servidor de aplicação; proteja ou desabilite endpoints de gestão (Actuator) em produção; remova banners e versões expostas.
```yaml
management.endpoints.web.exposure.include: health,info  # em prod, mínimo necessário
```
No Portal: o Actuator e o H2 console estão abertos; o Lab 6.1 os restringe.

## Gestão de segredos em deploy
> Segredo em imagem ou em arquivo versionado é segredo vazado.
Teoria: siga o 12-factor — configuração (incl. segredos) no ambiente, não no código; prefira secrets manager (Vault, AWS/GCP Secrets); nunca coloque segredo em `ENV` do Dockerfile (fica nas layers).
Erro comum: `ENV DB_PASSWORD=...` no Dockerfile — qualquer um com a imagem lê com `docker history`.
No Portal: os segredos vêm do ambiente em runtime; o Dockerfile hardened não os embute (Lab 6.3).

## Software e Data Integrity Failures
> Garantir que o que você executa é exatamente o que deveria — sem adulteração no caminho.
Teoria: assine e verifique artefatos; gere um SBOM (inventário de componentes); proteja-se contra dependency confusion; trave versões e checksums.
Analogia: conferir o lacre da encomenda antes de usar o conteúdo.
No Portal: endereçamos com verificação de dependências e gates de pipeline (Labs 6.2 e 6.4).

## Verificação de dependências (SCA)
> Você é responsável também pelo código que não escreveu — o de terceiros.
Teoria: ferramentas de SCA (OWASP Dependency-Check, Snyk, GitHub Dependabot) rodam no build, cruzam suas dependências com bases de CVE e falham o build em risco alto.
```bash
./mvnw -Psecurity verify   # Dependency-Check: falha em CVSS alto
```
No Portal: usamos `commons-text` 1.9 (CVE-2022-42889, "Text4Shell"); o Lab 6.2 detecta e atualiza.

## Segurança em CI/CD (shift-left na prática)
> O pipeline é onde a segurança deixa de ser intenção e vira gate obrigatório.
Teoria: insira gates — SAST no PR, SCA no build, DAST em staging antes do deploy; o build falha diante de um achado crítico e o merge é bloqueado sem os checks verdes.
Exemplo tangível: reintroduzir uma dependência vulnerável faz o job de SCA falhar — a falha é barrada antes de chegar à produção.
No Portal: o Lab 6.4 monta um pipeline com build+test, SAST e SCA.

## Segurança de containers
> O container é a unidade de deploy — e também uma superfície de ataque.
Teoria: no Dockerfile, use build multi-stage, imagem mínima, usuário não-root e nenhum segredo em layer; escaneie a imagem (Trivy/Grype).
```dockerfile
FROM eclipse-temurin:17-jre
RUN useradd -r -u 1001 app
USER app                      # não-root
```
No Portal: o baseline roda como root com segredo embutido; o Lab 6.3 endurece e escaneia a imagem.

=== Manutenção Segura

## Patch management
> Software seguro hoje é software inseguro amanhã, se não for mantido.
Teoria: dependências desatualizadas são uma das causas mais comuns de incidente; monitore CVEs continuamente e priorize atualizações por risco.
Na prática: automatize com Dependabot/Renovate e faça triagem — atualizar cegamente também tem custo.
Regra de ouro: trate atualização de segurança como parte do trabalho, não como interrupção dele.

## Monitoramento e resposta
> Você não defende o que não enxerga — e não responde ao que não detecta.
Teoria: combine WAF, logging centralizado e alertas de comportamento anômalo; tenha um plano de resposta a incidente com runbooks. RASP (Runtime Application Self-Protection) é uma camada adicional, embutida na aplicação.
Conexão com A09: sem os logs de segurança da Aula 5, o monitoramento fica cego.
Analogia: câmeras e alarme só servem se alguém olha os alertas e sabe o que fazer.

## Code review de segurança
> A revisão humana pega o que a ferramenta não vê — intenção e contexto.
Teoria: revise o diff (não só o arquivo) com um checklist: autenticação, autorização (posse!), validação de entrada, dados sensíveis, dependências novas, tratamento de erro/log.
Exemplo tangível: um PR que adiciona `GET /pedidos/{id}` deve levantar imediatamente a pergunta "cadê a checagem de posse?".
No Portal: o checklist de revisão está nos anexos da Aula 6.

## Security Champions e o SDLC ágil
> Segurança escala por cultura, não por um gargalo central.
Teoria: um Security Champion é o elo de segurança dentro do squad — dissemina práticas, revisa com olhar de segurança e faz a ponte com o time de AppSec. SAST/SCA viram gate obrigatório no PR.
Regra de ouro: ferramenta habilita, cultura sustenta — sem Champions, os gates viram obstáculo ignorado.

## Métricas de um programa de AppSec
> O que não é medido não melhora — e não recebe investimento.
Teoria: acompanhe MTTR de vulnerabilidade (tempo médio de correção), % de findings corrigidos vs. aceitos como risco, cobertura de scans por repositório e densidade de findings por KLOC.
Exemplo tangível: um MTTR caindo mês a mês mostra que o processo (não só o esforço pontual) está funcionando.

=== Fechamento do Curso

## Recapitulação integrada
> Em 6 aulas, você percorreu o Secure SDLC inteiro sobre a mesma aplicação.
- Requisitos e ameaças (Aula 1) → design e validação (Aula 2).
- Autenticação/autorização (Aula 3) → criptografia e sessão (Aula 4).
- Erros e testes (Aula 5) → deploy e manutenção (Aula 6).
No Portal: a aplicação saiu de vulnerável (baseline) para endurecida (hardened), com testes que provam as correções.

## Capture the Flag (visão geral)
> Um desafio curto para integrar tudo: cada "flag" remete a um módulo diferente.
Teoria: você vai reproduzir (ou provar que não é mais possível) falhas na versão final — IDOR, segredo mal protegido, SQLi, dependência vulnerável, log injection.
Juntos: individual ou em duplas, cada flag com evidência e o módulo/OWASP associado.
No Portal: o enunciado está nos anexos da Aula 6 (ctf.md).

## Simulado de certificação (visão geral)
> Fechamento avaliativo no estilo do exame, cobrindo os 10 módulos do curso.
Teoria: 20 questões de múltipla escolha, proporcionais aos módulos, com gabarito comentado.
Na prática: cronometrado quando possível; a correção ao vivo revisa os pontos fracos da turma.
No Portal: o simulado está em `materiais/simulado-final.md`.

## Encerramento e próximos passos
> Segurança não termina aqui — vira parte de como você desenvolve.
- Adote o ASVS como padrão de verificação nos seus projetos.
- Institua Security Champions e gates de SAST/SCA no CI/CD.
- Continue estudando: OWASP Top 10, ASVS e as cheat sheets da OWASP.
Regra de ouro: do requisito ao deploy, segurança é responsabilidade de quem constrói — e agora você tem as ferramentas.
