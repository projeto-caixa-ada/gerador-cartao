package org.emissao.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import org.emissao.dto.CartaoEmitidoEvent;
import org.emissao.service.CartaoService;

@Path("/api/emissoes")
public class CartaoResource {

    @Inject
    CartaoService cartaoService;

    @GET
    @Path("/cartoes/{cartaoId}")
    public CartaoEmitidoEvent buscarCartaoPorId(@PathParam("cartaoId") Long cartaoId) {
        return cartaoService.buscarPorId(cartaoId);
    }

    @GET
    @Path("/cartoes/solicitacao/{solicitacaoId}")
    public CartaoEmitidoEvent buscarCartaoPorSolicitacaoId(
            @PathParam("solicitacaoId") String solicitacaoId) {
        return cartaoService.buscarPorSolicitacaoId(solicitacaoId);
    }

    @PATCH
    @Path("/cartoes/solicitacao/{cartaoId}/cache")
    public Response invalidarCachePorCartaoId(@PathParam("cartaoId") Long cartaoId) {
        cartaoService.invalidar(cartaoId);
        return Response.noContent().build();
    }

}
