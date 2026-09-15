# Aula 2 — Design Seguro e Arquitetura + Validação de Entrada

=== Design Seguro e Arquitetura

## O que vamos aprender hoje
> Na Aula 1 você reconheceu a aplicação e escreveu requisitos. Hoje entendemos por que as falhas nascem — e como eliminá-las já no design e na validação de entrada.
- Princípios de secure by design e como eles guiam decisões.
- Threat modeling com STRIDE sobre a arquitetura do Portal.
- Validação de entrada: allowlist, Bean Validation e upload seguro.
- Lab: corrigir SQL Injection, adicionar validação e proteger o upload.
Regra de ouro: a maioria das vulnerabilidades é barata de evitar no design e cara de remediar em produção.

## Secure by Design — a teoria
> Segurança não é um recurso a ser adicionado depois; é uma consequência de boas decisões de projeto.
Teoria: "secure by design" significa incorporar segurança nas decisões de arquitetura desde o início — escolher onde os controles moram, quais dados cruzam quais fronteiras e como o sistema se comporta sob abuso.
Analogia: é a diferença entre projetar um banco já com cofre e sala-forte, e tentar "adicionar segurança" a um galpão depois de pronto.
No Portal: o IDOR existe porque a decisão "onde verificar a posse do pedido" nunca foi tomada no design.

## Princípio — Least Privilege
> Cada usuário, serviço ou componente deve ter o mínimo de acesso necessário para sua função.
Teoria: privilégios excessivos ampliam o dano de qualquer comprometimento. Menos acesso = menor raio de explosão.
Exemplo tangível: a aplicação deveria conectar ao banco com um usuário que só faz o necessário — não com um superusuário que pode apagar tabelas.
Analogia: o funcionário da limpeza tem a chave das salas que limpa, não a chave do cofre.
Juntos: no Portal, que privilégios o usuário de banco realmente precisa?

## Princípio — Defense in Depth
> Nunca dependa de uma única defesa; empilhe camadas para que a falha de uma não comprometa o todo.
Teoria: cada camada (validação, autorização, criptografia, monitoramento) é independente; o atacante precisa vencer todas.
Exemplo tangível: mesmo com um WAF na frente, o código ainda usa consultas parametrizadas — se o WAF falha, a parametrização segura.
Analogia: um banco tem porta, cofre, câmeras e alarme; se uma falha, as outras seguram.
Regra de ouro: assuma que qualquer camada isolada pode falhar — e projete para isso.

## Princípio — Fail Securely
> Diante de um erro inesperado, o estado padrão deve ser negar, nunca liberar.
Teoria: exceções não tratadas não podem "abrir" acesso ou autorização por acidente.
Exemplo tangível: se a checagem de permissão lança uma exceção, o resultado deve ser 403 — jamais "deixa passar".
Erro comum: um `catch` genérico que engole a exceção de autorização e continua o fluxo como se estivesse tudo bem.
Como se defende: estado inicial "negar"; só conceda acesso após a verificação explícita ter sucesso.

## Princípios — Mediação, Simplicidade e Zero Trust
> Mais três ideias que fecham o conjunto de design seguro.
- Complete mediation: toda requisição a um recurso é verificada — não cacheie indevidamente a decisão de acesso.
- Economy of mechanism (KISS): simplicidade reduz a superfície de erro; código complexo esconde falhas.
- Zero Trust: nunca confie por localização de rede; verifique sempre identidade e autorização.
Analogia: Zero Trust é o prédio onde cada porta exige o crachá — não basta "já estar lá dentro".

## Arquitetura em camadas: onde a segurança mora
> Em uma aplicação Spring, cada camada tem uma responsabilidade de segurança específica.
Teoria:
- Controller (borda): autenticação, validação sintática, mapeamento de erro.
- Service (domínio): autorização de negócio, validação semântica, invariantes (ex.: posse do recurso).
- Repository: acesso a dados parametrizado.
No Portal: o IDOR se resolve no Service (verificar dono), não no template — esconder um link não é controle.
Regra de ouro: o design decide em qual camada cada controle vive; se não vive em nenhuma, a falha nasce.

## Onde validar: borda x domínio
> Validação acontece em lugares diferentes, com propósitos diferentes — e nunca só no cliente.
- Client-side: apenas UX; nunca confiar.
- Server-side na borda: formato e sintaxe (Bean Validation nos DTOs).
- Domínio: regras de negócio e invariantes (ex.: "preço vem do servidor").
Exemplo tangível: o navegador pode validar o formato do e-mail para ajudar o usuário, mas o servidor precisa validar de novo — o atacante ignora o navegador.

## Gateway e WAF: camada adicional, não substituta
> Proteções de borda ajudam, mas não corrigem a causa dentro do código.
Teoria: API Gateway e WAF oferecem rate limiting, filtragem e terminação de TLS — reduzem ruído e barram ataques triviais.
Erro comum: tratar o WAF como "a" segurança. O atacante reescreve o payload até passar pela assinatura.
Regra de ouro: WAF compra tempo; a correção mora no código.

## Threat Modeling — por que e quando
> Modelar ameaças é antecipar, no design, o que pode dar errado — antes de escrever a primeira linha.
Teoria: responda quatro perguntas — o que estamos construindo? o que pode dar errado? o que faremos? fizemos bem?
Na prática: é uma atividade de time, feita sobre um diagrama do sistema, e revisitada quando a arquitetura muda.
No Portal: hoje modelamos as ameaças do Portal e derivamos mitigações — a base do Lab 2.1.

## O diagrama de fluxo (DFD) do Portal
> Para modelar ameaças, primeiro desenhamos por onde os dados fluem e onde estão as fronteiras de confiança.
Teoria: um Data Flow Diagram mostra entidades externas, processos, armazenamentos e os fluxos entre eles — e marca as trust boundaries.
No Portal:
- Entidades externas: Cliente (browser), Admin, Fornecedor (URL de catálogo).
- Processos: a aplicação web; Armazenamentos: banco e storage de anexos.
- Fronteiras: internet↔app, app↔banco, app↔storage, app↔fornecedor.
Juntos: em qual fronteira está o maior risco, e por quê?

## STRIDE — a teoria
> STRIDE é uma taxonomia para enumerar ameaças; cada letra mapeia uma propriedade de segurança violada.
- Spoofing (autenticidade) · Tampering (integridade) · Repudiation (auditoria)
- Information Disclosure (confidencialidade) · Denial of Service (disponibilidade) · Elevation of Privilege (autorização)
Analogia: é um checklist de "tipos de coisa ruim" — para cada fluxo, você percorre as seis letras e pergunta "isto se aplica aqui?".

## STRIDE aplicado ao Portal
> Percorrendo as letras sobre fluxos reais, as ameaças (e as mitigações) aparecem.
- Login (Spoofing): brute force sem limite → rate limiting/MFA.
- Busca (Tampering/Info Disclosure): SQL Injection → query parametrizada.
- Ver pedido (Elevation/Info Disclosure): IDOR → checar posse.
- Importar catálogo (Info Disclosure): SSRF → allowlist de destino.
- Upload (Tampering/DoS): arquivo malicioso/gigante → validar tipo e tamanho.
Juntos: adicionem uma ameaça de Repudiation (ex.: ações sem trilha de log) e sua mitigação.

## Do modelo à mitigação (priorização)
> Cada ameaça vira um requisito/controle e um teste — priorizados por risco.
Teoria: Risco ≈ Probabilidade × Impacto. Ameaças fáceis de explorar e de alto impacto vêm primeiro.
Exemplo tangível: o IDOR (fácil, expõe dados de pagamento) é prioridade máxima; um DoS teórico e difícil pode vir depois.
Regra de ouro: threat modeling só entrega valor se virar mitigação e teste — não pare no diagrama.

=== Validação de Entrada

## Fundamentos da validação de entrada
> A primeira linha de defesa contra injeção e abuso: decidir o que entra, antes de usar.
Teoria: valide em três níveis — sintático (formato), semântico (faz sentido) e de negócio (regra do domínio).
- Allowlist (aceitar só o conhecido-bom) é preferível a denylist (bloquear o conhecido-ruim).
Analogia: a denylist é a lista de "bandidos conhecidos" na portaria — sempre atrás do próximo disfarce; a allowlist é a lista de convidados.

## Allowlist x Denylist — por que allowlist vence
> Bloquear o que é ruim é uma corrida perdida; aceitar só o que é bom é uma decisão estável.
Exemplo tangível: para um CNPJ, aceitar apenas 14 dígitos com verificação dos dígitos verificadores (allowlist) é robusto; tentar "remover caracteres perigosos" (denylist) sempre esquece um caso.
Erro comum: sanitizar removendo `'` para "evitar SQLi" — o atacante usa encoding, comentários ou outra sintaxe.
Regra de ouro: defina o formato válido e rejeite todo o resto.

## Bean Validation (Jakarta) — teoria e uso
> No Spring, a validação sintática na borda é declarativa: anotações nos DTOs.
Teoria: anotações como `@NotBlank`, `@Size`, `@Email`, `@Pattern` declaram o contrato; `@Valid` no controller dispara a verificação.
```java
public class CadastroClienteForm {
  @NotBlank @Size(max=150) private String razaoSocial;
  @Email @NotBlank private String email;
  @Size(min=10) private String senha;
}
```
No Portal: hoje os DTOs não têm nenhuma anotação — qualquer valor é aceito. O Lab 2.3 corrige isso.

## Validador customizado — @CNPJ
> Quando a regra é de negócio, encapsule-a como uma anotação reutilizável.
Teoria: uma anotação `@Constraint` + um `ConstraintValidator` transformam uma regra (CNPJ válido) em validação declarativa, reaproveitável em qualquer DTO.
```java
@Constraint(validatedBy = CnpjValidator.class)
public @interface CNPJ { /* message, groups, payload */ }
```
Exemplo tangível: o validador confere os 14 dígitos e os dois dígitos verificadores — rejeitando `00.000.000/0000-00`.
Na prática: valida no servidor, sempre; o mesmo `@CNPJ` protege web e API.

## Canonicalização e normalização
> Valide o dado na sua forma canônica — senão o atacante escolhe a forma que engana sua validação.
Teoria: antes de validar, normalize encoding, Unicode e caminhos. Path traversal usa `../`, `%2e%2e` e variações para escapar de diretórios.
Exemplo tangível: um nome de arquivo `..%2f..%2fetc%2fpasswd` só é perigoso porque foi validado antes de ser decodificado.
Como se defende: decodifique/normalize primeiro, valide depois; para arquivos, resolva o caminho e verifique se ele permanece dentro do diretório permitido.

## Upload de arquivo seguro
> Upload é um vetor clássico de RCE e DoS — trate cada arquivo como hostil.
Teoria: valide o conteúdo real (magic number), não só a extensão; imponha limite de tamanho; gere o nome no servidor; armazene fora do webroot e sem execução.
```java
// tipo decidido pelo conteúdo, nome gerado pelo servidor
String tipo = detectarPorMagicNumber(bytes);      // pdf/png/jpg
String nome = UUID.randomUUID() + "." + tipo;
```
No Portal: hoje o upload aceita qualquer extensão e usa o nome do cliente (permite `../`). O Lab 2.4 corrige.

## Injeção na origem — a defesa primária
> Validar reduz o risco, mas a defesa central contra injeção é separar comando de dado.
Teoria: em SQL, use consultas parametrizadas (PreparedStatement / binding do JPA); o dado vai separado da query e nunca é interpretado como código.
Analogia: é dar à secretária o texto num envelope lacrado ("isto é conteúdo") em vez de ditá-lo junto com as instruções.
Regra de ouro: nunca construa comandos concatenando entrada do usuário — nem SQL, nem shell, nem HTML.

## SQL Injection — antes e depois
> O mesmo endpoint, com e sem a defesa: a diferença é onde o dado do usuário entra.
```java
// Vulnerável: termo vira parte do comando
"SELECT ... WHERE nome LIKE '%" + termo + "%'";

// Seguro: termo é parâmetro, o curinga vai no valor
jdbc.query("SELECT ... WHERE nome LIKE ?", ps -> ps.setString(1, "%"+termo+"%"), mapper);
```
Como explorar (antes): `zzz' UNION SELECT id,email,senha,0,0 FROM usuario --` vaza usuários.
No Portal: no Lab 2.2 você reproduz o ataque e depois vê o `UNION` deixar de funcionar após parametrizar.

## Outras injeções: OS, LDAP, XPath
> A mesma raiz (misturar comando e dado) aparece fora do SQL.
- OS Command: evite `Runtime.exec` com entrada do usuário; use APIs seguras e allowlist de comandos/argumentos.
- LDAP e XPath: use escaping/binding específico do provedor — não concatene filtros com dado do usuário.
Regra de ouro: para cada interpretador (banco, shell, diretório, parser), existe uma forma de passar dado como dado — use-a.

=== Laboratório e Fechamento

## Laboratório da Aula 2 (visão geral)
> Agora você aplica o design e a validação corrigindo falhas reais do Portal.
- Lab 2.1 — Completar a matriz STRIDE com novas ameaças.
- Lab 2.2 — Corrigir o SQL Injection na busca (parametrização) + teste.
- Lab 2.3 — Bean Validation nos DTOs + validador `@CNPJ`.
- Lab 2.4 — Upload seguro (magic number, tamanho, nome gerado).
No Portal: o guia passo a passo está no deck de laboratório (lab-aula-2) e no lab.md.

## Fechamento e ponte para a Aula 3
> Hoje vimos por que as falhas nascem no design e como a validação de entrada fecha uma classe inteira de ataques.
- Você modelou ameaças com STRIDE e corrigiu SQLi, validação e upload.
- Na Aula 3: autenticação e autorização — vamos explorar e corrigir de vez o IDOR e o hashing de senha.
Tarefa: revise consultas parametrizadas e Bean Validation. A Aula 3 parte de `aula-3-baseline`.
