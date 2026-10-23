package com.cefet.godziny.infraestrutura.persistencia.curso;

import java.time.LocalDateTime;
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
import com.cefet.godziny.infraestrutura.exceptions.curso.CursoNaoEncontradoException;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioEntidade;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class CursoRepositorioJpaTest {
    private static final UUID ID = UUID.randomUUID();
    private static final String SIGLA = "ODONT_DIV";
    private static final Integer CARGA_HORARIA_COMPLEMENTAR = 615;
    private static final String NOME = "Odontologia";
    private static final UsuarioEntidade COORDENADOR = new UsuarioEntidade(99999, null, "nome TESTE", "teste@test.com", "senha TESTE", EnumRecursos.ADM, LocalDateTime.now());

    private Optional<CursoEntidade> optional;
    private CursoEntidade entidade;

    @Mock
    private CursoRepositorioJpaSpring cursoRepositorioJpaSpring;

    @InjectMocks
    private CursoRepositorioJpa cursoRepositorio;


    @BeforeEach
    void start() {
        MockitoAnnotations.openMocks(this);
        cursoRepositorio = new CursoRepositorioJpa(cursoRepositorioJpaSpring);
    };

    @AfterEach
    void clean() {
        cursoRepositorio.deleteAll();

        this.optional = null;
        this.entidade = null;
    }

    @Test
    @DisplayName("Search for a Curso and return an existing successfully from DataBase")
    void testFindBySiglaSuccess() throws Exception {
        this.optional = createOptionalCurso();

        when(cursoRepositorioJpaSpring.findBySigla(Mockito.anyString())).thenReturn(this.optional);
        CursoEntidade result = cursoRepositorio.findBySigla(SIGLA);

        assertThat(result).isInstanceOf(CursoEntidade.class);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Search for a Curso and return an existing Optional successfully from DataBase")
    void testFindBySiglaOptionalSuccess() throws Exception {
        this.optional = createOptionalCurso();

        when(cursoRepositorioJpaSpring.findBySigla(Mockito.anyString())).thenReturn(this.optional);
        Optional<CursoEntidade> result = cursoRepositorio.findBySiglaOptional(SIGLA);

        assertThat(result).isPresent();
        assertThat(result).containsInstanceOf(CursoEntidade.class);
    }

    @Test
    @DisplayName("Search for a Curso without SIGLA and return null")
    void testFindByIdSuccessWithoutSigla() throws Exception {
        CursoEntidade result = cursoRepositorio.findBySigla("");

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Search for a Curso Optional without SIGLA and return null")
    void testFindBySiglaOptionalSuccessWithoutSigla() throws Exception {
        Optional<CursoEntidade> result = cursoRepositorio.findBySiglaOptional("");

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Search for a Curso and return an excepiton because the SIGLA doesn't exist")
    void testFindByIdCursoNaoEncontradoException() throws Exception {
        this.optional = Optional.empty();

        when(cursoRepositorioJpaSpring.findBySigla(Mockito.anyString())).thenReturn(this.optional);
        Exception thrown = assertThrows(CursoNaoEncontradoException.class, () -> {
            cursoRepositorio.findBySigla("Curso que não existe");
        });
        
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).isEqualTo("Curso não encontrado na base de dados");
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Should list all Cursos successfully with Specification")
    void testListCursosSuccess() {
        this.entidade = createCursoEntidade();
        Page<CursoEntidade> page = new PageImpl<>(List.of(this.entidade));
        Pageable pageable = PageRequest.of(0, 10);
        Specification<CursoEntidade> specification = Specification.unrestricted();;

        when(cursoRepositorioJpaSpring.findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class))).thenReturn(page);
        Page<CursoEntidade> result = cursoRepositorio.listCursos(specification, pageable);

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(Page.class);
        assertThat(result.getSize()).isEqualTo(1);
        assertThat(result.getContent()).contains(this.entidade);
    }

    @Test
    @DisplayName("Should create a Curso successfully")
    void testCreateCursoSuccess() {
        this.entidade = createCursoEntidade();

        when(cursoRepositorioJpaSpring.save(Mockito.any(CursoEntidade.class))).thenReturn(this.entidade);
        String result = cursoRepositorio.createCurso(entidade);

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(String.class);
        assertThat(result).isEqualTo(SIGLA);

    }

    @Test
    @DisplayName("Should update a Curso successfully")
    void testUpdateCursoSuccess() throws Exception {
        this.entidade = createCursoEntidade();
        this.optional = createOptionalCurso();

        when(cursoRepositorioJpaSpring.findBySigla(Mockito.anyString())).thenReturn(optional);
        doNothing().when(cursoRepositorioJpaSpring).updateCursoById(
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyInt(),
            Mockito.anyString()
        );
        String result = cursoRepositorio.updateCurso(SIGLA, entidade);

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(String.class);
        assertThat(result).isEqualTo(SIGLA);
    }

    @Test
    @DisplayName("Try to update a Curso by ID successfully")
    void testUpdateCursoIDSuccess() throws Exception {
        this.entidade = createCursoEntidade();
        this.optional = createOptionalCurso();

        when(cursoRepositorioJpaSpring.findBySigla(Mockito.anyString())).thenReturn(optional);
        doNothing().when(cursoRepositorioJpaSpring).updateCursoById(
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyInt(),
            Mockito.anyString()
        );
        String result = cursoRepositorio.updateCurso("SIGLA diferente", entidade);

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(String.class);
        assertThat(result).isEqualTo(SIGLA);
    }

    @Test
    @DisplayName("Try to update a Curso and return an exception because there isn't any Curso with that SIGLA")
    void testUpdateCursoCursoNaoEncontradoException() throws Exception {
        this.entidade = createCursoEntidade();
        this.optional = Optional.empty();

        when(cursoRepositorioJpaSpring.findBySigla(Mockito.anyString())).thenReturn(this.optional);
        Exception thrown = assertThrows(CursoNaoEncontradoException.class, () -> {
            cursoRepositorio.updateCurso(SIGLA, entidade);
        });
        
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).isEqualTo("Curso não encontrado na base de dados");
    }

    @Test
    @DisplayName("Should delete a Curso successfully")
    void testDeleteCursoSuccess() throws Exception{
        this.optional = createOptionalCurso();

        when(cursoRepositorioJpaSpring.findBySigla(Mockito.anyString())).thenReturn(this.optional);
        doNothing().when(cursoRepositorioJpaSpring).deleteBySigla(Mockito.anyString());
        cursoRepositorio.deleteCurso(SIGLA);

        verify(cursoRepositorioJpaSpring, times(1)).deleteBySigla(Mockito.anyString());
    }

    @Test
    @DisplayName("Try to delete a Curso and return an excepiton because there isn't any Curso with that SIGLA")
    void testDeleteCursoCursoNaoEncontradoException() throws Exception{
        this.optional = Optional.empty();

        when(cursoRepositorioJpaSpring.findBySigla(Mockito.anyString())).thenReturn(this.optional);
        Exception thrown = assertThrows(CursoNaoEncontradoException.class, () -> {
            cursoRepositorio.deleteCurso("Curso que não existe");
        });
        
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).isEqualTo("Curso não encontrado na base de dados");
    }

    @Test
    @DisplayName("Should delete allCurso successfully")
    void testDeleteAllSuccess() {
        doNothing().when(cursoRepositorioJpaSpring).deleteAll();
        cursoRepositorio.deleteAll();

        verify(cursoRepositorioJpaSpring, times(1)).deleteAll();
    }


    private Optional<CursoEntidade> createOptionalCurso(){
        CursoEntidade curso = new CursoEntidade(ID, SIGLA, NOME, CARGA_HORARIA_COMPLEMENTAR, COORDENADOR);
    
        Optional<CursoEntidade> cursoOptional = Optional.ofNullable(curso);
        return cursoOptional;
    }

    private CursoEntidade createCursoEntidade(){
        CursoEntidade curso = new CursoEntidade(ID, SIGLA, NOME, CARGA_HORARIA_COMPLEMENTAR, COORDENADOR);
        return curso;
    }
}
