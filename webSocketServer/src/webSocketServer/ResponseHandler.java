package webSocketServer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import org.json.*;

public class ResponseHandler {

	public static void handler(String message, ClientManager currentClientManager, UUID clientID) throws IOException {
		JSONObject json = new JSONObject(message);

		if (json.has("userSetter")) {
			currentClientManager.setClientUsername(clientID, json.getString("userSetter"));
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
				if (opponent != null && !opponent.getSocket().isClosed()) {
					try {
						WebSocketUtils.sendMessage(opponent.getSocket().getOutputStream(), challengeMsg.toString());
						System.out.println("✉️ Desafio enviado de " +
								currentClientManager.getClientUsername(clientID) + " para " + opponentUsername);
					} catch (IOException e) {
						System.err.println("Erro ao enviar desafio para " + opponentUsername + ": " + e.getMessage());
					}
				}
			}
			return;
		}

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
					String playerXUsername = currentClientManager.getClientUsername(challengerId);
					String playerOUsername = currentClientManager.getClientUsername(clientID);
					try {
						sendGameStartMessage(challenger.getSocket().getOutputStream(), game, "X", playerOUsername);
						sendGameStartMessage(
								currentClientManager.getClient(clientID).getSocket().getOutputStream(),
								game, "O", playerXUsername);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					try {
						WebSocketUtils.sendMessage(challenger.getSocket().getOutputStream(), responseMsg.toString());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return;
		}

		if (json.has("move")) {
			int position = json.getInt("move");
			Game game = GameManager.getGameForPlayer(clientID);
			if (game != null && game.isActive()) {
				if (game.makeMove(clientID, position)) {
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

		if (json.has("quizInvite")) {
			QuizManager.prepareGlobalQuiz(clientID);

			JSONObject invite = new JSONObject();
			invite.put("type", "quizInvite");
			invite.put("host", currentClientManager.getClientUsername(clientID));
			invite.put("message", currentClientManager.getClientUsername(clientID)
					+ " iniciou um quiz global! Deseja participar?");

			broadcastToAll(currentClientManager, invite);
			return;
		}

		if (json.has("quizJoin")) {
			boolean join = json.getBoolean("quizJoin");
			if (join) {
				QuizManager.addPendingPlayer(clientID);
				JSONObject joined = new JSONObject();
				joined.put("type", "quizLobby");
				joined.put("joined", true);
				joined.put("message", "Você entrou no lobby do quiz global.");
				WebSocketUtils.sendMessage(
						currentClientManager.getClient(clientID).getSocket().getOutputStream(),
						joined.toString());
			}
			return;
		}

		if (json.has("quizStartGlobal")) {
			UUID host = QuizManager.getCurrentHost();
			if (host == null || !host.equals(clientID)) {
				sendError(currentClientManager, clientID, "quizError",
						"Apenas o criador do convite pode iniciar o quiz global.");
				return;
			}

			List<UUID> participants = QuizManager.getPendingPlayers();
			if (participants.isEmpty()) {
				sendError(currentClientManager, clientID, "quizError",
						"Nenhum participante no lobby.");
				return;
			}

			Quiz quiz = QuizManager.createNewQuiz(participants);
			QuizManager.clearPendingPlayers();

			JSONObject started = new JSONObject();
			started.put("type", "quizStartGlobal");
			started.put("quizId", quiz.getQuizId().toString());
			broadcastToList(currentClientManager, participants, started);

			for (UUID pid : participants) {
				ClientConnection cc = currentClientManager.getClient(pid);
				if (cc == null) continue;
				try {
					sendQuizQuestion(cc.getSocket().getOutputStream(),
							quiz.getCurrentQuestion(), quiz.getCurrentQuestionIndex());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return;
		}

		if (json.has("quizAnswer")) {
			int answerIndex = json.getInt("quizAnswer");
			Quiz quiz = QuizManager.getQuizForPlayer(clientID);
			if (quiz == null || !quiz.isActive() || quiz.getCurrentQuestion() == null) {
				sendError(currentClientManager, clientID, "quizError",
						"Nenhum quiz ativo ou pergunta indisponível.");
				return;
			}

			boolean acertou = quiz.answerQuestion(clientID, answerIndex);
			ClientConnection self = currentClientManager.getClient(clientID);
			if (self != null) {
				try {
					sendQuizAnswerResult(self.getSocket().getOutputStream(), acertou,
							quiz.getScores().getOrDefault(clientID, 0));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (quiz.todosResponderam()) {
				QuizQuestion prox = quiz.nextQuestion();
				if (prox == null) {
					broadcastQuizEnd(quiz, currentClientManager);
					QuizManager.endQuiz(quiz.getQuizId());
				} else {
					broadcastQuizQuestion(quiz, prox, currentClientManager);
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
		String senderUsername = currentClientManager.getClientUsername(clientID);
		if (senderUsername == null) senderUsername = "Anonymous user";
		String formattedMessage = senderUsername + ": " + messageContent;

		ClientConnection referredClient = currentClientManager.getClient(UUUIDusernameToSend);
		try {
			WebSocketUtils.sendMessage(referredClient.getSocket().getOutputStream(), formattedMessage);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


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

	private static void sendQuizQuestion(OutputStream out, QuizQuestion q, int index) throws IOException {
		if (q == null) return;
		JSONObject msg = new JSONObject();
		msg.put("type", "quizQuestion");
		msg.put("index", index);
		msg.put("textoPergunta", q.getTextoPergunta());
		JSONArray opcoes = new JSONArray();
		opcoes.put(q.getOpcao1());
		opcoes.put(q.getOpcao2());
		opcoes.put(q.getOpcao3());
		opcoes.put(q.getOpcao4());
		msg.put("opcoes", opcoes);
		WebSocketUtils.sendMessage(out, msg.toString());
	}

	private static void sendQuizAnswerResult(OutputStream out, boolean correct, int newScore)
			throws IOException {
		JSONObject res = new JSONObject();
		res.put("type", "quizAnswerResult");
		res.put("correct", correct);
		res.put("score", newScore);
		WebSocketUtils.sendMessage(out, res.toString());
	}

	private static void broadcastQuizQuestion(Quiz quiz, QuizQuestion q, ClientManager cm) {
		for (UUID pid : quiz.getPlayers()) {
			ClientConnection cc = cm.getClient(pid);
			if (cc == null) continue;
			try {
				sendQuizQuestion(cc.getSocket().getOutputStream(), q, quiz.getCurrentQuestionIndex());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void broadcastQuizEnd(Quiz quiz, ClientManager cm) {
		List<Map.Entry<UUID, Integer>> ranking = new ArrayList<>(quiz.getScores().entrySet());
		ranking.sort(Map.Entry.<UUID, Integer>comparingByValue().reversed());

		StringBuilder txt = new StringBuilder("🏆 RANKING do Quiz 🏆\n\n");
		String[] medalhas = {"🥇", "🥈", "🥉"};
		int i = 0;
		JSONArray rankingJson = new JSONArray();

		for (Map.Entry<UUID, Integer> e : ranking) {
			String nome = cm.getClientUsername(e.getKey());
			if (nome == null || nome.isBlank()) nome = "Jogador";
			String medalha = i < medalhas.length ? medalhas[i] + " " : (i + 1) + "º ";
			txt.append(medalha)
					.append(nome)
					.append(" — ")
					.append(e.getValue())
					.append(" ponto")
					.append(e.getValue() == 1 ? "" : "s")
					.append("\n");

			JSONObject j = new JSONObject();
			j.put("username", nome);
			j.put("score", e.getValue());
			rankingJson.put(j);
			i++;
		}

		JSONObject end = new JSONObject();
		end.put("type", "quizEnd");
		end.put("rankingText", txt.toString());
		end.put("ranking", rankingJson);

		for (UUID pid : quiz.getPlayers()) {
			ClientConnection cc = cm.getClient(pid);
			if (cc == null) continue;
			try {
				WebSocketUtils.sendMessage(cc.getSocket().getOutputStream(), end.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		System.out.println(txt);
	}

	private static void broadcastToAll(ClientManager cm, JSONObject msg) {
		try {
			Map<UUID, ClientConnection> all = cm.getClients();
			for (ClientConnection c : all.values()) {
				if (c == null || c.getSocket().isClosed()) continue;
				WebSocketUtils.sendMessage(c.getSocket().getOutputStream(), msg.toString());
			}
		} catch (Exception e) {
			System.err.println("⚠️ broadcastToAll falhou: " + e.getMessage());
		}
	}

	private static void broadcastToList(ClientManager cm, List<UUID> ids, JSONObject msg) {
		for (UUID id : ids) {
			ClientConnection c = cm.getClient(id);
			if (c == null) continue;
			try {
				WebSocketUtils.sendMessage(c.getSocket().getOutputStream(), msg.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void sendError(ClientManager cm, UUID id, String type, String msgText) {
		ClientConnection c = cm.getClient(id);
		if (c == null) return;
		JSONObject err = new JSONObject();
		err.put("type", type);
		err.put("message", msgText);
		try {
			WebSocketUtils.sendMessage(c.getSocket().getOutputStream(), err.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
