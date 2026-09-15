# Aula 1 — Application Security, Ameaças e Ataques + Levantamento de Requisitos

=== BLOCO 1 · Panorama de Application Security

## O que é Application Security
> Application Security (AppSec) é o conjunto de práticas que protege o software contra uso indevido ao longo de todo o seu ciclo de vida — do requisito ao deploy e à manutenção.
AppSec olha para o comportamento do software: a lógica, os dados que ele manipula e as decisões que ele toma. Não é um produto que se "instala no fim"; é uma propriedade que emerge de como a aplicação é projetada, escrita, testada e operada.
Definição: segurança de aplicação é garantir que a aplicação faça o que deve — e, principalmente, que NÃO faça o que não deve, mesmo diante de entradas e usuários maliciosos.
No Portal: quando João acessa `/pedidos/2`, é o código da aplicação (não a rede) que decide se ele pode ver aquele pedido. Essa decisão é AppSec.
Dinâmica: peça à turma exemplos de "coisas que a aplicação não deveria deixar acontecer" no dia a dia deles.

## Por que AppSec é diferente de segurança de rede/infra
> Firewalls, IDS e segmentação protegem o transporte e o perímetro. Eles não enxergam a lógica de negócio.
A camada de rede vê pacotes e conexões; ela não sabe se "este usuário pode ver este pedido". Uma requisição de ataque na camada de aplicação normalmente é uma requisição HTTP perfeitamente válida.
- O firewall deixa passar `GET /pedidos/2` — para ele, é tráfego HTTPS legítimo na porta 443.
- Um WAF pode ajudar bloqueando padrões conhecidos, mas é mitigação, não correção: o atacante adapta o payload.
- A responsabilidade muda de lugar: infra é do time de operações; boa parte da AppSec é do desenvolvedor.
Erro comum: achar que "temos firewall e HTTPS, então estamos seguros". HTTPS protege o dado em trânsito, não a lógica da aplicação.
Regra de ouro: quem decide autorização, valida entrada e trata o dado é o código — portanto é onde a segurança precisa morar.

## Quem ataca, e por quê
> Entender o atacante ajuda a priorizar defesas: nem todo alvo interessa a todo adversário, mas software exposto quase sempre interessa a alguém.
- Cibercriminosos: motivação financeira — fraude, ransomware, roubo e revenda de dados.
- Insiders: funcionários ou parceiros com acesso, por descuido ou má-fé.
- Hacktivistas e Estados-nação: ideologia, espionagem, sabotagem.
- Pesquisadores e "script kiddies": desde reporte responsável até uso de ferramentas automatizadas em massa.
A superfície de ataque cresce com APIs, mobile, nuvem e dependências de terceiros — cada integração é uma porta a mais.
Dinâmica: "Quem teria interesse em atacar o nosso Portal de Pedidos B2B, e o que ganharia com isso?"

## O custo de uma brecha e o "shift-left"
> Quanto mais tarde uma falha é descoberta, mais cara ela é para corrigir — e uma falha explorada custa muito mais do que uma corrigida a tempo.
- Custos diretos: resposta a incidente, perícia, correção emergencial, notificação de afetados.
- Custos indiretos: multas regulatórias (LGPD/GDPR), perda de clientes, reputação e litígio.
Corrigir um requisito mal escrito no início custa uma fração do que custa corrigir o mesmo problema em produção, sob incidente.
Regra de ouro: "shift-left" — antecipar a segurança para as fases iniciais (requisito, design) é a decisão de maior retorno em AppSec.
Atenção: use fontes reconhecidas (OWASP, relatórios de mercado) para embasar custos — evite citar números específicos sem referência verificável.

## A base: tríade CIA (e um pouco além)
> Toda vulnerabilidade viola uma ou mais propriedades de segurança. Nomeá-las ajuda a comunicar impacto.
- Confidentiality (Confidencialidade): só quem pode ver, vê. Ex.: vazar dados de pagamento fere confidencialidade.
- Integrity (Integridade): dados e comportamento não são adulterados indevidamente. Ex.: alterar o preço de um pedido.
- Availability (Disponibilidade): o sistema permanece utilizável. Ex.: um upload gigante derruba o serviço.
Complementos importantes: Autenticação (provar quem é), Autorização (o que pode fazer), Accountability/auditoria (registrar quem fez o quê) e Não-repúdio.
Dinâmica: para cada falha citada hoje, pergunte à turma "qual propriedade da CIA isso quebra?".

## OWASP Top 10 (2021): o que é e como usar
> O OWASP Top 10 é uma lista das dez categorias de risco mais críticas em aplicações web. É uma ferramenta de conscientização e priorização — não um checklist completo.
Para verificação detalhada existe o ASVS (veremos no Bloco 3). O Top 10 serve para alinhar vocabulário e priorizar o que dói mais.
No Portal: a aplicação de referência do curso já nasce com exemplos vivos das dez categorias — vamos encontrá-los no laboratório de hoje e corrigi-los nas próximas aulas.
- A seguir, um slide por categoria: definição, um exemplo em Java e o impacto de negócio.

## A01 — Broken Access Control
> Ocorre quando um usuário consegue fazer algo além do que sua permissão deveria permitir: ver dados de outros, agir como admin, ou acessar telas escondidas.
É a categoria nº 1 de 2021. Inclui IDOR (referência direta a objeto sem checar posse) e escalada de privilégio.
```java
@GetMapping("/pedidos/{id}")
public Pedido ver(@PathVariable Long id){
    return repo.findById(id).get();   // NÃO verifica se o pedido é do usuário logado
}
```
Impacto: vazamento e alteração de dados de outros clientes; acesso a áreas administrativas.
No Portal: `GET /pedidos/{id}` devolve o pedido de qualquer cliente — corrigimos isso na Aula 3.

## A02 — Cryptographic Failures
> Dados sensíveis expostos por criptografia ausente, fraca ou mal utilizada.
Inclui senha em texto claro, hashing fraco (MD5), e o clássico "confundir codificação com criptografia".
```java
// Base64 é CODIFICAÇÃO, não criptografia — qualquer um reverte sem chave
String protegido = Base64.getEncoder().encodeToString(cartao.getBytes());
```
Impacto: exposição de senhas, cartões e PII; quebra de sigilo e de conformidade.
No Portal: senha em MD5 e "dados de pagamento" em Base64 — corrigimos nas Aulas 3 e 4.

## A03 — Injection
> Acontece quando dado não confiável é interpretado como comando: SQL, comandos de SO, LDAP, XPath. Inclui também XSS na taxonomia de 2021.
A causa raiz é misturar dados com instruções. A defesa primária é separar os dois (consultas parametrizadas).
```java
// Vulnerável: o termo do usuário vira parte do comando SQL
String sql = "SELECT * FROM produto WHERE nome LIKE '%" + termo + "%'";
```
Impacto: leitura e alteração de dados, e em casos graves execução remota de código.
No Portal: SQL Injection na busca de produtos — corrigimos na Aula 2.

## A04 — Insecure Design
> Uma falha de concepção: o controle de segurança nunca foi projetado. Não é um bug de implementação — é a ausência de uma defesa desde o desenho.
Exemplos: um fluxo de login sem qualquer limite de tentativas; preços que vêm do cliente; ausência de threat modeling.
Impacto: classes inteiras de ataque ficam viáveis porque nada foi projetado para impedi-las.
Erro comum: tentar "corrigir com um patch" algo que precisaria ter sido projetado com segurança desde o início.
No Portal: ausência de rate limiting e de validação — tratamos no design (Aula 2) e na implementação (Aula 3).

## A05 — Security Misconfiguration
> Configuração insegura: defaults perigosos, endpoints abertos, permissões largas, mensagens de erro verbosas.
```yaml
management.endpoints.web.exposure.include: "*"   # expõe todo o Actuator, sem auth
server.error.include-stacktrace: always          # vaza stack trace ao usuário
```
Impacto: facilita o reconhecimento do atacante e expõe segredos e detalhes internos.
No Portal: Actuator e H2 console abertos, stack trace ao cliente — corrigimos nas Aulas 5 e 6.

## A06 — Vulnerable and Outdated Components
> Usar bibliotecas ou versões com vulnerabilidades conhecidas. Você herda a falha da dependência, mesmo que seu código esteja correto.
Exemplo: uma versão antiga de uma biblioteca com CVE pública (ex.: `commons-text` 1.9, "Text4Shell").
Impacto: exploração de uma falha já documentada e, muitas vezes, com exploit pronto.
No Portal: há uma dependência com CVE didática — detectamos e atualizamos na Aula 6 com o Dependency-Check.

## A07 — Identification and Authentication Failures
> Falhas em provar e gerenciar identidade: força bruta sem limite, senhas fracas, "esqueci minha senha" que revela contas, ausência de MFA, tokens mal validados.
```java
// Sem rate limiting: o atacante testa milhares de senhas
if (usuario == null || !senhaConfere(senha, usuario.getHash())) return erro401();
```
Impacto: account takeover — o atacante assume a conta da vítima.
No Portal: login sem rate limiting, user enumeration e JWT sem verificação — Aulas 3 e 4.

## A08 — Software and Data Integrity Failures
> Confiar em código ou dados sem verificar sua integridade: atualizações sem assinatura, deserialização insegura, pipelines de CI/CD comprometidos.
Muito ligada à cadeia de suprimentos (supply chain): um pacote adulterado entra no seu build.
Impacto: execução de código malicioso a partir de uma fonte "confiável".
No Portal: build sem verificação de dependências/artefatos — endereçamos com SBOM e gates de CI na Aula 6.

## A09 — Security Logging and Monitoring Failures
> Não registrar eventos de segurança (ou registrá-los de forma insegura) faz incidentes passarem despercebidos e inviabiliza a perícia.
Inclui log injection: registrar entrada do usuário sem sanitizar permite forjar linhas de log.
```java
log.info("Importando de: " + urlDoUsuario);   // \n no input forja linhas falsas
```
Impacto: o atacante age sem ser detectado e apaga rastros; a resposta a incidente fica cega.
No Portal: log injection na importação e ausência de logs de segurança — Aula 5.

## A10 — Server-Side Request Forgery (SSRF)
> O servidor é induzido a fazer requisições a destinos não pretendidos, informados pelo atacante.
```java
// Sem allowlist: busca QUALQUER URL, inclusive interna
new URL(urlDoUsuario).openConnection().getInputStream();
```
Impacto: acesso a serviços internos e a metadados de nuvem (ex.: credenciais em `169.254.169.254`).
No Portal: "importar catálogo por URL" busca qualquer endereço — tratamos com allowlist (design, Aula 2).

## Falhas raramente andam sozinhas
> Um incidente real quase nunca é uma única falha: o atacante encadeia várias, cada uma abrindo a porta da próxima.
- Misconfiguration entrega informação no reconhecimento.
- Injection dá o primeiro apoio dentro do sistema.
- Broken Access Control permite pivotar para dados de outros.
- Cryptographic Failure transforma o que foi roubado em dano concreto.
Dinâmica: adiante o estudo de caso — "com o que já vimos, como você juntaria 3 dessas falhas no nosso Portal?".

=== BLOCO 2 · Anatomia de um ataque + estudo de caso

## A Cyber Kill Chain (simplificada)
> Pensar o ataque em etapas ajuda a defender em cada uma delas — a ideia de "defense in depth".
- Reconnaissance (reconhecimento): mapear o alvo com o mínimo de ruído.
- Weaponization/Exploitation: escolher a falha e construir o payload que a explora.
- Post-Exploitation: escalar privilégio, mover-se lateralmente, persistir e exfiltrar dados.
Cada etapa quebrada por uma defesa encarece o ataque e aumenta a chance de detecção.

## Etapa 1 — Reconhecimento
> O atacante quer entender a aplicação antes de tocá-la: tecnologias, versões, endpoints, mensagens que vazam informação.
- Ler headers de resposta, mensagens de erro e comentários em HTML/JS.
- Enumerar endpoints e procurar rotas não linkadas (forced browsing).
- Consultar endpoints de gestão expostos (ex.: `/actuator/env`) em busca de segredos e configuração.
No Portal: é exatamente o que vocês farão no Lab 1.2, preenchendo a Ficha de Reconhecimento.
Dinâmica: "Que informação um erro 500 com stack trace entrega de graça ao atacante?"

## Etapa 2 — Weaponization e Exploitation
> De posse do mapa, o atacante escolhe a falha mais barata de explorar e monta o payload.
- Na busca vulnerável, montar um `UNION SELECT` para extrair usuários e hashes.
- Em um JWT mal validado, forjar um token com `role: ADMIN`.
A "arma" é sob medida para a falha encontrada — por isso o reconhecimento importa tanto.
Atenção: no curso, exploramos em ambiente controlado e autorizado. Isso é ética profissional, não opcional.

## Etapa 3 — Pós-exploração
> Ter um ponto de apoio raramente é o objetivo final; o valor está no que vem depois.
- Escalada vertical (virar admin) e horizontal (acessar contas de outros clientes).
- Movimento lateral para outros sistemas e persistência para manter o acesso.
- Exfiltração dos dados e, quando possível, apagar os rastros.
Conexão com A09: sem logging adequado, o atacante trabalha no escuro — e a vítima também.

## Estudo de caso "Empresa X" (1/3) — o cenário
> Caso fictício e genérico, para praticar o raciocínio de encadeamento — sem atribuir a empresas reais.
A Empresa X tem um portal B2B parecido com o nosso. Por engano, um ambiente de staging foi exposto à internet, sem WAF na frente.
Nesse ambiente, o Actuator estava aberto e a busca de produtos era vulnerável a SQL Injection.
Situação inicial: nenhuma dessas falhas, isoladamente, "parecia" crítica para o time.

## Estudo de caso "Empresa X" (2/3) — a cadeia
> Veja como quatro fraquezas "médias" somam um incidente grave.
- 1) Reconhecimento: o atacante acessa `/actuator/env` e descobre o banco, bibliotecas e um segredo de JWT.
- 2) Injection: um `UNION SELECT` na busca extrai e-mails e hashes de senha.
- 3) Cryptographic Failure: os hashes eram MD5 sem salt — quebrados offline em minutos.
- 4) Broken Access Control: com uma conta válida (ou um JWT forjado), o atacante navega por pedidos e dados de pagamento de todos os clientes.
Cada elo, sozinho, seria "só um achado". Juntos, viram vazamento de dados.

## Estudo de caso "Empresa X" (3/3) — impacto e lição
> O impacto raramente fica na técnica: ele chega ao negócio.
- Impacto: vazamento de PII e de dados de pagamento; obrigação de notificação; multa e dano de reputação.
- Cada camada quebrada foi uma oportunidade de defesa em profundidade que não existiu.
Regra de ouro: se qualquer um dos elos tivesse uma defesa (Actuator fechado, query parametrizada, bcrypt, checagem de posse), a cadeia teria sido interrompida.
Dinâmica: "Qual seria o elo mais barato de defender primeiro, e por quê?"

## O ciclo: Secure SDLC
> Segurança não é uma fase — é uma preocupação em todas as fases do desenvolvimento.
- Requisitos: escrever requisitos de segurança e abuse cases (hoje, Bloco 3).
- Design: threat modeling e princípios seguros (Aula 2).
- Implementação: validação, autenticação/autorização, criptografia (Aulas 2 a 4).
- Teste: SAST, DAST e revisão de código (Aula 5).
- Deploy e Manutenção: hardening, dependências e monitoramento (Aula 6).
Este é o mapa do curso inteiro: cada aula ataca uma fase do SDLC.

=== BLOCO 3 · Levantamento de Requisitos de Segurança

## Por que escrever requisitos de segurança
> O que não é requisito não é projetado, não é implementado com cuidado, não é testado e não é cobrado. A segurança "implícita" vira dívida — e incidente.
Se "o cliente só pode ver os próprios pedidos" nunca virou requisito escrito, o IDOR nasce como comportamento "normal" do sistema.
Requisitos de segurança guiam o design, orientam a implementação e viram critérios de aceite (testes).
No Portal: no Lab 1.3 vocês transformam funcionalidades em requisitos de segurança verificáveis.

## Requisito funcional vs. requisito de segurança
> O funcional diz o QUE o sistema faz. O de segurança diz sob QUAIS garantias e restrições — e muitas vezes o que ele NÃO deve permitir.
- Funcional: "o cliente cria um pedido e anexa um comprovante."
- Segurança: "apenas o dono do pedido anexa a ele"; "a senha nunca é armazenada de forma reversível"; "o sistema não revela se um e-mail já existe".
Muitos requisitos de segurança são "negativos" (o sistema NÃO deve…), e isso é normal e desejável.
Dinâmica: pegue uma funcionalidade citada pela turma e peça, em duplas, um requisito funcional e um de segurança para ela.

## Abuse cases e misuse cases
> É o caso de uso visto pela ótica do atacante: o que ele quer alcançar usando (ou abusando) da funcionalidade.
Formato simples: "As an attacker, I want <ação> so that <ganho>."
Exemplo: "As an attacker, I want to change the order id in the URL so that I can read other clients' orders."
De cada abuse case nasce uma contramedida — ou seja, um requisito de segurança.
Dinâmica: para o upload de comprovante, quantos abuse cases a turma consegue listar em 3 minutos?

## Security User Stories
> Uma forma prática de registrar o requisito defensivo, com critério de aceite que vira teste.
Da história do atacante deriva-se a história defensiva do sistema:
- Atacante: "quero trocar o id do pedido para ler o de outro cliente."
- Sistema: "devo verificar a posse do pedido antes de retorná-lo."
Critério de aceite: "Dado o id de um pedido de outro cliente, quando eu solicitá-lo, então recebo 403."
Regra de ouro: se você não consegue escrever o teste de aceite, o requisito ainda está vago demais.

## OWASP ASVS — o padrão de verificação
> O Application Security Verification Standard é um catálogo de requisitos de segurança verificáveis, organizado por categorias e níveis.
- Nível 1: básico, para qualquer aplicação.
- Nível 2: para aplicações que lidam com dados sensíveis — é o nosso caso (dados de pagamento).
Categorias úteis hoje: V2 (Autenticação), V3 (Sessão), V4 (Controle de acesso), V5 (Validação), V12 (Arquivos).
Como usar: não é para decorar; é para consultar e não esquecer categorias inteiras de requisito.

## Derivando requisitos — Cadastro de cliente
> Funcionalidade: cadastrar razão social, CNPJ, e-mail e senha.
- Validar e normalizar todas as entradas por allowlist (formato de e-mail, CNPJ válido, tamanho máximo). [A03/A04 · ASVS V5]
- Armazenar a senha com hashing forte (bcrypt/Argon2), nunca reversível. [A02 · ASVS V2.4]
- Não revelar se um e-mail já existe (resposta indistinguível). [A07 · ASVS V2.2]
- Proteger contra automação e força bruta (rate limiting). [A07 · ASVS V2.1]
Critério de aceite (ex.): "Dado um CNPJ com dígito verificador inválido, quando submeter, então o cadastro é rejeitado."

## Derivando requisitos — Upload de comprovante
> Funcionalidade: anexar um comprovante a um pedido.
- Aceitar apenas tipos permitidos, validando o conteúdo (magic number), não só a extensão. [A04 · ASVS V12.1]
- Gerar o nome do arquivo no servidor; a entrada do usuário nunca compõe o caminho. [A03 · ASVS V12.3]
- Impor limite de tamanho e de taxa (anti-DoS). [A04]
- Verificar que o pedido pertence ao usuário antes de aceitar o upload. [A01]
Critério de aceite (ex.): "Dado um executável renomeado para .png, quando enviar, então é rejeitado pelo magic number."

## Derivando requisitos — Checkout
> Funcionalidade: fechar o pedido e registrar o pagamento.
- Os preços vêm do servidor, nunca do cliente (evita adulteração de valor). [A04]
- Dados de pagamento cifrados em repouso (AES-GCM) e TLS em trânsito. [A02 · ASVS V6/V9]
- Registrar auditoria da transação — sem gravar dados sensíveis no log. [A09 · ASVS V7]
Erro comum: confiar no preço enviado pelo formulário do cliente. Nunca confie em dado que veio do navegador.

## Priorização por risco (e um aperitivo de STRIDE)
> Não dá para fazer tudo ao mesmo tempo: priorize os requisitos que mitigam os maiores riscos primeiro.
Risco ≈ Probabilidade × Impacto. Um IDOR fácil de explorar e que expõe dados de pagamento é risco alto — vem primeiro.
STRIDE (aprofundado na Aula 2) é uma taxonomia para enumerar ameaças: Spoofing, Tampering, Repudiation, Information Disclosure, Denial of Service, Elevation of Privilege.
Dinâmica: classifique 3 requisitos derivados hoje como risco Alto/Médio/Baixo e justifique.

## Como escrever bons requisitos de segurança
> Um bom requisito de segurança é verificável, específico, rastreável e, quando preciso, negativo.
- Verificável: existe um teste de aceite que prova (passa/falha).
- Específico: "senha via bcrypt com work factor ≥ 10", não "senha segura".
- Rastreável: ligado a uma funcionalidade e a uma ameaça (abuse case).
- Use "deve", não "deveria" — requisito não é sugestão.
Regra de ouro: escreva o requisito junto com o seu critério de aceite; se o teste não sai, o requisito ainda está vago.

## Laboratório da Aula 1 (visão geral)
> Agora é a sua vez: subir a aplicação, reconhecê-la como um atacante e transformar isso em requisitos.
- Lab 1.1 — Subir o Portal de Pedidos e navegar pelas telas.
- Lab 1.2 — Reconhecimento manual guiado, preenchendo a Ficha de Reconhecimento.
- Lab 1.3 — Escrever abuse cases e derivar requisitos de segurança para 2 funcionalidades.
O guia detalhado está no deck de laboratório (lab-aula-1) e no arquivo lab.md.

## Fechamento e ponte para a Aula 2
> Hoje entendemos o campo de jogo (ameaças e Top 10), como o atacante pensa, e como transformar isso em requisitos verificáveis.
- Você reconheceu a aplicação e produziu requisitos de segurança reais.
- Na Aula 2: por que essas falhas nascem (design seguro e threat modeling com STRIDE) e como a validação de entrada as elimina.
Tarefa: revise o OWASP Top 10 e leia uma seção do ASVS nível 1/2. Mantenha o ambiente pronto — a Aula 2 parte de `aula-2-baseline`.
