package org.emissao.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.emissao.dto.SolicitacaoValidadaMessage;
import org.emissao.entity.Cartao;
import org.emissao.entity.Emissao;
import org.emissao.service.CartaoApplicationService;

@Path("/api/emissoes")
public class CartaoResource {

    @Inject
    CartaoApplicationService cartaoApplicationService;

    @POST
    @Path("/cartoes/processar")
    public Emissao processarMensagem(SolicitacaoValidadaMessage mensagem) {
        return cartaoApplicationService.processarSolicitacaoValidada(mensagem);
    }

    @GET
    @Path("/cartoes/{id}")
    public Cartao buscarCartao(@PathParam("id") Long id) {
        return cartaoApplicationService.buscarCartao(id);
    }

    @GET
    @Path("/{id}")
    public Emissao buscarEmissao(@PathParam("id") Long id) {
        return cartaoApplicationService.buscarEmissao(id);
    }
}
