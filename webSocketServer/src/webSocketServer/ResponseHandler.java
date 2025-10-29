package webSocketServer;

import java.io.IOException;
import org.json.*;
import java.util.UUID;

import org.json.JSONObject;
public  class ResponseHandler {
	
	public static void handler(String message, ClientManager currentClientManager, UUID clientID) {
		 JSONObject json = new JSONObject(message);
		 
		 
		 
		 if (json.has("userSetter")){
			// UUID clientUUID = currentClientManager.getClientId(currentClientManager.);
			currentClientManager.setClientUsername(clientID, json.getString("userSetter"));
		
			 return;
		 }
		 
		 UUID UUUIDusernameToSend = null;
		 if (json.has("username")) {
			 String usernameToSend = json.getString("username");
			 UUUIDusernameToSend = currentClientManager.getClientIdByUsername(usernameToSend);
		 }
		 
		 if (json.has("id")) {
			 String uuidUser = json.getString("id");
			 UUUIDusernameToSend = UUID.fromString(uuidUser);
		 }
		 
		 String messageContent = json.getString("message");
		 //String username = json.getString("username");
		 String senderUsername = null;
		 senderUsername = currentClientManager.getClientUsername(clientID);
		 if (senderUsername == null) {
			 senderUsername = "Anonymous user";
		 }
		 String formattedMessage = String.format("%s: %s", senderUsername, messageContent);
		 
		 ClientConnection referredClient = currentClientManager.getClient(UUUIDusernameToSend);
		 try {
			WebSocketUtils.sendMessage(referredClient.getSocket().getOutputStream(), formattedMessage);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		 
		 
	}
	
	
	
	
	
}
