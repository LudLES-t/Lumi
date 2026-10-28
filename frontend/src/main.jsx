import React, { useState, useEffect } from "react";
import { createRoot } from "react-dom/client";
import {
  Home,
  Heart,
  CalendarDays,
  Sparkles,
  FileText,
  Activity,
  Moon,
  Footprints,
  ArrowRight,
  Plus,
  Check,
  Download,
  ChevronLeft,
  ChevronRight,
} from "lucide-react";
import { api } from "./api";
import { reportLines } from "./reportContent";
import "./style.css";
const labels = {
  COLICA: "Cólica",
  FADIGA: "Fadiga",
  ENERGIA: "Energia",
  BEM_ESTAR: "Bem-estar",
  INCHACO: "Inchaço",
  DOR: "Dor",
  HUMOR: "Humor",
};
const pages = [
  ["home", "Início", Home],
  ["cycle", "Cycle", CalendarDays],
  ["care", "Care", Heart],
  ["coach", "Coach", Sparkles],
  ["agenda", "Agenda", CalendarDays],
  ["report", "Relatório", FileText],
];
const date = (d) => new Date(d + "T12:00:00").toLocaleDateString("pt-BR");
function Card({ children, className = "" }) {
  return <section className={"card " + className}>{children}</section>;
}
function App() {
  const [page, setPage] = useState(location.hash.slice(1) || "home"),
    [data, setData] = useState(null),
    [error, setError] = useState(""),
    [toast, setToast] = useState(""),
    [busy, setBusy] = useState(false),
    [report, setReport] = useState(null),
    [pdfUrl, setPdfUrl] = useState(null),
    [pdfBusy, setPdfBusy] = useState(false),
    [selected, setSelected] = useState(["sintomas", "ciclo"]),
    [month, setMonth] = useState("2026-10");
  const go = (p) => {
    location.hash = p;
    setPage(p);
    setError("");
  };
  useEffect(() => {
    const f = () => setPage(location.hash.slice(1) || "home");
    window.addEventListener("hashchange", f);
    return () => window.removeEventListener("hashchange", f);
  }, []);
  useEffect(() => {
    window.scrollTo({ top: 0, behavior: "instant" });
  }, [page]);
  useEffect(() => {
    setPdfUrl(null);
  }, [report]);
  useEffect(
    () => () => {
      if (pdfUrl) URL.revokeObjectURL(pdfUrl);
    },
    [pdfUrl],
  );
  async function preparePdf() {
    setPdfBusy(true);
    setError("");
    try {
      const { createPdf } = await import("./report.js");
      const blob = createPdf(report).output("blob");
      setPdfUrl(URL.createObjectURL(blob));
    } catch {
      setError("Não foi possível gerar o PDF. Tente novamente.");
    } finally {
      setPdfBusy(false);
    }
  }
  async function refresh() {
    const [home, cycle, symptoms, coach, appointments, signals] =
      await Promise.all(
        [
          "/hoje",
          "/ciclo",
          "/sintomas",
          "/coach",
          "/compromissos",
          "/sinais",
        ].map((p) => api(p)),
      );
    setData({ home, cycle, symptoms, coach, appointments, signals });
  }
  useEffect(() => {
    refresh().catch((e) =>
      setError(
        "Não foi possível conectar à API. Inicie o backend. " + e.message,
      ),
    );
  }, []);
  async function save(path, body) {
    setBusy(true);
    setError("");
    setToast("");
    try {
      await api(path, body);
      setReport(null);
      await refresh();
      setToast("Registro salvo. Histórico e Coach atualizados.");
    } catch (e) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  }
  const day = data?.home.dataReferencia || "2026-10-27";
  function SymptomForm({ care = false }) {
    const [type, setType] = useState(care ? "FADIGA" : "COLICA"),
      [level, setLevel] = useState(7);
    return (
      <form
        onSubmit={(e) => {
          e.preventDefault();
          const f = new FormData(e.currentTarget);
          save("/sintomas", {
            data: f.get("data"),
            tipo: type,
            intensidade: Number(level),
          });
        }}
      >
        <label>
          Data do registro
          <input
            name="data"
            type="date"
            max={day}
            defaultValue={day}
            required
          />
        </label>
        <label>
          O que você quer registrar?
          <select value={type} onChange={(e) => setType(e.target.value)}>
            {Object.entries(labels)
              .filter(([k]) =>
                care
                  ? ["FADIGA", "ENERGIA", "BEM_ESTAR", "DOR"].includes(k)
                  : true,
              )
              .map(([k, v]) => (
                <option key={k} value={k}>
                  {v}
                </option>
              ))}
          </select>
        </label>
        <label>
          {["ENERGIA", "BEM_ESTAR"].includes(type)
            ? "Nível (0 = muito baixo; 10 = muito alto)"
            : "Intensidade (0 = nenhuma; 10 = máxima)"}
          <div className="range">
            <input
              aria-label="Intensidade"
              type="range"
              min="0"
              max="10"
              value={level}
              onChange={(e) => setLevel(e.target.value)}
            />
            <strong>{level}/10</strong>
          </div>
        </label>
        <button disabled={busy} className="primary">
          <Plus size={17} /> {busy ? "Salvando…" : "Salvar registro"}
        </button>
      </form>
    );
  }
  function History({ care = false }) {
    const list = data.symptoms.filter(
      (s) =>
        !care || ["FADIGA", "ENERGIA", "BEM_ESTAR", "DOR"].includes(s.tipo),
    );
    return (
      <div className="history">
        {list.length ? (
          list.map((s) => (
            <div className="record" key={s.id}>
              <span className="dot" />
              <div>
                <b>{labels[s.tipo]}</b>
                <small>
                  {date(s.data)} · registro #{s.id}
                </small>
              </div>
              <strong>
                {s.intensidade}
                <small>/10</small>
              </strong>
            </div>
          ))
        ) : (
          <p>Nenhum registro ainda.</p>
        )}
      </div>
    );
  }
  function Appointments({ short = false }) {
    const items = short ? data.home.proximos : data.appointments;
    return (
      <div>
        {items.map((c) => (
          <div className="appointment" key={c.id}>
            <span className="icon">
              <CalendarDays size={20} />
            </span>
            <div>
              <b>{c.descricao}</b>
              <small>
                {date(c.data.slice(0, 10))} às {c.data.slice(11, 16)} · {c.tipo}
              </small>
            </div>
            <span className="badge">
              {c.data.slice(0, 10) < day ? "Data passada" : "Agendado"}
            </span>
          </div>
        ))}
        {!items.length && <p>Nenhum compromisso cadastrado.</p>}
      </div>
    );
  }
  function Calendar() {
    let [year, m] = month.split("-").map(Number);
    const first = new Date(year, m - 1, 1).getDay(),
      count = new Date(year, m, 0).getDate();
    const change = (delta) => {
      const d = new Date(year, m - 1 + delta, 1);
      setMonth(
        `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}`,
      );
    };
    return (
      <>
        <div className="calendar-head">
          <button aria-label="Mês anterior" onClick={() => change(-1)}>
            <ChevronLeft />
          </button>
          <b>
            {new Date(year, m - 1).toLocaleDateString("pt-BR", {
              month: "long",
              year: "numeric",
            })}
          </b>
          <button aria-label="Próximo mês" onClick={() => change(1)}>
            <ChevronRight />
          </button>
        </div>
        <div className="calendar">
          {["D", "S", "T", "Q", "Q", "S", "S"].map((s, i) => (
            <small key={"w" + i}>{s}</small>
          ))}
          {Array.from({ length: first }, (_, i) => (
            <span key={"e" + i} />
          ))}
          {Array.from({ length: count }, (_, i) => {
            const d = `${month}-${String(i + 1).padStart(2, "0")}`,
              period = data.cycle.menstruacoes.some(
                (p) => d >= p.inicio && d <= p.fim,
              ),
              symptom = data.symptoms.some((s) => s.data === d);
            return (
              <span
                key={d}
                title={`${date(d)}${period ? " · Menstruação" : ""}${symptom ? " · Sintoma registrado" : ""}`}
                className={
                  (period ? "period " : "") +
                  (d === day ? "today " : "") +
                  (symptom ? "symptom" : "")
                }
              >
                {i + 1}
              </span>
            );
          })}
        </div>
        <small>
          ● Rosa: menstruação registrada · ponto: sintomas · contorno: dia da
          demo
        </small>
      </>
    );
  }
  return (
    <div className="layout">
      <aside>
        <a className="logo" href="#home" onClick={() => go("home")}>
          lumi<span>✦</span>
        </a>
        <p className="slogan">Luz sobre você.</p>
        <nav aria-label="Módulos Lumi">
          {pages.map(([id, label, Icon]) => (
            <button
              key={id}
              className={page === id ? "active" : ""}
              aria-current={page === id ? "page" : undefined}
              onClick={() => go(id)}
            >
              <Icon size={20} />
              {label}
            </button>
          ))}
        </nav>
        <div className="sidebar-note">
          <span className="live-dot" /> Modo demonstração
          <p>
            Uma história fictícia.
            <br />
            Um cuidado mais organizado.
          </p>
          <small>IV CodeCraft 2026</small>
        </div>
      </aside>
      <main>
        <header>
          <a className="mobile-logo" href="#home" aria-label="Lumi, início">
            lumi<span>✦</span>
          </a>
          <span className="desktop-kicker">SEU ESPAÇO DE CUIDADO</span>
          <div className="persona">
            <span className="avatar">A</span> Ana · persona fictícia
          </div>
        </header>
        <div className="demo-banner">
          DADOS FICTÍCIOS{" "}
          <span>
            · Demo em {date(day)} · Sem conexão com pulseira ou clínica
          </span>
        </div>
        {error && (
          <div role="alert" className="error">
            {error}
            <button
              onClick={() =>
                refresh()
                  .then(() => setError(""))
                  .catch((e) => setError(e.message))
              }
            >
              Tentar novamente
            </button>
          </div>
        )}
        {toast && (
          <div role="status" className="toast">
            <Check size={18} />
            {toast}
            <button aria-label="Fechar aviso" onClick={() => setToast("")}>
              ×
            </button>
          </div>
        )}
        {!data ? (
          <Card>
            <h1>Preparando seu espaço…</h1>
            <p>Aguardando a API Lumi.</p>
          </Card>
        ) : (
          <>
            {page === "home" && (
              <>
                <div className="page-title">
                  <div>
                    <p className="eyebrow">LUMI HOME</p>
                    <h1>
                      Olá, Ana <span className="sun">✺</span>
                    </h1>
                    <p>Seu dia, seus registros, seu cuidado.</p>
                  </div>
                  <button className="secondary" onClick={() => go("report")}>
                    <FileText size={17} /> Preparar consulta
                  </button>
                </div>
                <div className="hero">
                  <div>
                    <span className="pill">
                      CONECTE OS PONTOS DA SUA ROTINA
                    </span>
                    <h2>
                      Mais clareza sobre você.
                      <br />
                      Mais contexto na consulta.
                    </h2>
                    <p>
                      O seu relógio mede.
                      <br />A Lumi leva isso até a consulta.
                    </p>
                    <button className="primary" onClick={() => go("care")}>
                      Como você está hoje? <ArrowRight size={18} />
                    </button>
                  </div>
                  <div className="orb">
                    <div className="orb-inner">
                      <Heart size={30} />
                      <b>
                        Você no
                        <br />
                        centro.
                      </b>
                      <small>Registrar · entender · levar</small>
                    </div>
                  </div>
                </div>
                <div className="metrics">
                  {[
                    [
                      Moon,
                      "Sono",
                      data.home.sinal
                        ? `${Math.floor(data.home.sinal.sonoMin / 60)}h ${data.home.sinal.sonoMin % 60}min`
                        : "—",
                      "Última noite simulada",
                    ],
                    [
                      Heart,
                      "FC em repouso",
                      data.home.sinal?.fcRepouso + " bpm",
                      "Medida simulada",
                    ],
                    [
                      Footprints,
                      "Passos",
                      data.home.sinal?.passos.toLocaleString("pt-BR"),
                      "Atividade simulada",
                    ],
                  ].map(([Icon, l, v, s]) => (
                    <Card key={l}>
                      <Icon className="metric-icon" />
                      <small>{l}</small>
                      <strong className="metric-value">{v}</strong>
                      <small>{s}</small>
                    </Card>
                  ))}
                </div>
                <div className="two-col">
                  <Card>
                    <div className="section-head">
                      <h2>Próximos cuidados</h2>
                      <button
                        className="text-button"
                        onClick={() => go("agenda")}
                      >
                        Ver agenda →
                      </button>
                    </div>
                    <Appointments short />
                  </Card>
                  <Card className="peach">
                    <Sparkles />
                    <h2>Seu sono em perspectiva</h2>
                    <p>{data.coach[0].texto}</p>
                    <button className="text-button" onClick={() => go("coach")}>
                      Entender os dados →
                    </button>
                  </Card>
                </div>
                <div className="module-grid">
                  {[
                    [
                      "cycle",
                      "Cycle",
                      "Conheça seus registros, respeite seu ritmo.",
                    ],
                    ["care", "Care", "Reserve um momento para seu bem-estar."],
                    ["coach", "Coach", "Veja o que os seus registros mostram."],
                  ].map(([id, l, d]) => (
                    <button className="module" key={id} onClick={() => go(id)}>
                      <span>{l}</span>
                      <p>{d}</p>
                      <ArrowRight size={20} />
                    </button>
                  ))}
                </div>
              </>
            )}
            {page === "cycle" && (
              <>
                <Title
                  eyebrow="LUMI CYCLE"
                  title="Seu ciclo, com contexto."
                  text="Registros ao longo do tempo. Estimativas com transparência."
                />
                <div className="two-col">
                  <Card>
                    <h2>Calendário menstrual</h2>
                    <Calendar />
                    {data.cycle.previsao && (
                      <div className="notice">
                        <b>
                          Próximo início estimado:{" "}
                          {date(data.cycle.previsao.data)}
                        </b>
                        <p>
                          Média de {data.cycle.previsao.mediaDias} dias ·{" "}
                          {data.cycle.previsao.amostras} intervalos.
                        </p>
                        <small>{data.cycle.previsao.incerteza}</small>
                      </div>
                    )}
                  </Card>
                  <Card>
                    <h2>Registrar sintoma</h2>
                    <SymptomForm />
                  </Card>
                  <Card>
                    <h2>Registrar menstruação</h2>
                    <form
                      onSubmit={(e) => {
                        e.preventDefault();
                        const f = new FormData(e.currentTarget);
                        save("/menstruacoes", Object.fromEntries(f));
                      }}
                    >
                      <label>
                        Início
                        <input type="date" name="inicio" required max={day} />
                      </label>
                      <label>
                        Fim
                        <input type="date" name="fim" required max={day} />
                      </label>
                      <label>
                        Fluxo
                        <select name="fluxo">
                          <option value="LEVE">Leve</option>
                          <option value="MODERADO">Moderado</option>
                          <option value="INTENSO">Intenso</option>
                        </select>
                      </label>
                      <button disabled={busy} className="primary">
                        Salvar período
                      </button>
                    </form>
                    <h3>Períodos registrados</h3>
                    {data.cycle.menstruacoes.map((p) => (
                      <p key={p.id}>
                        {date(p.inicio)} a {date(p.fim)} ·{" "}
                        {p.fluxo.toLowerCase()}
                      </p>
                    ))}
                  </Card>
                  <Card>
                    <h2>Histórico de sintomas</h2>
                    <History />
                  </Card>
                </div>
              </>
            )}
            {page === "care" && (
              <>
                <Title
                  eyebrow="LUMI CARE"
                  title="Um momento para você."
                  text="Como sua rotina tem sido? Seu relato também faz parte do cuidado."
                />
                <div className="two-col">
                  <Card>
                    <h2>Como você está hoje?</h2>
                    <p>Registre fadiga, energia ou bem-estar.</p>
                    <SymptomForm care />
                  </Card>
                  <Card className="care-education">
                    <Heart size={32} />
                    <h2>Seu cotidiano merece ser ouvido.</h2>
                    <p>
                      Durante cuidados oncológicos, a Lumi oferece um espaço
                      para organizar relatos de qualidade de vida e levá-los à
                      equipe que acompanha você.
                    </p>
                    <p>
                      Não avalia risco clínico, não orienta tratamentos e não
                      substitui atendimento.
                    </p>
                    <button className="secondary" onClick={() => go("report")}>
                      Organizar meus registros <ArrowRight size={16} />
                    </button>
                  </Card>
                </div>
                <Card>
                  <h2>Sua linha do tempo</h2>
                  <History care />
                </Card>
              </>
            )}
            {page === "coach" && (
              <>
                <Title
                  eyebrow="LUMI COACH"
                  title="Clareza, com origem."
                  text="Regras determinísticas executadas na API. Sem chatbot, deep learning ou diagnóstico."
                />
                <div className="section-head">
                  <span className="pill light">
                    3 regras · dados recalculados a cada consulta
                  </span>
                  <button
                    className="secondary"
                    disabled={busy}
                    onClick={() => {
                      setBusy(true);
                      refresh()
                        .then(() =>
                          setToast(
                            "Coach recalculado com os registros atuais.",
                          ),
                        )
                        .catch((e) => setError(e.message))
                        .finally(() => setBusy(false));
                    }}
                  >
                    Recalcular insights
                  </button>
                </div>
                <div className="insights">
                  {data.coach.map((i, n) => (
                    <Card key={n}>
                      <div className="insight-top">
                        <span className="icon">
                          <Sparkles />
                        </span>
                        <small>REGRA {i.regra} · DESCRITIVA</small>
                      </div>
                      <h2>{i.titulo}</h2>
                      <p>{i.texto}</p>
                      <small>
                        Período: {date(i.inicio)} a {date(i.fim)}
                      </small>
                      <details>
                        <summary>
                          Ver origem da análise ({i.origem.length} registros)
                        </summary>
                        <ul>
                          {i.origem.map((o, n) => (
                            <li key={n}>{o}</li>
                          ))}
                        </ul>
                      </details>
                      <div className="limitation">{i.limitacoes}</div>
                    </Card>
                  ))}
                </div>
              </>
            )}
            {page === "agenda" && (
              <>
                <Title
                  eyebrow="AGENDA LUMI"
                  title="Seu próximo cuidado."
                  text="Consultas, exames e retornos organizados em um só lugar."
                />
                <div className="two-col">
                  <Card>
                    <h2>Novo compromisso fictício</h2>
                    <form
                      onSubmit={(e) => {
                        e.preventDefault();
                        save(
                          "/compromissos",
                          Object.fromEntries(new FormData(e.currentTarget)),
                        );
                      }}
                    >
                      <label>
                        Tipo
                        <select name="tipo">
                          <option value="CONSULTA">Consulta</option>
                          <option value="EXAME">Exame</option>
                          <option value="RETORNO">Retorno</option>
                        </select>
                      </label>
                      <label>
                        Data e horário
                        <input
                          type="datetime-local"
                          name="data"
                          defaultValue={day + "T15:00"}
                          required
                        />
                      </label>
                      <label>
                        Descrição
                        <input
                          name="descricao"
                          maxLength="200"
                          placeholder="Ex.: Retorno demonstrativo"
                          required
                        />
                      </label>
                      <button disabled={busy} className="primary">
                        Salvar compromisso
                      </button>
                    </form>
                    <small>
                      Horário de São Paulo. Avisos apenas dentro do aplicativo;
                      sem envio de notificações.
                    </small>
                  </Card>
                  <Card>
                    <h2>Seus agendamentos</h2>
                    <Appointments />
                  </Card>
                </div>
              </>
            )}
            {page === "report" && (
              <>
                <Title
                  eyebrow="RELATÓRIO LUMI"
                  title="Leve sua história à consulta."
                  text="Você escolhe quais registros entram no documento."
                />
                <div className="report-layout">
                  <Card>
                    <h2>O que incluir?</h2>
                    {[
                      [
                        "sinais",
                        "Sono e atividade",
                        "90 dias de sinais fictícios",
                      ],
                      [
                        "sintomas",
                        "Sintomas e bem-estar",
                        "Seus relatos e intensidades",
                      ],
                      ["ciclo", "Ciclo menstrual", "Períodos e estimativa"],
                      [
                        "compromissos",
                        "Agenda",
                        "Consultas, exames e retornos",
                      ],
                      [
                        "insights",
                        "Análises do Coach",
                        "Inclui dados derivados e origens de sono, sintomas e agenda, mesmo sem selecionar essas categorias acima.",
                      ],
                    ].map(([k, l, d]) => (
                      <label className="checkbox" key={k}>
                        <input
                          type="checkbox"
                          checked={selected.includes(k)}
                          onChange={(e) => {
                            setSelected(
                              e.target.checked
                                ? [...selected, k]
                                : selected.filter((x) => x !== k),
                            );
                            setReport(null);
                          }}
                        />
                        <span>
                          <b>{l}</b>
                          <small>{d}</small>
                        </span>
                      </label>
                    ))}
                    <button
                      className="primary"
                      disabled={!selected.length || busy}
                      onClick={async () => {
                        setBusy(true);
                        try {
                          setReport(
                            await api("/relatorio", { incluir: selected }),
                          );
                        } catch (e) {
                          setError(e.message);
                        } finally {
                          setBusy(false);
                        }
                      }}
                    >
                      Gerar prévia
                    </button>
                    <p className="fine">
                      Exportação local autorizada apenas para os itens
                      selecionados. Nenhum envio automático à clínica.
                    </p>
                  </Card>
                  <Card>
                    <div className="section-head">
                      <h2>Prévia do relatório</h2>
                      <button
                        className="secondary"
                        disabled={!report || pdfBusy}
                        onClick={preparePdf}
                      >
                        <Download size={16} />{" "}
                        {pdfBusy ? "Preparando…" : "Preparar PDF"}
                      </button>
                    </div>
                    {pdfUrl && (
                      <div className="pdf-actions" role="status">
                        <b>Seu PDF está pronto.</b>
                        <div>
                          <a
                            className="primary"
                            href={pdfUrl}
                            download="Lumi-relatorio-ficticio.pdf"
                          >
                            Salvar PDF
                          </a>
                          <a
                            className="secondary"
                            href={pdfUrl}
                            target="_blank"
                            rel="noopener noreferrer"
                          >
                            Abrir PDF
                          </a>
                        </div>
                        <small>
                          No celular, use “Abrir PDF” e as opções do navegador
                          para salvar ou compartilhar.
                        </small>
                      </div>
                    )}
                    {report ? (
                      <div className="report-preview">
                        <h2>lumi · Resumo para consulta</h2>
                        {reportLines(report).map((l, i) => (
                          <p key={i}>{l || " "}</p>
                        ))}
                      </div>
                    ) : (
                      <div className="empty">
                        <FileText size={42} />
                        <h3>Sua consulta começa com contexto.</h3>
                        <p>
                          Selecione os dados e gere a prévia.
                          <br />O PDF terá exatamente o conteúdo apresentado
                          aqui.
                        </p>
                      </div>
                    )}
                  </Card>
                </div>
              </>
            )}
          </>
        )}
        <footer>
          Lumi · Luz sobre você.{" "}
          <span>MVP educacional · Dados fictícios · Não é diagnóstico</span>
        </footer>
      </main>
    </div>
  );
}
function Title({ eyebrow, title, text }) {
  return (
    <div className="page-title">
      <div>
        <p className="eyebrow">{eyebrow}</p>
        <h1>{title}</h1>
        <p>{text}</p>
      </div>
    </div>
  );
}
createRoot(document.getElementById("root")).render(<App />);
