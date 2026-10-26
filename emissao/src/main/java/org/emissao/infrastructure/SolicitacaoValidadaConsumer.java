package org.emissao.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.emissao.dto.SolicitacaoValidadaMessage;
import org.emissao.service.CartaoService;
import org.jboss.logging.Logger;

@ApplicationScoped
public class SolicitacaoValidadaConsumer {

    private static final Logger LOG =
            Logger.getLogger(SolicitacaoValidadaConsumer.class);

    @Inject
    ObjectMapper objectMapper;

    @Inject
    CartaoService cartaoApplicationService;

    @Incoming("cartao-solicitado")
    public void consumir(String payload) {
        try {
            SolicitacaoValidadaMessage mensagem =
                    objectMapper.readValue(
                            payload,
                            SolicitacaoValidadaMessage.class
                    );

            LOG.infof("Evento consumido do canal cartao-solicitado: %s", mensagem);
            cartaoApplicationService.processarSolicitacaoValidada(mensagem);

        } catch (Exception e) {
            LOG.errorf(
                    "Falha ao processar mensagem Kafka: %s",
                    e.getMessage()
            );

            throw new RuntimeException(
                    "Erro ao processar mensagem de solicitação validada",
                    e
            );
        }
    }

}
