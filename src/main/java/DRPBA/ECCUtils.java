package DRPBA;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static DRPBA.InfoEntropy.ProbabilityCalculate;
import static DRPBA.InfoEntropy.calculateInfoEntropy;
import static DRPBA.DRPBAUtil.*;
public class ECCUtils {

    /**
     * 生成密钥对(公钥和私钥)
     *
     * @return
     * @throws Exception
     */
    public static KeyPair initKey(int keySize, String KEY_ALGORITHM) throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        keyPairGen.initialize(keySize);
        KeyPair keyPair = keyPairGen.generateKeyPair();
        return keyPair;
    }

    /**
     * 公钥加密
     *
     * @param data      源数据
     * @param publicKey 公钥(BASE64编码)
     * @return
     * @throws Exception
     */
    public static String encryptByPublicKey(String data, String publicKey)
            throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(publicKey);
        Security.addProvider(new BouncyCastleProvider());
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        Cipher cipher = Cipher.getInstance("ECIES", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, keyFactory.generatePublic(x509KeySpec));
        System.out.println(cipher.doFinal(data.getBytes()));
        byte[] bytes = cipher.doFinal(data.getBytes());
        int[] pros = ProbabilityCalculate(bytes,4);
        System.out.println("ie after encrypt: "+calculateInfoEntropy(pros));
        return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));

    }

    /**
     * 私钥解密
     *
     * @param encryptedData 已加密数据
     * @param privateKey    私钥(BASE64编码)
     * @return
     * @throws Exception
     */
    public static String decryptByPrivateKey(String encryptedData, String privateKey)
            throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(privateKey);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        Security.addProvider(new BouncyCastleProvider());
        Cipher cipher = Cipher.getInstance("ECIES", "BC");
        cipher.init(Cipher.DECRYPT_MODE, keyFactory.generatePrivate(pkcs8KeySpec));
        return Base64.getEncoder().encodeToString(cipher.doFinal(Base64.getDecoder().decode(encryptedData)));
    }

    /**
     * 用私钥对信息生成数字签名
     *
     * @param content     已加密数据 base64
     * @param priKey      私钥(BASE64编码)
     * @param signatureAl 签名算法
     * @return
     * @throws Exception
     */
    public static String sign(String content, String priKey, String signatureAl) throws Exception {
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(priKey));
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        ECPrivateKey privateK = (ECPrivateKey) keyFactory.generatePrivate(pkcs8KeySpec);
        Signature sign = Signature.getInstance(signatureAl);//"SHA256withECDSA/"
        sign.initSign(privateK);
        sign.update(Base64.getDecoder().decode(content));
        return Base64.getEncoder().encodeToString(sign.sign());
    }

    public static void main(String[] args) {
        try {
            //初始化获取公钥和私钥
            KeyPair keypair = initKey(256, "EC");

            PublicKey publicKey = keypair.getPublic();
            PrivateKey privateKey = keypair.getPrivate();

            System.out.println("origin private key:" + privateKey);
            System.out.println("origin public key:" + publicKey);

//            String publicKeyBase64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());
//            String privateKeyBase64 = Base64.getEncoder().encodeToString(privateKey.getEncoded());

            //生成固定公钥私钥
            String publicKeyBase64 = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEvVlOVXJQe6yyLlCSCWQr246yay4Hl9qfB3C5S9al9t6cNzP3lwjJIRGzFmGywspn0OwiMJWmFV7daLhzCx79kQ==";
            String privateKeyBase64 = "MEECAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQcEJzAlAgEBBCDyvdnfevbZyiWDWOmwRp5hLDftlNWHzdD5YkiQW6hR6g==";

            System.out.println("public key:" + publicKeyBase64);
            System.out.println("-----");
            System.out.println("private key:" + privateKeyBase64);

            String con = "Hello world";
            byte[] bytes = con.getBytes(StandardCharsets.UTF_8);
            int[] pros = ProbabilityCalculate(bytes,4);
            System.out.println("ie before encrypt: "+calculateInfoEntropy(pros));
            System.out.println("before:" + con);
            //加密
            String content = encryptByPublicKey(con, publicKeyBase64);
            System.out.println("content:"+content);
            //解密
            String contentDe = decryptByPrivateKey(content, privateKeyBase64);
            //解密之后
            String deStr = new String(Base64.getDecoder().decode(contentDe));
            System.out.println("after:" + deStr);
            getEncryptData("hello world".getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}