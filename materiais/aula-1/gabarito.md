# Aula 1 — Gabarito / Solução comentada (SOMENTE INSTRUTOR)

> Não distribuir aos alunos.

## Lab 1.1 — Subir e navegar

Sucesso = app na porta 8080; login funciona; pedido criável. Ponto de discussão já aqui:
`/admin` abre para qualquer usuário logado (broken access control) — deixe o aluno notar,
mas não corrija (é o Lab 3.5).

## Lab 1.2 — Reconhecimento: achados esperados

**Endpoints (amostra):**
`/`, `/login`, `/registrar`, `/esqueci-senha`, `/produtos`, `/produtos/buscar`,
`/pedidos`, `/pedidos/{id}`, `/pedidos/{id}/comprovante`, `/importar-catalogo`,
`/admin`, `/api/auth/login`, `/api/pedidos`, `/api/pedidos/{id}`, `/actuator/**`, `/h2-console`.

**Tecnologias inferidas:**
- Spring Boot / Tomcat embutido (headers, comportamento de erro).
- H2 / PostgreSQL (via `/actuator/env`, mensagens de erro SQL, `/h2-console`).
- Java 17.

**Headers de segurança:** ausentes por padrão no baseline — `X-Frame-Options` desabilitado,
sem `Content-Security-Policy`, sem `Strict-Transport-Security`, cookie de sessão **sem** HttpOnly/Secure/SameSite adequados.

**Exposições de configuração (A05):**
- `/actuator` totalmente aberto — em especial `/actuator/env` (revela `portal.jwt.secret`,
  credenciais do datasource), `/actuator/mappings`, `/actuator/beans`.
- `/h2-console` acessível.
- Stack trace completo retornado ao cliente (ex.: `/pedidos/abc` → erro de conversão de tipo com trace).

**Autenticação/sessão:**
- Web: sessão via cookie `JSESSIONID` (sem flags seguras).
- API: JWT Bearer (`/api/auth/login` devolve `token`).

**Hipóteses de vulnerabilidade que costumam surgir (todas corretas):**
- Busca de produto → Injection (A03).
- `/pedidos/{id}` e `/api/pedidos/{id}` → Broken Access Control / IDOR (A01).
- `/admin` sem checagem de papel → Broken Access Control (A01).
- Segredo JWT no `/actuator/env` → Cryptographic Failures (A02).
- `/importar-catalogo` → SSRF (A10).

## Lab 1.3 — Abuse cases e requisitos: exemplos de referência

### Funcionalidade: Cadastro de cliente (`/registrar`)

**Abuse cases:**
1. As an attacker, I want to submit thousands of registrations so that I exhaust the database / spam accounts. *(sem rate limiting / anti-automação)*
2. As an attacker, I want to enumerate which e-mails already exist so that I build a target list.
3. As an attacker, I want to inject script/oversized values into fields so that I break validation or store XSS.

**Requisitos de segurança (verificáveis):**

| # | Requisito | Critério de aceite | OWASP / ASVS |
|---|-----------|--------------------|--------------|
| 1 | Todas as entradas devem ser validadas por allowlist (formato de e-mail, CNPJ válido, tamanho máximo por campo) | Dado um CNPJ inválido/campo > limite, quando submeter, então o cadastro é rejeitado com erro genérico | A03/A04 · ASVS V5 |
| 2 | A senha deve ser armazenada com hashing forte (bcrypt/Argon2), nunca reversível | Dado um usuário cadastrado, quando inspecionar o banco, então o valor é um hash bcrypt (`$2...`), não a senha/MD5 | A02 · ASVS V2.4 |
| 3 | O cadastro não deve revelar se um e-mail já existe | Dado um e-mail já cadastrado, quando submeter, então a resposta é indistinguível de e-mail novo | A07 · ASVS V2.2 |
| 4 | Deve haver política mínima de senha e proteção anti-automação | Dado ≥ N tentativas, quando exceder, então há throttling/CAPTCHA | A07 · ASVS V2.1 |

### Funcionalidade: Upload de comprovante (`/pedidos/{id}/comprovante`)

**Abuse cases:**
1. As an attacker, I want to upload a `.jsp`/`.sh` so that I achieve remote code execution.
2. As an attacker, I want to use a filename like `../../etc/x` so that I overwrite files (path traversal).
3. As an attacker, I want to upload a 2 GB file so that I exhaust disk/memory (DoS).
4. As an attacker, I want to upload to another client's order id so that I tamper with their records (IDOR).

**Requisitos de segurança (verificáveis):**

| # | Requisito | Critério de aceite | OWASP / ASVS |
|---|-----------|--------------------|--------------|
| 1 | Só aceitar tipos permitidos, validando o *magic number* (não só a extensão) | Dado um `.png` renomeado para conter conteúdo executável, quando enviar, então é rejeitado | A04 · ASVS V12.1 |
| 2 | O nome do arquivo salvo deve ser gerado pelo servidor; entrada do usuário nunca compõe o caminho | Dado nome com `../`, quando enviar, então o arquivo é salvo com nome seguro no diretório previsto | A03 · ASVS V12.3 |
| 3 | Impor limite de tamanho e de taxa | Dado arquivo > limite, quando enviar, então é rejeitado (413) | A04 · ASVS V12 |
| 4 | Verificar que o pedido pertence ao usuário autenticado antes de aceitar o upload | Dado o id de pedido de outro cliente, quando enviar, então retorna 403 | A01 · ASVS V4 |
| 5 | Arquivos ficam fora do webroot e não são executáveis | Dado um upload, quando acessar a URL do storage, então o arquivo não é servido/executado | A05 · ASVS V12.5 |

## Quiz — ver `quiz.md` (gabarito ao final do próprio arquivo).
