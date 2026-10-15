package com.cefet.godziny.domain.casouso.categoria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import com.cefet.godziny.api.categoria.CategoriaRecuperarDto;
import com.cefet.godziny.constantes.usuario.EnumRecursos;
import com.cefet.godziny.infraestrutura.persistencia.categoria.CategoriaEntidade;
import com.cefet.godziny.infraestrutura.persistencia.categoria.CategoriaRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoEntidade;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioEntidade;

@ExtendWith(MockitoExtension.class)
class PesquisarCategoriaCasoUsoTest {

    @Mock
    private CategoriaRepositorioJpa categoriaRepositorioJpa;

    private PesquisarCategoriaCasoUso pesquisarCategoriaCasoUso;

    private CategoriaEntidade entidade;

    @BeforeEach
    void inicializarDados() {
        pesquisarCategoriaCasoUso = new PesquisarCategoriaCasoUso(
            categoriaRepositorioJpa,
            "SIGLA_TESTE",
            "NOME_TESTE"
        );
    }

    @AfterEach
    void limparDados() {
        entidade = null;
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should search for the categorias as a ADM user while aply every filters")
    void devePesquisarCategoriasComoAdm() {
        configurarUsuario(EnumRecursos.ADM, null);

        entidade = criarCategoriaEntidade();

        executarPesquisaEValidarSpecification(
            new PageImpl<>(List.of(entidade)),
            PageRequest.of(0, 10),
            true,
            true,
            true
        );
    }

    @Test
    @DisplayName("It should search for the categorias as a NORMAL user")
    void devePesquisarCategoriasComoUsuarioNormal() {
        EnumRecursos tipoNormal = Arrays.stream(EnumRecursos.values())
            .filter(tipo -> tipo != EnumRecursos.ADM)
            .findFirst()
            .orElseThrow(() ->
                new AssertionError("Não existe outro tipo em EnumRecursos")
            );

        CursoEntidade cursoUsuario = Mockito.mock(CursoEntidade.class);
        when(cursoUsuario.getId()).thenReturn(UUID.randomUUID());

        configurarUsuario(tipoNormal, cursoUsuario);

        entidade = criarCategoriaEntidade();

        executarPesquisaEValidarSpecification(
            new PageImpl<>(List.of(entidade)),
            PageRequest.of(0, 10),
            true,
            true,
            false
        );
    }

    @Test
    @DisplayName("It should search only by sigla when nome is null")
    void devePesquisarSomentePorSigla() {
        configurarUsuario(EnumRecursos.ADM, null);

        pesquisarCategoriaCasoUso = new PesquisarCategoriaCasoUso(
            categoriaRepositorioJpa,
            "SIGLA_TESTE",
            null
        );

        entidade = criarCategoriaEntidade();

        executarPesquisaEValidarSpecification(
            new PageImpl<>(List.of(entidade)),
            PageRequest.of(0, 10),
            true,
            false,
            true
        );
    }

    @Test
    @DisplayName("It should search only by name when sigla is null")
    void devePesquisarSomentePorNome() {
        configurarUsuario(EnumRecursos.ADM, null);

        pesquisarCategoriaCasoUso = new PesquisarCategoriaCasoUso(
            categoriaRepositorioJpa,
            null,
            "NOME_TESTE"
        );

        entidade = criarCategoriaEntidade();

        executarPesquisaEValidarSpecification(
            new PageImpl<>(List.of(entidade)),
            PageRequest.of(0, 10),
            false,
            true,
            true
        );
    }

    @Test
    @DisplayName("It should return empty page with no results")
    void deveRetornarPaginaVazia() {
        configurarUsuario(EnumRecursos.ADM, null);

        executarPesquisaEValidarSpecification(
            new PageImpl<>(List.of()),
            PageRequest.of(0, 10),
            true,
            true,
            true
        );
    }

    private void configurarUsuario(EnumRecursos tipo, CursoEntidade curso) {
        UsuarioEntidade usuario = Mockito.mock(UsuarioEntidade.class);

        when(usuario.getTipo()).thenReturn(tipo);

        if (tipo == EnumRecursos.ADM) {
            when(usuario.getMatricula()).thenReturn(99999);
        } else {
            when(usuario.getCurso()).thenReturn(curso);
        }

        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(usuario);

        SecurityContext context =
            SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void executarPesquisaEValidarSpecification(
        Page<CategoriaEntidade> paginaEsperada,
        Pageable pageable,
        boolean esperaFiltroSigla,
        boolean esperaFiltroNome,
        boolean usuarioAdm
    ) {
        when(categoriaRepositorioJpa.listCategorias(
            any(Specification.class),
            any(Pageable.class)
        )).thenReturn(paginaEsperada);

        Page<CategoriaRecuperarDto> resultado =
            pesquisarCategoriaCasoUso.pesquisarCategoria(pageable);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent())
            .hasSize(paginaEsperada.getContent().size());

        ArgumentCaptor<Specification> captor =
            ArgumentCaptor.forClass(Specification.class);

        verify(categoriaRepositorioJpa).listCategorias(
            captor.capture(),
            eq(pageable)
        );

        Root<CategoriaEntidade> root =
            Mockito.mock(Root.class, Mockito.RETURNS_DEEP_STUBS);

        CriteriaQuery<?> query = Mockito.mock(CriteriaQuery.class);

        CriteriaBuilder criteriaBuilder =
            Mockito.mock(
                CriteriaBuilder.class,
                Mockito.RETURNS_DEEP_STUBS
            );

        Predicate predicate = captor.getValue().toPredicate(
            root,
            query,
            criteriaBuilder
        );

        assertThat(predicate).isNotNull();

        if (esperaFiltroSigla) {
            verify(criteriaBuilder).lower(any());
            verify(criteriaBuilder).like(
                any(),
                eq("%sigla_teste%")
            );
        }

        if (esperaFiltroNome) {
            verify(criteriaBuilder).like(
                any(),
                eq("%NOME_TESTE%")
            );
        }

        if (usuarioAdm) {
            verify(criteriaBuilder).equal(any(), eq(99999));
        } else {
            verify(criteriaBuilder).in(any());
        }
    }

    private CategoriaEntidade criarCategoriaEntidade() {
        return new CategoriaEntidade(
            UUID.randomUUID(),
            criarCursoEntidade(),
            "NOME_TESTE",
            1.0f,
            1.0f,
            "Descrição_TESTE"
        );
    }

    private CursoEntidade criarCursoEntidade() {
        return new CursoEntidade(
            UUID.randomUUID(),
            "ODONT_DIV",
            "Odontologia",
            1,
            new UsuarioEntidade(
                99999,
                null,
                "nome TESTE",
                "teste@test.com",
                "senha TESTE",
                EnumRecursos.ADM,
                LocalDateTime.now()
            )
        );
    }
}
