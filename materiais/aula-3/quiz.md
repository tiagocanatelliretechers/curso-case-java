# Aula 3 — Quiz de fixação (10 questões)

**1.** Qual a diferença entre autenticação e autorização?
- a) São sinônimos
- b) Autenticação prova quem é; autorização define o que pode fazer
- c) Autorização vem antes da autenticação
- d) Autenticação só existe em APIs

**2.** Por que MD5 sem salt é inadequado para senhas?
- a) Não é suportado em Java
- b) É rápido demais e vulnerável a rainbow tables / brute force
- c) Gera hashes grandes demais
- d) Só funciona com números

**3.** O que o **salt** garante no hashing de senha?
- a) Torna o hash reversível
- b) Faz senhas iguais gerarem hashes diferentes, frustrando rainbow tables
- c) Substitui a necessidade de work factor
- d) Criptografa a senha

**4.** Segundo o NIST 800-63B, uma boa prática é:
- a) Forçar troca de senha a cada 30 dias
- b) Exigir muitos caracteres especiais sempre
- c) Priorizar comprimento e usar rate limiting em vez de rotação forçada
- d) Bloquear a conta permanentemente após 1 erro

**5.** IDOR é um exemplo de:
- a) Injection
- b) Broken Access Control (falta de object-level authorization)
- c) Cryptographic Failure
- d) SSRF

**6.** A forma correta de impedir IDOR em `/api/pedidos/{id}` é:
- a) Esconder o link no frontend
- b) Ofuscar o id na URL
- c) Verificar no servidor que o objeto pertence ao usuário autenticado
- d) Usar HTTPS

**7.** Ao migrar de MD5 para bcrypt sem forçar reset geral, uma estratégia é:
- a) Descriptografar os MD5 e recriptografar
- b) Reidratar (rehash) a senha em bcrypt no próximo login bem-sucedido
- c) Impossível migrar
- d) Guardar as duas versões para sempre

**8.** Em OIDC/OAuth2, um erro grave de implementação é:
- a) Usar HTTPS
- b) Não validar assinatura, issuer e audience do token
- c) Usar id_token
- d) Ter expiração curta

**9.** `@PreAuthorize("hasRole('ADMIN')")` no Spring Security:
- a) Só funciona no frontend
- b) Aplica controle de acesso a nível de método no servidor
- c) Criptografa a resposta
- d) Substitui o login

**10.** (Laboratório) Após o Lab 3.2, ao repetir o acesso ao pedido de outro cliente:
- a) Continua acessível
- b) Retorna 403 (ou 404), pois a posse é verificada no servidor
- c) Retorna todos os pedidos
- d) Derruba a aplicação

---

## Gabarito comentado
1. **b** — Authn = identidade; Authz = permissões.
2. **b** — Rápido + sem salt = quebra trivial.
3. **b** — Salt individualiza hashes, anula rainbow tables.
4. **c** — NIST: comprimento + rate limiting > complexidade/rotação forçada.
5. **b** — IDOR = autorização por objeto ausente.
6. **c** — Verificação de posse no servidor.
7. **b** — Rehash on login (não dá para "descriptografar" hash).
8. **b** — Validar assinatura/issuer/audience é obrigatório.
9. **b** — Method security no servidor.
10. **b** — Posse verificada → 403/404.
