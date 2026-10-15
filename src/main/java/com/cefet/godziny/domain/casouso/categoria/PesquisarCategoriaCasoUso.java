package com.cefet.godziny.domain.casouso.categoria;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.cefet.godziny.api.categoria.CategoriaRecuperarDto;
import com.cefet.godziny.constantes.usuario.EnumRecursos;
import com.cefet.godziny.infraestrutura.persistencia.categoria.CategoriaRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioEntidade;
import com.cefet.godziny.infraestrutura.persistencia.categoria.CategoriaEntidade;
import com.cefet.godziny.infraestrutura.rest.categoria.CategoriaRestConverter;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
public class PesquisarCategoriaCasoUso {

    private final CategoriaRepositorioJpa categoriaRepositorioJpa;

    @NotNull(message = "A sigla do curso é obrigatória")
    private String cursoSigla;

    @NotNull(message = "O nome da categoria é obrigatória")
    private String nome;
    
    public void validarPesquisa() throws Exception {}

    public Page<CategoriaRecuperarDto> pesquisarCategoria(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UsuarioEntidade userContext = (UsuarioEntidade) authentication.getPrincipal();

        Specification<CategoriaEntidade> specification = Specification.where(null);

        if (cursoSigla != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("curso").get("sigla")), "%" + cursoSigla.toLowerCase() + "%"));
        }
        if (nome != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get("nome"), "%" + nome + "%"));
        }

        if(userContext.getTipo().equals(EnumRecursos.ADM)){
            specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("curso").get("coordenador").get("matricula"), userContext.getMatricula()));
        }
        else{
            specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.in(root.get("curso").get("id")).value(userContext.getCurso().getId()));  
        }

        Page<CategoriaRecuperarDto> pageCategoriaRecuperarDto = categoriaRepositorioJpa.listCategorias(specification, pageable)
            .map(CategoriaRestConverter::EntidadeToCategoriaRecuperarDto);
        
        return pageCategoriaRecuperarDto;
    }
    
}