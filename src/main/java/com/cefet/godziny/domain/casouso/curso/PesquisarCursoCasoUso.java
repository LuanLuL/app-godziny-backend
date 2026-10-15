package com.cefet.godziny.domain.casouso.curso;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.cefet.godziny.api.curso.CursoRecuperarDto;
import com.cefet.godziny.constantes.usuario.EnumRecursos;
import com.cefet.godziny.infraestrutura.exceptions.UsuarioNaoAutorizadoException;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoEntidade;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioEntidade;
import com.cefet.godziny.infraestrutura.rest.curso.CursoRestConverter;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
public class PesquisarCursoCasoUso {

    private final CursoRepositorioJpa cursoRepositorioJpa;

    @NotNull(message = "A sigla do curso é obrigatória")
    private String sigla;

    @NotNull(message = "O nome do curso é obrigatório")
    private String nome;

    public void validarPesquisa() throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UsuarioEntidade userContext = (UsuarioEntidade) authentication.getPrincipal();
        if(!userContext.getTipo().equals(EnumRecursos.ADM)){
            throw new UsuarioNaoAutorizadoException();
        }
    }
    
    public Page<CursoRecuperarDto> pesquisarCursos(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UsuarioEntidade admin = (UsuarioEntidade) authentication.getPrincipal();

        Specification<CursoEntidade> specification = Specification.where(null);
    
        if (nome != null) {
            specification = specification.and((root, query, criteriaBuilder) -> 
                criteriaBuilder.like(root.get("nome"), "%" + nome + "%"));
        }
        if (sigla != null) {
            specification = specification.and((root, query, criteriaBuilder) -> 
                criteriaBuilder.like(root.get("sigla"), "%" + sigla + "%"));
        }

        specification = specification.and((root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("coordenador").get("matricula"), admin.getMatricula()));

        Page<CursoRecuperarDto> pageCursoRecuperarDto = cursoRepositorioJpa.listCursos(specification, pageable)
            .map(CursoRestConverter::EntidadeToCursoRecuperarDto);
        
        return pageCursoRecuperarDto;
    }

}