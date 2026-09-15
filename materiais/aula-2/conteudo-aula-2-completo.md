# Aula 2 — Design Seguro e Arquitetura + Validação de Entrada (versão completa)

=== Design Seguro e Arquitetura

## O que vamos aprender hoje
> Na Aula 1 você reconheceu a aplicação e escreveu requisitos. Hoje entendemos por que as falhas nascem — e como eliminá-las já no design e na validação de entrada.
- Princípios de secure by design, um a um, com exemplos concretos.
- Threat modeling com STRIDE, letra por letra, sobre a arquitetura do Portal.
- Fundamentos de validação: níveis, allowlist, canonicalização e injeção.
- Lab: corrigir SQL Injection, adicionar Bean Validation e proteger o upload.
Regra de ouro: a maioria das vulnerabilidades é barata de evitar no design e cara de remediar em produção.

## Por que as falhas nascem no design
> Muitas vulnerabilidades não são "bugs" — são decisões de projeto que nunca foram tomadas.
Teoria: quando o design não define onde um controle mora (validar, autorizar, cifrar), a implementação simplesmente segue sem ele — e a falha nasce "correta" do ponto de vista funcional.
Exemplo tangível: o IDOR do Portal existe porque ninguém decidiu, no design, em que camada verificar a posse do pedido.
Analogia: é como esquecer de prever o cofre na planta do banco — depois, nenhuma tranca na porta compensa.
Regra de ouro: segurança é uma decisão de arquitetura, não um remendo posterior.

## Secure by Design — a teoria
> Segurança não é um recurso adicionado depois; é consequência de boas decisões de projeto.
Teoria: "secure by design" é incorporar segurança nas decisões de arquitetura desde o início — quais dados cruzam quais fronteiras, onde os controles vivem e como o sistema se comporta sob abuso.
Por dentro: parte-se dos requisitos de segurança (Aula 1) e do modelo de ameaças para decidir a arquitetura — não o contrário.
Analogia: projetar um banco já com cofre e sala-forte, em vez de "adicionar segurança" a um galpão pronto.

## Secure by Design — no Portal
> Vamos ver onde decisões de design (ausentes) explicam as falhas do Portal.
No Portal: a busca concatena SQL (faltou decidir "consultas sempre parametrizadas"); o upload aceita qualquer arquivo (faltou "política de arquivos"); `/pedidos/{id}` não checa dono (faltou "autorização por objeto").
Exemplo tangível: nenhuma dessas é um erro de digitação — são controles que o design não previu.
Juntos: escolham uma tela do Portal e digam qual decisão de design a protegeria.

## Princípio 1 — Least Privilege (teoria)
> Cada usuário, serviço ou componente deve ter o mínimo de acesso necessário à sua função.
Teoria: privilégios excessivos ampliam o dano de qualquer comprometimento — menos acesso significa menor raio de explosão.
Por que acontece: por conveniência, dá-se acesso "amplo para não travar" — e esse acesso nunca é revisado.
Regra de ouro: comece negando tudo e conceda apenas o estritamente necessário, de forma explícita.

## Princípio 1 — Least Privilege (tangível)
> O princípio aparece em vários níveis — do banco ao token.
Exemplo tangível: a aplicação deveria conectar ao banco com um usuário que só faz SELECT/INSERT/UPDATE nas tabelas do domínio — nunca um superusuário que pode dropar tabelas.
No Portal: um JWT deveria carregar apenas o papel necessário; contas de serviço, apenas as permissões da sua função.
Analogia: o funcionário da limpeza tem a chave das salas que limpa, não a chave do cofre.

## Princípio 2 — Defense in Depth (teoria)
> Nunca dependa de uma única defesa; empilhe camadas independentes.
Teoria: cada camada (validação, autorização, criptografia, monitoramento) é independente — o atacante precisa vencer todas, não apenas uma.
Por dentro: as camadas devem ser diversas (não a mesma defesa repetida) para que uma falha não derrube as demais.
Analogia: um banco tem porta, cofre, câmeras e alarme; se uma falha, as outras seguram.

## Princípio 2 — Defense in Depth (tangível)
> No Portal, veremos a mesma ameaça barrada em mais de uma camada.
Exemplo tangível: contra SQLi temos validação de entrada (borda) E consultas parametrizadas (dados) — se a validação falhar, a parametrização ainda protege.
No Portal: contra roubo de sessão, combinamos cookie HttpOnly, CSP e HTTPS (Aulas 4 e 5).
Regra de ouro: assuma que qualquer camada isolada pode falhar — e projete para isso.

## Princípio 3 — Fail Securely (teoria)
> Diante de um erro inesperado, o estado padrão deve ser negar, nunca liberar.
Teoria: uma exceção não tratada não pode "abrir" acesso por acidente; o caminho de erro precisa terminar em negação.
Por que acontece: um `catch` genérico que engole a exceção de autorização e deixa o fluxo continuar como se estivesse tudo bem.
Exemplo tangível: se a checagem de permissão lança exceção, o resultado deve ser 403 — jamais "passa".

## Princípio 3 — Fail Securely (tangível)
> O padrão de código certo torna a falha segura por construção.
```java
boolean permitido = false;          // estado inicial NEGADO
try { permitido = verificarPermissao(user, recurso); }
catch (Exception e) { permitido = false; }  // erro => nega
if (!permitido) throw new AccessDeniedException();
```
Erro comum: iniciar `permitido = true` e só desligar em alguns casos — qualquer caminho esquecido "abre".
Regra de ouro: inicialize negando; conceda só após a verificação ter sucesso explícito.

## Princípio 4 — Separation of Duties
> Nenhuma pessoa (ou componente) deve concentrar poder suficiente para abusar sozinha.
Teoria: separar quem solicita de quem aprova, e quem opera de quem audita, reduz fraude e erro.
Exemplo tangível: quem cria um pedido de reembolso não deveria ser quem o aprova; um mesmo serviço não deveria emitir e também validar seus próprios tokens sem verificação independente.
Analogia: no banco, quem abre o cofre e quem tem o segredo do alarme são pessoas diferentes.

## Princípio 5 — Complete Mediation
> Toda requisição a um recurso é verificada — sempre, não só na primeira vez.
Teoria: não cacheie indevidamente a decisão de acesso; cada acesso ao objeto deve revalidar a autorização.
Por que acontece: por performance, valida-se uma vez e "confia" nas próximas — e o contexto muda.
No Portal: cada `GET /pedidos/{id}` precisa reverificar a posse, não confiar em uma checagem anterior da sessão.

## Princípio 6 — Economy of Mechanism (KISS)
> Simplicidade é uma propriedade de segurança: código complexo esconde falhas.
Teoria: quanto menor e mais simples o mecanismo de segurança, menor a superfície de erro e mais fácil a revisão.
Exemplo tangível: uma regra de autorização clara em um único ponto (o Service) é mais segura que a mesma regra espalhada e duplicada.
Regra de ouro: prefira o desenho mais simples que atenda ao requisito — complexidade é dívida de segurança.

## Princípio 7 — Zero Trust (teoria)
> Nunca confie por localização de rede; verifique sempre identidade e autorização.
Teoria: a rede interna não é "confiável" por definição — cada requisição prova quem é e o que pode, mesmo vinda de dentro.
Por dentro: é a evolução moderna de least privilege + complete mediation aplicada a serviços e usuários.
Analogia: o prédio onde cada porta exige o crachá — não basta "já estar lá dentro".

## Arquitetura em camadas — a teoria
> Em uma aplicação Spring, cada camada tem uma responsabilidade de segurança específica.
Teoria:
- Controller (borda): autenticação, validação sintática, mapeamento de erro.
- Service (domínio): autorização de negócio, validação semântica, invariantes (ex.: posse).
- Repository: acesso a dados parametrizado.
Regra de ouro: o design decide em qual camada cada controle vive; se não vive em nenhuma, a falha nasce.

## Arquitetura em camadas — no Portal
> Onde cada correção do curso vai morar.
Exemplo tangível:
- SQLi → Repository/Service (consulta parametrizada).
- IDOR → Service (checagem de posse), reforçado por `@PostAuthorize`.
- Formato de entrada → Controller (Bean Validation nos DTOs).
No Portal: esconder o link de `/admin` no template não é controle — a decisão mora no servidor.

## Fundamento — Fronteiras de confiança
> A pergunta central do design seguro: por onde o dado entra e a partir de onde não posso confiar nele?
Teoria: uma trust boundary é o ponto onde o dado deixa o controle do sistema; tudo que a cruza é não confiável até validação.
No Portal (fronteiras): internet↔app, app↔banco, app↔storage de anexos, app↔fornecedor (URL de catálogo).
Juntos: em qual fronteira do Portal está o maior risco, e por quê?

## Onde validar — client x server
> Validação no cliente é conveniência; segurança acontece no servidor.
Teoria: o cliente roda na máquina do usuário, que pode inspecionar e alterar qualquer requisição — controles só no cliente são decorativos.
Exemplo tangível: `maxlength` e `readonly` no HTML não impedem o atacante de enviar o que quiser via Postman.
Regra de ouro: valide (de novo) no servidor, sempre — o cliente é território do atacante.

## Onde validar — borda x domínio
> Validação acontece em lugares diferentes, com propósitos diferentes.
Teoria:
- Borda (Controller): formato e sintaxe — Bean Validation nos DTOs.
- Domínio (Service): regras de negócio e invariantes (ex.: "preço vem do servidor", "pedido pertence ao cliente").
Exemplo tangível: o formato do CNPJ é validação de borda; "este CNPJ pode fechar este pedido" é regra de domínio.

## Gateway e WAF — camada adicional, não substituta
> Proteções de borda ajudam, mas não corrigem a causa dentro do código.
Teoria: API Gateway e WAF oferecem rate limiting, filtragem e terminação de TLS — reduzem ruído e barram ataques triviais.
Erro comum: tratar o WAF como "a" segurança; o atacante reescreve o payload até passar pela assinatura.
Regra de ouro: WAF compra tempo; a correção mora no código.

## Threat Modeling — por que e quando
> Modelar ameaças é antecipar, no design, o que pode dar errado — antes de escrever a primeira linha.
Teoria: é uma atividade de time, feita sobre um diagrama do sistema e revisitada quando a arquitetura muda.
Por dentro: o resultado não é um documento morto — é uma lista de mitigações e testes priorizados.
No Portal: hoje modelamos as ameaças do Portal e derivamos mitigações — a base do Lab 2.1.

## Threat Modeling — o processo em 4 perguntas
> Um método simples e repetível, que cabe em qualquer sprint.
- 1) O que estamos construindo? (o diagrama / DFD)
- 2) O que pode dar errado? (STRIDE)
- 3) O que vamos fazer a respeito? (mitigações)
- 4) Fizemos um bom trabalho? (revisão e testes)
Regra de ouro: threat modeling só entrega valor se as respostas de (3) e (4) virarem código e teste.

## Fundamento — o diagrama de fluxo (DFD)
> Para modelar ameaças, primeiro desenhamos por onde os dados fluem e onde estão as fronteiras.
Teoria: um Data Flow Diagram mostra entidades externas, processos, armazenamentos e os fluxos — e marca as trust boundaries.
No Portal:
- Entidades externas: Cliente (browser), Admin, Fornecedor (URL).
- Processos: a aplicação web; Armazenamentos: banco e storage de anexos.
- Fluxos: login, busca, checkout, upload, importar catálogo, painel admin.

## STRIDE — a teoria
> STRIDE é uma taxonomia para enumerar ameaças; cada letra mapeia uma propriedade de segurança violada.
- Spoofing → autenticidade · Tampering → integridade · Repudiation → auditoria
- Information Disclosure → confidencialidade · Denial of Service → disponibilidade · Elevation of Privilege → autorização
Analogia: é um checklist de "tipos de coisa ruim" — para cada fluxo, percorre-se as seis letras.

## STRIDE — S de Spoofing
> Fingir ser quem não é: forjar identidade de usuário, serviço ou origem.
Teoria: ameaça à autenticidade; mitiga-se com autenticação forte, MFA e verificação de origem.
No Portal: brute force no login sem limite; um JWT sem verificação permite "ser" outro usuário.
Como se defende: rate limiting, MFA e verificação de assinatura de token (Aulas 3 e 4).

## STRIDE — T de Tampering
> Adulterar dados em trânsito, em repouso ou em parâmetros.
Teoria: ameaça à integridade; mitiga-se com validação, parametrização, HMAC/assinatura e TLS.
No Portal: SQL Injection na busca adultera a consulta; alterar o preço no checkout adultera o valor.
Como se defende: consultas parametrizadas, "preço vem do servidor", integridade criptográfica.

## STRIDE — R de Repudiation
> Negar ter feito uma ação, por falta de trilha confiável.
Teoria: ameaça à auditoria/não-repúdio; mitiga-se com logging de segurança íntegro e assinado.
No Portal: ações sensíveis sem log — impossível provar quem fez o quê.
Como se defende: registrar eventos de segurança (sem dado sensível) e proteger os logs (Aula 5).

## STRIDE — I de Information Disclosure
> Vazar informação a quem não deveria vê-la.
Teoria: ameaça à confidencialidade; mitiga-se com autorização por objeto, criptografia e controle de erros.
No Portal: IDOR expõe pedidos alheios; `/actuator/env` expõe segredos; SSRF alcança recursos internos.
Como se defende: checagem de posse, cifrar dado sensível, fechar endpoints de gestão (Aulas 3, 4, 6).

## STRIDE — D de Denial of Service
> Tornar o sistema indisponível para quem tem direito.
Teoria: ameaça à disponibilidade; mitiga-se com limites, cotas e validação de tamanho.
No Portal: upload sem limite de tamanho pode esgotar disco/memória; ausência de rate limiting facilita abuso.
Como se defende: limite de tamanho no upload, rate limiting e timeouts.

## STRIDE — E de Elevation of Privilege
> Ganhar permissões além das concedidas.
Teoria: ameaça à autorização; mitiga-se com RBAC correto, autorização por objeto e verificação de token.
No Portal: `/admin` acessível a qualquer logado; trocar `role` num JWT não verificado vira admin.
Como se defende: `hasRole('ADMIN')`, checagem de posse e assinatura de JWT (Aulas 3 e 4).

## Da matriz à mitigação (priorização)
> Cada ameaça vira um requisito/controle e um teste — priorizados por risco.
Teoria: Risco ≈ Probabilidade × Impacto; ameaças fáceis e de alto impacto vêm primeiro.
Exemplo tangível: o IDOR (fácil, expõe dados de pagamento) é prioridade máxima; um DoS difícil e teórico pode vir depois.
No Portal: a matriz STRIDE do Lab 2.1 termina em uma lista priorizada de correções.

=== Validação de Entrada

## Fundamento — os três níveis de validação
> Validar não é uma coisa só; são três perguntas diferentes sobre o mesmo dado.
Teoria:
- Sintática: o formato está correto? (ex.: e-mail tem `@`, CNPJ tem 14 dígitos)
- Semântica: o valor faz sentido? (ex.: data de nascimento não é no futuro)
- De negócio: a regra do domínio permite? (ex.: este cliente pode comprar este produto)
Regra de ouro: os três níveis são necessários; a borda cuida do sintático, o domínio do resto.

## Allowlist x Denylist — a teoria
> Aceitar só o conhecido-bom vence tentar bloquear o conhecido-ruim.
Teoria: a denylist está sempre atrás da próxima variação de ataque (encoding, Unicode, sintaxe alternativa); a allowlist define o válido e rejeita todo o resto.
Analogia: a denylist é a lista de "bandidos conhecidos" na portaria; a allowlist é a lista de convidados.
Regra de ouro: defina o formato válido e recuse o que não casar.

## Allowlist x Denylist — tangível
> O mesmo campo, protegido de dois jeitos — só um é robusto.
Exemplo tangível: para CNPJ, aceitar apenas 14 dígitos com verificação dos dígitos verificadores (allowlist) é sólido; "remover caracteres perigosos" (denylist) sempre esquece um caso.
Erro comum: sanitizar removendo `'` para "evitar SQLi" — o atacante usa encoding, comentários ou outra sintaxe.
No Portal: no Lab 2.3 aplicamos allowlist com Bean Validation e um validador de CNPJ.

## Bean Validation — a teoria
> No Spring, a validação sintática na borda é declarativa: anotações nos DTOs.
Teoria: as anotações declaram o contrato do dado; `@Valid` no controller dispara a verificação e os erros voltam num `BindingResult`.
Por dentro: por trás está o Hibernate Validator (implementação da Jakarta Bean Validation).
No Portal: hoje os DTOs não têm nenhuma anotação — qualquer valor é aceito.

## Bean Validation — anotações e uso
> Um catálogo pequeno resolve a maioria dos casos de formato.
```java
public class CadastroClienteForm {
  @NotBlank @Size(max=150) private String razaoSocial;
  @Email    @NotBlank       private String email;
  @Size(min=10)             private String senha;
  @CNPJ                     private String cnpj;   // customizada
}
```
Teoria: `@NotBlank` (não nulo/vazio), `@Size` (tamanho), `@Email`, `@Pattern` (regex), `@Positive`.
No Portal: o Lab 2.3 adiciona essas anotações e ativa `@Valid` no controller.

## Fundamento — grupos e mensagens de validação
> Nem toda regra vale em todo momento — e o erro precisa comunicar sem vazar.
Teoria: grupos de validação permitem aplicar regras diferentes por contexto (ex.: criação vs. edição); mensagens devem ser claras ao usuário e genéricas quanto a detalhes internos.
Exemplo tangível: exigir senha só no cadastro (não na edição de perfil) usando grupos.
Regra de ouro: mensagem de erro ajuda o usuário, não o atacante — nada de stack trace ou detalhe de banco.

## Validador customizado — @CNPJ
> Quando a regra é de negócio, encapsule-a como uma anotação reutilizável.
Teoria: uma anotação `@Constraint` + um `ConstraintValidator` transformam a regra (CNPJ válido) em validação declarativa, reaproveitável em qualquer DTO.
```java
@Constraint(validatedBy = CnpjValidator.class)
public @interface CNPJ { /* message, groups, payload */ }
```
No Portal: o validador confere os 14 dígitos e os dois dígitos verificadores — rejeitando `00.000.000/0000-00`.

## Fundamento — canonicalização e normalização
> Valide o dado na sua forma canônica; senão o atacante escolhe a forma que engana sua validação.
Teoria: normalize encoding, Unicode e caminhos ANTES de validar; a mesma informação tem muitas representações.
Exemplo tangível: `..%2f..%2f` e `..\` representam o mesmo `../` — validar antes de decodificar deixa passar.
Regra de ouro: decodifique/normalize primeiro, valide depois.

## Path traversal — tangível
> Um nome de arquivo malicioso pode escapar do diretório previsto.
Teoria: entradas como `../../etc/passwd` sobem na árvore de diretórios se compuserem o caminho sem checagem.
Como se defende: gere o nome no servidor; resolva o caminho final e verifique se ele permanece dentro do diretório permitido.
No Portal: hoje o upload usa o nome enviado pelo cliente — o Lab 2.4 gera um nome seguro (UUID) e valida o caminho.

## Upload seguro — o que validar
> Upload é um vetor clássico de RCE e DoS; trate cada arquivo como hostil.
Teoria: valide o conteúdo real (magic number), não só a extensão; imponha limite de tamanho; gere o nome no servidor; armazene fora do webroot e sem execução.
No Portal: hoje o upload aceita qualquer extensão, sem limite, com o nome do cliente.
Regra de ouro: a extensão do arquivo é uma sugestão do cliente — não uma verdade.

## Upload seguro — magic number (tangível)
> O tipo real de um arquivo está nos seus primeiros bytes, não no nome.
```java
byte[] head = primeirosBytes(arquivo, 8);
String hex = HexFormat.of().formatHex(head);
// %PDF=25504446  .PNG=89504e47  JPEG=ffd8ff
String tipo = detectarPorAssinatura(hex);   // rejeita se não casar allowlist
```
Como explorar (antes): renomear `shell.jsp` para `foto.png` e enviar — sem magic number, passa.
No Portal: o Lab 2.4 decide o tipo pelo conteúdo e rejeita o resto.

## Upload seguro — armazenamento
> Onde e como você grava o arquivo importa tanto quanto o que você aceita.
Teoria: grave fora do webroot (para não ser servido/executado), com nome gerado pelo servidor, e imponha o limite no framework.
```yaml
spring.servlet.multipart.max-file-size: 5MB
spring.servlet.multipart.max-request-size: 6MB
```
Regra de ouro: um arquivo enviado nunca deve ser executável nem acessível por URL direta.

## Injeção na origem — a teoria
> Validar reduz o risco, mas a defesa central contra injeção é separar comando de dado.
Teoria: injeção nasce de misturar, na mesma string, a instrução e o dado do usuário; o interpretador não distingue um do outro.
Analogia: ditar uma carta e, no meio, dizer "…e agora apague os arquivos" — sem separar, o interpretador obedece.
Regra de ouro: nunca construa comandos concatenando entrada do usuário — nem SQL, nem shell, nem HTML.

## Fundamento — PreparedStatement, por dentro
> Por que a query parametrizada é imune, e a concatenada não é.
Teoria: no PreparedStatement, o banco recebe a estrutura da query com marcadores `?` e compila o plano ANTES de receber os valores; o valor chega depois, como dado — nunca é reinterpretado como SQL.
```java
jdbc.query("SELECT ... WHERE nome LIKE ?", ps -> ps.setString(1, "%"+termo+"%"), mapper);
```
Por dentro: aspas, `--` e `UNION` dentro do valor viram texto literal, não comando.

## SQL Injection — antes e depois
> O mesmo endpoint, com e sem a defesa: a diferença é onde o dado entra.
```java
// Vulnerável: termo vira parte do comando
"SELECT ... WHERE nome LIKE '%" + termo + "%'";
// Seguro: termo é parâmetro; o curinga vai no valor
"SELECT ... WHERE nome LIKE ?"  // ps.setString(1, "%"+termo+"%")
```
Como explorar (antes): `zzz' UNION SELECT id,email,senha,0,0 FROM usuario --` vaza usuários.
No Portal: no Lab 2.2 você reproduz o ataque e depois vê o `UNION` deixar de funcionar.

## Outras injeções — OS, LDAP, XPath
> A mesma raiz aparece fora do SQL — e a defesa é a mesma ideia.
Teoria:
- OS Command: evite `Runtime.exec` com entrada do usuário; use APIs seguras e allowlist de comandos/argumentos.
- LDAP e XPath: use escaping/binding específico do provedor; não concatene filtros com dado do usuário.
Regra de ouro: para cada interpretador (banco, shell, diretório, parser), existe uma forma de passar dado como dado — use-a.

## Fundamento — validação de entrada x output encoding
> Duas defesas complementares que resolvem problemas diferentes.
Teoria: validação de entrada decide o que entra; output encoding decide como o dado é renderizado com segurança no contexto de saída (HTML, atributo, JS, URL) — é a defesa central contra XSS.
Exemplo tangível: um nome com `<script>` pode ser um dado válido; o que impede o XSS é escapá-lo ao exibir (Thymeleaf faz isso por padrão com `th:text`).
Regra de ouro: valide na entrada E codifique na saída — uma não substitui a outra.

=== Laboratório e Fechamento

## Laboratório da Aula 2 (visão geral)
> Agora você aplica o design e a validação corrigindo falhas reais do Portal.
- Lab 2.1 — Completar a matriz STRIDE com novas ameaças.
- Lab 2.2 — Corrigir o SQL Injection na busca (parametrização) + teste.
- Lab 2.3 — Bean Validation nos DTOs + validador `@CNPJ`.
- Lab 2.4 — Upload seguro (magic number, tamanho, nome gerado).
No Portal: o guia passo a passo está no deck de laboratório (lab-aula-2) e no lab.md.

## Fechamento e ponte para a Aula 3
> Hoje vimos por que as falhas nascem no design e como a validação fecha uma classe inteira de ataques.
- Você modelou ameaças com STRIDE, letra por letra, e corrigiu SQLi, validação e upload.
- Na Aula 3: autenticação e autorização — vamos explorar e corrigir de vez o IDOR e o hashing de senha.
Tarefa: revise consultas parametrizadas e Bean Validation. A Aula 3 parte de `aula-3-baseline`.
