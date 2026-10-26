package org.emissao.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.emissao.dto.SolicitacaoValidadaMessage;
import org.emissao.service.CartaoService;
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
class SolicitacaoValidadaConsumerTest {

    @Mock
    ObjectMapper objectMapper;

    @Mock
    CartaoService cartaoService;

    SolicitacaoValidadaConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new SolicitacaoValidadaConsumer();
        consumer.objectMapper = objectMapper;
        consumer.cartaoApplicationService = cartaoService;
    }

    @Test
    void consomeMensagemEProcessaSolicitacao() throws Exception {
        String payload = "{\"solicitacaoId\":101}";
        SolicitacaoValidadaMessage mensagem = new SolicitacaoValidadaMessage(
                101L,
                "cliente",
                Instant.parse("2026-10-26T10:00:00Z")
        );
        when(objectMapper.readValue(payload, SolicitacaoValidadaMessage.class))
                .thenReturn(mensagem);

        consumer.consumir(payload);

        verify(cartaoService).processarSolicitacaoValidada(mensagem);
    }

    @Test
    void relancaRuntimeExceptionQuandoPayloadInvalido() throws Exception {
        String payload = "payload-invalido";
        JsonProcessingException causa = new JsonProcessingException("json inválido") {
        };
        when(objectMapper.readValue(payload, SolicitacaoValidadaMessage.class)).thenThrow(causa);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> consumer.consumir(payload)
        );

        assertTrue(exception.getMessage().contains("solicitação validada"));
        assertSame(causa, exception.getCause());
        verifyNoInteractions(cartaoService);
    }
}
