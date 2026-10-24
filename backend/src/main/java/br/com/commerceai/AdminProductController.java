package br.com.commerceai;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {
 private final ProductRepository products;
 private final OrderItemRepository orderItems;
 public AdminProductController(ProductRepository products, OrderItemRepository orderItems) {
  this.products = products;
  this.orderItems = orderItems;
 }

 public record ProductInput(
   @NotBlank @Size(max = 150) String name,
   @NotBlank @Size(max = 70) String category,
   @NotBlank @Size(max = 500) String description,
   @NotNull @DecimalMin("0.01") @Digits(integer=10,fraction=2) BigDecimal price,
   @NotNull @Min(0) Integer stock,
   @Size(max = 500) String imageUrl
 ) {}

 @GetMapping
 public List<Product> list() { return products.findAll(); }

 @PostMapping
 @ResponseStatus(HttpStatus.CREATED)
 public Product create(@Valid @RequestBody ProductInput input) {
  Product product = new Product();
  apply(product, input);
  return products.save(product);
 }

 @PutMapping("/{id}")
 @Transactional
 public Product update(@PathVariable Long id, @Valid @RequestBody ProductInput input) {
  Product product = products.findByIdForUpdate(id)
    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));
  apply(product, input);
  return products.save(product);
 }

 @DeleteMapping("/{id}")
 @ResponseStatus(HttpStatus.NO_CONTENT)
 @Transactional
 public void delete(@PathVariable Long id) {
  Product product = products.findByIdForUpdate(id)
    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));
  if (orderItems.existsByProductId(id)) {
   throw new ResponseStatusException(HttpStatus.CONFLICT, "Produto faz parte do histórico de pedidos");
  }
  try {
   products.delete(product);
   products.flush();
  } catch (DataIntegrityViolationException ex) {
   throw new ResponseStatusException(HttpStatus.CONFLICT, "Produto faz parte do histórico de pedidos");
  }
 }

 private static void apply(Product product, ProductInput input) {
  product.name = input.name().strip();
  product.category = input.category().strip();
  product.description = input.description().strip();
  product.price = input.price();
  product.stock = input.stock();
  product.imageUrl = input.imageUrl() == null || input.imageUrl().isBlank() ? null : input.imageUrl().strip();
 }
}
