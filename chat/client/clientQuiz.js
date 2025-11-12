// ===============================
// 🧠 CLIENT QUIZ (Kahoot Global)
// ===============================

let quizActive = false;
let currentQuestion = null;

// Lidar com mensagens relacionadas ao quiz
function handleQuizMessages(data) {
    switch (data.type) {
        case "quizInvite":
            handleQuizInvite(data);
            return true;

        case "quizLobby":
            appendToLog(data.message, true);
            return true;

        case "quizStartGlobal":
            showQuizUI(true);
            appendToLog("🎮 Quiz global iniciado!", true);
            return true;

        case "quizQuestion":
            showQuizQuestion(data);
            return true;

        case "quizAnswerResult":
            showQuizFeedback(data);
            return true;

        case "quizEnd":
            showQuizRanking(data);
            return true;

        case "quizError":
            appendToLog("⚠️ " + data.message, true);
            return true;

        default:
            return false;
    }
}

// Enviar convite global
function inviteGlobalQuiz() {
    ws.send(JSON.stringify({ quizInvite: true }));
    appendToLog("🧠 Você convidou todos para participar do Quiz Global.", true);
}

// Jogador aceita convite
function acceptQuizInvite() {
    ws.send(JSON.stringify({ quizJoin: true }));
    appendToLog("✅ Você entrou no quiz global!", true);
}

// Iniciar o quiz (somente o host)
function startGlobalQuizNow() {
    ws.send(JSON.stringify({ quizStartGlobal: true }));
}

// Mostrar pergunta na tela
function showQuizQuestion(data) {
    quizActive = true;
    currentQuestion = data.index;

    const container = document.getElementById("quizContainer");
    container.style.display = "block";

    document.getElementById("quizQuestion").innerText =
        `Pergunta ${data.index + 1}: ${data.textoPergunta}`;

    const optionsDiv = document.getElementById("quizOptions");
    optionsDiv.innerHTML = "";

    data.opcoes.forEach((opt, idx) => {
        const btn = document.createElement("button");
        btn.textContent = opt;
        btn.className = "quiz-option";
        btn.onclick = () => sendQuizAnswer(idx);
        optionsDiv.appendChild(btn);
    });

    document.getElementById("quizFeedback").innerText = "";
}

// Enviar resposta
function sendQuizAnswer(index) {
    if (!quizActive) return;
    ws.send(JSON.stringify({ quizAnswer: index }));
    appendToLog(`Você respondeu opção ${index + 1}.`, true);
}

// Mostrar feedback
function showQuizFeedback(data) {
    const fb = document.getElementById("quizFeedback");
    fb.innerText = data.correct
        ? `✅ Acertou! Pontuação: ${data.score}`
        : `❌ Errou! Pontuação: ${data.score}`;
}

// Mostrar ranking final
function showQuizRanking(data) {
    quizActive = false;
    document.getElementById("quizOptions").innerHTML = "";
    document.getElementById("quizQuestion").innerText = "🏁 Quiz encerrado!";
    document.getElementById("quizFeedback").innerText = "";
    document.getElementById("quizRanking").innerText = data.rankingText;
}

// Receber convite de quiz
function handleQuizInvite(data) {
    const aceitar = confirm(data.message);
    if (aceitar) {
        acceptQuizInvite();
    } else {
        appendToLog("❌ Você recusou participar do quiz global.", true);
    }
}

// Mostrar/ocultar quiz na interface
function showQuizUI(show) {
    document.getElementById("quizContainer").style.display = show ? "block" : "none";
}

// Tornar função visível para clientChat.js
window.handleQuizMessages = handleQuizMessages;
