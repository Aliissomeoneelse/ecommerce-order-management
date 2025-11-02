package org.company.ecommerce.services;

import org.company.ecommerce.dto.ProductResponse;
import org.company.ecommerce.models.Product;
import org.company.ecommerce.repository.ProductRepository;
import org.company.ecommerce.exceptions.ProductNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public Page<Product> listAll(int page, int size) {
        Pageable p = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return productRepository.findByIsActiveTrue(p);
    }

    public Product get(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }

    public Product create(Product product) {
        log.info("Creating new product: {}", product.getName());
        product.setCreatedAt(LocalDateTime.now());
        if (product.getIsActive() == null) product.setIsActive(true);
        Product saved = productRepository.save(product);
        log.debug("Product created with ID: {}", saved.getId());
        return saved;
    }

    public Product update(Long id, Product update) {
        Product p = get(id);
        p.setName(update.getName());
        p.setPrice(update.getPrice());
        p.setStock(update.getStock());
        p.setCategory(update.getCategory());
        p.setIsActive(update.getIsActive());
        p.setUpdatedAt(LocalDateTime.now());
        return productRepository.save(p);
    }

    public void delete(Long id) {
        Product p = get(id);
        log.warn("Deactivating product with ID: {}", id);
        p.setIsActive(false);
        p.setDeletedAt(LocalDateTime.now());
        productRepository.save(p);
        log.info("Product {} marked as deleted", id);
    }

    public Page<Product> search(String name, String category, int page, int size) {
        Pageable p = PageRequest.of(page, size, Sort.by("createdAt").descending());
        if (name == null) name = "";
        if (category == null) category = "";
        return productRepository.findByNameContainingIgnoreCaseAndCategoryContainingIgnoreCase(name, category, p);
    }
}
