package br.com.commerceai;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
interface ProductRepository extends JpaRepository<Product,Long>{List<Product> findByCategoryIgnoreCase(String category);}
interface OrderRepository extends JpaRepository<CustomerOrder,Long>{}
