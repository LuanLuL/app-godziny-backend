package com.cefet.godziny.domain.casouso.categoria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import com.cefet.godziny.api.categoria.CategoriaDto;
import com.cefet.godziny.constantes.usuario.EnumRecursos;
import com.cefet.godziny.infraestrutura.exceptions.CampoRepetidoNoBancoException;
import com.cefet.godziny.infraestrutura.exceptions.UsuarioNaoAutorizadoException;
import com.cefet.godziny.infraestrutura.exceptions.categoria.CriarCategoriaIncompletaException;
import com.cefet.godziny.infraestrutura.persistencia.categoria.CategoriaEntidade;
import com.cefet.godziny.infraestrutura.persistencia.categoria.CategoriaRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoEntidade;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioEntidade;

@ExtendWith(MockitoExtension.class)
class AtualizarCategoriaCasoUsoTest {

    private CategoriaDto categoriaDto;
    private CategoriaEntidade categoriaEntidade;
    private CursoEntidade cursoEntidade;

    @Mock
    private CursoRepositorioJpa cursoRepositorioJpa;

    @Mock
    private CategoriaRepositorioJpa categoriaRepositorioJpa;

    private AtualizarCategoriaCasoUso atualizarCategoriaCasoUso;

    @BeforeEach
    void inicializarDados() {
        atualizarCategoriaCasoUso = new AtualizarCategoriaCasoUso(
            categoriaRepositorioJpa,
            cursoRepositorioJpa,
            UUID.randomUUID(),
            "sigla_TESTE",
            "nome_TESTE",
            1.0f,
            1.0f,
            "descrição_TESTE"
        );

        configurarUsuario(EnumRecursos.ADM);
    }

    @AfterEach
    void limparDados() {
        categoriaDto = null;
        categoriaEntidade = null;
        cursoEntidade = null;

        SecurityContextHolder.clearContext();
    }

    private void configurarUsuario(EnumRecursos tipo) {
        UsuarioEntidade usuario = new UsuarioEntidade(
            99999,
            null,
            "nome TESTE",
            "teste@test.com",
            "senha TESTE",
            tipo,
            LocalDateTime.now()
        );

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                usuario,
                null,
                List.of()
            );

        SecurityContext context =
            SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    @Test
    @DisplayName("It should validate and update a categoria successfully")
    void testeAtualizarCategoriaCasoUsoSuccess() throws Exception {
        categoriaDto = createCategoriaDto();
        categoriaEntidade = createCategoriaEntidade();
        cursoEntidade = createCursoEntidade();

        when(cursoRepositorioJpa.findBySigla(Mockito.anyString()))
            .thenReturn(cursoEntidade);

        when(categoriaRepositorioJpa.findByCursoAndNome(
            Mockito.any(CursoEntidade.class),
            Mockito.anyString()
        )).thenReturn(List.of());

        UUID idAtualizado = UUID.randomUUID();

        when(categoriaRepositorioJpa.updateCategoria(
            Mockito.any(CategoriaEntidade.class)
        )).thenReturn(idAtualizado);

        CursoEntidade cursoRetornado =
            atualizarCategoriaCasoUso.validarAtualizacao();

        UUID response = atualizarCategoriaCasoUso.atualizarCategoria(
            categoriaDto,
            cursoEntidade
        );

        assertThat(cursoRetornado).isEqualTo(cursoEntidade);
        assertThat(response).isEqualTo(idAtualizado);

        verify(cursoRepositorioJpa).findBySigla("sigla_TESTE");
        verify(categoriaRepositorioJpa).findByCursoAndNome(
            cursoEntidade,
            "nome_TESTE"
        );
        verify(categoriaRepositorioJpa).updateCategoria(
            Mockito.any(CategoriaEntidade.class)
        );
    }

    @Test
    @DisplayName("It should update when there is no change in nome")
    void testeAtualizarCategoriaCasoUsoSuccessCase2() throws Exception {
        categoriaEntidade = createCategoriaEntidade();
        cursoEntidade = createCursoEntidade();
        categoriaDto = createCategoriaDto();

        atualizarCategoriaCasoUso.setCategoriaId(
            categoriaEntidade.getId()
        );

        when(cursoRepositorioJpa.findBySigla(Mockito.anyString()))
            .thenReturn(cursoEntidade);

        when(categoriaRepositorioJpa.findByCursoAndNome(
            Mockito.any(CursoEntidade.class),
            Mockito.anyString()
        )).thenReturn(List.of(categoriaEntidade));

        UUID idAtualizado = UUID.randomUUID();

        when(categoriaRepositorioJpa.updateCategoria(
            Mockito.any(CategoriaEntidade.class)
        )).thenReturn(idAtualizado);

        CursoEntidade cursoRetornado =
            atualizarCategoriaCasoUso.validarAtualizacao();

        UUID response = atualizarCategoriaCasoUso.atualizarCategoria(
            categoriaDto,
            cursoEntidade
        );

        assertThat(cursoRetornado).isEqualTo(cursoEntidade);
        assertThat(response).isEqualTo(idAtualizado);
    }

    @Test
    @DisplayName("It should not update as a NORMAL user")
    void testeAtualizarCategoriaUsuarioNaoAutorizado() {
        configurarUsuario(EnumRecursos.NORMAL);

        UsuarioNaoAutorizadoException thrown = assertThrows(
            UsuarioNaoAutorizadoException.class,
            () -> atualizarCategoriaCasoUso.validarAtualizacao()
        );

        assertThat(thrown).isNotNull();

        verifyNoInteractions(cursoRepositorioJpa, categoriaRepositorioJpa);
    }

    @Test
    @DisplayName("It should throw an exception when cursoSigla exceeds 20 characters.")
    void testAtualizarCategoriaCasoUsoExceptionCase1() {
        atualizarCategoriaCasoUso.setCursoSigla(
            "SIGLAS bigger than 20 letters are not allowed"
        );

        CriarCategoriaIncompletaException thrown = assertThrows(
            CriarCategoriaIncompletaException.class,
            () -> atualizarCategoriaCasoUso.validarAtualizacao()
        );

        assertThat(thrown.getMessage()).isEqualTo(
            "A sigla do curso na categoria deve ter entre 3 e 20 caracteres"
        );
    }

    @Test
    @DisplayName("It should throw an exception when cursoSigla has fewer than 3 characters")
    void testAtualizarCategoriaCasoUsoExceptionCase2() {
        atualizarCategoriaCasoUso.setCursoSigla("S");

        CriarCategoriaIncompletaException thrown = assertThrows(
            CriarCategoriaIncompletaException.class,
            () -> atualizarCategoriaCasoUso.validarAtualizacao()
        );

        assertThat(thrown.getMessage()).isEqualTo(
            "A sigla do curso na categoria deve ter entre 3 e 20 caracteres"
        );
    }

    @Test
    @DisplayName("It should throw an exception when the nome exceeds 250 characters")
    void testAtualizarCategoriaCasoUsoExceptionCase3() {
        atualizarCategoriaCasoUso.setNome("N".repeat(251));

        CriarCategoriaIncompletaException thrown = assertThrows(
            CriarCategoriaIncompletaException.class,
            () -> atualizarCategoriaCasoUso.validarAtualizacao()
        );

        assertThat(thrown.getMessage()).isEqualTo(
            "O nome da categoria deve ter entre 3 e 250 caracteres"
        );
    }

    @Test
    @DisplayName("It should throw an exception when nome has fewer than 3 characters")
    void testAtualizarCategoriaCasoUsoExceptionCase4() {
        atualizarCategoriaCasoUso.setNome("N");

        CriarCategoriaIncompletaException thrown = assertThrows(
            CriarCategoriaIncompletaException.class,
            () -> atualizarCategoriaCasoUso.validarAtualizacao()
        );

        assertThat(thrown.getMessage()).isEqualTo(
            "O nome da categoria deve ter entre 3 e 250 caracteres"
        );
    }

    @Test
    @DisplayName("It should throw an exception when porcentagemHorasMaximas is negative")
    void testAtualizarCategoriaCasoUsoExceptionCase5() {
        atualizarCategoriaCasoUso.setPorcentagemHorasMaximas(-5.5f);

        CriarCategoriaIncompletaException thrown = assertThrows(
            CriarCategoriaIncompletaException.class,
            () -> atualizarCategoriaCasoUso.validarAtualizacao()
        );

        assertThat(thrown.getMessage()).isEqualTo(
            "A porcentagem de horas máximas da categoria deve ser maior que zero"
        );
    }

    @Test
    @DisplayName("It should throw an exception when porcentagemHorasMaximas is zero")
    void testAtualizarCategoriaCasoUsoExceptionCase6() {
        atualizarCategoriaCasoUso.setPorcentagemHorasMaximas(0.0f);

        CriarCategoriaIncompletaException thrown = assertThrows(
            CriarCategoriaIncompletaException.class,
            () -> atualizarCategoriaCasoUso.validarAtualizacao()
        );

        assertThat(thrown.getMessage()).isEqualTo(
            "A porcentagem de horas máximas da categoria deve ser maior que zero"
        );
    }

    @Test
    @DisplayName("It should throw an exception when horasMultiplicador is negative")
    void testAtualizarCategoriaCasoUsoExceptionCase7() {
        atualizarCategoriaCasoUso.setHorasMultiplicador(-5.5f);

        CriarCategoriaIncompletaException thrown = assertThrows(
            CriarCategoriaIncompletaException.class,
            () -> atualizarCategoriaCasoUso.validarAtualizacao()
        );

        assertThat(thrown.getMessage()).isEqualTo(
            "O multiplicador por horas da categoria deve ser maior que zero"
        );
    }

    @Test
    @DisplayName("It should throw an exception when horasMultiplicador is zero")
    void testAtualizarCategoriaCasoUsoExceptionCase8() {
        atualizarCategoriaCasoUso.setHorasMultiplicador(0.0f);

        CriarCategoriaIncompletaException thrown = assertThrows(
            CriarCategoriaIncompletaException.class,
            () -> atualizarCategoriaCasoUso.validarAtualizacao()
        );

        assertThat(thrown.getMessage()).isEqualTo(
            "O multiplicador por horas da categoria deve ser maior que zero"
        );
    }

    @Test
    @DisplayName("It should throw an exception when descricao exceeds 500 characters")
    void testAtualizarCategoriaCasoUsoExceptionCase9() {
        atualizarCategoriaCasoUso.setDescricao("D".repeat(501));

        CriarCategoriaIncompletaException thrown = assertThrows(
            CriarCategoriaIncompletaException.class,
            () -> atualizarCategoriaCasoUso.validarAtualizacao()
        );

        assertThat(thrown.getMessage()).isEqualTo(
            "A descrição da categoria deve ter entre 10 e 500 caracteres"
        );
    }

    @Test
    @DisplayName("It should throw an exception when descricao is less than 10 characters longs")
    void testAtualizarCategoriaCasoUsoExceptionCase10() {
        atualizarCategoriaCasoUso.setDescricao("D");

        CriarCategoriaIncompletaException thrown = assertThrows(
            CriarCategoriaIncompletaException.class,
            () -> atualizarCategoriaCasoUso.validarAtualizacao()
        );

        assertThat(thrown.getMessage()).isEqualTo(
            "A descrição da categoria deve ter entre 10 e 500 caracteres"
        );
    }

    @Test
    @DisplayName("It should throw an exception when another categoria in the curso already has the same name")
    void testAtualizarCategoriaCasoUsoExceptionCase11() throws Exception {
        cursoEntidade = createCursoEntidade();
        categoriaEntidade = createCategoriaEntidade();

        atualizarCategoriaCasoUso.setCategoriaId(UUID.randomUUID());

        when(cursoRepositorioJpa.findBySigla(Mockito.anyString()))
            .thenReturn(cursoEntidade);

        when(categoriaRepositorioJpa.findByCursoAndNome(
            Mockito.any(CursoEntidade.class),
            Mockito.anyString()
        )).thenReturn(List.of(categoriaEntidade));

        CampoRepetidoNoBancoException thrown = assertThrows(
            CampoRepetidoNoBancoException.class,
            () -> atualizarCategoriaCasoUso.validarAtualizacao()
        );

        assertThat(thrown.getMessage()).isEqualTo(
            "Já existe uma categoria com este nome no curso selecionado"
        );
    }

    private CategoriaDto createCategoriaDto() {
        return new CategoriaDto(
            UUID.randomUUID(),
            "sigla_TESTE",
            "nome_TESTE",
            1.0f,
            1.0f,
            "descrição_TESTE"
        );
    }

    private CursoEntidade createCursoEntidade() {
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

    private CategoriaEntidade createCategoriaEntidade() {
        return new CategoriaEntidade(
            UUID.randomUUID(),
            createCursoEntidade(),
            "Categoria_TESTE",
            1.0f,
            1.0f,
            "Descrição_TESTE"
        );
    }
}