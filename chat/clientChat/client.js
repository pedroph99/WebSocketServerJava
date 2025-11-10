//Configuração de conexão
const SERVER_IP = window.location.hostname;
const SERVER_PORT = 8081;
const SOCKET_URL = `ws://${SERVER_IP}:${SERVER_PORT}`;

let ws;
let myUsername = null;

//Interface
function appendToLog(message, isSystem = false) {
    const log = document.getElementById('log');
    const p = document.createElement('p');
    p.innerHTML = message;
    if (isSystem) p.className = 'system-msg';
    log.appendChild(p);
    log.scrollTop = log.scrollHeight;
}

function updateStatus(text) {
    document.getElementById('status').innerText = `Status: ${text}`;
}

//Conexão WebSocket
function connect() {
    while (!myUsername) {
        myUsername = prompt("Digite seu nome de usuário:");
        if (!myUsername) alert("Nome obrigatório!");
    }

    ws = new WebSocket(SOCKET_URL);
    updateStatus("Conectando...");
    appendToLog(`Tentando conectar a ${SOCKET_URL}...`, true);

    ws.onopen = () => {
        updateStatus("Conectado!");
        appendToLog("Conexão estabelecida.", true);

        // envia o identificador inicial (ResponseHandler espera a chave 'userSetter')
        ws.send(JSON.stringify({ userSetter: myUsername }));
    };

    ws.onmessage = (event) => {
        appendToLog(event.data);
    };

    ws.onerror = (error) => {
        updateStatus("Erro!");
        appendToLog(`Erro do WebSocket. Verifique se o servidor Java está rodando.`, true);
        console.error(error);
    };

    ws.onclose = (event) => {
        updateStatus("Desconectado.");
        appendToLog("Conexão encerrada.", true);
        if (event.code !== 1000) setTimeout(connect, 3000); // tenta reconectar
    };
}

//Envio de mensagem
function sendMessage() {
    const recipient = document.getElementById('recipientUsername').value.trim();
    const content = document.getElementById('messageContent').value.trim();

    if (!ws || ws.readyState !== WebSocket.OPEN) {
        alert("Conexão ainda não está aberta.");
        return;
    }

    if (!recipient || !content) {
        alert("Preencha o destinatário e a mensagem.");
        return;
    }

    const msg = JSON.stringify({
        username: recipient,
        message: content
    });

    ws.send(msg);
    appendToLog(`Você → ${recipient}: ${content}`);
    document.getElementById('messageContent').value = '';
}

//Início
window.onload = connect;
