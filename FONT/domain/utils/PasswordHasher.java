package domain.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Clase de Utilidad encargada de generar hashes a las contraseñas en texto plano.
 * El hash generado es irreversible y sirve para almacenar las contraseñas de manera segura en la base de datos.
 */

public class PasswordHasher {
    /**
     * Algoritmo de hash SHA-256: estándar seguro.
     * Alternativas: SHA-512, PBKDF2, bcrypt, scrypt, Argon2.
     */
    private static final String ALGORITHM = "SHA-256";

    /**
     * Genera un hash de la contraseña utilizando el algoritmo seleccionado en ALGORITHM
     *
     * @param password contraseña en texto plano a convertir con hash
     * @return representación hexadecimal del hash generado
     * @throws RuntimeException si el algoritmo de hash no esta disponible
     */
    public static String hash(String password) {
        try {
            // Encargado de aplicar el algoritmo de hash seleccionado
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            // Obtiene el hash de la contraseña en formato byte[] utilizando el algoritmo seleccionado
            // Se especifica el charset UTF-8 para que el resultado sea consistente en cualquier entorno
            byte[] hashBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            // StringBuilder usado para convertir los bytes del hash en formato hexadecimal
            // Más eficiente que String, el cual crea nuevas instancias cada vez que se concatena
            StringBuilder sb = new StringBuilder();
            // Convierte los bytes del hash en formato hexadecimal
            for (byte b : hashBytes)
                sb.append(String.format("%02x", b));
            // Devuelve el hash convirtiendo el StringBuilder en String
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(ALGORITHM + " no disponible", e);
        }
    }
}
