package webSocketServer;

import java.util.Arrays;
import java.util.UUID;

public class Game {
    private final UUID gameId;
    private final UUID playerXId;
    private final UUID playerOId;
    private char[] board; // ' ' para vazio, 'X' ou 'O'
    private UUID currentPlayerId;
    private boolean isActive;

    public Game(UUID challengerId, UUID opponentId) {
        this.gameId = UUID.randomUUID();
        this.playerXId = challengerId; // Quem desafia é sempre X
        this.playerOId = opponentId;
        this.board = new char[9];
        Arrays.fill(this.board, ' '); // Inicializa o tabuleiro vazio
        this.currentPlayerId = playerXId; // X sempre começa
        this.isActive = true;
    }

    public UUID getGameId() {
        return gameId;
    }

    public UUID getOpponentId(UUID playerId) {
        return playerId.equals(playerXId) ? playerOId : playerXId;
    }

    public UUID getPlayerXId() {
        return playerXId;
    }

    public UUID getPlayerOId() {
        return playerOId;
    }
    
    public char getPlayerMark(UUID playerId) {
        if (playerId.equals(playerXId)) return 'X';
        if (playerId.equals(playerOId)) return 'O';
        return ' ';
    }
    
    public UUID getCurrentPlayerId() {
        return currentPlayerId;
    }

    public boolean isActive() {
        return isActive;
    }

    // Tenta fazer um movimento (posição é de 0 a 8)
    public boolean makeMove(UUID playerId, int position) {
        if (!isActive || !playerId.equals(currentPlayerId) || position < 0 || position > 8 || board[position] != ' ') {
            return false; // Jogada inválida
        }

        board[position] = getPlayerMark(playerId);
        
        // Verifica se houve vitória
        if (checkWin()) {
            isActive = false;
        } else if (checkDraw()) {
            isActive = false;
        } else {
            // Troca a vez
            currentPlayerId = getOpponentId(playerId);
        }
        
        return true;
    }

    private boolean checkWin() {
        // Todas as 8 combinações de vitória (linhas, colunas, diagonais)
        return (board[0] != ' ' && board[0] == board[1] && board[0] == board[2]) ||
               (board[3] != ' ' && board[3] == board[4] && board[3] == board[5]) ||
               (board[6] != ' ' && board[6] == board[7] && board[6] == board[8]) ||
               (board[0] != ' ' && board[0] == board[3] && board[0] == board[6]) ||
               (board[1] != ' ' && board[1] == board[4] && board[1] == board[7]) ||
               (board[2] != ' ' && board[2] == board[5] && board[2] == board[8]) ||
               (board[0] != ' ' && board[0] == board[4] && board[0] == board[8]) ||
               (board[2] != ' ' && board[2] == board[4] && board[2] == board[6]);
    }

    private boolean checkDraw() {
        // Verifica se há espaços vazios
        for (char cell : board) {
            if (cell == ' ') return false;
        }
        return true; // Empate
    }

    public String getBoardState() {
        return new String(board);
    }

    public char getWinner() {
        if (!isActive && checkWin()) {
            return (currentPlayerId.equals(playerXId) ? 'O' : 'X'); // O vencedor é o que jogou por último
        }
        return ' ';
    }
}