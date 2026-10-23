package com.cefet.godziny.api.curso;

import lombok.*;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
@Getter
@Setter
public class CursoFiltroDto{
    
    private String sigla;
    
    private String nome;
}
