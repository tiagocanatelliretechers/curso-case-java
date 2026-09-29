# Aula 3 — Roteiro de Demonstração (INSTRUTOR)

Guia **pronto para demonstrar ao vivo**, sem programar na hora. A ideia é sempre a mesma:
**mostrar a falha no `baseline` → trocar para o código pronto (`hardened`) → provar que bloqueou → mostrar o trecho que mudou.**

- **Nada de codar ao vivo.** As soluções já estão na tag `aula-3-hardened`.
- Cada demo tem: **o que falar**, **o comando**, **a saída esperada** e **qual arquivo abrir**.
- Duração total sugerida: ~25 min de demonstração (o restante é hands-on do aluno).

## Preparação (uma vez, antes da aula)
```bash
# 1) parta do estado vulnerável
git checkout aula-3-baseline
./scripts/start.sh --lab 3 --no-docker      # Windows: .\scripts\start.ps1 -Lab 3 -NoDocker
# 2) deixe dois terminais abertos: um para os comandos, outro para reiniciar o app
# 3) navegador em http://localhost:8080  (contas na tela de login)
```
> Para alternar baseline ↔ hardened durante a aula: `Ctrl+C` no app, `git checkout aula-3-hardened` (ou baseline), subir de novo. Em ~15s está no ar.

> **PowerShell:** onde aparecer `curl`, use `curl.exe`. Onde aparecer `$TOKEN=...`, veja a variante ao final de cada demo.

---

## DEMO 1 — IDOR (o carro-chefe da aula)  · ~8 min

**O que falar (contexto):** "Autenticado não é o mesmo que autorizado. O João está logado, mas será que ele só vê o *dele*? Vamos testar trocando o número na URL."

### 1a) A falha (no `baseline`)
No navegador, logado como `joao@acme.com` / `senha123`:
- Abra `http://localhost:8080/pedidos/2` (um pedido da Globex, não do João).
- **Esperado:** a tela mostra o pedido de **outro cliente**. 👈 é o IDOR.

Mostre que também vale na API (fora da tela):
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login -H 'Content-Type: application/json' \
  -d '{"email":"joao@acme.com","senha":"senha123"}' | jq -r .token)
curl -s http://localhost:8080/api/pedidos/2 -H "Authorization: Bearer $TOKEN"
```
- **Esperado:** JSON do pedido 2 (alheio) — **HTTP 200**.
- **O que dizer:** "O código buscou `findById(2)` e devolveu. Nunca perguntou de quem é."

### 1b) A correção pronta (troque para `hardened`)
```bash
# Ctrl+C no app
git checkout aula-3-hardened
./scripts/start.sh --lab 3 --no-docker
```
Repita o mesmo ataque:
- Navegador `/pedidos/2` → **403 / acesso negado**.
- API (gere o token de novo, o app reiniciou):
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login -H 'Content-Type: application/json' \
  -d '{"email":"joao@acme.com","senha":"senha123"}' | jq -r .token)
curl -s -o /dev/null -w '%{http_code}\n' http://localhost:8080/api/pedidos/2 -H "Authorization: Bearer $TOKEN"  # 403
curl -s -o /dev/null -w '%{http_code}\n' http://localhost:8080/api/pedidos/1 -H "Authorization: Bearer $TOKEN"  # 200 (o dele)
```
- **Esperado:** `403` para o pedido 2, `200` para o 1.

### 1c) Mostre o código que mudou
Abra **`PedidoService.java`** (tag hardened) e destaque a checagem de posse:
```java
if (!pedido.getClienteId().equals(clienteId))
    throw new AccessDeniedException("Pedido nao pertence ao cliente autenticado");
```
- **O que dizer:** "A regra vive no Service — vale para web e API de uma vez. É a pergunta que faltava: *este objeto é dele?*"

**Diga em voz alta o conceito:** autorização por objeto (posse / ReBAC). Fecha o IDOR sem quebrar o acesso legítimo.

---

## DEMO 2 — MD5 → BCrypt (migração automática)  · ~6 min

**O que falar:** "Se o banco vazar, o jeito que guardamos a senha decide o estrago. Vamos ver o hash real e migrar sem forçar ninguém a trocar a senha."

### 2a) A senha fraca (no `baseline`)
Abra o console H2: `http://localhost:8080/h2-console` (JDBC `jdbc:h2:mem:portal`, user/senha `sa`/`sa`) e rode:
```sql
SELECT email, senha FROM usuario;
```
- **Esperado:** hashes de **32 caracteres hex** (MD5), ex.: `admin123` → `0192023a7bbd73250516f069df18b500`.
- **O que dizer:** "MD5, sem sal. Duas pessoas com a mesma senha têm o mesmo hash — rainbow table resolve na hora."

### 2b) Migração pronta (no `hardened`)
Com o app em `aula-3-hardened`, faça **login** com um usuário antigo (`joao@acme.com` / `senha123`) e volte ao H2:
```sql
SELECT email, senha FROM usuario WHERE email = 'joao@acme.com';
```
- **Esperado:** o hash do João agora começa com **`$2a$...`** (BCrypt) — migrou sozinho no login.

### 2c) Mostre o código
Abra **`MigratingPasswordEncoder.java`** / **`PortalUserDetailsPasswordService.java`** (hardened) e destaque:
```java
if (hash.startsWith("$2")) ...            // já é bcrypt
else { ok = md5(senha).equals(hash);      // formato antigo
       if (ok) regravaEmBCrypt(); }       // migra no login
```
- **O que dizer:** "Rehash on login: o usuário nem percebe, e o banco vai ficando forte a cada acesso."

---

## DEMO 3 — Rate limiting no login  · ~4 min

**O que falar:** "Hash forte protege a senha guardada; mas sem limite de tentativas, é só testar até acertar."

### No `hardened`, provoque o bloqueio:
```bash
for i in 1 2 3 4 5 6; do
  curl -s -o /dev/null -w "tentativa $i -> %{http_code}\n" -X POST http://localhost:8080/api/auth/login \
    -H 'Content-Type: application/json' -d '{"email":"joao@acme.com","senha":"errada"}'
done
```
- **Esperado:** as primeiras retornam `401`; por volta da **6ª** vem **`429`** (Too Many Requests).
- **Mostre** o arquivo **`RateLimitFilter.java`** (bucket de 5/min por IP).
- **O que dizer:** "É a catraca que trava. Em produção, o bucket vai para o Redis para valer entre instâncias."

---

## DEMO 4 — RBAC: proteger `/admin`  · ~4 min

**O que falar:** "Escalada vertical: um cliente comum abrindo o painel de admin. A diferença entre *estar logado* e *ter o papel*."

### 4a) No `baseline`, logado como `joao@acme.com`:
- Abra `http://localhost:8080/admin` → **ele entra** (não deveria!).

### 4b) No `hardened`, repita:
- `joao@acme.com` em `/admin` → **403**.
- `admin@portal.com` em `/admin` → **200**.

### 4c) Mostre o código
Abra **`SecurityConfig.java`** (hardened):
```java
.requestMatchers("/admin/**").hasRole("ADMIN")   // era .authenticated()
```
- **O que dizer:** "`authenticated()` só exige login; `hasRole('ADMIN')` exige o papel. Uma palavra muda tudo."

---

## Encerramento da demonstração (30s)
Volte ao mapa da aula e amarre: **IDOR → posse no objeto**, **MD5 → BCrypt com migração**, **brute force → rate limiting**, **/admin → RBAC**. Em seguida, os alunos repetem no `lab.md` (agora com o "porquê" de cada passo).

## Variações PowerShell (Windows)
Substitua a captura de token por:
```powershell
$TOKEN = (Invoke-RestMethod -Method Post http://localhost:8080/api/auth/login `
  -ContentType 'application/json' -Body '{"email":"joao@acme.com","senha":"senha123"}').token
curl.exe -s -o NUL -w "%{http_code}`n" http://localhost:8080/api/pedidos/2 -H "Authorization: Bearer $TOKEN"
```

## Erros comuns na hora da demo
- **App não sobe após checkout:** aguarde ~15s; confira a porta 8080 livre.
- **Login volta pro login:** confirme `-NoDocker` (H2); o cookie de sessão já está com `same-site: lax`.
- **`jq` ausente:** copie o token do JSON manualmente, ou use a variação PowerShell acima.
- **H2 vazio depois de reiniciar:** o H2 é em memória — cada restart recria/popula a base (é esperado).
