package br.com.commerceai;

import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminOrdersController {
 private final OrderRepository orders;
 private final ProductRepository products;
 public AdminOrdersController(OrderRepository orders, ProductRepository products){
  this.orders=orders;this.products=products;
 }
 public record ItemView(Long productId,String productName,int quantity,BigDecimal unitPrice){}
 public record OrderView(Long id,String customerName,String customerEmail,String status,
     BigDecimal total,OffsetDateTime createdAt,List<ItemView> items){}
 public record Metrics(long ordersCreated,BigDecimal orderValueCreated,long products,
     long stockUnits,long lowStockProducts){}
 @Transactional(readOnly = true)
 @GetMapping("/orders")
 public List<OrderView> list(@RequestParam(defaultValue="20") int limit) {
  int safeLimit=Math.max(1,Math.min(100,limit));
  return orders.findAllByOrderByCreatedAtDescIdDesc(PageRequest.of(0,safeLimit))
      .stream().map(AdminOrdersController::view).toList();
 }
 @Transactional(readOnly = true)
 @GetMapping("/orders/{id}")
 public OrderView detail(@PathVariable Long id) {
  var order=orders.findDetailedById(id).orElseThrow(()->
   new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND,"Pedido não encontrado"));
  return view(order);
 }
 @GetMapping("/metrics")
 public Metrics metrics(){
  var allProducts=products.findAll();
  var summary=orders.aggregateCreated();
  long stockUnits=allProducts.stream().mapToLong(p->p.stock).sum();
  long low=allProducts.stream().filter(p->p.stock<=10).count();
  return new Metrics(summary.getCount(),summary.getValue(),allProducts.size(),
    stockUnits,low);
 }
 private static OrderView view(CustomerOrder order){
  return new OrderView(order.id,order.customerName,order.customerEmail,order.status,
   order.total,order.createdAt,order.items.stream().map(i->
    new ItemView(i.productId,i.productName,i.quantity,i.unitPrice)).toList());
 }
}
