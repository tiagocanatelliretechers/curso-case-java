# Checklist de Code Review de Segurança

Use ao revisar um Pull Request. Revise o **diff** e questione tudo que cruza uma trust boundary.

## Autenticação
- [ ] Senhas via hashing forte (bcrypt/Argon2), nunca MD5/SHA puro ou texto claro?
- [ ] Segredos fora do código (env/secrets manager)?
- [ ] Fluxos de senha/recuperação sem user enumeration?

## Autorização
- [ ] Toda rota/método sensível verifica permissão no servidor?
- [ ] **Object-level authorization** (posse do recurso) para evitar IDOR?
- [ ] `hasRole`/`@PreAuthorize` correto para áreas administrativas?

## Validação de entrada
- [ ] Bean Validation nos DTOs (allowlist, tamanho, formato)?
- [ ] Queries parametrizadas (sem concatenar SQL)?
- [ ] Upload valida magic number, tamanho e nome gerado pelo servidor?

## Dados sensíveis / cripto
- [ ] Dado que precisa ser recuperado é cifrado (AES-GCM), não codificado (Base64)?
- [ ] `SecureRandom` para IV/tokens; nunca `java.util.Random`?
- [ ] TLS exigido; sem TrustManager permissivo?

## Sessão / tokens
- [ ] Cookie com HttpOnly/Secure/SameSite?
- [ ] JWT com verificação de assinatura, algoritmo fixado, expiração curta?
- [ ] CSRF habilitado para fluxos com cookie?

## Tratamento de erro / logging
- [ ] Sem stack trace ao cliente; erro genérico + detalhe no log do servidor?
- [ ] Eventos de segurança logados; sem senha/token/cartão em log; sem log injection?

## Dependências / deploy
- [ ] Dependências novas verificadas (sem CVE conhecida)?
- [ ] Config de prod endurecida (Actuator/console restritos)?
- [ ] Sem segredo em imagem/Dockerfile; container não-root?

## Testes
- [ ] Há teste automatizado provando o comportamento seguro (ex.: 403 no IDOR)?
