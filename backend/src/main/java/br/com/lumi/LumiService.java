package br.com.lumi;

import static br.com.lumi.Models.*;
import static org.springframework.http.HttpStatus.*;

import java.time.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LumiService {
  final LumiRepository repo;
  final CoachRules rules;
  final LocalDate day;

  public LumiService(
      LumiRepository repo, CoachRules rules, @Value("${lumi.demo-date}") String day) {
    this.repo = repo;
    this.rules = rules;
    this.day = LocalDate.parse(day);
  }

  public LocalDate referenceDate() {
    return day;
  }

  void check(long u) {
    if (!repo.exists(u))
      throw new ResponseStatusException(NOT_FOUND, "Usuária fictícia não encontrada");
  }

  void past(LocalDate d) {
    if (d.isAfter(day))
      throw new ResponseStatusException(BAD_REQUEST, "Registro posterior à data da demonstração");
  }

  public Map<String, Object> home(long u) {
    check(u);
    var signals = repo.sinais(u);
    var m = new LinkedHashMap<String, Object>();
    m.put("usuaria", repo.user(u));
    m.put("dataReferencia", day);
    m.put("simulado", true);
    m.put("sinal", signals.stream().filter(s -> s.data().equals(day)).findFirst().orElse(null));
    m.put(
        "proximos",
        repo.compromissos(u).stream()
            .filter(c -> !c.data().toLocalDate().isBefore(day))
            .limit(3)
            .toList());
    m.put("totalSintomas", repo.sintomas(u).size());
    return m;
  }

  public Map<String, Object> cycle(long u) {
    check(u);
    var periods = repo.menstruacoes(u);
    var m = new LinkedHashMap<String, Object>();
    m.put("menstruacoes", periods);
    Object prediction = null;
    if (periods.size() >= 2) {
      var gaps = new ArrayList<Long>();
      for (int i = 1; i < periods.size(); i++)
        gaps.add(
            java.time.temporal.ChronoUnit.DAYS.between(
                periods.get(i - 1).inicio(), periods.get(i).inicio()));
      long avg = Math.round(gaps.stream().mapToLong(x -> x).average().orElseThrow());
      prediction =
          Map.of(
              "data",
              periods.getLast().inicio().plusDays(avg),
              "mediaDias",
              avg,
              "amostras",
              gaps.size(),
              "incerteza",
              "Estimativa pela média de poucos ciclos. Pode variar e ficar desatualizada sem novos"
                  + " registros. Não identifica ovulação e não serve para contracepção.");
    }
    m.put("previsao", prediction);
    return m;
  }

  public List<Insight> coach(long u) {
    check(u);
    return rules.calculate(day, repo.sinais(u), repo.sintomas(u), repo.compromissos(u));
  }

  @Transactional
  public Sintoma save(long u, Sintoma s) {
    check(u);
    past(s.data());
    return repo.add(u, s);
  }

  @Transactional
  public Sinal save(long u, Sinal s) {
    check(u);
    past(s.data());
    return repo.add(u, s);
  }

  @Transactional
  public Menstruacao save(long u, Menstruacao s) {
    check(u);
    past(s.inicio());
    past(s.fim());
    if (s.fim().isBefore(s.inicio()))
      throw new ResponseStatusException(BAD_REQUEST, "Fim anterior ao início");
    if (repo.menstruacoes(u).stream()
        .anyMatch(p -> !p.fim().isBefore(s.inicio()) && !p.inicio().isAfter(s.fim())))
      throw new ResponseStatusException(CONFLICT, "Período sobreposto a um registro existente");
    return repo.add(u, s);
  }

  @Transactional
  public Compromisso save(long u, Compromisso c) {
    check(u);
    return repo.add(u, c);
  }

  public Map<String, Object> report(long u, Selecao selection) {
    check(u);
    Set<String> allowed = Set.of("sinais", "sintomas", "ciclo", "compromissos", "insights");
    if (!allowed.containsAll(selection.incluir()))
      throw new ResponseStatusException(BAD_REQUEST, "Categoria inválida");
    var sections = new LinkedHashMap<String, Object>();
    for (String key : List.of("sinais", "sintomas", "ciclo", "compromissos", "insights"))
      if (selection.incluir().contains(key))
        sections.put(
            key,
            switch (key) {
              case "sinais" -> repo.sinais(u);
              case "sintomas" -> repo.sintomas(u);
              case "ciclo" -> cycle(u);
              case "compromissos" -> repo.compromissos(u);
              default -> coach(u);
            });
    return Map.of(
        "titulo",
        "Lumi • Resumo para consulta",
        "dataReferencia",
        day,
        "simulado",
        true,
        "aviso",
        "Persona e dados fictícios. Resumo descritivo, sem validação clínica; não é diagnóstico."
            + " Seleção autoriza apenas exportação local; nenhum dado é enviado à clínica.",
        "secoes",
        sections);
  }
}
