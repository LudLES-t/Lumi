const base = import.meta.env.VITE_API_URL || "/api/usuarias/1";
export async function api(path, body) {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 15000);
  try {
    const r = await fetch(base + path, {
      signal: controller.signal,
      ...(body
        ? {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(body),
          }
        : {}),
    });
    const raw = await r.text();
    let json;
    try {
      json = JSON.parse(raw);
    } catch {
      throw new Error(
        "A API não respondeu em JSON. Verifique se o backend está disponível.",
      );
    }
    if (!r.ok) throw new Error(json.mensagem || "Falha na API");
    return json;
  } catch (e) {
    if (e.name === "AbortError")
      throw new Error(
        "A conexão demorou demais. Confira a rede e tente novamente.",
      );
    throw e;
  } finally {
    clearTimeout(timeout);
  }
}
