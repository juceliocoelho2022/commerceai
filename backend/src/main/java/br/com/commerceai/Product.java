package br.com.commerceai;
import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity @Table(name="products") public class Product {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false) public String name;
 @Column(nullable=false) public String category;
 @Column(nullable=false) public String description;
 @Column(nullable=false) public BigDecimal price;
 @Column(nullable=false) public Integer stock;
 @Column(name="image_url") public String imageUrl;
 protected Product() {}
}
