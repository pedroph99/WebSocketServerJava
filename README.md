# WebSocketServerJava

## Visão geral
Este repositório contém uma implementação didática de um servidor WebSocket escrito em Java. O código demonstra como realizar o handshake HTTP inicial, decodificar e codificar frames WebSocket manualmente e gerenciar múltiplos clientes conectados utilizando `java.net.Socket`. O objetivo é servir como base de estudo para quem deseja compreender o protocolo sem depender de bibliotecas de alto nível.

## Principais recursos
- **Handshake completo**: geração do cabeçalho `Sec-WebSocket-Accept` a partir da chave enviada pelo cliente e resposta `HTTP/1.1 101` personalizada.
- **Gerenciamento de conexões**: cada cliente recebe um `UUID` exclusivo e é rastreado por meio de `ClientManager`, permitindo recuperá-lo de forma thread-safe.
- **Envio e leitura de frames**: utilitário que constrói frames de texto (FIN + opcode 0x1) e realiza a remoção da máscara de payload recebida de clientes WebSocket padrão.
- **Tratamento de mensagens em JSON**: `ResponseHandler` interpreta o payload esperado (`id`, `username`, `message`) e envia a resposta formatada apenas ao cliente alvo.

## Estrutura do projeto
```text
WebSocketServerJava/
├── README.md
└── webSocketServer/
    ├── src/
    │   ├── module-info.java
    │   └── webSocketServer/
    │       ├── ClientConnection.java
    │       ├── ClientManager.java
    │       ├── MainWebSocketServer.java
    │       ├── ResponseHandler.java
    │       ├── WebSocketUtils.java
    │       ├── WebsocketServer.java
    │       └── WebsocketServerInterface.java
    └── .classpath (configuração Eclipse que aponta para `json-20231013.jar`)
```

## Pré-requisitos
- **Java 21** (ou versão compatível). O projeto foi configurado originalmente em um ambiente Eclipse com JavaSE-21.
- **Biblioteca org.json**. A classe `ResponseHandler` depende do pacote `org.json`. Você pode baixar a versão atual em <https://repo1.maven.org/maven2/org/json/json/> (por exemplo, `json-20231013.jar`).

## Como compilar e executar
1. Crie uma pasta `libs` na raiz do projeto e copie `json-20231013.jar` para dentro dela:
   ```bash
   mkdir -p libs
   cp /caminho/para/json-20231013.jar libs/
   ```
2. Compile o módulo para a pasta `out`:
   ```bash
   cd webSocketServer
   javac \
     --module-path ../libs/json-20231013.jar \
     -d ../out \
     $(find src -name "*.java")
   ```
3. Inicie o servidor WebSocket (por padrão `127.0.0.1:8081`):
   ```bash
   cd ..
   java \
     --module-path out:libs/json-20231013.jar \
     --module webSocketServer/webSocketServer.MainWebSocketServer
   ```
4. Use qualquer cliente WebSocket para conectar (por exemplo, `wscat`, Postman ou uma aplicação frontend). O terminal exibirá os cabeçalhos do handshake e as mensagens recebidas.

> 💡 Para alterar o IP ou porta, edite `MainWebSocketServer` ou utilize um construtor diferente ao instanciar `WebsocketServer`.

## Formato de mensagem esperado
O servidor aguarda mensagens de texto em JSON com o formato abaixo:
```json
{
  "id": "<UUID do cliente>",
  "username": "nome",
  "message": "conteúdo"
}
```
O `ClientManager` mantém o mapeamento `UUID -> Socket`, permitindo que `ResponseHandler` envie uma mensagem de volta apenas ao cliente correspondente.

## Próximos passos sugeridos
- Implementar broadcast para todos os clientes conectados.
- Adicionar tratamento de desconexões para remover clientes do `ClientManager` adequadamente.
- Introduzir logs estruturados e testes automatizados para o handshake e manipulação de frames.

## Licença
Este projeto não possui uma licença explícita. Considere adicionar uma antes de distribuir ou reutilizar o código.
