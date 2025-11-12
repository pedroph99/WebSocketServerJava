package webSocketServer;

import java.io.IOException;
import java.io.OutputStream;
import org.json.*;
import java.util.UUID;

import org.json.JSONObject;

public class ResponseHandler {
	
	public static void handler(String message, ClientManager currentClientManager, UUID clientID) {
		 JSONObject json = new JSONObject(message);
		 
		 
		 
		 if (json.has("userSetter")){
			// UUID clientUUID = currentClientManager.getClientId(currentClientManager.);
			currentClientManager.setClientUsername(clientID, json.getString("userSetter"));
			// Transmite a lista de usuários para todos após um novo usuário se identificar.
            currentClientManager.broadcastUserList();
			 return;
		 }
		 
		 if (json.has("challenge")) {
			String opponentUsername = json.getString("challenge");
			UUID opponentId = currentClientManager.getClientIdByUsername(opponentUsername);
			
			if (opponentId != null && !opponentId.equals(clientID)) {

				JSONObject challengeMsg = new JSONObject();
				challengeMsg.put("type", "challenge");
				challengeMsg.put("challengerUsername", currentClientManager.getClientUsername(clientID));

				ClientConnection opponent = currentClientManager.getClient(opponentId);
				
				// Verifica se a conexão do oponente ainda está viva
				if (opponent != null && !opponent.getSocket().isClosed()) { 
					try {
						// Envia o desafio para o oponente
						WebSocketUtils.sendMessage(opponent.getSocket().getOutputStream(), challengeMsg.toString());
						System.out.println("✉️ Desafio enviado de " + currentClientManager.getClientUsername(clientID) + " para " + opponentUsername);
					} catch (IOException e) {
						System.err.println("Erro ao enviar desafio para " + opponentUsername + ". Cliente pode ter desconectado: " + e.getMessage());
					}
				} else {
					System.out.println("Oponente " + opponentUsername + " não encontrado ou desconectado.");
				}
			} else {
				System.out.println("Tentativa de desafio inválida (oponente nulo ou desafiando a si mesmo).");
			}
			return;
		}
		
		// Resposta ao desafio
		if (json.has("challengeResponse")) {
			boolean accepted = json.getBoolean("challengeResponse");
			String challengerUsername = json.getString("opponentUsername");
			UUID challengerId = currentClientManager.getClientIdByUsername(challengerUsername);
			
			if (challengerId != null) {
				ClientConnection challenger = currentClientManager.getClient(challengerId);
				
				JSONObject responseMsg = new JSONObject();
				responseMsg.put("type", "challengeResult");
				responseMsg.put("opponentUsername", currentClientManager.getClientUsername(clientID));
				responseMsg.put("accepted", accepted);
				
				if (accepted) {

					Game game = GameManager.createNewGame(challengerId, clientID); 
					
					// Nomes dos jogadores para o envio
					String playerXUsername = currentClientManager.getClientUsername(challengerId);
					String playerOUsername = currentClientManager.getClientUsername(clientID);
					
					// Envia a resposta de sucesso/início para ambos
					try {
					   sendGameStartMessage(challenger.getSocket().getOutputStream(), game, "X", playerOUsername);
					   sendGameStartMessage(currentClientManager.getClient(clientID).getSocket().getOutputStream(), game, "O", playerXUsername);
					} catch (IOException e) {
					   e.printStackTrace();
					}

				} else {
					// Envia a recusa pro desafiante
					try {
					   WebSocketUtils.sendMessage(challenger.getSocket().getOutputStream(), responseMsg.toString());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return;
		}
		
		// Jogadas
		if (json.has("move")) {
			int position = json.getInt("move");
			Game game = GameManager.getGameForPlayer(clientID);
			
			if (game != null && game.isActive()) {
				if (game.makeMove(clientID, position)) {
					// Jogada válida: envia o estado para ambos os jogadores
					UUID opponentId = game.getOpponentId(clientID);
					ClientConnection self = currentClientManager.getClient(clientID);
					ClientConnection opponent = currentClientManager.getClient(opponentId);
					
					try {
						sendGameState(self.getSocket().getOutputStream(), game, clientID);
						sendGameState(opponent.getSocket().getOutputStream(), game, opponentId);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
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

	// Métodos auxiliares para o jogo

	private static void sendGameState(OutputStream out, Game game, UUID recipientId) throws IOException {
		JSONObject stateMsg = new JSONObject();
		stateMsg.put("type", "gameState");
		stateMsg.put("board", game.getBoardState());
		stateMsg.put("isYourTurn", game.getCurrentPlayerId().equals(recipientId));
		stateMsg.put("isActive", game.isActive());

		if (!game.isActive()) {
			char winner = game.getWinner();
			if (winner != ' ') {
				stateMsg.put("result",
						(winner == game.getPlayerMark(recipientId) ? "Você venceu!" : winner + " venceu!"));
			} else {
				stateMsg.put("result", "Empate!");
			}
			GameManager.endGame(game.getGameId());
		}

		WebSocketUtils.sendMessage(out, stateMsg.toString());
	}

	private static void sendGameStartMessage(OutputStream out, Game game, String mark, String opponentUsername)
			throws IOException {
		JSONObject startMsg = new JSONObject();
		startMsg.put("type", "gameStart");
		startMsg.put("gameId", game.getGameId().toString());
		startMsg.put("yourMark", mark);
		startMsg.put("opponentUsername", opponentUsername);
		startMsg.put("board", game.getBoardState());
		startMsg.put("isYourTurn", mark.equals("X"));

		WebSocketUtils.sendMessage(out, startMsg.toString());
	}
}
