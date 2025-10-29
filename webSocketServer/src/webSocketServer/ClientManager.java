package webSocketServer;

import java.net.Socket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
