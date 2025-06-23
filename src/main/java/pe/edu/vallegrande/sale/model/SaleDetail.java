package pe.edu.vallegrande.sale.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("sale_detail")
public class SaleDetail {

    @Id
    private Long id;

    @Column("sale_id")
    private Long saleId;

    @Column("product_id")
    private Long productId;

    private BigDecimal weight;

    private Integer packages;

    @Column("total_weight")
    private BigDecimal totalWeight;

    @Column("price_per_kg")
    private BigDecimal pricePerKg;

    @Column("total_price")
    private BigDecimal totalPrice;  
}
