package webSocketServer;

public class QuizQuestion {
    private String id;
    private String textoPergunta;
    private String opcao1;
    private String opcao2;
    private String opcao3;
    private String opcao4;
    private int indiceRespostaCorreta;

    public QuizQuestion() {}

    public String getId() {
        return id;
    }

    public String getTextoPergunta() {
        return textoPergunta;
    }

    public String getOpcao1() {
        return opcao1;
    }

    public String getOpcao2() {
        return opcao2;
    }

    public String getOpcao3() {
        return opcao3;
    }

    public String getOpcao4() {
        return opcao4;
    }

    public int getIndiceRespostaCorreta() {
        return indiceRespostaCorreta;
    }
}
