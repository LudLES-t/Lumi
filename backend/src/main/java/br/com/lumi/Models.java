package br.com.lumi;

import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;

public class Models {
  public enum TipoSintoma {
    COLICA,
    FADIGA,
    ENERGIA,
    BEM_ESTAR,
    INCHACO,
    DOR,
    HUMOR
  }

  public enum Fluxo {
    LEVE,
    MODERADO,
    INTENSO
  }

  public enum TipoCompromisso {
    CONSULTA,
    EXAME,
    RETORNO
  }

  public record Usuaria(long id, String nome, LocalDate nascimento, String perfil) {}

  public record Sinal(
      Long id,
      @NotNull LocalDate data,
      @Min(0) @Max(1440) int sonoMin,
      @Min(20) @Max(250) int fcRepouso,
      @DecimalMin("-5") @DecimalMax("5") double tempDelta,
      @Min(0) @Max(100000) int passos) {}

  public record Menstruacao(
      Long id, @NotNull LocalDate inicio, @NotNull LocalDate fim, @NotNull Fluxo fluxo) {}

  public record Sintoma(
      Long id,
      @NotNull LocalDate data,
      @NotNull TipoSintoma tipo,
      @Min(0) @Max(10) int intensidade) {}

  public record Compromisso(
      Long id,
      @NotNull TipoCompromisso tipo,
      @NotNull LocalDateTime data,
      @NotBlank @Size(max = 200) String descricao,
      String status) {}

  public record Insight(
      String regra,
      String titulo,
      String texto,
      LocalDate inicio,
      LocalDate fim,
      List<String> origem,
      String limitacoes) {}

  public record Selecao(@NotEmpty Set<String> incluir) {}
}
