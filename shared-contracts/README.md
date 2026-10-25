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
