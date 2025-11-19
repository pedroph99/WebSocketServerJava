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

// Atualiza a lista de usuários
function updateUserList(users) {
    const listDiv = document.getElementById("userList");
    listDiv.innerHTML = "";

    users.forEach(username => {
        if (username === myUsername) return; // não mostra o próprio nome
        const btn = document.createElement("button");
        btn.textContent = username;
        btn.className = "user-btn";
        btn.onclick = () => {
            document.getElementById("recipientUsername").value = username;
        };
        listDiv.appendChild(btn);
    });
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
        let handled = false;
        let data;
        
        try {
            data = JSON.parse(event.data);
            
            // 1. Delegar mensagens de Jogo ao clientGame.js
            // Usamos window.handleGameMessages para garantir o acesso global
            if (typeof window.handleGameMessages === 'function') {
                handled = window.handleGameMessages(data);
            }
            
            // 2. Handler de Lista de Usuários (Se não foi tratada pelo jogo)
            if (!handled && data.type === "userList") {
                updateUserList(data.users);
                handled = true;
            }
            
            // 3. Se não foi tratada, é uma mensagem JSON que deve ser logada
            if (!handled) {
                // Loga como uma mensagem de chat normal (ex: DM)
                appendToLog(event.data);
            }

            if (!handled && typeof window.handleQuizMessages === 'function') {
                handled = window.handleQuizMessages(data);
            }
            
        } catch (e) {
            // Não conseguiu fazer JSON.parse (é uma mensagem de chat comum ou de erro)
            appendToLog(event.data);
            

            if (event.data.includes("challenge")) {
                 console.error("ERRO CRÍTICO ao delegar mensagem de desafio. Verifique o escopo de handleGameMessages:", e);
            }
        }
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
        alert("Selecione um destinatário e escreva algo.");
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
