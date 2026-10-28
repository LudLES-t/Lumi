package br.com.lumi;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"})
@AutoConfigureMockMvc
@Transactional
class ApiIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired LumiRepository repo;
  @Autowired SeedData seed;
  final String base = "/api/usuarias/1";

  @Test
  void fullApiFlow() throws Exception {
    mvc.perform(get(base + "/hoje"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.simulado").value(true));
    mvc.perform(
            post(base + "/sintomas")
                .contentType("application/json")
                .content("{\"data\":\"2026-10-27\",\"tipo\":\"FADIGA\",\"intensidade\":8}"))
        .andExpect(status().isCreated());
    mvc.perform(get(base + "/sintomas")).andExpect(jsonPath("$[0].intensidade").value(8));
    mvc.perform(get(base + "/coach"))
        .andExpect(jsonPath("$[1].titulo").value("Fadiga recorrente registrada"))
        .andExpect(jsonPath("$[1].origem").isNotEmpty());
    mvc.perform(
            post(base + "/compromissos")
                .contentType("application/json")
                .content(
                    "{\"tipo\":\"CONSULTA\",\"data\":\"2026-10-28T15:00\",\"descricao\":\"Demo\"}"))
        .andExpect(status().isCreated());
    mvc.perform(
            post(base + "/relatorio")
                .contentType("application/json")
                .content("{\"incluir\":[\"sintomas\",\"compromissos\"]}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.secoes.sintomas").isArray())
        .andExpect(jsonPath("$.secoes.sinais").doesNotExist())
        .andExpect(jsonPath("$.secoes.insights").doesNotExist());
  }

  @Test
  void seedIsIdempotent() {
    int n = repo.sintomas(1).size();
    seed.run();
    seed.run();
    assertThat(repo.sinais(1)).hasSize(90);
    assertThat(repo.sintomas(1)).hasSize(n);
  }

  @Test
  void rejectsInvalidIntensityFutureDatesAndUnknownUsers() throws Exception {
    for (String body :
        new String[] {
          "{\"data\":\"2026-10-27\",\"tipo\":\"FADIGA\",\"intensidade\":11}",
          "{\"data\":\"2026-10-28\",\"tipo\":\"FADIGA\",\"intensidade\":7}"
        })
      mvc.perform(post(base + "/sintomas").contentType("application/json").content(body))
          .andExpect(status().isBadRequest());
    mvc.perform(get("/api/usuarias/999/hoje")).andExpect(status().isNotFound());
  }

  @Test
  void rejectsOverlappingPeriodsAndDuplicateSignals() throws Exception {
    mvc.perform(
            post(base + "/menstruacoes")
                .contentType("application/json")
                .content("{\"inicio\":\"2026-10-06\",\"fim\":\"2026-10-08\",\"fluxo\":\"LEVE\"}"))
        .andExpect(status().isConflict());
    mvc.perform(
            post(base + "/sinais")
                .contentType("application/json")
                .content(
                    "{\"data\":\"2026-10-27\",\"sonoMin\":400,\"fcRepouso\":60,\"tempDelta\":0,\"passos\":5000}"))
        .andExpect(status().isConflict());
  }

  @Test
  void reportRejectsEmptyOrUnknownSelection() throws Exception {
    for (String body : new String[] {"{\"incluir\":[]}", "{\"incluir\":[\"segredo\"]}"})
      mvc.perform(post(base + "/relatorio").contentType("application/json").content(body))
          .andExpect(status().isBadRequest());
  }

  @Test
  void corsOnlyAllowsConfiguredOrigins() throws Exception {
    mvc.perform(
            options(base + "/hoje")
                .header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "GET"))
        .andExpect(status().isOk())
        .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    mvc.perform(
            options(base + "/hoje")
                .header("Origin", "https://example.org")
                .header("Access-Control-Request-Method", "GET"))
        .andExpect(status().isForbidden());
  }
}
