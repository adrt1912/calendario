package Utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class SeguridadUtils {

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
}