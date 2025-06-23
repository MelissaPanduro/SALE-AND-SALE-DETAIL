package pe.edu.vallegrande.sale.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("sale")
public class Sale {

    @Id
    private Long id;

    private LocalDate saleDate;

    private String name;

    private String ruc;

    private String address;
    
}
