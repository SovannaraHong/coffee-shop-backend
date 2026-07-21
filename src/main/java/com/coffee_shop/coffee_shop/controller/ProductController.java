package com.coffee_shop.coffee_shop.controller;

import com.coffee_shop.coffee_shop.dto.PageDTO;
import com.coffee_shop.coffee_shop.dto.request.ProductRequest;
import com.coffee_shop.coffee_shop.dto.response.ProductResponse;
import com.coffee_shop.coffee_shop.entity.Product;
import com.coffee_shop.coffee_shop.mapper.ProductMapper;
import com.coffee_shop.coffee_shop.service.ProductService;
import com.coffee_shop.coffee_shop.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController()
@RequestMapping("api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;
    private final S3Service s3Service;


    @GetMapping("/pagination")
    public ResponseEntity<PageDTO<ProductResponse>> getProducts(@RequestParam Map<String, String> params) {
        Page<ProductResponse> pagination = productService.getPagination(params);
        return ResponseEntity.ok().body(new PageDTO<>(pagination));

    }

    @PostMapping("/create")
    public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductRequest productRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(productRequest));
    }


    @PutMapping("/{id}/image")
    public ResponseEntity<?> uploadProductImage(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file
    ) throws Exception {

        Product product = productService.findById(id);

        // delete old image first, if it was S3-hosted
        if (product.getImageUrl() != null && product.getImageUrl().startsWith("https://")) {
            s3Service.deleteFile(product.getImageUrl());
        }

        String url = s3Service.uploadFile(file, "product_images");
        ProductResponse response = productService.updateImage(id, url);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


}
