package org.emissao.infrastructure.http;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.emissao.dto.ErroResponse;
import org.emissao.exception.RecursoNaoEncontradoException;

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
