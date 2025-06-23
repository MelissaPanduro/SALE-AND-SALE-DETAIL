package pe.edu.vallegrande.sale.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProductDto {
    private Long id;
    private String type;
    private String description;
    private BigDecimal packageWeight;
    private Integer stock;
    private LocalDate entryDate;
    private String typeProduct;
    private String status;
}