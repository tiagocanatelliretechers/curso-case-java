# Capture the Flag — Portal de Pedidos B2B (revisão integrada)

**Tempo:** ~25–30 min · **Modalidade:** individual ou em duplas.
**Alvo:** a versão **final** da aplicação (`aula-6-hardened`, salvo indicação da flag).

Cada flag remete a um módulo diferente do curso. Registre a **evidência** e o **módulo/OWASP** associado.
Algumas flags pedem para **explorar**; outras para **explicar/corrigir**.

---

**Flag 1 — Controle de acesso (Aula 3 / A01).**
Na versão final, tente reproduzir o IDOR: logado como `joao@acme.com`, acesse `/pedidos/2` e `/api/pedidos/2`.
> Objetivo: demonstrar que **não** é mais possível (403/404) e apontar onde a posse é verificada no código.

**Flag 2 — Segredo mal protegido (Aula 4 / A02).**
Localize onde um segredo poderia vazar (`/actuator/env`, `application.yml`, imagem Docker).
> Objetivo: mostrar que o segredo do JWT/cripto **não** está mais no código/endpoint e explicar de onde ele vem agora.

**Flag 3 — Requisito de segurança (Aula 1).**
Pegue **um** requisito de segurança que você escreveu no Lab 1.3 (cadastro ou upload).
> Objetivo: dizer se ele foi **atendido** na versão final e apontar o código/config que o satisfaz (ou não).

**Flag 4 — Injeção/validação (Aula 2 / A03).**
Rode o payload de SQLi da Aula 1 na busca de produtos.
> Objetivo: comprovar que o `UNION`/tautologia **não** injeta mais e indicar a linha parametrizada.

**Flag 5 — Componente vulnerável (Aula 6 / A06).**
Rode `./mvnw -Psecurity verify` (ou consulte o relatório).
> Objetivo: confirmar que a dependência **Text4Shell** foi atualizada e o CVE não aparece.

**Flag 6 — Erro/Logging (Aula 5 / A09).**
Provoque um erro (`/pedidos/abc`) e tente uma log injection no import (`url` com `\n`).
> Objetivo: mostrar que o cliente recebe erro **genérico** e que o log **não** aceita linhas forjadas.

---

## Entrega
Para cada flag: evidência (comando/print), módulo + OWASP, e 1 linha de conclusão
("atendido/não atendido, porque…").
