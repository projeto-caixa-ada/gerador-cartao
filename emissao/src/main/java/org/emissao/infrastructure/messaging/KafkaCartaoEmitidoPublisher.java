
package org.emissao.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.emissao.dto.CartaoEmitidoEvent;
import org.jboss.logging.Logger;

@ApplicationScoped
public class KafkaCartaoEmitidoPublisher {

    private static final Logger LOG =
            Logger.getLogger(KafkaCartaoEmitidoPublisher.class);

    @Inject
    ObjectMapper objectMapper;

    @Channel("cartao-emitido")
    Emitter<String> emitter;

    public void publicar(CartaoEmitidoEvent evento) {
        try {
            String mensagem = objectMapper.writeValueAsString(evento);

            emitter.send(mensagem);

            String jsonFormatado = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(evento);

            LOG.infof(
                      "\n"+
                             "Evento CARTAO_EMITIDO enviado para publicação no Kafka"
                            + "\nCanal: cartao-emitido"
                            + "\nEvento:\n%s",
                    jsonFormatado
            );

        } catch (JsonProcessingException e) {
            LOG.error("Erro ao serializar evento de cartão emitido", e);

            throw new IllegalStateException(
                    "Erro ao serializar evento de cartão emitido", e
            );
        }
    }
}