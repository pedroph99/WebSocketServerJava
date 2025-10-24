package webSocketServer;

import java.net.Socket;
import java.util.UUID;

public class ClientConnection {
    private final UUID id;
    private final Socket socket;

    public ClientConnection(Socket socket) {
        this.id = UUID.randomUUID(); // Gera um identificador único
        this.socket = socket;
    }

    public UUID getId() {
        return id;
    }

    public Socket getSocket() {
        return socket;
    }

    @Override
    public String toString() {
        return "ClientConnection{id=" + id + ", address=" + socket.getInetAddress() + "}";
    }
}