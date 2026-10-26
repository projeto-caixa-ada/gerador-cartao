# ADR-005: Resiliência na publicação de eventos para o Kafka

- Status: Aceito
- Data: 2026-10-26

## Contexto
O `POST /api/solicitacoes` grava a solicitação no PostgreSQL e publica `cartao.solicitado` no Kafka. Como o Kafka não participa da transação do banco, a publicação pode falhar por indisponibilidade, pode ser confirmada e o commit falhar depois, ou pode duplicar a mensagem. Sem tratamento, o resultado é solicitação sem cartão, thread presa ou cartão duplicado.

## Decisão
Adotar resiliência em camadas na publicação, com **entrega pelo menos uma vez (at-least-once) e consumidores idempotentes**:

| Camada | Implementação |
|---|---|
| Ack na requisição | `emitter.send(...).get(3, SECONDS)` — só responde `202` após o ack |
| Durabilidade | `acks=all` |
| Idempotência do produtor | `enable.idempotence=true` |
| Retry e timeout | `@Retry(maxRetries = 4, delay = 1000, jitter = 200)` + `@Timeout(5000)` |
| Erro ao cliente | `PublicacaoEventoException` mapeada para `503` |
| Idempotência no consumo | cartão já existente para o `solicitacaoId` é ignorado |
| Mensagem inválida | `failure-strategy=dead-letter-queue` → `cartao.dlt` |

## Justificativa
- **Não responder sem confirmação:** o `202` só é devolvido após o ack. Se a publicação falha, a transação sofre rollback e nenhuma solicitação órfã fica gravada; o `flush()` garante que o ID do evento já existe.
- **Consistência sem transação distribuída:** a garantia realista é at-least-once. A duplicata é resolvida no consumidor pela chave de negócio `solicitacaoId`.
- **Falha transitória versus prolongada:** o retry com jitter absorve reinício de broker sem efeito manada; o timeout impede envio pendurado e falha prolongada retorna `503`.

## Consequências
- o cliente recebe `202` apenas quando o evento foi aceito pelo Kafka;
- falhas transitórias são absorvidas; falhas prolongadas degradam para `503` em vez de esgotar threads;
- duplicatas são toleradas e descartadas no consumo, não prevenidas na origem;
- a latência do caminho de erro é maior, em torno de 15 s com o Kafka fora;
- a publicação não é atômica com o commit: se o commit falhar após o envio, o evento fica órfão;
- a DLT não reprocessa sozinha: exige replay manual.

## Alternativas consideradas
- **REST síncrono entre os serviços:** simples, mas exige disponibilidade simultânea e aumenta o acoplamento.
- **Transações Kafka integradas ao JPA (XA):** daria atomicidade, mas acopla o domínio ao broker e não resolve o efeito colateral no banco do consumidor.
