package br.com.commerceai;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
interface AppUserRepository extends JpaRepository<AppUser,Long> {
 Optional<AppUser> findByEmailIgnoreCase(String email);
}
