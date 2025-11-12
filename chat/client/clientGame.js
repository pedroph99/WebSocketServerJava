let activeGame = {
    id: null,
    myMark: null,
    opponentUsername: null,
    isMyTurn: false,
    board: Array(9).fill(' ')
};

// Criação do tabuleiro
function createBoardUI() {
    const boardDiv = document.getElementById('ticTacToeBoard');
    boardDiv.innerHTML = '';
    for (let i = 0; i < 9; i++) {
        const cell = document.createElement('div');
        cell.className = 'cell';
        cell.dataset.index = i;
        cell.onclick = () => handleCellClick(i);
        boardDiv.appendChild(cell);
    }
}

// Desenha o estado atual do tabuleiro
function drawBoard() {
    const cells = document.querySelectorAll('#ticTacToeBoard .cell');
    activeGame.board.forEach((mark, index) => {
        cells[index].textContent = mark === ' ' ? '' : mark;
        cells[index].dataset.mark = mark;
        cells[index].classList.remove('disabled'); 
        
        // Desabilita células preenchidas ou se não for a vez do jogador
        if (mark !== ' ' || !activeGame.isMyTurn || activeGame.id === null) {
             cells[index].classList.add('disabled');
        }
    });
    
    // Atualiza o status do jogo
    let statusText = activeGame.isMyTurn ? "Sua vez de jogar!" : `Esperando ${activeGame.opponentUsername}...`;
    if (activeGame.id === null) statusText = "Jogo finalizado.";
    document.getElementById('gameStatus').innerText = statusText;
}

// Mostra/Esconde a UI do chat e do jogo
function showGameUI(show) {
    document.getElementById('gameContainer').style.display = show ? 'block' : 'none';
    document.getElementById('log').style.display = show ? 'none' : 'block';
    document.querySelector('.message-form').style.display = show ? 'none' : 'flex';
    document.querySelector('.user-section').style.display = show ? 'none' : 'block';
    if (show) {
        createBoardUI();
    }
}

// Função global para fechar a UI do jogo
function closeGameUI() {

    activeGame.id = null;
    activeGame.myMark = null;
    activeGame.opponentUsername = null;
    activeGame.isMyTurn = false;
    activeGame.board = Array(9).fill(' ');
    
    showGameUI(false);
    // Verifica se appendToLog existe antes de chamar
    if (typeof appendToLog === 'function') {
        appendToLog("Sessão de jogo encerrada.", true);
    }
}


// Handlers de Mensagens de Jogo

function handleGameMessages(data) {
    switch (data.type) {
        case "challenge":
            handleChallenge(data.challengerUsername);
            return true;
            
        case "challengeResult":
            handleChallengeResult(data.opponentUsername, data.accepted);
            return true;
            
        case "gameStart":
            handleGameStart(data);
            return true;
            
        case "gameState":
            handleGameState(data);
            return true;
            
        default:
            return false;
    }
}

// Recebe um desafio de outro usuário
function handleChallenge(challengerUsername) {
    if (activeGame.id !== null) {
        sendChallengeResponse(challengerUsername, false);
        return;
    }

    const response = confirm(`${challengerUsername} está te desafiando para um Jogo da Velha! Você aceita?`);
    sendChallengeResponse(challengerUsername, response);
}

// Envia a resposta de aceitação/recusa
function sendChallengeResponse(opponentUsername, accepted) {
    const msg = {
        challengeResponse: accepted,
        opponentUsername: opponentUsername
    };
    ws.send(JSON.stringify(msg));
    
    if (!accepted) {
         appendToLog(`Você recusou o desafio de ${opponentUsername}.`, true);
    }
}

// Resultado do desafio
function handleChallengeResult(opponentUsername, accepted) {
    if (!accepted) {
        appendToLog(`${opponentUsername} recusou seu desafio.`, true);
    }
}

// Início do jogo
function handleGameStart(data) {
    activeGame.id = data.gameId;
    activeGame.myMark = data.yourMark;
    activeGame.opponentUsername = data.opponentUsername; 

    document.getElementById('opponentName').innerText = activeGame.opponentUsername;
    document.getElementById('myMark').innerText = activeGame.myMark;
    
    appendToLog(`Partida iniciada contra ${activeGame.opponentUsername}. Sua marca: ${activeGame.myMark}.`, true);
    
    activeGame.board = Array.from(data.board); 
    activeGame.isMyTurn = data.isYourTurn;
    
    showGameUI(true);
    drawBoard();
}

// Atualização do estado do jogo após uma jogada
function handleGameState(data) {
    activeGame.board = Array.from(data.board);
    activeGame.isMyTurn = data.isYourTurn;
    
    drawBoard();
    
    if (!data.isActive) {
        // Jogo terminou
        appendToLog(`FIM DE JOGO: ${data.result}`, true);
        activeGame.id = null; 
        
        setTimeout(closeGameUI, 5000); 
    }
}



// Envio de desafio
function sendChallenge(recipient) {
    if (activeGame.id !== null) {
        alert("Você já está em um jogo!");
        return;
    }
    
    const opponent = recipient || document.getElementById('recipientUsername').value.trim();

    if (!opponent || opponent === myUsername) {
        alert("Selecione um oponente válido.");
        return;
    }
    
    const msg = { challenge: opponent };
    ws.send(JSON.stringify(msg)); // Usa 'ws' do clientChat.js
    appendToLog(`Você desafiou ${opponent}. Esperando resposta...`, true);
}

// Jogadas
function handleCellClick(index) {
    if (activeGame.id === null || !activeGame.isMyTurn || activeGame.board[index] !== ' ') {
        return;
    }
    
    activeGame.board[index] = activeGame.myMark;
    activeGame.isMyTurn = false;
    drawBoard();

    const msg = { move: index };
    ws.send(JSON.stringify(msg)); // Usa 'ws' do clientChat.js
}