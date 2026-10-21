package org.emissao.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.emissao.dto.CartaoEmitidoEvent;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@ApplicationScoped
public class KafkaCartaoEmitidoPublisher {

    @Inject
    ObjectMapper objectMapper;

    @Channel("cartao-emitido")
    Emitter<String> emitter;

    public void publicar(CartaoEmitidoEvent evento) {
        try {
            emitter.send(objectMapper.writeValueAsString(evento));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Erro ao serializar evento de cartão emitido", e);
        }
    }
}
