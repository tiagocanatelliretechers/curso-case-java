# Introdução ao Curso — Application Security em Java (CASE)

## Boas-vindas
> Este é um curso prático de segurança de aplicações para quem desenvolve em Java — você vai atacar e defender uma aplicação real.
- 24 horas, 6 encontros de 4 horas, ao vivo.
- Cada aula combina teoria e laboratório na mesma sessão.
- Formato inspirado no CASE (Certified Application Security Engineer).
Regra de ouro: aqui a gente aprende segurança fazendo — quebrando e depois consertando código de verdade.

## Para quem é este curso
> Pensado para desenvolvedores Java que já constroem software e querem construir com segurança.
- Público: pessoas com 2+ anos de experiência em Java/Spring.
- Pré-requisitos técnicos: JDK 17, Maven, Docker, uma IDE e Postman/Insomnia.
- Não é preciso experiência prévia em segurança — vamos construir essa base juntos.
Dinâmica: rápida rodada de apresentação — nome, o que você desenvolve e uma dúvida de segurança que te incomoda.

## O que você vai saber fazer ao final
> A meta é sair com competências aplicáveis no seu dia a dia, não só conceitos.
- Reconhecer e explorar as principais vulnerabilidades (OWASP Top 10) em aplicações Java.
- Corrigir essas falhas com código seguro (validação, autenticação, autorização, criptografia).
- Fazer threat modeling e escrever requisitos de segurança verificáveis.
- Usar ferramentas de teste (SAST/DAST) e montar gates de segurança no CI/CD.
- Aplicar segurança em todo o ciclo de desenvolvimento (Secure SDLC).

## Como o curso funciona
> Todo encontro tem o mesmo ritmo: entender a teoria, ver o exemplo, e pôr a mão na massa.
- Teoria com exemplos concretos em Java e analogias do mundo real.
- Laboratório guiado, com passo a passo e checkpoints de verificação.
- Quiz de fixação ao final de cada aula.
- Material de apoio: slides, guias de laboratório, gabaritos e templates.
Analogia: é como aprender a dirigir — um pouco de teoria e muito volante, sempre em pista segura.

## A aplicação que vamos proteger
> Em vez de trocar de exemplo a cada aula, você evolui a MESMA aplicação do começo ao fim.
- "Portal de Pedidos B2B": login, cadastro de clientes, catálogo, carrinho, checkout, área admin e upload de comprovantes.
- Stack: Java 17, Spring Boot 3, Spring Security, JPA, banco H2/PostgreSQL, API REST com JWT.
- Ela nasce cheia de vulnerabilidades reais, propositais e didáticas.
No Portal: aula após aula, você transforma a versão "quebrada" na versão segura.

## Por que Application Security importa
> A maioria das brechas de alto impacto hoje está na camada de aplicação — não na rede.
- Firewall e HTTPS protegem o transporte, mas não a lógica que decide "quem pode ver o quê".
- Uma requisição de ataque quase sempre é um HTTP perfeitamente válido.
- O custo de corrigir cresce a cada fase: barato no requisito, caríssimo sob incidente.
Regra de ouro: segurança que "entra no fim" é cara e frágil; segurança projetada desde o início é barata e robusta (shift-left).

## O mapa do inimigo: OWASP Top 10 (2021)
> É a lista das dez categorias de risco mais críticas em aplicações web — nosso vocabulário comum.
- A01 Broken Access Control · A02 Cryptographic Failures · A03 Injection
- A04 Insecure Design · A05 Security Misconfiguration · A06 Vulnerable Components
- A07 Auth Failures · A08 Software/Data Integrity · A09 Logging Failures · A10 SSRF
No Portal: todas as dez categorias estão vivas na aplicação — e você vai encontrá-las e corrigi-las.

## Segurança em todo o ciclo (Secure SDLC)
> Segurança não é uma etapa no fim — é uma preocupação em cada fase do desenvolvimento.
- Requisitos → Design → Implementação → Teste → Deploy → Manutenção.
- Cada aula do curso ataca uma dessas fases, usando o mesmo Portal.
Analogia: segurança é como qualidade — não se "inspeciona no fim", se constrói em cada etapa.

## Aula 1 — Ameaças, Ataques e Requisitos
> Entender o campo de jogo e transformar isso em requisitos de segurança.
- Panorama de AppSec, o OWASP Top 10 item a item e a anatomia de um ataque.
- Levantamento de requisitos: abuse cases, security user stories e ASVS.
- Lab: subir o Portal, reconhecê-lo como um atacante e derivar requisitos.

## Aula 2 — Design Seguro e Validação de Entrada
> Por que as falhas nascem no design — e como a validação de entrada as elimina.
- Princípios de secure by design e threat modeling com STRIDE.
- Validação de entrada, Bean Validation e upload seguro.
- Lab: corrigir SQL Injection, adicionar validação e proteger o upload.

## Aula 3 — Autenticação e Autorização
> Provar quem é o usuário e garantir o que ele pode fazer.
- Hashing de senha (bcrypt), políticas modernas, MFA e SSO.
- Controle de acesso: RBAC, IDOR e autorização por objeto.
- Lab: explorar e corrigir o IDOR, migrar senhas para bcrypt, limitar tentativas de login.

## Aula 4 — Criptografia e Sessão
> Proteger dados de verdade e blindar a sessão do usuário.
- Cifra x hash x codificação; AES-GCM, chaves e SecureRandom.
- Gestão de sessão, cookies seguros, JWT e CSRF.
- Lab: substituir "Base64" por AES, verificar assinatura de JWT e proteger a sessão.

## Aula 5 — Tratamento de Erros e Testes (SAST/DAST)
> Falhar com segurança e encontrar falhas com ferramentas.
- Tratamento de erro sem vazar informação e logging seguro.
- SAST (análise estática) e DAST (análise dinâmica): forças e limites.
- Lab: handler global de erros, scan com SonarQube/Semgrep e OWASP ZAP.

## Aula 6 — Deploy Seguro e Fechamento
> Levar a segurança até a produção e amarrar tudo.
- Hardening, gestão de segredos, dependências vulneráveis e segurança em containers.
- Gates de segurança no CI/CD e revisão de código.
- Lab final + Capture the Flag + simulado de certificação.

## O que você precisa na máquina
> Prepare o ambiente antes da Aula 1 para não perder tempo de laboratório.
- JDK 17+, Git, Docker + Docker Compose, uma IDE Java e Postman/Insomnia.
- Portas livres: 8080 (app), 5432 (banco) e 9000 (ferramenta de análise, Aula 5).
- Internet na primeira execução (download de dependências).
Dinâmica: valide agora com `java -version`, `docker --version` e `git --version`.

## Você evolui a mesma aplicação
> Cada laboratório parte do estado corrigido do anterior — como um projeto real que amadurece.
- `aula-1-baseline`: a aplicação vulnerável (ponto de partida).
- A cada aula, uma versão "endurecida" (hardened) com as correções acumuladas.
- Ao final, a mesma aplicação sai segura, com testes que provam as correções.
Analogia: é o seu backlog de segurança sendo resolvido sprint a sprint.

## Como você é avaliado (e aprende)
> A avaliação é prática e contínua — o foco é aprendizado, não pegadinha.
- Laboratórios com checkpoints em cada aula.
- Quizzes de fixação ao final de cada encontro.
- Capture the Flag e simulado de certificação na Aula 6.

## Combinados e postura ética
> Vamos atacar sistemas — e isso exige responsabilidade.
- Todo ataque acontece em ambiente controlado e autorizado (o nosso Portal).
- Técnicas de ataque aqui servem para aprender a defender.
- Fora de um escopo autorizado, testar segurança em sistemas de terceiros é crime.
Regra de ouro: com grande acesso vem grande responsabilidade — segurança é também uma postura profissional.

## Vamos começar
> Você sai deste curso capaz de encontrar, explicar e corrigir vulnerabilidades reais em aplicações Java.
- Próximo passo: Aula 1 — Ameaças, Ataques e Levantamento de Requisitos.
- Deixe o ambiente pronto e traga suas dúvidas.
No Portal: na Aula 1 você sobe a aplicação e faz seu primeiro reconhecimento como atacante.
Dinâmica: em uma frase, o que você mais quer levar deste curso?
