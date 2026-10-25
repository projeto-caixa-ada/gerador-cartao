package org.solicitacao.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.solicitacao.dto.CriarSolicitacaoRequest;
import org.solicitacao.dto.ErroResponse;
import org.solicitacao.dto.SolicitacaoResponse;
import org.solicitacao.entity.Solicitacao;
import org.solicitacao.exception.ValidacaoException;
import org.solicitacao.service.SolicitacaoApplicationService;

import java.net.URI;

@Tag(
        name = "Solicitações",
        description = "Operações de criação e consulta de solicitações de cartão"
)
@Path("/api/solicitacoes")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SolicitacaoResource {

    @Inject
    SolicitacaoApplicationService solicitacaoApplicationService;

    @POST
    @Operation(
            operationId = "criarSolicitacao",
            summary = "Criar uma solicitação de cartão",
            description = """
                    Registra uma nova solicitação para o cliente informado.

                    A solicitação é criada com o status `ABERTA` e o evento
                    `cartao.solicitado` é publicado no Kafka para que o serviço
                    de emissão gere o cartão.
                    """
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "201",
                    description = "Solicitação criada com sucesso",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = SolicitacaoResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Dados da solicitação inválidos",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "503",
                    description = "Falha ao publicar o evento no Kafka",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            )
    })
    public Response criar(
            @RequestBody(
                    description = "Dados do cliente que está solicitando o cartão",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = CriarSolicitacaoRequest.class)
                    )
            )
            CriarSolicitacaoRequest request) {
        if (request == null) {
            throw new ValidacaoException("Corpo da requisição é obrigatório");
        }

        Solicitacao solicitacao = solicitacaoApplicationService.criarSolicitacao(request.clienteId());

        return Response.created(URI.create("/api/solicitacoes/" + solicitacao.getId()))
                .entity(SolicitacaoResponse.from(solicitacao))
                .build();
    }

    @GET
    @Path("/{id}")
    @Operation(
            operationId = "buscarSolicitacaoPorId",
            summary = "Buscar solicitação por ID",
            description = """
                    Retorna os dados atuais da solicitação identificada pelo ID.

                    O status pode ser `ABERTA`, `VALIDADA`, `EMITIDA` ou `CANCELADA`.
                    Quando o evento `cartao.emitido` é processado, uma solicitação
                    que estava `ABERTA` passa para `EMITIDA`.
                    """
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Solicitação encontrada",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = SolicitacaoResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "ID da solicitação inválido",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Solicitação não encontrada",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            )
    })
    public SolicitacaoResponse buscarPorId(
            @Parameter(
                    description = "ID da solicitação",
                    required = true,
                    example = "1"
            )
            @PathParam("id") Long id) {
        return SolicitacaoResponse.from(solicitacaoApplicationService.buscarPorId(id));
    }
}
