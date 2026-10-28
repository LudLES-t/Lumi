import { jsPDF } from "jspdf";
import pdfFont from "./pdfFont.js";
import { reportLines } from "./reportContent.js";
export { reportLines };
export function createPdf(report) {
  const doc = new jsPDF();
  doc.addFileToVFS("DejaVuSans.ttf", pdfFont);
  doc.addFont("DejaVuSans.ttf", "LumiSans", "normal");
  doc.setFont("LumiSans");
  let y = 28;
  function header() {
    doc.setFillColor(38, 29, 38);
    doc.rect(0, 0, 210, 19, "F");
    doc.setTextColor(255, 255, 255);
    doc.setFontSize(15);
    doc.text("lumi | Luz sobre você", 14, 12);
    doc.setTextColor(50, 40, 48);
    doc.setFontSize(10);
  }
  header();
  for (const line of reportLines(report)) {
    const split = doc.splitTextToSize(line, 180);
    for (const row of split) {
      if (y > 275) {
        doc.addPage();
        header();
        y = 28;
      }
      doc.text(row, 14, y);
      y += 5;
    }
    y += 2;
  }
  const pages = doc.getNumberOfPages();
  for (let i = 1; i <= pages; i++) {
    doc.setPage(i);
    doc.setFontSize(8);
    doc.setTextColor(130);
    doc.text(`DADOS FICTÍCIOS • Não é diagnóstico | ${i}/${pages}`, 14, 290);
  }
  return doc;
}

export function exportPdf(report) {
  return createPdf(report).save("Lumi-relatorio-ficticio.pdf");
}
