package org.solicitacao.infrastructure.http;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.faulttolerance.exceptions.FaultToleranceException;
import org.solicitacao.dto.ErroResponse;

@Provider
public class FaultToleranceExceptionMapper implements ExceptionMapper<FaultToleranceException> {

    @Override
    public Response toResponse(FaultToleranceException exception) {
        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErroResponse("Não foi possível publicar o evento no Kafka. Tente novamente."))
                .build();
    }
}
