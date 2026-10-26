package org.solicitacao.infrastructure.http;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.faulttolerance.exceptions.FaultToleranceException;
import org.junit.jupiter.api.Test;
import org.solicitacao.dto.ErroResponse;
import org.solicitacao.exception.PublicacaoEventoException;
import org.solicitacao.exception.RecursoNaoEncontradoException;
import org.solicitacao.exception.ValidacaoException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ExceptionMappersTest {

    @Test
    void validacaoRetornaBadRequest() {
        Response response = new ValidacaoExceptionMapper()
                .toResponse(new ValidacaoException("clienteId é obrigatório"));

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
        ErroResponse erro = assertInstanceOf(ErroResponse.class, response.getEntity());
        assertEquals("clienteId é obrigatório", erro.erro());
    }

    @Test
    void recursoNaoEncontradoRetornaNotFound() {
        Response response = new RecursoNaoEncontradoExceptionMapper()
                .toResponse(new RecursoNaoEncontradoException("Solicitação 1 não encontrada"));

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
        ErroResponse erro = assertInstanceOf(ErroResponse.class, response.getEntity());
        assertEquals("Solicitação 1 não encontrada", erro.erro());
    }

    @Test
    void falhaDePublicacaoRetornaServiceUnavailable() {
        Response response = new PublicacaoEventoExceptionMapper()
                .toResponse(new PublicacaoEventoException("falha", new RuntimeException()));

        assertEquals(
                Response.Status.SERVICE_UNAVAILABLE.getStatusCode(),
                response.getStatus()
        );
        ErroResponse erro = assertInstanceOf(ErroResponse.class, response.getEntity());
        assertEquals(
                "Não foi possível publicar o evento no Kafka. Tente novamente.",
                erro.erro()
        );
    }

    @Test
    void falhaDeFaultToleranceRetornaServiceUnavailable() {
        Response response = new FaultToleranceExceptionMapper()
                .toResponse(new FaultToleranceException("timeout"));

        assertEquals(
                Response.Status.SERVICE_UNAVAILABLE.getStatusCode(),
                response.getStatus()
        );
        assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
        ErroResponse erro = assertInstanceOf(ErroResponse.class, response.getEntity());
        assertEquals(
                "Não foi possível publicar o evento no Kafka. Tente novamente.",
                erro.erro()
        );
    }
}
