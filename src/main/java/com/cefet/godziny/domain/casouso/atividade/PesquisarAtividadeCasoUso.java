package com.cefet.godziny.domain.casouso.atividade;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.cefet.godziny.api.atividade.AtividadeRecuperarDto;
import com.cefet.godziny.constantes.atividade.EnumStatus;
import com.cefet.godziny.constantes.usuario.EnumRecursos;
import com.cefet.godziny.infraestrutura.persistencia.atividade.AtividadeRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.curso.CursoRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioEntidade;
import com.cefet.godziny.infraestrutura.persistencia.atividade.AtividadeEntidade;
import com.cefet.godziny.infraestrutura.rest.atividade.AtividadeRestConverter;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
public class PesquisarAtividadeCasoUso {

    private final AtividadeRepositorioJpa atividadeRepositorioJpa;
    
    private final CursoRepositorioJpa cursoRepositorioJpa;

    @NotNull(message = "O nome do usuario é obrigatório")
    private String usuarioNome;

    @NotNull(message = "O titulo da atividade é obrigatória")
    private String titulo;

    @NotNull(message = "O status da atividade é obrigatório")
    private EnumStatus status;

    @NotNull(message = "O nome da categoria é obrigatória")
    private String categoria;
    
    public void validarPesquisa() throws Exception {}

    public Page<AtividadeRecuperarDto> pesquisarAtividade(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UsuarioEntidade userContext = (UsuarioEntidade) authentication.getPrincipal();

        Specification<AtividadeEntidade> specification = Specification.unrestricted();
    
        if (usuarioNome != null && userContext.getTipo().equals(EnumRecursos.ADM)) {
            specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("usuario").get("nome")), "%" + usuarioNome.toLowerCase() + "%"));
        }
        if (categoria != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("categoria").get("nome")), "%" + categoria.toLowerCase() + "%"));
        }
        if (titulo != null) {
            specification = specification.and((root, query, criteriaBuilder) -> 
                criteriaBuilder.like(root.get("titulo"), "%" + titulo + "%"));
        }
        if (status != null) {
            specification = specification.and((root, query, criteriaBuilder) -> 
                criteriaBuilder.equal(root.get("status"), status));
        }

        if(userContext.getTipo().equals(EnumRecursos.ADM)){
            List<UUID> cursoIds = cursoRepositorioJpa.findByCoordenador(userContext.getMatricula());
            specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.in(root.get("categoria").get("curso").get("id")).value(cursoIds));

        }
        else{
            specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("usuario").get("matricula"), userContext.getMatricula()));
        }

        Page<AtividadeRecuperarDto> pageAtividadeRecuperarDto = atividadeRepositorioJpa.listAtividades(specification, pageable)
            .map(AtividadeRestConverter::EntidadeToAtividadeRecuperarDto);
        
        return pageAtividadeRecuperarDto;
    }
    
}