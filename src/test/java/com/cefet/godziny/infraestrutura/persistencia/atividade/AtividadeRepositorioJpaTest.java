package com.cefet.godziny.infraestrutura.persistencia.atividade;

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
import com.cefet.godziny.constantes.atividade.EnumStatus;
import com.cefet.godziny.constantes.usuario.EnumRecursos;
import com.cefet.godziny.infraestrutura.exceptions.atividade.AtividadeNaoEncontradaException;
import com.cefet.godziny.infraestrutura.exceptions.categoria.CategoriaNaoEncontradaException;
import com.cefet.godziny.infraestrutura.exceptions.usuario.UsuarioNaoEncontradoException;
import com.cefet.godziny.infraestrutura.persistencia.atividade.arquivo.ArquivoEntidade;
import com.cefet.godziny.infraestrutura.persistencia.categoria.CategoriaEntidade;
import com.cefet.godziny.infraestrutura.persistencia.categoria.CategoriaRepositorioJpaSpring;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoEntidade;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioEntidade;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioRepositorioJpaSpring;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class AtividadeRepositorioJpaTest {
    private static final UUID ID = UUID.randomUUID();
    private static final CursoEntidade CURSO = new CursoEntidade(
        UUID.randomUUID(),
        "ODONT_DIV",
        "Odontologia",
        1,
        new UsuarioEntidade(99999, null, "nome TESTE", "teste@test.com", "senha TESTE", EnumRecursos.ADM, LocalDateTime.now())
    );
    private static final UsuarioEntidade USUARIO = new UsuarioEntidade(
        99999,
        CURSO,
        "nome TESTE",
        "teste@test.com",
        "senha TESTE",
        EnumRecursos.NORMAL,
        LocalDateTime.now()
    );
    private static final CategoriaEntidade CATEGORIA = new CategoriaEntidade(
        UUID.randomUUID(),
        CURSO,
        "nome TESTE",
        (float) 0.5,
        (float) 0.2, 
        "descriçao TESTE"
    );
    private static final ArquivoEntidade ARQUIVO = new ArquivoEntidade(
        UUID.randomUUID(),
        "nome TESTE",
        "tipo TESTE",
        "dados TESTE".getBytes()
    );

    private Optional<AtividadeEntidade> optional;
    private AtividadeEntidade entidade;

    @Mock
    private AtividadeRepositorioJpaSpring atividadeRepositorioJpaSpring;

    @Mock
    private CategoriaRepositorioJpaSpring categoriaRepositorioJpaSpring;

    @Mock
    private UsuarioRepositorioJpaSpring usuarioRepositorioJpaSpring;

    @InjectMocks
    private AtividadeRepositorioJpa atividadeRepositorio;

    @BeforeEach
    void start() {
        MockitoAnnotations.openMocks(this);
        atividadeRepositorio = new AtividadeRepositorioJpa(atividadeRepositorioJpaSpring, categoriaRepositorioJpaSpring, usuarioRepositorioJpaSpring);
    };

    @AfterEach
    void clean() {
        atividadeRepositorio.deleteAll();
        this.optional = null;
        this.entidade = null;
    }

    @Test
    @DisplayName("Search for an Atividade and return an existing successfully from DataBase")
    void testFindByIdSuccess() throws Exception {
        this.optional = createOptionalAtividade();

        when(atividadeRepositorioJpaSpring.findById(Mockito.any(UUID.class))).thenReturn(this.optional);
        AtividadeEntidade result = atividadeRepositorio.findById(ID);

        assertThat(result).isInstanceOf(AtividadeEntidade.class);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Search for an Atividade only by Categoria successfully")
    void testFindByCategoriaSuccess() {
        this.entidade = createAtividadeEntidade();
        List<AtividadeEntidade> list = new ArrayList<>();
        list.add(entidade);

        when(atividadeRepositorioJpaSpring.findByCategoria(Mockito.any(CategoriaEntidade.class))).thenReturn(list);
        List<AtividadeEntidade> result = atividadeRepositorio.findByCategoria(CATEGORIA);
        
        assertThat(result).isInstanceOf(List.class);
        assertThat(result).contains(entidade);
        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("Search for an Atividade only by Usuario successfully")
    void testFindByUsuarioSuccess() {
        this.entidade = createAtividadeEntidade();
        List<AtividadeEntidade> list = new ArrayList<>();
        list.add(entidade);

        when(atividadeRepositorioJpaSpring.findByUsuario(Mockito.any(UsuarioEntidade.class))).thenReturn(list);
        List<AtividadeEntidade> result = atividadeRepositorio.findByUsuario(USUARIO);
        
        assertThat(result).isInstanceOf(List.class);
        assertThat(result).contains(entidade);
        assertThat(result).isNotEmpty();
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Should list all Atividades successfully with Specification")
    void testListAtividadesSuccess() {
        this.entidade = createAtividadeEntidade();
        Page<AtividadeEntidade> page = new PageImpl<>(List.of(this.entidade));
        Pageable pageable = PageRequest.of(0, 10);
        Specification<AtividadeEntidade> specification = Specification.unrestricted();;

        when(atividadeRepositorioJpaSpring.findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class))).thenReturn(page);
        Page<AtividadeEntidade> result = atividadeRepositorio.listAtividades(specification, pageable);

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(Page.class);
        assertThat(result.getSize()).isEqualTo(1);
        assertThat(result.getContent()).contains(this.entidade);
    }

    @Test
    @DisplayName("Search for an Atividade and return an excepiton because the ID doesn't exist")
    void testFindByIdAtividadeNaoEncontradaException() throws Exception {
        this.optional = Optional.empty();

        when(atividadeRepositorioJpaSpring.findById(Mockito.any(UUID.class))).thenReturn(this.optional);
        Exception thrown = assertThrows(AtividadeNaoEncontradaException.class, () -> {
            atividadeRepositorio.findById(ID);
        });
        
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).isEqualTo("Atividade não encontrada na base de dados");
    }

    @Test
    @DisplayName("Should get the sum of all CargaHoraria of an Atividade By Usuario and Categoria successfully")
    void sumCargaHorarioByUsuarioIdAndCategoriaIdSuccess() throws Exception {
        this.optional = createOptionalAtividade();
        Optional<CategoriaEntidade> categoriaOptional = Optional.of(CATEGORIA);
        Optional<UsuarioEntidade> usuarioOptional = Optional.of(USUARIO);

        when(categoriaRepositorioJpaSpring.findById(Mockito.any(UUID.class))).thenReturn(categoriaOptional);
        when(usuarioRepositorioJpaSpring.findById(Mockito.anyInt())).thenReturn(usuarioOptional);
        when(atividadeRepositorioJpaSpring.findById(Mockito.any(UUID.class))).thenReturn(this.optional);
        when(atividadeRepositorioJpaSpring.sumCargaHorariaByUsuarioAndCategoria(Mockito.anyInt(), Mockito.any(UUID.class), Mockito.any(UUID.class))).thenReturn((float) 110.54);
        Float result = atividadeRepositorio.sumCargaHorarioByUsuarioIdAndCategoriaId(USUARIO.getMatricula(), CATEGORIA.getId(), ID);

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(Float.class);
        assertThat(result).isEqualTo((float) 110.54);
    }

    @Test
    @DisplayName("Should try to get the sum of all CargaHoraria of an Atividade and return an exception because there isn't Usuario with that ID")
    void sumCargaHorarioByUsuarioIdAndCategoriaIdUsuarioNaoEncontradoException() throws Exception {
        Optional<CategoriaEntidade> categoriaOptional = Optional.of(CATEGORIA);
        Optional<UsuarioEntidade> usuarioOptional = Optional.empty();

        when(categoriaRepositorioJpaSpring.findById(Mockito.any(UUID.class))).thenReturn(categoriaOptional);
        when(usuarioRepositorioJpaSpring.findById(Mockito.anyInt())).thenReturn(usuarioOptional);
        Exception thrown = assertThrows(UsuarioNaoEncontradoException.class, () -> {
            atividadeRepositorio.sumCargaHorarioByUsuarioIdAndCategoriaId(99999, CATEGORIA.getId(), ID);
        });
        
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).isEqualTo("Usuário não encontrado na base de dados");
    }

    @Test
    @DisplayName("Should try to get the sum of all CargaHoraria of an Atividade and return an exception cause there isn't Categoria with that ID")
    void sumCargaHorarioByUsuarioIdAndCategoriaIdCategoriaNaoEncontradaException() throws Exception {
        Optional<CategoriaEntidade> categoriaOptional = Optional.empty();
        Optional<UsuarioEntidade> usuarioOptional = Optional.of(USUARIO);

        when(categoriaRepositorioJpaSpring.findById(Mockito.any(UUID.class))).thenReturn(categoriaOptional);
        when(usuarioRepositorioJpaSpring.findById(Mockito.anyInt())).thenReturn(usuarioOptional);
        Exception thrown = assertThrows(CategoriaNaoEncontradaException.class, () -> {
            atividadeRepositorio.sumCargaHorarioByUsuarioIdAndCategoriaId(USUARIO.getMatricula(), UUID.randomUUID(), ID);
        });
        
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).isEqualTo("Categoria não encontrada na base de dados");
    }

    @Test
    @DisplayName("Should try to get the sum of all CargaHoraria of an Atividade and return an exception cause there isn't Atividade with that ID")
    void sumCargaHorarioByUsuarioIdAndCategoriaIdAtividadeNaoEncontradaException() throws Exception {
        Optional<CategoriaEntidade> categoriaOptional = Optional.of(CATEGORIA);
        Optional<UsuarioEntidade> usuarioOptional = Optional.of(USUARIO);

        when(categoriaRepositorioJpaSpring.findById(Mockito.any(UUID.class))).thenReturn(categoriaOptional);
        when(usuarioRepositorioJpaSpring.findById(Mockito.anyInt())).thenReturn(usuarioOptional);
        Exception thrown = assertThrows(AtividadeNaoEncontradaException.class, () -> {
            atividadeRepositorio.sumCargaHorarioByUsuarioIdAndCategoriaId(USUARIO.getMatricula(), CATEGORIA.getId(), UUID.randomUUID());
        });
        
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).isEqualTo("Atividade não encontrada na base de dados");
    }

    @Test
    @DisplayName("Should create an Atividade successfully")
    void testCreateAtividadeSuccess() {
        this.entidade = createAtividadeEntidade();

        when(atividadeRepositorioJpaSpring.save(Mockito.any(AtividadeEntidade.class))).thenReturn(this.entidade);
        UUID result = atividadeRepositorio.createAtividade(this.entidade);

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(UUID.class);
        assertThat(result).isEqualTo(ID);
    }

    @Test
    @DisplayName("Try to update a Atividade by ID successfully")
    void testUpdateAtividadeIDSuccess() throws Exception {
        this.entidade = createAtividadeEntidade();
        this.optional = createOptionalAtividade();

        when(atividadeRepositorioJpaSpring.findById(Mockito.any(UUID.class))).thenReturn(optional);
        when(atividadeRepositorioJpaSpring.save(Mockito.any(AtividadeEntidade.class))).thenReturn(this.entidade);
        UUID result = atividadeRepositorio.updateAtividade(this.entidade);

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(UUID.class);
        assertThat(result).isEqualTo(ID);
    }

    @Test
    @DisplayName("Try to update a Atividade and return an exception because there isn't any Atividade with that ID")
    void testUpdateAtividadeAtividadeNaoEncontradaException() throws Exception {
        this.entidade = createAtividadeEntidade();
        this.optional = Optional.empty();

        when(atividadeRepositorioJpaSpring.findById(Mockito.any(UUID.class))).thenReturn(this.optional);
        Exception thrown = assertThrows(AtividadeNaoEncontradaException.class, () -> {
            atividadeRepositorio.updateAtividade(this.entidade);
        });
        
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).isEqualTo("Atividade não encontrada na base de dados");
    }

    @Test
    @DisplayName("Should delete a Atividade successfully")
    void testDeleteAtividadeSuccess() throws Exception{
        this.optional = createOptionalAtividade();

        when(atividadeRepositorioJpaSpring.findById(Mockito.any(UUID.class))).thenReturn(this.optional);
        doNothing().when(atividadeRepositorioJpaSpring).deleteById(Mockito.any(UUID.class));
        atividadeRepositorio.deleteAtividade(ID);

        verify(atividadeRepositorioJpaSpring, times(1)).deleteById(Mockito.any(UUID.class));
    }

    @Test
    @DisplayName("Try to delete a Atividade and return an excepiton because there isn't any Atividade with that ID")
    void testDeleteAtividadeAtividadeNaoEncontradaException() throws Exception{
        this.optional = Optional.empty();

        when(atividadeRepositorioJpaSpring.findById(Mockito.any(UUID.class))).thenReturn(this.optional);
        Exception thrown = assertThrows(AtividadeNaoEncontradaException.class, () -> {
            atividadeRepositorio.deleteAtividade(ID);
        });
        
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).isEqualTo("Atividade não encontrada na base de dados");
    }

    @Test
    @DisplayName("Should delete all Atividades successfully")
    void testDeleteAllSuccess() {
        doNothing().when(atividadeRepositorioJpaSpring).deleteAll();
        atividadeRepositorio.deleteAll();

        verify(atividadeRepositorioJpaSpring, times(1)).deleteAll();
    }

    private Optional<AtividadeEntidade> createOptionalAtividade(){
        AtividadeEntidade atividade = new AtividadeEntidade(
            ID,
            USUARIO,
            CATEGORIA,
            "nome atividade TESTE",
            LocalDateTime.now(),
            EnumStatus.APROVADA,
            ARQUIVO,
            (float) 2.4,
            "comentario TESTE"
        );
        Optional<AtividadeEntidade> atividadeOptional = Optional.of(atividade);
        return atividadeOptional;
    }

    private AtividadeEntidade createAtividadeEntidade(){
        AtividadeEntidade atividade = new AtividadeEntidade(
            ID,
            USUARIO,
            CATEGORIA,
            "nome atividade TESTE",
            LocalDateTime.now(),
            EnumStatus.APROVADA,
            ARQUIVO,
            (float) 2.4,
            "comentario TESTE"
        );
        return atividade;
    }
}
