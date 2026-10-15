package com.cefet.godziny.domain.casouso.categoria;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.cefet.godziny.api.categoria.CategoriaRecuperarDto;
import com.cefet.godziny.constantes.usuario.EnumRecursos;
import com.cefet.godziny.infraestrutura.exceptions.UsuarioNaoAutorizadoException;
import com.cefet.godziny.infraestrutura.persistencia.categoria.CategoriaEntidade;
import com.cefet.godziny.infraestrutura.persistencia.categoria.CategoriaRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioEntidade;
import com.cefet.godziny.infraestrutura.rest.categoria.CategoriaRestConverter;
import com.cefet.godziny.infraestrutura.rest.curso.CursoRestConverter;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ListarCategoriaCasoUso {

    private final CategoriaRepositorioJpa categoriaRepositorioJpa;

    @NotNull(message = "O ID da categoria é obrigatório")
    private UUID id;

    public CategoriaRecuperarDto validarListagem() throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UsuarioEntidade userContext = (UsuarioEntidade) authentication.getPrincipal();
        if(!userContext.getTipo().equals(EnumRecursos.ADM)){
            throw new UsuarioNaoAutorizadoException();
        }

        CategoriaEntidade entidade = categoriaRepositorioJpa.findById(this.id);
        createCategoriaRecuperarDto(entidade);
        return CategoriaRestConverter.EntidadeToCategoriaRecuperarDto(entidade);
    }
    
    private CategoriaRecuperarDto createCategoriaRecuperarDto(CategoriaEntidade categoriaEntidade){
        CategoriaRecuperarDto dto = new  CategoriaRecuperarDto();
        dto.setId(categoriaEntidade.getId());
        dto.setCurso(CursoRestConverter.EntidadeToCursoDto(categoriaEntidade.getCurso()));
        dto.setCurso(null);
        dto.setNome(categoriaEntidade.getNome());
        dto.setPorcentagemHorasMaximas(categoriaEntidade.getPorcentagemHorasMaximas());
        dto.setHorasMultiplicador(categoriaEntidade.getHorasMultiplicador());
        dto.setDescricao(categoriaEntidade.getDescricao());
        dto.getId();
        dto.getCurso();
        dto.getNome();
        dto.getPorcentagemHorasMaximas();
        dto.getHorasMultiplicador();
        dto.getDescricao();
        return dto;
    }
}
