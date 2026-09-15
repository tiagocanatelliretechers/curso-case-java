# Aula 1 — Application Security, Ameaças e Ataques + Levantamento de Requisitos

=== BLOCO 1 · Panorama de Application Security

## O que vamos aprender hoje
> Nesta aula você vai entender o campo de jogo da segurança de aplicações e sair sabendo transformar isso em requisitos concretos.
- O que é AppSec e por que ela é diferente (e complementar) da segurança de rede.
- Quem ataca, por quê, e quanto custa uma brecha.
- O OWASP Top 10 (2021) explicado item a item, com exemplos em Java e no nosso Portal.
- Como um ataque real se encadeia, da coleta de informação à exfiltração.
- Como escrever requisitos de segurança verificáveis (abuse cases, user stories, ASVS).
Dinâmica: ao longo da aula, sempre que aparecer uma falha, vamos perguntar "onde isso está no nosso Portal?".

## O que é Application Security — a teoria
> Application Security é o conjunto de práticas que garante que uma aplicação faça o que deve e, sobretudo, NÃO faça o que não deve — mesmo diante de usuários e entradas maliciosas.
Teoria: AppSec atua sobre o comportamento do software: a lógica de negócio, o fluxo de dados e as decisões que o código toma (quem entra, quem vê o quê, o que é validado). Não é um "produto" instalado no fim — é uma propriedade que emerge de como se especifica, projeta, codifica, testa e opera o sistema.
Toda vulnerabilidade é, no fundo, uma diferença entre o que o desenvolvedor imaginou que aconteceria e o que o atacante consegue fazer acontecer.
Analogia: segurança de aplicação é como as regras internas de um prédio — quem pode entrar em cada sala, quem assina o quê. O muro externo (a rede) é importante, mas não decide nada disso.

## O que é Application Security — tangível
> Vamos aterrissar o conceito no Portal de Pedidos B2B, a aplicação que você vai proteger nas 6 aulas.
Na prática: quando o cliente João abre a URL `/pedidos/2`, quem decide se ele pode ver aquele pedido não é o firewall nem o HTTPS — é uma linha de código na aplicação. Se essa checagem não existe, João vê o pedido de outro cliente.
Exemplo tangível: imagine um sistema bancário onde trocar o número da conta na URL mostra o extrato de outra pessoa. A conexão é segura (cadeado no navegador), o servidor está atualizado — e ainda assim há um vazamento grave, porque a falha está na lógica.
No Portal: essa exata falha existe hoje em `/pedidos/{id}` — vamos explorá-la no laboratório e corrigi-la na Aula 3.

## AppSec x Segurança de rede/infra — a teoria
> Redes e aplicações se protegem em camadas diferentes; uma não substitui a outra.
Teoria: firewall, IDS/IPS e segmentação operam na camada de transporte — veem endereços, portas e pacotes. Eles não entendem "este usuário pode ver este pedido", porque isso é semântica da aplicação, não do pacote.
Uma requisição de ataque na camada de aplicação quase sempre é sintaticamente perfeita: é um HTTP legítimo, na porta certa, com TLS válido. Para a rede, nada de errado; para o negócio, um desastre.
Analogia: o muro e a portaria (rede) controlam quem entra no prédio. Mas, uma vez lá dentro, quem impede um visitante de entrar na sala errada são as regras e as fechaduras internas (aplicação).

## AppSec x Segurança de rede/infra — tangível
> O mesmo ataque que a rede aprova pode ser exatamente o que derruba a confidencialidade dos dados.
Exemplo tangível: `GET /pedidos/2` passa pelo firewall sem alarme — é HTTPS na 443. Mas se o código não checa o dono, é um vazamento.
Na prática: um WAF pode bloquear padrões conhecidos (ex.: `' OR '1'='1`), o que ajuda — porém o atacante reescreve o payload até passar. WAF compra tempo; não corrige a causa.
Erro comum: "temos firewall e HTTPS, logo estamos seguros". HTTPS protege o dado em trânsito, não a lógica que decide o acesso.
Regra de ouro: quem autoriza, valida a entrada e trata o dado é o código — é lá que a segurança precisa morar.

## Quem ataca — os atores
> Defender bem começa por entender quem está do outro lado e o que essa pessoa quer.
- Cibercriminosos: motivação financeira — fraude, ransomware, roubo e revenda de dados. É a maioria.
- Insiders: funcionários ou parceiros com acesso legítimo, agindo por descuido ou má-fé.
- Hacktivistas e Estados-nação: ideologia, espionagem e sabotagem; mais sofisticados e persistentes.
- Automação e oportunistas: bots varrem a internet testando falhas conhecidas em qualquer alvo exposto.
Analogia: nem todo assaltante quer o mesmo — uns querem o cofre, outros só a bicicleta na garagem. Você defende conforme quem tem interesse no quê.

## Quem ataca — motivações e superfície
> O que muda o risco não é só quem ataca, mas quantas portas você deixa abertas.
Teoria: a superfície de ataque é o total de pontos por onde o sistema pode ser tocado — cada endpoint, formulário, parâmetro, integração e dependência é uma porta a mais.
Na prática: APIs, apps mobile, serviços em nuvem e bibliotecas de terceiros multiplicaram essa superfície nos últimos anos.
No Portal: só na nossa aplicação já temos telas web, uma API REST com JWT, upload de arquivos e uma integração que busca URL externa — cinco famílias de porta para cuidar.
Dinâmica: "Quem teria interesse no nosso Portal B2B e o que ganharia? Dados de pagamento? Lista de clientes?"

## O custo de uma brecha e o "shift-left" — teoria
> Corrigir cedo é barato; corrigir sob incidente é caríssimo. Essa é a lógica econômica da AppSec.
Teoria: o custo de corrigir uma falha cresce a cada fase do ciclo. Um requisito ajustado no início custa uma fração do mesmo problema descoberto em produção — e uma fração ínfima do custo de uma falha explorada.
- Custos diretos: resposta a incidente, perícia, correção emergencial, notificação dos afetados.
- Custos indiretos: multas (LGPD/GDPR), perda de clientes, reputação e litígio.
Regra de ouro: "shift-left" significa puxar a segurança para as fases iniciais — é a decisão de maior retorno.

## O custo de uma brecha — tangível
> Um número concreto ajuda a sentir a diferença de escala entre "cedo" e "tarde".
Exemplo tangível: mudar a regra "o preço vem do servidor" no design custa alguns minutos de discussão. Descobrir em produção que clientes alteraram preços no checkout custa estorno, auditoria, retrabalho e confiança perdida.
Analogia: é a diferença entre corrigir a planta antes de construir e quebrar a parede depois de pronta para passar o cano esquecido.
Atenção: use fontes reconhecidas (OWASP, relatórios de mercado) para embasar custos — evite citar cifras específicas sem referência verificável.

## A tríade CIA — a teoria
> Toda vulnerabilidade viola uma ou mais propriedades de segurança. Nomeá-las é a forma de comunicar impacto com precisão.
- Confidentiality (Confidencialidade): só quem pode ver, vê.
- Integrity (Integridade): dados e comportamento não são adulterados indevidamente.
- Availability (Disponibilidade): o sistema continua utilizável por quem tem direito.
Complementos essenciais: Autenticação (provar quem é), Autorização (o que pode fazer), Accountability/auditoria (registrar quem fez o quê) e Não-repúdio (não poder negar que fez).

## A tríade CIA — tangível
> Vamos classificar falhas reais do Portal por propriedade violada — é assim que se prioriza.
Exemplo tangível:
- Vazar dados de pagamento → fere Confidencialidade.
- Alterar o preço de um pedido no checkout → fere Integridade.
- Um upload de 2 GB que derruba o serviço → fere Disponibilidade.
- Login sem limite de tentativas → falha de Autenticação; ausência de logs → falha de Accountability.
Dinâmica: para cada falha citada hoje, a turma responde em voz alta "qual propriedade da CIA isso quebra?".

## OWASP Top 10 (2021) — o que é e como usar
> É a lista das dez categorias de risco mais críticas em aplicações web — uma ferramenta de conscientização e priorização, não um checklist completo.
Teoria: o Top 10 alinha vocabulário e ajuda a priorizar o que mais dói. Para verificação exaustiva existe o ASVS (veremos no Bloco 3).
Como usar: trate cada categoria como uma "família" de falhas a investigar no seu sistema, não como uma caixa a marcar.
No Portal: a aplicação nasce com exemplos vivos das dez categorias — a seguir, cada uma com teoria, exemplo tangível e onde ela mora no Portal.

## A01 Broken Access Control — o que é
> Acontece quando um usuário consegue fazer algo além do que sua permissão deveria permitir: ver dados de outros, agir como admin ou acessar telas escondidas.
Teoria: controle de acesso responde a três perguntas — "quem é você?" (autenticação), "o que seu papel permite?" (autorização por função) e "este objeto específico é seu?" (autorização por objeto). O IDOR nasce quando a terceira pergunta não é feita.
Por que acontece: é fácil esconder um botão no frontend e esquecer de checar no servidor; e é fácil confiar no id que veio na URL.
Analogia: um cartão de hotel que abre qualquer porta se você trocar o número do quarto no visor — a fechadura só olha o número, não o hóspede.

## A01 Broken Access Control — exemplo tangível
> É a categoria nº 1 de 2021 — e a mais presente no nosso Portal.
```java
@GetMapping("/pedidos/{id}")
public Pedido ver(@PathVariable Long id){
    return repo.findById(id).get();   // nunca pergunta: este pedido é do usuário logado?
}
```
Como explorar: logado como João, troque `/pedidos/1` por `/pedidos/2` e veja o pedido da Globex.
Impacto: vazamento e alteração de dados de outros clientes; escalada para funções administrativas.
No Portal: existe na web (`/pedidos/{id}`), na API (`/api/pedidos/{id}`) e no `/admin` (acessível a qualquer logado). Corrigimos na Aula 3.

## A02 Cryptographic Failures — o que é
> Dados sensíveis expostos por criptografia ausente, fraca ou mal utilizada — incluindo o erro clássico de confundir codificação com criptografia.
Teoria: há três primitivas que as pessoas confundem. Cifrar (recuperável com chave, ex.: AES) protege confidencialidade. Hashing (irreversível, ex.: bcrypt) serve para verificar senha. Codificar (Base64) só muda o formato — não protege nada.
Analogia: Base64 é escrever de trás para frente — parece embaralhado, mas qualquer um lê no espelho. Não é um cofre. Hashing é triturar o documento: não dá para "descolar" de volta.
Por que acontece: bibliotecas facilitam Base64 e MD5; o desenvolvedor acha que "está embaralhado, logo protegido".

## A02 Cryptographic Failures — exemplo tangível
> Duas falhas clássicas convivem no Portal: senha em MD5 e "dado protegido" em Base64.
```java
// "Proteção" falsa: qualquer um reverte, sem chave
String protegido = Base64.getEncoder().encodeToString(cartao.getBytes());
```
Analogia: MD5 sem "sal" é como um triturador que corta todo papel igual — existe um catálogo pronto (rainbow table) que reconhece o resultado. O sal faz cada corte ser único.
Impacto: exposição de cartões, senhas e PII; quebra de sigilo e de conformidade.
No Portal: senha em MD5 (Aula 3) e dados de pagamento em Base64 (Aula 4). Vamos decodificar o dado ao vivo para provar que não há proteção.

## A03 Injection — o que é
> Ocorre quando dado não confiável é interpretado como comando: SQL, comandos de SO, LDAP, XPath (inclui XSS na taxonomia de 2021).
Teoria: a causa raiz é misturar, na mesma string, o comando e o dado. O interpretador (banco, shell) não sabe onde termina a instrução e começa o conteúdo do usuário — então o conteúdo vira instrução.
Analogia: você dita uma carta para a secretária e, no meio do texto, diz "…e agora rasgue todos os arquivos". Sem separar instrução de conteúdo, ela obedece.
Como se defende: separar comando de dado com consultas parametrizadas — o dado nunca é interpretado como código.

## A03 Injection — exemplo tangível
> No Portal, a busca de produtos concatena o termo direto na consulta SQL.
```java
String sql = "SELECT * FROM produto WHERE nome LIKE '%" + termo + "%'";
```
Como explorar: enviar `zzz' UNION SELECT id,email,senha,0,0 FROM usuario --` na busca faz a consulta devolver e-mails e hashes de senha junto com os produtos.
Impacto: leitura e alteração de dados; em casos graves, execução remota de código.
No Portal: no laboratório desta aula você verá o vazamento de usuários pela busca; a correção (query parametrizada) é na Aula 2.

## A04 Insecure Design — o que é
> É uma falha de concepção: o controle de segurança nunca foi projetado. Diferente de um bug, aqui não há o que "consertar" — há o que "projetar".
Teoria: insecure design é a ausência de uma defesa desde o desenho — não existe fluxo previsto para o abuso. Misconfiguration (A05) é ter o controle e configurá-lo errado; insecure design é não ter o controle.
Analogia: construir um banco sem prever o cofre. Não adianta reforçar a fechadura da porta depois — o dinheiro está na mesa.
Por que acontece: pressa, foco só no caminho feliz, e ausência de threat modeling na fase de design.

## A04 Insecure Design — exemplo tangível
> Falhas de design aparecem como "o sistema simplesmente permite" algo que nunca deveria ter sido possível.
Exemplo tangível: um checkout que confia no preço enviado pelo formulário do cliente — o atacante muda o preço para R$ 1,00 e o sistema aceita, porque nunca foi projetado para desconfiar.
Na prática: login sem qualquer limite de tentativas; ausência de regras de negócio que impeçam abuso (limites, cotas, verificação de posse).
No Portal: ausência de rate limiting e de validação — tratamos no design (Aula 2) e na implementação (Aula 3).

## A05 Security Misconfiguration — o que é
> Configuração insegura: defaults perigosos, endpoints abertos, permissões largas, mensagens de erro verbosas.
Teoria: o controle existe, mas está mal ajustado — muitas vezes por herdar o padrão de fábrica ou por reaproveitar config de desenvolvimento em produção.
Analogia: mudar para uma casa nova e deixar o alarme com a senha "0000" de fábrica, com a planta da casa colada na porta.
Por que acontece: diferença entre ambientes (dev/prod), pressa no deploy e falta de hardening.

## A05 Security Misconfiguration — exemplo tangível
> O Portal expõe painéis de gestão e erros detalhados que entregam o mapa ao atacante.
```yaml
management.endpoints.web.exposure.include: "*"   # todo o Actuator, sem autenticação
server.error.include-stacktrace: always          # stack trace na cara do usuário
```
Como explorar: abrir `/actuator/env` revela segredos e configuração; um erro 500 mostra o stack trace com versões e estrutura interna.
Impacto: facilita o reconhecimento e expõe segredos diretamente.
No Portal: Actuator e H2 console abertos, stack trace ao cliente — corrigimos nas Aulas 5 e 6.

## A06 Vulnerable and Outdated Components — o que é
> Usar bibliotecas ou versões com vulnerabilidades conhecidas. Seu código pode estar impecável e ainda assim herdar a falha da dependência.
Teoria: aplicações modernas são feitas majoritariamente de código de terceiros. Cada dependência (e as dependências dela) entra no seu produto — inclusive as falhas já catalogadas como CVE.
Analogia: instalar em casa uma fechadura cujo defeito saiu no jornal — o ladrão já sabe exatamente como abri-la.
Por que acontece: dependências desatualizadas, falta de inventário (SBOM) e ausência de verificação automática no build.

## A06 Vulnerable and Outdated Components — exemplo tangível
> Uma única biblioteca antiga pode abrir a porta inteira.
Exemplo tangível: o Portal usa `commons-text` 1.9, que tem a CVE-2022-42889 ("Text4Shell") — uma falha pública, com exploit conhecido.
Como se defende: ferramentas de SCA (OWASP Dependency-Check, Snyk, Dependabot) rodam no build e apontam a versão vulnerável para você atualizar.
Impacto: exploração de uma falha documentada, muitas vezes com código de ataque pronto na internet.
No Portal: detectamos e atualizamos essa dependência na Aula 6.

## A07 Identification & Authentication Failures — o que é
> Falhas em provar e gerenciar identidade: força bruta sem limite, senhas fracas, "esqueci minha senha" que revela contas, ausência de MFA, tokens mal validados.
Teoria: autenticar é provar quem você é. As falhas surgem quando o processo permite adivinhação (sem limite de tentativas), aceita segredos fracos, ou confia em tokens sem verificar sua autenticidade.
Analogia: uma catraca que aceita infinitas tentativas de senha sem nunca travar — com tempo, qualquer combinação entra.
Por que acontece: reaproveitar hashing fraco, não limitar tentativas e confiar em campos que o cliente controla (como a "role" dentro de um token).

## A07 Identification & Authentication Failures — exemplo tangível
> No Portal, o login não limita tentativas e o "esqueci senha" entrega informação.
Como explorar: repetir o login milhares de vezes (brute force) sem bloqueio; e comparar as respostas do "esqueci minha senha" para descobrir quais e-mails existem (user enumeration).
Exemplo tangível: um JWT em que a aplicação lê a "role" do token sem verificar a assinatura — o atacante troca `ROLE_USER` por `ROLE_ADMIN` e vira administrador.
Impacto: account takeover — o atacante assume a conta da vítima.
No Portal: sem rate limiting, com user enumeration e JWT sem verificação — corrigimos nas Aulas 3 e 4.

## A08 Software & Data Integrity Failures — o que é
> Confiar em código ou dados sem verificar sua integridade: atualizações sem assinatura, deserialização insegura, pipelines de CI/CD comprometidos.
Teoria: integridade aqui é garantir que o que você executa é exatamente o que deveria — não foi trocado no caminho. Muito ligada à cadeia de suprimentos (supply chain).
Analogia: aceitar uma encomenda sem conferir o lacre — pode ter sido aberta e adulterada no trajeto.
Por que acontece: instalar pacotes de fontes não confiáveis, não assinar artefatos e não travar versões/checksums.

## A08 Software & Data Integrity Failures — exemplo tangível
> A ameaça entra por um caminho "confiável", por isso passa despercebida.
Exemplo tangível: um pacote malicioso com nome parecido com o de uma dependência real (dependency confusion) é baixado pelo build e executa código no seu servidor.
Como se defende: SBOM (inventário de componentes), verificação de checksums/assinaturas e gates no CI/CD que barram artefatos não verificados.
Impacto: execução de código malicioso a partir de uma origem aparentemente legítima.
No Portal: endereçamos com verificação de dependências e gates de pipeline na Aula 6.

## A09 Security Logging & Monitoring Failures — o que é
> Não registrar eventos de segurança (ou registrá-los de forma insegura) faz incidentes passarem despercebidos e inviabiliza a perícia.
Teoria: sem logs de autenticação, autorização e falhas, você não detecta o ataque enquanto ele acontece nem consegue reconstruí-lo depois. E logar entrada do usuário sem sanitizar cria a "log injection".
Analogia: um banco sem câmeras — o assalto acontece e ninguém sabe quem foi, como entrou nem o que levou.
Por que acontece: logging tratado como "detalhe", sem plano do que registrar (e do que nunca registrar: senha, token, cartão).

## A09 Security Logging & Monitoring Failures — exemplo tangível
> Logar o dado cru do usuário permite que ele "escreva" no seu log.
```java
log.info("Importando catálogo de: " + urlDoUsuario);   // um \n no input forja linhas falsas
```
Como explorar: enviar uma URL contendo quebras de linha para inserir entradas falsas no log e esconder rastros.
Impacto: incidentes não detectados, perícia comprometida e trilha de auditoria adulterada.
No Portal: log injection na importação de catálogo e ausência de logs de segurança — corrigimos na Aula 5.

## A10 Server-Side Request Forgery (SSRF) — o que é
> O servidor é induzido a fazer requisições a destinos não pretendidos, escolhidos pelo atacante.
Teoria: quando a aplicação busca uma URL fornecida pelo usuário sem restringir destino, o atacante usa o servidor como "procurador" — e o servidor costuma ter acesso a redes internas que o atacante não tem.
Analogia: pedir ao porteiro do prédio (que tem a chave mestra) para buscar uma encomenda num endereço que VOCÊ escolhe — e mandá-lo abrir portas internas às quais você não teria acesso.
Por que acontece: funcionalidades de "importar de URL", webhooks e proxies sem allowlist de destino.

## A10 SSRF — exemplo tangível
> No Portal, "importar catálogo por URL" busca qualquer endereço informado.
```java
new URL(urlDoUsuario).openConnection().getInputStream();   // sem validar host/esquema
```
Como explorar: informar `http://169.254.169.254/latest/meta-data/` (metadados de nuvem, com credenciais) ou `http://localhost:8080/actuator/env` (segredos internos), ou `file:///etc/passwd`.
Impacto: acesso a serviços internos e a credenciais de nuvem — um dos vetores mais graves em ambientes cloud.
No Portal: tratamos com allowlist de destino no design (Aula 2).

## Falhas raramente andam sozinhas
> Um incidente real quase nunca é uma única falha: o atacante encadeia várias, cada uma abrindo a porta da próxima.
- Misconfiguration entrega informação no reconhecimento (A05).
- Injection dá o primeiro apoio dentro do sistema (A03).
- Broken Access Control permite pivotar para dados de outros (A01).
- Cryptographic Failure transforma o que foi roubado em dano concreto (A02).
Dinâmica: "Com o que já vimos, como você juntaria três dessas falhas no nosso Portal para chegar aos dados de pagamento?"

=== BLOCO 2 · Anatomia de um ataque + estudo de caso

## A Cyber Kill Chain — a teoria
> Pensar o ataque em etapas revela onde é possível interrompê-lo — a ideia de "defense in depth".
Teoria: um ataque dirigido costuma seguir uma sequência: reconhecimento → preparação da "arma" → exploração → pós-exploração (persistência, movimento, exfiltração). Cada etapa é uma oportunidade de defesa.
Analogia: um assalto planejado — observar a rotina, escolher a ferramenta, arrombar, e então agir lá dentro. Uma câmera na etapa de observação já muda o jogo.
Regra de ouro: você não precisa vencer em todas as etapas; interromper qualquer uma quebra a cadeia.

## Etapa 1 — Reconhecimento
> O atacante quer entender a aplicação antes de tocá-la de verdade: tecnologias, versões, endpoints e o que vaza informação.
Na prática: ler headers de resposta e mensagens de erro; procurar comentários em HTML/JS; enumerar rotas; e consultar endpoints de gestão expostos (como `/actuator/env`).
Exemplo tangível: um erro 500 com stack trace entrega, de graça, o framework, a versão e a estrutura do banco — economizando horas de trabalho do atacante.
No Portal: é exatamente o que você fará no Lab 1.2, preenchendo a Ficha de Reconhecimento.
Dinâmica: "Que informação um header `Server` ou um stack trace entrega sobre a nossa stack?"

## Etapa 2 — Preparação e Exploração
> De posse do mapa, o atacante escolhe a falha mais barata de explorar e constrói o payload sob medida.
Na prática: na busca vulnerável, montar um `UNION SELECT` para extrair usuários; num JWT mal validado, forjar um token com `role: ADMIN`.
Exemplo tangível: o mesmo endpoint de busca que serve o cliente vira a porta de saída dos dados — a "arma" é feita para a falha encontrada no reconhecimento.
Atenção: no curso, exploramos apenas em ambiente controlado e autorizado. Isso é ética profissional — e, fora de um escopo autorizado, é crime.

## Etapa 3 — Pós-exploração
> Ter um ponto de apoio raramente é o objetivo final; o valor está no que vem depois.
- Escalada vertical (virar admin) e horizontal (acessar contas de outros clientes).
- Movimento lateral para outros sistemas e persistência para manter o acesso.
- Exfiltração dos dados e, quando possível, apagar rastros.
Analogia: entrar pela janela é só o começo; o assalto de fato é circular pela casa, achar o cofre e sair sem deixar digitais.
Conexão com A09: sem logging adequado, o atacante trabalha no escuro — e a vítima também.

## Estudo de caso "Empresa X" (1/3) — o cenário
> Caso fictício e genérico, para praticar o raciocínio de encadeamento — sem atribuir a empresas reais.
A Empresa X tem um portal B2B parecido com o nosso. Por engano, um ambiente de staging foi exposto à internet, sem WAF na frente.
Nesse ambiente, o Actuator estava aberto e a busca de produtos era vulnerável a SQL Injection.
Na prática: isoladamente, o time considerava cada um desses pontos "de baixa prioridade".

## Estudo de caso "Empresa X" (2/3) — a cadeia
> Veja como quatro fraquezas "médias" somam um incidente grave.
- 1) Reconhecimento: o atacante acessa `/actuator/env` e descobre o banco, as bibliotecas e um segredo de JWT.
- 2) Injection: um `UNION SELECT` na busca extrai e-mails e hashes de senha.
- 3) Cryptographic Failure: os hashes eram MD5 sem sal — quebrados offline em minutos.
- 4) Broken Access Control: com uma conta válida (ou um JWT forjado), o atacante navega por pedidos e dados de pagamento de todos os clientes.
Regra de ouro: cada elo, sozinho, era "só um achado". Juntos, viram vazamento.

## Estudo de caso "Empresa X" (3/3) — impacto e lição
> O impacto raramente fica na técnica: ele chega ao negócio.
- Impacto: vazamento de PII e de dados de pagamento; obrigação de notificação; multa e dano de reputação.
- Cada camada quebrada foi uma defesa em profundidade que não existiu.
Exemplo tangível: bastaria UMA defesa em qualquer elo — Actuator fechado, query parametrizada, bcrypt, ou checagem de posse — para interromper a cadeia inteira.
Dinâmica: "Qual seria o elo mais barato de defender primeiro no nosso Portal, e por quê?"

## O ciclo: Secure SDLC — teoria
> Segurança não é uma fase — é uma preocupação presente em todas as fases do desenvolvimento.
- Requisitos: escrever requisitos de segurança e abuse cases (hoje).
- Design: threat modeling e princípios seguros (Aula 2).
- Implementação: validação, autenticação/autorização, criptografia (Aulas 2 a 4).
- Teste: SAST, DAST e revisão de código (Aula 5).
- Deploy e Manutenção: hardening, dependências e monitoramento (Aula 6).
Analogia: segurança é como qualidade — não se "inspeciona no fim", se constrói em cada etapa.

## Mapa do curso por fase do SDLC
> Cada aula deste curso ataca uma fase — e todas usam o mesmo Portal como campo de treino.
- Aula 1 (hoje): ameaças, Top 10 e requisitos de segurança.
- Aula 2: design seguro, threat modeling (STRIDE) e validação de entrada.
- Aula 3: autenticação e autorização (corrige o IDOR e o hashing).
- Aula 4: criptografia e sessão (corrige o Base64 e o JWT).
- Aula 5: tratamento de erros e testes (SAST/DAST).
- Aula 6: deploy seguro, dependências e revisão — fechando o ciclo.

=== BLOCO 3 · Levantamento de Requisitos de Segurança

## Por que escrever requisitos de segurança — teoria
> O que não é requisito não é projetado, não é implementado com cuidado, não é testado e não é cobrado. A segurança "implícita" vira dívida — e incidente.
Teoria: requisitos de segurança tornam explícitas as garantias e restrições do sistema. Eles guiam o design, orientam a implementação e viram critérios de aceite (testes).
Analogia: é o contrato de uma obra — o que não está escrito no contrato não é construído nem cobrado. "Segurança" precisa estar no contrato.
No Portal: se "o cliente só vê os próprios pedidos" nunca virou requisito, o IDOR nasce como comportamento "normal" do sistema.

## Requisito funcional x requisito de segurança
> O funcional diz o QUE o sistema faz. O de segurança diz sob QUAIS garantias — e muitas vezes o que ele NÃO deve permitir.
Exemplo tangível:
- Funcional: "o cliente cria um pedido e anexa um comprovante."
- Segurança: "apenas o dono do pedido anexa a ele"; "a senha nunca é armazenada de forma reversível"; "o sistema não revela se um e-mail já existe".
Teoria: muitos requisitos de segurança são "negativos" (o sistema NÃO deve…), e isso é esperado — eles descrevem o abuso a impedir.
Dinâmica: em duplas, escrevam um requisito funcional e um de segurança para o login.

## Abuse cases e misuse cases
> É o caso de uso visto pela ótica do atacante: o que ele quer alcançar usando (ou abusando) da funcionalidade.
Teoria: para cada funcionalidade, imaginamos como ela pode ser abusada; de cada abuso nasce uma contramedida — ou seja, um requisito de segurança.
Formato: "As an attacker, I want <ação> so that <ganho>."
Exemplo tangível: "As an attacker, I want to change the order id in the URL so that I can read other clients' orders."
Analogia: para projetar uma casa segura, pense como o ladrão — por onde ele entraria?

## Security User Stories
> Uma forma prática de registrar o requisito defensivo, já com o critério de aceite que vira teste.
Teoria: da história do atacante deriva-se a história defensiva do sistema, e dela o teste.
Exemplo tangível:
- Atacante: "quero trocar o id do pedido para ler o de outro cliente."
- Sistema (requisito): "devo verificar a posse do pedido antes de retorná-lo."
- Critério de aceite: "Dado o id de um pedido de outro cliente, quando eu solicitá-lo, então recebo 403."
Regra de ouro: se você não consegue escrever o teste de aceite, o requisito ainda está vago demais.

## OWASP ASVS — o padrão de verificação
> O Application Security Verification Standard é um catálogo de requisitos de segurança verificáveis, organizado por categorias e níveis.
Teoria: em vez de reinventar, você consulta um padrão maduro para não esquecer categorias inteiras de requisito.
- Nível 1: básico, para qualquer aplicação. Nível 2: aplicações com dados sensíveis — o nosso caso (dados de pagamento).
- Categorias úteis hoje: V2 (Autenticação), V3 (Sessão), V4 (Controle de acesso), V5 (Validação), V12 (Arquivos).
Analogia: é o checklist de vistoria do imóvel — você não confia na memória, você percorre a lista.

## Derivando requisitos — Cadastro de cliente
> Funcionalidade: cadastrar razão social, CNPJ, e-mail e senha. Vamos transformá-la em requisitos verificáveis.
- Validar e normalizar todas as entradas por allowlist (e-mail, CNPJ, tamanho máximo). [A03/A04 · ASVS V5]
- Armazenar a senha com hashing forte (bcrypt/Argon2), nunca reversível. [A02 · ASVS V2.4]
- Não revelar se um e-mail já existe (resposta indistinguível). [A07 · ASVS V2.2]
- Proteger contra automação e força bruta (rate limiting). [A07 · ASVS V2.1]
Exemplo tangível (critério de aceite): "Dado um CNPJ com dígito verificador inválido, quando submeter, então o cadastro é rejeitado."

## Derivando requisitos — Upload de comprovante
> Funcionalidade: anexar um comprovante a um pedido.
- Aceitar apenas tipos permitidos, validando o conteúdo (magic number), não só a extensão. [A04 · ASVS V12.1]
- Gerar o nome do arquivo no servidor; a entrada do usuário nunca compõe o caminho. [A03 · ASVS V12.3]
- Impor limite de tamanho e de taxa (anti-DoS). [A04]
- Verificar que o pedido pertence ao usuário antes de aceitar o upload. [A01]
Exemplo tangível (critério de aceite): "Dado um executável renomeado para .png, quando enviar, então é rejeitado pelo magic number."

## Derivando requisitos — Checkout
> Funcionalidade: fechar o pedido e registrar o pagamento.
- Os preços vêm do servidor, nunca do cliente (evita adulteração de valor). [A04]
- Dados de pagamento cifrados em repouso (AES-GCM) e TLS em trânsito. [A02 · ASVS V6/V9]
- Registrar auditoria da transação — sem gravar dados sensíveis no log. [A09 · ASVS V7]
Erro comum: confiar no preço enviado pelo formulário. Nunca confie em dado que veio do navegador — ele é território do atacante.

## Priorização por risco (e um aperitivo de STRIDE)
> Não dá para fazer tudo ao mesmo tempo: priorize os requisitos que mitigam os maiores riscos primeiro.
Teoria: Risco ≈ Probabilidade × Impacto. Um IDOR fácil de explorar e que expõe dados de pagamento é alto risco — vem primeiro.
STRIDE (aprofundado na Aula 2) é uma taxonomia para enumerar ameaças: Spoofing, Tampering, Repudiation, Information Disclosure, Denial of Service, Elevation of Privilege.
Analogia: numa emergência, você apaga primeiro o incêndio que ameaça a estrutura, não a lixeira que fumega.
Dinâmica: classifiquem três requisitos de hoje como risco Alto/Médio/Baixo e justifiquem.

## Como escrever bons requisitos de segurança
> Um bom requisito de segurança é verificável, específico, rastreável e, quando preciso, negativo.
- Verificável: existe um teste de aceite que prova (passa/falha).
- Específico: "senha via bcrypt com work factor ≥ 10", não "senha segura".
- Rastreável: ligado a uma funcionalidade e a uma ameaça (abuse case).
- Use "deve", não "deveria" — requisito não é sugestão.
Regra de ouro: escreva o requisito junto com o critério de aceite; se o teste não sai, o requisito ainda está vago.

## Laboratório da Aula 1 (visão geral)
> Agora é a sua vez: subir a aplicação, reconhecê-la como um atacante e transformar isso em requisitos.
- Lab 1.1 — Subir o Portal de Pedidos e navegar pelas telas.
- Lab 1.2 — Reconhecimento manual guiado, preenchendo a Ficha de Reconhecimento.
- Lab 1.3 — Escrever abuse cases e derivar requisitos de segurança para 2 funcionalidades.
No Portal: o guia detalhado está no deck de laboratório (lab-aula-1) e no arquivo lab.md.

## Fechamento e ponte para a Aula 2
> Hoje entendemos o campo de jogo (ameaças e Top 10), como o atacante pensa, e como transformar isso em requisitos verificáveis.
- Você reconheceu a aplicação e produziu requisitos de segurança reais.
- Na Aula 2: por que essas falhas nascem (design seguro e threat modeling com STRIDE) e como a validação de entrada as elimina.
Tarefa: revise o OWASP Top 10 e leia uma seção do ASVS nível 1/2. Mantenha o ambiente pronto — a Aula 2 parte de `aula-2-baseline`.
