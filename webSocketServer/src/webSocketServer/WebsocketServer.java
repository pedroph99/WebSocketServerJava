/**
 * 
 */
package webSocketServer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket; // cria o servidor TCP (porta de escuta)
import java.net.Socket;       // representa uma conexão com um cliente
import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;
/**
 * 
 * 
 * Cr
 */
public class WebsocketServer implements WebsocketServerInterface {
	private int  serverPort;
	private String serverIP;
	private ClientManager listaClientes;
	public WebsocketServer(String  ip, int port) {
		// TODO Auto-generated constructor stub
		this.configureServer(ip, port);
		this.listaClientes = new ClientManager();
	}
	@Override
	public void startServer() throws IOException {
		System.out.print("STARTING SERVER ON IP: ");
		System.out.print( this.serverIP);
		System.out.print("  AND ON PORT: ");
		System.out.print(this.serverPort);
		
		final ServerSocket server = new ServerSocket(this.serverPort);
		try {
			
			while (true) {
	            // Aceita o cliente (bloqueia até alguém conectar)
	            Socket client = server.accept();

	            System.out.println("Novo cliente: " + client.getInetAddress());

	            // Cria uma nova thread para esse cliente
	            new Thread(() -> {
	                UUID uuidCurrentClient = null; // Armazenar o ID do cliente
	                try {
	                    uuidCurrentClient = this.handleHandshake(client);
                        
                        if (uuidCurrentClient == null) {
                            // Handshake falhou ou cliente fechou conexão prematuramente
                            return; 
                        }
                        
	                    try {
	                        InputStream in = client.getInputStream();
	                        OutputStream out = client.getOutputStream();

	                        while (true) {
	                            String message = WebSocketUtils.readMessage(in);
	                            if (message == null) break; // conexão fechada
	                            System.out.println("📩 [" + client.getInetAddress() + "] " + message);

	                           
	                            ResponseHandler.handler(message, this.listaClientes, uuidCurrentClient );
	                        }
                            
                            // Desconexão de um usuário
                            System.out.println("Cliente desconectado normalmente: " + client.getInetAddress());

	                    } catch (IOException e) {
	                        System.out.println("Erro lendo mensagens ou desconexão forçada: " + e.getMessage());
	                    } finally {
                             // Garante a remoção do cliente, e atualiza a lista de usuários
                             if (uuidCurrentClient != null) {
                                ClientManager.removeClient(uuidCurrentClient);
                                // Broadcast para que o cliente.js de todos os outros clientes atualize a lista
                                ClientManager.broadcastUserList();
                             }
                            try {
                                client.close(); // Fecha o socket
                            } catch (IOException e) {
                                // Ignora, pois estamos fechando o socket
                            }
                        }
                        
	                } catch (Exception e) {
	                    System.out.println("Erro na conexão: " + e.getMessage());
                         // Se o erro ocorrer antes de obter o UUID, não há nada para remover.
	                }
	            }).start();
	            
			}
		} catch (IOException e) {

			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		
	}

	@Override
	public void configureServer(String ip, int port) {
		this.serverIP = ip;
		this.serverPort = port;
		
	}

	@Override
	public String[] getServerIPPort() {
		String[] serverInfos = new String[2];
		serverInfos[0] = this.serverIP;
		serverInfos[1] = Integer.toString(serverPort);
		return serverInfos;
}
	
	private UUID handleHandshake(Socket client) {
        try {
            InputStream in = client.getInputStream();
            OutputStream out = client.getOutputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String line;
            String webSocketKey = null;

            // 1️⃣ Lê os cabeçalhos HTTP até a linha em branco
            while (true) {
                line = reader.readLine();
                if (line == null || line.isEmpty()) break;
                
                System.out.println("Header: " + line);
                if (line.startsWith("Sec-WebSocket-Key:")) {
                    webSocketKey = line.substring("Sec-WebSocket-Key:".length()).trim();
                }
            }

            if (webSocketKey == null) {
                System.out.println("⚠️ Cabeçalho Sec-WebSocket-Key não encontrado ou conexão fechada prematuramente.");
                client.close();
                return null;
            }

            // 2️⃣ Gera o valor do cabeçalho Sec-WebSocket-Accept
            String acceptKey = generateAcceptKey(webSocketKey);

            // 3️⃣ Monta a resposta HTTP 101
            String response = 
                "HTTP/1.1 101 Switching Protocols\r\n" +
                "Upgrade: websocket\r\n" +
                "Connection: Upgrade\r\n" +
                "Sec-WebSocket-Accept: " + acceptKey + "\r\n\r\n";

            // 4️⃣ Envia ao cliente
            out.write(response.getBytes("UTF-8"));
            out.flush();

            System.out.println("✅ Handshake concluído com sucesso!\n");
            System.out.println(acceptKey);
            
            
            // A partir daqui, o canal é WebSocket — pode ler frames binários
            // (exemplo: use readMessage() que você já viu antes)
            
         // ✅ 5️⃣ Cria a conexão do cliente
            ClientConnection connection = new ClientConnection(client);
            ClientManager.addClient(connection);

            // (Opcional) envie uma mensagem de boas-vindas com o ID
            WebSocketUtils.sendMessage(out, "Conexão aceita! Seu ID é: " + connection.getId());
            return connection.getId();
            // A partir daqui, pode começar a ler frames do cliente (loop de mensagens)
        } catch (Exception e) {
            System.out.println("Erro no handshake: " + e.getMessage());
            try {
                client.close(); // Garante que o socket é fechado em caso de falha no handshake
            } catch (IOException ioException) {
                // Ignora
            }
        }
		return null;
    }

    // 🔐 Método que gera o Sec-WebSocket-Accept
    private static String generateAcceptKey(String key) throws Exception {
        String magicString = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        String combined = key + magicString;
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] hash = sha1.digest(combined.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(hash);
    }
}


