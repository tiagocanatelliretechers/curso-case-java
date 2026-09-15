# Aula 5 — Tratamento de Erros + Testes de Segurança (SAST & DAST)

=== Tratamento de Erros e Logging

## O que vamos aprender hoje
> Falhar sem vazar informação e encontrar falhas com ferramentas — duas competências que separam times maduros.
- Tratamento de erro seguro e logging que ajuda (sem entregar munição ao atacante).
- SAST (análise estática) e DAST (análise dinâmica): o que são, forças e limites.
- Lab: handler global de erros; scan com SonarQube/Semgrep (SAST) e OWASP ZAP (DAST).
Regra de ouro: nenhuma ferramenta sozinha cobre tudo — segurança é processo, não um botão.

## Mensagens de erro são vazamento
> Um erro verboso é um presente de reconhecimento para o atacante.
Teoria: stack traces, versões de framework e mensagens do banco revelam a stack e a estrutura interna — economizando horas do atacante.
```yaml
server.error.include-stacktrace: always   # entrega o trace ao cliente
```
Como explorar: provocar um erro (ex.: `/pedidos/abc`) e ler o stack trace para mapear classes, banco e versões.
No Portal: hoje o stack trace vai ao cliente; o Lab 5.1 move o detalhe para o log do servidor.

## Fail securely no tratamento de erros
> O comportamento diante do inesperado precisa ser "negar", nunca "abrir".
Teoria: uma exceção não tratada não pode resultar em acesso concedido; e respostas de erro devem ser consistentes (não revelar existência de recursos por diferença de mensagem).
Exemplo tangível: se buscar `/pedidos/2` de outro cliente retorna 403 e um id inexistente retorna 404, você acabou de confirmar quais pedidos existem.
Como se defende: padronize as respostas de erro e decida conscientemente o que cada status revela.

## Tratamento de exceções no Spring
> Centralize o tratamento e separe o que o cliente vê do que o servidor registra.
Teoria: um `@ControllerAdvice` com `@ExceptionHandler` devolve uma resposta genérica ao cliente e registra o detalhe (com um traceId) apenas no log do servidor.
```java
@ExceptionHandler(Exception.class)
ResponseEntity<?> handle(Exception e){
  String id = UUID.randomUUID().toString();
  log.error("Erro [{}]", id, e);                 // detalhe só no servidor
  return status(500).body(Map.of("erro","Ref: "+id)); // genérico ao cliente
}
```
No Portal: o Lab 5.1 implementa exatamente esse handler e desliga o stack trace ao cliente.

## Logging seguro
> O log é sua caixa-preta — precisa registrar o certo, e nunca o proibido.
Teoria:
- Logar: autenticação (sucesso/falha), mudanças de permissão, falhas de validação, acesso negado.
- Nunca logar: senha, token, dados de cartão, PII desnecessária.
- Log injection (CWE-117): sanitizar quebras de linha do input e usar placeholders.
```java
log.info("URL importada: {}", url.replaceAll("[\\r\\n]", "_")); // evita forjar linhas
```
No Portal: hoje a URL do import é logada crua; o Lab 5.1 corrige (ligação com A09).

=== SAST — Static Application Security Testing

## O que é SAST
> Análise do código-fonte/bytecode sem executar a aplicação — "white-box", cedo no ciclo.
Teoria: o SAST procura padrões perigosos — SQL dinâmico, cripto fraca, segredos hardcoded — direto no código, o que permite rodar já no commit/PR (shift-left).
Analogia: é o revisor que lê a planta da casa e aponta "esta parede não aguenta" antes de construir.
No Portal: muitos achados das Aulas 2–4 (SQLi, MD5, segredo) devem sumir no scan — reforço de que a correção funcionou.

## Ferramentas de SAST
> Do enterprise ao open-source, o mesmo papel com trade-offs diferentes.
Teoria:
- Enterprise: Fortify SCA (OpenText) e AppScan Source (HCL) — cobertura ampla, integração em pipeline, governança.
- Open-source/dev: SonarQube (com regras de segurança) e Semgrep (regras em YAML, rápido).
No Portal: no laboratório usamos SonarQube/Semgrep (sempre disponíveis); a trilha comercial é opcional se houver licença.

## Forças e limites do SAST
> Poderoso para achar classes conhecidas cedo — mas não enxerga tudo.
Teoria: acha padrões conhecidos sem precisar de ambiente; porém gera falsos positivos e não vê problemas de runtime, configuração ou ambiente.
Erro comum: tratar todo achado como verdadeiro — ou, no oposto, suprimir sem justificativa.
Regra de ouro: o SAST aponta onde olhar; o humano confirma e decide (corrigir ou suprimir com rastreio).

## Triando resultados de SAST
> Um relatório sem triagem vira ruído que o time aprende a ignorar.
Teoria: para cada achado, avalie severidade × confiança; priorize os exploráveis; suprima com justificativa quando for falso positivo; estabeleça um baseline e evite regressão (gate no PR).
Juntos: dado um achado de "hardcoded secret" no `application.yml`, é verdadeiro positivo? qual a ação?

=== DAST — Dynamic Application Security Testing

## O que é DAST
> Testa a aplicação em execução, como um atacante externo — "black-box", sem acesso ao código.
Teoria: o DAST navega (crawling) e ataca a aplicação rodando, encontrando problemas de configuração, headers ausentes e comportamento real (ex.: XSS refletido).
Analogia: é o "cliente oculto" que testa as portas do prédio já construído, sem ver a planta.
No Portal: rodamos o OWASP ZAP contra a aplicação para revelar o que só aparece em execução.

## Ferramentas de DAST
> Também do enterprise ao open-source, com o mesmo papel.
Teoria:
- Enterprise: AppScan Standard (HCL) e WebInspect (OpenText) — crawling, ataque automatizado, scans autenticados, relatórios corporativos.
- Open-source: OWASP ZAP — proxy/scanner completo, com baseline scan e scan autenticado.
Na prática: todos precisam de tuning para cobrir fluxos autenticados e complexos.

## Forças e limites do DAST
> Enxerga o que o SAST não vê — mas depende de cobertura.
Teoria: acha problemas de config e runtime com poucos falsos positivos em certas classes; porém a cobertura depende do crawling e é difícil em SPAs/fluxos complexos sem configuração.
No Portal: o ZAP revela, por exemplo, o Actuator exposto e headers de segurança ausentes.
Como se defende: combine o scan com fluxos autenticados e casos de negócio manuais.

## SAST x DAST x IAST
> Técnicas complementares — um processo maduro usa mais de uma.
Teoria: SAST (código) acha o segredo hardcoded e o MD5; DAST (execução) acha o Actuator aberto e os headers ausentes; IAST instrumenta a aplicação em runtime durante os testes, unindo visões.
Exemplo tangível: no Portal, cada técnica encontrou coisas diferentes — nenhuma sozinha cobriria tudo.
Regra de ouro: some SAST + DAST + SCA (dependências, Aula 6) para uma rede de segurança de verdade.

=== Laboratório e Fechamento

## Laboratório da Aula 5 (visão geral)
> Duas trilhas: open-source (sempre disponível) e comercial (opcional, com licença).
- Lab 5.1 — Handler global de erros + correção de log injection.
- Lab 5.2 — SAST: SonarQube/Semgrep no Portal; triar ≥ 5 achados; corrigir 1 alto (ou Fortify/AppScan Source).
- Lab 5.3 — DAST: OWASP ZAP (baseline + scan autenticado); validar 1 achado manualmente (ou WebInspect/AppScan Standard).
No Portal: o guia detalhado está no deck de laboratório (lab-aula-5) e no lab.md.

## Fechamento e ponte para a Aula 6
> Hoje aprendemos a falhar com segurança e a caçar falhas com ferramentas.
- Você padronizou o tratamento de erro, corrigiu a log injection e rodou SAST e DAST no Portal.
- Na Aula 6: levar a segurança até a produção — hardening, dependências, containers e gates de CI/CD.
Tarefa: revise SAST × DAST e triagem de findings. A Aula 6 parte de `aula-6-baseline`.
