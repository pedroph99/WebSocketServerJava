package webSocketServer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GameManager {
    // Mapeia o ID do cliente para o ID do jogo ativo
    private static final Map<UUID, UUID> activePlayerGames = new ConcurrentHashMap<>();
    // Mapeia o ID do jogo para a instância do jogo
    private static final Map<UUID, Game> activeGames = new ConcurrentHashMap<>();

    // 1. Inicia um novo jogo
    public static Game createNewGame(UUID challengerId, UUID opponentId) {
        // Encerra qualquer jogo anterior de ambos
        endGameForPlayer(challengerId);
        endGameForPlayer(opponentId);
        
        Game game = new Game(challengerId, opponentId);
        activeGames.put(game.getGameId(), game);
        activePlayerGames.put(challengerId, game.getGameId());
        activePlayerGames.put(opponentId, game.getGameId());
        System.out.println("🎉 Novo jogo iniciado. ID: " + game.getGameId());
        return game;
    }

    //Obtém o jogo ativo de um jogador
    public static Game getGameForPlayer(UUID playerId) {
        UUID gameId = activePlayerGames.get(playerId);
        return gameId != null ? activeGames.get(gameId) : null;
    }

    //Remove o jogo quando terminar
    public static void endGame(UUID gameId) {
        Game game = activeGames.remove(gameId);
        if (game != null) {
            activePlayerGames.remove(game.getPlayerXId());
            activePlayerGames.remove(game.getPlayerOId());
            System.out.println("❌ Jogo finalizado e removido. ID: " + gameId);
        }
    }
    
    //Remove o jogo se um jogador se desconectar
    public static void endGameForPlayer(UUID playerId) {
        UUID gameId = activePlayerGames.get(playerId);
        if (gameId != null) {
             endGame(gameId);
        }
    }
    
}