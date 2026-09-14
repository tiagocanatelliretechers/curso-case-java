# Aula 2 — Slides (outline)
## Secure Application Design & Architecture + Secure Coding para Input Validation

> 240 min (2 intervalos de 10 min). Termos técnicos em inglês mantidos.
> Abertura: "Na Aula 1 vocês reconheceram a aplicação e escreveram requisitos. Hoje
> entendemos por que essas falhas nascem (design) e como a validação de entrada as elimina."

---

### BLOCO 1 (60 min) — Secure Design & Architecture

**Slide 1 — Abertura e ponte com a Aula 1**
- Retomar 2–3 requisitos de segurança que a turma derivou.
- Hoje: princípios de design seguro, threat modeling (STRIDE) e input validation.
- **Nota:** Conecte cada princípio a uma falha já observada no Portal.

**Slide 2 — Objetivos**
- Aplicar princípios de Secure by Design.
- Fazer threat modeling com STRIDE sobre a arquitetura do Portal.
- Dominar validação de entrada (Bean Validation, allowlist, upload seguro).
- Corrigir SQL Injection e falhas de validação no laboratório.

**Slide 3 — Princípios de Secure by Design (1/2)**
- **Least privilege:** cada componente/usuário com o mínimo necessário.
- **Defense in depth:** múltiplas camadas; nenhuma é o único ponto de falha.
- **Fail securely:** em erro, negar por padrão (não abrir acesso).
- **Separation of duties:** separar papéis críticos.
- **Nota:** Exemplo fail-secure: exceção no filtro de autorização deve resultar em 403, não em "passa".

**Slide 4 — Princípios de Secure by Design (2/2)**
- **Economy of mechanism (KISS):** simplicidade reduz superfície de erro.
- **Complete mediation:** toda requisição a um recurso é verificada (não cachear decisão de acesso indevidamente).
- **Zero Trust:** nunca confiar por localização de rede; verificar sempre identidade e autorização.
- **Nota:** Zero Trust é a evolução moderna desses princípios; ligue com autorização por objeto (Aula 3).

**Slide 5 — Arquitetura em camadas e responsabilidade de segurança**
- Controller (borda): autenticação, validação sintática, mapeamento de erro.
- Service (domínio): autorização de negócio, validação semântica, invariantes.
- Repository: acesso a dados parametrizado.
- **Nota:** No Portal, o IDOR existe porque a autorização de posse não está em nenhuma camada. Design decide onde ela mora.

**Slide 6 — Onde validar: borda vs. domínio**
- Client-side: só UX; nunca confiar.
- Server-side na borda: formato/sintaxe (Bean Validation nos DTOs).
- Domínio: regras de negócio e invariantes (ex.: "preço vem do servidor").
- **Nota:** Reforce: validação client-side é conveniência, não controle.

**Slide 7 — Gateway/WAF como camada adicional**
- API Gateway/WAF ajudam (rate limiting, filtragem, TLS termination).
- São **complemento**, não substituto do código seguro.
- Atacante adapta payload para burlar assinaturas do WAF.
- **Nota:** "WAF compra tempo; não corrige a vulnerabilidade."

**Slide 8 — Threat Modeling: por que e quando**
- Antecipar ameaças no design, antes de codar.
- Perguntas: o que estamos construindo? o que pode dar errado? o que faremos? fizemos bem?
- STRIDE é uma taxonomia para "o que pode dar errado".
- **Nota:** Threat modeling é atividade de time; hoje faremos um exemplo guiado.

**Slide 9 — Data Flow Diagram (DFD) do Portal (nível 1)**
- Entidades externas: Cliente (browser), Admin, Fornecedor (URL de catálogo).
- Processos: App web (Controllers/Services).
- Data stores: Banco (usuário/cliente/pedido/produto), Storage de anexos.
- Fluxos: login, busca, checkout, upload, importar catálogo, painel admin.
- Trust boundaries: internet↔app, app↔banco, app↔storage, app↔fornecedor.
- **Nota:** Peça aos alunos que desenhem junto; este DFD é a base do Lab 2.1.

**Slide 10 — STRIDE (definição)**
- **S**poofing (autenticidade) · **T**ampering (integridade) · **R**epudiation (auditoria)
- **I**nformation Disclosure (confidencialidade) · **D**enial of Service (disponibilidade) · **E**levation of Privilege (autorização)
- **Nota:** Cada letra mapeia para uma propriedade de segurança violada.

**Slide 11 — STRIDE aplicado ao Portal (matriz — exemplos)**
- Ver `anexos/matriz-stride.md` (preenchida com 8+ ameaças).
- Exemplos:
  - Login (Spoofing): brute force sem rate limiting → mitigar com throttling/MFA.
  - Busca (Tampering/Info Disclosure): SQLi → query parametrizada.
  - `/pedidos/{id}` (Elevation/Info Disclosure): IDOR → checar posse.
  - Import catálogo (Info Disclosure): SSRF → allowlist de destino.
  - Upload (Tampering/DoS): arquivo malicioso/gigante → validar tipo/tamanho.
- **Nota:** Mostre 3–4 linhas ao vivo; o resto os alunos completam no Lab 2.1.

**Slide 12 — Do modelo à mitigação**
- Cada ameaça → um requisito/controle → um teste.
- Priorize por risco (prob × impacto).
- **Nota:** Ligue com priorização vista na Aula 1.

---

### BLOCO 2 (40 min) — Threat Modeling prático (STRIDE)

**Slide 13 — Exercício guiado (com a turma)**
- Preencher juntos 4 linhas da matriz STRIDE para: login, busca, upload, import.
- **Nota:** Este bloco é hands-on de modelagem; a matriz continua no Lab 2.1.

**Slide 14 — Erros comuns em threat modeling**
- Focar só em "hackers externos" e esquecer insiders/erros.
- Parar no diagrama e não derivar mitigações/testes.
- Não revisar quando a arquitetura muda.
- **Nota:** Transição para input validation após o intervalo.

---

### INTERVALO (10 min)

---

### BLOCO 3 (40 min) — Input Validation

**Slide 15 — Fundamentos de validação**
- **Allowlist** (aceitar o conhecido-bom) > denylist (bloquear o conhecido-ruim).
- Camadas de validação: **sintática** (formato) → **semântica** (faz sentido) → **de negócio** (regra do domínio).
- **Nota:** Denylist sempre fica para trás do atacante; allowlist é o default seguro.

**Slide 16 — Onde validar (recapitulando com código)**
- Client-side = UX. Server-side = obrigatório.
- Na borda: Bean Validation nos DTOs. No domínio: invariantes no Service.
- **Nota:** Mostre um DTO com e sem anotações.

**Slide 17 — Bean Validation (Jakarta) no Spring**
- Anotações: `@NotNull`, `@NotBlank`, `@Size`, `@Pattern`, `@Email`, `@Positive`.
- Ativar com `@Valid` no controller; erros via `BindingResult`/`@ControllerAdvice`.
  ```java
  public class CadastroClienteForm {
    @NotBlank @Size(max=150) private String razaoSocial;
    @Email @NotBlank private String email;
    @Size(min=8, max=100) private String senha;
    @CNPJ private String cnpj; // validador customizado
  }
  ```
- **Nota:** No baseline os DTOs não têm nenhuma anotação — Lab 2.3 adiciona.

**Slide 18 — Validador customizado (`@Constraint`)**
- Regra de negócio própria (ex.: CNPJ válido) como anotação reutilizável.
- Componentes: anotação `@CNPJ` + `ConstraintValidator<CNPJ,String>`.
- **Nota:** Mostre a estrutura; o código completo está no gabarito do Lab 2.3.

**Slide 19 — Canonicalização e normalização**
- Normalizar antes de validar (encoding, Unicode, path).
- Path traversal: `../`, `%2e%2e`, variações de encoding.
- **Nota:** "Valide o dado na forma canônica; senão o atacante escolhe a forma."

**Slide 20 — Validação de upload de arquivo**
- Validar: **magic number** (conteúdo real) + extensão (allowlist) + content-type + tamanho.
- Nome de arquivo gerado pelo servidor (UUID); armazenar fora do webroot; sem execução.
- Antivírus/scanning quando aplicável.
- **Nota:** No Portal o upload aceita qualquer coisa — Lab 2.4 corrige.

**Slide 21 — Injection na origem**
- SQL: **queries parametrizadas** (PreparedStatement / JPA binding), nunca concatenar.
- OS Command: evitar `Runtime.exec` com input; usar APIs seguras / allowlist de comandos.
- LDAP/XPath: escaping/binding específico.
- **Nota:** Validação reduz risco, mas a defesa primária contra SQLi é parametrização.

**Slide 22 — SQL Injection: antes e depois (Java)**
- Antes (vulnerável):
  ```java
  "SELECT ... WHERE nome LIKE '%" + termo + "%'"
  ```
- Depois (seguro):
  ```java
  jdbc.query("SELECT ... WHERE nome LIKE ?", ps -> ps.setString(1, "%"+termo+"%"), mapper);
  // ou, com JPA:
  @Query("... WHERE p.nome LIKE %:termo%") List<Produto> buscar(@Param("termo") String t);
  ```
- **Nota:** O parâmetro é enviado separado da query; o dado nunca vira código SQL.

---

### BLOCO 4 (70 min) — Laboratório

**Slide 23 — Laboratório da Aula 2**
- Lab 2.1: completar a matriz STRIDE (3 ameaças novas).
- Lab 2.2: corrigir SQL Injection na busca de produtos.
- Lab 2.3: Bean Validation nos DTOs + validador `@CNPJ`.
- Lab 2.4: upload seguro (magic number, allowlist, tamanho, rename).
- **Nota:** Distribua `lab.md`. Comece pelo 2.2 se o tempo apertar (maior valor).

---

### INTERVALO (10 min)

---

### BLOCO 5 (20 min) — Debrief + Quiz

**Slide 24 — Debrief**
- Revisar a correção do SQLi: por que o payload `UNION` deixa de funcionar.
- Mostrar o teste que prova a validação de upload.
- **Nota:** Rode o payload de SQLi da Aula 1 contra a versão corrigida e mostre que falha.

**Slide 25 — Quiz + ponte para a Aula 3**
- Quiz (`quiz.md`).
- Aula 3: autenticação e autorização — vamos explorar o IDOR e corrigir de verdade.
- **Nota:** Aula 3 parte de `aula-3-baseline` (com o SQLi/validação já corrigidos).
