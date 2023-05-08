package com.mca.demo.visibilityalgorithm.service.impl;

import com.mca.demo.visibilityalgorithm.controller.VisibilityController;
import com.mca.demo.visibilityalgorithm.model.Product;
import com.mca.demo.visibilityalgorithm.model.Size;
import com.mca.demo.visibilityalgorithm.model.Stock;
import com.mca.demo.visibilityalgorithm.service.VisibilityService;
import com.mca.demo.visibilityalgorithm.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class VisibilityServiceImpl implements VisibilityService {

    Logger logger = LoggerFactory.getLogger(VisibilityController.class);

    private static Util util = new Util();

    @Override
    public List<String> productsVisibility() {

        //Init "database"
        List<Product> products = readProductFromCSV("src/main/resources/files/product.csv");
        List<Size> sizes = readSizeFromCSV("src/main/resources/files/size.csv");
        List<Stock> stock = readStockFromCSV("src/main/resources/files/stock.csv");
        //Case 1
        List<String> backSoonProducts = getBackSoonProducts(products, sizes);
        //Case 2
        List<String> specialProducts = getSpecialProducts(products, sizes, stock);

       return util.intersection(backSoonProducts, specialProducts);

    }

    @Cacheable(value = "backSoonProducts")
    private List<String> getBackSoonProducts(List<Product> products, List<Size> sizes) {
        List<String> backSoonProducts = new ArrayList<>();//products with backSoon=TRUE
        products.stream().forEach(currentProduct -> {
            Boolean backSoonMatch = sizes.stream().anyMatch(currentSize ->
                    currentSize.getProductId().trim().equals(currentProduct.getId()) && currentSize.isBackSoon()
            );
            if(backSoonMatch){
                backSoonProducts.add(currentProduct.getId());
            }
        });
        return backSoonProducts;
    }
    @Cacheable(value = "specialProducts")
    private List<String> getSpecialProducts(List<Product> products, List<Size> sizes, List<Stock> stock) {
        List<String> productsSpecialCase = new ArrayList<>();
        products.stream().forEach(currentProduct -> {
            List<Size> filteredByProduct = sizes.stream().filter(s ->
                            s.getProductId().trim().equals(currentProduct.getId().trim()))
                    .collect(Collectors.toList());
            //product has at least 1 special size
            Boolean specialCase = specialCaseWithStock(true, currentProduct, filteredByProduct, stock);
            //product has at least 1 special size OR back Soon flag
            Boolean nonSpecialCase =specialCaseWithStock(false, currentProduct, filteredByProduct, stock);

            if(specialCase && nonSpecialCase){
                logger.info("ProductID="+currentProduct.getId() + " with Special and Non-Special flag.");
                    productsSpecialCase.add(currentProduct.getId());
            }
        });
       return productsSpecialCase;
    }

    private Boolean specialCaseWithStock(Boolean special, Product currentProduct, List<Size> sizes, List<Stock> stock) {
        List<Size> filteredList = new ArrayList<>();
        if(special){
            List<Size> filteredSpecialCase = sizes.stream().filter(s -> s.isSpecial())
                    .collect(Collectors.toList());
            if(!filteredSpecialCase.isEmpty()) {
                logger.info("Special sizes in productID=" + currentProduct.getId().trim() + " --> " + filteredSpecialCase.toString());
            }
            filteredList = filteredSpecialCase;
        }else{
            List<Size> filteredNonSpecialCase = sizes.stream().filter(s ->(!s.isSpecial() || s.isBackSoon()))
                    .collect(Collectors.toList());
            if(!filteredNonSpecialCase.isEmpty()) {
                logger.info("NON Special sizes in productID=" + currentProduct.getId().trim() + " --> " + filteredNonSpecialCase.toString());
            }
            filteredList = filteredNonSpecialCase;

        }
       return evaluateStock(currentProduct.getId().trim(), filteredList, stock);

    }
    private boolean evaluateStock(String productId, List<Size> productSizes, List<Stock> stock) {
        AtomicReference<Boolean> sizeIdFound = new AtomicReference<>(false);

        productSizes.forEach(sizeId -> {
            Stock currentStock = stock.stream()
                    .filter(s -> s.getSizeId().equals(sizeId.getId().trim()) && s.getQuantity()>0)
                    .findAny()
                    .orElse(null);
            if(Objects.nonNull(currentStock)){
                sizeIdFound.set(true);
            }
        });
        return sizeIdFound.get();
    }

    private List<Stock> readStockFromCSV(String fileName) {
        List<Stock> stocks = new ArrayList<>();
        Path pathToFile = Paths.get(fileName);
        try (BufferedReader br = Files.newBufferedReader(pathToFile,
                StandardCharsets.US_ASCII)) {
            String line = br.readLine();
            while (line != null) {
                String[] attributes = line.split(",");
                Stock stock = createStock(attributes);
                stocks.add(stock);
                line = br.readLine();
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return stocks;
    }

    private List<Size> readSizeFromCSV(String fileName) {
        List<Size> sizes = new ArrayList<>();
        Path pathToFile = Paths.get(fileName);
        try (BufferedReader br = Files.newBufferedReader(pathToFile,
                StandardCharsets.US_ASCII)) {
            String line = br.readLine();
            while (line != null) {
                String[] attributes = line.split(",");
                Size size = createSize(attributes);
                sizes.add(size);
                line = br.readLine();
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return sizes;
    }

    private static List<Product> readProductFromCSV(String fileName){
        List<Product> products = new ArrayList<>();
        Path pathToFile = Paths.get(fileName);
        try (BufferedReader br = Files.newBufferedReader(pathToFile,
                StandardCharsets.US_ASCII)) {
            String line = br.readLine();
            while (line != null) {
                String[] attributes = line.split(",");
                Product product = createProduct(attributes);
                products.add(product);
                line = br.readLine();
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return products;
    }

    private static Product createProduct(String[] attributes) {
        List<String> lineCollection = Arrays.stream(attributes).collect(Collectors.toList());
        return Product.builder()
                .id(lineCollection.get(0))
                .sequence(lineCollection.get(1))
                .build();
    }

    private static Size createSize(String[] attributes) {
        List<String> lineCollection = Arrays.stream(attributes).collect(Collectors.toList());
        return Size.builder()
                .id(lineCollection.get(0))
                .productId(lineCollection.get(1))
                .backSoon(Boolean.parseBoolean(lineCollection.get(2).trim()))
                .special(Boolean.parseBoolean(lineCollection.get(3).trim()))
                .build();
    }

    private static Stock createStock(String[] attributes) {
        List<String> lineCollection = Arrays.stream(attributes).collect(Collectors.toList());
        return Stock.builder()
                .sizeId(lineCollection.get(0))
                .quantity(Integer.valueOf(lineCollection.get(1).trim()))
                .build();
    }
}

