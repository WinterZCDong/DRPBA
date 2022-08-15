package DRPBA;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.ArrayList;
//import java.util.Base64;
import java.util.Base64;
import java.util.HashMap;
//import com.google.gson.GsonBuilder;

import it.unisa.dia.gas.jpbc.Element;
import com.codahale.shamir.Scheme;
import java.security.SecureRandom;
import java.util.Map;


import static DRPBA.InfoEntropy.ProbabilityCalculate;
import static DRPBA.InfoEntropy.calculateInfoEntropy;

public class DRPBA {

    public static ArrayList<Block> blockchain = new ArrayList<Block>();
    public static HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();

    public static int difficulty = 3;
    public static float minimumTransaction = 0.1f;
    public static Wallet walletA;
    public static Wallet walletB;
    public static Transaction genesisTransaction;

    public static void main(String[] args) {
        byte[] bytes = new byte[8];
        bytes[0] = (byte)0xf1;
        bytes[1] = (byte)0x23;
        bytes[2] = (byte)0x37;
        bytes[3] = (byte)0xaf;
        bytes[4] = (byte)0xd5;
        bytes[5] = (byte)0x58;
        bytes[6] = (byte)0xcc;
        bytes[7] = (byte)0xd2;
        int[] pros = ProbabilityCalculate(bytes,2);
        for(int i = 0; i < pros.length; i++){
            System.out.println(i+":"+pros[i]);
        }
        System.out.println("ie: "+calculateInfoEntropy(pros));
        /*
        //add our blocks to the blockchain ArrayList:
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); //Setup Bouncey castle as a Security Provider
        // set G&H
        ChameleonHash ch = new ChameleonHash();
        ChameleonHash.Keys key = ch.keyGenerator();
        //Create wallets:
        walletA = new Wallet(StringUtil.Element2Bytes(key.getG()),StringUtil.Element2Bytes(key.getH()));
        walletB = new Wallet(StringUtil.Element2Bytes(key.getG()),StringUtil.Element2Bytes(key.getH()));
        Wallet coinbase = new Wallet(StringUtil.Element2Bytes(key.getG()),StringUtil.Element2Bytes(key.getH()));

        String str = "Hello world";
        //create genesis transaction, which sends 100 NoobCoin to walletA:
        genesisTransaction = new Transaction(coinbase.publicKey, walletA.publicKey, 100f, null, str.getBytes(StandardCharsets.UTF_8),StringUtil.Element2Bytes(key.getG()),StringUtil.Element2Bytes(key.getH()));
        genesisTransaction.generateSignature(coinbase.privateKey);	 //manually sign the genesis transaction
        genesisTransaction.transactionId = "0"; //manually set the transaction id
        genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.reciepient, genesisTransaction.value, genesisTransaction.transactionId)); //manually add the Transactions Output
        UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0)); //its important to store our first transaction in the UTXOs list.

        System.out.println("Creating and Mining Genesis block... ");
        Block genesis = new Block("0");
        genesis.addTransaction(genesisTransaction);
        addBlock(genesis);

        //testing
        Block block1 = new Block(genesis.hash);
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("\nWalletA is Attempting to send funds (40) to WalletB...");
        String str2 = "nihao";
        block1.addTransaction(walletA.sendFundsWithOPReturn(walletB.publicKey, 40f,str2.getBytes(StandardCharsets.UTF_8)));
        addBlock(block1);
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());

        Block block2 = new Block(block1.hash);
        System.out.println("\nWalletA Attempting to send more funds (1000) than it has...");
        String str3 = "hhhhh I'm zcd";
        block2.addTransaction(walletA.sendFundsWithOPReturn(walletB.publicKey, 1000f, str3.getBytes(StandardCharsets.UTF_8)));
        addBlock(block2);
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());

        Block block3 = new Block(block2.hash);
        System.out.println("\nWalletB is Attempting to send funds (20) to WalletA...");
        String str4 = "I Love XGS";
        block3.addTransaction(walletB.sendFundsWithOPReturn( walletA.publicKey, 20, str4.getBytes(StandardCharsets.UTF_8)));
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());

        isChainValid();
        // 测试不可修改性质
        Transaction t = block1.transactions.get(0);
        System.out.println("original chameleonhash:");
        System.out.println(t.calculateChameleonHash());
        System.out.println("op_return of transaction 0 in block2:");
        System.out.println(new String(t.op_return));
        System.out.println("old r:");
        System.out.println(StringUtil.Bytes2R(t.R));
        String str5 = "haoni";
        t.op_return = str5.getBytes(StandardCharsets.UTF_8);
        isChainValid();
        byte[] privatekey = StringUtil.Element2Bytes(key.getPrivateKey());
        byte[] newR = StringUtil.forge(privatekey, t.R,str2.getBytes(StandardCharsets.UTF_8),str5.getBytes(StandardCharsets.UTF_8));
        ChameleonHash.HashData hd1 = ch.setHashData(str2.getBytes(StandardCharsets.UTF_8),StringUtil.Bytes2R(t.R));
        ChameleonHash.HashData hd2 = ch.forge(key.getPrivateKey(),hd1,str5.getBytes(StandardCharsets.UTF_8));
        System.out.println("chameleon forge:");
        System.out.println(hd2.getR().toBytes());
        System.out.println("StringUtil forge:");
        System.out.println(newR);
        t.R = newR;
        System.out.println("new chameleonHash:");
        System.out.println(t.calculateChameleonHash());
        isChainValid();


        // 测试群签名
        String ss = "Hello World";
        String ss2 = "world hello";
        GroupSig groupSig = new GroupSig();
        groupSig.InitialGroupSig();
        groupSig.generateGPKandGSK();
        GroupSig.SK sk = groupSig.generateSK();
        GroupSig.SK sk2 = groupSig.generateSK();
        GroupSig.GPSig gpsig = groupSig.Sign(sk,ss);
        GroupSig.GPSig gpsig2 = groupSig.Sign(sk2,ss);
        if(groupSig.Verify(gpsig,ss)){
            System.out.println("group signature1 success!");
        }
        else System.out.println("group signature1 fail!");
        if(groupSig.Verify(gpsig2,ss)){
            System.out.println("group signature2 success!");
        }
        else System.out.println("group signature2 fail!");
        System.out.println(groupSig.Open(gpsig2));
        System.out.println(sk2.A);
        // Secret sharing
        System.out.println("Secret Sharing");
        doIt(privatekey);
         */
    }

    public static Boolean isChainValid() {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');
        HashMap<String,TransactionOutput> tempUTXOs = new HashMap<String,TransactionOutput>(); //a temporary working list of unspent transactions at a given block state.
        tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

        //loop through blockchain to check hashes:
        for(int i=1; i < blockchain.size(); i++) {

            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i-1);
            //compare registered hash and calculated hash:
            if(!currentBlock.hash.equals(currentBlock.calculateHash()) ){
                System.out.println("#Current Hashes not equal");
                return false;
            }
            //compare previous hash and registered previous hash
            if(!previousBlock.hash.equals(currentBlock.previousHash) ) {
                System.out.println("#Previous Hashes not equal");
                return false;
            }
            //check if hash is solved
            if(!currentBlock.hash.substring( 0, difficulty).equals(hashTarget)) {
                System.out.println("#This block hasn't been mined");
                return false;
            }

            //loop thru blockchains transactions:
            TransactionOutput tempOutput;
            for(int t=0; t <currentBlock.transactions.size(); t++) {
                Transaction currentTransaction = currentBlock.transactions.get(t);

                if(!currentTransaction.verifySignature()) {
                    System.out.println("#Signature on Transaction(" + t + ") in block"+i+" is Invalid");
                    return false;
                }
                if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
                    System.out.println("#Inputs are note equal to outputs on Transaction(" + t + ")");
                    return false;
                }

                for(TransactionInput input: currentTransaction.inputs) {
                    tempOutput = tempUTXOs.get(input.transactionOutputId);

                    if(tempOutput == null) {
                        System.out.println("#Referenced input on Transaction(" + t + ") is Missing");
                        return false;
                    }

                    if(input.UTXO.value != tempOutput.value) {
                        System.out.println("#Referenced input Transaction(" + t + ") value is Invalid");
                        return false;
                    }

                    tempUTXOs.remove(input.transactionOutputId);
                }

                for(TransactionOutput output: currentTransaction.outputs) {
                    tempUTXOs.put(output.id, output);
                }

                if( currentTransaction.outputs.get(0).reciepient != currentTransaction.reciepient) {
                    System.out.println("#Transaction(" + t + ") output reciepient is not who it should be");
                    return false;
                }
                if( currentTransaction.outputs.get(1).reciepient != currentTransaction.sender) {
                    System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
                    return false;
                }

            }

        }
        System.out.println("Blockchain is valid");
        return true;
    }

    public static void addBlock(Block newBlock) {
        newBlock.mineBlock(difficulty);
        blockchain.add(newBlock);
    }
    public static void doIt(byte[] secret) {
        final Scheme scheme = new Scheme(new SecureRandom(), 255, 255);
        long startInitialize = System.nanoTime();
        final Map<Integer, byte[]> parts = scheme.split(secret);
        final byte[] recovered = scheme.join(parts);
        long endInitialize = System.nanoTime();
        long timeK = endInitialize-startInitialize;
        System.out.println(new String(recovered, StandardCharsets.UTF_8));
        System.out.println("Secret revocer:"+timeK/(1e6)+"ms");
    }
}