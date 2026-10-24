package br.com.commerceai;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;
@RestControllerAdvice public class ApiErrors {
 @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<Map<String,String>> invalid(MethodArgumentNotValidException e){return ResponseEntity.badRequest().body(Map.of("error","Dados inválidos para o pedido"));}
 @ExceptionHandler(ArithmeticException.class) ResponseEntity<Map<String,String>> overflow(ArithmeticException e){return ResponseEntity.badRequest().body(Map.of("error","Quantidade inválida"));}
}
