package org.emissao.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.emissao.dto.CartaoEmitidoEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaCartaoEmitidoPublisherTest {

    @Mock
    ObjectMapper objectMapper;

    @Mock
    ObjectWriter objectWriter;

    @Mock
    Emitter<String> emitter;

    KafkaCartaoEmitidoPublisher publisher;

    private final CartaoEmitidoEvent evento = new CartaoEmitidoEvent(
            "101",
            "cliente",
            "5001",
            "1000000000000000",
            "EMITIDO",
            Instant.parse("2026-10-26T10:00:00Z"),
            Instant.parse("2026-10-26T10:00:05Z")
    );

    @BeforeEach
    void setUp() {
        publisher = new KafkaCartaoEmitidoPublisher();
        publisher.objectMapper = objectMapper;
        publisher.emitter = emitter;
    }

    @Test
    void publicaEventoNoCanalCartaoEmitido() throws Exception {
        when(objectMapper.writeValueAsString(evento)).thenReturn("{\"cartaoId\":\"5001\"}");
        when(objectMapper.writerWithDefaultPrettyPrinter()).thenReturn(objectWriter);
        when(objectWriter.writeValueAsString(evento)).thenReturn("{\n  \"cartaoId\": \"5001\"\n}");

        publisher.publicar(evento);

        verify(emitter).send("{\"cartaoId\":\"5001\"}");
    }

    @Test
    void lancaIllegalStateExceptionQuandoFalhaAoSerializar() throws Exception {
        JsonProcessingException causa = new JsonProcessingException("falha") {
        };
        when(objectMapper.writeValueAsString(evento)).thenThrow(causa);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> publisher.publicar(evento)
        );

        assertTrue(exception.getMessage().contains("serializar"));
        assertSame(causa, exception.getCause());
        verifyNoInteractions(emitter);
    }
}
