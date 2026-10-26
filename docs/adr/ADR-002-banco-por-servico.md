# ADR-002: Banco de dados por microsserviço

- Status: Aceito
- Data: 2026-10-24

## Contexto
Os contextos de negócio de Solicitação e Emissão possuem responsabilidades, regras de negócio, ciclos de evolução e requisitos de disponibilidade diferentes. Eles também precisam evoluir de forma independente e não devem compartilhar schema nem transação para evitar acoplamento de persistência.

Em uma arquitetura orientada a microsserviços, compartilhar o mesmo banco entre contextos costuma criar dependências invisíveis: uma alteração em uma tabela pode impactar o outro domínio e uma falha de uma operação pode afetar o fluxo completo do sistema. Como a regra de negócio exige autonomia entre as etapas, a persistência precisa refletir essa separação.

## Decisão
Cada microsserviço possui seu próprio banco PostgreSQL:

- `solicitacao_cartao_db`
- `emissao_cartao_db`

Assim, o serviço de Solicitação mantém os dados do processo de registro e validação da solicitação, enquanto o serviço de Emissão mantém os dados de geração, status e emissão do cartão.

## Justificativa
- Isolamento de domínio: cada contexto possui seus próprios dados, estados e regras de negócio.
- Independência de evolução: a modelagem do banco pode mudar sem afetar diretamente o outro serviço.
- Redução do acoplamento: a persistência deixa de ser um ponto de dependência compartilhada entre contextos.
- Escalabilidade mais clara: cada serviço pode dimensionar e otimizar seu banco conforme sua demanda real.
- Melhor tratamento de falhas: um problema no banco de um contexto não precisa comprometer imediatamente o outro contexto.

## Consequências
- reduz o acoplamento de persistência;
- evita alterações em uma tabela afetando o outro contexto;
- permite evoluções independentes de schema e negócio;
- exige integração por evento para dados compartilhados;
- aumenta a complexidade operacional e a necessidade de observabilidade e monitoramento de cada base.
- exige consistência eventual quando informações precisam ser sincronizadas entre os contextos.

## Trade-off
A separação de bancos aumenta a autonomia e a clareza da arquitetura, mas também exige mais cuidado em integração, rastreabilidade e governança de eventos. O ganho é a previsibilidade operacional e a capacidade de evoluir cada domínio sem depender do outro.
