package webSocketServer;

import java.net.Socket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClientManager {
	private static final Map<UUID, ClientConnection> clients = new ConcurrentHashMap<>();

    public static void addClient(ClientConnection connection) {
        clients.put(connection.getId(), connection);
        System.out.println("🔗 Cliente conectado: " + connection);
    }

    public static void removeClient(UUID id) {
        clients.remove(id);
        System.out.println("❌ Cliente removido: " + id);
    }

    public static Map<UUID, ClientConnection> getClients() {
        return clients;
    }
}
