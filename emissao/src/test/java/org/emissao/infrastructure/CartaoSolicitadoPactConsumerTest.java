package org.emissao.infrastructure;

import au.com.dius.pact.consumer.MessagePactBuilder;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.consumer.junit5.ProviderType;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.messaging.Message;
import au.com.dius.pact.core.model.messaging.MessagePact;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.emissao.dto.SolicitacaoValidadaMessage;
import org.emissao.service.CartaoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(
        providerName = "solicitacao",
        providerType = ProviderType.ASYNCH,
        pactMethod = "cartaoSolicitadoPact",
        pactVersion = PactSpecVersion.V3
)
class CartaoSolicitadoPactConsumerTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Pact(consumer = "emissao")
    MessagePact cartaoSolicitadoPact(MessagePactBuilder builder) {
        PactDslJsonBody corpo = new PactDslJsonBody()
                .integerType("solicitacaoId", 101L)
                .stringType("clienteId", "001")
                .stringType("dataSolicitacao", "2026-10-24T18:30:00Z");

        return builder
                .given("uma solicitação de cartão foi criada")
                .expectsToReceive("evento cartao.solicitado")
                .withContent(corpo)
                .toPact(MessagePact.class);
    }

    @Test
    void deveConsumirEventoCartaoSolicitado(List<Message> mensagens) throws Exception {
        String payload = mensagens.getFirst().contentsAsString();

        CartaoService cartaoService = mock(CartaoService.class);
        SolicitacaoValidadaConsumer consumer = new SolicitacaoValidadaConsumer();
        consumer.objectMapper = objectMapper;
        consumer.cartaoApplicationService = cartaoService;

        consumer.consumir(payload);

        SolicitacaoValidadaMessage esperada = new SolicitacaoValidadaMessage(
                101L,
                "001",
                Instant.parse("2026-10-24T18:30:00Z")
        );
        verify(cartaoService).processarSolicitacaoValidada(esperada);
    }
}
