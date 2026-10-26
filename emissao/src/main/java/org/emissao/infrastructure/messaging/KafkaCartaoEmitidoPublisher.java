
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

            CartaoEmitidoEvent eventoParaLog = new CartaoEmitidoEvent(
                    evento.solicitacaoId(),
                    evento.clienteId(),
                    evento.cartaoId(),
                    mascararNumero(evento.numeroCartao()),
                    evento.status(),
                    evento.dataSolicitacao(),
                    evento.ocorridoEm()
            );
            String jsonFormatado = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(eventoParaLog);

            LOG.infof(
                    "Evento publicado no canal Kafka cartao-emitido:\n%s",
                    jsonFormatado
            );

        } catch (JsonProcessingException e) {
            LOG.error("Erro ao serializar evento de cartão emitido", e);

            throw new IllegalStateException(
                    "Erro ao serializar evento de cartão emitido", e
            );
        }
    }

    private static String mascararNumero(String numero) {
        if (numero == null || numero.length() <= 4) {
            return "****";
        }

        return "*".repeat(numero.length() - 4) + numero.substring(numero.length() - 4);
    }
}