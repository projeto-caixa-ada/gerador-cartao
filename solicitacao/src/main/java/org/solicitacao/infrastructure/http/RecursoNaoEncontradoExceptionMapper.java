package org.solicitacao.infrastructure.http;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.solicitacao.dto.ErroResponse;
import org.solicitacao.exception.RecursoNaoEncontradoException;

@Provider
public class RecursoNaoEncontradoExceptionMapper
        implements ExceptionMapper<RecursoNaoEncontradoException> {

    @Override
    public Response toResponse(RecursoNaoEncontradoException exception) {
        return Response.status(Response.Status.NOT_FOUND)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErroResponse(exception.getMessage()))
                .build();
    }
}
