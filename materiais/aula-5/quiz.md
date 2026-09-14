# Aula 5 — Quiz de fixação (10 questões)

**1.** Retornar o stack trace completo ao usuário final é problema porque:
- a) Deixa a página lenta
- b) Vaza informação (framework, banco, estrutura) útil ao atacante
- c) Consome banda
- d) Não é problema

**2.** "Fail securely" em tratamento de erros significa:
- a) Registrar o erro e liberar o acesso
- b) Em caso de erro, negar por padrão
- c) Reiniciar a aplicação
- d) Mostrar detalhes ao usuário

**3.** No Spring, o mecanismo idiomático para respostas de erro padronizadas é:
- a) `System.out.println`
- b) `@ControllerAdvice` + `@ExceptionHandler`
- c) `try/catch` em cada método
- d) Um filtro de rede

**4.** O que **nunca** deve ir para os logs?
- a) Timestamp
- b) Senhas, tokens e dados de cartão
- c) Nível do log
- d) ID de correlação

**5.** Log injection (CWE-117) é mitigada ao:
- a) Aumentar o nível de log
- b) Neutralizar quebras de linha do input e usar placeholders
- c) Desligar os logs
- d) Logar em JSON apenas

**6.** SAST se caracteriza por:
- a) Testar a aplicação em execução
- b) Analisar código-fonte/bytecode sem executar
- c) Só funcionar em produção
- d) Ser um WAF

**7.** DAST se caracteriza por:
- a) Analisar o código estático
- b) Testar a aplicação em execução, como atacante externo
- c) Precisar do código-fonte
- d) Ser um linter

**8.** Uma limitação típica do SAST é:
- a) Não encontrar SQL Injection
- b) Gerar falsos positivos e não ver problemas de runtime/config
- c) Exigir a app em produção
- d) Não rodar em Java

**9.** Qual combinação reflete um processo maduro de testes de segurança?
- a) Só SAST
- b) Só DAST
- c) SAST + DAST (+ IAST/SCA), pois se complementam
- d) Só revisão manual

**10.** (Laboratório) Ao rodar o SonarQube na branch da Aula 5, os findings de SQLi e MD5:
- a) Aparecem em maior número
- b) Não aparecem mais (foram corrigidos nas Aulas 2 e 3)
- c) Viram falsos positivos
- d) Quebram o scanner

---

## Gabarito comentado
1. **b** — Stack trace é reconhecimento pronto para o atacante.
2. **b** — Erro → negar por padrão.
3. **b** — `@ControllerAdvice`/`@ExceptionHandler`.
4. **b** — Segredos e dados sensíveis jamais em log.
5. **b** — Neutralizar CR/LF + placeholders.
6. **b** — SAST = estático, sem executar.
7. **b** — DAST = dinâmico, app rodando.
8. **b** — Falsos positivos e cegueira a runtime/config.
9. **c** — Combinação de técnicas.
10. **b** — Correções anteriores fazem os findings sumirem.
