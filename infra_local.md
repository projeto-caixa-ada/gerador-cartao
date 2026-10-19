# Guia rápido da infra local

PostgreSQL (banco principal), Redis (cache) e Kafka (eventos), via Docker Compose.

## 1. Como subir

```bash
docker compose up -d              # sobe tudo
docker compose up -d postgres     # só um serviço
docker compose ps                 # STATUS deve ficar (healthy)
docker compose logs -f kafka      # logs de um serviço
docker compose down               # derruba (mantém dados)
docker compose down -v            # derruba e APAGA os dados
```

O Kafka leva de 15 a 30 segundos para ficar `healthy`.

## 2. Endereços e credenciais

| Serviço    | Container     | Do seu computador (IDE/Maven) | De dentro do compose |
|------------|---------------|-------------------------------|----------------------|
| PostgreSQL | `app-postgres`| `localhost:5432`              | `postgres:5432`      |
| Redis      | `app-redis`   | `localhost:6379`              | `redis:6379`         |
| Kafka      | `app-kafka`   | `localhost:9092`              | `kafka:29092`        |

**PostgreSQL**

- Banco: `appdb`
- Usuário: `app`
- Senha: `app`
- JDBC: `jdbc:postgresql://localhost:5432/appdb`

**Redis:** sem senha.

**Kafka:** sem autenticação (PLAINTEXT).

> Rodando pela IDE, use `localhost`. Rodando dentro do Docker Compose, use o
> nome do serviço (`postgres`, `redis`, `kafka:29092`). Nunca `localhost`
> dentro de um container.

## 3. Variáveis de ambiente (serviço rodando dentro do compose)

| Spring                           | Quarkus                       | Valor                                  |
|----------------------------------|-------------------------------|----------------------------------------|
| `SPRING_DATASOURCE_URL`          | `QUARKUS_DATASOURCE_JDBC_URL` | `jdbc:postgresql://postgres:5432/appdb`|
| `SPRING_DATASOURCE_USERNAME`     | `QUARKUS_DATASOURCE_USERNAME` | `app`                                  |
| `SPRING_DATASOURCE_PASSWORD`     | `QUARKUS_DATASOURCE_PASSWORD` | `app`                                  |
| `SPRING_DATA_REDIS_HOST`         | `QUARKUS_REDIS_HOSTS`         | `redis` / `redis://redis:6379`         |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `KAFKA_BOOTSTRAP_SERVERS`     | `kafka:29092`                          |

## 4. Convenções de tópicos Kafka

O broker cria tópicos automaticamente no primeiro uso (1 partição,
replicação 1). Mesmo assim, combinem os nomes antes para não haver divergência.

**Padrão sugerido:** `<dominio>.<entidade>.<evento>` em minúsculas, separado por ponto.

| Tópico (EXEMPLO, ajustar ao projeto) | Produtor    | Consumidor  | Payload |
|--------------------------------------|-------------|-------------|---------|
| `pedido.pedido.criado`               | `servico-a` | `servico-b` | JSON    |
| `pedido.pedido.cancelado`            | `servico-a` | `servico-b` | JSON    |
| `pedido.pedido.criado.dlt`           | (erros)     | (análise)   | JSON    |

**TODO do grupo:** preencher esta tabela com os tópicos reais.

Regras sugeridas:

- Um `group-id` diferente por serviço consumidor.
- Sufixo `.dlt` para tópicos de mensagens que falharam (dead letter).
- Eventos no passado (`criado`, não `criar`).
- Usar sempre o mesmo formato de serialização (JSON) e um campo `eventId` único para permitir idempotência.

## 5. Convenções de cache Redis

**Padrão de chave:** `<app>:<cache>:<id>`, ex.: `meuapp:produto:42`.

| Cache (EXEMPLO, ajustar ao projeto) | Chave                   | TTL    | Invalidar quando        |
|-------------------------------------|-------------------------|--------|-------------------------|
| `produto`                           | `meuapp:produto:{id}`   | 10 min | produto for atualizado  |
| `usuario`                           | `meuapp:usuario:{id}`   | 5 min  | usuário for alterado    |

**TODO do grupo:** preencher com os caches reais.

Regras sugeridas:

- Sempre definir TTL. Cache sem expiração vira dado velho.
- O PostgreSQL é a fonte da verdade, o Redis pode ser apagado a qualquer momento.
- Prefixo `meuapp:` evita colisão de chaves entre serviços.

## 6. Problemas comuns

| Sintoma | Causa provável | Solução |
|---------|----------------|---------|
| `Conexão recusada` na 5432 | Container parado ou porta ocupada por outro Postgres | `docker compose ps -a`, `docker compose logs postgres` |
| `port is already allocated` | Porta em uso no host | Parar o serviço local ou mudar o mapeamento (`5433:5432`) |
| Serviço no Docker não conecta no Kafka | Usando `localhost:9092` dentro do container | Usar `kafka:29092` |
| Serviço na IDE não conecta no Kafka | Usando `kafka:29092` fora do Docker | Usar `localhost:9092` |
| Kafka `unhealthy` logo após o `up` | Ainda iniciando | Esperar 30 segundos e conferir `docker compose ps` |
| Dados antigos atrapalhando | Volume com estado de testes anteriores | `docker compose down -v` |
| Quarkus sobe containers sozinho | Dev Services ativo | `quarkus.devservices.enabled=false` |
