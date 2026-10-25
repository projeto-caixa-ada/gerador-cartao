# Auto-avaliação — Gerador de Cartão

Grupo: Copilot + equipe do projeto
Tema/domínio: Sistema de solicitação e emissão de cartões de crédito e débito, com separação por contextos de negócio e integração por eventos assíncronos.
Perfil de execução: A | B | C  ·  Fallbacks usados: Redis como cache de leitura; Kafka para desacoplamento entre serviços; banco PostgreSQL segregado por contexto.

## Evidências por critério

1. Decomposição de domínio — nível auto-atribuído: 5/5
   Evidência: `solicitacao/` e `emissao/` como módulos independentes; `docker-compose.yml` com bancos separados; `docs/arquitetura.md`; `docs/adr/ADR-001-separacao-de-contextos.md`; `docs/adr/ADR-002-banco-por-servico.md`

2. Comunicação assíncrona — 5/5
   Evidência: `docker-compose.yml` com Kafka; `docs/arquitetura.md` com fluxo de eventos; `docs/adr/ADR-003-comunicacao-assincrona-com-kafka.md`; `shared-contracts/eventos.json`

3. Idempotência e consistência — 4/5
   Evidência: `emissao/src/main/java/org/emissao/service/CartaoService.java` valida presença de cartão por `solicitacaoId` antes de emitir; `docs/adr/ADR-003-comunicacao-assincrona-com-kafka.md` menciona idempotência e DLT; `shared-contracts/eventos.json` padroniza payloads para reprocessamento seguro.

4. Cache — 4/5
   Evidência: `emissao/src/main/java/org/emissao/service/CartaoService.java` usa Redis para consultas por cartão e por solicitação; `docs/adr/ADR-004-cache-de-leitura-com-redis.md`; `README.md` apresenta estratégia de cache e TTL.

5. Resiliência — 4/5
   Evidência: Kafka como fila tolerante a falhas; DLQ citado em `docs/arquitetura.md` e `ADR-003`; `emissao` implementa fallback de leitura via banco quando cache falha; tratamento de erro em `SolicitacaoValidadaConsumer` e `CartaoService`.

6. Testabilidade — 4/5
   Evidência: estrutura modular em `solicitacao/` e `emissao/`; Quarkus + Maven com suporte a testes; contratos centralizados em `shared-contracts`; código organizado por camadas e serviços bem delimitados.

7. Decisões arquiteturais — 5/5
   Evidência: `docs/adr/ADR-001-separacao-de-contextos.md`, `docs/adr/ADR-002-banco-por-servico.md`, `docs/adr/ADR-003-comunicacao-assincrona-com-kafka.md`, `docs/adr/ADR-004-cache-de-leitura-com-redis.md`

8. Uso crítico de IA — 5/5
   Como usamos IA e o que validamos manualmente: a IA foi usada para estruturar a documentação, sintetizar a justificativa arquitetural, mapear ADRs e consolidar a visão de microsserviços. A validação manual foi feita verificando a coerência com a estrutura real do projeto (`solicitacao/`, `emissao/`, `docker-compose.yml`, código de Kafka e Redis) e a consistência entre documentos e implementação.

9. Execução — 5/5
   Como rodar: `docker compose up -d` para infraestrutura local; `cd solicitacao && ./mvnw quarkus:dev`; `cd emissao && ./mvnw quarkus:dev`.
   Perfil declarado: A (infraestrutura local), B (serviço de solicitação), C (serviço de emissão).

## Observação final
A arquitetura está coerente com o domínio e com a prática de microsserviços: separação de contexto, bancos isolados, comunicação assíncrona e cache de leitura com a base de dados como fonte da verdade. A documentação foi organizada para facilitar compreensão, manutenção e defesa técnica do projeto.
