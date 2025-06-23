package pe.edu.vallegrande.sale.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class SaleDto {
    private Long id;
    private LocalDate saleDate;
    private String name;
    private String ruc;
    private String address;
    private List<SaleDetailDto> details;
}
