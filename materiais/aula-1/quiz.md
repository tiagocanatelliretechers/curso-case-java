# Aula 1 — Quiz de fixação (10 questões)

> Marque uma alternativa. Gabarito comentado ao final.

**1.** Por que um firewall de rede tradicional normalmente **não** impede um ataque de IDOR?
- a) Porque o firewall não inspeciona a porta 443
- b) Porque a requisição do IDOR é sintaticamente válida e a decisão de acesso é do código da aplicação
- c) Porque IDOR só ocorre em redes internas
- d) Porque o firewall bloqueia apenas SQL

**2.** No OWASP Top 10 (2021), qual categoria ocupa a **primeira** posição?
- a) Injection
- b) Cryptographic Failures
- c) Broken Access Control
- d) Security Misconfiguration

**3.** Armazenar dados de pagamento com `Base64.getEncoder()` é um exemplo de:
- a) Criptografia simétrica adequada
- b) Cryptographic Failure — Base64 é codificação, não criptografia
- c) Hashing seguro
- d) Tokenização

**4.** Qual afirmação melhor distingue **Insecure Design** de **Security Misconfiguration**?
- a) São sinônimos
- b) Insecure Design é ausência de controle desde a concepção; Misconfiguration é um controle existente mal configurado
- c) Misconfiguration só ocorre em produção
- d) Insecure Design só se aplica a mobile

**5.** No estudo de caso, qual foi a sequência que caracterizou o ataque encadeado?
- a) DoS → phishing → ransomware
- b) Recon (Actuator) → Injection (SQLi) → Crypto Failure (MD5) → Broken Access Control
- c) XSS → CSRF → SSRF
- d) Apenas um SQL Injection isolado

**6.** Um **requisito de segurança** difere de um **requisito funcional** porque:
- a) É sempre opcional
- b) Descreve restrições/garantias e frequentemente é "negativo" (o sistema NÃO deve…)
- c) Só existe em projetos bancários
- d) É escrito apenas após o deploy

**7.** Qual é um exemplo correto de **abuse case** para o upload de comprovante?
- a) "Como cliente, quero anexar meu comprovante em PDF"
- b) "As an attacker, I want to upload a `.jsp` so that I can execute code on the server"
- c) "O sistema deve validar o magic number do arquivo"
- d) "O upload deve ter no máximo 5 MB"

**8.** O que torna um requisito de segurança **verificável**?
- a) Ter sido aprovado pelo gerente
- b) Possuir um critério de aceite testável (dado/quando/então)
- c) Estar escrito em inglês
- d) Referenciar uma CVE

**9.** Para que serve o OWASP **ASVS**?
- a) É um scanner automático de vulnerabilidades
- b) É um WAF
- c) É um padrão com requisitos de segurança verificáveis, organizados por nível
- d) É um framework Java

**10.** (Baseado no laboratório) Ao acessar `/actuator/env` no Portal, o que o aluno observou?
- a) Erro 403 (protegido)
- b) Configurações internas, incluindo o segredo do JWT e dados do datasource
- c) A página de login
- d) O catálogo de produtos

---

## Gabarito comentado

1. **b** — O controle de acesso é lógica de aplicação; a rede vê uma requisição HTTP válida.
2. **c** — Broken Access Control subiu para #1 em 2021.
3. **b** — Base64 é reversível trivialmente; não oferece confidencialidade.
4. **b** — Design = ausência do controle na concepção; Misconfiguration = controle existente mal ajustado.
5. **b** — Encadeamento recon→injection→crypto→access control (Slide 25).
6. **b** — Requisitos de segurança frequentemente restringem comportamento e são negativos.
7. **b** — Abuse case é a ótica do atacante; (c) e (d) são requisitos/derivados, (a) é caso de uso funcional.
8. **b** — Verificável = tem teste de aceite objetivo.
9. **c** — ASVS é um padrão de verificação, não uma ferramenta.
10. **b** — Actuator exposto revela segredos (A05/A02) — achado central do Lab 1.2.
