package com.coffee_shop.coffee_shop.controller;

import com.coffee_shop.coffee_shop.mapper.ProductMapper;
import com.coffee_shop.coffee_shop.service.ProductService;
import com.coffee_shop.coffee_shop.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;
    private final S3Service s3Service;


//    @GetMapping("/pagination")
//    public ResponseEntity<PageDTO<ProductResponse>> getProducts(@RequestParam Map<String, String> params) {
//        Page<ProductResponse> pagination = productService.getPagination(params);
//        return ResponseEntity.ok().body(new PageDTO<>(pagination));
//
//    }
//
//
//    @PutMapping("/{id}/image")
//    public ResponseEntity<?> uploadProductImage(
//            @PathVariable Long id,
//            @RequestPart("file") MultipartFile file
//    ) throws Exception {
//
//        Product product = productService.findById(id);
//
//        // delete old image first, if it was S3-hosted
//        if (product.getImageUrl() != null && product.getImageUrl().startsWith("https://")) {
//            s3Service.deleteFile(product.getImageUrl());
//        }
//
//        String url = s3Service.uploadFile(file, "product_images");
//        ProductResponse response = productService.updateImage(id, url);
//
//        return ResponseEntity.status(HttpStatus.OK).body(response);
//    }


}
