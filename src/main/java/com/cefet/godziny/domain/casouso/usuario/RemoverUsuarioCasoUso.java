package com.cefet.godziny.domain.casouso.usuario;

import com.cefet.godziny.infraestrutura.exceptions.usuario.RemoverUsuarioComAtividadesException;
import com.cefet.godziny.infraestrutura.persistencia.atividade.AtividadeRepositorioJpa;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioEntidade;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioRepositorioJpa;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
public class RemoverUsuarioCasoUso {
    @NotNull(message = "A matrícula do usuario é obrigatório")
    private Integer matricula;

    private final UsuarioRepositorioJpa usuarioRepositorioJpa;

    private final AtividadeRepositorioJpa atividadeRepositorioJpa;

    public void validarRemocao() throws Exception {
        UsuarioEntidade usuario = usuarioRepositorioJpa.findById(this.matricula);
        if(!(atividadeRepositorioJpa.findByUsuario(usuario).isEmpty())){
            throw new RemoverUsuarioComAtividadesException();
        }
    }

    public void removerUsuario() throws Exception {
        usuarioRepositorioJpa.deleteUsuario(this.matricula);
    }
}
