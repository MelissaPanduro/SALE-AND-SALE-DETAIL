package pe.edu.vallegrande.sale.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.sale.dto.SaleDetailDto;
import pe.edu.vallegrande.sale.dto.SaleDto;
import pe.edu.vallegrande.sale.model.Sale;
import pe.edu.vallegrande.sale.model.SaleDetail;
import pe.edu.vallegrande.sale.repository.SaleRepository;
import pe.edu.vallegrande.sale.repository.SaleDetailRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final SaleDetailRepository saleDetailRepository;
    private final ProductoService productoService;
    private final ProductClient productClient; // 🔗 Cliente para comunicación

    public Mono<SaleDto> save(SaleDto saleDto) {
        Sale sale = Sale.builder()
                .saleDate(saleDto.getSaleDate())
                .name(saleDto.getName())
                .ruc(saleDto.getRuc())
                .address(saleDto.getAddress())
                .build();

        return saleRepository.save(sale).flatMap(savedSale -> {
            List<SaleDetailDto> details = saleDto.getDetails();

            return Flux.fromIterable(details)
                    .flatMap(detailDto -> {
                        Long productId = detailDto.getProductId();
                        int packages = detailDto.getPackages();
                        BigDecimal pricePerKg = detailDto.getPricePerKg(); // ✅ se toma del DTO

                        return productoService.getProductById(productId)
                                .flatMap(product -> {
                                    BigDecimal weight = product.getPackageWeight();
                                    BigDecimal totalWeight = weight.multiply(BigDecimal.valueOf(packages));
                                    BigDecimal totalPrice = totalWeight.multiply(pricePerKg);

                                    SaleDetail detail = SaleDetail.builder()
                                            .saleId(savedSale.getId())
                                            .productId(productId)
                                            .weight(weight)
                                            .packages(packages)
                                            .totalWeight(totalWeight)
                                            .pricePerKg(pricePerKg)
                                            .totalPrice(totalPrice)
                                            .build();

                                    return productoService.reduceStock(productId, packages)
                                            .then(saleDetailRepository.save(detail));
                                });
                    })
                    .then(Mono.just(savedSale))
                    .map(s -> {
                        saleDto.setId(s.getId());
                        return saleDto;
                    });
        });
    }

    public Mono<SaleDto> updateSale(Long id, SaleDto saleDto) {
        return saleRepository.findById(id)
                .flatMap(existingSale -> {
                    existingSale.setSaleDate(saleDto.getSaleDate());
                    existingSale.setName(saleDto.getName());
                    existingSale.setRuc(saleDto.getRuc());
                    existingSale.setAddress(saleDto.getAddress());
                    return saleRepository.save(existingSale);
                })
                .flatMap(savedSale -> saleDetailRepository.deleteBySaleId(savedSale.getId())
                        .thenMany(Flux.fromIterable(saleDto.getDetails())
                                .flatMap(detailDto -> {
                                    SaleDetail detail = SaleDetail.builder()
                                            .saleId(savedSale.getId())
                                            .productId(detailDto.getProductId())
                                            .weight(detailDto.getWeight())
                                            .packages(detailDto.getPackages())
                                            .totalWeight(detailDto.getTotalWeight())
                                            .pricePerKg(detailDto.getPricePerKg())
                                            .totalPrice(detailDto.getTotalPrice())
                                            .build();
                                    return saleDetailRepository.save(detail);
                                }))
                        .then(Mono.just(saleDto)));
    }

    public Mono<Void> delete(Long id) {
        return saleRepository.deleteById(id);
    }

    public Mono<SaleDto> getById(Long id) {
        return saleRepository.findById(id)
                .flatMap(sale -> saleDetailRepository.findBySaleId(sale.getId()).collectList()
                        .map(details -> {
                            SaleDto dto = new SaleDto();
                            dto.setId(sale.getId());
                            dto.setSaleDate(sale.getSaleDate());
                            dto.setName(sale.getName());
                            dto.setRuc(sale.getRuc());
                            dto.setAddress(sale.getAddress());

                            List<SaleDetailDto> detailDtos = details.stream().map(detail -> {
                                SaleDetailDto d = new SaleDetailDto();
                                d.setProductId(detail.getProductId());
                                d.setWeight(detail.getWeight());
                                d.setPackages(detail.getPackages());
                                d.setTotalWeight(detail.getTotalWeight());
                                d.setPricePerKg(detail.getPricePerKg());
                                d.setTotalPrice(detail.getTotalPrice());
                                return d;
                            }).toList();

                            dto.setDetails(detailDtos);
                            return dto;
                        }));
    }

    public Flux<SaleDto> findAll() {
        return saleRepository.findAll()
                .flatMap(sale -> getById(sale.getId()));
    }


    public Flux<SaleDto> getSalesByRuc(String ruc) {
    return saleRepository.findByRuc(ruc)
            .map(sale -> {
                SaleDto dto = new SaleDto();
                dto.setId(sale.getId());
                dto.setSaleDate(sale.getSaleDate());
                dto.setName(sale.getName());
                dto.setRuc(sale.getRuc());
                dto.setAddress(sale.getAddress());
                dto.setDetails(null); // no se incluyen los detalles
                return dto;
            });
    }


}
