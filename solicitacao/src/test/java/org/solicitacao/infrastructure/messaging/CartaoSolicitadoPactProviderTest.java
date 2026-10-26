package org.solicitacao.infrastructure.messaging;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.solicitacao.dto.SolicitacaoCriadaEvent;
import org.solicitacao.entity.Solicitacao;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Provider("solicitacao")
@PactFolder("${pact.rootDir}")
class CartaoSolicitadoPactProviderTest {

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

    @State("uma solicitação de cartão foi criada")
    void solicitacaoCriada() {
        // Estado do produtor já refletido no evento abaixo; nada a preparar.
    }

    @SuppressWarnings("unchecked")
    @PactVerifyProvider("evento cartao.solicitado")
    String produzirEventoCartaoSolicitado() {
        Solicitacao solicitacao = new Solicitacao();
        solicitacao.setId(101L);
        solicitacao.setClienteId("001");
        solicitacao.setCriadoEm(Instant.parse("2026-10-24T18:30:00Z"));

        Emitter<String> emitter = mock(Emitter.class);
        when(emitter.send(anyString()))
                .thenReturn(CompletableFuture.<Void>completedFuture(null));

        KafkaSolicitacaoPublisher publisher = new KafkaSolicitacaoPublisher();
        publisher.objectMapper = objectMapper;
        publisher.emitter = emitter;

        publisher.publicar(SolicitacaoCriadaEvent.from(solicitacao));

        ArgumentCaptor<String> payload = ArgumentCaptor.forClass(String.class);
        verify(emitter).send(payload.capture());

        return payload.getValue();
    }
}
