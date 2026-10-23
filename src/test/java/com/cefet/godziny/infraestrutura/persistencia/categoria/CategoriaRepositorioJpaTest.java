package com.cefet.godziny.infraestrutura.persistencia.categoria;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;
import com.cefet.godziny.constantes.usuario.EnumRecursos;
import com.cefet.godziny.infraestrutura.exceptions.categoria.CategoriaNaoEncontradaException;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoEntidade;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioEntidade;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class CategoriaRepositorioJpaTest {
    private static final UUID ID = UUID.randomUUID();
    private static final CursoEntidade CURSO = new CursoEntidade(
        UUID.randomUUID(),
        "ODONT_DIV",
        "Odontologia",
        1,
        new UsuarioEntidade(99999, null, "nome TESTE", "teste@test.com", "senha TESTE", EnumRecursos.ADM, LocalDateTime.now())
    );
    private static final String NOME = "Categoria Teste";
    private static final float PORCENTAGEM_HORAS_MAXIMAS = (float) 0.5;
    private static final float HORAS_MULTIPLICADOR = (float) 0.1;
    private static final String DESCRICAO = "Categoria Descrição Teste";

    private Optional<CategoriaEntidade> optional;
    private CategoriaEntidade entidade;

    @Mock
    private CategoriaRepositorioJpaSpring categoriaRepositorioJpaSpring;

    @InjectMocks
    private CategoriaRepositorioJpa categoriaRepositorio;

    @BeforeEach
    void start() {
        MockitoAnnotations.openMocks(this);
        categoriaRepositorio = new CategoriaRepositorioJpa(categoriaRepositorioJpaSpring);
    };

    @AfterEach
    void clean() {
        categoriaRepositorio.deleteAll();

        this.optional = null;
        this.entidade = null;
    }

    @Test
    @DisplayName("Search for a Categoria and return an existing successfully from DataBase")
    void testFindByIdSuccess() throws Exception {
        this.optional = createOptionalCategoria();

        when(categoriaRepositorioJpaSpring.findById(Mockito.any(UUID.class))).thenReturn(this.optional);
        CategoriaEntidade result = categoriaRepositorio.findById(ID);

        assertThat(result).isInstanceOf(CategoriaEntidade.class);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Search for a Categoria and return an excepiton because the ID doesn't exist")
    void testFindByIdCategoriaNaoEncontradaException() throws Exception {
        this.optional = Optional.empty();

        when(categoriaRepositorioJpaSpring.findById(Mockito.any(UUID.class))).thenReturn(this.optional);
        Exception thrown = assertThrows(CategoriaNaoEncontradaException.class, () -> {
            categoriaRepositorio.findById(ID);
        });
        
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).isEqualTo("Categoria não encontrada na base de dados");
    }

    @Test
    @DisplayName("Search for a Categoria by Curso And Nome successfully")
    void testFindByCursoAndNomeSuccess() {
        this.entidade = createCategoriaEntidade();
        List<CategoriaEntidade> list = new ArrayList<>();
        list.add(entidade);

        when(categoriaRepositorioJpaSpring.findByCursoAndNome(Mockito.any(CursoEntidade.class), Mockito.anyString())).thenReturn(list);
        List<CategoriaEntidade> result = categoriaRepositorio.findByCursoAndNome(CURSO, NOME);
        
        assertThat(result).isInstanceOf(List.class);
        assertThat(result).contains(entidade);
        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("Search for a Categoria only by Curso successfully")
    void testFindByCursoSuccess() {
        this.entidade = createCategoriaEntidade();
        List<CategoriaEntidade> list = new ArrayList<>();
        list.add(entidade);

        when(categoriaRepositorioJpaSpring.findByCurso(Mockito.any(CursoEntidade.class))).thenReturn(list);
        List<CategoriaEntidade> result = categoriaRepositorio.findByCurso(CURSO);
        
        assertThat(result).isInstanceOf(List.class);
        assertThat(result).contains(entidade);
        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("Search for an Categoria by NOME and return an existing Optional successfully from DataBase")
    void testFindByNomeOptionalSuccess() throws Exception {
        this.optional = createOptionalCategoria();

        when(categoriaRepositorioJpaSpring.findByNome(Mockito.anyString())).thenReturn(this.optional);
        Optional<CategoriaEntidade> result = categoriaRepositorio.findByNomeOptional(NOME);

        assertThat(result).isNotNull();
        assertThat(result).isPresent();
        assertThat(result.get()).isInstanceOf(CategoriaEntidade.class);
        assertThat(result.get()).isEqualTo(this.optional.get());
        assertThat(result.get().getNome()).isEqualTo(NOME);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Should list all Categorias successfully with Specification")
    void testListCategoriasSuccess() {
        this.entidade = createCategoriaEntidade();
        Page<CategoriaEntidade> page = new PageImpl<>(List.of(this.entidade));
        Pageable pageable = PageRequest.of(0, 10);
        Specification<CategoriaEntidade> specification = Specification.unrestricted();

        when(categoriaRepositorioJpaSpring.findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class))).thenReturn(page);
        Page<CategoriaEntidade> result = categoriaRepositorio.listCategorias(specification, pageable);

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(Page.class);
        assertThat(result.getSize()).isEqualTo(1);
        assertThat(result.getContent()).contains(this.entidade);
    }

    @Test
    @DisplayName("Should create a Categoria successfully")
    void testCreateCategoriaSuccess() {
        this.entidade = createCategoriaEntidade();

        when(categoriaRepositorioJpaSpring.save(Mockito.any(CategoriaEntidade.class))).thenReturn(this.entidade);
        UUID result = categoriaRepositorio.createCategoria(this.entidade);

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(UUID.class);
        assertThat(result).isEqualTo(ID);
    }

    @Test
    @DisplayName("Try to update a Categoria by ID successfully")
    void testUpdateCategoriaIDSuccess() throws Exception {
        this.entidade = createCategoriaEntidade();
        this.optional = createOptionalCategoria();

        when(categoriaRepositorioJpaSpring.findById(Mockito.any(UUID.class))).thenReturn(this.optional);
        when(categoriaRepositorioJpaSpring.save(Mockito.any(CategoriaEntidade.class))).thenReturn(this.entidade);
        UUID result = categoriaRepositorio.updateCategoria(this.entidade);

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(UUID.class);
        assertThat(result).isEqualTo(ID);
    }

    @Test
    @DisplayName("Try to update a Categoria and return an exception because there isn't any Categoria with that SIGLA")
    void testUpdateCategoriaCategoriaNaoEncontradaException() throws Exception {
        this.entidade = createCategoriaEntidade();
        this.optional = Optional.empty();

        when(categoriaRepositorioJpaSpring.findById(Mockito.any(UUID.class))).thenReturn(this.optional);
        Exception thrown = assertThrows(CategoriaNaoEncontradaException.class, () -> {
            categoriaRepositorio.updateCategoria(entidade);
        });
        
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).isEqualTo("Categoria não encontrada na base de dados");
    }

    @Test
    @DisplayName("Should delete a Categoria successfully")
    void testDeleteCategoriaSuccess() throws Exception{
        this.optional = createOptionalCategoria();

        when(categoriaRepositorioJpaSpring.findById(Mockito.any(UUID.class))).thenReturn(this.optional);
        doNothing().when(categoriaRepositorioJpaSpring).deleteById(Mockito.any(UUID.class));
        categoriaRepositorio.deleteCategoria(ID);

        verify(categoriaRepositorioJpaSpring, times(1)).deleteById(Mockito.any(UUID.class));
    }

    @Test
    @DisplayName("Try to delete a Categoria and return an excepiton because there isn't any Categoria with that ID")
    void testDeleteCategoriaCategoriaNaoEncontradaException() throws Exception{
        this.optional = Optional.empty();

        when(categoriaRepositorioJpaSpring.findById(Mockito.any(UUID.class))).thenReturn(this.optional);
        Exception thrown = assertThrows(CategoriaNaoEncontradaException.class, () -> {
            categoriaRepositorio.deleteCategoria(ID);
        });
        
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).isEqualTo("Categoria não encontrada na base de dados");
    }

    @Test
    @DisplayName("Should delete all Categorias successfully")
    void testDeleteAllSuccess() {
        doNothing().when(categoriaRepositorioJpaSpring).deleteAll();
        categoriaRepositorio.deleteAll();

        verify(categoriaRepositorioJpaSpring, times(1)).deleteAll();
    }

    private Optional<CategoriaEntidade> createOptionalCategoria(){
        CategoriaEntidade categoria = new CategoriaEntidade(ID, CURSO, NOME, PORCENTAGEM_HORAS_MAXIMAS, HORAS_MULTIPLICADOR, DESCRICAO);
        Optional<CategoriaEntidade> categoriaOptional = Optional.of(categoria);
        return categoriaOptional;
    }

    private CategoriaEntidade createCategoriaEntidade(){
        CategoriaEntidade categoria = new CategoriaEntidade(ID, CURSO, NOME, PORCENTAGEM_HORAS_MAXIMAS, HORAS_MULTIPLICADOR, DESCRICAO);
        return categoria;
    }
}

