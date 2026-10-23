package org.solicitacao.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.solicitacao.dto.CriarSolicitacaoRequest;
import org.solicitacao.dto.SolicitacaoResponse;
import org.solicitacao.entity.Solicitacao;
import org.solicitacao.exception.ValidacaoException;
import org.solicitacao.service.SolicitacaoApplicationService;

import java.net.URI;

@Path("/api/solicitacoes")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SolicitacaoResource {

    @Inject
    SolicitacaoApplicationService solicitacaoApplicationService;

    @POST
    public Response criar(CriarSolicitacaoRequest request) {
        if (request == null) {
            throw new ValidacaoException("Corpo da requisição é obrigatório");
        }

        Solicitacao solicitacao = solicitacaoApplicationService.criarSolicitacao(request.clienteId());

        return Response.created(URI.create("/api/solicitacoes/" + solicitacao.getId()))
                .entity(SolicitacaoResponse.from(solicitacao))
                .build();
    }
}
