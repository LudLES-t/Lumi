package br.com.lumi;

import static br.com.lumi.Models.*;

import java.time.*;
import java.util.*;
import java.util.stream.*;
import org.springframework.stereotype.Component;

@Component
public class CoachRules {
  public static final String LIMIT =
      "Dados fictícios. Regra demonstrativa, sem validação clínica. Não é diagnóstico; associação"
          + " não indica causa. Leve dúvidas à equipe de saúde.";

  public List<Insight> calculate(
      LocalDate day, List<Sinal> signals, List<Sintoma> symptoms, List<Compromisso> appointments) {
    var result = new ArrayList<Insight>();
    var recent =
        signals.stream()
            .filter(s -> !s.data().isBefore(day.minusDays(2)) && !s.data().isAfter(day))
            .toList();
    var base =
        signals.stream()
            .filter(
                s -> !s.data().isBefore(day.minusDays(32)) && s.data().isBefore(day.minusDays(2)))
            .toList();
    if (recent.size() == 3 && base.size() >= 21) {
      double a = recent.stream().mapToInt(Sinal::sonoMin).average().orElseThrow(),
          b = base.stream().mapToInt(Sinal::sonoMin).average().orElseThrow();
      var src = new ArrayList<String>();
      Stream.concat(base.stream(), recent.stream())
          .forEach(
              s ->
                  src.add(
                      "Sinal #" + s.id() + " | " + s.data() + " | sono " + s.sonoMin() + " min"));
      result.add(
          new Insight(
              "A",
              "Seu sono em perspectiva",
              String.format(
                  Locale.forLanguageTag("pt-BR"),
                  "Média recente: %.0f min; referência pessoal anterior: %.0f min. Diferença: %+.0f"
                      + " min. %d noites de referência, sem sobreposição com as 3 recentes.",
                  a,
                  b,
                  a - b,
                  base.size()),
              day.minusDays(32),
              day,
              src,
              LIMIT));
    } else
      result.add(
          new Insight(
              "A",
              "Sono: dados insuficientes",
              "São necessárias 3 noites recentes e pelo menos 21 noites nos 30 dias anteriores.",
              day.minusDays(32),
              day,
              List.of("Noites recentes: " + recent.size(), "Noites de referência: " + base.size()),
              LIMIT));
    var fatigue =
        symptoms.stream()
            .filter(
                s ->
                    s.tipo() == TipoSintoma.FADIGA
                        && s.intensidade() >= 6
                        && !s.data().isBefore(day.minusDays(6))
                        && !s.data().isAfter(day))
            .toList();
    var dates = fatigue.stream().map(Sintoma::data).collect(Collectors.toSet());
    var week =
        signals.stream()
            .filter(s -> !s.data().isBefore(day.minusDays(6)) && !s.data().isAfter(day))
            .toList();
    var with = week.stream().filter(s -> dates.contains(s.data())).toList();
    var without = week.stream().filter(s -> !dates.contains(s.data())).toList();
    String activity =
        with.isEmpty()
            ? "Sem atividade disponível nos dias com fadiga."
            : String.format(
                Locale.forLanguageTag("pt-BR"),
                "Atividade média nos %d dias com fadiga e sinal disponível: %.0f passos.",
                with.size(),
                with.stream().mapToInt(Sinal::passos).average().orElseThrow());
    if (!without.isEmpty())
      activity +=
          String.format(
              Locale.forLanguageTag("pt-BR"),
              " Nos outros %d dias com sinal: %.0f passos.",
              without.size(),
              without.stream().mapToInt(Sinal::passos).average().orElseThrow());
    var origins = new ArrayList<String>();
    fatigue.forEach(
        s ->
            origins.add(
                "Sintoma #" + s.id() + " | " + s.data() + " | FADIGA " + s.intensidade() + "/10"));
    week.forEach(
        s -> origins.add("Sinal #" + s.id() + " | " + s.data() + " | " + s.passos() + " passos"));
    result.add(
        new Insight(
            "B",
            dates.size() >= 3 ? "Fadiga recorrente registrada" : "Acompanhe sua energia",
            dates.size()
                + " dia(s) distinto(s) com fadiga >= 6/10 nos últimos 7 dias. Recorrência"
                + " demonstrativa: 3 dias. "
                + activity
                + " Ausência de registro não significa ausência de fadiga.",
            day.minusDays(6),
            day,
            origins,
            LIMIT));
    var near =
        appointments.stream()
            .filter(
                c ->
                    "AGENDADO".equals(c.status())
                        && !c.data().toLocalDate().isAfter(day.plusDays(30)))
            .toList();
    for (var c : near) {
      long days = java.time.temporal.ChronoUnit.DAYS.between(day, c.data().toLocalDate());
      result.add(
          new Insight(
              "C",
              days < 0 ? "Confirme um compromisso passado" : "Seu próximo cuidado",
              c.tipo()
                  + " em "
                  + c.data().toLocalDate()
                  + " às "
                  + c.data().toLocalTime()
                  + " — "
                  + c.descricao()
                  + ". "
                  + (days < 0
                      ? "Data passada; realização não confirmada."
                      : "Faltam " + days + " dia(s)."),
              day,
              day.plusDays(30),
              List.of("Compromisso #" + c.id() + " | status " + c.status()),
              "Agenda fictícia cadastrada pela usuária. Não recomenda exames ou periodicidade"
                  + " clínica."));
    }
    return result;
  }
}
