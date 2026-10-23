package com.cefet.godziny.api.categoria;

import lombok.*;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
@Getter
@Setter
public class CategoriaFiltroDto {

    private String cursoSigla;
    
    private String nome;
}
