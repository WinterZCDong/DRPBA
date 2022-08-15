package DRPBA;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
public class DRPBAUtil {
    public static byte[] getEncryptData(byte[] data) throws Exception{
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("EC");
        keyPairGen.initialize(256);
        KeyPair keyPair = keyPairGen.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();
        Security.addProvider(new BouncyCastleProvider());
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(publicKey.getEncoded());
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        Cipher cipher = Cipher.getInstance("ECIES","BC");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        String dataStr = new String(data);
        //System.out.println("origin data:"+dataStr);
        byte[] endata = cipher.doFinal(data);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(privateKey.getEncoded());
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        String endataStr = new String(cipher.doFinal(endata));
       // System.out.println("after:"+endataStr);
        return endata;
    }
}
