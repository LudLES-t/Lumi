# Entrega mobile

Versão web responsiva, 28/10/2026. Mesmas seis áreas e banco H2 do MVP. Backend permanece Java 21.

- Menu inferior com identificação da tela ativa.
- Cabeçalho fixo, safe areas e controles com tamanho de toque adequado.
- Formulários legíveis e sem zoom por fontes pequenas.
- Calendário e relatórios ajustados para telas estreitas.
- Histórico e fontes sem rolagem interna no celular.
- Retorno ao topo ao navegar.
- PDF e fonte carregados somente ao preparar o documento; bundle inicial de aplicação ~69,6 KB gzip no build verificado.
- Links separados para salvar e abrir o PDF; sem envio automático de dados.
- Timeout e mensagens para falhas da API.
- Inicializadores Windows para desktop e rede local; guia MOBILE.md.

Verificado: 18 combinações de tela/largura sem overflow horizontal, cadastro mobile persistido e geração da prévia/Blob PDF. Não confundir viewport de navegador com teste em hardware: Safari/iOS, teclado virtual e download final em aparelho físico ainda precisam de ensaio.
