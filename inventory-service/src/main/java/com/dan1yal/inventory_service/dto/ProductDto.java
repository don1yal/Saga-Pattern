package com.dan1yal.inventory_service.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDto {
    private String id;
    private String name;
    private Integer quantity;
    private BigDecimal price;
}
