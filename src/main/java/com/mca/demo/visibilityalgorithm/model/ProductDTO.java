package com.mca.demo.visibilityalgorithm.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProductDTO {
    List<String> productIDs;
}
