package DRPBA;
import it.unisa.dia.gas.jpbc.*;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

public class ChameleonHash {

    //ChameleonHash Key Pairs
    public class Keys {
        private Element privateKey;

        private Element[] publicKey;

        public Keys(Element privateKey, Element g, Element h){
            this.publicKey = new Element[2];

            this.privateKey = privateKey.getImmutable();
            this.publicKey[0] = g.getImmutable();
            this.publicKey[1] = h.getImmutable();
        }

        public Element getPrivateKey(){
            return this.privateKey;
        }

        public Element getH(){
            return this.publicKey[1];
        }

        public Element getG(){
            return this.publicKey[0];
        }
    }

    //ChameleonHash Data
    public class HashData {
        private Element value;
        private Element r;

        public HashData(Element data, Element r){
            this.value = data;
            this.r = r;
        }

        public Element getValue(){
            return value;
        }

        public Element getR(){
            return r;
        }
    }

    private final Pairing pairing;

    public ChameleonHash(){
        this.pairing = PairingFactory.getPairing("a.properties");
    }

    public Keys keyGenerator() {
        Element g = pairing.getG1().newRandomElement().getImmutable();
        Element privateKey = pairing.getZr().newRandomElement().getImmutable();
        Element h = g.powZn(privateKey).getImmutable();
        return new Keys(privateKey, g, h);
    }

    public HashData setHashData(byte[] message){
        Element value = pairing.getZr().newElement().setFromHash(message, 0, message.length).getImmutable();
        Element r = pairing.getZr().newRandomElement().getImmutable();

        return new HashData(value, r);
    }
    public HashData setHashData(byte[] message, Element r){
        Element value = pairing.getZr().newElement().setFromHash(message, 0, message.length).getImmutable();
        return new HashData(value, r);
    }

    public Element hash(Element g, Element h, HashData hd){
        return  g.powZn(hd.getValue()).mul(h.powZn(hd.getR())).getImmutable();
    }

    public HashData forge(Element sk, HashData hd1, byte[] message){
        System.out.println("old r in chameleon hash:");
        System.out.println(hd1.getR());
        System.out.println("old v in chameleon hash:");
        System.out.println(hd1.getValue());
        System.out.println("new v in Chameleon hash:");

        Element value = pairing.getZr().newElement().setFromHash(message, 0, message.length).getImmutable();
        System.out.println(value);
        Element r_shadow = hd1.getValue().sub(value).add(sk.mulZn(hd1.getR())).mulZn(sk.invert()).getImmutable();
        System.out.println("r_shadow in chameleon hash:");
        System.out.println(r_shadow);
        return new HashData(value, r_shadow);
    }
}