package Utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class SeguridadUtils {

    private static final String CLAVE_ALGORITMO = "ClaveSecreta1234";
    // Transforma el PIN en un churro de letras y números irreversible (SHA-256)
    public static String encriptarPIN(String pinRaw) {
        if (pinRaw == null || pinRaw.isBlank()) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(pinRaw.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error al cifrar el PIN de seguridad", e);
        }
    }
    public static String cifrarTexto(String texto) {
        if (texto == null || texto.isBlank() || texto.equals("null")) return texto;
        try {
            SecretKeySpec secretKey = new SecretKeySpec(CLAVE_ALGORITMO.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] bytesCifrados = cipher.doFinal(texto.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(bytesCifrados);
        } catch (Exception e) {
            return texto; // Si falla, devuelve el original para no romper la app
        }
    }

    public static String descifrarTexto(String textoCifrado) {
        if (textoCifrado == null || textoCifrado.isBlank() || textoCifrado.equals("null")) return textoCifrado;
        try {
            SecretKeySpec secretKey = new SecretKeySpec(CLAVE_ALGORITMO.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] bytesDescifrados = cipher.doFinal(Base64.getDecoder().decode(textoCifrado));
            return new String(bytesDescifrados, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return textoCifrado; // Si no está cifrado (datos viejos), devuelve el original
        }
    }
}