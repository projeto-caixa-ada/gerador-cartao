package org.emissao.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import org.emissao.dto.CartaoEmitidoEvent;
import org.emissao.dto.SolicitacaoValidadaMessage;
import org.emissao.entity.Cartao;
import org.emissao.entity.Emissao;
import org.emissao.enums.StatusCartao;
import org.emissao.enums.StatusEmissao;
import org.emissao.exception.RecursoNaoEncontradoException;
import org.emissao.infrastructure.CartaoNumberGenerator;
import org.emissao.infrastructure.messaging.KafkaCartaoEmitidoPublisher;
import org.emissao.mapper.CartaoMapper;
import org.emissao.repository.CartaoRepository;
import org.emissao.repository.EmissaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartaoServiceTest {

    private static final Instant DATA_SOLICITACAO = Instant.parse("2026-10-26T10:00:00Z");

    @Mock
    CartaoRepository cartaoRepository;

    @Mock
    EmissaoRepository emissaoRepository;

    @Mock
    CartaoNumberGenerator cartaoNumberGenerator;

    @Mock
    KafkaCartaoEmitidoPublisher kafkaCartaoEmitidoPublisher;

    @Mock
    RedisDataSource redis;

    @Mock
    ValueCommands<String, String> valueCommands;

    @Mock
    KeyCommands<String> keyCommands;

    @Mock
    ObjectMapper objectMapper;

    CartaoService service;

    @BeforeEach
    void setUp() {
        service = new CartaoService();
        service.cartaoRepository = cartaoRepository;
        service.emissaoRepository = emissaoRepository;
        service.cartaoNumberGenerator = cartaoNumberGenerator;
        service.kafkaCartaoEmitidoPublisher = kafkaCartaoEmitidoPublisher;
        service.redis = redis;
        service.objectMapper = objectMapper;
        service.cartaoMapper = new CartaoMapper();
    }

    @Test
    void processarSolicitacaoValidadaCriaCartaoEmissaoEPublicaEvento() {
        prepararEmissaoNova();

        Emissao emissao = service.processarSolicitacaoValidada(mensagem(101L, DATA_SOLICITACAO));

        assertEquals("101", emissao.getSolicitacaoId());
        assertEquals("cliente", emissao.getClienteId());
        assertEquals(DATA_SOLICITACAO, emissao.getDataSolicitacao());
        assertEquals(StatusEmissao.EMITIDA, emissao.getStatus());
        assertNotNull(emissao.getCartao());
        assertEquals("5001", String.valueOf(emissao.getCartao().getId()));
        assertEquals(StatusCartao.EMITIDO, emissao.getCartao().getStatus());

        verify(cartaoRepository).persist(any(Cartao.class));
        verify(emissaoRepository).persist(emissao);

        ArgumentCaptor<CartaoEmitidoEvent> captor =
                ArgumentCaptor.forClass(CartaoEmitidoEvent.class);
        verify(kafkaCartaoEmitidoPublisher).publicar(captor.capture());

        CartaoEmitidoEvent evento = captor.getValue();
        assertEquals("101", evento.solicitacaoId());
        assertEquals("cliente", evento.clienteId());
        assertEquals("5001", evento.cartaoId());
        assertEquals("1000000000000000", evento.numeroCartao());
        assertEquals("EMITIDO", evento.status());
        assertEquals(DATA_SOLICITACAO, evento.dataSolicitacao());
        assertNotNull(evento.ocorridoEm());
    }

    @Test
    void processarSolicitacaoValidadaUsaInstantAtualQuandoDataAusente() {
        prepararEmissaoNova();
        Instant antes = Instant.now();

        Emissao emissao = service.processarSolicitacaoValidada(mensagem(101L, null));

        Instant depois = Instant.now();
        assertNotNull(emissao.getDataSolicitacao());
        assertFalse(emissao.getDataSolicitacao().isBefore(antes));
        assertFalse(emissao.getDataSolicitacao().isAfter(depois));
    }

    @Test
    void processarSolicitacaoValidadaIgnoraEventoDuplicado() {
        Cartao cartao = cartao(5001L, "101");
        Emissao emissao = new Emissao();
        emissao.setSolicitacaoId("101");
        when(cartaoRepository.buscarPorSolicitacaoId("101")).thenReturn(Optional.of(cartao));
        when(emissaoRepository.buscarEmissaoPorSolicitacaoId("101"))
                .thenReturn(Optional.of(emissao));

        Emissao resultado = service.processarSolicitacaoValidada(mensagem(101L, DATA_SOLICITACAO));

        assertSame(emissao, resultado);
        verify(cartaoNumberGenerator, never()).gerarNumero();
        verify(cartaoRepository, never()).persist(any(Cartao.class));
        verify(kafkaCartaoEmitidoPublisher, never()).publicar(any(CartaoEmitidoEvent.class));
    }

    @Test
    void processarSolicitacaoValidadaFalhaQuandoCartaoExisteSemEmissao() {
        when(cartaoRepository.buscarPorSolicitacaoId("101"))
                .thenReturn(Optional.of(cartao(5001L, "101")));
        when(emissaoRepository.buscarEmissaoPorSolicitacaoId("101"))
                .thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.processarSolicitacaoValidada(mensagem(101L, DATA_SOLICITACAO))
        );

        assertTrue(exception.getMessage().contains("101"));
    }

    @Test
    void processarSolicitacaoValidadaRejeitaMensagemInvalida() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.processarSolicitacaoValidada(null)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> service.processarSolicitacaoValidada(
                        new SolicitacaoValidadaMessage(null, "cliente", DATA_SOLICITACAO))
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> service.processarSolicitacaoValidada(
                        new SolicitacaoValidadaMessage(101L, null, DATA_SOLICITACAO))
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> service.processarSolicitacaoValidada(
                        new SolicitacaoValidadaMessage(101L, "  ", DATA_SOLICITACAO))
        );
        verifyNoInteractions(cartaoRepository);
    }

    @Test
    void buscarPorSolicitacaoIdRetornaDoCacheQuandoExiste() throws Exception {
        CartaoEmitidoEvent esperado = evento("101");
        prepararRedisValue();
        when(valueCommands.get("cartao:solicitacao:101")).thenReturn("{\"json\":true}");
        when(objectMapper.readValue("{\"json\":true}", CartaoEmitidoEvent.class))
                .thenReturn(esperado);

        CartaoEmitidoEvent resultado = service.buscarPorSolicitacaoId("101");

        assertSame(esperado, resultado);
        verifyNoInteractions(cartaoRepository);
    }

    @Test
    void buscarPorSolicitacaoIdConsultaBancoEGravaNoCacheNoMiss() throws Exception {
        prepararRedisValue();
        when(valueCommands.get("cartao:solicitacao:101")).thenReturn(null);
        when(cartaoRepository.buscarPorSolicitacaoId("101"))
                .thenReturn(Optional.of(cartao(5001L, "101")));
        when(objectMapper.writeValueAsString(any(CartaoEmitidoEvent.class)))
                .thenReturn("{\"cartaoId\":\"5001\"}");

        CartaoEmitidoEvent resultado = service.buscarPorSolicitacaoId("101");

        assertEquals("101", resultado.solicitacaoId());
        assertEquals("5001", resultado.cartaoId());
        assertEquals("cliente", resultado.clienteId());
        assertEquals("EMITIDO", resultado.status());
        verify(valueCommands).setex(
                "cartao:solicitacao:101",
                20L,
                "{\"cartaoId\":\"5001\"}"
        );
    }

    @Test
    void buscarPorSolicitacaoIdSegueParaOBancoQuandoRedisFalhaNaLeitura() throws Exception {
        prepararRedisValue();
        when(valueCommands.get("cartao:solicitacao:101"))
                .thenThrow(new IllegalStateException("redis indisponível"));
        when(cartaoRepository.buscarPorSolicitacaoId("101"))
                .thenReturn(Optional.of(cartao(5001L, "101")));
        when(objectMapper.writeValueAsString(any(CartaoEmitidoEvent.class))).thenReturn("{}");

        CartaoEmitidoEvent resultado = service.buscarPorSolicitacaoId("101");

        assertEquals("5001", resultado.cartaoId());
        verify(valueCommands).setex("cartao:solicitacao:101", 20L, "{}");
    }

    @Test
    void buscarPorSolicitacaoIdLancaRecursoNaoEncontrado() throws Exception {
        prepararRedisValue();
        when(valueCommands.get("cartao:solicitacao:404")).thenReturn(null);
        when(cartaoRepository.buscarPorSolicitacaoId("404")).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> service.buscarPorSolicitacaoId("404")
        );

        assertTrue(exception.getMessage().contains("404"));
    }

    @Test
    void buscarPorSolicitacaoIdRetornaMesmoSeFalharAoGravarNoCache() throws Exception {
        prepararRedisValue();
        when(valueCommands.get("cartao:solicitacao:101")).thenReturn(null);
        when(cartaoRepository.buscarPorSolicitacaoId("101"))
                .thenReturn(Optional.of(cartao(5001L, "101")));
        when(objectMapper.writeValueAsString(any(CartaoEmitidoEvent.class)))
                .thenThrow(new JsonProcessingException("falha") {
                });

        CartaoEmitidoEvent resultado = service.buscarPorSolicitacaoId("101");

        assertEquals("5001", resultado.cartaoId());
    }

    @Test
    void buscarPorIdRetornaDoCacheQuandoExiste() throws Exception {
        CartaoEmitidoEvent esperado = evento("101");
        prepararRedisValue();
        when(valueCommands.get("cartao:5001")).thenReturn("{\"json\":true}");
        when(objectMapper.readValue("{\"json\":true}", CartaoEmitidoEvent.class))
                .thenReturn(esperado);

        assertSame(esperado, service.buscarPorId(5001L));
        verifyNoInteractions(cartaoRepository);
    }

    @Test
    void buscarPorIdConsultaBancoEGravaNoCacheNoMiss() throws Exception {
        prepararRedisValue();
        when(valueCommands.get("cartao:5001")).thenReturn(null);
        when(cartaoRepository.findById(5001L)).thenReturn(cartao(5001L, "101"));
        when(objectMapper.writeValueAsString(any(CartaoEmitidoEvent.class))).thenReturn("{}");

        CartaoEmitidoEvent resultado = service.buscarPorId(5001L);

        assertEquals("5001", resultado.cartaoId());
        verify(valueCommands).setex("cartao:5001", 20L, "{}");
    }

    @Test
    void buscarPorIdSegueParaOBancoQuandoRedisFalhaNaLeitura() throws Exception {
        prepararRedisValue();
        when(valueCommands.get("cartao:5001"))
                .thenThrow(new IllegalStateException("redis indisponível"));
        when(cartaoRepository.findById(5001L)).thenReturn(cartao(5001L, "101"));
        when(objectMapper.writeValueAsString(any(CartaoEmitidoEvent.class))).thenReturn("{}");

        assertEquals("5001", service.buscarPorId(5001L).cartaoId());
    }

    @Test
    void buscarPorIdLancaRecursoNaoEncontrado() throws Exception {
        prepararRedisValue();
        when(valueCommands.get("cartao:404")).thenReturn(null);
        when(cartaoRepository.findById(404L)).thenReturn(null);

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> service.buscarPorId(404L)
        );

        assertTrue(exception.getMessage().contains("404"));
    }

    @Test
    void buscarPorIdRetornaMesmoSeFalharAoGravarNoCache() throws Exception {
        prepararRedisValue();
        when(valueCommands.get("cartao:5001")).thenReturn(null);
        when(cartaoRepository.findById(5001L)).thenReturn(cartao(5001L, "101"));
        when(objectMapper.writeValueAsString(any(CartaoEmitidoEvent.class)))
                .thenThrow(new JsonProcessingException("falha") {
                });

        assertEquals("5001", service.buscarPorId(5001L).cartaoId());
    }

    @Test
    void atualizarStatusAlteraCartaoEInvalidaCache() {
        Cartao cartao = cartao(5001L, "101");
        when(cartaoRepository.findById(5001L)).thenReturn(cartao);
        when(redis.key()).thenReturn(keyCommands);

        service.atualizarStatus(5001L, StatusCartao.BLOQUEADO);

        assertEquals(StatusCartao.BLOQUEADO, cartao.getStatus());
        verify(keyCommands).del("cartao:5001");
    }

    @Test
    void atualizarStatusRejeitaCartaoInexistente() {
        when(cartaoRepository.findById(404L)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.atualizarStatus(404L, StatusCartao.BLOQUEADO)
        );

        assertEquals("Cartão não encontrado", exception.getMessage());
    }

    @Test
    void invalidarRemoveChaveDoCache() {
        when(redis.key()).thenReturn(keyCommands);

        service.invalidar(5001L);

        verify(keyCommands).del("cartao:5001");
    }

    @Test
    void invalidarNaoPropagaFalhaDoRedis() {
        when(redis.key()).thenReturn(keyCommands);
        doThrow(new IllegalStateException("redis indisponível"))
                .when(keyCommands).del(anyString());

        assertDoesNotThrow(() -> service.invalidar(5001L));
    }

    private void prepararEmissaoNova() {
        when(cartaoRepository.buscarPorSolicitacaoId("101")).thenReturn(Optional.empty());
        when(cartaoNumberGenerator.gerarNumero()).thenReturn("1000000000000000");
        doAnswer(invocation -> {
            Cartao cartao = invocation.getArgument(0);
            cartao.setId(5001L);
            return null;
        }).when(cartaoRepository).persist(any(Cartao.class));
    }

    private void prepararRedisValue() {
        when(redis.value(String.class)).thenReturn(valueCommands);
    }

    private SolicitacaoValidadaMessage mensagem(Long solicitacaoId, Instant dataSolicitacao) {
        return new SolicitacaoValidadaMessage(solicitacaoId, "cliente", dataSolicitacao);
    }

    private Cartao cartao(Long id, String solicitacaoId) {
        Cartao cartao = new Cartao();
        cartao.setId(id);
        cartao.setSolicitacaoId(solicitacaoId);
        cartao.setClienteId("cliente");
        cartao.setNumero("1000000000000000");
        cartao.setDataSolicitacao(DATA_SOLICITACAO);
        cartao.setStatus(StatusCartao.EMITIDO);
        return cartao;
    }

    private CartaoEmitidoEvent evento(String solicitacaoId) {
        return new CartaoEmitidoEvent(
                solicitacaoId,
                "cliente",
                "5001",
                "1000000000000000",
                "EMITIDO",
                DATA_SOLICITACAO,
                DATA_SOLICITACAO
        );
    }
}
