package cf.rafl.applicationserver.core.security;

import cf.rafl.applicationserver.util.Crash;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Hasher
{
    private static Logger logger = Logger.getLogger(Hasher.class.getName());

    private final static String algorithm = "PBKDF2WithHmacSHA1";

    public static String generatePasswordHash(LoginCredentials credentials)
    {
        return generatePasswordHash(credentials, System.currentTimeMillis() / 1000);
    }

    public static String generatePasswordHash(LoginCredentials credentials, long timestamp)
    {
        try
        {
            byte[] salt = (credentials.username + timestamp).getBytes();
            PBEKeySpec spec = new PBEKeySpec(credentials.password.toCharArray(), salt, 42069, 128);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(algorithm);

            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);

        } catch (NoSuchAlgorithmException e)
        {
            logger.log(Level.SEVERE, "Algorithm " + algorithm + " not valid!", e);
            Crash.crash();
            return null;
        } catch (InvalidKeySpecException e)
        {
            logger.log(Level.SEVERE, "", e);
            return null;
        }
    }

}
