package org.emissao.infrastructure.messaging;

import au.com.dius.pact.provider.PactVerifyProvider;
import au.com.dius.pact.provider.junit5.MessageTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.emissao.dto.CartaoEmitidoEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Provider("emissao")
@PactFolder("${pact.rootDir}")
class CartaoEmitidoPactProviderTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @BeforeEach
    void configurarTarget(PactVerificationContext context) {
        context.setTarget(new MessageTestTarget());
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verificarContrato(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @State("um cartão foi emitido para a solicitação")
    void cartaoEmitido() {
        // Estado do produtor já refletido no evento abaixo; nada a preparar.
    }

    @SuppressWarnings("unchecked")
    @PactVerifyProvider("evento cartao.emitido")
    String produzirEventoCartaoEmitido() {
        CartaoEmitidoEvent evento = new CartaoEmitidoEvent(
                "101",
                "001",
                "5001",
                "1234 5678 9012 3456",
                "EMITIDO",
                Instant.parse("2026-10-24T18:30:00Z"),
                Instant.parse("2026-10-24T18:31:00Z")
        );

        Emitter<String> emitter = mock(Emitter.class);
        when(emitter.send(anyString()))
                .thenReturn(CompletableFuture.<Void>completedFuture(null));

        KafkaCartaoEmitidoPublisher publisher = new KafkaCartaoEmitidoPublisher();
        publisher.objectMapper = objectMapper;
        publisher.emitter = emitter;

        publisher.publicar(evento);

        ArgumentCaptor<String> payload = ArgumentCaptor.forClass(String.class);
        verify(emitter).send(payload.capture());

        return payload.getValue();
    }
}
