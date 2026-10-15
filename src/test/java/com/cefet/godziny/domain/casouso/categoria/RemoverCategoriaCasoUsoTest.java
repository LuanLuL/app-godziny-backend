
package com.cefet.godziny.domain.casouso.categoria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
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
import com.cefet.godziny.constantes.atividade.EnumStatus;
import com.cefet.godziny.constantes.usuario.EnumRecursos;
import com.cefet.godziny.infraestrutura.exceptions.UsuarioNaoAutorizadoException;
import com.cefet.godziny.infraestrutura.exceptions.categoria.RemoverCategoriaComAtividadesException;
import com.cefet.godziny.infraestrutura.persistencia.atividade.AtividadeEntidade;
import com.cefet.godziny.infraestrutura.persistencia.atividade.AtividadeRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.atividade.arquivo.ArquivoEntidade;
import com.cefet.godziny.infraestrutura.persistencia.categoria.CategoriaEntidade;
import com.cefet.godziny.infraestrutura.persistencia.categoria.CategoriaRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoEntidade;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioEntidade;

@ExtendWith(MockitoExtension.class)
class RemoverCategoriaCasoUsoTest {

    @Mock
    private CategoriaRepositorioJpa categoriaRepositorioJpa;

    @Mock
    private AtividadeRepositorioJpa atividadeRepositorioJpa;

    private RemoverCategoriaCasoUso removerCategoriaCasoUso;

    private UUID categoriaId;

    @BeforeEach
    void inicializarDados() {
        categoriaId = UUID.randomUUID();

        removerCategoriaCasoUso = new RemoverCategoriaCasoUso(
            categoriaRepositorioJpa,
            atividadeRepositorioJpa,
            categoriaId
        );
    }

    @AfterEach
    void limparDados() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("It should remove categoria with no atividade as a ADM user successfully")
    void deveRemoverCategoriaSemAtividades() throws Exception {
        configurarUsuario(EnumRecursos.ADM);

        CategoriaEntidade categoria = criarCategoriaEntidade();

        when(categoriaRepositorioJpa.findById(categoriaId))
            .thenReturn(categoria);

        when(atividadeRepositorioJpa.findByCategoria(categoria))
            .thenReturn(List.of());

        doNothing().when(categoriaRepositorioJpa)
            .deleteCategoria(categoriaId);

        assertDoesNotThrow(
            () -> removerCategoriaCasoUso.validarRemocao()
        );

        removerCategoriaCasoUso.removerCategoria();

        verify(categoriaRepositorioJpa).findById(categoriaId);
        verify(atividadeRepositorioJpa).findByCategoria(categoria);
        verify(categoriaRepositorioJpa).deleteCategoria(categoriaId);
    }

    @Test
    @DisplayName("It should not remove categoria as user ADM because it has atividades")
    void naoDeveRemoverCategoriaComAtividades() throws Exception {
        configurarUsuario(EnumRecursos.ADM);

        CategoriaEntidade categoria = criarCategoriaEntidade();

        when(categoriaRepositorioJpa.findById(categoriaId))
            .thenReturn(categoria);

        when(atividadeRepositorioJpa.findByCategoria(categoria))
            .thenReturn(List.of(criarAtividade(categoria)));

        RemoverCategoriaComAtividadesException exception =
            assertThrows(
                RemoverCategoriaComAtividadesException.class,
                () -> removerCategoriaCasoUso.validarRemocao()
            );

        assertThat(exception.getMessage()).isEqualTo(
            "Não é possível excluir uma categoria que possui atividades cadastradas"
        );

        verify(categoriaRepositorioJpa).findById(categoriaId);
        verify(atividadeRepositorioJpa).findByCategoria(categoria);
        verify(categoriaRepositorioJpa, never())
            .deleteCategoria(any(UUID.class));
    }

    @Test
    @DisplayName("It should not remove categoria as NORMAL user")
    void usuarioNormalNaoDeveRemoverCategoria() {
        configurarUsuario(EnumRecursos.NORMAL);

        UsuarioNaoAutorizadoException exception =
            assertThrows(
                UsuarioNaoAutorizadoException.class,
                () -> removerCategoriaCasoUso.validarRemocao()
            );

        assertThat(exception.getMessage()).isEqualTo(
            "Acesso restrito. Este módulo é acessível exclusivamente a administradores"
        );

        verifyNoInteractions(categoriaRepositorioJpa, atividadeRepositorioJpa
        );
    }

    @Test
    @DisplayName("It should not remove categoria as NORMAL user even when it does not have atividades")
    void usuarioNormalNaoDeveExcluirCategoriaSemAtividades() {
        configurarUsuario(EnumRecursos.NORMAL);

        assertThrows(
            UsuarioNaoAutorizadoException.class,
            () -> {
                removerCategoriaCasoUso.validarRemocao();
                removerCategoriaCasoUso.removerCategoria();
            }
        );

        verifyNoInteractions(
            categoriaRepositorioJpa,
            atividadeRepositorioJpa
        );
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
        return new CategoriaEntidade(
            categoriaId,
            criarCursoEntidade(),
            "categoria_TESTE",
            0.1f,
            0.1f,
            "descricao_TESTE"
        );
    }

    private CursoEntidade criarCursoEntidade() {
        return new CursoEntidade(
            UUID.randomUUID(),
            "CURSO_ID_TESTE",
            "TESTE",
            100,
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

    private AtividadeEntidade criarAtividade(
        CategoriaEntidade categoria
    ) {
        return new AtividadeEntidade(
            UUID.randomUUID(),
            new UsuarioEntidade(
                99999,
                criarCursoEntidade(),
                "nome TESTE",
                "teste@test.com",
                "senha TESTE",
                EnumRecursos.NORMAL,
                LocalDateTime.now()
            ),
            categoria,
            "nome atividade TESTE",
            LocalDateTime.now(),
            EnumStatus.APROVADA,
            new ArquivoEntidade(
                UUID.randomUUID(),
                "nome TESTE",
                "tipo TESTE",
                "dados TESTE".getBytes()
            ),
            2.4f,
            "comentario TESTE"
        );
    }
}
