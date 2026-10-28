# Lumi mobile — 28/10/2026

A mesma aplicação React funciona no navegador do celular, consumindo a API Java. Esta entrega não contém APK, app nativo iOS, instalação PWA ou integração com sensores.

## Abrir no seu celular

1. Extraia o ZIP no computador e instale Java 21.
2. Conecte computador e celular à mesma rede Wi-Fi privada.
3. No Windows, execute `INICIAR-MOBILE.bat`. O script mostra `ipconfig` e inicia o JAR ouvindo na rede local. Anote o IPv4 da conexão Wi-Fi do computador.
4. No navegador do celular, acesse `http://IP-DO-COMPUTADOR:8080` (substitua pelo IPv4 encontrado).
5. Mantenha o computador ligado. Ctrl+C no terminal encerra o servidor.

Se o firewall solicitar acesso, limite-o à rede privada usada na demonstração. Em Wi-Fi de instituição com isolamento entre clientes, os aparelhos podem não se comunicar. Não abra portas do roteador nem use dados de pacientes: todos na rede que alcançarem essa porta poderão acessar a mesma persona fictícia, pois o MVP não tem autenticação.

Alternativa no macOS/Linux, a partir da raiz:
```sh
java -jar release/lumi-1.0.0.jar --server.address=0.0.0.0
```

Para demonstrar apenas no computador, use `INICIAR.bat` ou o comando do README sem a opção de rede. No celular, `localhost` significa o próprio celular: use o endereço do computador.

## Ajustes implementados

- Navegação inferior com seis áreas, estado ativo e áreas de toque maiores.
- Cabeçalho com marca e suporte CSS às áreas seguras do aparelho.
- Campos de 16 px, evitando o zoom automático típico dos formulários no iOS.
- Calendário, cartões e relatórios em coluna única; sem rolagem horizontal nas larguras verificadas.
- Histórico e fontes do Coach sem painéis de rolagem aninhados no mobile.
- Mudança de tela retorna ao topo.
- PDF carregado sob demanda; botão “Preparar PDF” gera um arquivo e apresenta “Salvar PDF” e “Abrir PDF”. No celular, o visualizador do navegador pode oferecer salvar/compartilhar.
- Erros de rede e timeout de 15 segundos com mensagem visível.

## Verificação

Navegador Chromium remoto, aplicativo real dentro de viewports de 320, 390 e 430 px. Todas as seis telas foram navegadas: 18 verificações de largura sem overflow horizontal. A barra de rolagem do navegador ocupa 15 px nesses testes (conteúdo útil 305/375/415 px).

Check-in de energia salvo pela interface e confirmado no histórico persistido da API. Prévia do relatório e geração do Blob PDF confirmadas, com links de salvar/abrir. O navegador de automação não confirmou o evento de download, portanto o salvamento final do arquivo no dispositivo ainda precisa de teste manual. O mesmo gerador produz o PDF de exemplo verificado incluído no pacote.

Não houve teste em aparelho físico, Safari/iOS ou teclado virtual real; safe areas e prevenção de zoom estão implementadas, sem validação nesses dispositivos. Não há funcionamento offline da API.
