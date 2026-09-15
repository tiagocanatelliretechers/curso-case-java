# Aula 3 — Autenticação e Autorização

=== Autenticação

## O que vamos aprender hoje
> Autenticar é provar quem é; autorizar é decidir o que pode fazer. Hoje dominamos os dois — e corrigimos o IDOR e o hashing do Portal.
- Armazenamento seguro de senha, políticas modernas, MFA e SSO.
- Controle de acesso: RBAC, IDOR e autorização por objeto.
- Spring Security aplicado: filtros, method security, checagem de posse.
- Lab: explorar e corrigir o IDOR, migrar para bcrypt, limitar tentativas de login.
Regra de ouro: autenticar não é autorizar — confundir os dois é a origem de metade das falhas de acesso.

## Armazenamento de senha — o que NÃO fazer
> A forma como você guarda a senha decide o estrago quando o banco vaza (e um dia vaza).
Teoria: senha em texto claro é inaceitável; MD5/SHA-1 "puro" é rápido demais e quebrável com tabelas prontas; criptografia reversível é ruim — se você descriptografa, o atacante também.
Analogia: guardar a senha cifrada é guardar a chave do cofre ao lado do cofre.
No Portal: as senhas estão em MD5 sem sal — no laboratório da Aula 1 já extraímos um hash via SQLi.

## Hashing de senha adequado
> Para senha, use funções de hash lentas e salgadas, feitas para resistir a força bruta.
Teoria: bcrypt, Argon2 e PBKDF2 são propositalmente lentas (work factor ajustável) e usam um sal único por senha.
```java
PasswordEncoder enc = new BCryptPasswordEncoder(); // sal embutido, custo configurável
String hash = enc.encode(senha);                   // $2a$10$...
```
Analogia: o sal faz cada senha ser "temperada" diferente — duas senhas iguais viram hashes diferentes, anulando rainbow tables.
Regra de ouro: nunca escreva sua própria criptografia; use bcrypt/Argon2 com work factor calibrado (≥ 10).

## Política de senha moderna (NIST 800-63B)
> As recomendações mudaram: comprimento e verificação de vazamento valem mais que regras de complexidade.
Teoria: priorize senhas longas; verifique contra listas de senhas vazadas; use rate limiting em vez de expiração forçada e bloqueio permanente.
Erro comum: exigir troca a cada 30 dias e muitos símbolos — leva a padrões previsíveis (`Senha@Jan2025`).
Na prática: permita frases longas, bloqueie senhas conhecidamente vazadas, e limite tentativas.

## Autenticação multifator (MFA) e step-up
> Uma segunda prova de identidade é a defesa mais eficaz contra senha vazada.
Teoria: MFA combina algo que você sabe (senha), tem (TOTP/passkey) ou é (biometria). Step-up exige o 2º fator só em ações sensíveis.
Exemplo tangível: exigir TOTP apenas no checkout ou na mudança de dados cadastrais — menos atrito, mais proteção onde importa.
Analogia: a porta de casa (senha) mais o cofre com segredo próprio (2º fator) para o que é valioso.

## Gestão de credenciais e segredos
> Segredos no código são segredos vazados — é só questão de tempo.
Teoria: nunca faça hardcode de senha, chave ou token; use variáveis de ambiente e, idealmente, um secrets manager (Vault, AWS/GCP Secrets), com rotação e escopo mínimo.
No Portal: o segredo do JWT está no `application.yml` e vaza no `/actuator/env` — corrigimos na Aula 4.
Regra de ouro: se um segredo entrou no repositório, considere-o comprometido e rotacione.

## "Esqueci minha senha" seguro
> Um fluxo de recuperação mal feito vira uma máquina de descobrir contas.
Teoria: use token de uso único, aleatório (CSPRNG), com expiração curta; invalide sessões após a redefinição; e responda de forma genérica.
Como explorar: se a resposta muda quando o e-mail existe ("enviamos o link") ou não ("e-mail não encontrado"), o atacante enumera contas válidas.
No Portal: hoje o fluxo revela se o e-mail existe (user enumeration) — a resposta deve ser sempre a mesma.

## SSO — SAML e OIDC/OAuth2
> Delegar a autenticação a um provedor central é comum — e cheio de armadilhas de implementação.
Teoria: SAML (XML, corporativo/legado) e OIDC (sobre OAuth2, moderno, baseado em JWT). OAuth2 é autorização/delegação; OIDC adiciona autenticação (id_token).
Erro comum: não validar assinatura, `issuer`, `audience` e `nonce` do token — aceitando tokens forjados ou de outra aplicação.
Regra de ouro: em qualquer token, valide SEMPRE assinatura, emissor e audiência.

=== Autorização e Controle de Acesso

## Modelos de autorização — RBAC, ABAC, ReBAC
> Escolha o modelo conforme a natureza das permissões do seu domínio.
Teoria:
- RBAC: por papel (admin/usuário) — simples e estável.
- ABAC: por atributos/contexto (horário, valor, região) — mais flexível.
- ReBAC: por relacionamento ("é dono de", "é membro de") — ideal para posse e compartilhamento.
No Portal: precisamos de RBAC (admin × cliente) e de ReBAC (o cliente é dono do pedido).

## Broken Access Control na prática
> A categoria nº 1 do OWASP se manifesta de três formas que você precisa reconhecer.
- IDOR: referência direta a objeto sem checar posse (trocar o id na URL).
- Escalada vertical: um usuário comum vira admin.
- Escalada horizontal: um usuário acessa a conta de outro.
- Forced browsing: acessar URLs/rotas não linkadas na interface.
No Portal: temos os três — `/pedidos/{id}` (horizontal), `/admin` (vertical) e endpoints acessíveis diretamente.

## Autorização em todas as camadas
> Esconder o botão no frontend não é controle de acesso.
Teoria: a autorização precisa ser verificada no servidor, no ponto de acesso ao recurso — e em toda rota que importa.
Exemplo tangível: remover o link de `/admin` da tela não impede alguém de digitar `/admin` na URL.
Como se defende: negue por padrão; verifique explicitamente a permissão no servidor antes de retornar ou alterar qualquer recurso.

## Object-level authorization — o coração do anti-IDOR
> Além de "está logado?" e "tem o papel?", falta a pergunta decisiva: "este objeto é dele?".
Teoria: a autorização por objeto compara o dono do recurso com o usuário autenticado. É a causa nº 1 de vazamento em APIs REST.
```java
Pedido p = repo.findById(id).orElseThrow();
if (!p.getClienteId().equals(clienteAtual)) throw new AccessDeniedException();
```
No Portal: é exatamente a correção do Lab 3.2 — no Service e/ou com `@PostAuthorize`.

## Erros comuns de autorização
> Pequenos descuidos abrem grandes portas.
- Confiar num campo controlado pelo cliente (ex.: a `role` dentro de um JWT não verificado).
- Checar autorização só em algumas rotas, esquecendo variações (API, exportação, admin).
- Cachear a decisão de acesso e reutilizá-la fora de contexto.
Pergunta: se a aplicação lê `role: ADMIN` de um token sem verificar a assinatura, o que impede a escalada?

=== Spring Security Aplicado

## Como o Spring Security funciona
> A base técnica: uma cadeia de filtros decide autenticação e autorização de cada requisição.
Teoria: a requisição passa por uma `SecurityFilterChain`. Um `UserDetailsService` carrega o usuário; um `PasswordEncoder` verifica a senha; regras de `authorizeHttpRequests` decidem o acesso.
```java
http.authorizeHttpRequests(a -> a
   .requestMatchers("/admin/**").hasRole("ADMIN")
   .anyRequest().authenticated());
```
No Portal: hoje `/admin` está como "authenticated" — deveria ser `hasRole('ADMIN')` (Lab 3.5).

## Method security — @PreAuthorize e @PostAuthorize
> Autorização a nível de método, expressa com regras próximas da lógica de negócio.
Teoria: com `@EnableMethodSecurity`, você anota métodos com expressões SpEL. `@PreAuthorize` valida antes; `@PostAuthorize` avalia o objeto retornado.
```java
@PreAuthorize("hasRole('ADMIN')")
@PostAuthorize("returnObject.clienteId == principal.clienteId")
```
Na prática: prefira concentrar a regra de posse no Service (um único lugar) e usar as anotações como reforço.

## BCrypt com migração de senhas
> Trocar o algoritmo sem forçar todo mundo a redefinir a senha: reidratar no próximo login.
Teoria: use `BCryptPasswordEncoder`; para os hashes MD5 legados, ao logar com sucesso, regrave a senha em bcrypt.
```java
if (hash.startsWith("$2")) ok = bcrypt.matches(senha, hash);      // já é bcrypt
else if (md5(senha).equals(hash)) { ok = true; regravarBcrypt(); } // migra no login
```
No Portal: o Lab 3.3 aplica exatamente essa estratégia — usuários antigos migram sozinhos.

## Rate limiting no login
> Sem limite de tentativas, força bruta é só questão de tempo e paciência.
Teoria: limite tentativas por IP/usuário em uma janela de tempo (ex.: 5/min); em produção, use um store distribuído (Redis) para valer entre instâncias.
```java
if (!bucket(ip).tryConsume(1)) return status(429); // muitas tentativas
```
No Portal: o Lab 3.4 adiciona rate limiting e uma política mínima de senha.
Analogia: é a catraca que trava após algumas tentativas erradas, em vez de aceitar infinitas.

=== Laboratório e Fechamento

## Laboratório da Aula 3 (visão geral)
> Postura ofensiva primeiro (explorar), defensiva depois (corrigir com teste que prova).
- Lab 3.1 — Explorar o IDOR (web e API) e documentar a evidência.
- Lab 3.2 — Corrigir o IDOR (posse no Service + `@PostAuthorize`) + teste 403.
- Lab 3.3 — Migrar MD5 → bcrypt com rehash no login.
- Lab 3.4 — Rate limiting no login + política de senha.
- Lab 3.5 — Proteger `/admin` com `hasRole('ADMIN')` + teste.
No Portal: o guia detalhado está no deck de laboratório (lab-aula-3) e no lab.md.

## Fechamento e ponte para a Aula 4
> Hoje separamos com clareza autenticação de autorização e fechamos o IDOR e o hashing.
- Você explorou o IDOR, corrigiu com checagem de posse e migrou senhas para bcrypt.
- Na Aula 4: criptografia e sessão — o "Base64 que não protege" e o JWT que ninguém verifica.
Tarefa: revise bcrypt e object-level authorization. A Aula 4 parte de `aula-4-baseline`.
