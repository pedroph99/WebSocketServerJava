package webSocketServer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class Quiz {

    private final UUID quizId;
    private final List<UUID> players;
    private final Map<UUID, Integer> scores;
    private List<QuizQuestion> perguntas;
    private int currentQuestionIndex;
    private boolean isActive;
    private Set<UUID> jogadoresQueResponderam;

    public static final String CAMINHO_PADRAO_JSON = "webSocketServer/perguntas.json";

    public Quiz(List<UUID> players) {
        this(players, CAMINHO_PADRAO_JSON);
    }

    public Quiz(List<UUID> players, String caminhoJson) {
        this.quizId = UUID.randomUUID();
        this.players = new ArrayList<>(players);
        this.scores = new HashMap<>();
        this.currentQuestionIndex = 0;
        this.isActive = true;
        this.jogadoresQueResponderam = new HashSet<>();
        carregarPerguntas(caminhoJson);

        for (UUID id : players) {
            scores.put(id, 0);
        }

        System.out.println("🧠 Novo quiz iniciado. ID: " + quizId + " com " + players.size() + " jogadores.");
    }

    public UUID getQuizId() {
        return quizId;
    }

    public boolean isActive() {
        return isActive;
    }

    public Map<UUID, Integer> getScores() {
        return scores;
    }

    public List<UUID> getPlayers() {
        return players;
    }

    public int getCurrentQuestionIndex() {
        return currentQuestionIndex;
    }

    public QuizQuestion getCurrentQuestion() {
        if (perguntas == null || perguntas.isEmpty()) return null;
        if (currentQuestionIndex < perguntas.size()) {
            return perguntas.get(currentQuestionIndex);
        }
        return null;
    }

    public boolean answerQuestion(UUID playerId, int resposta) {
        if (!isActive || playerId == null || getCurrentQuestion() == null) return false;

        if (jogadoresQueResponderam.contains(playerId)) {
            System.out.println("⚠️ Jogador " + playerId + " já respondeu essa pergunta.");
            return false;
        }

        QuizQuestion q = getCurrentQuestion();
        boolean acertou = (resposta == q.getIndiceRespostaCorreta());

        if (acertou) {
            scores.put(playerId, scores.getOrDefault(playerId, 0) + 1);
        }

        jogadoresQueResponderam.add(playerId);
        return acertou;
    }

    public boolean todosResponderam() {
        return jogadoresQueResponderam.size() >= players.size();
    }

    public QuizQuestion nextQuestion() {
        if (!isActive || currentQuestionIndex + 1 >= perguntas.size()) {
            isActive = false;
            System.out.println("🏁 Quiz finalizado. ID: " + quizId);
            return null;
        }

        currentQuestionIndex++;
        jogadoresQueResponderam.clear();
        return getCurrentQuestion();
    }

    public String getRanking() {
        StringBuilder sb = new StringBuilder("🏆 RANKING do Quiz 🏆\n\n");

        List<Map.Entry<UUID, Integer>> ranking = new ArrayList<>(scores.entrySet());
        ranking.sort(Map.Entry.<UUID, Integer>comparingByValue().reversed());

        String[] medalhas = {"🥇", "🥈", "🥉"};
        int pos = 0;

        for (Map.Entry<UUID, Integer> e : ranking) {
            String username = "Jogador";
            String medalha = pos < medalhas.length ? medalhas[pos] + " " : (pos + 1) + "º ";
            sb.append(medalha)
                    .append(username)
                    .append(" — ")
                    .append(e.getValue())
                    .append(" ponto")
                    .append(e.getValue() == 1 ? "" : "s")
                    .append("\n");
            pos++;
        }

        return sb.toString();
    }

    public void endQuiz() {
        isActive = false;
        System.out.println("🚫 Quiz encerrado manualmente. ID: " + quizId);
        System.out.println(getRanking());
    }

    private void carregarPerguntas(String caminhoJson) {
        ObjectMapper mapper = new ObjectMapper();
        File arquivo = new File("C:\\Users\\Usuario\\projetos\\WebSocketServerJava\\WebSocketServer\\perguntas.json");

        if (!arquivo.exists() || !arquivo.isFile()) {
            System.err.println("❌ Arquivo perguntas.json não encontrado em: " + arquivo.getAbsolutePath());
            this.perguntas = new ArrayList<>();
            return;
        }

        try {
            this.perguntas = mapper.readValue(arquivo, new TypeReference<List<QuizQuestion>>() {});
            System.out.println("✅ " + perguntas.size() + " perguntas carregadas de: " + arquivo.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("❌ Erro ao ler perguntas: " + e.getMessage());
            this.perguntas = new ArrayList<>();
        }
    }

}

