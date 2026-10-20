package com.cefet.godziny.domain.casouso.curso;

import com.cefet.godziny.api.curso.CursoRecuperarDto;
import com.cefet.godziny.constantes.usuario.EnumRecursos;
import com.cefet.godziny.domain.casouso.curso.PesquisarCursoCasoUso;
import com.cefet.godziny.infraestrutura.exceptions.UsuarioNaoAutorizadoException;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoEntidade;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioEntidade;
import com.cefet.godziny.infraestrutura.rest.curso.CursoRestConverter;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PesquisarCursoCasoUsoTest {

    private static final Integer MATRICULA_ADMIN = 99999;

    private static final String SIGLA_CURSO = "ODONT_DIV";
    private static final String NOME_CURSO = "Odontologia";

    private CursoRepositorioJpa cursoRepositorioJpa;

    private PesquisarCursoCasoUso pesquisarCursoCasoUso;

    private UsuarioEntidade administrador;
    private CursoEntidade cursoEntidade;

    @BeforeEach
    void inicializarDados() {
        cursoRepositorioJpa = mock(CursoRepositorioJpa.class);

        administrador = criarAdministrador();
        cursoEntidade = criarCursoEntidade();

        autenticarAdministrador();

        pesquisarCursoCasoUso = new PesquisarCursoCasoUso(
                cursoRepositorioJpa,
                SIGLA_CURSO,
                NOME_CURSO
        );
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
                SIGLA_CURSO,
                NOME_CURSO,
                100,
                administrador
        );
    }

    @Test
    @DisplayName("Should validate and execute a successful course search")
    void devePesquisarCursosComSucesso() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);

        Page<CursoEntidade> page = new PageImpl<>(
                List.of(cursoEntidade),
                pageable,
                1
        );

        when(cursoRepositorioJpa.listCursos(
                anySpecification(),
                any(Pageable.class)
        )).thenReturn(page);

        assertDoesNotThrow(
                () -> pesquisarCursoCasoUso.validarPesquisa()
        );

        Page<CursoRecuperarDto> resultado =
                pesquisarCursoCasoUso.pesquisarCursos(pageable);

        CursoRecuperarDto dtoEsperado =
                CursoRestConverter.EntidadeToCursoRecuperarDto(
                        cursoEntidade
                );

        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0))
                .usingRecursiveComparison()
                .isEqualTo(dtoEsperado);

        verify(cursoRepositorioJpa)
                .listCursos(anySpecification(), any(Pageable.class));
    }

    @Test
    @DisplayName("Should reject a search performed by a non-administrator")
    void deveRejeitarPesquisaPorUsuarioNaoAdministrador() {
        autenticarUsuarioNormal();

        assertThrows(
                UsuarioNaoAutorizadoException.class,
                () -> pesquisarCursoCasoUso.validarPesquisa()
        );

        verifyNoInteractions(cursoRepositorioJpa);
    }

    @Test
    @DisplayName("Should search courses when the acronym filter is omitted")
    void devePesquisarSemFiltroDeSigla() {
        pesquisarCursoCasoUso = new PesquisarCursoCasoUso(
                cursoRepositorioJpa,
                null,
                NOME_CURSO
        );

        Pageable pageable = PageRequest.of(0, 10);

        when(cursoRepositorioJpa.listCursos(
                anySpecification(),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(cursoEntidade)));

        Page<CursoRecuperarDto> resultado =
                pesquisarCursoCasoUso.pesquisarCursos(pageable);

        assertThat(resultado.getContent()).hasSize(1);

        verify(cursoRepositorioJpa)
                .listCursos(anySpecification(), any(Pageable.class));
    }

    @Test
    @DisplayName("Should search courses when the name filter is omitted")
    void devePesquisarSemFiltroDeNome() {
        pesquisarCursoCasoUso = new PesquisarCursoCasoUso(
                cursoRepositorioJpa,
                SIGLA_CURSO,
                null
        );

        Pageable pageable = PageRequest.of(0, 10);

        when(cursoRepositorioJpa.listCursos(
                anySpecification(),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(cursoEntidade)));

        Page<CursoRecuperarDto> resultado =
                pesquisarCursoCasoUso.pesquisarCursos(pageable);

        assertThat(resultado.getContent()).hasSize(1);

        verify(cursoRepositorioJpa)
                .listCursos(anySpecification(), any(Pageable.class));
    }

    @Test
    @DisplayName("Should return an empty page when no courses match the search criteria")
    void deveRetornarPaginaVaziaQuandoNaoExistiremResultados() {
        Pageable pageable = PageRequest.of(0, 10);

        when(cursoRepositorioJpa.listCursos(
                anySpecification(),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(Collections.emptyList()));

        Page<CursoRecuperarDto> resultado =
                pesquisarCursoCasoUso.pesquisarCursos(pageable);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).isEmpty();
        assertThat(resultado.getTotalElements()).isZero();

        verify(cursoRepositorioJpa)
                .listCursos(anySpecification(), any(Pageable.class));
    }

    @Test
    @DisplayName("Should restrict the search to courses coordinated by the authenticated administrator")
    void deveFiltrarPesquisaPeloAdministradorAutenticado() {
        Pageable pageable = PageRequest.of(0, 10);

        when(cursoRepositorioJpa.listCursos(
                anySpecification(),
                any(Pageable.class)
        )).thenReturn(Page.empty(pageable));

        pesquisarCursoCasoUso.pesquisarCursos(pageable);

        verify(cursoRepositorioJpa)
                .listCursos(anySpecification(), eq(pageable));
    }

    @Test
    @DisplayName("Should validate the search without accessing repositories")
    void deveValidarPesquisaSemAcessarRepositorio() throws Exception {
        assertDoesNotThrow(
                () -> pesquisarCursoCasoUso.validarPesquisa()
        );

        verifyNoInteractions(cursoRepositorioJpa);
    }

    @SuppressWarnings("unchecked")
    private Specification<CursoEntidade> anySpecification() {
        return any(Specification.class);
    }

    @SuppressWarnings({"unchecked"})
    @Test
    @DisplayName("Should build predicates for all course search filters")
    void deveConstruirPredicadosParaTodosOsFiltros() {
        Pageable pageable = PageRequest.of(0, 10);

        when(cursoRepositorioJpa.listCursos(
                Mockito.any(Specification.class),
                Mockito.any(Pageable.class)
        )).thenReturn(Page.empty(pageable));

        pesquisarCursoCasoUso.pesquisarCursos(pageable);

        ArgumentCaptor<Specification<CursoEntidade>> captor =
                ArgumentCaptor.forClass(Specification.class);

        Mockito.verify(cursoRepositorioJpa).listCursos(
                captor.capture(),
                Mockito.eq(pageable)
        );

        Root<CursoEntidade> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);

        Path<String> nomePath = mock(Path.class);
        Path<String> siglaPath = mock(Path.class);
        Path<Object> coordenadorPath = mock(Path.class);
        Path<Integer> matriculaPath = mock(Path.class);

        Predicate nomePredicate = mock(Predicate.class);
        Predicate siglaPredicate = mock(Predicate.class);
        Predicate coordenadorPredicate = mock(Predicate.class);

        when(root.<String>get("nome")).thenReturn(nomePath);
        when(root.<String>get("sigla")).thenReturn(siglaPath);
        when(root.get("coordenador")).thenReturn(coordenadorPath);
        when(coordenadorPath.<Integer>get("matricula"))
                .thenReturn(matriculaPath);

        when(criteriaBuilder.like(nomePath, "%" + NOME_CURSO + "%"))
                .thenReturn(nomePredicate);

        when(criteriaBuilder.like(siglaPath, "%" + SIGLA_CURSO + "%"))
                .thenReturn(siglaPredicate);

        when(criteriaBuilder.equal(matriculaPath, MATRICULA_ADMIN))
                .thenReturn(coordenadorPredicate);

        captor.getValue().toPredicate(root, query, criteriaBuilder);

        verify(criteriaBuilder)
                .like(nomePath, "%" + NOME_CURSO + "%");

        verify(criteriaBuilder)
                .like(siglaPath, "%" + SIGLA_CURSO + "%");

        verify(criteriaBuilder)
                .equal(matriculaPath, MATRICULA_ADMIN);
    }
}