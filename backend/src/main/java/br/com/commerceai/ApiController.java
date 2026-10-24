package br.com.commerceai;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;
@RestController @RequestMapping("/api") public class ApiController {
 private final ProductRepository products; private final OrderRepository orders;
 public ApiController(ProductRepository products,OrderRepository orders){this.products=products;this.orders=orders;}
 public record Line(@NotNull Long productId,@NotNull @Min(1) Integer quantity){}
 public record Checkout(@NotBlank String customerName,@NotBlank @Email String customerEmail,@NotEmpty List<@Valid Line> items){}
 public record OrderView(Long id,String status,BigDecimal total){}
 public record Dashboard(long products,long orders,BigDecimal revenue,long lowStock){}
 @GetMapping("/products") public List<Product> products(@RequestParam(required=false) String category){return category==null||category.isBlank()?products.findAll():products.findByCategoryIgnoreCase(category);}
 @GetMapping("/products/{id}") public Product product(@PathVariable Long id){return products.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Produto não encontrado"));}
 @PostMapping("/orders") @ResponseStatus(HttpStatus.CREATED) @Transactional public OrderView checkout(@Valid @RequestBody Checkout request){
  CustomerOrder order=new CustomerOrder(request.customerName(),request.customerEmail());
  Map<Long,Integer> grouped=new LinkedHashMap<>();
  for(Line line:request.items()){grouped.merge(line.productId(),line.quantity(),(a,b)->Math.addExact(a,b));}
  List<Long> ids=new ArrayList<>(grouped.keySet());Collections.sort(ids);
  for(Long id:ids){Product p=products.findByIdForUpdate(id).orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST,"Produto inválido: "+id));int q=grouped.get(id);if(q>p.stock)throw new ResponseStatusException(HttpStatus.CONFLICT,"Estoque insuficiente: "+p.name);p.stock-=q;order.add(p,q);}
  CustomerOrder saved=orders.saveAndFlush(order);return new OrderView(saved.id,saved.status,saved.total);
 }
 @GetMapping("/dashboard") public Dashboard dashboard(){List<Product> all=products.findAll();List<CustomerOrder> allOrders=orders.findAll();return new Dashboard(all.size(),allOrders.size(),allOrders.stream().map(o->o.total).reduce(BigDecimal.ZERO,BigDecimal::add),all.stream().filter(p->p.stock<=10).count());}
 @GetMapping("/recommendations") public List<Product> recommend(@RequestParam(required=false) String category){List<Product> all=products.findAll().stream().filter(p->p.stock>0).toList();return all.stream().sorted(Comparator.<Product>comparingInt(p->category!=null&&p.category.equalsIgnoreCase(category)?0:1).thenComparing((Product p)->-p.stock).thenComparing(p->p.id)).limit(4).toList();}
}
