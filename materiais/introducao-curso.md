# Introdução ao Curso — Application Security em Java (CASE)

=== Panorama do Curso

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

=== Aquecimento Técnico

## Aquecimento: o vocabulário que vamos usar
> Antes de mergulhar, vamos alinhar os conceitos-base que atravessam todas as 6 aulas — pense nisto como o aquecimento antes do treino.
- São ideias simples, mas que sustentam quase toda vulnerabilidade que veremos.
- Cada slide traz a teoria, um exemplo tangível e um exercício rápido para fazermos juntos.
Regra de ouro: se estes fundamentos ficarem sólidos, o resto do curso vira aplicação deles.

## Fundamento 1 — Fronteira de confiança
> A pergunta mais importante em AppSec: "de onde vem este dado, e eu posso confiar nele?".
Teoria: uma trust boundary (fronteira de confiança) é o ponto onde um dado sai do controle do sistema e passa a poder ser manipulado por terceiros — tudo que cruza essa fronteira é "não confiável" até prova em contrário.
Na prática: qualquer coisa que venha do navegador — parâmetros, formulários, headers, cookies, corpo JSON — foi potencialmente forjada pelo usuário.
Analogia: é a portaria do prédio — visitante só entra depois de identificado; encomenda só sobe depois de conferida.
Juntos: listem, para a tela de login, tudo que "vem de fora" e portanto não é confiável.

## Fundamento 2 — Anatomia de uma requisição HTTP
> Toda interação web é uma requisição HTTP — saber lê-la é enxergar onde o atacante age.
Teoria: uma requisição tem método (GET/POST…), caminho, headers (incl. `Authorization`, `Cookie`), e às vezes um corpo (form ou JSON). A resposta tem status, headers e corpo.
```http
POST /api/auth/login HTTP/1.1
Host: portal.local
Content-Type: application/json

{"email":"joao@acme.com","senha":"senha123"}
```
Na prática: método, caminho, headers e corpo — todos são controlados pelo cliente e podem ser alterados (ex.: com o Postman).
Juntos: nessa requisição, o que é entrada do usuário? E o que o servidor devolve que poderia vazar informação?

## Fundamento 3 — Cliente x Servidor: nunca confie no cliente
> Validação no navegador é conveniência para o usuário; segurança acontece no servidor.
Teoria: o cliente (browser/app) roda na máquina do usuário, que pode inspecionar, alterar e reenviar qualquer requisição. Portanto, controles só no cliente são decorativos.
Exemplo tangível: esconder o botão "Admin" no frontend não impede alguém de chamar `/admin` direto pela URL.
Erro comum: "o campo é `readonly` e tem `maxlength`, então está seguro". O atacante ignora o HTML e manda o que quiser.
Regra de ouro: valide e autorize sempre no servidor — o cliente é território do atacante.

## Fundamento 4 — Autenticação x Autorização
> Dois conceitos que se confundem o tempo todo — e cuja mistura gera metade das falhas de acesso.
Teoria: autenticação prova QUEM você é (login). Autorização decide O QUE você pode fazer (permissões). Autenticar não é autorizar.
Exemplo tangível: estar logado (autenticado) não deveria permitir ler o pedido de outro cliente — isso é uma decisão de autorização.
Analogia: o crachá prova quem você é (autenticação); a catraca que libera cada andar decide onde você entra (autorização).
Juntos: "ver o próprio extrato", "aprovar um pagamento", "provar a identidade no login" — cada um é authn ou authz?

## Fundamento 5 — Como o servidor "lembra" de você: sessão x token
> HTTP é sem memória; para manter você logado, o servidor usa sessão (cookie) ou token (JWT).
Teoria: na sessão stateful, o servidor guarda o estado e envia um `JSESSIONID` no cookie. No stateless, o servidor envia um token assinado (JWT) que o cliente reapresenta a cada requisição.
Na prática: cada abordagem tem trade-offs — sessão é fácil de revogar; JWT é fácil de escalar, mas difícil de revogar antes de expirar.
No Portal: usamos sessão na web e JWT na API — e as duas terão falhas didáticas a corrigir na Aula 4.
Pergunta: se um token não for verificado corretamente, o que impede alguém de forjar `role: ADMIN`?

## Fundamento 6 — Codificar x Cifrar x Hashear
> O trio mais confundido da segurança — e a raiz de muitas "criptografias" falsas.
Teoria:
- Codificar (Base64): muda o formato, é reversível sem chave — NÃO protege nada.
- Cifrar (AES): reversível com chave — protege confidencialidade de dado que precisa ser recuperado.
- Hashear (bcrypt): irreversível — serve para verificar senha sem guardá-la.
Analogia: codificar é escrever no espelho; cifrar é trancar no cofre; hashear é triturar o papel.
Juntos: classifiquem — guardar uma senha, proteger um número de cartão, transmitir um dado em Base64: é hash, cifra ou codificação?

## Fundamento 7 — Injeção: a fronteira entre dado e comando
> Quase toda "injeção" nasce de misturar, na mesma string, a instrução e o dado do usuário.
Teoria: quando o dado do usuário é concatenado num comando (SQL, shell, HTML), o interpretador não distingue instrução de conteúdo — e o conteúdo vira comando.
```java
"SELECT * FROM produto WHERE nome LIKE '%" + termo + "%'"  // termo vira SQL
```
Analogia: ditar uma carta e, no meio, dizer "…e agora apague os arquivos" — sem separar instrução de conteúdo, o interpretador obedece.
Como se defende: separar comando de dado (consultas parametrizadas) — o dado nunca é interpretado como código.

## Fundamento 8 — Validação de entrada
> A primeira linha de defesa: decidir o que entra, antes de usar.
Teoria: prefira allowlist (aceitar apenas o que é sabidamente válido) a denylist (tentar bloquear o que é ruim) — a denylist sempre fica atrás de novas variações de ataque.
Na prática: valide formato, tamanho e tipo; e valide no servidor, na borda (ex.: Bean Validation nos DTOs) e no domínio (regras de negócio).
Exemplo tangível: aceitar um upload só se o conteúdo real (magic number) for PDF/PNG — não confiar na extensão do nome.
Juntos: para o campo "CNPJ", quais regras de allowlist você escreveria?

## Fundamento 9 — Princípios de design seguro
> Alguns princípios guiam quase toda boa decisão de segurança — vamos usá-los o curso inteiro.
- Least privilege: cada um com o mínimo de acesso necessário.
- Defense in depth: várias camadas; nenhuma é o único ponto de falha.
- Fail securely: em erro, negar por padrão — nunca "abrir" o acesso.
- Complete mediation: toda requisição a um recurso é verificada.
Analogia: um banco não tem só uma tranca — tem porta, cofre, câmeras e alarme; se uma falha, as outras seguram.

## Fundamento 10 — Pensando como atacante (mini threat model)
> Modelar ameaças é responder, para cada funcionalidade, três perguntas simples.
Teoria: (1) O que estamos protegendo? (2) De quem/de quê? (3) Como isso poderia dar errado — e o que faremos a respeito?
Exemplo tangível: no upload de comprovante — proteger o servidor e os arquivos; de um usuário malicioso; contra arquivos executáveis, nomes com `../` e uploads gigantes.
No Portal: na Aula 2 formalizamos isso com STRIDE; hoje já dá para pensar assim.
Juntos: apliquem as três perguntas à funcionalidade de login.

## Fundamento 11 — Spring Security em um slide
> A base técnica em Java que usaremos para autenticar e autorizar.
Teoria: o Spring Security intercepta requisições numa cadeia de filtros. Um `UserDetailsService` carrega o usuário; um `PasswordEncoder` verifica a senha; regras de autorização decidem o acesso.
```java
http.authorizeHttpRequests(a -> a
    .requestMatchers("/admin/**").hasRole("ADMIN")
    .anyRequest().authenticated());
```
Na prática: `@PreAuthorize` e checagens no service permitem autorização por função e por objeto (contra IDOR).
No Portal: vamos configurar exatamente isso nas Aulas 3 e 4.

## Aquecimento — glossário essencial
> Termos que vão aparecer o tempo todo; guarde-os à mão.
- PII: dados pessoais identificáveis. CVE: vulnerabilidade pública catalogada.
- Payload: o dado/carga que o atacante envia. IDOR: acesso a objeto de outro por trocar o id.
- Hardening: reduzir a superfície e endurecer a configuração. SBOM: inventário de componentes.
- CIA: Confidencialidade, Integridade, Disponibilidade.
Juntos: alguém explica com as próprias palavras o que é "IDOR" e dá um exemplo no Portal.

## Vamos começar
> Você sai deste curso capaz de encontrar, explicar e corrigir vulnerabilidades reais em aplicações Java.
- Aquecimento feito: agora temos vocabulário e fundamentos comuns.
- Próximo passo: Aula 1 — Ameaças, Ataques e Levantamento de Requisitos.
No Portal: na Aula 1 você sobe a aplicação e faz seu primeiro reconhecimento como atacante.
Dinâmica: em uma frase, o que você mais quer levar deste curso?
