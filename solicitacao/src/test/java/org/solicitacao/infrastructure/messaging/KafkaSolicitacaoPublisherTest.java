package org.solicitacao.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.solicitacao.dto.SolicitacaoCriadaEvent;
import org.solicitacao.exception.PublicacaoEventoException;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaSolicitacaoPublisherTest {

    @Mock
    ObjectMapper objectMapper;

    @Mock
    ObjectWriter objectWriter;

    @Mock
    Emitter<String> emitter;

    @Mock
    CompletionStage<Void> completionStage;

    @Mock
    CompletableFuture<Void> completableFuture;

    KafkaSolicitacaoPublisher publisher;

    private final SolicitacaoCriadaEvent evento = new SolicitacaoCriadaEvent(
            10L,
            "cliente",
            Instant.parse("2026-10-26T10:00:00Z")
    );

    @BeforeEach
    void setUp() {
        publisher = new KafkaSolicitacaoPublisher();
        publisher.objectMapper = objectMapper;
        publisher.emitter = emitter;
    }

    @Test
    void publicaEventoQuandoKafkaConfirmaEntrega() throws Exception {
        when(objectMapper.writeValueAsString(evento)).thenReturn("{\"solicitacaoId\":10}");
        when(emitter.send(anyString())).thenReturn(completionStage);
        when(completionStage.toCompletableFuture()).thenReturn(completableFuture);
        when(completableFuture.get(anyLong(), org.mockito.ArgumentMatchers.any(TimeUnit.class)))
                .thenReturn(null);
        when(objectMapper.writerWithDefaultPrettyPrinter()).thenReturn(objectWriter);
        when(objectWriter.writeValueAsString(evento)).thenReturn("{\n  \"solicitacaoId\": 10\n}");

        publisher.publicar(evento);

        verify(emitter).send("{\"solicitacaoId\":10}");
        verify(completableFuture).get(3L, TimeUnit.SECONDS);
    }

    @Test
    void lancaPublicacaoEventoExceptionQuandoFalhaAoSerializar() throws Exception {
        when(objectMapper.writeValueAsString(evento))
                .thenThrow(new JsonProcessingException("falha") {
                });

        PublicacaoEventoException exception = assertThrows(
                PublicacaoEventoException.class,
                () -> publisher.publicar(evento)
        );

        assertTrue(exception.getMessage().contains("Erro ao serializar"));
    }

    @Test
    void lancaPublicacaoEventoExceptionQuandoFalhaAoSerializarLogFormatado() throws Exception {
        when(objectMapper.writeValueAsString(evento)).thenReturn("{}");
        when(emitter.send(anyString())).thenReturn(completionStage);
        when(completionStage.toCompletableFuture()).thenReturn(completableFuture);
        when(completableFuture.get(anyLong(), org.mockito.ArgumentMatchers.any(TimeUnit.class)))
                .thenReturn(null);
        when(objectMapper.writerWithDefaultPrettyPrinter()).thenReturn(objectWriter);
        when(objectWriter.writeValueAsString(evento))
                .thenThrow(new JsonProcessingException("falha") {
                });

        assertThrows(PublicacaoEventoException.class, () -> publisher.publicar(evento));
    }

    @Test
    void lancaPublicacaoEventoExceptionQuandoEnvioFalha() throws Exception {
        prepararEnvio();
        when(completableFuture.get(anyLong(), org.mockito.ArgumentMatchers.any(TimeUnit.class)))
                .thenThrow(new ExecutionException(new RuntimeException("broker fora")));

        PublicacaoEventoException exception = assertThrows(
                PublicacaoEventoException.class,
                () -> publisher.publicar(evento)
        );

        assertTrue(exception.getMessage().contains("Falha ao publicar o evento"));
    }

    @Test
    void lancaPublicacaoEventoExceptionQuandoEnvioExpira() throws Exception {
        prepararEnvio();
        when(completableFuture.get(anyLong(), org.mockito.ArgumentMatchers.any(TimeUnit.class)))
                .thenThrow(new TimeoutException("timeout"));

        PublicacaoEventoException exception = assertThrows(
                PublicacaoEventoException.class,
                () -> publisher.publicar(evento)
        );

        assertTrue(exception.getMessage().contains("Falha ao publicar o evento"));
    }

    @Test
    void lancaPublicacaoEventoExceptionERepoeInterruptQuandoThreadInterrompida() throws Exception {
        prepararEnvio();
        when(completableFuture.get(anyLong(), org.mockito.ArgumentMatchers.any(TimeUnit.class)))
                .thenThrow(new InterruptedException("interrompido"));

        try {
            PublicacaoEventoException exception = assertThrows(
                    PublicacaoEventoException.class,
                    () -> publisher.publicar(evento)
            );

            assertTrue(exception.getMessage().contains("interrompido"));
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }

    private void prepararEnvio() throws Exception {
        when(objectMapper.writeValueAsString(evento)).thenReturn("{}");
        when(emitter.send(anyString())).thenReturn(completionStage);
        when(completionStage.toCompletableFuture()).thenReturn(completableFuture);
    }
}
