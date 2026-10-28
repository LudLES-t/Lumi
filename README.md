# Lumi — dados que ajudam a preparar a conversa

> O seu relógio mede. A Lumi organiza, explica e leva contexto para a consulta.

O Lumi é um MVP de acompanhamento pessoal de saúde e bem-estar. Ele reúne sinais simulados, ciclo, sintomas, registros de cuidado, agenda e relatórios em uma experiência web local — com regras transparentes, fontes visíveis e linguagem cuidadosa.

Este repositório contém uma demonstração técnica, não um produto clínico. Todos os dados são fictícios. Não insira dados reais nem exponha o servidor à internet.

## Por que o Lumi existe

Dados de saúde costumam ficar espalhados entre aplicativos, anotações e memórias. O Lumi explora uma forma mais simples de transformar esse histórico em contexto útil para a própria pessoa e para a conversa com profissionais de saúde, sem fingir que uma regra é um diagnóstico.

## O que está na demonstração

- **Home:** sinais simulados do dia, próximos compromissos e atalhos.
- **Cycle:** calendário, períodos, sintomas de 0 a 10, histórico e previsão média com limitações explícitas.
- **Care:** registros de fadiga, energia, bem-estar e dor, com histórico e conteúdo educativo.
- **Coach:** regras A/B/C implementadas em Java, com origem dos registros, janelas analisadas e limitações.
- **Agenda:** consultas, exames e retornos, com avisos no Home e no Coach.
- **Relatório:** seleção explícita de categorias, prévia baseada na API e PDF paginado para uso local.
- **API:** validação, erros JSON, CORS restrito e separação entre controller, service, repository e regras.

## Início mais rápido (pacote entregue)

Instale **JDK 21**. No terminal, entre na pasta `lumi-mvp` extraída do ZIP:

```sh
java -jar release/lumi-1.0.0.jar
```

Abra **http://localhost:8080**. O JAR inclui o frontend compilado e cria automaticamente o banco H2 em `data/lumi.mv.db`, relativo à pasta do comando. Não precisa instalar banco, Maven ou Node para executar o JAR. Internet só é necessária para instalar o Java; o aplicativo funciona localmente. Ctrl+C encerra.

Windows: confirme `java -version` no PowerShell. Se o Java não for reconhecido, instale JDK 21 e abra um novo terminal. Execute a partir da pasta extraída, nunca dentro da visualização do ZIP.

## Executar e modificar o código

Requisitos: JDK 21, Maven 3.9+, Node.js 22+ e npm. Primeira instalação requer internet.

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
Abra http://localhost:5173. Vite encaminha `/api` para localhost:8080. O backend usa `backend/data/lumi.mv.db` nesse modo. Não use duas APIs sobre o mesmo banco simultaneamente.

## Compilar e testar

```sh
cd backend
mvn test
cd ../frontend
npm ci
npm run build
```
Para gerar novamente o executável com frontend, na raiz:
```sh
python scripts/package.py
```
O script executa build React, copia `dist` para recursos estáticos e roda `mvn clean package` (inclui testes), gerando `release/lumi-1.0.0.jar`.

Testes de navegador opcionais para execução local: veja `frontend/e2e/flow.spec.js`. Requer API e frontend ligados e banco de demonstração limpo. Execute `npx playwright install chromium` e `npx playwright test` em `frontend`. Eles inserem dados fictícios. Consulte `docs/VALIDACAO.md` para distinguir testes efetivamente executados aqui de testes fornecidos.

## O que está implementado

- **Home:** sinais simulados do dia, próximos compromissos e atalhos.
- **Cycle:** calendário navegável, períodos e fluxo, sintomas 0–10, histórico, previsão média com limitações.
- **Care:** registros de fadiga, energia, bem-estar e dor; histórico e texto educativo, sem risco clínico.
- **Coach:** regras A/B/C no Java; origem por registro, janelas e limitações. Recalcula após cada inclusão.
- **Agenda:** cadastrar consultas, exames e retornos; listar; avisos no Home e Coach.
- **Relatório:** seleção explícita de categorias, prévia da resposta da API, download PDF paginado. Insights incluem suas próprias fontes; aviso antes da seleção.
- **API:** validação, erros JSON, CORS restrito, separação controller/service/repository/regras.
- **H2:** 90 sinais, 3 menstruações, sintomas e agenda; `Random(42)`; seed transacional somente se persona não existe; restrição única por dia para sinais.

## Ensaio em 2 minutos

1. Home: mostre “dados fictícios” e sono/atividade.
2. Coach: regra B começa com dois dias de fadiga.
3. Care: registre Fadiga, intensidade 7, em 27/10/2026.
4. Histórico mostra o novo registro. Coach muda para **Fadiga recorrente registrada** (3 dias distintos).
5. Abra “Ver origem da análise”; mostre IDs, datas e sinais usados.
6. Agenda: cadastre “Retorno demonstrativo” para 28/10/2026 às 15h; confira Home/Coach.
7. Relatório: selecione itens, gere prévia, toque em Preparar PDF e use Salvar PDF ou Abrir PDF.

## Persistência e restauração da demo

Reiniciar mantém inclusões e não repete seed. Para voltar ao início, **pare a API e renomeie** a pasta `data` usada (por exemplo, para `data-ensaio-1`). Ao iniciar novamente, um banco limpo é criado. Assim o ensaio anterior é preservado. O mesmo dia pode conter vários sintomas, mas a regra B conta dias distintos.

A data pode ser alterada com `--lumi.demo-date=2026-10-28` ao executar o JAR. Para reconstruir todos os dados em torno de outra data, use um novo banco com `--spring.datasource.url=jdbc:h2:file:./data-nova/lumi`. Alterar só a data não reescreve o banco existente.

## Limitações e roadmap

Sem wearable real, Lumi Band, ML/deep learning, chatbot generativo, rede de clínicas, credenciamento, notificações externas, envio de PDF, autenticação ou multiusuário. Não há edição/exclusão de registros nem confirmação de realização da agenda; compromissos passados são descritos como não confirmados. FC e temperatura são exibidas, não interpretadas. Previsão não estima ovulação e não é contraceptivo. Não há validação clínica dos limiares.

É uma demo local, não um produto pronto para pacientes: **não digite dados reais e não exponha o servidor à internet**. A escolha de itens do PDF é controle de exportação local, não uma implementação completa de consentimento/LGPD. Fontes visuais são locais no pacote; nenhuma telemetria foi implementada.

Mais detalhes: [API](docs/API.md), [arquitetura](docs/ARQUITETURA.md), [pitch e perguntas](docs/PITCH.md), [validação](docs/VALIDACAO.md).

## Versão mobile

Veja [MOBILE.md](docs/MOBILE.md). No Windows, `INICIAR-MOBILE.bat` permite abrir a demo no celular pela mesma rede Wi-Fi privada. A versão entregue é web responsiva; não é um APK.

