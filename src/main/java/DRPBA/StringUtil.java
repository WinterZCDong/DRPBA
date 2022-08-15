package DRPBA;
import java.security.*;
import java.util.ArrayList;
import java.util.Base64;
import com.google.gson.GsonBuilder;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.util.List;

public class StringUtil {

    //Applies Sha256 to a string and returns the result.
    public static String applySha256(String input){

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            //Applies sha256 to our input,
            byte[] hash = digest.digest(input.getBytes("UTF-8"));

            StringBuffer hexString = new StringBuffer(); // This will contain hash as hexidecimal
            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    //Applies ECDSA Signature and returns the result ( as bytes ).
    public static byte[] applyECDSASig(PrivateKey privateKey, String input) {
        Signature dsa;
        byte[] output = new byte[0];
        try {
            dsa = Signature.getInstance("ECDSA", "BC");
            dsa.initSign(privateKey);
            byte[] strByte = input.getBytes();
            dsa.update(strByte);
            byte[] realSig = dsa.sign();
            output = realSig;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return output;
    }

    //Verifies a String signature
    public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature) {
        try {
            Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(data.getBytes());
            return ecdsaVerify.verify(signature);
        }catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    //Short hand helper to turn Object into a json string
    public static String getJson(Object o) {
        return new GsonBuilder().setPrettyPrinting().create().toJson(o);
    }

    //Returns difficulty string target, to compare to hash. eg difficulty of 5 will return "00000"
    public static String getDificultyString(int difficulty) {
        return new String(new char[difficulty]).replace('\0', '0');
    }

    public static String getStringFromKey(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static String getMerkleRoot(ArrayList<Transaction> transactions) {
        int count = transactions.size();

        List<String> previousTreeLayer = new ArrayList<String>();
        for(Transaction transaction : transactions) {
            previousTreeLayer.add(transaction.transactionId);
        }
        List<String> treeLayer = previousTreeLayer;

        while(count > 1) {
            treeLayer = new ArrayList<String>();
            for(int i=1; i < previousTreeLayer.size(); i+=2) {
                treeLayer.add(applySha256(previousTreeLayer.get(i-1) + previousTreeLayer.get(i)));
            }
            count = treeLayer.size();
            previousTreeLayer = treeLayer;
        }

        String merkleRoot = (treeLayer.size() == 1) ? treeLayer.get(0) : "";
        return merkleRoot;
    }

    public static Element Bytes2Element(byte[] bytes){
        Pairing pairing = PairingFactory.getPairing("a.properties");
        Element ele = pairing.getG1().newElementFromBytes(bytes).getImmutable();
        return ele;
    }

    public static byte[] Element2Bytes(Element ele){
        Pairing pairing = PairingFactory.getPairing("a.properties");
        byte[] bytes = ele.toBytes();
        return bytes;
    }

    public static Element Bytes2R(byte[] bytes){
        Pairing pairing = PairingFactory.getPairing("a.properties");
        Element ele = pairing.getZr().newElementFromBytes(bytes).getImmutable();
        return ele;
    }

    public static byte[] getRandom(){
        Pairing pairing = PairingFactory.getPairing("a.properties");
        Element r = pairing.getZr().newRandomElement().getImmutable();
        byte[] bytes = r.toBytes();
        return bytes;
    }
    public static byte[] forge(byte[] privatekey,byte[] oldr, byte[] oldv, byte[] newv){
        Pairing pairing = PairingFactory.getPairing("a.properties");
        Element value = pairing.getZr().newElement().setFromHash(newv, 0, newv.length).getImmutable();
        Element sk = pairing.getZr().newElementFromBytes(privatekey).getImmutable();
        Element oldR = Bytes2R(oldr);
        System.out.println(oldR);
        Element oldV = pairing.getZr().newElement().setFromHash(oldv, 0,oldv.length).getImmutable();
        System.out.println(oldV);
        System.out.println(value);
        Element r_shadow = oldV.sub(value).add(sk.mulZn(oldR)).mulZn(sk.invert()).getImmutable();
        System.out.println(r_shadow);
        return Element2Bytes(r_shadow);
    }
}