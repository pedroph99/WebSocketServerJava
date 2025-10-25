package webSocketServer;

import java.io.IOException;
import java.util.UUID;

import org.json.JSONObject;
public  class ResponseHandler {
	
	public static void handler(String message, ClientManager currentClientManager) {
		 JSONObject json = new JSONObject(message);
		 
		 String uuidUser = json.getString("id");
		 UUID uuidUserStringtoUUID = UUID.fromString(uuidUser);
		 String messageContent = json.getString("message");
		 String username = json.getString("username");
		 
		 String formattedMessage = String.format("%s: %s", username, messageContent);
		 
		 ClientConnection referredClient = currentClientManager.getClient(uuidUserStringtoUUID);
		 try {
			WebSocketUtils.sendMessage(referredClient.getSocket().getOutputStream(), formattedMessage);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		 
		 
	}
	
	
	
	
	
}
