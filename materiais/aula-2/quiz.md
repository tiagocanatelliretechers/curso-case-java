# Aula 2 — Quiz de fixação (10 questões)

**1.** O princípio "fail securely" significa que, diante de um erro inesperado, o sistema deve:
- a) Registrar e continuar liberando o acesso
- b) Negar por padrão (não conceder acesso)
- c) Reiniciar o servidor
- d) Mostrar o stack trace ao usuário

**2.** Um WAF na frente da aplicação:
- a) Substitui a necessidade de código seguro
- b) É um complemento (defense in depth), não substituto da correção
- c) Elimina qualquer SQL Injection
- d) Só serve para DDoS

**3.** No STRIDE, um **IDOR** que expõe pedidos de outro cliente é primariamente:
- a) Spoofing
- b) Repudiation
- c) Elevation of Privilege / Information Disclosure
- d) Denial of Service

**4.** A defesa **primária** contra SQL Injection é:
- a) Escapar aspas manualmente
- b) Usar um WAF
- c) Queries parametrizadas (binding de parâmetros)
- d) Renomear as colunas

**5.** Por que **allowlist** é preferível a **denylist** na validação de entrada?
- a) É mais rápida de escrever
- b) Aceita apenas o conhecido-bom; denylist sempre fica atrás de novas variações de ataque
- c) Denylist não funciona em Java
- d) Allowlist dispensa validação no servidor

**6.** Validação **client-side** (JavaScript):
- a) É suficiente se bem feita
- b) Serve para UX, mas nunca substitui a validação server-side
- c) Impede SQL Injection
- d) É obrigatória por lei

**7.** Ao validar upload de arquivos, confiar **apenas** na extensão do nome é inseguro porque:
- a) A extensão pode ser trocada; é preciso validar o conteúdo (magic number)
- b) Extensões não existem em Linux
- c) O Spring não lê extensões
- d) Isso deixa o upload lento

**8.** Qual anotação Jakarta Validation garante que um campo de texto não seja nulo **nem** vazio/espaços?
- a) `@NotNull`
- b) `@NotEmpty`
- c) `@NotBlank`
- d) `@Size(min=1)`

**9.** Um `ConstraintValidator` customizado (`@CNPJ`) é usado para:
- a) Validação sintática apenas
- b) Regras de negócio/semânticas reutilizáveis como anotação
- c) Substituir o banco de dados
- d) Criptografar o campo

**10.** (Laboratório) Após a correção do Lab 2.2, ao repetir o payload `' UNION SELECT ... FROM usuario --`:
- a) Continua vazando os usuários
- b) O termo é tratado como dado literal e nenhum usuário é retornado
- c) A aplicação cai com erro 500 sempre
- d) O banco é apagado

---

## Gabarito comentado
1. **b** — Estado padrão em erro = negar.
2. **b** — WAF é camada extra; não corrige a causa.
3. **c** — IDOR = autorização quebrada (Elevation) com exposição de dados (Info Disclosure).
4. **c** — Parametrização separa dado de comando.
5. **b** — Allowlist aceita só o bom conhecido.
6. **b** — Client-side é UX; server-side é o controle.
7. **a** — Validar o conteúdo real (magic number).
8. **c** — `@NotBlank` cobre nulo, vazio e só-espaços.
9. **b** — Encapsula regra de negócio verificável como constraint.
10. **b** — Parametrizado, o input não vira SQL.
