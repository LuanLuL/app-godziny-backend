
package com.cefet.godziny.domain.casouso.categoria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import com.cefet.godziny.api.categoria.CategoriaRecuperarDto;
import com.cefet.godziny.constantes.usuario.EnumRecursos;
import com.cefet.godziny.infraestrutura.exceptions.UsuarioNaoAutorizadoException;
import com.cefet.godziny.infraestrutura.persistencia.categoria.CategoriaEntidade;
import com.cefet.godziny.infraestrutura.persistencia.categoria.CategoriaRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoEntidade;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioEntidade;
import com.cefet.godziny.infraestrutura.rest.categoria.CategoriaRestConverter;

@ExtendWith(MockitoExtension.class)
class ListarCategoriaCasoUsoTest {

    @Mock
    private CategoriaRepositorioJpa categoriaRepositorioJpa;

    private ListarCategoriaCasoUso listarCategoriaCasoUso;

    private UUID categoriaId;

    @BeforeEach
    void inicializarDados() {
        categoriaId = UUID.randomUUID();

        listarCategoriaCasoUso = new ListarCategoriaCasoUso(
            categoriaRepositorioJpa,
            categoriaId
        );
    }

    @AfterEach
    void limparDados() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("It should list categoria as a ADM user successfully")
    void deveListarCategoriaComoAdm() throws Exception {
        configurarUsuario(EnumRecursos.ADM);

        CategoriaEntidade categoria = criarCategoriaEntidade();

        when(categoriaRepositorioJpa.findById(categoriaId))
            .thenReturn(categoria);

        CategoriaRecuperarDto resultado =
            listarCategoriaCasoUso.validarListagem();

        CategoriaRecuperarDto esperado =
            CategoriaRestConverter.EntidadeToCategoriaRecuperarDto(categoria);

        assertThat(resultado).isNotNull();
        assertThat(resultado)
            .usingRecursiveComparison()
            .isEqualTo(esperado);

        verify(categoriaRepositorioJpa).findById(categoriaId);
    }

    @Test
    @DisplayName("It should not list categoria as a NORMAL user")
    void naoDeveListarCategoriaComoUsuarioNormal() {
        configurarUsuario(EnumRecursos.NORMAL);

        assertThrows(
            UsuarioNaoAutorizadoException.class,
            () -> listarCategoriaCasoUso.validarListagem()
        );

        verifyNoInteractions(categoriaRepositorioJpa);
    }

    @Test
    @DisplayName("It should list categoria as a ADM user with the expected datas")
    void deveRetornarDadosCorretosDaCategoria() throws Exception {
        configurarUsuario(EnumRecursos.ADM);

        CategoriaEntidade categoria = criarCategoriaEntidade();

        when(categoriaRepositorioJpa.findById(categoriaId))
            .thenReturn(categoria);

        CategoriaRecuperarDto resultado =
            listarCategoriaCasoUso.validarListagem();

        assertThat(resultado.getId()).isEqualTo(categoria.getId());
        assertThat(resultado.getNome()).isEqualTo(categoria.getNome());
        assertThat(resultado.getPorcentagemHorasMaximas())
            .isEqualTo(categoria.getPorcentagemHorasMaximas());
        assertThat(resultado.getHorasMultiplicador())
            .isEqualTo(categoria.getHorasMultiplicador());
        assertThat(resultado.getDescricao()).isEqualTo(categoria.getDescricao());

        verify(categoriaRepositorioJpa, times(1)).findById(categoriaId);
    }

    private void configurarUsuario(EnumRecursos tipo) {
        UsuarioEntidade usuario = mock(UsuarioEntidade.class);
        when(usuario.getTipo()).thenReturn(tipo);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(usuario);

        SecurityContext context =
            SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    private CategoriaEntidade criarCategoriaEntidade() {
        CursoEntidade curso = new CursoEntidade(
            UUID.randomUUID(),
            "CURSO_TESTE",
            "Curso de Teste",
            100,
            new UsuarioEntidade(
                99999,
                null,
                "Coordenador Teste",
                "teste@test.com",
                "senha",
                EnumRecursos.ADM,
                java.time.LocalDateTime.now()
            )
        );

        return new CategoriaEntidade(
            categoriaId,
            curso,
            "Categoria Teste",
            0.5f,
            1.5f,
            "Descrição da categoria"
        );
    }
}
