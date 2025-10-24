package webSocketServer;

import java.io.IOException;

public class MainWebSocketServer {

	public static void main(String[] args)  {
		// TODO Auto-generated method stub
		WebsocketServer server = new WebsocketServer("127.0.0.1", 8081);
		try {
			server.startServer();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
}
