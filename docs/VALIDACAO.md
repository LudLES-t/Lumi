# Validação efetivamente executada — 27/10/2026

## Aprovada

- Frontend: `npm install`, depois `npm run build`, com React 19.2.0 e Vite 6.4.1. Build concluído. Aviso de bundle grande por jsPDF e fonte incorporada; não impede execução.
- Backend: compilação e execução com OpenJDK 21.0.12 e Maven 3.9.11. Spring Boot 3.5.7, H2 persistente e frontend servido no mesmo JAR.
- JUnit: **13 testes, 0 falhas, 0 erros** (7 regras + 6 integração Spring/MockMvc).
- Regra A: janelas sem sobreposição, médias exatas e insuficiência de dados.
- Regra B: dias distintos, limiar, dados fora da janela, ausência de sinais e médias de atividade.
- Regra C: data limite de 30 dias, agendamento passado e status concluído.
- REST: inserir sintoma, consultar histórico, recalcular regra e consultar origem, inserir agenda e produzir JSON do relatório selecionado.
- Validação de intensidade/data futura, usuária inexistente, sobreposição menstrual, sinais duplicados, categorias inválidas e CORS.
- `scripts/smoke.py`: servidor real iniciado em porta de teste, ciclo HTTP completo, encerramento e reinício sobre o mesmo banco. Registros persistem; sinais continuam em 90; seed não duplica.
- PDF: gerado pelo **mesmo módulo JavaScript `src/report.js` usado no frontend**, com JSON produzido pela API real. Arquivo multipágina A4; estrutura verificada com `pdfinfo`, extração de texto e inspeção das páginas renderizadas. Fonte incorporada para preservar português. Exemplo em `release/Lumi-relatorio-ficticio.pdf`.

## Verificação de interface e limites (atualizado em 28/10/2026)

O isolamento inicial do preview foi resolvido iniciando Java e Vite no mesmo ambiente de desenvolvimento e configurando a origem específica de preview. No navegador desktop, foram verificados dashboard, cadastro de fadiga, histórico, insight recalculado, abertura de origem, agenda e prévia.

No mobile, foram verificadas as seis páginas em viewports de 320, 390 e 430 px, sem overflow horizontal (18 verificações). Check-in de energia salvo e confirmado no histórico. A prévia gerou um Blob PDF com links de abrir/salvar. O evento final de download não foi confirmado pelo navegador de automação; verificar manualmente no aparelho. O PDF de exemplo foi gerado pelo mesmo módulo e inspecionado.

Os testes Playwright em arquivo foram fornecidos, mas não executados pelo runner. A interação descrita acima foi executada com automação do navegador. Não houve teste em aparelho físico ou Safari, nem validação clínica, auditoria de segurança ou implantação pública. Veja `MOBILE.md`.

## Repetir o teste HTTP

Na raiz, com Java 21 no PATH e o JAR compilado:
```sh
python scripts/smoke.py
```
Usa banco temporário e porta 18080, mantém a demo intacta, salva um JSON de exemplo em `release/`. O PDF de exemplo fornecido contém a inclusão de fadiga e retorno feitas pelo teste; o seed inicial do aplicativo ainda começa com dois dias de fadiga.

## Repetir o teste de interface

Use backend e Vite ligados conforme README, banco novo e:
```sh
cd frontend
npx playwright install chromium
npx playwright test
```
Teste Playwright insere dados fictícios no banco da aplicação. Para repetir a transição de dois para três dias, pare a API e renomeie `backend/data` antes de reiniciar. Não execute os testes sobre dados reais.

## Rebuild mobile

Em 28/10/2026, build React e Maven package concluídos novamente; 13 testes JUnit aprovados. O smoke HTTP foi repetido no JAR final, incluindo persistência após reinício. Evidência resumida em TESTES-EXECUTADOS.txt.
