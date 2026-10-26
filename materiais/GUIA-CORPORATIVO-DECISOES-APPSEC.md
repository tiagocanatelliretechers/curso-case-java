# Guia Corporativo de Decisões — AppSec

Material **orientativo** para o dia a dia de desenvolvimento: matrizes de decisão sobre
**autenticação, MFA, controle de acesso, sessão/token e segredos**. Serve como referência
rápida ("qual escolher?") e base para padronizar as decisões entre times.

> Este guia resume **boas práticas** (OWASP ASVS, NIST SP 800-63B). Não substitui a política
> de segurança da sua organização — adapte os limiares (ex.: work factor, expiração) ao seu contexto.
> Use-o em design reviews, PRs e onboarding.

---

## Princípios transversais (valem para tudo)

| Princípio | O que significa na prática |
|-----------|----------------------------|
| **Default deny** | Na dúvida, negar. Acesso é concedido explicitamente, nunca por omissão. |
| **Least privilege** | Cada usuário/serviço com o mínimo necessário, pelo menor tempo. |
| **Defense in depth** | Várias camadas; nenhuma é o único ponto de falha. |
| **Fail securely** | Em erro/exceção, negar — nunca "abrir" o acesso. |
| **Complete mediation** | Toda requisição a um recurso é verificada no servidor (sem cachear decisão indevidamente). |
| **Zero Trust** | Nunca confiar por localização de rede; verificar identidade e autorização sempre. |
| **Verificar no servidor** | Cliente é conveniência/UX. A decisão de segurança mora no backend. |

---

## 1. Autenticação — qual método usar

**Pergunta-guia:** *Quem está se autenticando — uma pessoa ou um sistema?* E *qual o nível de risco?*

| Cenário | Recomendado | Evitar | Observação |
|---------|-------------|--------|------------|
| Login de usuário final (web/app) | **OIDC via IdP** (SSO) **ou** senha **+ MFA** | Senha sozinha | Prefira **passkeys/WebAuthn** quando possível |
| App interno (colaboradores) | **SSO corporativo** (OIDC/SAML) + MFA | Contas locais por app | Centraliza offboarding e políticas |
| Alta segurança / anti-phishing | **Passkeys / WebAuthn (FIDO2)** | SMS OTP como fator único | Resistente a phishing por design |
| Serviço→serviço (backend) | **mTLS** ou **OAuth2 client credentials** (token curto) | API key estática longa | Identidade de workload; rotação automática |
| API pública p/ parceiros/devs | **API key com escopo + rotação** ou **OAuth2** | Credencial de usuário em API | Uma chave por cliente; revogável |
| Federação com legado | **SAML 2.0** | Integração caseira de SSO | Valide assinatura, issuer, audience |
| CI/CD → nuvem | **OIDC federation** (ex.: GitHub OIDC → cloud) | Chave de acesso longa no secret | Credencial efêmera, sem segredo guardado |
| Dispositivos / IoT | **Certificados / mTLS** | Senha compartilhada | Provisionar e rotacionar certificados |

**Regra de ouro de tokens/SSO:** valide **SEMPRE** assinatura, `issuer`, `audience` e expiração;
rejeite `alg:none`; verifique `nonce`/`state` e `redirect_uri` em fluxos OAuth/OIDC.

---

## 2. Fatores de autenticação (MFA) — força × uso

| Fator | Força | Resistente a phishing? | Usar quando |
|-------|-------|------------------------|-------------|
| Senha (sozinha) | Fraca | Não | Nunca como único fator p/ dado sensível |
| OTP por **SMS/e-mail** | Baixa–média | **Não** (SIM swap, phishing) | Só como fallback, nunca primário |
| **TOTP** (app autenticador) | Média | Não (phishable) | 2º fator comum e aceitável |
| **Push** com *number matching* | Média–alta | Parcial | Boa UX; cuidado com "MFA fatigue" |
| **Passkeys / WebAuthn** | Alta | **Sim** | Preferível para todos os usuários |
| **Chave de hardware** (FIDO2) | Alta | **Sim** | Admins, acessos críticos |

**NIST 800-63B (resumo):** comprimento > complexidade forçada; permitir frases longas; checar
contra listas de senhas vazadas; **rate limiting** em vez de expiração/rotação forçada.

---

## 3. Autenticação × Autorização — não confundir

| | Autenticação (AuthN) | Autorização (AuthZ) |
|---|----------------------|---------------------|
| Pergunta | "Quem é você?" | "O que você pode fazer?" |
| Prova | Credencial/fator (senha, passkey, token) | Papel/atributo/relação + política |
| Falha típica | Senha fraca, MFA ausente (A07) | IDOR, privilege escalation (A01) |
| Onde mora | Borda (filtro de autenticação) | **Camada de serviço/domínio** |

---

## 4. Controle de acesso — qual modelo

| Modelo | Quando usar | Exemplo | Cuidado |
|--------|-------------|---------|---------|
| **RBAC** (papéis) | Papéis estáveis, poucos níveis | `ADMIN`, `GERENTE`, `USER` | Explosão de papéis se virar "1 papel por permissão" |
| **ABAC** (atributos) | Decisão depende de contexto | "gerente **da mesma filial**, em horário comercial" | Complexidade; teste todos os caminhos |
| **ReBAC** (relações) | Grafos de "dono de / membro de" | Compartilhar documento com um usuário | Modelagem e infra (estilo Google Zanzibar) |
| **ACL** (lista por recurso) | Poucos recursos, permissão explícita | Arquivo com lista de quem acessa | Escala mal em volume |

Na prática, **RBAC + ABAC** combinados cobrem a maioria dos casos: papel define o "grande balde",
atributos refinam a decisão.

---

## 5. Onde e como verificar autorização (anti-IDOR)

> IDOR (*Insecure Direct Object Reference*) é a falha de acesso nº 1. Uma requisição `GET /pedidos/2`
> é **HTTP perfeitamente válido** — quem decide se você pode ver o pedido 2 é o **código**.

Checklist para toda operação sobre um recurso:

- [ ] Verificar **no servidor**, na **camada de serviço/domínio** (não só no controller).
- [ ] Checar a **posse/escopo** do objeto: o recurso pertence (ou foi compartilhado com) o usuário atual?
- [ ] **Default deny**: sem regra que permita → negar (403/404).
- [ ] **Nunca** confiar em campo vindo do cliente (papel no front, id oculto, preço no form).
- [ ] **Complete mediation**: cada acesso é verificado, inclusive leitura.
- [ ] Preferir **404** a 403 quando revelar a existência do recurso já é vazamento.
- [ ] **Teste obrigatório:** usuário A tenta acessar recurso de B → **deve** dar 403/404.

---

## 6. Sessão × Token — como manter o usuário autenticado

| Critério | **Cookie de sessão** (server-side) | **JWT / token** (stateless) |
|----------|-----------------------------------|-----------------------------|
| Melhor para | Web clássica same-origin | APIs, SPA, mobile, microserviços |
| Revogação | **Fácil** (invalidar no servidor) | **Difícil** → usar expiração curta + refresh |
| Armazenamento | Cookie `HttpOnly` `Secure` `SameSite` | Header `Authorization: Bearer` (não em `localStorage`) |
| Risco principal | CSRF → exige **token CSRF** | Roubo de token, não validar assinatura |
| Regenerar no login | **Sim** (anti session fixation) | N/A (novo token por login) |

**Recomendações:**
- **Web same-origin** → cookie de sessão (`HttpOnly`+`Secure`+`SameSite=Lax/Strict`) + **CSRF token**.
- **API / SPA / mobile** → token **curto** no header, com **refresh token rotation** e validação de `assinatura/iss/aud/exp`.
- Nunca guardar JWT em `localStorage` (exposto a XSS). Não colocar dado sensível no payload (é apenas Base64).

---

## 7. Senhas, chaves e segredos

| O quê | Recomendado | Nunca |
|-------|-------------|-------|
| **Hash de senha** | **BCrypt** (work ≥ 10–12), **Argon2id** ou PBKDF2 | MD5/SHA-1/SHA-256 "puro", cripto reversível |
| **Dado sensível a recuperar** (ex.: pagamento) | **AES-256-GCM** (AEAD), IV único por operação | Base64/hex/"ofuscação" como "proteção" |
| **Segredos de app** (tokens, chaves, senhas de DB) | **Secrets manager / Vault / KMS**; env como mínimo | Hardcode no código ou no repositório |
| **Chave de criptografia** | **KMS/HSM**; separar a chave do ciphertext; rotação | Guardar a chave ao lado do dado cifrado |
| **Aleatoriedade de segurança** | `SecureRandom` (CSPRNG) | `java.util.Random` para chaves/IV/tokens |

**Pergunta-chave da cripto:** *"preciso ler o dado de volta?"* → **Sim**: cifra (AES-GCM). **Não**: hash (BCrypt).
*"preciso só garantir integridade/autenticidade?"* → **HMAC**.

---

## 8. Apêndice — quando usar cada tipo de teste

| Teste | O que vê | Acha bem | Ponto cego | Momento |
|-------|----------|----------|------------|---------|
| **SAST** | O **código** (estático) | Injection, segredos hardcoded, padrões ruins | Falha de runtime/config; gera falso-positivo | IDE / PR / CI |
| **DAST** | A **app rodando** (dinâmico) | Headers ausentes, exposições, config | Não aponta a linha no código | Staging / CI |
| **SCA** | As **dependências** | CVEs em libs (ex.: Text4Shell) | Falha no **seu** código | Build / PR / CI |

Os três **se complementam** — e idealmente viram **gates** no CI (o crítico não passa do merge).

---

## Como usar este guia

- **Design review / PR:** use as matrizes como checklist — "qual método de auth? qual modelo de acesso?".
- **Onboarding:** leitura inicial para alinhar vocabulário e padrões.
- **Exceções:** se precisar fugir de uma recomendação, **registre o porquê** (decisão consciente, não omissão).
- **Evoluir:** este é um documento vivo — ajuste limiares e acrescente casos conforme a organização.

> Referências: OWASP ASVS, OWASP Top 10 (2021), OWASP Cheat Sheets, NIST SP 800-63B.
