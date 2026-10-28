package br.com.lumi;

import static br.com.lumi.Models.*;

import jakarta.validation.Valid;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarias/{u}")
public class ApiController {
  final LumiService service;
  final LumiRepository repo;

  public ApiController(LumiService s, LumiRepository r) {
    service = s;
    repo = r;
  }

  @GetMapping("/hoje")
  public Object home(@PathVariable long u) {
    return service.home(u);
  }

  @GetMapping("/ciclo")
  public Object cycle(@PathVariable long u) {
    return service.cycle(u);
  }

  @GetMapping("/coach")
  public Object coach(@PathVariable long u) {
    return service.coach(u);
  }

  @GetMapping("/sinais")
  public Object signals(@PathVariable long u) {
    service.check(u);
    return repo.sinais(u);
  }

  @GetMapping("/sintomas")
  public Object symptoms(@PathVariable long u) {
    service.check(u);
    return repo.sintomas(u);
  }

  @GetMapping("/compromissos")
  public Object appointments(@PathVariable long u) {
    service.check(u);
    return repo.compromissos(u);
  }

  @PostMapping("/sintomas")
  @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
  public Object symptom(@PathVariable long u, @Valid @RequestBody Sintoma s) {
    return service.save(u, s);
  }

  @PostMapping("/sinais")
  @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
  public Object signal(@PathVariable long u, @Valid @RequestBody Sinal s) {
    return service.save(u, s);
  }

  @PostMapping("/menstruacoes")
  @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
  public Object period(@PathVariable long u, @Valid @RequestBody Menstruacao s) {
    return service.save(u, s);
  }

  @PostMapping("/compromissos")
  @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
  public Object appointment(@PathVariable long u, @Valid @RequestBody Compromisso s) {
    return service.save(u, s);
  }

  @PostMapping("/relatorio")
  public Object report(@PathVariable long u, @Valid @RequestBody Selecao s) {
    return service.report(u, s);
  }
}
