# Guia rápido da infra local

Dois bancos PostgreSQL (um por serviço), Redis (cache) e Kafka (eventos), via Docker Compose.

## 1. Como subir

```bash
docker compose up -d                                # sobe tudo
docker compose up -d postgres-solicitacao redis     # só o que precisar
docker compose ps                                   # STATUS deve ficar (healthy)
docker compose logs -f kafka                        # logs de um serviço
docker compose down                                 # derruba (mantém dados)
docker compose down -v                              # derruba e APAGA os dados
```

O Kafka leva de 15 a 30 segundos para ficar `healthy`.

## 2. Endereços e credenciais

| Serviço                         | Container                  | Do seu computador (IDE/Maven) | De dentro do compose           |
|---------------------------------|----------------------------|-------------------------------|--------------------------------|
| PostgreSQL (solicitação)        | `app-postgres-solicitacao` | `localhost:5432`              | `postgres-solicitacao:5432`    |
| PostgreSQL (emissão)            | `app-postgres-emissao`     | `localhost:5433`              | `postgres-emissao:5432`        |
| Redis                           | `app-redis`                | `localhost:6379`              | `redis:6379`                   |
| Kafka                           | `app-kafka`                | `localhost:9092`              | `kafka:29092`                  |

**PostgreSQL (mesmo usuário e senha nos dois)**

- Usuário: `app`
- Senha: `app`

| Serviço   | Banco                   | JDBC na IDE                                         | JDBC dentro do compose                                          |
|-----------|-------------------------|-----------------------------------------------------|-----------------------------------------------------------------|
| Solicitação | `solicitacao_cartao_db` | `jdbc:postgresql://localhost:5432/solicitacao_cartao_db` | `jdbc:postgresql://postgres-solicitacao:5432/solicitacao_cartao_db` |
| Emissão     | `emissao_cartao_db`     | `jdbc:postgresql://localhost:5433/emissao_cartao_db`     | `jdbc:postgresql://postgres-emissao:5432/emissao_cartao_db`         |

**Redis:** sem senha.

**Kafka:** sem autenticação (PLAINTEXT).

> Rodando pela IDE, use `localhost` (e a porta **5433** para o banco de emissão).
> Rodando dentro do Docker Compose, use o nome do serviço, e a porta do
> Postgres é sempre **5432**. Nunca `localhost` dentro de um container.

## 3. Variáveis de ambiente (serviço rodando dentro do compose)

**Redis e Kafka (iguais nos dois serviços)**

| Spring                           | Quarkus                       | Valor                          |
|----------------------------------|-------------------------------|--------------------------------|
| `SPRING_DATA_REDIS_HOST`         | `QUARKUS_REDIS_HOSTS`         | `redis` / `redis://redis:6379` |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `KAFKA_BOOTSTRAP_SERVERS`     | `kafka:29092`                  |

**Banco do serviço de solicitação**

| Spring                       | Quarkus                       | Valor                                                               |
|------------------------------|-------------------------------|---------------------------------------------------------------------|
| `SPRING_DATASOURCE_URL`      | `QUARKUS_DATASOURCE_JDBC_URL` | `jdbc:postgresql://postgres-solicitacao:5432/solicitacao_cartao_db` |
| `SPRING_DATASOURCE_USERNAME` | `QUARKUS_DATASOURCE_USERNAME` | `app`                                                               |
| `SPRING_DATASOURCE_PASSWORD` | `QUARKUS_DATASOURCE_PASSWORD` | `app`                                                               |

**Banco do serviço de emissão**

| Spring                       | Quarkus                       | Valor                                                       |
|------------------------------|-------------------------------|-------------------------------------------------------------|
| `SPRING_DATASOURCE_URL`      | `QUARKUS_DATASOURCE_JDBC_URL` | `jdbc:postgresql://postgres-emissao:5432/emissao_cartao_db` |
| `SPRING_DATASOURCE_USERNAME` | `QUARKUS_DATASOURCE_USERNAME` | `app`                                                       |
| `SPRING_DATASOURCE_PASSWORD` | `QUARKUS_DATASOURCE_PASSWORD` | `app`                                                       |

## 4. Convenções de tópicos Kafka

O broker cria tópicos automaticamente no primeiro uso (1 partição,
replicação 1). Mesmo assim, combinem os nomes antes para não haver divergência.

**Padrão sugerido:** `<dominio>.<entidade>.<evento>` em minúsculas, separado por ponto.

| Tópico (EXEMPLO, ajustar ao projeto) | Produtor              | Consumidor            | Payload |
|--------------------------------------|-----------------------|-----------------------|---------|
| `cartao.solicitacao.criada`          | `servico-solicitacao` | `servico-emissao`     | JSON    |
| `cartao.emissao.concluida`           | `servico-emissao`     | `servico-solicitacao` | JSON    |
| `cartao.solicitacao.criada.dlt`      | (erros)               | (análise)             | JSON    |

**TODO do grupo:** preencher esta tabela com os tópicos reais.

Regras sugeridas:

- Um `group-id` diferente por serviço consumidor.
- Sufixo `.dlt` para tópicos de mensagens que falharam (dead letter).
- Eventos no passado (`criada`, não `criar`).
- Usar sempre o mesmo formato de serialização (JSON) e um campo `eventId` único para permitir idempotência.

## 5. Convenções de cache Redis

**Padrão de chave:** `<app>:<cache>:<id>`, ex.: `gerador-cartao:solicitacao:42`.

| Cache (EXEMPLO, ajustar ao projeto) | Chave                              | TTL    | Invalidar quando             |
|-------------------------------------|------------------------------------|--------|------------------------------|
| `solicitacao`                       | `gerador-cartao:solicitacao:{id}`  | 10 min | solicitação for atualizada   |
| `cartao`                            | `gerador-cartao:cartao:{id}`       | 5 min  | cartão for alterado          |

**TODO do grupo:** preencher com os caches reais.

Regras sugeridas:

- Sempre definir TTL. Cache sem expiração vira dado velho.
- O PostgreSQL é a fonte da verdade, o Redis pode ser apagado a qualquer momento.
- O prefixo `gerador-cartao:` evita colisão de chaves entre serviços.
- Cada serviço acessa apenas o próprio banco. Dados do outro serviço chegam por evento Kafka, nunca por consulta direta ao banco dele.

## 6. Problemas comuns

| Sintoma | Causa provável | Solução |
|---------|----------------|---------|
| `Conexão recusada` na 5432 ou 5433 | Container parado ou porta ocupada por outro Postgres | `docker compose ps -a`, `docker compose logs postgres-solicitacao` (ou `postgres-emissao`) |
| `port is already allocated` | Porta em uso no host | Parar o serviço local ou mudar o mapeamento (ex.: `5434:5432`) |
| Serviço de emissão não conecta no banco pela IDE | Usando a porta 5432 | Usar `localhost:5433` |
| Serviço de emissão não conecta no banco dentro do Docker | Usando `localhost` ou a porta 5433 | Usar `postgres-emissao:5432` |
| Serviço no Docker não conecta no Kafka | Usando `localhost:9092` dentro do container | Usar `kafka:29092` |
| Serviço na IDE não conecta no Kafka | Usando `kafka:29092` fora do Docker | Usar `localhost:9092` |
| `database "appdb" does not exist` | Configuração antiga apontando para o banco `appdb` | Usar `solicitacao_cartao_db` ou `emissao_cartao_db` |
| Container `app-postgres` antigo ainda aparece | Sobra do compose anterior | `docker compose up -d --remove-orphans` |
| Kafka `unhealthy` logo após o `up` | Ainda iniciando | Esperar 30 segundos e conferir `docker compose ps` |
| Dados antigos atrapalhando | Volume com estado de testes anteriores | `docker compose down -v` |
| Quarkus sobe containers sozinho | Dev Services ativo | `quarkus.devservices.enabled=false` |