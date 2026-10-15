package com.cefet.godziny.infraestrutura.rest.curso;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import com.cefet.godziny.api.curso.CursoDto;
import com.cefet.godziny.api.curso.CursoFiltroDto;
import com.cefet.godziny.api.curso.CursoRecuperarDto;
import com.cefet.godziny.api.curso.ICursoApi;
import com.cefet.godziny.domain.casouso.curso.CriarCursoCasoUso;
import com.cefet.godziny.domain.casouso.curso.ListarCursoCasoUso;
import com.cefet.godziny.domain.casouso.curso.PesquisarCursoCasoUso;
import com.cefet.godziny.domain.casouso.curso.RemoverCursoCasoUso;
import com.cefet.godziny.domain.casouso.curso.AtualizarCursoCasoUso;
import com.cefet.godziny.infraestrutura.persistencia.categoria.CategoriaRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioRepositorioJpa;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class CursoControle implements ICursoApi{

    private final CursoRepositorioJpa cursoRepositorioJpa;

    private final UsuarioRepositorioJpa usuarioRepositorioJpa;

    private final CategoriaRepositorioJpa categoriaRepositorioJpa;

    @Override
    public ResponseEntity<CursoRecuperarDto> getCurso(String sigla) throws Exception {
        ListarCursoCasoUso casoUso = new ListarCursoCasoUso(cursoRepositorioJpa, sigla);
        return ResponseEntity.status(HttpStatus.OK).body(casoUso.validarListagem());
    }

    @Override
    public ResponseEntity<Page<CursoRecuperarDto>> pesquisarCursos(Pageable pageable, CursoFiltroDto cursoFiltroDto) throws Exception {
        PesquisarCursoCasoUso casoUso = new PesquisarCursoCasoUso(cursoRepositorioJpa, cursoFiltroDto.getSigla(), cursoFiltroDto.getNome());
        casoUso.validarPesquisa();
        return ResponseEntity.status(HttpStatus.OK).body(casoUso.pesquisarCursos(pageable));
    }

    @Override
    public ResponseEntity<String> createCurso(@Valid CursoDto dto) throws Exception{
        CriarCursoCasoUso casoUso = CursoRestConverter.DtoToCriarCursoCasoUso(dto, cursoRepositorioJpa, usuarioRepositorioJpa);
        return ResponseEntity.status(HttpStatus.CREATED).body(casoUso.createCurso(dto, casoUso.validarCriacao()));
    }

    @Override
    public ResponseEntity<String> updateCurso(String cursoSigla, @Valid CursoDto dto) throws Exception {
        AtualizarCursoCasoUso casoUso = CursoRestConverter.DtoToUpdateCursoCasoUso(cursoSigla, dto, cursoRepositorioJpa, usuarioRepositorioJpa);
        return ResponseEntity.status(HttpStatus.OK).body(casoUso.AtualizarCurso(cursoSigla, dto, casoUso.validarAtualizacao()));
    }

    @Override
    @Transactional
    public ResponseEntity<Void> removeCurso(String cursoSigla) throws Exception {
        RemoverCursoCasoUso casoUso = new RemoverCursoCasoUso(cursoRepositorioJpa, usuarioRepositorioJpa, categoriaRepositorioJpa, cursoSigla);
        casoUso.validarRemocao();
        casoUso.removerCurso();
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
