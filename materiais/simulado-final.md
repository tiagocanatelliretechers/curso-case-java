# Simulado Final — Curso CASE Java (20 questões)

**Formato:** múltipla escolha, estilo exame de certificação. **Tempo sugerido:** 30 min (cronometrado).
Cobre proporcionalmente os 10 módulos. Gabarito comentado ao final.

---

**1. (Mód. 1)** A principal razão de a segurança de aplicação diferir da de rede é:
- a) A rede é sempre mais segura
- b) Falhas de lógica/negócio não são visíveis para controles de perímetro
- c) AppSec só existe na nuvem
- d) Firewalls resolvem SQL Injection

**2. (Mód. 1)** Qual categoria é a #1 do OWASP Top 10 (2021)?
- a) Injection
- b) Broken Access Control
- c) SSRF
- d) Cryptographic Failures

**3. (Mód. 2)** Um bom requisito de segurança é, acima de tudo:
- a) Escrito em inglês
- b) Verificável, com critério de aceite
- c) Aprovado pelo cliente
- d) Longo

**4. (Mód. 2)** "As an attacker, I want to change the order id so that I read others' orders" é:
- a) Um caso de uso funcional
- b) Um abuse case
- c) Um requisito de aceite
- d) Uma política de senha

**5. (Mód. 3)** No STRIDE, negação de serviço corresponde a:
- a) Spoofing
- b) Tampering
- c) Denial of Service
- d) Elevation of Privilege

**6. (Mód. 3)** "Defense in depth" significa:
- a) Uma única defesa muito forte
- b) Múltiplas camadas independentes de defesa
- c) Defender só o banco
- d) Usar apenas WAF

**7. (Mód. 4)** A defesa primária contra SQL Injection é:
- a) WAF
- b) Escapar aspas
- c) Queries parametrizadas
- d) Ofuscar a query

**8. (Mód. 4)** Validar upload apenas pela extensão é inseguro porque:
- a) É lento
- b) A extensão é facilmente falsificável; valide o magic number
- c) Java não lê extensões
- d) Não é inseguro

**9. (Mód. 5)** Prevenir IDOR exige:
- a) Esconder botões
- b) Verificar a posse do objeto no servidor
- c) Usar HTTPS
- d) Ofuscar ids

**10. (Mód. 5)** Segundo o NIST 800-63B, para senhas deve-se:
- a) Forçar troca a cada 30 dias
- b) Priorizar comprimento e usar rate limiting
- c) Exigir sempre símbolos e trocas frequentes
- d) Bloquear a conta após 1 erro

**11. (Mód. 6)** Para dados que precisam ser recuperados, use:
- a) Hash
- b) Cifra simétrica (AES-GCM)
- c) Base64
- d) bcrypt

**12. (Mód. 6)** Em Java, para gerar IV/token de segurança use:
- a) `java.util.Random`
- b) `Math.random()`
- c) `SecureRandom`
- d) `nanoTime()`

**13. (Mód. 7)** Regenerar o ID de sessão após login previne:
- a) SQLi
- b) Session fixation
- c) SSRF
- d) XSS

**14. (Mód. 7)** O flag de cookie que impede leitura por JavaScript é:
- a) Secure
- b) SameSite
- c) HttpOnly
- d) Path

**15. (Mód. 8)** Retornar stack trace ao cliente é:
- a) Boa prática de debug
- b) Vazamento de informação (misconfiguration/logging)
- c) Necessário em produção
- d) Irrelevante

**16. (Mód. 8)** Log injection é mitigada ao:
- a) Aumentar o nível de log
- b) Neutralizar CR/LF e usar placeholders
- c) Desligar logs
- d) Logar tudo

**17. (Mód. 9)** SAST e DAST diferem porque:
- a) São a mesma coisa
- b) SAST analisa código estático; DAST testa a app em execução
- c) DAST precisa do código-fonte
- d) SAST roda só em produção

**18. (Mód. 9)** Uma limitação do SAST é:
- a) Não achar SQLi
- b) Falsos positivos e cegueira a runtime/config
- c) Exigir a app rodando
- d) Só rodar em Python

**19. (Mód. 10)** Colocar um segredo em `ENV` no Dockerfile é ruim porque:
- a) Deixa o build lento
- b) O segredo fica gravado nas layers da imagem
- c) Docker não suporta ENV
- d) Não é problema

**20. (Mód. 10)** Um gate de segurança em CI/CD típico:
- a) Aumenta os logs
- b) Bloqueia o merge/deploy diante de finding crítico (SAST/SCA)
- c) Faz deploy sem testes
- d) Substitui o code review

---

## Gabarito comentado
1. **b** — Controles de perímetro não veem lógica de negócio.
2. **b** — Broken Access Control é #1 em 2021.
3. **b** — Verificável = testável.
4. **b** — É a ótica do atacante (abuse case).
5. **c** — D de STRIDE = Denial of Service.
6. **b** — Camadas independentes.
7. **c** — Parametrização separa dado de comando.
8. **b** — Validar conteúdo (magic number).
9. **b** — Object-level authorization no servidor.
10. **b** — Comprimento + rate limiting.
11. **b** — Precisa recuperar → cifra simétrica.
12. **c** — `SecureRandom` (CSPRNG).
13. **b** — Anti session fixation.
14. **c** — HttpOnly.
15. **b** — Vazamento de informação.
16. **b** — Neutralizar CR/LF + placeholders.
17. **b** — Estático vs. dinâmico.
18. **b** — Falsos positivos + cegueira a runtime.
19. **b** — Segredo persiste nas layers.
20. **b** — Gate bloqueia em crítico.

**Grade sugerida:** 18–20 ótimo · 14–17 bom · 10–13 revisar · <10 reforço.
