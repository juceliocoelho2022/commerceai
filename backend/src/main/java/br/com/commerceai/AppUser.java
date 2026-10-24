package br.com.commerceai;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "app_users")
public class AppUser {
 @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
 @Column(nullable = false, unique = true, length = 200) public String email;
 @Column(name = "password_hash", nullable = false, length = 100) public String passwordHash;
 @Column(nullable = false, length = 20) public String role = "CUSTOMER";
 @Column(nullable = false) public boolean active = true;
 @Column(name = "created_at", insertable = false, updatable = false) public OffsetDateTime createdAt;
 protected AppUser() {}
 public AppUser(String email, String passwordHash) {
  this.email = email;
  this.passwordHash = passwordHash;
 }
}
