# Postman — Cenários do Curso CASE Java (Aulas 3–6)

Coleção com os cenários **HTTP** de ataque e correção do Portal de Pedidos B2B.
Cada requisição traz, no teste e na descrição, o resultado esperado no **baseline** (vulnerável) e no **hardened** (corrigido).

## Importar
1. Postman → **Import** → selecione `CASE-Java-Aulas-3-6.postman_collection.json`.
2. (Opcional) Importe também `CASE-Java-Local.postman_environment.json` e selecione o environment **CASE Java — Local**.
   - A variável `{{baseURL}}` já vem como `http://localhost:8080`.

## Antes de rodar
- Suba o app: `./scripts/start.sh --no-docker` (ou `.\scripts\start.ps1 -NoDocker`).
- Rode primeiro a pasta **00 · Setup / Login** (captura `{{tokenJoao}}` e `{{tokenAdmin}}`).

## Como ler os resultados
- Abra o **Console** do Postman (View → Show Postman Console) — cada request loga uma linha explicando o código.
- Ex.: *IDOR alheio → 200 (baseline VULNERÁVEL) / 403 (hardened OK)*.
- Para ver a diferença: rode no código **baseline** e depois faça `git checkout hardened` (ou use a branch `hardened` do repo da aula) e rode de novo.

## O que está coberto (HTTP)
- **Aula 3:** IDOR (próprio × alheio), rate limiting no login, RBAC `/admin`.
- **Aula 4:** JWT forjado (`alg:none`) × legítimo, flags do cookie, CSRF (login web + POST sem token).
- **Aula 5:** vazamento por erro (`/pedidos/abc`), cabeçalhos de segurança (DAST).
- **Aula 6:** Actuator `/env` e `/health`, H2 console.

> SAST (SonarLint), DAST completo (ZAP), SCA (Dependency-Check), Trivy e gates de CI **não são HTTP** — ver `materiais/lab.md` e `materiais/roteiro-demonstracao.md` de cada aula.
