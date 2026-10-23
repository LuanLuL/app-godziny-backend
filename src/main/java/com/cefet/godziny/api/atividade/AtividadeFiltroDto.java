package com.cefet.godziny.api.atividade;

import com.cefet.godziny.constantes.atividade.EnumStatus;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
@Getter
@Setter
public class AtividadeFiltroDto {

    private String usuarioNome;
    
    private String titulo;

    private EnumStatus status;

    private String categoria;
}
