package com.cefet.godziny.domain.casouso.categoria;

import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;
import com.cefet.godziny.constantes.usuario.EnumRecursos;
import com.cefet.godziny.infraestrutura.exceptions.UsuarioNaoAutorizadoException;
import com.cefet.godziny.infraestrutura.exceptions.categoria.RemoverCategoriaComAtividadesException;
import com.cefet.godziny.infraestrutura.persistencia.atividade.AtividadeRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.categoria.CategoriaEntidade;
import com.cefet.godziny.infraestrutura.persistencia.categoria.CategoriaRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioEntidade;
import org.springframework.security.core.Authentication;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
public class RemoverCategoriaCasoUso {

    private final CategoriaRepositorioJpa categoriaRepositorioJpa;

    private final AtividadeRepositorioJpa atividadeRepositorioJpa;

    @NotNull(message = "O ID da categoria é obrigatório")
    private UUID id;

    public void validarRemocao() throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UsuarioEntidade userContext = (UsuarioEntidade) authentication.getPrincipal();
        if(!userContext.getTipo().equals(EnumRecursos.ADM)){
            throw new UsuarioNaoAutorizadoException();
        }
        CategoriaEntidade categoria = categoriaRepositorioJpa.findById(this.id);
        if(!(atividadeRepositorioJpa.findByCategoria(categoria).isEmpty())){
            throw new RemoverCategoriaComAtividadesException();
        }
    }

    public void removerCategoria() throws Exception {
        categoriaRepositorioJpa.deleteCategoria(this.id);
    }
}
