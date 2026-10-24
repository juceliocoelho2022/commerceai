package br.com.commerceai;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminProductControllerTest {
 private final ProductRepository products = mock(ProductRepository.class);
 private final OrderItemRepository orderItems = mock(OrderItemRepository.class);
 private final AdminProductController controller = new AdminProductController(products, orderItems);

 private AdminProductController.ProductInput input() {
  return new AdminProductController.ProductInput("Teclado", "Eletrônicos", "Teclado para escritório",
    new BigDecimal("120.00"), 10, null);
 }

 @Test void cadastraProdutoComDadosValidadosNaBorda() {
  when(products.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));
  Product created = controller.create(input());
  assertEquals("Teclado", created.name);
  assertEquals(new BigDecimal("120.00"), created.price);
  assertEquals(10, created.stock);
 }

 @Test void atualizaProdutoExistente() {
  Product existing = new Product(); existing.id=7L; existing.stock=3;
  when(products.findByIdForUpdate(7L)).thenReturn(Optional.of(existing));
  when(products.save(existing)).thenReturn(existing);
  Product updated=controller.update(7L,input());
  assertEquals(10,updated.stock);
  verify(products).findByIdForUpdate(7L);
 }

 @Test void impedeExclusaoDeProdutoVinculadoAPedido() {
  Product existing = new Product(); existing.id=7L;
  when(products.findByIdForUpdate(7L)).thenReturn(Optional.of(existing));
  when(orderItems.existsByProductId(7L)).thenReturn(true);
  ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> controller.delete(7L));
  assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
  verify(products,never()).delete(any(Product.class));
 }

 @Test void retorna404ParaProdutoInexistente() {
  when(products.findByIdForUpdate(404L)).thenReturn(Optional.empty());
  ResponseStatusException exception=assertThrows(ResponseStatusException.class,
    () -> controller.update(404L,input()));
  assertEquals(HttpStatus.NOT_FOUND,exception.getStatusCode());
 }
}
