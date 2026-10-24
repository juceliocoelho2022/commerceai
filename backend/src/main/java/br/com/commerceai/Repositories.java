package br.com.commerceai;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.Optional;

interface ProductRepository extends JpaRepository<Product,Long> {
 List<Product> findByCategoryIgnoreCase(String category);

 // Bloqueio de linha válido durante a transação de checkout.
 @Lock(LockModeType.PESSIMISTIC_WRITE)
 @Query("select p from Product p where p.id = :id")
 Optional<Product> findByIdForUpdate(@Param("id") Long id);
}
interface OrderRepository extends JpaRepository<CustomerOrder,Long> {
 List<CustomerOrder> findAllByOrderByCreatedAtDescIdDesc(Pageable pageable);
 @EntityGraph(attributePaths = "items")
 @Query("select o from CustomerOrder o where o.id = :id")
 Optional<CustomerOrder> findDetailedById(@Param("id") Long id);
 interface CreatedSummary {
  long getCount();
  java.math.BigDecimal getValue();
 }
 @Query("select count(o) as count, coalesce(sum(o.total), 0) as value from CustomerOrder o where o.status = 'CREATED'")
 CreatedSummary aggregateCreated();
}

interface OrderItemRepository extends JpaRepository<OrderItem,Long> { boolean existsByProductId(Long productId); }
