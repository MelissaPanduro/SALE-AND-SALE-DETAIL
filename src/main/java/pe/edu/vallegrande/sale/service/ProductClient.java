package pe.edu.vallegrande.sale.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.vallegrande.sale.dto.ProductDto;
import pe.edu.vallegrande.sale.exception.ProductNotFoundException;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ProductClient {

    private final WebClient webClient;

    @Value("${microservices.product-service.url}")
    private String productServiceUrl;

    public Mono<ProductDto> getProductById(Long productId) {
        return webClient.get()
                .uri(productServiceUrl + "/NPH/products/{id}", productId)
                .retrieve()
                .bodyToMono(ProductDto.class)
                .onErrorMap(throwable -> new ProductNotFoundException("Producto no encontrado: " + productId));
    }

    public Mono<Void> reduceStock(Long productId, int quantity) {
        return webClient.put()
                .uri(productServiceUrl + "/NPH/products/reduce-stock/{id}?quantity={quantity}", productId, quantity)
                .retrieve()
                .bodyToMono(Void.class);
    }
}