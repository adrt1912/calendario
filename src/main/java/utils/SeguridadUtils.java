package utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeguridadUtils {

    private static final Logger logger = LoggerFactory.getLogger(SeguridadUtils.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private SeguridadUtils() {
        throw new IllegalStateException("Clase de utilidad inmutable. No se puede instanciar.");
    }

    // Tu metodo de Hashing irreversible para almacenar el PIN de forma segura (SHA-256)
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
            throw new IllegalStateException("Error crítico al cifrar el PIN del usuario", e);
        }
    }

    // Genera una clave secreta AES válida de 16 bytes a partir del PIN plano del usuario
    public static SecretKeySpec generarClaveDesdePIN(String pinRaw) {
        if (pinRaw == null || pinRaw.isBlank()) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(pinRaw.getBytes(StandardCharsets.UTF_8));

            // El algoritmo AES exige claves de exactamente 16, 24 o 32 bytes.
            byte[] clave16bytes = Arrays.copyOf(hash, 16);

            return new SecretKeySpec(clave16bytes, "AES");
        } catch (Exception e) {
            throw new IllegalStateException("Error crítico al generar la clave simétrica de sesión", e);
        }
    }

    // Cifra cualquier texto usando el algoritmo de máxima seguridad militar AES/GCM/NoPadding
    public static String cifrarTexto(String texto, SecretKeySpec claveUsuario) {
        if (texto == null || texto.isBlank() || texto.equals("null") || claveUsuario == null) return texto;

        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding"); // (O la configuración que tengas establecida)
            // Generamos un Vector de Inicialización (IV) de 12 bytes aleatorio
            byte[] iv = new byte[12];

            // 🚀 CORREGIDO: Usamos la constante estática en lugar de crear un objeto "new" cada vez
            SECURE_RANDOM.nextBytes(iv);

            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.ENCRYPT_MODE, claveUsuario, gcmSpec);
            byte[] bytesCifrados = cipher.doFinal(texto.getBytes(StandardCharsets.UTF_8));

            // Combinamos el IV (12 bytes) y el bloque cifrado autenticado
            byte[] resultadoCombinado = new byte[iv.length + bytesCifrados.length];
            System.arraycopy(iv, 0, resultadoCombinado, 0, iv.length);
            System.arraycopy(bytesCifrados, 0, resultadoCombinado, iv.length, bytesCifrados.length);

            return Base64.getEncoder().encodeToString(resultadoCombinado);
        } catch (Exception e) {
            logger.error("Advertencia: No se pudo cifrar el texto del formulario. Se mantendrá el original.", e);
            return texto;
        }
    }

    // Descifra el texto autenticado por GCM desde la base de datos
    public static String descifrarTexto(String textoCifrado, SecretKeySpec claveUsuario) {
        if (textoCifrado == null || textoCifrado.isBlank() || textoCifrado.equals("null") || claveUsuario == null) return textoCifrado;

        try {
            byte[] resultadoCombinado = Base64.getDecoder().decode(textoCifrado);

            // Control de seguridad: el bloque mínimo debe contener al menos los 12 bytes del IV de GCM
            if (resultadoCombinado.length < 12) return textoCifrado;

            // Separamos los 12 primeros bytes (IV) del contenido cifrado
            byte[] iv = new byte[12];
            byte[] bytesCifrados = new byte[resultadoCombinado.length - 12];
            System.arraycopy(resultadoCombinado, 0, iv, 0, 12);
            System.arraycopy(resultadoCombinado, 12, bytesCifrados, 0, bytesCifrados.length);

            // Configuramos el descifrador en modo GCM con el tag de verificación de integridad
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, claveUsuario, gcmSpec);

            byte[] bytesDescifrados = cipher.doFinal(bytesCifrados);
            return new String(bytesDescifrados, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.warn("No se pudo descifrar el registro (formato de cifrado antiguo o alterado de forma externa).", e);
            return textoCifrado;
        }
    }
}