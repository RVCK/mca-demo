package com.mca.demo.visibilityalgorithm.controller;

import com.mca.demo.visibilityalgorithm.model.ProductDTO;
import com.mca.demo.visibilityalgorithm.service.VisibilityService;
//import jakarta.annotation.Nonnull;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping(value = "visibility")
public class VisibilityController {

    Logger logger = LoggerFactory.getLogger(VisibilityController.class);

    private final VisibilityService visibilityService;

    public VisibilityController(VisibilityService v){
        this.visibilityService=v;
    }

    @Operation(summary = "Execute visibility algorithm.", tags = {"Visibility Stock"})
    @PutMapping(produces = { "application/json"})
    public ResponseEntity<ProductDTO> getGenericContent() {
        logger.info("GET /visibility started.");
        List<String> sizesToVisualize = visibilityService.productsVisibility();
        ProductDTO productDTO = ProductDTO.builder()
                .productIDs(sizesToVisualize)
                .build();
        logger.info("GET /visibility ends with response: " + productDTO.getProductIDs().toString());
        return new ResponseEntity<>(productDTO, HttpStatus.OK);
    }

}
