package com.cefet.godziny.api.atividade;

import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import com.cefet.godziny.api.categoria.CategoriaRecuperarDto;
import com.cefet.godziny.api.usuario.UsuarioRecuperarDto;
import com.cefet.godziny.constantes.atividade.EnumStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
@Getter
@Setter
public class AtividadeRecuperarDto{
    @NotNull
    private UUID id;

    @NotNull
    @NotBlank
    private UsuarioRecuperarDto usuario;

    @NotNull
    @NotBlank
    private CategoriaRecuperarDto categoria;

    @NotNull
    @NotBlank
    private String titulo;

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @NotNull
    private EnumStatus status;

    @NotNull
    @NotNull
    private UUID arquivoId;

    private float cargaHoraria;

    private String comentario;
}

