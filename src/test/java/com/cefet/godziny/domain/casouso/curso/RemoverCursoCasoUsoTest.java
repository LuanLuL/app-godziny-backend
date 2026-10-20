package com.cefet.godziny.domain.casouso.curso;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.cefet.godziny.constantes.usuario.EnumRecursos;
import com.cefet.godziny.infraestrutura.exceptions.UsuarioNaoAutorizadoException;
import com.cefet.godziny.infraestrutura.exceptions.curso.RemoverCursoComCategoriasException;
import com.cefet.godziny.infraestrutura.exceptions.curso.RemoverCursoComUsuariosException;
import com.cefet.godziny.infraestrutura.persistencia.categoria.CategoriaEntidade;
import com.cefet.godziny.infraestrutura.persistencia.categoria.CategoriaRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoEntidade;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioEntidade;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioRepositorioJpa;

@ExtendWith(MockitoExtension.class)
class RemoverCursoCasoUsoTest {

    private static final String SIGLA_CURSO = "TESTE";
    private static final Integer MATRICULA_ADMIN = 99999;

    @Mock
    private CursoRepositorioJpa cursoRepositorioJpa;

    @Mock
    private UsuarioRepositorioJpa usuarioRepositorioJpa;

    @Mock
    private CategoriaRepositorioJpa categoriaRepositorioJpa;

    private RemoverCursoCasoUso removerCursoCasoUso;

    private UsuarioEntidade administrador;
    private CursoEntidade cursoEntidade;

    @BeforeEach
    void inicializarDados() {
        administrador = criarAdministrador();
        configurarAutenticacao(administrador);

        cursoEntidade = criarCursoEntidade();

        removerCursoCasoUso = new RemoverCursoCasoUso(
            cursoRepositorioJpa,
            usuarioRepositorioJpa,
            categoriaRepositorioJpa,
            SIGLA_CURSO
        );
    }

    @AfterEach
    void limparDados() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should validate and remove a course successfully")
    void deveValidarERemoverCursoComSucesso() throws Exception {
        when(cursoRepositorioJpa.findBySigla(SIGLA_CURSO))
            .thenReturn(cursoEntidade);

        when(usuarioRepositorioJpa.listUsuariosByCurso(
            org.mockito.ArgumentMatchers.any(Pageable.class),
            org.mockito.ArgumentMatchers.eq(cursoEntidade)
        )).thenReturn(new PageImpl<>(List.of()));

        when(categoriaRepositorioJpa.findByCurso(cursoEntidade))
            .thenReturn(List.of());

        removerCursoCasoUso.validarRemocao();
        removerCursoCasoUso.removerCurso();

        verify(cursoRepositorioJpa).findBySigla(SIGLA_CURSO);
        verify(usuarioRepositorioJpa).listUsuariosByCurso(
            org.mockito.ArgumentMatchers.any(Pageable.class),
            org.mockito.ArgumentMatchers.eq(cursoEntidade)
        );
        verify(categoriaRepositorioJpa).findByCurso(cursoEntidade);
        verify(cursoRepositorioJpa).deleteCurso(SIGLA_CURSO);
    }

    @Test
    @DisplayName("Should throw an exception when the course has enrolled users")
    void deveLancarExcecaoQuandoCursoPossuirUsuarios() throws Exception {
        UsuarioEntidade usuario = new UsuarioEntidade(
            12345,
            cursoEntidade,
            "Usuário Teste",
            "usuario@teste.com",
            "senha_teste",
            EnumRecursos.NORMAL,
            LocalDateTime.now()
        );

        Page<UsuarioEntidade> usuarios =
            new PageImpl<>(List.of(usuario));

        when(cursoRepositorioJpa.findBySigla(SIGLA_CURSO))
            .thenReturn(cursoEntidade);

        when(usuarioRepositorioJpa.listUsuariosByCurso(
            org.mockito.ArgumentMatchers.any(Pageable.class),
            org.mockito.ArgumentMatchers.eq(cursoEntidade)
        )).thenReturn(usuarios);
 
        assertThatThrownBy(() -> removerCursoCasoUso.validarRemocao())
            .isInstanceOf(RemoverCursoComUsuariosException.class)
            .hasMessage(
                "Não é possível excluir um curso que possui usuários matriculados"
            );

        verify(cursoRepositorioJpa).findBySigla(SIGLA_CURSO);
        verify(usuarioRepositorioJpa).listUsuariosByCurso(
            org.mockito.ArgumentMatchers.any(Pageable.class),
            org.mockito.ArgumentMatchers.eq(cursoEntidade)
        );
        verify(categoriaRepositorioJpa, never()).findByCurso(
            org.mockito.ArgumentMatchers.any(CursoEntidade.class)
        );
        verify(cursoRepositorioJpa, never()).deleteCurso(SIGLA_CURSO);
    }

    @Test
    @DisplayName("Should throw an exception when the course has categories")
    void deveLancarExcecaoQuandoCursoPossuirCategorias() throws Exception {
        CategoriaEntidade categoria = new CategoriaEntidade(
            UUID.randomUUID(),
            cursoEntidade,
            "CATEGORIA_TESTE",
            0.5f,
            1.0f,
            "Descrição de teste"
        );

        when(cursoRepositorioJpa.findBySigla(SIGLA_CURSO))
            .thenReturn(cursoEntidade);

        when(usuarioRepositorioJpa.listUsuariosByCurso(
            org.mockito.ArgumentMatchers.any(Pageable.class),
            org.mockito.ArgumentMatchers.eq(cursoEntidade)
        )).thenReturn(new PageImpl<>(List.of()));

        when(categoriaRepositorioJpa.findByCurso(cursoEntidade))
            .thenReturn(List.of(categoria));
 
        assertThatThrownBy(() -> removerCursoCasoUso.validarRemocao())
            .isInstanceOf(RemoverCursoComCategoriasException.class)
            .hasMessage(
                "Não é possível excluir um curso que possui categorias cadastradas"
            );

        verify(cursoRepositorioJpa).findBySigla(SIGLA_CURSO);
        verify(usuarioRepositorioJpa).listUsuariosByCurso(
            org.mockito.ArgumentMatchers.any(Pageable.class),
            org.mockito.ArgumentMatchers.eq(cursoEntidade)
        );
        verify(categoriaRepositorioJpa).findByCurso(cursoEntidade);
        verify(cursoRepositorioJpa, never()).deleteCurso(SIGLA_CURSO);
    }

    @Test
    @DisplayName("Should reject course removal when the user is not an administrator")
    void deveRejeitarRemocaoQuandoUsuarioNaoForAdministrador() {
        UsuarioEntidade usuarioNormal = new UsuarioEntidade(
            12345,
            null,
            "Usuário Normal",
            "normal@teste.com",
            "senha_teste",
            EnumRecursos.NORMAL,
            LocalDateTime.now()
        );

        configurarAutenticacao(usuarioNormal);
 
        assertThatThrownBy(() -> removerCursoCasoUso.validarRemocao())
            .isInstanceOf(UsuarioNaoAutorizadoException.class);

        verifyNoInteractions(
            cursoRepositorioJpa,
            usuarioRepositorioJpa,
            categoriaRepositorioJpa
        );
    }

    @Test
    @DisplayName("Should remove a course by its registration code")
    void deveRemoverCursoPelaSigla() throws Exception {
        removerCursoCasoUso.removerCurso();

        verify(cursoRepositorioJpa).deleteCurso(SIGLA_CURSO);
    }

    private UsuarioEntidade criarAdministrador() {
        return new UsuarioEntidade(
            MATRICULA_ADMIN,
            null,
            "Administrador Teste",
            "admin@teste.com",
            "senha_admin",
            EnumRecursos.ADM,
            LocalDateTime.now()
        );
    }

    private CursoEntidade criarCursoEntidade() {
        return new CursoEntidade(
            UUID.randomUUID(),
            SIGLA_CURSO,
            "Curso de Teste",
            100,
            administrador
        );
    }

    private void configurarAutenticacao(UsuarioEntidade usuario) {
        var authentication =
            new UsernamePasswordAuthenticationToken(usuario, null);

        SecurityContextHolder.getContext()
            .setAuthentication(authentication);
    }
}