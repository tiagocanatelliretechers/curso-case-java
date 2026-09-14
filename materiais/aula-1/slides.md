# Aula 1 — Slides (outline)
## AppSec, Ameaças e Ataques + Levantamento de Requisitos de Segurança

> Formato: 1 slide = título + bullets + **Nota do apresentador**.
> Duração total: 240 min (2 intervalos de 10 min). Blocos ao final de cada seção.
> Termos técnicos em inglês são mantidos (ex.: *Broken Access Control*).

---

### BLOCO 1 (50 min) — Panorama de Application Security

**Slide 1 — Abertura**
- Certified Application Security Engineer (CASE) — Java
- 24h, 6 aulas de 4h. Hoje: por que AppSec, como pensam os atacantes, como levantar requisitos de segurança.
- **Nota:** Apresente-se, apresente o formato (teoria + laboratório na mesma aula), e a aplicação "Portal de Pedidos B2B" que vamos proteger nas 6 aulas. Peça que todos confirmem ambiente (JDK 17, Docker, IDE) já no início.

**Slide 2 — Objetivos da aula**
- Entender o que é AppSec e por que difere de segurança de rede/infra.
- Conhecer o OWASP Top 10 (2021) e a anatomia de um ataque.
- Introduzir Secure SDLC.
- Praticar levantamento de requisitos de segurança (abuse cases, security user stories, ASVS).
- **Nota:** Deixe claro o "gancho": tudo que virmos hoje será aprofundado nas próximas aulas.

**Slide 3 — O que é Application Security**
- Proteger a aplicação (código, lógica, dados) contra uso indevido.
- Foco em *como o software se comporta*, não só em *onde ele roda*.
- Segurança como propriedade emergente do ciclo de desenvolvimento, não um "produto" acoplado no fim.
- **Nota:** Enfatize: a maioria das brechas de alto impacto hoje é na camada de aplicação, não na rede.

**Slide 4 — AppSec ≠ Network/Infra Security**
- Firewall/IDS/segmentação protegem o perímetro e o transporte.
- Não enxergam lógica de negócio: um IDOR é uma requisição HTTP perfeitamente "válida".
- WAF ajuda, mas é mitigação, não correção — atacante adapta o payload.
- Responsabilidade: infra é do time de operações; AppSec é (também) do desenvolvedor.
- **Nota:** Use o exemplo do Portal: o firewall deixa passar `GET /pedidos/2` — quem decide se João pode ver o pedido 2 é o código.

**Slide 5 — Quem ataca e por quê**
- Atores: cibercriminosos (financeiro), insiders, hacktivistas, estados-nação, script kiddies, pesquisadores.
- Motivações: dinheiro (fraude, ransomware, venda de dados), vantagem competitiva, ideologia, notoriedade.
- Superfície cresce: APIs, mobile, cloud, dependências de terceiros.
- **Nota:** Não invente estatísticas específicas. Referencie fontes reconhecidas (OWASP, Verizon DBIR, relatórios de fornecedores) como leitura, sem citar números não verificáveis.

**Slide 6 — Custo de uma brecha**
- Custos diretos: resposta a incidente, forense, correção, notificação.
- Indiretos: multas regulatórias (ex.: LGPD/GDPR), perda de clientes, reputação, litígio.
- Quanto mais tarde a falha é encontrada, mais cara é a correção (shift-left).
- **Nota:** Introduza informalmente a curva de custo por fase do SDLC — retomada no Slide 12.

**Slide 7 — Tríade CIA e além**
- Confidentiality, Integrity, Availability.
- Complementos: Authentication, Authorization, Accountability (auditoria/logging), Non-repudiation.
- Cada vulnerabilidade viola uma ou mais dessas propriedades.
- **Nota:** Peça exemplos rápidos: SQLi (C+I), DoS (A), IDOR (C), log ausente (Accountability).

---

**Slide 8 — OWASP Top 10 (2021): visão geral**
- Lista das 10 categorias de risco mais críticas em aplicações web.
- É conscientização/priorização, não checklist completo (para isso, ASVS).
- A seguir: 1 slide por categoria com definição + exemplo Java + impacto.
- **Nota:** Mostre que cada categoria já existe, viva, no Portal (ver `VULNERABILITIES.md`).

**Slide 9 — A01: Broken Access Control**
- Def.: usuário faz algo além do que sua permissão deveria permitir (IDOR, escalada).
- Java (vulnerável):
  ```java
  @GetMapping("/pedidos/{id}")
  public Pedido ver(@PathVariable Long id){ return repo.findById(id).get(); } // sem checar dono
  ```
- Impacto: vazamento/alteração de dados de outros usuários.
- **Nota:** #1 do Top 10 2021. É exatamente o IDOR do Portal — será explorado na Aula 3.

**Slide 10 — A02: Cryptographic Failures**
- Def.: dado sensível exposto por cripto ausente/fraca/mal usada.
- Java (vulnerável): `Base64.getEncoder().encodeToString(cartao)` — codificação, não cripto.
- Impacto: exposição de senhas, cartões, PII.
- **Nota:** No Portal: senha em MD5, "dados de pagamento" em Base64, segredo JWT fraco. Aulas 3 e 4.

**Slide 11 — A03: Injection**
- Def.: dado não confiável interpretado como comando (SQL, OS, LDAP, XPath).
- Java (vulnerável):
  ```java
  "SELECT * FROM produto WHERE nome LIKE '%" + termo + "%'"
  ```
- Impacto: leitura/alteração de dados, RCE.
- **Nota:** Inclui XSS na taxonomia 2021. No Portal: SQLi na busca. Aula 2.

**Slide 12 — A04: Insecure Design**
- Def.: falha de concepção — ausência de controle desde o design (não é bug de implementação).
- Ex.: fluxo sem rate limiting, sem limites de negócio, sem threat modeling.
- Impacto: classes inteiras de ataque viáveis por ausência de defesa.
- **Nota:** Diferencie de misconfiguration. "Não dá para 'corrigir bug' o que nunca foi projetado com segurança."

**Slide 13 — A05: Security Misconfiguration**
- Def.: config insegura (defaults, portas/endpoints abertos, verbose errors).
- Java/Spring: Actuator exposto, H2 console aberto, stack trace ao cliente.
- Impacto: reconhecimento facilitado, exposição de segredos.
- **Nota:** No Portal, `/actuator/env` está aberto. Aulas 5 e 6.

**Slide 14 — A06: Vulnerable and Outdated Components**
- Def.: uso de bibliotecas/versões com vulnerabilidades conhecidas.
- Java: dependência desatualizada com CVE (ex.: `commons-text` 1.9 — Text4Shell).
- Impacto: herda a vulnerabilidade da dependência.
- **Nota:** No Portal há uma dependência com CVE didática. Dependency-Check na Aula 6.

**Slide 15 — A07: Identification & Authentication Failures**
- Def.: falhas em provar/identificar identidade (brute force, senhas fracas, user enumeration, MFA ausente).
- Java: login sem rate limiting, JWT sem verificação de assinatura.
- Impacto: account takeover.
- **Nota:** No Portal: MD5, user enumeration, JWT quebrado. Aulas 3 e 4.

**Slide 16 — A08: Software & Data Integrity Failures**
- Def.: confiar em código/dados sem verificar integridade (update sem assinatura, deserialização insegura, CI/CD comprometido).
- Java: pipeline sem verificação de dependências/artefatos.
- Impacto: supply chain, execução de código malicioso.
- **Nota:** Ligue com A06. SBOM e assinatura na Aula 6.

**Slide 17 — A09: Security Logging & Monitoring Failures**
- Def.: eventos de segurança não registrados/monitorados; ou logs inseguros (log injection).
- Java: não logar tentativas de login; logar input cru com `\n`.
- Impacto: incidentes passam despercebidos; forense inviável.
- **Nota:** No Portal: log injection no import. Aula 5.

**Slide 18 — A10: SSRF**
- Def.: servidor é induzido a fazer requisições a destinos não pretendidos.
- Java: `new URL(urlDoUsuario).openConnection()` sem allowlist.
- Impacto: acesso a serviços internos/metadados de cloud.
- **Nota:** No Portal: importar catálogo por URL. Design seguro na Aula 2.

**Slide 19 — Como as categorias se conectam**
- Raramente uma falha isolada: atacante encadeia várias.
- Ex.: misconfig (recon) → injection (foothold) → broken access control (pivot) → crypto failure (loot).
- **Nota:** Transição para a anatomia de um ataque (Bloco 2).

---

### BLOCO 2 (50 min) — Anatomia de um ataque + estudo de caso

**Slide 20 — Cyber Kill Chain (simplificado)**
- Reconnaissance → Weaponization → Delivery/Exploitation → Post-Exploitation (persistência, exfiltração).
- Defesa em cada etapa ("defense in depth").
- **Nota:** Vamos mapear um ataque real ao Portal nesta estrutura.

**Slide 21 — Reconnaissance**
- Objetivo: mapear alvo sem "tocar" muito.
- Técnicas: enumerar endpoints, ler headers/erros, comentários em HTML/JS, versões expostas, `/actuator`.
- **Nota:** É exatamente o que os alunos farão no Lab 1.2. Antecipe a "Ficha de Reconhecimento".

**Slide 22 — Weaponization & Exploitation**
- Escolher a falha e construir o payload.
- Ex.: montar o `UNION SELECT` para a busca de produtos; forjar JWT.
- **Nota:** Não detalhe o payload aqui — será visto nas aulas específicas; foco na sequência lógica.

**Slide 23 — Post-Exploitation**
- Escalada (horizontal/vertical), movimento lateral, persistência, exfiltração.
- Encobrir rastros (ausência de logging ajuda o atacante).
- **Nota:** Ligue com A09: sem logs, o atacante age impune.

**Slide 24 — Estudo de caso "Empresa X" (1/3): contexto**
- Portal B2B semelhante ao nosso, exposto por engano em staging na internet.
- Sem WAF, Actuator aberto, busca vulnerável a SQLi.
- **Nota:** Caso genérico e fictício. Não atribua a empresas reais.

**Slide 25 — Estudo de caso (2/3): a cadeia**
- 1) Recon: atacante acha `/actuator/env` → descobre nome do banco e libs.
- 2) Injection: SQLi na busca extrai e-mails e hashes MD5.
- 3) Crypto failure: MD5 sem salt → quebra offline em minutos.
- 4) Broken access control: login + IDOR/JWT forjado → acesso a pedidos e dados de pagamento.
- **Nota:** Mostre como 4 categorias do Top 10 se somam. Cada elo sozinho já seria grave.

**Slide 26 — Estudo de caso (3/3): impacto e lições**
- Impacto: vazamento de PII e dados de pagamento; multa e dano reputacional.
- Lições: cada camada quebrada foi uma oportunidade perdida de defesa em profundidade.
- **Nota:** Transição: "e se tivéssemos pensado em segurança desde o requisito?" → Bloco 3.

**Slide 27 — Secure SDLC: visão geral**
- Segurança em TODAS as fases: Requisitos → Design → Implementação → Teste → Deploy → Manutenção.
- Cada fase tem atividades e artefatos de segurança próprios.
- **Nota:** Este é o mapa do curso inteiro. Mostre qual aula cobre qual fase.

**Slide 28 — Onde a segurança entra em cada fase**
- Requisitos: security requirements, abuse cases (hoje).
- Design: threat modeling, princípios seguros (Aula 2).
- Implementação: secure coding, validação, authn/z, cripto (Aulas 2–4).
- Teste: SAST/DAST/IAST (Aula 5).
- Deploy/Manutenção: hardening, dependências, monitoramento (Aula 6).
- **Nota:** Reforce shift-left: quanto mais cedo, mais barato.

---

### INTERVALO (10 min)

---

### BLOCO 3 (50 min) — Security Requirements Gathering

**Slide 29 — Por que requisitos de segurança**
- O que não é requisito não é testado nem cobrado.
- Segurança "implícita" vira dívida e incidente.
- Requisitos guiam design, implementação e critérios de aceite.
- **Nota:** "Se 'o cliente só vê os próprios pedidos' não é requisito escrito, o IDOR nasce legítimo."

**Slide 30 — Requisito funcional vs. de segurança**
- Funcional: *o que* o sistema faz ("cliente cria pedido").
- Segurança: *sob quais restrições/garantias* ("apenas o dono acessa o pedido"; "senha nunca em texto claro").
- Muitos requisitos de segurança são "negativos" (o sistema NÃO deve permitir X).
- **Nota:** Apresente a tabela de contraste com 3 exemplos.

**Slide 31 — Abuse cases / Misuse cases**
- Caso de uso pela ótica do atacante: o que ele quer alcançar.
- Deriva contramedidas (requisitos de segurança).
- Notação simples: Ator malicioso → objetivo → ameaça → mitigação.
- **Nota:** No Lab 1.3 os alunos escreverão abuse cases para 2 funcionalidades.

**Slide 32 — Security User Stories**
- Formato: *"As an attacker, I want <ação> so that <ganho>"* → gera a story defensiva.
- Ex.: "As an attacker, I want to change the order id in the URL so that I can read other clients' orders."
- Defensiva: "As the system, I must verify order ownership before returning it."
- Critérios de aceite viram testes.
- **Nota:** Mostre o template do anexo. Enfatize critérios de aceite verificáveis.

**Slide 33 — OWASP ASVS (níveis 1 e 2)**
- Application Security Verification Standard: catálogo de requisitos verificáveis.
- Nível 1: básico/oportunístico; Nível 2: aplicações que lidam com dados sensíveis (nosso caso).
- Use como checklist para não esquecer categorias (V2 Auth, V3 Session, V4 Access Control, V5 Validation...).
- **Nota:** Não é preciso decorar; é preciso saber consultar. Aponte o repositório ASVS.

**Slide 34 — Derivando requisitos (exemplo 1: Cadastro de cliente)**
- Funcional: cadastrar razão social, CNPJ, e-mail, senha.
- Segurança (amostra):
  - Validar/normalizar todas as entradas (allowlist).
  - Senha via hashing forte (bcrypt/Argon2), nunca reversível.
  - Não revelar se e-mail já existe (anti user-enumeration).
- ASVS: V2 (Auth), V5 (Validation).
- **Nota:** Conecte com A02/A07 do Portal.

**Slide 35 — Derivando requisitos (exemplo 2: Upload de comprovante)**
- Funcional: enviar arquivo de comprovante ao pedido.
- Segurança (amostra):
  - Allowlist de tipos (ex.: PDF/PNG/JPG) validando magic number, não só extensão.
  - Limite de tamanho; nome de arquivo gerado pelo servidor.
  - Armazenar fora do webroot; sem execução.
- ASVS: V12 (File and Resources).
- **Nota:** Conecte com A03/A04. Antecipa Lab 2.4.

**Slide 36 — Derivando requisitos (exemplo 3: Checkout)**
- Funcional: fechar pedido e registrar pagamento.
- Segurança (amostra):
  - Reautenticar/validar posse do carrinho; preços vêm do servidor (não do cliente).
  - Dados de pagamento cifrados em repouso (AES-GCM), TLS em trânsito.
  - Log de auditoria da transação (sem dado sensível).
- ASVS: V3 (Session), V6 (Cryptography), V7 (Logging).
- **Nota:** Mostre o "preço vindo do cliente" como insecure design clássico.

**Slide 37 — Priorização por risco**
- Risco ≈ Probabilidade × Impacto.
- Priorize requisitos que mitigam riscos altos primeiro.
- Introdução informal ao STRIDE (Spoofing, Tampering, Repudiation, Info Disclosure, DoS, Elevation) — aprofundado na Aula 2.
- **Nota:** Deixe claro que na Aula 2 faremos threat modeling completo com STRIDE.

**Slide 38 — Boas práticas de escrita de requisitos**
- Verificáveis, específicos, sem ambiguidade ("deve", não "deveria").
- Rastreáveis (a qual funcionalidade/ameaça pertencem).
- Com critério de aceite/teste associado.
- **Nota:** Prepare a transição para o laboratório.

---

### BLOCO 4 (60 min) — Laboratório

**Slide 39 — Laboratório da Aula 1**
- Lab 1.1: subir o Portal de Pedidos (docker/local) e navegar.
- Lab 1.2: reconhecimento manual + preencher a Ficha de Reconhecimento.
- Lab 1.3: escrever abuse cases e derivar requisitos de segurança (2 funcionalidades).
- **Nota:** Distribua `lab.md` e os anexos. Circule pela sala; use os "Checkpoints" para validar.

---

### INTERVALO (10 min)

---

### BLOCO 5 (20 min) — Debrief + Quiz + Fechamento

**Slide 40 — Debrief do laboratório**
- Compartilhe achados da Ficha de Reconhecimento (endpoints, versões, erros verbosos).
- Compare abuse cases: quantos requisitos verificáveis cada dupla derivou?
- **Nota:** Colete 2–3 requisitos de segurança da turma e "adote-os" oficialmente para as próximas aulas.

**Slide 41 — Quiz de fixação**
- 10 perguntas (ver `quiz.md`).
- **Nota:** Aplique ao vivo; discuta as respostas erradas.

**Slide 42 — Encerramento e ponte para a Aula 2**
- Hoje: reconhecemos a aplicação e escrevemos requisitos.
- Aula 2: por que essas falhas existem (design) e como validar entrada para eliminá-las.
- Tarefa: revisar OWASP Top 10 e ler 1 seção do ASVS.
- **Nota:** Peça que mantenham o ambiente pronto; a Aula 2 parte de `aula-2-baseline`.
