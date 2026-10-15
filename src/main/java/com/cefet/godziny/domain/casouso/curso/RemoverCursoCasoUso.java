package com.cefet.godziny.domain.casouso.curso;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.cefet.godziny.infraestrutura.exceptions.curso.RemoverCursoComCategoriasException;
import com.cefet.godziny.infraestrutura.exceptions.curso.RemoverCursoComUsuariosException;
import com.cefet.godziny.infraestrutura.persistencia.categoria.CategoriaRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoEntidade;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioEntidade;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioRepositorioJpa;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
public class RemoverCursoCasoUso {

    private final CursoRepositorioJpa cursoRepositorioJpa;

    private final UsuarioRepositorioJpa usuarioRepositorioJpa;

    private final CategoriaRepositorioJpa categoriaRepositorioJpa;

    @NotNull(message = "A sigla do curso é obrigatória")
    private String sigla;

    public void validarRemocao() throws Exception {
        CursoEntidade curso = cursoRepositorioJpa.findBySigla(this.sigla);
        Pageable pageable = PageRequest.of(0, 1);
        Page<UsuarioEntidade> usuariosDoCurso = usuarioRepositorioJpa.listUsuariosByCurso(pageable, curso);
        if(!usuariosDoCurso.isEmpty()){
            throw new RemoverCursoComUsuariosException();
        }
        if(!(categoriaRepositorioJpa.findByCurso(curso).isEmpty())){
            throw new RemoverCursoComCategoriasException();
        }
    }

    public void removerCurso() throws Exception {
        cursoRepositorioJpa.deleteCurso(this.sigla);
    }
}
