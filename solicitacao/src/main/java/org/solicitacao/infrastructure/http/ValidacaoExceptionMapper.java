package org.solicitacao.infrastructure.http;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.solicitacao.dto.ErroResponse;
import org.solicitacao.exception.ValidacaoException;

@Provider
public class ValidacaoExceptionMapper implements ExceptionMapper<ValidacaoException> {

    @Override
    public Response toResponse(ValidacaoException exception) {
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErroResponse(exception.getMessage()))
                .build();
    }
}
