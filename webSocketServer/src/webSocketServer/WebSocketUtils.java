package webSocketServer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class WebSocketUtils {
    public static void sendMessage(OutputStream out, String message) throws IOException {
        byte[] data = message.getBytes(StandardCharsets.UTF_8);
        int length = data.length;

        // buffer temporário
        ByteArrayOutputStream frame = new ByteArrayOutputStream();

        // 1️⃣ byte: FIN (1) + opcode (1 para texto)
        frame.write(0x81);

        // 2️⃣ bytes seguintes: tamanho do payload
        if (length <= 125) {
            frame.write(length);
        } else if (length <= 65535) {
            frame.write(126);
            frame.write((length >> 8) & 0xFF);
            frame.write(length & 0xFF);
        } else {
            frame.write(127);
            for (int i = 7; i >= 0; i--) {
                frame.write((length >> (8 * i)) & 0xFF);
            }
        }

        // 3️⃣ payload (dados em UTF-8)
        frame.write(data);

        // envia tudo
        out.write(frame.toByteArray());
        out.flush();
    }
    
    public static String readMessage(InputStream in) throws IOException {
        int b1 = in.read();
        if (b1 == -1) return null; // conexão fechada

        int b2 = in.read();
        boolean masked = (b2 & 0x80) != 0; // bit 7 indica máscara
        int payloadLength = b2 & 0x7F;     // bits 0-6 indicam tamanho base

        if (payloadLength == 126) {
            // tamanho estendido (16 bits)
            payloadLength = (in.read() << 8) | in.read();
        } else if (payloadLength == 127) {
            // tamanho muito grande (64 bits) — ignorado pra simplicidade
            for (int i = 0; i < 8; i++) in.read();
            return null;
        }

        byte[] maskingKey = new byte[4];
        if (masked) in.read(maskingKey);

        byte[] encoded = new byte[payloadLength];
        int read = 0;
        while (read < payloadLength) {
            int r = in.read(encoded, read, payloadLength - read);
            if (r == -1) return null;
            read += r;
        }

        // Desmascarar (cliente → servidor)
        byte[] decoded = new byte[payloadLength];
        for (int i = 0; i < payloadLength; i++) {
            decoded[i] = (byte) (encoded[i] ^ maskingKey[i % 4]);
        }

        return new String(decoded, "UTF-8");
    }
}
