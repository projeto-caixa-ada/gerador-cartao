# ADR-003: Comunicação assíncrona com Kafka

- Status: Aceito
- Data: 2026-10-24

## Contexto
A geração do cartão depende de um processo que pode ser processado depois da validação inicial. Também é importante desacoplar a criação da solicitação da emissão final.

No projeto, a solicitação e a emissão são etapas distintas do negócio, com necessidades diferentes de tempo de resposta, carga e disponibilidade. Em cenários como este, uma chamada síncrona direta via REST seria inadequada porque aumenta o acoplamento entre serviços e exige a disponibilidade simultânea dos dois lados.

## Decisão
Usar Kafka para publicar eventos de negócio entre serviços.

## Justificativa
- Desacoplamento entre microsserviços: o serviço de Solicitação não precisa chamar diretamente o serviço de Emissão. Ele publica um evento e o consumidor processa quando estiver disponível.
- Resiliência e tolerância a falhas: se o serviço de Emissão ficar temporariamente indisponível, as mensagens podem ser retidas pelo Kafka e processadas posteriormente.
- Escalabilidade independente: o número de consumidores pode aumentar sem exigir que o serviço de Solicitação seja escalado na mesma proporção.
- Recuperação e reprocessamento: eventos falhos podem ser retentados e enviados para uma Dead Letter Topic (DLT) quando necessário.
- Retenção de eventos: mensagens podem permanecer disponíveis por um período configurado, permitindo reprocessamento posterior e rastreabilidade.
- Adoção de padrões de integração: a arquitetura fica alinhada com microsserviços orientados a eventos, reduzindo dependências de implementação e facilitando evolução independente.

## Boas práticas que reforçam a decisão
- Idempotência no consumidor: impedir que a mesma mensagem gere duas emissões do mesmo cartão.
- Retentativas e DLT: tratar falhas transitórias e enviar mensagens problemáticas para fila de análise.
- Rastreabilidade: propagar identificadores de correlação entre solicitação, evento e emissão.
- Segurança: publicar apenas dados mínimos necessários no evento.
- Observabilidade: monitorar volume, latência, erros e acúmulo de mensagens no tópico.

## Consequências
- reduz acoplamento temporal entre serviços;
- permite fila de processamento e tolerância a picos de carga;
- exige idempotência e tratamento de falhas em DLQ;
- aumenta complexidade operacional de infraestrutura, monitoramento e recuperação;
- exige estratégia clara de versionamento de contratos e governança de eventos.

## Observação
Kafka não substitui REST em cenários de resposta imediata e consultas síncronas. Ele é mais adequado quando o processo é assíncrono, desacoplado, tolerante a falhas e orientado a eventos.
