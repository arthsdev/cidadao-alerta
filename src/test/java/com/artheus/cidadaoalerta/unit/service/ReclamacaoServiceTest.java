package com.artheus.cidadaoalerta.unit.service;

import com.artheus.cidadaoalerta.dto.AtualizacaoReclamacao;
import com.artheus.cidadaoalerta.dto.CadastroReclamacao;
import com.artheus.cidadaoalerta.dto.DetalhamentoReclamacao;
import com.artheus.cidadaoalerta.dto.ReclamacaoPageResponse;
import com.artheus.cidadaoalerta.exception.reclamacao.*;
import com.artheus.cidadaoalerta.exception.usuario.*;
import com.artheus.cidadaoalerta.mapper.ReclamacaoMapper;
import com.artheus.cidadaoalerta.model.Localizacao;
import com.artheus.cidadaoalerta.model.Reclamacao;
import com.artheus.cidadaoalerta.model.Usuario;
import com.artheus.cidadaoalerta.model.enums.CategoriaReclamacao;
import com.artheus.cidadaoalerta.model.enums.Role;
import com.artheus.cidadaoalerta.model.enums.StatusReclamacao;
import com.artheus.cidadaoalerta.repository.ReclamacaoRepository;
import com.artheus.cidadaoalerta.repository.UsuarioRepository;
import com.artheus.cidadaoalerta.service.EmailService;
import com.artheus.cidadaoalerta.service.ReclamacaoService;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReclamacaoServiceTest {

    @Mock
    private EmailService emailService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ReclamacaoService reclamacaoService;

    @Mock
    private ReclamacaoRepository reclamacaoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ReclamacaoMapper reclamacaoMapper;

    private Usuario usuario;
    private Reclamacao reclamacao;
    private Localizacao localizacao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        localizacao = new Localizacao();
        localizacao.setLatitude(-22.5);
        localizacao.setLongitude(-45.4);

        usuario = new Usuario(1L, "Test User", "user@test.com", "senha123456", true, Role.ROLE_USER, null);

        reclamacao = new Reclamacao();
        reclamacao.setId(1L);
        reclamacao.setTitulo("Título Teste");
        reclamacao.setDescricao("Descrição detalhada da reclamação");
        reclamacao.setUsuario(usuario);
        reclamacao.setLocalizacao(localizacao);
        reclamacao.setStatus(StatusReclamacao.ABERTA);
        reclamacao.setAtivo(true);

        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        var authentication = new UsernamePasswordAuthenticationToken(usuario.getEmail(), null, usuario.getAuthorities());
        when(securityContext.getAuthentication()).thenReturn(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ===================== CADASTRO =====================
    @Test
    void deveCadastrarReclamacaoComSucesso() {
        CadastroReclamacao dto = new CadastroReclamacao("Título Teste",
                "Descrição detalhada da reclamação de teste",
                CategoriaReclamacao.SANEAMENTO, localizacao);

        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(reclamacaoRepository.findByTituloAndUsuarioIdAndAtivoTrue(dto.titulo(), usuario.getId())).thenReturn(Optional.empty());
        when(reclamacaoMapper.toEntity(dto, usuario)).thenReturn(reclamacao);
        when(reclamacaoRepository.save(reclamacao)).thenReturn(reclamacao);
        when(reclamacaoMapper.toDetalhamentoDto(reclamacao)).thenReturn(
                new DetalhamentoReclamacao(1L, dto.titulo(), dto.descricao(),
                        dto.categoriaReclamacao(), localizacao, StatusReclamacao.ABERTA,
                        LocalDateTime.now(), usuario.getId(), usuario.getNome())
        );

        DetalhamentoReclamacao result = reclamacaoService.cadastrarReclamacao(dto);

        assertEquals(dto.titulo(), result.titulo());
        assertEquals(StatusReclamacao.ABERTA, result.statusReclamacao());
    }

    @Test
    void deveLancarExceptionSeReclamacaoDuplicada() {
        CadastroReclamacao dto = new CadastroReclamacao("Título Teste",
                "Descrição detalhada da reclamação de teste",
                CategoriaReclamacao.ILUMINACAO, localizacao);

        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(reclamacaoRepository.findByTituloAndUsuarioIdAndAtivoTrue(dto.titulo(), usuario.getId()))
                .thenReturn(Optional.of(reclamacao));

        assertThrows(ReclamacaoDuplicadaException.class, () -> reclamacaoService.cadastrarReclamacao(dto));
    }

    @Test
    void deveLancarExceptionQuandoUsuarioNaoAutenticado() {
        SecurityContextHolder.clearContext();
        CadastroReclamacao dto = new CadastroReclamacao("Titulo Teste",
                "Descricao válida para teste", CategoriaReclamacao.ASFALTO, localizacao);

        assertThrows(UsuarioNaoAutenticadoException.class, () -> reclamacaoService.cadastrarReclamacao(dto));
    }

    // ===================== LISTAGEM =====================
    @Test
    void deveListarReclamacoesComSucesso() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Reclamacao> page = new PageImpl<>(List.of(reclamacao));

        when(reclamacaoRepository.findByAtivoTrue(any(Pageable.class))).thenReturn(page);
        when(reclamacaoMapper.toDetalhamentoDto(reclamacao)).thenReturn(
                new DetalhamentoReclamacao(1L, reclamacao.getTitulo(), reclamacao.getDescricao(),
                        reclamacao.getCategoriaReclamacao(), localizacao, StatusReclamacao.ABERTA,
                        LocalDateTime.now(), usuario.getId(), usuario.getNome())
        );

        ReclamacaoPageResponse<DetalhamentoReclamacao> response = reclamacaoService.listarReclamacoes(pageable);

        assertEquals(1, response.content().size());
    }

    @Test
    void deveUsarSortPadraoQuandoCampoSortInvalido() {
        Pageable pageable = PageRequest.of(0, 5, Sort.by("campoInvalido"));
        Page<Reclamacao> page = new PageImpl<>(List.of(reclamacao));

        when(reclamacaoRepository.findByAtivoTrue(any(Pageable.class))).thenReturn(page);
        when(reclamacaoMapper.toDetalhamentoDto(reclamacao)).thenReturn(
                new DetalhamentoReclamacao(1L, reclamacao.getTitulo(), reclamacao.getDescricao(),
                        reclamacao.getCategoriaReclamacao(), reclamacao.getLocalizacao(),
                        reclamacao.getStatus(), reclamacao.getDataCriacao(), usuario.getId(), usuario.getNome())
        );

        ReclamacaoPageResponse<DetalhamentoReclamacao> response = reclamacaoService.listarReclamacoes(pageable);

        assertEquals(1, response.content().size());
    }

    // ===================== AJUSTAR PAGEABLE =====================
    @Test
    void deveAjustarPageableQuandoPageETamanhoInvalidos() {
        Pageable pageableMock = mock(Pageable.class);
        when(pageableMock.getPageNumber()).thenReturn(-1);
        when(pageableMock.getPageSize()).thenReturn(0);
        when(pageableMock.getSort()).thenReturn(Sort.by("titulo"));

        Pageable ajustado = ReflectionTestUtils.invokeMethod(reclamacaoService, "ajustarPageable", pageableMock);

        assertEquals(0, ajustado.getPageNumber());
        assertEquals(10, ajustado.getPageSize());
        assertEquals("titulo", ajustado.getSort().iterator().next().getProperty());
    }

    @Test
    void deveAjustarPageableQuandoSizeMaiorQue50() {
        Pageable pageableMock = mock(Pageable.class);
        when(pageableMock.getPageNumber()).thenReturn(0);
        when(pageableMock.getPageSize()).thenReturn(100);
        when(pageableMock.getSort()).thenReturn(Sort.by("titulo"));

        Pageable ajustado = ReflectionTestUtils.invokeMethod(reclamacaoService, "ajustarPageable", pageableMock);

        assertEquals(10, ajustado.getPageSize());
    }

    @Test
    void deveUsarOrdenacaoPadraoQuandoCampoNaoPermitido() {
        Pageable pageableInvalido = PageRequest.of(0, 10, Sort.by("invalido").ascending());

        Pageable ajustado = ReflectionTestUtils.invokeMethod(reclamacaoService, "ajustarPageable", pageableInvalido);

        assertEquals(Sort.by(Sort.Direction.DESC, "dataCriacao"), ajustado.getSort());
    }

    // ===================== BUSCA =====================
    @Test
    void deveBuscarReclamacaoPorId() {
        when(reclamacaoRepository.findById(1L)).thenReturn(Optional.of(reclamacao));
        when(reclamacaoMapper.toDetalhamentoDto(reclamacao)).thenReturn(
                new DetalhamentoReclamacao(1L, reclamacao.getTitulo(), reclamacao.getDescricao(),
                        reclamacao.getCategoriaReclamacao(), localizacao, StatusReclamacao.ABERTA,
                        LocalDateTime.now(), usuario.getId(), usuario.getNome())
        );

        DetalhamentoReclamacao result = reclamacaoService.buscarPorId(1L);
        assertEquals(1L, result.id());
    }

    @Test
    void deveLancarExceptionSeReclamacaoNaoEncontrada() {
        when(reclamacaoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ReclamacaoNaoEncontradaException.class, () -> reclamacaoService.buscarPorId(99L));
    }

    // ===================== ATUALIZAÇÃO =====================
    @Test
    void deveAtualizarReclamacaoComSucesso() {
        AtualizacaoReclamacao dto = new AtualizacaoReclamacao();
        dto.setTitulo("Novo Título");
        dto.setDescricao("Nova Descrição detalhada");
        dto.setCategoriaReclamacao(CategoriaReclamacao.ILUMINACAO);
        dto.setLocalizacao(localizacao);

        // Mock do findById
        when(reclamacaoRepository.findById(1L)).thenReturn(Optional.of(reclamacao));
        // Mock do save
        when(reclamacaoRepository.save(any(Reclamacao.class))).thenReturn(reclamacao);
        // Mock do mapper
        when(reclamacaoMapper.toDetalhamentoDto(reclamacao)).thenReturn(
                new DetalhamentoReclamacao(
                        1L,
                        dto.getTitulo(),
                        dto.getDescricao(),
                        dto.getCategoriaReclamacao(),
                        localizacao,
                        StatusReclamacao.ABERTA,
                        LocalDateTime.now(),
                        usuario.getId(),
                        usuario.getNome()
                )
        );

        DetalhamentoReclamacao result = reclamacaoService.atualizarReclamacao(1L, dto);

        assertEquals("Novo Título", result.titulo());
        assertEquals(localizacao, result.localizacao());
    }


    @Test
    void deveLancarExceptionSeAtualizacaoInvalida() {
        AtualizacaoReclamacao dto = new AtualizacaoReclamacao();
        when(reclamacaoRepository.findById(1L)).thenReturn(Optional.of(reclamacao));

        assertThrows(ReclamacaoAtualizacaoInvalidaException.class, () ->
                reclamacaoService.atualizarReclamacao(1L, dto)
        );
    }

    @Test
    void deveAtualizarParcialmenteReclamacao() {
        AtualizacaoReclamacao dto = new AtualizacaoReclamacao();
        dto.setTitulo("Título Parcial");
        dto.setLocalizacao(localizacao);

        when(reclamacaoRepository.findById(1L)).thenReturn(Optional.of(reclamacao));

        reclamacaoService.atualizarParcialReclamacao(1L, dto);
        assertEquals("Título Parcial", reclamacao.getTitulo());
        assertEquals(localizacao, reclamacao.getLocalizacao());
    }

    @Test
    void deveManterReclamacaoQuandoAtualizacaoParcialComCamposNulos() {
        AtualizacaoReclamacao dto = new AtualizacaoReclamacao();
        when(reclamacaoRepository.findById(1L)).thenReturn(Optional.of(reclamacao));

        reclamacaoService.atualizarParcialReclamacao(1L, dto);
        assertEquals("Título Teste", reclamacao.getTitulo());
        assertEquals("Descrição detalhada da reclamação", reclamacao.getDescricao());
        assertEquals(StatusReclamacao.ABERTA, reclamacao.getStatus());
    }

    // ===================== INATIVAÇÃO =====================
    @Test
    void deveInativarReclamacaoComSucesso() {
        when(reclamacaoRepository.findById(1L)).thenReturn(Optional.of(reclamacao));
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));

        reclamacaoService.inativarReclamacao(1L);
        assertFalse(reclamacao.isAtivo());
    }

    @Test
    void deveLancarExceptionSeUsuarioSemPermissao() {
        Usuario outroUsuario = new Usuario(2L, "Outro User", "outro@test.com", "senha123456", true, Role.ROLE_USER, null);
        reclamacao.setUsuario(outroUsuario);

        when(reclamacaoRepository.findById(1L)).thenReturn(Optional.of(reclamacao));
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));

        assertThrows(UsuarioSemPermissaoException.class, () -> reclamacaoService.inativarReclamacao(1L));
    }
}
