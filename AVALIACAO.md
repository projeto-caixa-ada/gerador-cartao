# Auto-avaliação — Gerador de Cartão

Grupo: Copilot + equipe do projeto — integrantes e papéis: _preencher_
Tema/domínio: sistema de solicitação e emissão de cartões, com contextos separados, banco por serviço, eventos no Kafka e cache de leitura no Redis.
Perfil de execução: **A (Docker Compose)** · Fallbacks documentados: **B (híbrido: infraestrutura em Docker + aplicações na JVM)**, testes sem infraestrutura e leitura direta no PostgreSQL quando o Redis falha.

## Evidências por critério

1. **Decomposição de domínio** — nível auto-atribuído: **3/3**
   Evidência: `solicitacao/` e `emissao/` como serviços independentes; `docker-compose.yml` com `postgres-solicitacao` e `postgres-emissao` (bases segregadas); `docs/arquitetura.md` (§1–3); `docs/adr/ADR-001-separacao-de-contextos.md` e `ADR-002-banco-por-servico.md`.

2. **Comunicação assíncrona** — **3/3**
   Evidência: `KafkaSolicitacaoPublisher` publica `cartao.solicitado`; `SolicitacaoValidadaConsumer` consome e `CartaoEmitidoConsumer` fecha o ciclo com `cartao.emitido`; tópicos e `acks=all` em `solicitacao/src/main/resources/application.properties`; garantia **at-least-once** declarada em `docs/arquitetura.md` (§4 e §8), `ADR-003` e `ADR-005`; contratos em `shared-contracts/eventos.json`. Os contratos são eventos de domínio (fatos ocorridos), não comandos.

3. **Idempotência e consistência** — **2/3**
   Evidência: `emissao/.../service/CartaoService.java` verifica cartão existente por `solicitacaoId` e ignora evento duplicado; `solicitacao/.../service/SolicitacaoApplicationService.java` só atualiza para `EMITIDA` se o status for `ABERTA`; `ADR-003` e `ADR-005`. Há teste unitário de reprocessamento sem duplicar (`CartaoServiceTest.processarSolicitacaoValidadaIgnoraEventoDuplicado`). Não há SAGA nesta versão (o fluxo tem duas etapas).

4. **Cache** — **3/3**
   Evidência: `emissao/.../service/CartaoService.java` implementa cache-aside nas consultas por `cartaoId` e por `solicitacaoId`, com `SETEX` e TTL de 20 s; invalidação por `DEL` em `atualizarStatus` e endpoint `PATCH /api/emissoes/cartoes/solicitacao/{cartaoId}/cache` em `CartaoResource`; fallback para o banco quando o Redis falha; `ADR-004` e `docs/arquitetura.md` (§5).

5. **Resiliência** — **2/3**
   Evidência: `KafkaSolicitacaoPublisher` espera o ack (`.get(3, SECONDS)`) antes de responder, com `@Retry(maxRetries = 4, delay = 1000, jitter = 200)`, `@Timeout(5000)` e `@CircuitBreaker`; produtor com `acks=all`, `enable.idempotence=true` e timeouts encadeados; erros mapeados para `503`; consumo com `failure-strategy=dead-letter-queue` para `cartao.dlt`; `ADR-005`. Testes unitários cobrem sucesso, falha de serialização, `ExecutionException`, `TimeoutException` e interrupção (`KafkaSolicitacaoPublisherTest`).

6. **Testabilidade** — **2/3**
   Evidência: **77 testes JUnit 5** (73 unitários + 4 de contrato), executados sem Docker/PostgreSQL/Kafka/Redis: 37 em `solicitacao/src/test/java` e 40 em `emissao/src/test/java`. Cobertura de linhas pelo JaCoCo: **95,45%** em `solicitacao` e **98,26%** em `emissao`; `./mvnw clean verify` verde nos dois serviços, com `jacoco:check` exigindo mínimo de 80% de linhas. Os unitários cobrem criação/consulta de solicitação, publicação e consumo Kafka, cache-aside, invalidação, idempotência, validações e mapeadores. O contract test (Pact) cobre os dois sentidos: `emissao` consome `cartao.solicitado` de `solicitacao`, e `solicitacao` consome `cartao.emitido` de `emissao`; cada serviço também verifica o evento que publica contra o pact versionado em `shared-contracts/pacts`. Não há teste de integração/e2e com a infraestrutura real, por isso o nível não é 3.

7. **Decisões arquiteturais** — **3/3**
   Evidência: `docs/adr/ADR-001` a `ADR-005`, todos com contexto, decisão, justificativa, consequências e alternativas consideradas/trade-offs.

8. **Uso crítico de IA** — **2/3**
   Como usamos IA e o que validamos manualmente: a IA apoiou a redação e a estruturação dos ADRs, do `docs/arquitetura.md` e da análise de trade-offs (TTL do cache, timeouts e garantia de entrega). A validação manual comparou cada documento com o código (`CartaoService`, `KafkaSolicitacaoPublisher`, `application.properties`, `docker-compose.yml`) e ajustou o que não correspondia: alinhamento do `201` para `202` e enxugamento dos ADRs para manter apenas o que sustenta a decisão. Falta registrar exemplos concretos de sugestões descartadas para sustentar nível 3.

9. **Execução comprovada** — **3/3**
   Como rodar: `docker compose up -d --build` (perfil A) e acessar `http://localhost:8081` e `http://localhost:8083` (Swagger em `/q/swagger-ui`). Alternativa híbrida: subir só a infraestrutura com `docker compose up -d postgres-solicitacao postgres-emissao redis kafka` e rodar `cd solicitacao && ./mvnw quarkus:dev` e `cd emissao && ./mvnw quarkus:dev`. O histórico do git registra commits ao longo do desenvolvimento, não um único dump.

## Resumo

| # | Critério | Peso | Nível | Pontos |
|---|---|:---:|:---:|:---:|
| 1 | Decomposição de domínio | 15 | 3 | 15,0 |
| 2 | Comunicação assíncrona | 15 | 3 | 15,0 |
| 3 | Idempotência e consistência | 12 | 2 | 8,0 |
| 4 | Cache | 10 | 3 | 10,0 |
| 5 | Resiliência | 12 | 2 | 8,0 |
| 6 | Testabilidade | 13 | 2 | 8,7 |
| 7 | Decisões arquiteturais | 13 | 3 | 13,0 |
| 8 | Uso crítico de IA | 5 | 2 | 3,3 |
| 9 | Execução comprovada | 5 | 3 | 5,0 |
| | **Total** | **100** | | **86,0** |

## Opcionais entregues

- **Resiliência avançada:** retry com backoff e jitter, timeout, circuit breaker no publicador e DLT no consumo.
- **Uso documentado de IA:** critério 8 e ajustes registrados neste arquivo.

## Pendências antes da banca

- **Testes de integração/e2e:** não há execução automatizada contra a infraestrutura real; a validação é unitária e de contrato.
- **Uso crítico de IA:** faltam exemplos concretos de sugestões descartadas para sustentar o nível 3 no critério 8.
- **Opcionais não implementados:** SAGA/compensação e outbox não fazem parte desta versão.

## Observações

- Estrutura de build: dois projetos Maven independentes (`solicitacao/pom.xml` e `emissao/pom.xml`), sem `pom.xml` raiz multi-módulo; o build e a execução são feitos por serviço.
- Testes: `cd solicitacao && ./mvnw clean verify` e `cd emissao && ./mvnw clean verify`. Os relatórios ficam em `target/site/jacoco/index.html` (linhas) e o build falha se a cobertura de linhas ficar abaixo de 80% (`jacoco:check`). Os contract tests usam `pact.rootDir` apontando para `shared-contracts/pacts` e verificam os eventos nos dois sentidos.
- O Redis é cache operacional; a fonte da verdade permanece no PostgreSQL de cada contexto.
