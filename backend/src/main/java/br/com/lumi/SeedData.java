package br.com.lumi;

import static br.com.lumi.Models.*;

import java.util.Random;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SeedData implements CommandLineRunner {
  final LumiRepository repo;
  final LumiService service;

  public SeedData(LumiRepository repo, LumiService service) {
    this.repo = repo;
    this.service = service;
  }

  @Override
  @Transactional
  public void run(String... args) {
    if (repo.exists(1)) return;
    repo.seedUser();
    var day = service.referenceDate();
    var random = new Random(42);
    for (int i = 89; i >= 0; i--) {
      var date = day.minusDays(i);
      repo.add(
          1,
          new Sinal(
              null,
              date,
              i < 3 ? 330 + random.nextInt(25) : 415 + random.nextInt(55),
              62 + random.nextInt(9),
              Math.round((random.nextDouble() * .6 - .2) * 100) / 100.0,
              i < 4 ? 2100 + random.nextInt(1300) : 5000 + random.nextInt(4000)));
      if (i % 28 == 21 || i % 28 == 20)
        repo.add(1, new Sintoma(null, date, TipoSintoma.COLICA, 7 + random.nextInt(3)));
      if (i % 9 == 0) repo.add(1, new Sintoma(null, date, TipoSintoma.BEM_ESTAR, 6));
    }
    for (int i : new int[] {77, 49, 21})
      repo.add(1, new Menstruacao(null, day.minusDays(i), day.minusDays(i - 4), Fluxo.MODERADO));
    for (int i : new int[] {2, 1})
      repo.add(1, new Sintoma(null, day.minusDays(i), TipoSintoma.FADIGA, 7));
    repo.add(1, new Sintoma(null, day, TipoSintoma.ENERGIA, 3));
    repo.add(
        1,
        new Compromisso(
            null,
            TipoCompromisso.CONSULTA,
            day.plusDays(1).atTime(14, 0),
            "Consulta demonstrativa — preparar resumo",
            null));
    repo.add(
        1,
        new Compromisso(
            null,
            TipoCompromisso.EXAME,
            day.plusDays(20).atTime(8, 0),
            "Exame fictício previamente agendado",
            null));
  }
}
