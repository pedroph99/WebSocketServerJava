package webSocketServer;

import java.net.ServerSocket;
import java.net.Socket;

// 
public interface WebsocketServerInterface {
	void startServer();
	void configureServer();
	String[] getServerIPPort();
}	
