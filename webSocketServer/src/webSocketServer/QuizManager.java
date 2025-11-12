package webSocketServer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class QuizManager {

    private static final Map<UUID, UUID> activePlayerQuizzes = new ConcurrentHashMap<>();
    private static final Map<UUID, Quiz> activeQuizzes = new ConcurrentHashMap<>();

    private static final String CAMINHO_PADRAO_JSON = "webSocketServer/perguntas.json";

    public static Quiz createNewQuiz(List<UUID> players) {
        return createNewQuiz(players, CAMINHO_PADRAO_JSON);
    }

    public static Quiz createNewQuiz(List<UUID> players, String caminhoJson) {
        for (UUID id : players) {
            endQuizForPlayer(id);
        }

        Quiz quiz = new Quiz(players, caminhoJson);
        activeQuizzes.put(quiz.getQuizId(), quiz);

        for (UUID id : players) {
            activePlayerQuizzes.put(id, quiz.getQuizId());
        }

        System.out.println("🎉 Novo quiz criado. ID: " + quiz.getQuizId());
        return quiz;
    }

    public static Quiz getQuizForPlayer(UUID playerId) {
        UUID quizId = activePlayerQuizzes.get(playerId);
        return quizId != null ? activeQuizzes.get(quizId) : null;
    }

    public static void endQuiz(UUID quizId) {
        Quiz quiz = activeQuizzes.remove(quizId);
        if (quiz != null) {
            for (UUID id : quiz.getPlayers()) {
                activePlayerQuizzes.remove(id);
            }
            quiz.endQuiz();
            System.out.println("❌ Quiz encerrado e removido. ID: " + quizId);
        }
    }

    public static void endQuizForPlayer(UUID playerId) {
        UUID quizId = activePlayerQuizzes.get(playerId);
        if (quizId != null) {
            endQuiz(quizId);
        }
    }

    public static Collection<Quiz> getActiveQuizzes() {
        return activeQuizzes.values();
    }

    private static final Set<UUID> pendingPlayers = ConcurrentHashMap.newKeySet();
    private static volatile UUID currentHost = null;

    /** Host inicia o convite global */
    public static void prepareGlobalQuiz(UUID hostId) {
        pendingPlayers.clear();
        currentHost = hostId;
        pendingPlayers.add(hostId); // host entra automaticamente
        System.out.println("⚡ Quiz global iniciado por: " + hostId);
    }

    public static void addPendingPlayer(UUID playerId) {
        if (currentHost == null) return;
        pendingPlayers.add(playerId);
        System.out.println("➕ Jogador entrou no lobby do quiz global: " + playerId);
    }

    public static List<UUID> getPendingPlayers() {
        return new ArrayList<>(pendingPlayers);
    }

    public static void clearPendingPlayers() {
        pendingPlayers.clear();
        currentHost = null;
    }

    public static UUID getCurrentHost() {
        return currentHost;
    }
    //
}
