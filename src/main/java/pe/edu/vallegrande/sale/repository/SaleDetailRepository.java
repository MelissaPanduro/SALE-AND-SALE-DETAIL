package pe.edu.vallegrande.sale.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.sale.model.SaleDetail;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

@Repository
public interface SaleDetailRepository extends ReactiveCrudRepository<SaleDetail, Long> {

    // Obtener todos los productos vendidos en una venta
    Flux<SaleDetail> findBySaleId(Long saleId);

    Mono<Void> deleteBySaleId(Long saleId); 
}
