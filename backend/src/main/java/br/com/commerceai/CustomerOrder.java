package br.com.commerceai;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
@Entity @Table(name="customer_orders") public class CustomerOrder {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(name="customer_name",nullable=false) public String customerName;
 @Column(name="customer_email",nullable=false) public String customerEmail;
 @Column(nullable=false) public String status="CREATED";
 @Column(nullable=false) public BigDecimal total=BigDecimal.ZERO;
 @Column(name="created_at",nullable=false,insertable=false,updatable=false) public OffsetDateTime createdAt;
 @OneToMany(mappedBy="order",cascade=CascadeType.ALL,orphanRemoval=true) public List<OrderItem> items=new ArrayList<>();
 protected CustomerOrder() {}
 public CustomerOrder(String name,String email){customerName=name;customerEmail=email;}
 public void add(Product product,int quantity){OrderItem item=new OrderItem(this,product,quantity);items.add(item);total=total.add(item.unitPrice.multiply(BigDecimal.valueOf(quantity)));}
}
