package DRPBA;
import com.tiemens.secretshare.BuildVersion;
import com.tiemens.secretshare.engine.SecretShare;
import com.tiemens.secretshare.engine.SecretShare.ShareInfo;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SecretSharingTest {
    public static void main(String[] args){
        try{
            final String secret = "Hello world";

            for(int i = 10; i < 300; i+=10){
                long start = new java.util.Date().getTime();
                String[] pieces = splitSecretIntoPieces(secret,i,i);
                long splittime = new java.util.Date().getTime();
                String reconstructed = mergePiecesIntoSecret(pieces);
                long mergetime = new java.util.Date().getTime();
                System.out.println("i="+i+",split time:"+(splittime-start)+"ms,reconstruct time:"+(mergetime-splittime)+"ms");
            }
        } catch (Exception e){
            e.printStackTrace();
        }

    }
    static BigInteger stringToBigInteger(String in)
            throws UnsupportedEncodingException
    {
        final BigInteger bigint = new BigInteger(in.getBytes("UTF-8"));
        return bigint;
    }
    static String[] splitSecretIntoPieces(String secret, int n, int k)
            throws UnsupportedEncodingException
    {
        final BigInteger secretInteger;
        secretInteger = stringToBigInteger(secret);
        // secretInteger = BigInteger.valueOf(2L).pow(4096);
        final BigInteger modulus;
        // modulus = secretInteger.nextProbablePrime(); // OK
        // modulus =
        // secretInteger.multiply(BigInteger.valueOf(32L)).nextProbablePrime(); // FAILS
        // modulus = secretInteger.multiply(secretInteger).nextProbablePrime(); // OK
        // modulus = SecretShare.getPrimeUsedFor384bitSecretPayload(); // OK
        // modulus = SecretShare.getPrimeUsedFor4096bigSecretPayload(); // OK
        modulus = SecretShare.createAppropriateModulusForSecret(secretInteger); // OK
        final SecretShare.PublicInfo publicInfo = new SecretShare.PublicInfo(n,
                k,
                modulus,
                null);
        final SecretShare.SplitSecretOutput splitSecretOutput = new SecretShare(publicInfo)
                .split(secretInteger);
        final List<ShareInfo> pieces = splitSecretOutput.getShareInfos();
        String[] out = new String[pieces.size()];

        for (int i = 0; i < out.length; i++)
        {
            final ShareInfo piece = pieces.get(i);
            out[i] = n + ":" + k + ":" + piece.getX() + ":" +
                    publicInfo.getPrimeModulus() + ":" + piece.getShare();
        }
        return out;
    }
    private static ShareInfo newShareInfo(String piece)
    {
        String[] parts = piece.split(":");
        int i = 0;
        int n = Integer.parseInt(parts[i++]);
        int k = Integer.parseInt(parts[i++]);
        int x = Integer.parseInt(parts[i++]);
        BigInteger primeModulus = new BigInteger(parts[i++]);
        BigInteger share = new BigInteger(parts[i++]);
        if (!piece.equals("" + n + ":" + k + ":" + x + ":" + primeModulus + ":" +
                share))
        {
            throw new RuntimeException("Failed to parse " + piece);
        }
        return new ShareInfo(x, share, new SecretShare.PublicInfo(n,
                k,
                primeModulus,
                null));
    }
    static String mergePiecesIntoSecret(String[] pieces)
            throws UnsupportedEncodingException
    {
        final ShareInfo shareInfo = newShareInfo(pieces[0]);
        final SecretShare.PublicInfo publicInfo = shareInfo.getPublicInfo();
        final SecretShare solver = new SecretShare(publicInfo);
        final int k = publicInfo.getK();
        List<SecretShare.ShareInfo> kPieces = new ArrayList<SecretShare.ShareInfo>(k);
        kPieces.add(shareInfo);

        for (int i = 1; i < pieces.length && i < k; i++)
        {
            kPieces.add(newShareInfo(pieces[i]));
        }
        // EasyLinearEquationTest.enableLogging();
        SecretShare.CombineOutput solved = solver.combine(kPieces);
        BigInteger secret = solved.getSecret();
        return new String(secret.toByteArray(), "UTF-8");
    }
}
