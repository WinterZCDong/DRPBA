package DRPBA;
import it.unisa.dia.gas.jpbc.Element;

import java.util.Base64;

public class TestChameleonHash {
    public static final boolean DEBUG = true;

    public static void main(String[] args){


        String strMsg1 = new String("Helloworld12sdfg34 ");
        String strMsg2 = new String("worldhello4sadfasdf213");
        byte[] msg1 = strMsg1.getBytes();
        byte[] msg2 = strMsg2.getBytes();

        ChameleonHash ch = new ChameleonHash();
        ChameleonHash.Keys key = ch.keyGenerator();

        ChameleonHash.HashData hd1 = ch.setHashData(msg1);

        //Test 1: stable
        Element chMsg1Result1 = ch.hash(key.getG(), key.getH(), hd1);
        Element chMsg1Result2 = ch.hash(key.getG(), key.getH(), hd1);
        if (DEBUG && !chMsg1Result1.equals(chMsg1Result2)){
            System.out.println("[Test 1] - The Hash is not Stable...");
            System.out.println("chMsg1Result1 = " + chMsg1Result1);
            System.out.println("chMsg1Result2 = " + chMsg1Result2);
            return;
        }

        //Test 2: forge
        ChameleonHash.HashData hd2 = ch.forge(key.getPrivateKey(), hd1, msg2);
        Element chMsg1Result = ch.hash(key.getG(), key.getH(), hd1);
        Element chMsg2Result = ch.hash(key.getG(), key.getH(), hd2);
        if (DEBUG && !chMsg1Result.equals(chMsg2Result)){
            System.out.println("[Test 2] - The Forge is not Correct...");
            System.out.println("chMsg1Result = " + chMsg1Result);
            System.out.println("chMsg2Result = " + chMsg2Result);

            Element chMsg1Expo1 = hd1.getValue().add(hd1.getR().mul(key.getPrivateKey()));
            Element chMsg1Expo2 = hd2.getValue().add(hd2.getR().mul(key.getPrivateKey()));
            System.out.println("Msg1 + sk r1 = " + chMsg1Expo1);
            System.out.println("Msg2 + sk r2 = " + chMsg1Expo2);
            return;
        }

        //Test 3: Hash Time
        int timeToTest = 1000;
        Timer timer = new Timer();
        timer.setFormat(0, Timer.FORMAT.MICRO_SECOND);
        long hashTime = 0;
        for (int i=0; i<timeToTest; i++){
            timer.start(0);
            ch.hash(key.getG(), key.getH(), hd1);
            hashTime += timer.stop(0);
        }
        hashTime = hashTime / timeToTest;

        //Test 4: Forge Time
        long forgeTime = 0;
        for (int i=0; i<timeToTest; i++){
            timer.start(0);
            ch.forge(key.getPrivateKey(), hd1, msg2);
            forgeTime += timer.stop(0);
        }
        forgeTime = forgeTime / timeToTest;

        //Show Hash and Forge Hash Result
        System.out.println("-------------Chameleon Hash Functions-------------------");

        System.out.println("strMsg1 = " + strMsg1 + "\t ChHash(strMsg1) = " + chMsg1Result);
        System.out.println("strMsg2 = " + strMsg2 + "\t ChHash(strMsg2) = " + chMsg2Result);
        System.out.println("-------------Benchmark (" + timeToTest +" Times Test, us)-------------------");
        System.out.println("Hash Time = " + hashTime + "us, Forge Time = " + forgeTime + "us");
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encodeResult = encoder.encode(chMsg1Result.toBytes());
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < encodeResult.length; i++) {
            String hex = Integer.toHexString(0xff & encodeResult[i]);
            if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        System.out.println("hexString:"+hexString);
    }
}