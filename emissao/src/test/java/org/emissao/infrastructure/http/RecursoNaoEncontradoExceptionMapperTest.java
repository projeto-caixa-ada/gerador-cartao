package org.emissao.infrastructure.http;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.emissao.dto.ErroResponse;
import org.emissao.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class RecursoNaoEncontradoExceptionMapperTest {

    @Test
    void retornaNotFoundComMensagemDaExcecao() {
        Response response = new RecursoNaoEncontradoExceptionMapper()
                .toResponse(new RecursoNaoEncontradoException("Cartão 404 não encontrado"));

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
        ErroResponse erro = assertInstanceOf(ErroResponse.class, response.getEntity());
        assertEquals("Cartão 404 não encontrado", erro.erro());
    }
}
