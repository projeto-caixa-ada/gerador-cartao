package org.solicitacao.infrastructure.messaging;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.solicitacao.dto.CartaoEmitidoEvent;
import org.solicitacao.service.SolicitacaoApplicationService;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(
        providerName = "emissao",
        providerType = ProviderType.ASYNCH,
        pactMethod = "cartaoEmitidoPact",
        pactVersion = PactSpecVersion.V3
)
class CartaoEmitidoPactConsumerTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Pact(consumer = "solicitacao")
    MessagePact cartaoEmitidoPact(MessagePactBuilder builder) {
        PactDslJsonBody corpo = new PactDslJsonBody()
                .stringType("solicitacaoId", "101")
                .stringType("clienteId", "001")
                .stringType("cartaoId", "5001")
                .stringType("numeroCartao", "1234 5678 9012 3456")
                .stringType("status", "EMITIDO")
                .stringType("dataSolicitacao", "2026-10-24T18:30:00Z")
                .stringType("ocorridoEm", "2026-10-24T18:31:00Z");

        return builder
                .given("um cartão foi emitido para a solicitação")
                .expectsToReceive("evento cartao.emitido")
                .withContent(corpo)
                .toPact(MessagePact.class);
    }

    @Test
    void deveConsumirEventoCartaoEmitido(List<Message> mensagens) throws Exception {
        String payload = mensagens.getFirst().contentsAsString();

        SolicitacaoApplicationService service = mock(SolicitacaoApplicationService.class);
        CartaoEmitidoConsumer consumer = new CartaoEmitidoConsumer();
        consumer.objectMapper = objectMapper;
        consumer.solicitacaoApplicationService = service;

        consumer.consumir(payload);

        CartaoEmitidoEvent esperado = new CartaoEmitidoEvent(
                "101",
                "001",
                "5001",
                "1234 5678 9012 3456",
                "EMITIDO",
                Instant.parse("2026-10-24T18:30:00Z"),
                Instant.parse("2026-10-24T18:31:00Z")
        );
        verify(service).atualizarStatusPorCartaoEmitido(esperado);
    }
}
