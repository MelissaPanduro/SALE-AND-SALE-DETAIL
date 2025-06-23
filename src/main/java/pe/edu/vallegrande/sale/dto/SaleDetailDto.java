package pe.edu.vallegrande.sale.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SaleDetailDto {
    private Long productId;
    private BigDecimal weight;
    private Integer packages;
    private BigDecimal totalWeight;
    private BigDecimal pricePerKg;
    private BigDecimal totalPrice;
}
