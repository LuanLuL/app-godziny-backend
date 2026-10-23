package com.cefet.godziny.api.usuario;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.cefet.godziny.api.curso.CursoRecuperarDto;
import com.cefet.godziny.constantes.usuario.EnumRecursos;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class UsuarioRecuperarDto{
    @NotNull
    @Positive
    private Integer matricula;

    private CursoRecuperarDto curso;
    
    @NotNull
    @NotBlank
    private String nome;

    @NotNull
    @NotBlank
    private String email;

    @NotNull
    @NotBlank
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String senha;

    @NotNull
    private EnumRecursos tipo;

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
