package com.cefet.godziny.api.usuario;

import lombok.*;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
@Getter
@Setter
public class UsuarioFiltroDto{
    
    private Integer matricula;
    
    private String cursoSigla;
    
    private String nome;
}
