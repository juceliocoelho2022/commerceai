package br.com.commerceai;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Configuration
public class SecurityConfiguration {
 @Bean PasswordEncoder passwordEncoder(){ return new BCryptPasswordEncoder(); }
 @Bean SecurityFilterChain securityFilterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
  return http.csrf(csrf -> csrf.disable())
    .cors(Customizer.withDefaults())
    .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
    .authorizeHttpRequests(auth -> auth
      .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
      .requestMatchers("/actuator/health", "/api/auth/register", "/api/auth/login").permitAll()
      .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/recommendations", "/api/dashboard").permitAll()
      .requestMatchers(HttpMethod.POST, "/api/orders").permitAll()
      .requestMatchers("/api/admin/**").hasRole("ADMIN")
      .anyRequest().denyAll())
    .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
    .build();
 }

 @Bean JwtFilter jwtFilter(@Value("${JWT_SECRET:}") String secret) { return new JwtFilter(secret); }

 static class JwtFilter extends OncePerRequestFilter {
  private final String secret;
  JwtFilter(String secret){this.secret=secret;}
  @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain)
    throws ServletException,IOException {
    String authorization=request.getHeader("Authorization");
    if(authorization!=null && authorization.startsWith("Bearer ") && secret.length()>=32) {
     try {
      var jwt=JWT.require(Algorithm.HMAC256(secret)).withIssuer("commerceai").build()
        .verify(authorization.substring(7));
      String role=jwt.getClaim("role").asString();
      if(("ADMIN".equals(role)||"CUSTOMER".equals(role)) && jwt.getSubject()!=null) {
       SecurityContextHolder.getContext().setAuthentication(
         new UsernamePasswordAuthenticationToken(jwt.getSubject(),null,
          List.of(new SimpleGrantedAuthority("ROLE_"+role))));
      }
     } catch(JWTVerificationException ignored) { SecurityContextHolder.clearContext(); }
    }
    chain.doFilter(request,response);
  }
 }
}
