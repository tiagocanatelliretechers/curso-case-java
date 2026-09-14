# Aula 2 — Guia de Laboratório (aluno)
## Threat Modeling + Correção de Input Validation

**Duração:** 70 min · **Ponto de partida:** `aula-2-baseline` (= baseline; ainda sem correções de código).
**Ambiente:** JDK 17 + IDE + Postman/navegador.

> Cada correção deve ser acompanhada de um teste que **prova** o comportamento seguro.

---

## Lab 2.1 — Completar a matriz STRIDE (15 min)

**Objetivo:** exercitar threat modeling adicionando ameaças ao modelo do Portal.

**Passo a passo:**
1. Abra `anexos/matriz-stride.md` (linhas 1–5 já preenchidas).
2. Adicione **≥ 3 ameaças novas** (linhas 6+), cada uma com categoria STRIDE, risco e mitigação.
3. Escolha ameaças que **não** repitam os exemplos.

**Resultado esperado:** ≥ 3 novas linhas coerentes (ameaça → STRIDE → risco → mitigação).

**Checkpoint (instrutor):**
- [ ] As categorias STRIDE estão corretas para cada ameaça?
- [ ] Cada ameaça tem uma mitigação acionável (não genérica)?

---

## Lab 2.2 — Corrigir SQL Injection na busca (25 min)

**Objetivo:** eliminar o SQLi em `ProdutoService.buscar` migrando para query parametrizada.

**Passo 0 — Reproduza o ataque (baseline):**
```bash
curl -s "http://localhost:8080/produtos/buscar?termo=zzz' UNION SELECT id,email,senha,0,0 FROM usuario --"
```
Confirme que e-mails e hashes MD5 aparecem na listagem.

**Passo a passo:**
1. Abra `service/ProdutoService.java`.
2. Substitua a concatenação por uma query **parametrizada** (JdbcTemplate com `?`, ou um método
   `@Query`/derivado no `ProdutoRepository`).
3. Garanta que o caractere curinga `%` faça parte do **parâmetro**, não da string SQL.
4. Reinicie e **repita o ataque** do Passo 0 — o `UNION` deve deixar de funcionar.
5. Escreva um teste (JUnit) que:
   - busca por um termo normal e retorna o produto esperado;
   - busca pelo payload `' OR '1'='1` e **não** retorna todos os produtos.

**Resultado esperado:** busca funciona; payloads de SQLi não injetam mais; teste passa.

**Checkpoint (instrutor):**
- [ ] A query usa binding de parâmetro (sem concatenar `termo`)?
- [ ] O payload `UNION` da Aula 1 falha (retorna vazio/nada de usuários)?
- [ ] Existe teste automatizado provando a correção?

---

## Lab 2.3 — Bean Validation + validador `@CNPJ` (20 min)

**Objetivo:** validar as entradas do cadastro na borda e criar um validador customizado.

**Passo a passo:**
1. Em `dto/CadastroClienteForm.java`, adicione anotações Jakarta Validation:
   `@NotBlank`, `@Size`, `@Email` nos campos apropriados; senha com tamanho mínimo.
2. Crie a anotação `@CNPJ` + `ConstraintValidator` que valida CNPJ (formato e dígitos verificadores).
   Aplique `@CNPJ` no campo `cnpj`.
3. No `HomeController.registrar`, receba o form com `@Valid` e trate erros (reexibir o formulário
   com mensagens; **não** vazar detalhes internos).
4. Teste manualmente: CNPJ inválido, e-mail malformado, senha curta → cadastro rejeitado.

**Resultado esperado:** entradas inválidas são rejeitadas na borda com mensagens claras e genéricas.

**Checkpoint (instrutor):**
- [ ] `@Valid` está ativo no controller e os erros são tratados?
- [ ] O validador `@CNPJ` rejeita um CNPJ com dígito verificador errado?
- [ ] Mensagens de erro não expõem stack trace/detalhes internos?

---

## Lab 2.4 — Upload seguro de comprovante (10 min)

**Objetivo:** transformar o upload irrestrito em um upload seguro.

**Passo a passo:**
1. Em `service/UploadService.salvar`, implemente:
   - **Allowlist** de tipos por **magic number** (ex.: PDF `%PDF`, PNG `\x89PNG`, JPG `\xFF\xD8`).
   - **Limite de tamanho** (ex.: 5 MB) — rejeite acima disso.
   - **Nome gerado pelo servidor** (UUID + extensão segura); nunca use o nome do cliente no caminho.
2. Reinicie e teste: enviar `.png` válido (aceita); enviar `.jsp`/`.sh` (rejeita);
   enviar nome com `../` (salvo com nome seguro, sem traversal).

**Resultado esperado:** apenas tipos permitidos e dentro do limite são aceitos; nome é seguro.

**Checkpoint (instrutor):**
- [ ] Um arquivo com extensão trocada (conteúdo executável, nome `.png`) é rejeitado pelo magic number?
- [ ] Nome com `../` não escreve fora do diretório previsto?
- [ ] Há limite de tamanho aplicado?

---

## Entrega
- Código corrigido (2.2, 2.3, 2.4) + testes do 2.2.
- Matriz STRIDE com ≥ 3 ameaças novas.
