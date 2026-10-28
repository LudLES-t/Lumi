import { test, expect } from "@playwright/test";
test("registro → histórico → regra → agenda → PDF", async ({ page }) => {
  await page.goto("/");
  await expect(page.getByRole("heading", { name: "Olá, Ana" })).toBeVisible();
  await page.getByRole("button", { name: "Care", exact: true }).click();
  await page
    .getByRole("button", { name: "Salvar registro", exact: true })
    .click();
  await expect(page.getByRole("status")).toContainText("Registro salvo");
  await expect(page.locator(".history")).toContainText("Fadiga");
  await page.getByRole("button", { name: "Coach", exact: true }).click();
  await expect(
    page.getByRole("heading", { name: "Fadiga recorrente registrada" }),
  ).toBeVisible();
  await page
    .locator(".card")
    .filter({
      has: page.getByRole("heading", { name: "Fadiga recorrente registrada" }),
    })
    .locator("summary")
    .click();
  await expect(page.locator("details[open]")).toContainText("Sintoma #");
  await page.getByRole("button", { name: "Agenda", exact: true }).click();
  await page.getByLabel("Descrição").fill("Retorno fictício E2E");
  await page.getByLabel("Data e horário").fill("2026-10-28T15:00");
  await page.getByRole("button", { name: "Salvar compromisso" }).click();
  await expect(
    page.locator(".appointment").filter({ hasText: "Retorno fictício E2E" }),
  ).toBeVisible();
  await page.getByRole("button", { name: "Relatório", exact: true }).click();
  await page.getByRole("button", { name: "Gerar prévia" }).click();
  await expect(page.locator(".report-preview")).toContainText("FADIGA");
  await page.getByRole("button", { name: "Preparar PDF", exact: true }).click();
  await expect(
    page.getByRole("link", { name: "Salvar PDF", exact: true }),
  ).toBeVisible();
  const downloadEvent = page.waitForEvent("download");
  await page.getByRole("link", { name: "Salvar PDF", exact: true }).click();
  const file = await downloadEvent;
  expect(file.suggestedFilename()).toBe("Lumi-relatorio-ficticio.pdf");
  await file.saveAs("test-results/Lumi-relatorio-ficticio.pdf");
});
test("layout cabe em celular", async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 });
  await page.goto("/");
  await expect(page.getByRole("heading", { name: "Olá, Ana" })).toBeVisible();
  expect(
    await page.evaluate(
      () => document.documentElement.scrollWidth <= window.innerWidth,
    ),
  ).toBe(true);
});
