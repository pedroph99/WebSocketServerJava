package webSocketServer;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;
public class ClientManager {
	private static final Map<UUID, ClientConnection> clients = new ConcurrentHashMap<>();
	private static final Map<UUID, String> clientsUsername = new ConcurrentHashMap<>();
    public static void addClient(ClientConnection connection) {
        clients.put(connection.getId(), connection);
        System.out.println("🔗 Cliente conectado: " + connection);
    }
    
    public static void setClientUsername(UUID idClient, String username) {
    	clientsUsername.put(idClient, username);
    	System.out.println("🔗 Nome de usuário colocado com sucesso: " + username);
    }

    public static void removeClient(UUID id) {
        clients.remove(id);
        clientsUsername.remove(id); // Removendo o username também
        System.out.println("❌ Cliente removido: " + id);
    }

    public static Map<UUID, ClientConnection> getClients() {
        return clients;
    }
    public static ClientConnection getClient(UUID id) {
        return clients.get(id);}
    public static String getClientUsername(UUID id) {
    	return clientsUsername.get(id);
    }
    
    public static String[] getAllUsernames() {
        return clientsUsername.values().toArray(new String[0]);
    }
    
    // Método para enviar lista de usuários ativos
    public static void broadcastUserList() {
        String[] usernames = getAllUsernames();
        // Constrói o JSON no formato esperado pelo client.js: {"type": "userList", "users": ["user1", "user2"]}
        JSONObject userListJson = new JSONObject();
        userListJson.put("type", "userList");
        userListJson.put("users", usernames);
        
        String message = userListJson.toString();
        
        // Broadcast para todos os clientes
        for (ClientConnection client : clients.values()) {
            try {
                WebSocketUtils.sendMessage(client.getSocket().getOutputStream(), message);
            } catch (IOException e) {
                System.err.println("Erro ao enviar lista de usuários para o cliente " + client.getId() + ": " + e.getMessage());

            }
        }
    }
    
    public static UUID getClientId(ClientConnection connection) {
        for (Map.Entry<UUID, ClientConnection> entry : clients.entrySet()) {
            if (entry.getValue().equals(connection)) {
                return entry.getKey();
            }
        }
        return null; // caso não encontre
    }
    
    
    public static UUID getClientIdByUsername(String username) {
        for (Map.Entry<UUID, String> entry : clientsUsername.entrySet()) {
            if (entry.getValue().equals(username)) {
                return entry.getKey();
            }
        }
        return null; // retorna null se não encontrar
    }
}