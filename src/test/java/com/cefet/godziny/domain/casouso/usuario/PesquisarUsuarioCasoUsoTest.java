package com.cefet.godziny.domain.casouso.usuario;

import com.cefet.godziny.api.usuario.UsuarioRecuperarDto;
import com.cefet.godziny.constantes.usuario.EnumRecursos;
import com.cefet.godziny.infraestrutura.exceptions.UsuarioNaoAutorizadoException;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoEntidade;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioEntidade;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioRepositorioJpa;
import com.cefet.godziny.infraestrutura.rest.usuario.UsuarioRestConverter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PesquisarUsuarioCasoUsoTest {

    private static final Integer MATRICULA_ADMIN = 99999;
    private static final Integer MATRICULA_USUARIO = 999999;

    private CursoRepositorioJpa cursoRepositorioJpa;
    private UsuarioRepositorioJpa usuarioRepositorioJpa;

    private PesquisarUsuarioCasoUso pesquisarUsuarioCasoUso;

    private UsuarioEntidade administrador;
    private CursoEntidade cursoEntidade;
    private UsuarioEntidade usuarioEntidade;

    @BeforeEach
    void inicializarDados() {
        cursoRepositorioJpa = mock(CursoRepositorioJpa.class);
        usuarioRepositorioJpa = mock(UsuarioRepositorioJpa.class);

        administrador = criarAdministrador();
        cursoEntidade = criarCursoEntidade();
        usuarioEntidade = criarUsuarioEntidade();

        autenticarAdministrador();

        pesquisarUsuarioCasoUso = new PesquisarUsuarioCasoUso(
                usuarioRepositorioJpa,
                cursoRepositorioJpa,
                1,
                "SIGLA_TESTE",
                "NOME_TESTE"
        );

        when(cursoRepositorioJpa.findByCoordenador(MATRICULA_ADMIN))
                .thenReturn(List.of(cursoEntidade.getId()));
    }

    @AfterEach
    void limparDados() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarAdministrador() {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        administrador,
                        null,
                        Collections.emptyList()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }

    private void autenticarUsuarioNormal() {
        UsuarioEntidade usuarioNormal = new UsuarioEntidade(
                2,
                null,
                "Usuario Normal",
                "usuario@teste.com.br",
                "senha123",
                EnumRecursos.NORMAL,
                LocalDateTime.now()
        );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        usuarioNormal,
                        null,
                        Collections.emptyList()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }

    private UsuarioEntidade criarAdministrador() {
        return new UsuarioEntidade(
                MATRICULA_ADMIN,
                null,
                "Administrador",
                "admin@teste.com.br",
                "senha123",
                EnumRecursos.ADM,
                LocalDateTime.now()
        );
    }

    private CursoEntidade criarCursoEntidade() {
        return new CursoEntidade(
                UUID.randomUUID(),
                "SIGLA_TESTE",
                "Curso de Teste",
                100,
                administrador
        );
    }

    private UsuarioEntidade criarUsuarioEntidade() {
        return new UsuarioEntidade(
                MATRICULA_USUARIO,
                cursoEntidade,
                "NOME_TESTE",
                "teste@teste.com.br",
                "teste123",
                EnumRecursos.NORMAL,
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("Should validate and execute a successful user search")
    void devePesquisarUsuariosComSucesso() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);

        Page<UsuarioEntidade> page = new PageImpl<>(
                List.of(usuarioEntidade),
                pageable,
                1
        );

        when(usuarioRepositorioJpa.listUsuarios(
                anySpecification(),
                any(Pageable.class)
        )).thenReturn(page);

        assertDoesNotThrow(
                () -> pesquisarUsuarioCasoUso.validarPesquisa()
        );

        Page<UsuarioRecuperarDto> resultado =
                pesquisarUsuarioCasoUso.pesquisarUsuarios(pageable);

        UsuarioRecuperarDto dtoEsperado =
                UsuarioRestConverter.EntidadeToUsuarioRecuperarDto(
                        usuarioEntidade
                );

        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0))
                .usingRecursiveComparison()
                .isEqualTo(dtoEsperado);

        verify(cursoRepositorioJpa)
                .findByCoordenador(MATRICULA_ADMIN);

        verify(usuarioRepositorioJpa)
                .listUsuarios(anySpecification(), any(Pageable.class));
    }

    @Test
    @DisplayName("Should reject a search performed by a non-administrator")
    void deveRejeitarPesquisaPorUsuarioNaoAdministrador() {
        autenticarUsuarioNormal();

        assertThrows(
                UsuarioNaoAutorizadoException.class,
                () -> pesquisarUsuarioCasoUso.validarPesquisa()
        );
    }

    @Test
    @DisplayName("Should return users when the matricula filter is omitted")
    void devePesquisarSemFiltroDeMatricula() {
        pesquisarUsuarioCasoUso = new PesquisarUsuarioCasoUso(
                usuarioRepositorioJpa,
                cursoRepositorioJpa,
                null,
                "SIGLA_TESTE",
                "NOME_TESTE"
        );

        Pageable pageable = PageRequest.of(0, 10);

        when(usuarioRepositorioJpa.listUsuarios(
                anySpecification(),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(usuarioEntidade)));

        Page<UsuarioRecuperarDto> resultado =
                pesquisarUsuarioCasoUso.pesquisarUsuarios(pageable);

        assertThat(resultado.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should return users when the name filter is omitted")
    void devePesquisarSemFiltroDeNome() {
        pesquisarUsuarioCasoUso = new PesquisarUsuarioCasoUso(
                usuarioRepositorioJpa,
                cursoRepositorioJpa,
                MATRICULA_USUARIO,
                "SIGLA_TESTE",
                null
        );

        Pageable pageable = PageRequest.of(0, 10);

        when(usuarioRepositorioJpa.listUsuarios(
                anySpecification(),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(usuarioEntidade)));

        Page<UsuarioRecuperarDto> resultado =
                pesquisarUsuarioCasoUso.pesquisarUsuarios(pageable);

        assertThat(resultado.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should return users when the course filter is omitted")
    void devePesquisarSemFiltroDeCurso() {
        pesquisarUsuarioCasoUso = new PesquisarUsuarioCasoUso(
                usuarioRepositorioJpa,
                cursoRepositorioJpa,
                MATRICULA_USUARIO,
                null,
                "NOME_TESTE"
        );

        Pageable pageable = PageRequest.of(0, 10);

        when(usuarioRepositorioJpa.listUsuarios(
                anySpecification(),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(usuarioEntidade)));

        Page<UsuarioRecuperarDto> resultado =
                pesquisarUsuarioCasoUso.pesquisarUsuarios(pageable);

        assertThat(resultado.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should return an empty page when no users match the search criteria")
    void deveRetornarPaginaVaziaQuandoNaoExistiremResultados() {
        Pageable pageable = PageRequest.of(0, 10);

        when(usuarioRepositorioJpa.listUsuarios(
                anySpecification(),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(Collections.emptyList()));

        Page<UsuarioRecuperarDto> resultado =
                pesquisarUsuarioCasoUso.pesquisarUsuarios(pageable);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).isEmpty();
        assertThat(resultado.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("Should restrict the search to courses coordinated by the authenticated administrator")
    void deveFiltrarPesquisaPelosCursosCoordenadosPeloAdministrador() {
        Pageable pageable = PageRequest.of(0, 10);

        when(usuarioRepositorioJpa.listUsuarios(
                anySpecification(),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(usuarioEntidade)));

        pesquisarUsuarioCasoUso.pesquisarUsuarios(pageable);

        verify(cursoRepositorioJpa)
                .findByCoordenador(MATRICULA_ADMIN);
    }

    @Test
    @DisplayName("Should validate the search without accessing repositories")
    void deveValidarPesquisaSemAcessarRepositorios() throws Exception {
        assertDoesNotThrow(
                () -> pesquisarUsuarioCasoUso.validarPesquisa()
        );

        verifyNoInteractions(
                usuarioRepositorioJpa,
                cursoRepositorioJpa
        );
    }

    @SuppressWarnings("unchecked")
    private Specification<UsuarioEntidade> anySpecification() {
        return any(Specification.class);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    @DisplayName("Should build predicates for all user search filters")
    void deveConstruirPredicadosParaTodosOsFiltros() {
        UUID cursoId = UUID.randomUUID();
        List<UUID> cursoIds = List.of(cursoId);

        Integer matricula = 1;
        String nome = "NOME_TESTE";
        String cursoSigla = "SIGLA_TESTE";

        UsuarioEntidade administrador = Mockito.mock(UsuarioEntidade.class);
        when(administrador.getMatricula()).thenReturn(matricula);

        var authentication =
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                administrador, null
            );

        org.springframework.security.core.context.SecurityContextHolder
            .getContext()
            .setAuthentication(authentication);

        pesquisarUsuarioCasoUso = new PesquisarUsuarioCasoUso(
            usuarioRepositorioJpa,
            cursoRepositorioJpa,
            matricula,
            cursoSigla,
            nome
        );

        when(cursoRepositorioJpa.findByCoordenador(matricula))
            .thenReturn(cursoIds);

        Pageable pageable = PageRequest.of(0, 10);

        when(usuarioRepositorioJpa.listUsuarios(
            Mockito.any(Specification.class),
            Mockito.any(Pageable.class)
        )).thenReturn(Page.empty());

        pesquisarUsuarioCasoUso.pesquisarUsuarios(pageable);

        ArgumentCaptor<Specification<UsuarioEntidade>> captor =
            ArgumentCaptor.forClass(Specification.class);

        Mockito.verify(usuarioRepositorioJpa).listUsuarios(
            captor.capture(),
            Mockito.eq(pageable)
        );

        jakarta.persistence.criteria.Root<UsuarioEntidade> root =
            Mockito.mock(jakarta.persistence.criteria.Root.class);

        jakarta.persistence.criteria.CriteriaQuery<?> query =
            Mockito.mock(jakarta.persistence.criteria.CriteriaQuery.class);

        jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder =
            Mockito.mock(jakarta.persistence.criteria.CriteriaBuilder.class);

        jakarta.persistence.criteria.Path nomePath =
            Mockito.mock(jakarta.persistence.criteria.Path.class);

        jakarta.persistence.criteria.Path matriculaPath =
            Mockito.mock(jakarta.persistence.criteria.Path.class);

        jakarta.persistence.criteria.Path cursoPath =
            Mockito.mock(jakarta.persistence.criteria.Path.class);

        jakarta.persistence.criteria.Path siglaPath =
            Mockito.mock(jakarta.persistence.criteria.Path.class);

        jakarta.persistence.criteria.Path siglaLowerPath =
            Mockito.mock(jakarta.persistence.criteria.Path.class);

        jakarta.persistence.criteria.Path cursoIdPath =
            Mockito.mock(jakarta.persistence.criteria.Path.class);

        jakarta.persistence.criteria.Predicate predicate =
            Mockito.mock(jakarta.persistence.criteria.Predicate.class);

        when(root.get("nome")).thenReturn(nomePath);
        when(root.get("matricula")).thenReturn(matriculaPath);
        when(root.get("curso")).thenReturn(cursoPath);
        when(cursoPath.get("sigla")).thenReturn(siglaPath);
        when(cursoPath.get("id")).thenReturn(cursoIdPath);

        when(criteriaBuilder.like(nomePath, "%" + nome + "%"))
            .thenReturn(predicate);

        when(criteriaBuilder.equal(matriculaPath, matricula))
            .thenReturn(predicate);

        when(criteriaBuilder.lower(siglaPath))
            .thenReturn(siglaLowerPath);

        when(criteriaBuilder.like(siglaLowerPath, "%" + cursoSigla.toLowerCase() + "%"))
            .thenReturn(predicate);

        when(cursoIdPath.in(cursoIds))
            .thenReturn(predicate);

        
        captor.getValue().toPredicate(root, query, criteriaBuilder);

        verify(criteriaBuilder).like(nomePath, "%" + nome + "%");
        verify(criteriaBuilder).equal(matriculaPath, matricula);
        verify(criteriaBuilder).lower(siglaPath);
        verify(criteriaBuilder).like(siglaLowerPath, "%" + cursoSigla.toLowerCase() + "%");
        verify(cursoIdPath).in(cursoIds);
    }
}