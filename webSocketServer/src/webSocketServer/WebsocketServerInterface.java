package webSocketServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

// 
public interface WebsocketServerInterface {
	void startServer() throws IOException;
	void configureServer(String ip, int port);
	String[] getServerIPPort();
}	
