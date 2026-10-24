package br.com.commerceai;
import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity @Table(name="order_items") public class OrderItem {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @ManyToOne(optional=false) @JoinColumn(name="order_id") public CustomerOrder order;
 @Column(name="product_id",nullable=false) public Long productId;
 @Column(name="product_name",nullable=false) public String productName;
 @Column(nullable=false) public int quantity;
 @Column(name="unit_price",nullable=false) public BigDecimal unitPrice;
 protected OrderItem(){}
 OrderItem(CustomerOrder order,Product product,int quantity){this.order=order;productId=product.id;productName=product.name;this.quantity=quantity;unitPrice=product.price;}
}
