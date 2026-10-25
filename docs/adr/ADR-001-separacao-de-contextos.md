# ADR-001: Separação por contexto de negócio

- Status: Aceito
- Data: 2026-10-24

## Contexto
O domínio de cartão possui duas responsabilidades distintas: aceitar a solicitação do cliente e emitir o produto final. Esses fluxos têm regras, dados e prazos diferentes.

O processo de solicitação e o processo de emissão são etapas distintas do negócio. A solicitação envolve cadastro, validação e acompanhamento do pedido; a emissão envolve processamento, geração do cartão e publicação do evento de conclusão. Quando mantidas no mesmo serviço, as responsabilidades ficam acopladas e a evolução de uma etapa impacta diretamente a outra.

## Decisão
Manter dois contextos: `solicitacao` e `emissao`.

## Justificativa
- Separação de responsabilidades: solicitar e emitir um cartão não são atividades equivalentes e não devem compartilhar o mesmo ciclo de vida de desenvolvimento.
- Desacoplamento operacional: cada serviço pode evoluir, testar e implantar de forma independente.
- Escalabilidade independente: o volume de solicitações pode crescer sem exigir que o processo de emissão escale na mesma proporção.
- Isolamento de falhas: um problema no fluxo de emissão não precisa bloquear imediatamente o processo de solicitação.
- Melhor manutenção: regras e estados de negócio ficam localizados no contexto correto.

## Consequências
- cada serviço tem autonomia para evoluir;
- cada base de dados é isolada;
- o acoplamento fica em eventos e contratos públicos;
- mudanças de regra de uma área não afetam diretamente a outra;
- a complexidade operacional aumenta, exigindo observabilidade, monitoramento e tratamento de falhas.

## Alternativa considerada
Manter todo o processo em um único microsserviço. Essa opção reduce a infraestrutura inicial, mas limita a independência de implantação, aumenta o acoplamento entre regras de negócio e torna a escalabilidade menos flexível.
