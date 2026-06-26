package utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeguridadUtils {

    private static final Logger logger = LoggerFactory.getLogger(SeguridadUtils.class);

    private SeguridadUtils() {
        throw new IllegalStateException("Clase de utilidad inmutable. No se puede instanciar.");
    }

    // Tu método de Hashing irreversible para almacenar el PIN de forma segura (SHA-256)
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
            // Recortamos limpiamente los primeros 16 bytes del hash SHA-256.
            byte[] clave16bytes = Arrays.copyOf(hash, 16);

            return new SecretKeySpec(clave16bytes, "AES");
        } catch (Exception e) {
            throw new IllegalStateException("Error crítico al generar la clave simétrica de sesión", e);
        }
    }

    // Cifra cualquier texto usando la clave exclusiva del usuario activo
    public static String cifrarTexto(String texto, SecretKeySpec claveUsuario) {
        if (texto == null || texto.isBlank() || texto.equals("null") || claveUsuario == null) return texto;

        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            // Generamos un Vector de Inicialización (IV) de 16 bytes aleatorio
            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            cipher.init(Cipher.ENCRYPT_MODE, claveUsuario, ivSpec);
            byte[] bytesCifrados = cipher.doFinal(texto.getBytes(StandardCharsets.UTF_8));

            // Combinamos el IV y los bytes cifrados en un único bloque para guardarlo todo junto
            byte[] resultadoCombinado = new byte[iv.length + bytesCifrados.length];
            System.arraycopy(iv, 0, resultadoCombinado, 0, iv.length);
            System.arraycopy(bytesCifrados, 0, resultadoCombinado, iv.length, bytesCifrados.length);

            return Base64.getEncoder().encodeToString(resultadoCombinado);
        } catch (Exception e) {
            logger.error("Advertencia: No se pudo cifrar el texto del formulario. Se mantendrá el original.", e);
            return texto;
        }
    }

    // Descifra el texto de la BD usando la clave exclusiva del usuario activo
    public static String descifrarTexto(String textoCifrado, SecretKeySpec claveUsuario) {
        if (textoCifrado == null || textoCifrado.isBlank() || textoCifrado.equals("null") || claveUsuario == null) return textoCifrado;

        try {
            // Descodificamos el Base64 combinado
            byte[] resultadoCombinado = Base64.getDecoder().decode(textoCifrado);

            // Control de seguridad por si el registro no tiene el tamaño mínimo del IV
            if (resultadoCombinado.length < 16) return textoCifrado;

            // Separamos los 16 primeros bytes (IV) del resto (Contenido cifrado)
            byte[] iv = new byte[16];
            byte[] bytesCifrados = new byte[resultadoCombinado.length - 16];
            System.arraycopy(resultadoCombinado, 0, iv, 0, 16);
            System.arraycopy(resultadoCombinado, 16, bytesCifrados, 0, bytesCifrados.length);

            // Configuramos el descifrador en el mismo modo CBC
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, claveUsuario, ivSpec);

            byte[] bytesDescifrados = cipher.doFinal(bytesCifrados);
            return new String(bytesDescifrados, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.warn("No se pudo descifrar el registro de la BD (es posible que pertenezca al formato de cifrado antiguo).", e);
            return textoCifrado;
        }
    }
}