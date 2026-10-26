package org.solicitacao.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.solicitacao.dto.CartaoEmitidoEvent;
import org.solicitacao.service.SolicitacaoApplicationService;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartaoEmitidoConsumerTest {

    @Mock
    ObjectMapper objectMapper;

    @Mock
    SolicitacaoApplicationService solicitacaoApplicationService;

    CartaoEmitidoConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new CartaoEmitidoConsumer();
        consumer.objectMapper = objectMapper;
        consumer.solicitacaoApplicationService = solicitacaoApplicationService;
    }

    @Test
    void consomeEventoEAtualizaSolicitacao() throws Exception {
        String payload = "{\"solicitacaoId\":\"42\"}";
        CartaoEmitidoEvent evento = new CartaoEmitidoEvent(
                "42",
                "cliente",
                "5001",
                "1000000000000000",
                "EMITIDO",
                Instant.parse("2026-10-26T10:00:00Z"),
                Instant.parse("2026-10-26T10:00:05Z")
        );
        when(objectMapper.readValue(payload, CartaoEmitidoEvent.class)).thenReturn(evento);

        consumer.consumir(payload);

        verify(solicitacaoApplicationService).atualizarStatusPorCartaoEmitido(evento);
    }

    @Test
    void relancaRuntimeExceptionQuandoPayloadInvalido() throws Exception {
        String payload = "payload-invalido";
        JsonProcessingException causa = new JsonProcessingException("json inválido") {
        };
        when(objectMapper.readValue(payload, CartaoEmitidoEvent.class)).thenThrow(causa);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> consumer.consumir(payload)
        );

        assertTrue(exception.getMessage().contains("cartão emitido"));
        assertSame(causa, exception.getCause());
        verifyNoInteractions(solicitacaoApplicationService);
    }
}
