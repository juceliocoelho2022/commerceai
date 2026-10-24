package br.com.commerceai;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CheckoutStockTest {

 @Test void pedidoDeveBaixarEstoqueECalcularTotal() {
  ProductRepository products = mock(ProductRepository.class);
  OrderRepository orders = mock(OrderRepository.class);
  Product product = new Product();
  product.id = 4L;
  product.name = "Garrafa Térmica";
  product.price = new BigDecimal("89.90");
  product.stock = 5;

  when(products.findByIdForUpdate(4L)).thenReturn(Optional.of(product));
  when(orders.saveAndFlush(any(CustomerOrder.class))).thenAnswer(invocation -> {
   CustomerOrder order = invocation.getArgument(0);
   order.id = 10L;
   return order;
  });
  ApiController api = new ApiController(products, orders);
  var created = api.checkout(new ApiController.Checkout(
   "Cliente", "cliente@example.com", List.of(new ApiController.Line(4L, 2))
  ));
  assertEquals(HttpStatus.CREATED.value(), 201);
  assertEquals(new BigDecimal("179.80"), created.total());
  assertEquals(3, product.stock);
  verify(products).findByIdForUpdate(4L);
  verify(orders).saveAndFlush(any(CustomerOrder.class));
 }

 @Test void estoqueInsuficienteNaoPersistePedido() {
  ProductRepository products = mock(ProductRepository.class);
  OrderRepository orders = mock(OrderRepository.class);
  Product product = new Product();
  product.id = 4L;
  product.name = "Garrafa Térmica";
  product.price = new BigDecimal("89.90");
  product.stock = 1;
  when(products.findByIdForUpdate(4L)).thenReturn(Optional.of(product));

  ApiController api = new ApiController(products, orders);
  ResponseStatusException error = assertThrows(ResponseStatusException.class, () ->
   api.checkout(new ApiController.Checkout(
    "Cliente", "cliente@example.com", List.of(new ApiController.Line(4L, 2))
   ))
  );
  assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
  assertEquals(1, product.stock);
  verify(orders, never()).saveAndFlush(any(CustomerOrder.class));
 }
}
