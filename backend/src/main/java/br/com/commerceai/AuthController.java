package br.com.commerceai;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
 private final AppUserRepository users;
 private final PasswordEncoder encoder;
 private final String jwtSecret;
 public AuthController(AppUserRepository users,PasswordEncoder encoder,@Value("${JWT_SECRET:}") String jwtSecret){
  this.users=users;this.encoder=encoder;this.jwtSecret=jwtSecret;
 }
 public record Credentials(@NotBlank @Email String email,@NotBlank @Size(min=8,max=72) String password){}
 public record UserView(Long id,String email,String role){}
 public record Token(String accessToken,String tokenType,long expiresIn){}
 @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED)
 public UserView register(@Valid @RequestBody Credentials data) {
  String email=data.email().strip().toLowerCase(Locale.ROOT);
  if(users.findByEmailIgnoreCase(email).isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT,"E-mail já cadastrado");
  try {
   AppUser saved=users.saveAndFlush(new AppUser(email,encoder.encode(data.password())));
   return new UserView(saved.id,saved.email,saved.role);
  } catch(DataIntegrityViolationException e) {
   throw new ResponseStatusException(HttpStatus.CONFLICT,"E-mail já cadastrado");
  }
 }
 @PostMapping("/login")
 public Token login(@Valid @RequestBody Credentials data) {
  if(jwtSecret.length()<32) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"JWT_SECRET deve ter no mínimo 32 caracteres");
  AppUser user=users.findByEmailIgnoreCase(data.email().strip()).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Credenciais inválidas"));
  if(!user.active || !encoder.matches(data.password(),user.passwordHash)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Credenciais inválidas");
  String token=JWT.create().withIssuer("commerceai").withSubject(user.email)
    .withClaim("role",user.role).withIssuedAt(Instant.now())
    .withExpiresAt(Instant.now().plus(2,ChronoUnit.HOURS))
    .sign(Algorithm.HMAC256(jwtSecret));
  return new Token(token,"Bearer",7200);
 }
}
