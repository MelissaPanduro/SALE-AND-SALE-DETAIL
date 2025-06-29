// SaleServiceTest.java
package pe.edu.vallegrande.sale.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.vallegrande.sale.dto.ProductDto;
import pe.edu.vallegrande.sale.dto.SaleDetailDto;
import pe.edu.vallegrande.sale.dto.SaleDto;
import pe.edu.vallegrande.sale.model.Sale;
import pe.edu.vallegrande.sale.model.SaleDetail;
import pe.edu.vallegrande.sale.repository.SaleRepository;
import pe.edu.vallegrande.sale.repository.SaleDetailRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private SaleDetailRepository saleDetailRepository;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private SaleService saleService;

    private SaleDto saleDto;
    private Sale sale;
    private ProductDto productDto;
    private SaleDetail saleDetail;

    @BeforeEach
    void setUp() {
        // Configurar datos de prueba
        saleDto = new SaleDto();
        saleDto.setId(1L);
        saleDto.setSaleDate(LocalDate.now());
        saleDto.setName("Juan Pérez");
        saleDto.setRuc("12345678901");
        saleDto.setAddress("Av. Lima 123");

        SaleDetailDto detailDto = new SaleDetailDto();
        detailDto.setProductId(1L);
        detailDto.setPackages(2);
        detailDto.setPricePerKg(BigDecimal.valueOf(5.50));
        saleDto.setDetails(List.of(detailDto));

        sale = Sale.builder()
                .id(1L)
                .saleDate(LocalDate.now())
                .name("Juan Pérez")
                .ruc("12345678901")
                .address("Av. Lima 123")
                .build();

        productDto = new ProductDto();
        productDto.setId(1L);
        productDto.setPackageWeight(BigDecimal.valueOf(2.5));
        productDto.setStock(10);

        saleDetail = SaleDetail.builder()
                .id(1L)
                .saleId(1L)
                .productId(1L)
                .weight(BigDecimal.valueOf(2.5))
                .packages(2)
                .totalWeight(BigDecimal.valueOf(5.0))
                .pricePerKg(BigDecimal.valueOf(5.50))
                .totalPrice(BigDecimal.valueOf(27.50))
                .build();
    }

    @Test
    void testSaveSuccess() {
        // Given
        when(saleRepository.save(any(Sale.class))).thenReturn(Mono.just(sale));
        when(productClient.getProductById(anyLong())).thenReturn(Mono.just(productDto));
        when(productClient.reduceStock(anyLong(), anyInt())).thenReturn(Mono.empty());
        when(saleDetailRepository.save(any(SaleDetail.class))).thenReturn(Mono.just(saleDetail));

        // When & Then
        StepVerifier.create(saleService.save(saleDto))
                .expectNextMatches(result -> {
                    return result.getId().equals(1L) &&
                           result.getName().equals("Juan Pérez") &&
                           result.getRuc().equals("12345678901");
                })
                .verifyComplete();

        verify(saleRepository).save(any(Sale.class));
        verify(productClient).getProductById(1L);
        verify(productClient).reduceStock(1L, 2);
        verify(saleDetailRepository).save(any(SaleDetail.class));
    }

    @Test
    void testGetByIdSuccess() {
        // Given
        when(saleRepository.findById(1L)).thenReturn(Mono.just(sale));
        when(saleDetailRepository.findBySaleId(1L)).thenReturn(Flux.just(saleDetail));

        // When & Then
        StepVerifier.create(saleService.getById(1L))
                .expectNextMatches(result -> {
                    return result.getId().equals(1L) &&
                           result.getDetails().size() == 1 &&
                           result.getDetails().get(0).getProductId().equals(1L);
                })
                .verifyComplete();

        verify(saleRepository).findById(1L);
        verify(saleDetailRepository).findBySaleId(1L);
    }

    @Test
    void testGetByIdNotFound() {
        // Given
        when(saleRepository.findById(1L)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(saleService.getById(1L))
                .verifyComplete();

        verify(saleRepository).findById(1L);
        verify(saleDetailRepository, never()).findBySaleId(anyLong());
    }

}

