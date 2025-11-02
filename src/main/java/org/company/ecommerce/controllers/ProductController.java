package org.company.ecommerce.controllers;

import org.company.ecommerce.dto.ProductResponse;
import org.company.ecommerce.models.Product;
import org.company.ecommerce.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    // GET /api/products?page=0&size=10
    @GetMapping
    public Page<ProductResponse> list(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size) {
        Page<Product> p = productService.listAll(page, size);
        return p.map(this::toResponse);
    }

    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable Long id) {
        return toResponse(productService.get(id));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody Product product) {
        Product created = productService.create(product);
        return ResponseEntity.created(URI.create("/api/products/" + created.getId())).body(toResponse(created));
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id, @RequestBody Product product) {
        return toResponse(productService.update(id, product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public Page<ProductResponse> search(@RequestParam(required = false) String name,
                                        @RequestParam(required = false) String category,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int size) {
        return productService.search(name, category, page, size).map(this::toResponse);
    }

    private ProductResponse toResponse(Product p) {
        ProductResponse r = new ProductResponse();
        r.setId(p.getId());
        r.setName(p.getName());
        r.setPrice(p.getPrice());
        r.setStock(p.getStock());
        r.setCategory(p.getCategory());
        r.setIsActive(p.getIsActive());
        r.setCreatedAt(p.getCreatedAt());
        return r;
    }
}
