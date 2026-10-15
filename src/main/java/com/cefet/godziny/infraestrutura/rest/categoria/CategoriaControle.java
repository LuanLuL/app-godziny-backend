package com.cefet.godziny.infraestrutura.rest.categoria;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.cefet.godziny.api.categoria.CategoriaDto;
import com.cefet.godziny.api.categoria.CategoriaFiltroDto;
import com.cefet.godziny.api.categoria.CategoriaRecuperarDto;
import com.cefet.godziny.api.categoria.ICategoriaApi;
import com.cefet.godziny.domain.casouso.categoria.AtualizarCategoriaCasoUso;
import com.cefet.godziny.domain.casouso.categoria.CriarCategoriaCasoUso;
import com.cefet.godziny.domain.casouso.categoria.ListarCategoriaCasoUso;
import com.cefet.godziny.domain.casouso.categoria.PesquisarCategoriaCasoUso;
import com.cefet.godziny.domain.casouso.categoria.RemoverCategoriaCasoUso;
import com.cefet.godziny.infraestrutura.persistencia.atividade.AtividadeRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.categoria.CategoriaRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoEntidade;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoRepositorioJpa;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class CategoriaControle implements ICategoriaApi {

    private final CategoriaRepositorioJpa categoriaRepositorioJpa;

    private final CursoRepositorioJpa cursoRepositorioJpa;

    private final AtividadeRepositorioJpa atividadeRepositorioJpa;

    @Override
    public ResponseEntity<CategoriaRecuperarDto> getCategoria(UUID id) throws Exception {
        ListarCategoriaCasoUso casoUso = new ListarCategoriaCasoUso(categoriaRepositorioJpa, id);
        return ResponseEntity.status(HttpStatus.OK).body(casoUso.validarListagem());
    }
    
    @Override
    public ResponseEntity<Page<CategoriaRecuperarDto>> pesquisarCategorias(Pageable pageable, CategoriaFiltroDto categoriaFiltroDto) throws Exception {
        PesquisarCategoriaCasoUso casoUso = new PesquisarCategoriaCasoUso(categoriaRepositorioJpa, categoriaFiltroDto.getCursoSigla(), categoriaFiltroDto.getNome());
        casoUso.validarPesquisa();
        return ResponseEntity.status(HttpStatus.OK).body(casoUso.pesquisarCategoria(pageable));
    }

    @Override
    public ResponseEntity<UUID> createCategoria(@Valid CategoriaDto dto) throws Exception {
        CriarCategoriaCasoUso casoUso = CategoriaRestConverter.DtoToCriarCategoriaCasoUso(dto, categoriaRepositorioJpa, cursoRepositorioJpa);
        CursoEntidade cursoEntidade = casoUso.validarCriacao();
        return ResponseEntity.status(HttpStatus.CREATED).body(casoUso.createCategoria(dto, cursoEntidade));
    }

    @Override
    public ResponseEntity<UUID> updateCategoria(UUID categoriaId, @Valid CategoriaDto dto) throws Exception {
        AtualizarCategoriaCasoUso casoUso = CategoriaRestConverter.DtoToUpdateCursoCasoUso(dto, categoriaRepositorioJpa, cursoRepositorioJpa);
        CursoEntidade cursoEntidade = casoUso.validarAtualizacao();
        return ResponseEntity.status(HttpStatus.OK).body(casoUso.atualizarCategoria(dto, cursoEntidade));
    }

    @Override
    @Transactional
    public ResponseEntity<Void> removeCategoria(UUID categoriaId) throws Exception {
        RemoverCategoriaCasoUso casoUso = new RemoverCategoriaCasoUso(categoriaRepositorioJpa, atividadeRepositorioJpa, categoriaId);
        casoUso.validarRemocao();
        casoUso.removerCategoria();
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
