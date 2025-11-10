// server_front.js
import express from "express";
import path from "path";
import { fileURLToPath } from "url";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const PORT = 5500; // Porta para o front
const HOST = "0.0.0.0"; // Permite acesso de outros dispositivos

// Serve a pasta 'client' como estática
app.use(express.static(path.join(__dirname, "clientChat")));

// Rota padrão -> envia index.html
app.get("/", (req, res) => {
  res.sendFile(path.join(__dirname, "client.js", "index.html"));
});

// Inicia o servidor
app.listen(PORT, HOST, () => {
  console.log(`🌐 Servidor do front rodando em http://${HOST}:${PORT}`);
  console.log(`💡 Acesse pelo PC: http://localhost:${PORT}`);
  console.log(`📱 Ou pelo celular: http://SEU_IP_LOCAL:${PORT}`);
});
