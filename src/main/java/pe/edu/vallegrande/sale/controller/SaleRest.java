package pe.edu.vallegrande.sale.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.sale.dto.SaleDto;
import pe.edu.vallegrande.sale.service.SaleService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/sales")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SaleRest {

    private final SaleService saleService;

    // Crear una venta con detalles (usa SaleDto)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<SaleDto> createSale(@RequestBody SaleDto saleDto) {
        return saleService.save(saleDto);
    }

    // Obtener todas las ventas con detalles
    @GetMapping
    public Flux<SaleDto> getAllSales() {
        return saleService.findAll();
    }

    // Obtener una venta con detalles por ID
    @GetMapping("/{id}")
    public Mono<ResponseEntity<SaleDto>> getSaleById(@PathVariable Long id) {
        return saleService.getById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // Eliminar una venta por ID
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteSale(@PathVariable Long id) {
        return saleService.delete(id);
    }

    // Actualizar una venta con detalles
    @PutMapping("/{id}")
    public Mono<ResponseEntity<SaleDto>> updateSale(@PathVariable Long id, @RequestBody SaleDto saleDto) {
        return saleService.updateSale(id, saleDto)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // Buscar una venta por documento
    @GetMapping("/ruc/{ruc}")
    public Flux<SaleDto> getByRuc(@PathVariable String ruc) {
        return saleService.getSalesByRuc(ruc);
    }

}
