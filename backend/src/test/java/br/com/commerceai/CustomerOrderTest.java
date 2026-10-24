package br.com.commerceai;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
class CustomerOrderTest {
 @Test void totalizaItensComPrecisaoDecimal(){
  Product p=new Product();p.id=1L;p.name="Teclado";p.price=new BigDecimal("199.90");
  CustomerOrder order=new CustomerOrder("Cliente","cliente@example.com");
  order.add(p,2);
  assertEquals(new BigDecimal("399.80"),order.total);
  assertEquals(1,order.items.size());
  assertEquals(2,order.items.getFirst().quantity);
 }
}
