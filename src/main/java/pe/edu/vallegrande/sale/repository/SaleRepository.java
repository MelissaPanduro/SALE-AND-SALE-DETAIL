package pe.edu.vallegrande.sale.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.sale.model.Sale;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Repository
public interface SaleRepository extends ReactiveCrudRepository<Sale, Long> {

    Flux<Sale> findByName(String name);

    Flux<Sale> findBySaleDateBetween(LocalDate startDate, LocalDate endDate);

    Flux<Sale> findByRuc(String ruc);

}
