package org.solicitacao.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;
import org.solicitacao.dto.CartaoEmitidoEvent;
import org.solicitacao.service.SolicitacaoApplicationService;

@ApplicationScoped
public class CartaoEmitidoConsumer {

    private static final Logger LOG = Logger.getLogger(CartaoEmitidoConsumer.class);

    @Inject
    ObjectMapper objectMapper;

    @Inject
    SolicitacaoApplicationService solicitacaoApplicationService;

    @Incoming("cartao-emitido")
    public void consumir(String payload) {
        try {
            CartaoEmitidoEvent evento = objectMapper.readValue(payload, CartaoEmitidoEvent.class);

            solicitacaoApplicationService.atualizarStatusPorCartaoEmitido(evento);

        } catch (Exception e) {
            LOG.errorf(
                    e,
                    "Falha ao processar mensagem do tópico cartao.emitido: %s",
                    payload
            );

            throw new RuntimeException(
                    "Erro ao processar evento de cartão emitido",
                    e
            );
        }
    }
}
