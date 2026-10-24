package org.solicitacao.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;
import org.solicitacao.dto.SolicitacaoCriadaEvent;
import org.solicitacao.exception.PublicacaoEventoException;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@ApplicationScoped
public class KafkaSolicitacaoPublisher {

    private static final Logger LOG = Logger.getLogger(KafkaSolicitacaoPublisher.class);

    private static final long TIMEOUT_ENVIO_SEGUNDOS = 3;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    @Channel("solicitacao-criada")
    Emitter<String> emitter;

    @Retry(maxRetries = 4, delay = 1000, jitter = 200)
    @Timeout(5000)
    @CircuitBreaker(requestVolumeThreshold = 10, failureRatio = 0.5, delay = 10000)
    public void publicar(SolicitacaoCriadaEvent evento) {

        String payload = serializar(evento);

        try {

            emitter.send(payload).toCompletableFuture().get(TIMEOUT_ENVIO_SEGUNDOS, TimeUnit.SECONDS);

            String jsonFormatado = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(evento);

            LOG.infof("\n"+
                             "Evento da solicitação %s publicado no Kafka:\n%s", evento.solicitacaoId(), jsonFormatado);

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new PublicacaoEventoException("Envio do evento interrompido", e);

        } catch (ExecutionException | TimeoutException e) {

            throw new PublicacaoEventoException(
                    "Falha ao publicar o evento da solicitação " + evento.solicitacaoId(), e);

        }
        catch (JsonProcessingException e) {
            throw new PublicacaoEventoException(
                    "Erro ao serializar o evento da solicitação " + evento.solicitacaoId(), e);
        }
    }

    private String serializar(SolicitacaoCriadaEvent evento) {
        try {
            return objectMapper.writeValueAsString(evento);
        } catch (JsonProcessingException e) {
            throw new PublicacaoEventoException(
                    "Erro ao serializar o evento da solicitação " + evento.solicitacaoId(), e);
        }
    }
}
