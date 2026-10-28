package br.com.lumi;

import static br.com.lumi.Models.*;

import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LumiRepository {
  private final JdbcTemplate db;

  public LumiRepository(JdbcTemplate db) {
    this.db = db;
  }

  public boolean exists(long id) {
    return db.queryForObject("select count(*) from usuaria where id=?", Integer.class, id) > 0;
  }

  public void seedUser() {
    db.update("insert into usuaria values(1,'Ana • persona fictícia','1996-04-10','AMBOS')");
  }

  public Usuaria user(long u) {
    return db.queryForObject(
        "select * from usuaria where id=?",
        (r, n) ->
            new Usuaria(
                r.getLong("id"),
                r.getString("nome"),
                r.getDate("nascimento").toLocalDate(),
                r.getString("perfil")),
        u);
  }

  public List<Sinal> sinais(long u) {
    return db.query(
        "select * from sinal_diario where usuaria_id=? order by data",
        (r, n) ->
            new Sinal(
                r.getLong("id"),
                r.getDate("data").toLocalDate(),
                r.getInt("sono_min"),
                r.getInt("fc_repouso"),
                r.getDouble("temp_delta"),
                r.getInt("passos")),
        u);
  }

  public List<Sintoma> sintomas(long u) {
    return db.query(
        "select * from sintoma where usuaria_id=? order by data desc,id desc",
        (r, n) ->
            new Sintoma(
                r.getLong("id"),
                r.getDate("data").toLocalDate(),
                TipoSintoma.valueOf(r.getString("tipo")),
                r.getInt("intensidade")),
        u);
  }

  public List<Menstruacao> menstruacoes(long u) {
    return db.query(
        "select * from menstruacao where usuaria_id=? order by inicio",
        (r, n) ->
            new Menstruacao(
                r.getLong("id"),
                r.getDate("inicio").toLocalDate(),
                r.getDate("fim").toLocalDate(),
                Fluxo.valueOf(r.getString("fluxo"))),
        u);
  }

  public List<Compromisso> compromissos(long u) {
    return db.query(
        "select * from compromisso where usuaria_id=? order by data",
        (r, n) ->
            new Compromisso(
                r.getLong("id"),
                TipoCompromisso.valueOf(r.getString("tipo")),
                r.getTimestamp("data").toLocalDateTime(),
                r.getString("descricao"),
                r.getString("status")),
        u);
  }

  private long insert(String sql, Object... args) {
    var key = new org.springframework.jdbc.support.GeneratedKeyHolder();
    db.update(
        c -> {
          var ps = c.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS);
          for (int i = 0; i < args.length; i++) ps.setObject(i + 1, args[i]);
          return ps;
        },
        key);
    return Objects.requireNonNull(key.getKey()).longValue();
  }

  public Sinal add(long u, Sinal s) {
    long id =
        insert(
            "insert into sinal_diario(usuaria_id,data,sono_min,fc_repouso,temp_delta,passos)"
                + " values(?,?,?,?,?,?)",
            u,
            s.data(),
            s.sonoMin(),
            s.fcRepouso(),
            s.tempDelta(),
            s.passos());
    return new Sinal(id, s.data(), s.sonoMin(), s.fcRepouso(), s.tempDelta(), s.passos());
  }

  public Sintoma add(long u, Sintoma s) {
    long id =
        insert(
            "insert into sintoma(usuaria_id,data,tipo,intensidade) values(?,?,?,?)",
            u,
            s.data(),
            s.tipo().name(),
            s.intensidade());
    return new Sintoma(id, s.data(), s.tipo(), s.intensidade());
  }

  public Menstruacao add(long u, Menstruacao s) {
    long id =
        insert(
            "insert into menstruacao(usuaria_id,inicio,fim,fluxo) values(?,?,?,?)",
            u,
            s.inicio(),
            s.fim(),
            s.fluxo().name());
    return new Menstruacao(id, s.inicio(), s.fim(), s.fluxo());
  }

  public Compromisso add(long u, Compromisso s) {
    long id =
        insert(
            "insert into compromisso(usuaria_id,tipo,data,descricao,status) values(?,?,?,?,?)",
            u,
            s.tipo().name(),
            s.data(),
            s.descricao(),
            "AGENDADO");
    return new Compromisso(id, s.tipo(), s.data(), s.descricao(), "AGENDADO");
  }
}
