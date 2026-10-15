package com.cefet.godziny.api.categoria;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/categoria")
public interface ICategoriaApi {
    
    @GetMapping("/{categoriaId}")
    ResponseEntity<CategoriaRecuperarDto> getCategoria(@PathVariable(value = "categoriaId") UUID id) throws Exception;

    @PostMapping("/pesquisar")
    ResponseEntity<Page<CategoriaRecuperarDto>> pesquisarCategorias(
        @PageableDefault(page = 0, size = 10, sort = "nome", direction = Sort.Direction.ASC)
        Pageable pageable,
        @RequestBody @Valid CategoriaFiltroDto categoriaFiltroDto
    ) throws Exception;

    @PostMapping("")
    ResponseEntity<UUID> createCategoria(@RequestBody @Valid CategoriaDto dto) throws Exception;

    @PutMapping("/{categoriaId}")
    ResponseEntity<UUID> updateCategoria(@PathVariable(value = "categoriaId") UUID categoriaId, @RequestBody @Valid CategoriaDto dto) throws Exception;


    @DeleteMapping("/{categoriaId}")
    ResponseEntity<Void> removeCategoria(@PathVariable(value = "categoriaId") UUID categoriaId) throws Exception;
}