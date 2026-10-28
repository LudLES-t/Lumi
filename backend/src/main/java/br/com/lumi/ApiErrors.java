package br.com.lumi;

import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiErrors {
  @ExceptionHandler(ResponseStatusException.class)
  ResponseEntity<?> status(ResponseStatusException e) {
    return ResponseEntity.status(e.getStatusCode())
        .body(Map.of("mensagem", e.getReason() == null ? "Requisição inválida" : e.getReason()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<?> validation(MethodArgumentNotValidException e) {
    return ResponseEntity.badRequest()
        .body(
            Map.of(
                "mensagem",
                "Confira os campos: "
                    + e.getBindingResult().getFieldErrors().stream()
                        .map(x -> x.getField() + " " + x.getDefaultMessage())
                        .toList()));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  ResponseEntity<?> json() {
    return ResponseEntity.badRequest().body(Map.of("mensagem", "JSON, data ou tipo inválido"));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<?> duplicate() {
    return ResponseEntity.status(409)
        .body(Map.of("mensagem", "Registro já existente ou conflito de dados"));
  }
}
