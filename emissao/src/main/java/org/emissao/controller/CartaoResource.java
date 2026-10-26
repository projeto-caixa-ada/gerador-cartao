package org.emissao.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.emissao.dto.CartaoEmitidoEvent;
import org.emissao.dto.ErroResponse;
import org.emissao.service.CartaoService;

@Tag(
        name = "Emissões",
        description = "Consultas de cartões emitidos e gerenciamento do cache de leitura"
)
@Path("/api/emissoes")
@Produces(MediaType.APPLICATION_JSON)
public class CartaoResource {

    @Inject
    CartaoService cartaoService;

    @GET
    @Path("/cartoes/{cartaoId}")
    @Operation(
            operationId = "buscarCartaoPorId",
            summary = "Buscar cartão por ID",
            description = """
                    Retorna os dados do cartão identificado pelo ID.

                    A consulta usa cache-aside: primeiro verifica a chave
                    `cartao:{cartaoId}` no Redis. Em caso de cache miss, consulta
                    o PostgreSQL e grava o resultado no cache com TTL de 20 segundos.
                    """
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Cartão encontrado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = CartaoEmitidoEvent.class)
                    )
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Cartão não encontrado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            )
    })
    public CartaoEmitidoEvent buscarCartaoPorId(
            @Parameter(
                    description = "ID do cartão",
                    required = true,
                    example = "1"
            )
            @PathParam("cartaoId") Long cartaoId) {
        return cartaoService.buscarPorId(cartaoId);
    }

    @GET
    @Path("/cartoes/solicitacao/{solicitacaoId}")
    @Operation(
            operationId = "buscarCartaoPorSolicitacaoId",
            summary = "Buscar cartão pela solicitação",
            description = """
                    Retorna o cartão emitido para a solicitação informada.

                    A consulta usa cache-aside: primeiro verifica a chave
                    `cartao:solicitacao:{solicitacaoId}` no Redis. Em caso de
                    cache miss, consulta o PostgreSQL e grava o resultado no
                    cache com TTL de 20 segundos.
                    """
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Cartão encontrado para a solicitação",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = CartaoEmitidoEvent.class)
                    )
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Nenhum cartão encontrado para a solicitação",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            )
    })
    public CartaoEmitidoEvent buscarCartaoPorSolicitacaoId(
            @Parameter(
                    description = "ID da solicitação que originou o cartão",
                    required = true,
                    example = "1"
            )
            @PathParam("solicitacaoId") String solicitacaoId) {
        return cartaoService.buscarPorSolicitacaoId(solicitacaoId);
    }

    @PATCH
    @Path("/cartoes/solicitacao/{cartaoId}/cache")
    @Operation(
            operationId = "invalidarCachePorCartaoId",
            summary = "Invalidar o cache de um cartão",
            description = """
                    Remove do Redis a chave `cartao:{cartaoId}` para forçar a
                    próxima consulta a buscar os dados atualizados no PostgreSQL.

                    A operação é idempotente: retorna `204 No Content` mesmo
                    quando a chave não existe no cache.
                    """
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "204",
                    description = "Cache invalidado com sucesso"
            )
    })
    public Response invalidarCachePorCartaoId(
            @Parameter(
                    description = "ID do cartão cujo cache será invalidado",
                    required = true,
                    example = "1"
            )
            @PathParam("cartaoId") Long cartaoId) {
        cartaoService.invalidar(cartaoId);
        return Response.noContent().build();
    }
}
