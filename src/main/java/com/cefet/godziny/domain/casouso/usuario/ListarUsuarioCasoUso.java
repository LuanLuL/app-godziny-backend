package com.cefet.godziny.domain.casouso.usuario;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.cefet.godziny.api.usuario.UsuarioRecuperarDto;
import com.cefet.godziny.constantes.usuario.EnumRecursos;
import com.cefet.godziny.infraestrutura.exceptions.UsuarioNaoAutorizadoException;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioEntidade;
import com.cefet.godziny.infraestrutura.persistencia.usuario.UsuarioRepositorioJpa;
import com.cefet.godziny.infraestrutura.rest.curso.CursoRestConverter;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
public class ListarUsuarioCasoUso {
    private final UsuarioRepositorioJpa usuarioRepositorioJpa;

    @NotNull(message = "A matricula do usuário é obrigatória")
    private Integer matricula;

    public UsuarioRecuperarDto validarListagem() throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UsuarioEntidade userContext = (UsuarioEntidade) authentication.getPrincipal();
        if(!userContext.getTipo().equals(EnumRecursos.ADM)){
            throw new UsuarioNaoAutorizadoException();
        }
        UsuarioEntidade usuarioEntidade = usuarioRepositorioJpa.findById(matricula);
        return createUsuarioRecuperarDto(usuarioEntidade);
    }

    private UsuarioRecuperarDto createUsuarioRecuperarDto(UsuarioEntidade usuarioEntidade){
        UsuarioRecuperarDto dto = new  UsuarioRecuperarDto();
        dto.setMatricula(usuarioEntidade.getMatricula());
        dto.setCurso(CursoRestConverter.EntidadeToCursoRecuperarDto(usuarioEntidade.getCurso()));
        dto.setNome(usuarioEntidade.getNome());
        dto.setEmail(usuarioEntidade.getEmail());
        dto.setSenha(usuarioEntidade.getSenha());
        dto.setTipo(usuarioEntidade.getTipo());
        dto.setCreatedAt(usuarioEntidade.getCreatedAt());
        dto.getMatricula();
        dto.getCurso();
        dto.getNome();
        dto.getEmail();
        dto.getSenha();
        dto.getTipo();
        dto.getCreatedAt();
        return dto;
    }
}