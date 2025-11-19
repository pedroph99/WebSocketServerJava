// server_front.js
import http from "http";
import path from "path";
import fs from "fs";
import { fileURLToPath } from "url";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const PORT = 5500; // Porta para o front
const HOST = "0.0.0.0"; // Permite acesso de outros dispositivos

// Mapeamento de extensões de arquivo para Content-Type (MIME types)
const mimeTypes = {
  ".html": "text/html",
  ".js": "text/javascript",
  ".css": "text/css",
  ".json": "application/json",
  ".png": "image/png",
  ".jpg": "image/jpg",
  ".gif": "image/gif",
  ".svg": "image/svg+xml",
};

const server = http.createServer((req, res) => {

  // Exibe informações de conexão e saída de clientes
  const { remoteAddress, remotePort } = req.socket;
  console.log(
    `Nova requisição de ${remoteAddress}:${remotePort} para ${req.url}`
  );

  req.socket.once("close", () => {
    console.log(`Conexão de ${remoteAddress}:${remotePort} fechada.`);
  });

  let filePath = path.join(
    __dirname,
    "client",
    req.url === "/" ? "index.html" : req.url
  );

  // Determina o tipo de conteúdo pela extensão do arquivo
  const extname = String(path.extname(filePath)).toLowerCase();
  const contentType = mimeTypes[extname] || "application/octet-stream";

  fs.readFile(filePath, (error, content) => {
    if (error) {
      // Se o arquivo não for encontrado, retorna 404
      if (error.code == "ENOENT") {
        res.writeHead(404, { "Content-Type": "text/html" });
        res.end("<h1>404 Not Found</h1>", "utf-8");
      } else {
        // Para outros erros do servidor, retorna 500
        res.writeHead(500);
        res.end(error.code);
      }
    } else {
      // Se o arquivo for encontrado, envia com status 200 e o conteúdo
      res.writeHead(200, { "Content-Type": contentType });
      res.end(content, "utf-8");
    }
  });
});

// Inicia o servidor
server.listen(PORT, HOST, () => {
  console.log(`🌐 Servidor do front (nativo) rodando em http://${HOST}:${PORT}`);
  console.log(`💡 Acesse pelo PC: http://localhost:${PORT}`);
  console.log(`📱 Ou pelo celular: http://SEU_IP_LOCAL:${PORT}`);
});
