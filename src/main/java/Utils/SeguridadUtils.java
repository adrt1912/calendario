package Utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class SeguridadUtils {

    //Tu metodo de Hashing irreversible para almacenar el PIN de forma segura (SHA-256)
    public static String encriptarPIN(String pinRaw) {
        if (pinRaw == null || pinRaw.isBlank()) return null;
        try {
            //Se encripta el PIN
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
            throw new RuntimeException("Error al cifrar el PIN", e);
        }
    }

    //Genera una clave secreta AES válida de 16 bytes a partir del PIN plano del usuario
    public static SecretKeySpec generarClaveDesdePIN(String pinRaw) {
        if (pinRaw == null || pinRaw.isBlank()) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(pinRaw.getBytes(StandardCharsets.UTF_8));

            // El algoritmo AES exige claves de exactamente 16, 24 o 32 bytes.
            // Recortamos limpiamente los primeros 16 bytes del hash SHA-256.
            byte[] clave16bytes = Arrays.copyOf(hash, 16);

            return new SecretKeySpec(clave16bytes, "AES");
        } catch (Exception e) {
            throw new RuntimeException("Error al generar la clave simétrica de sesión", e);
        }
    }

    //  Cifra cualquier texto usando la clave exclusiva del usuario activo
    public static String cifrarTexto(String texto, SecretKeySpec claveUsuario) {
        if (texto == null || texto.isBlank() || texto.equals("null") || claveUsuario == null) return texto;

        try {
            //Se encripta el texto
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, claveUsuario);
            byte[] bytesCifrados = cipher.doFinal(texto.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(bytesCifrados);
        } catch (Exception e) {
            // Si algo falla, devuelve el original para proteger el flujo de la app
            return texto;
        }
    }

    // Descifra el texto de la BD usando la clave exclusiva del usuario activo
    public static String descifrarTexto(String textoCifrado, SecretKeySpec claveUsuario) {
        if (textoCifrado == null || textoCifrado.isBlank() || textoCifrado.equals("null") || claveUsuario == null) return textoCifrado;

        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, claveUsuario);
            byte[] bytesDescifrados = cipher.doFinal(Base64.getDecoder().decode(textoCifrado));
            return new String(bytesDescifrados, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // Si no se puede descifrar (por ejemplo, datos antiguos corruptos), devuelve el texto tal cual
            return textoCifrado;
        }
    }
}