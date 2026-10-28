# Lumi MVP â€” Luz sobre vocÃª

**O seu relÃ³gio mede. A Lumi leva isso atÃ© a consulta.**

AplicaÃ§Ã£o local demonstrÃ¡vel: React + Vite, API Java 21/Spring Boot, H2 em arquivo, regras explicÃ¡veis e PDF. Somente dados fictÃ­cios. Demo ancorada em **27/10/2026**; relÃ³gio fixo para repetir o ensaio. NÃ£o Ã© software clÃ­nico.

## InÃ­cio mais rÃ¡pido (pacote entregue)

Instale **JDK 21**. No terminal, entre na pasta `lumi-mvp` extraÃ­da do ZIP:

```sh
java -jar release/lumi-1.0.0.jar
```

Abra **http://localhost:8080**. O JAR inclui o frontend compilado e cria automaticamente o banco H2 em `data/lumi.mv.db`, relativo Ã  pasta do comando. NÃ£o precisa instalar banco, Maven ou Node para executar o JAR. Internet sÃ³ Ã© necessÃ¡ria para instalar o Java; o aplicativo funciona localmente. Ctrl+C encerra.

Windows: confirme `java -version` no PowerShell. Se o Java nÃ£o for reconhecido, instale JDK 21 e abra um novo terminal. Execute a partir da pasta extraÃ­da, nunca dentro da visualizaÃ§Ã£o do ZIP.

## Executar e modificar o cÃ³digo

Requisitos: JDK 21, Maven 3.9+, Node.js 22+ e npm. Primeira instalaÃ§Ã£o requer internet.

Terminal 1:
```sh
cd backend
mvn spring-boot:run
```
Terminal 2:
```sh
cd frontend
npm ci
npm run dev
```
Abra http://localhost:5173. Vite encaminha `/api` para localhost:8080. O backend usa `backend/data/lumi.mv.db` nesse modo. NÃ£o use duas APIs sobre o mesmo banco simultaneamente.

## Compilar e testar

```sh
cd backend
mvn test
cd ../frontend
npm ci
npm run build
```
Para gerar novamente o executÃ¡vel com frontend, na raiz:
```sh
python scripts/package.py
```
O script executa build React, copia `dist` para recursos estÃ¡ticos e roda `mvn clean package` (inclui testes), gerando `release/lumi-1.0.0.jar`.

Testes de navegador opcionais para execuÃ§Ã£o local: veja `frontend/e2e/flow.spec.js`. Requer API e frontend ligados e banco de demonstraÃ§Ã£o limpo. Execute `npx playwright install chromium` e `npx playwright test` em `frontend`. Eles inserem dados fictÃ­cios. Consulte `docs/VALIDACAO.md` para distinguir testes efetivamente executados aqui de testes fornecidos.

## O que estÃ¡ implementado

- **Home:** sinais simulados do dia, prÃ³ximos compromissos e atalhos.
- **Cycle:** calendÃ¡rio navegÃ¡vel, perÃ­odos e fluxo, sintomas 0â€“10, histÃ³rico, previsÃ£o mÃ©dia com limitaÃ§Ãµes.
- **Care:** registros de fadiga, energia, bem-estar e dor; histÃ³rico e texto educativo, sem risco clÃ­nico.
- **Coach:** regras A/B/C no Java; origem por registro, janelas e limitaÃ§Ãµes. Recalcula apÃ³s cada inclusÃ£o.
- **Agenda:** cadastrar consultas, exames e retornos; listar; avisos no Home e Coach.
- **RelatÃ³rio:** seleÃ§Ã£o explÃ­cita de categorias, prÃ©via da resposta da API, download PDF paginado. Insights incluem suas prÃ³prias fontes; aviso antes da seleÃ§Ã£o.
- **API:** validaÃ§Ã£o, erros JSON, CORS restrito, separaÃ§Ã£o controller/service/repository/regras.
- **H2:** 90 sinais, 3 menstruaÃ§Ãµes, sintomas e agenda; `Random(42)`; seed transacional somente se persona nÃ£o existe; restriÃ§Ã£o Ãºnica por dia para sinais.

## Ensaio em 2 minutos

1. Home: mostre â€œdados fictÃ­ciosâ€ e sono/atividade.
2. Coach: regra B comeÃ§a com dois dias de fadiga.
3. Care: registre Fadiga, intensidade 7, em 27/10/2026.
4. HistÃ³rico mostra o novo registro. Coach muda para **Fadiga recorrente registrada** (3 dias distintos).
5. Abra â€œVer origem da anÃ¡liseâ€; mostre IDs, datas e sinais usados.
6. Agenda: cadastre â€œRetorno demonstrativoâ€ para 28/10/2026 Ã s 15h; confira Home/Coach.
7. RelatÃ³rio: selecione itens, gere prÃ©via, toque em Preparar PDF e use Salvar PDF ou Abrir PDF.

## PersistÃªncia e restauraÃ§Ã£o da demo

Reiniciar mantÃ©m inclusÃµes e nÃ£o repete seed. Para voltar ao inÃ­cio, **pare a API e renomeie** a pasta `data` usada (por exemplo, para `data-ensaio-1`). Ao iniciar novamente, um banco limpo Ã© criado. Assim o ensaio anterior Ã© preservado. O mesmo dia pode conter vÃ¡rios sintomas, mas a regra B conta dias distintos.

A data pode ser alterada com `--lumi.demo-date=2026-10-28` ao executar o JAR. Para reconstruir todos os dados em torno de outra data, use um novo banco com `--spring.datasource.url=jdbc:h2:file:./data-nova/lumi`. Alterar sÃ³ a data nÃ£o reescreve o banco existente.

## LimitaÃ§Ãµes e roadmap

Sem wearable real, Lumi Band, ML/deep learning, chatbot generativo, rede de clÃ­nicas, credenciamento, notificaÃ§Ãµes externas, envio de PDF, autenticaÃ§Ã£o ou multiusuÃ¡rio. NÃ£o hÃ¡ ediÃ§Ã£o/exclusÃ£o de registros nem confirmaÃ§Ã£o de realizaÃ§Ã£o da agenda; compromissos passados sÃ£o descritos como nÃ£o confirmados. FC e temperatura sÃ£o exibidas, nÃ£o interpretadas. PrevisÃ£o nÃ£o estima ovulaÃ§Ã£o e nÃ£o Ã© contraceptivo. NÃ£o hÃ¡ validaÃ§Ã£o clÃ­nica dos limiares.

Ã‰ uma demo local, nÃ£o um produto pronto para pacientes: **nÃ£o digite dados reais e nÃ£o exponha o servidor Ã  internet**. A escolha de itens do PDF Ã© controle de exportaÃ§Ã£o local, nÃ£o uma implementaÃ§Ã£o completa de consentimento/LGPD. Fontes visuais sÃ£o locais no pacote; nenhuma telemetria foi implementada.

Mais detalhes: [API](docs/API.md), [arquitetura](docs/ARQUITETURA.md), [pitch e perguntas](docs/PITCH.md), [validaÃ§Ã£o](docs/VALIDACAO.md).

## VersÃ£o mobile

Veja [MOBILE.md](docs/MOBILE.md). No Windows, `INICIAR-MOBILE.bat` permite abrir a demo no celular pela mesma rede Wi-Fi privada. A versÃ£o entregue Ã© web responsiva; nÃ£o Ã© um APK.

