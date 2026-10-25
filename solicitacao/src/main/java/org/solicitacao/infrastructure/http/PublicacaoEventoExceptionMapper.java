package org.solicitacao.infrastructure.http;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.solicitacao.dto.ErroResponse;
import org.solicitacao.exception.PublicacaoEventoException;

@Provider
public class PublicacaoEventoExceptionMapper implements ExceptionMapper<PublicacaoEventoException> {

    @Override
    public Response toResponse(PublicacaoEventoException exception) {
        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErroResponse("Não foi possível publicar o evento no Kafka. Tente novamente."))
                .build();
    }
}
