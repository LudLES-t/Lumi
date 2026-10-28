package br.com.lumi;

import static br.com.lumi.Models.*;
import static org.assertj.core.api.Assertions.*;

import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;

class CoachRulesTest {
  final LocalDate day = LocalDate.of(2026, 10, 27);
  final CoachRules rules = new CoachRules();

  Sinal signal(int days, int sleep, int steps) {
    return new Sinal((long) days, day.minusDays(days), sleep, 65, 0, steps);
  }

  @Test
  void sleepUsesNonOverlappingWindows() {
    var s = new ArrayList<Sinal>();
    for (int i = 0; i < 33; i++) s.add(signal(i, i < 3 ? 360 : 420, 5000));
    var a = rules.calculate(day, s, List.of(), List.of()).getFirst();
    assertThat(a.texto()).contains("360 min", "420 min", "-60 min");
    assertThat(a.origem()).hasSize(33);
  }

  @Test
  void insufficientSleepDoesNotInventComparison() {
    var a = rules.calculate(day, List.of(signal(0, 360, 2000)), List.of(), List.of()).getFirst();
    assertThat(a.titulo()).contains("insuficientes");
  }

  @Test
  void duplicateFatigueOnOneDayDoesNotCountAsRecurrence() {
    var symptoms =
        List.of(
            new Sintoma(1L, day, TipoSintoma.FADIGA, 8),
            new Sintoma(2L, day, TipoSintoma.FADIGA, 9),
            new Sintoma(3L, day.minusDays(1), TipoSintoma.FADIGA, 7));
    var b = rules.calculate(day, List.of(), symptoms, List.of()).get(1);
    assertThat(b.titulo()).isEqualTo("Acompanhe sua energia");
    assertThat(b.texto()).contains("2 dia(s)", "Sem atividade");
  }

  @Test
  void thirdDayTriggersRecurrenceAndDescribesActivity() {
    var symptoms = new ArrayList<Sintoma>();
    for (int i = 0; i < 3; i++)
      symptoms.add(new Sintoma((long) i, day.minusDays(i), TipoSintoma.FADIGA, 6));
    var b =
        rules
            .calculate(
                day,
                List.of(signal(0, 400, 2000), signal(1, 400, 4000), signal(4, 400, 6000)),
                symptoms,
                List.of())
            .get(1);
    assertThat(b.titulo()).isEqualTo("Fadiga recorrente registrada");
    assertThat(b.texto()).contains("3000 passos", "6000 passos");
  }

  @Test
  void ignoresOldFutureAndLowFatigue() {
    var s =
        List.of(
            new Sintoma(1L, day.minusDays(7), TipoSintoma.FADIGA, 9),
            new Sintoma(2L, day.plusDays(1), TipoSintoma.FADIGA, 9),
            new Sintoma(3L, day, TipoSintoma.FADIGA, 5));
    assertThat(rules.calculate(day, List.of(), s, List.of()).get(1).texto()).contains("0 dia(s)");
  }

  @Test
  void appointmentsIncludeBoundaryAndPastButNotCompleted() {
    var cs =
        List.of(
            appointment(30, "AGENDADO"),
            appointment(31, "AGENDADO"),
            appointment(-1, "AGENDADO"),
            appointment(2, "CONCLUIDO"));
    var result =
        rules.calculate(day, List.of(), List.of(), cs).stream()
            .filter(i -> i.regra().equals("C"))
            .toList();
    assertThat(result).hasSize(2);
    assertThat(result.get(1).texto()).contains("realização não confirmada");
  }

  @Test
  void calculationIsDeterministic() {
    assertThat(rules.calculate(day, List.of(), List.of(), List.of()))
        .isEqualTo(rules.calculate(day, List.of(), List.of(), List.of()));
  }

  Compromisso appointment(int d, String status) {
    return new Compromisso(
        (long) d, TipoCompromisso.EXAME, day.plusDays(d).atTime(10, 0), "Teste fictício", status);
  }
}
