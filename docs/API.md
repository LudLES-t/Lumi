# Contrato REST v1 — fechado antes da integração
Base: `/api/usuarias/1`. JSON UTF-8; persona única fictícia. Datas ISO `YYYY-MM-DD`; agenda `YYYY-MM-DDTHH:mm` no horário de São Paulo. Data da demonstração: `2026-10-27` (configurável no backend).

| Método / recurso | Entrada | Resposta 200 (POST: 201) |
|---|---|---|
| GET /hoje | — | `{usuaria:{id,nome,nascimento,perfil},dataReferencia,simulado:true,sinal:{id,data,sonoMin,fcRepouso,tempDelta,passos},proximos:[],totalSintomas}` |
| GET /sinais | — | `[{id,data,sonoMin,fcRepouso,tempDelta,passos}]` |
| POST /sinais | `{data,sonoMin,fcRepouso,tempDelta,passos}` | sinal criado; 409 se dia existente |
| GET /ciclo | — | `{menstruacoes:[{id,inicio,fim,fluxo}],previsao:{data,mediaDias,amostras,incerteza}}`; previsao null com menos de 2 inícios |
| POST /menstruacoes | `{inicio,fim,fluxo:"MODERADO"}` | período criado; não permite sobreposição |
| GET /sintomas | — | `[{id,data,tipo,intensidade}]` ordenados por data/id decrescente |
| POST /sintomas | `{data:"2026-10-27",tipo:"FADIGA",intensidade:8}` | registro com id |
| GET /coach | — | `[{regra,titulo,texto,inicio,fim,origem:["..."],limitacoes}]` recalculados a cada GET |
| GET /compromissos | — | `[{id,tipo,data,descricao,status}]` |
| POST /compromissos | `{tipo:"CONSULTA",data:"2026-10-28T14:00",descricao:"Consulta demonstrativa"}` | registro com status AGENDADO |
| POST /relatorio | `{incluir:["sinais","sintomas","ciclo","compromissos","insights"]}` | `{titulo,dataReferencia,simulado:true,aviso,secoes:{...}}`; somente itens selecionados |

Tipos de sintomas: COLICA, FADIGA, ENERGIA, BEM_ESTAR, INCHACO, DOR, HUMOR. Intensidade inteira 0–10 (energia/bem-estar: maior = melhor; demais: maior = mais intenso). Fluxo: LEVE, MODERADO, INTENSO. Compromissos: CONSULTA, EXAME, RETORNO. Datas de registros não podem ser posteriores ao dia da demo; agenda permite futuro. Sono 0–1440 min; FC 20–250; passos 0–100000; temperatura delta -5 a 5 (medidas fictícias, limites técnicos).

Erros: `{mensagem:"..."}` com 400 (validação), 404 (usuária inexistente), 409 (duplicidade/conflito). Requisições inválidas não alteram dados. Sem autenticação: servidor restrito a localhost; não usar pacientes reais. CORS permite apenas http://localhost:5173 e http://127.0.0.1:5173 por padrão. PDF produzido no frontend por jsPDF a partir do mesmo JSON da prévia.

Regras: A compara últimos 3 dias com 30 anteriores, janelas sem sobreposição; exige 3 e 21 medidas. B conta dias distintos com fadiga >= 6 nos últimos 7 dias (recorrência >= 3 dias) e apresenta média de passos nos dias com fadiga versus demais dias disponíveis. C lista agendamentos entre agora e +30 dias, além de agendamentos passados ainda não resolvidos. Limiares são demonstrativos, sem validação clínica. Relatório com insights pode revelar dados derivados de outras categorias: o seletor informa isso explicitamente.
