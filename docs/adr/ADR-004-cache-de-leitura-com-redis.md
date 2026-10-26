# ADR-004: Cache de leitura com Redis

- Status: Aceito
- Data: 2026-10-24

## Contexto
As consultas de cartões podem ser mais frequentes do que as alterações, especialmente em cenários de leitura repetida por clientes, consultas de status e validações em filas. Em um sistema assim, um cache pode reduzir a carga no banco e melhorar a latência das respostas.

A estratégia adotada deve preservar a fonte da verdade no banco de dados e não mascarar inconsistências. Por isso, o Redis será usado apenas como acelerador de leitura, não como repositório principal.

## Decisão
Usar Redis como cache de leitura com TTL e cache-aside, mantendo o PostgreSQL como fonte da verdade.

## Justificativa
- Melhor desempenho: leitura em memória é muito mais rápida do que consultas repetidas no banco relacional.
- Redução de carga: minimiza acessos redundantes ao banco para dados que mudam pouco ao longo do tempo.
- Preservação da persistência transacional: o banco continua sendo a origem oficial dos dados.
- Simplicidade operacional: o cache pode ser apagado, reconstruído e revalidado sem afetar a consistência principal.
- Estratégia adequada para leitura frequente: a aplicação consulta o Redis primeiro; se não houver valor, busca no banco e armazena o resultado com TTL.

## Como funciona a estratégia
- A aplicação tenta buscar o dado no Redis.
- Se o valor existir e estiver válido, retorna imediatamente.
- Se não existir, consulta o banco.
- Depois do resultado, salva o valor no cache com TTL.
- Quando há alteração do cartão ou do seu status, o cache correspondente é invalidado.

## Vantagens do Cache-Aside
- Mantém a responsabilidade na aplicação: o controle de preenchimento e invalidação fica no código de negócio.
- Preserva a persistência no banco: o Redis não substitui a base principal.
- Reduz impacto de falhas no Redis: se ele ficar indisponível, a aplicação pode consultar o banco diretamente.
- Evita popular o cache com dados que nunca serão acessados: apenas os cartões efetivamente consultados são armazenados.

## Consequências
- melhora a latência em consultas repetidas;
- reduz a pressão sobre o banco relacional;
- exige invalidação cuidadosa em atualizações e alterações de status;
- exige TTL bem definido para evitar cache stale;
- mantém consistência porque a base de dados continua sendo a origem oficial.

## Observação
Redis não deve ser usado como solução para sincronização de eventos ou substituição de fonte de verdade. Ele é uma camada de aceleração para leitura e não substitui o modelo transacional do banco.
