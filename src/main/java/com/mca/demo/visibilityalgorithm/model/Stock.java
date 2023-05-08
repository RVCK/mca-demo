package com.mca.demo.visibilityalgorithm.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Stock {
    private String sizeId;
    private Integer quantity;
}
