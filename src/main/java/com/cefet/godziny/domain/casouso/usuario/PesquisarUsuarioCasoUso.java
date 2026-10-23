package com.cefet.godziny.domain.casouso.usuario;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.cefet.godziny.api.usuario.UsuarioRecuperarDto;
import com.cefet.godziny.constantes.usuario.EnumRecursos;
import com.cefet.godziny.infraestrutura.exceptions.UsuarioNaoAutorizadoException;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioEntidade;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioRepositorioJpa;
import com.cefet.godziny.infraestrutura.rest.usuario.UsuarioRestConverter;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
public class PesquisarUsuarioCasoUso {
    private final UsuarioRepositorioJpa usuarioRepositorioJpa;

    private final CursoRepositorioJpa cursoRepositorioJpa;

    @NotNull(message = "A matricula do usuário é obrigatória")
    private Integer matricula;

    @NotNull(message = "A sigla do curso do usuário é obrigatória")
    private String cursoSigla;

    @NotNull(message = "O nome do usuário é obrigatório")
    private String nome;

    public void validarPesquisa() throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UsuarioEntidade userContext = (UsuarioEntidade) authentication.getPrincipal();
        if(!userContext.getTipo().equals(EnumRecursos.ADM)){
            throw new UsuarioNaoAutorizadoException();
        }
    }
    
    public Page<UsuarioRecuperarDto> pesquisarUsuarios(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UsuarioEntidade admin = (UsuarioEntidade) authentication.getPrincipal();
        List<UUID> cursoIds = cursoRepositorioJpa.findByCoordenador(admin.getMatricula());

        Specification<UsuarioEntidade> specification = Specification.unrestricted();
        if (nome != null) {
            specification = specification.and((root, query, criteriaBuilder) -> 
                criteriaBuilder.like(root.get("nome"), "%" + nome + "%"));
        }
        if (matricula != null) {
            specification = specification.and((root, query, criteriaBuilder) -> 
                criteriaBuilder.equal(root.get("matricula"), matricula));
        }
        if (cursoSigla != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("curso").get("sigla")), "%" + cursoSigla.toLowerCase() + "%"));
        }

        specification = specification.and((root, query, criteriaBuilder) ->
            root.get("curso").get("id").in(cursoIds)
        );
    
        Page<UsuarioRecuperarDto> pageUsuarioRecuperarDto = usuarioRepositorioJpa.listUsuarios(specification, pageable)
            .map(UsuarioRestConverter::EntidadeToUsuarioRecuperarDto);
        
        return pageUsuarioRecuperarDto;
    }

}