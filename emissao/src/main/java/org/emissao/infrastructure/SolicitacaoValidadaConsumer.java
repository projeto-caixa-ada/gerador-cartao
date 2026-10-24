package org.emissao.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.emissao.dto.SolicitacaoValidadaMessage;
import org.emissao.service.CartaoApplicationService;

import static io.quarkus.arc.ComponentsProvider.LOG;

@ApplicationScoped
public class SolicitacaoValidadaConsumer {

    @Inject
    ObjectMapper objectMapper;

    @Inject
    CartaoApplicationService cartaoApplicationService;

    @Incoming("cartao-solicitado")
    public void consumir(String payload) {
        try {
            SolicitacaoValidadaMessage mensagem =
                    objectMapper.readValue(
                            payload,
                            SolicitacaoValidadaMessage.class
                    );

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
