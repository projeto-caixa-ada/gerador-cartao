# Shared contracts

Este diretório centraliza os contratos compartilhados entre os microsserviços.

## Objetivo
- manter eventos e payloads consistentes entre `solicitacao` e `emissao`;
- favorecer evolução controlada dos schemas;
- reduzir risco de incompatibilidade entre producer e consumer.

## Contratos principais

### Evento: `cartao.solicitado`
- producer: `solicitacao`
- consumer: `emissao`
- payload:
  - `solicitacaoId`
  - `clienteId`
  - `dataSolicitacao`

### Evento: `cartao.emitido`
- producer: `emissao`
- consumer: `solicitacao` e consumidores externos
- payload:
  - `solicitacaoId`
  - `clienteId`
  - `cartaoId`
  - `numeroCartao`
  - `status`
  - `dataSolicitacao`
  - `ocorridoEm`

## Exemplo de contrato

O arquivo `eventos.json` contém exemplos executáveis de payloads no formato JSON.

## Contract tests (Pact)

Os contratos são verificados nos dois sentidos, com mensagens assíncronas (Pact V3):

- `pacts/emissao-solicitacao.json`: consumer `emissao` verifica o evento `cartao.solicitado` produzido por `solicitacao`.
- `pacts/solicitacao-emissao.json`: consumer `solicitacao` verifica o evento `cartao.emitido` produzido por `emissao`.

Cada serviço executa o consumer test do evento que consome e a verificação do provider do evento que publica. Os testes rodam no `test`/`verify`, sem Docker, PostgreSQL, Kafka ou Redis:

```bash
cd emissao && ./mvnw clean verify
cd solicitacao && ./mvnw clean verify
```

O diretório dos pacts é configurado por `pact.rootDir` nos POMs. Os arquivos gerados ficam versionados para que cada serviço consiga verificar o contrato de forma independente.
