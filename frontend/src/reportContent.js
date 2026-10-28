export function reportLines(r) {
  const lines = [
    r.aviso,
    `Data de referência: ${r.dataReferencia}`,
    "Período: histórico disponível nas categorias selecionadas.",
  ];
  for (const [key, values] of Object.entries(r.secoes)) {
    lines.push("", key.toUpperCase());
    if (key === "sinais") {
      lines.push(
        "Data | Sono (min) | FC repouso (bpm) | Passos | Delta temp. (C)",
      );
      values.forEach((v) =>
        lines.push(
          `${v.data} | ${v.sonoMin} | ${v.fcRepouso} | ${v.passos} | ${v.tempDelta}`,
        ),
      );
    }
    if (key === "sintomas")
      values.forEach((v) =>
        lines.push(
          `${v.data} | ${v.tipo} | ${v.intensidade}/10 | Registro #${v.id}`,
        ),
      );
    if (key === "ciclo") {
      values.menstruacoes.forEach((v) =>
        lines.push(`${v.inicio} a ${v.fim} | Fluxo ${v.fluxo}`),
      );
      if (values.previsao)
        lines.push(
          `Estimativa: ${values.previsao.data}; média ${values.previsao.mediaDias} dias. ${values.previsao.incerteza}`,
        );
    }
    if (key === "compromissos")
      values.forEach((v) =>
        lines.push(
          `${v.data.replace("T", " ")} | ${v.tipo} | ${v.descricao} | ${v.status}`,
        ),
      );
    if (key === "insights")
      values.forEach((v) =>
        lines.push(
          `${v.regra} - ${v.titulo}`,
          v.texto,
          `Período: ${v.inicio} a ${v.fim}`,
          `Origem: ${v.origem.join("; ")}`,
          v.limitacoes,
        ),
      );
    if (Array.isArray(values) && !values.length) lines.push("Sem registros.");
  }
  return lines;
}
