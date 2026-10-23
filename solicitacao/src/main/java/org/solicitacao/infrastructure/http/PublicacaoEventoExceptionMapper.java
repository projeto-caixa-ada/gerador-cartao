package org.solicitacao.infrastructure.http;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.solicitacao.exception.PublicacaoEventoException;

import java.util.Map;

@Provider
public class PublicacaoEventoExceptionMapper implements ExceptionMapper<PublicacaoEventoException> {

    @Override
    public Response toResponse(PublicacaoEventoException exception) {
        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("erro", "Não foi possível publicar o evento no Kafka. Tente novamente."))
                .build();
    }
}
