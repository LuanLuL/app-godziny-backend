package com.cefet.godziny.domain.casouso.categoria;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class CriarCategoriaCasoUsoTest {
    private CategoriaDto categoriaDto;
    private CategoriaEntidade categoriaEntidade;
    private CursoEntidade cursoEntidade;

    @Mock
    CursoRepositorioJpa cursoRepositorioJpa;

    @Mock
    CategoriaRepositorioJpa categoriaRepositorioJpa;

    private CriarCategoriaCasoUso criarCategoriaCasoUso;

    @BeforeEach
    void inicializarDados() {
        criarCategoriaCasoUso = new CriarCategoriaCasoUso(
            categoriaRepositorioJpa, cursoRepositorioJpa,
            "sigla_TESTE", "nome_TESTE",
            (float) 1.0, (float) 1.0,
            "descrição_TESTE"
        );

        configurarUsuario(EnumRecursos.ADM);
    };

    @AfterEach
    void limparDados() {
        this.categoriaDto = null;
        this.categoriaEntidade = null;
        this.cursoEntidade = null;
        categoriaRepositorioJpa.deleteAll();
        cursoRepositorioJpa.deleteAll();

        SecurityContextHolder.clearContext();
    }
    
    @Test
    @DisplayName("Should valided an CriarCategoriaCasoUso successfully")
    void testeCriarCategoriaCasoUsoSuccess() throws Exception {
        this.categoriaDto = createCategoriaDto();
        this.categoriaEntidade = createCategoriaEntidade();
        this.cursoEntidade = createCursoEntidade();

        when(cursoRepositorioJpa.findBySigla(Mockito.anyString())).thenReturn(this.cursoEntidade);
        when(categoriaRepositorioJpa.createCategoria(Mockito.any(CategoriaEntidade.class))).thenReturn(UUID.randomUUID());
        criarCategoriaCasoUso.validarCriacao();
        UUID response = criarCategoriaCasoUso.createCategoria(this.categoriaDto, this.cursoEntidade);

        assertThat(response).isInstanceOf(UUID.class);
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("Try to create a Categoria and return an excepiton because the CURSOSIGLA is too big")
    void testCriarCategoriaCasoUsoExceptionCase1() throws Exception{
        criarCategoriaCasoUso.setCursoSigla("SIGLAS bigger than 20 letters are not allowed");

        Exception thrown = assertThrows(CriarCategoriaIncompletaException.class, () -> {
            criarCategoriaCasoUso.validarCriacao();
        });
        
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).isEqualTo("A sigla do curso na categoria deve ter entre 3 e 20 caracteres");
    }

    @Test
    @DisplayName("Try to create a Categoria and return an excepiton because the CURSOSIGLA is too short")
    void testCriarCategoriaCasoUsoExceptionCase2() throws Exception{
        criarCategoriaCasoUso.setCursoSigla("S");

        Exception thrown = assertThrows(CriarCategoriaIncompletaException.class, () -> {
            criarCategoriaCasoUso.validarCriacao();
        });
        
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).isEqualTo("A sigla do curso na categoria deve ter entre 3 e 20 caracteres");
    }

    @Test
    @DisplayName("Try to create a Categoria and return an excepiton because the NOME is too big")
    void testCriarCategoriaCasoUsoExceptionCase3() throws Exception{
        criarCategoriaCasoUso.setNome("That NOME is too big! The Godziny's rules doesn't let this happen because it's not allowed and Who do want to put a long NOME like this? That NOME is too big! The Godziny's rules doesn't let this happen because it's not allowed and Who do want to put a long NOME like this?");

        Exception thrown = assertThrows(CriarCategoriaIncompletaException.class, () -> {
            criarCategoriaCasoUso.validarCriacao();
        });
        
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).isEqualTo("O nome da categoria deve ter entre 3 e 250 caracteres");
    }

    @Test
    @DisplayName("Try to create a Categoria and return an excepiton because the NOME is too short")
    void testCriarCategoriaCasoUsoExceptionCase4() throws Exception{
        criarCategoriaCasoUso.setNome("N");

        Exception thrown = assertThrows(CriarCategoriaIncompletaException.class, () -> {
            criarCategoriaCasoUso.validarCriacao();
        });
        
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).isEqualTo("O nome da categoria deve ter entre 3 e 250 caracteres");
    }

    @Test
    @DisplayName("Try to create a Categoria and return an excepiton because the PORCENTAGEM HORAS MAXIMAS is lesser than 0")
    void testCriarCategoriaCasoUsoExceptionCase5() throws Exception{
        criarCategoriaCasoUso.setPorcentagemHorasMaximas((float) -5.5);

        Exception thrown = assertThrows(CriarCategoriaIncompletaException.class, () -> {
            criarCategoriaCasoUso.validarCriacao();
        });
        
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).isEqualTo("A porcentagem de horas máximas da categoria deve ser maior que zero");
    }

    @Test
    @DisplayName("Try to create a Categoria and return an excepiton because the PORCENTAGEM HORAS MAXIMAS is equal to 0")
    void testCriarCategoriaCasoUsoExceptionCase6() throws Exception{
        criarCategoriaCasoUso.setPorcentagemHorasMaximas((float) 0.0);

        Exception thrown = assertThrows(CriarCategoriaIncompletaException.class, () -> {
            criarCategoriaCasoUso.validarCriacao();
        });
        
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).isEqualTo("A porcentagem de horas máximas da categoria deve ser maior que zero");
    }

    @Test
    @DisplayName("Try to create a Categoria and return an excepiton because the MULTIPLICADOR HORAS is lesser than 0")
    void testCriarCategoriaCasoUsoExceptionCase7() throws Exception{
        criarCategoriaCasoUso.setHorasMultiplicador((float) -5.5);

        Exception thrown = assertThrows(CriarCategoriaIncompletaException.class, () -> {
            criarCategoriaCasoUso.validarCriacao();
        });
        
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).isEqualTo("O multiplicador por horas da categoria deve ser maior que zero");
    }

    @Test
    @DisplayName("Try to create a Categoria and return an excepiton because the MULTIPLICADOR HORAS is equal to 0")
    void testCriarCategoriaCasoUsoExceptionCase8() throws Exception{
        criarCategoriaCasoUso.setHorasMultiplicador((float) 0.0);

        Exception thrown = assertThrows(CriarCategoriaIncompletaException.class, () -> {
            criarCategoriaCasoUso.validarCriacao();
        });
        
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).isEqualTo("O multiplicador por horas da categoria deve ser maior que zero");
    }

    @Test
    @DisplayName("Try to create a Categoria and return an excepiton because the DESCRICAO is too big")
    void testCriarCategoriaCasoUsoExceptionCase9() throws Exception{
        criarCategoriaCasoUso.setDescricao(
            "Localizado no coração da cidade, nosso hotel combina conforto moderno com charme histórico. Quartos espaçosos e elegantemente decorados oferecem vistas deslumbrantes da paisagem urbana. Desfrute de refeições requintadas em nosso restaurante premiado, seguido de relaxamento completo em nosso spa de classe mundial. Para reuniões e eventos, temos instalações versáteis e tecnologicamente avançadas. Nossa equipe dedicada está pronta para garantir que sua estadia seja memorável e livre de preocupações. Descubra uma experiência única no nosso hotel."
        );

        Exception thrown = assertThrows(CriarCategoriaIncompletaException.class, () -> {
            criarCategoriaCasoUso.validarCriacao();
        });
        
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).isEqualTo("A descrição da categoria deve ter entre 10 e 500 caracteres");
    }

    @Test
    @DisplayName("Try to create a Categoria and return an excepiton because the DESCRICAO is too short")
    void testCriarCategoriaCasoUsoExceptionCase10() throws Exception{
        criarCategoriaCasoUso.setDescricao("D");

        Exception thrown = assertThrows(CriarCategoriaIncompletaException.class, () -> {
            criarCategoriaCasoUso.validarCriacao();
        });
        
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).isEqualTo("A descrição da categoria deve ter entre 10 e 500 caracteres");
    }

    @Test
    @DisplayName("Try to create a Categoria and return an excepiton because already exists one with the same CURSO and NOME")
    void testCriarCategoriaCasoUsoExceptionCase11() throws Exception{
        this.categoriaEntidade = createCategoriaEntidade();
        this.cursoEntidade = createCursoEntidade();
        List<CategoriaEntidade> list = new ArrayList<>();
        list.add(categoriaEntidade);

        when(cursoRepositorioJpa.findBySigla(Mockito.anyString())).thenReturn(this.cursoEntidade);
        when(categoriaRepositorioJpa.findByCursoAndNome(Mockito.any(CursoEntidade.class), Mockito.anyString())).thenReturn(list);
        Exception thrown = assertThrows(CampoRepetidoNoBancoException.class, () -> {
            criarCategoriaCasoUso.validarCriacao();
        });
        
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).isEqualTo("Já existe uma categoria com este nome no curso selecionado");
    }

    @Test
    @DisplayName("It should not create categoria as a NORMAL user")
    void testeCriarCategoriaUsuarioNormalNaoAutorizado() {
        configurarUsuario(EnumRecursos.NORMAL);

        UsuarioNaoAutorizadoException exception = assertThrows(
            UsuarioNaoAutorizadoException.class,
            () -> criarCategoriaCasoUso.validarCriacao()
        );

        assertThat(exception.getMessage()).isEqualTo(
            "Acesso restrito. Este módulo é acessível exclusivamente a administradores"
        );

        verifyNoInteractions(cursoRepositorioJpa, categoriaRepositorioJpa);
    }

    private CategoriaDto createCategoriaDto(){
        return new CategoriaDto(
            UUID.randomUUID(), 
            "sigla_TESTE",
            "nome_TESTE",
            (float) 1.0, (float) 1.0,
            "descrição_TESTE"
        );
    }

    private CursoEntidade createCursoEntidade(){
        return new CursoEntidade(
            UUID.randomUUID(),
            "ODONT_DIV",
            "Odontologia",
            1,
            new UsuarioEntidade(99999, null, "nome TESTE", "teste@test.com", "senha TESTE", EnumRecursos.ADM, LocalDateTime.now())
        );
    }

    private CategoriaEntidade createCategoriaEntidade(){
        return new CategoriaEntidade(
            UUID.randomUUID(),
            createCursoEntidade(), "Categoria_TESTE",
            (float) 1.0, (float) 1.0,
            "Descrição_TESTE");
    }


    private void configurarUsuario(EnumRecursos tipo) {
        UsuarioEntidade usuario = Mockito.mock(UsuarioEntidade.class);
        lenient().when(usuario.getTipo()).thenReturn(tipo);

        Authentication authentication = Mockito.mock(Authentication.class);
        lenient().when(authentication.getPrincipal()).thenReturn(usuario);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

}
