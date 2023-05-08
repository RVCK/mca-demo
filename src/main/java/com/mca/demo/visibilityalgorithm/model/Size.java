package com.mca.demo.visibilityalgorithm.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Size {
    private String id;
    private String productId;
    private boolean backSoon;
    private boolean special;

}
