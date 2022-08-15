package DRPBA;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Element;

import java.nio.charset.StandardCharsets;

public class GroupSig {
    public Pairing bp;
    public Field G1;
    public Field G2;
    public Field Zr;
    public GPK gpk;
    public GSK gsk;
    public Element gamma;
    public class GPK{
        public Element g1;
        public Element g2;
        public Element h;
        public Element u;
        public Element v;
        public Element omega;
        public GPK(Element g1,Element g2, Element h, Element u, Element v, Element omega){
            this.g1 = g1;
            this.g2 = g2;
            this.h = h;
            this.u = u;
            this.v = v;
            this.omega = omega;
        }
    }

    public class GSK{
        public Element xi_1;
        public Element xi_2;
        public GSK(Element xi_1, Element xi_2){
            this.xi_1 = xi_1;
            this.xi_2 = xi_2;
        }
    }

    public class SK{
        public Element x;
        public Element A;
        public SK(Element x, Element A){
            this.x = x;
            this.A = A;
        }
    }

    public class GPSig{
        public Element T1;
        public Element T2;
        public Element T3;
        public Element c;
        public Element s_alpha;
        public Element s_beta;
        public Element s_x;
        public Element s_delta1;
        public Element s_delta2;
        public GPSig(Element T1,Element T2,Element T3,Element c,Element s_alpha,Element s_beta,Element s_x,Element s_delta1, Element s_delta2){
            this.T1 = T1;
            this.T2 = T2;
            this.T3 = T3;
            this.c = c;
            this.s_alpha = s_alpha;
            this.s_beta = s_beta;
            this.s_x = s_x;
            this.s_delta1 = s_delta1;
            this.s_delta2 = s_delta2;
        }
    }

    public void InitialGroupSig(){
        bp = PairingFactory.getPairing("f.properties");
        G1 = bp.getG1();
        G2 = bp.getG2();
        Zr = bp.getZr();
    }

    public void generateGPKandGSK(){
        Element g1 = G1.newRandomElement().getImmutable();
        Element g2 = G2.newRandomElement().getImmutable();
        Element h = G1.newRandomElement().getImmutable();
        Element xi_1 = Zr.newRandomElement().getImmutable();
        Element xi_2 = Zr.newRandomElement().getImmutable();
        Element u=h.powZn(xi_1.invert()).getImmutable();
        Element v=h.powZn(xi_2.invert()).getImmutable();
        this.gamma = Zr.newRandomElement().getImmutable();
        Element omega = g2.powZn(gamma).getImmutable();
        this.gpk = new GPK(g1,g2,h,u,v,omega);
        this.gsk = new GSK(xi_1,xi_2);
    }

    public SK generateSK(){
        Element x = Zr.newRandomElement().getImmutable();
        Element gammaAddx = this.gamma.add(x).getImmutable();
        Element A = gpk.g1.powZn(gammaAddx.invert()).getImmutable();
        return new SK(x,A);
    }


    public GPSig Sign(SK sk, String msg){
        Element alpha = Zr.newRandomElement().getImmutable();
        Element beta = Zr.newRandomElement().getImmutable();
        Element T1 = gpk.u.powZn(alpha).getImmutable();
        Element T2 = gpk.v.powZn(beta).getImmutable();
        Element alphaAddBeta = alpha.add(beta);
        Element hPowAlphaAddBeta = gpk.h.powZn(alphaAddBeta);
        Element T3 = sk.A.mul(hPowAlphaAddBeta).getImmutable();

        Element r_alpha = Zr.newRandomElement().getImmutable();
        Element r_beta = Zr.newRandomElement().getImmutable();
        Element r_x = Zr.newRandomElement().getImmutable();
        Element r_delta1 = Zr.newRandomElement().getImmutable();
        Element r_delta2 = Zr.newRandomElement().getImmutable();

        Element R1 = gpk.u.powZn(r_alpha).getImmutable();
        Element R2 = gpk.v.powZn(r_beta).getImmutable();
        Element R3Part1Pair = bp.pairing(T3,gpk.g2).getImmutable();
        Element R3Part2Pair = bp.pairing(gpk.h,gpk.omega).getImmutable();
        Element R3Part3Pair = bp.pairing(gpk.h,gpk.g2).getImmutable();
        Element R3Part1 = R3Part1Pair.powZn(r_x);
        Element fu_r_alpha = r_alpha.negate().getImmutable();
        Element fu_r_beta = r_beta.negate().getImmutable();

        Element R3Part2 = R3Part2Pair.powZn(fu_r_alpha.add(fu_r_beta));
        Element R3Part3 = R3Part3Pair.powZn((r_delta1.negate()).sub(r_delta2));
        Element R3 = ((R3Part1.mul(R3Part2)).mul(R3Part3)).getImmutable();
        Element R4Part1 = T1.powZn(r_x);
        Element R4Part2 = gpk.u.powZn(r_delta1.negate());
        Element R4 = R4Part1.mul(R4Part2).getImmutable();
        Element R5Part1 = T2.powZn(r_x);
        Element R5Part2 = gpk.v.powZn(r_delta2.negate());
        Element R5 = R5Part1.mul(R5Part2).getImmutable();
        String msg_sign = msg;
        msg_sign += T1;
        msg_sign += T2;
        msg_sign += T3;
        msg_sign += R1;
        msg_sign += R2;
        msg_sign += R3;
        msg_sign += R4;
        msg_sign += R5;
        int c_sign=msg_sign.hashCode();
        byte[] c_sign_byte = Integer.toString(c_sign).getBytes(StandardCharsets.UTF_8);
        Element c = (Zr.newElementFromHash(c_sign_byte, 0, c_sign_byte.length)).getImmutable();
        Element delta1 = sk.x.mul(alpha).getImmutable();
        Element delta2 = sk.x.mul(beta).getImmutable();
        Element s_alpha = r_alpha.add(c.mul(alpha)).getImmutable();
        Element s_beta = r_beta.add(c.mul(beta)).getImmutable();
        Element s_x = r_x.add(c.mul(sk.x)).getImmutable();
        Element s_delta1 = r_delta1.add(c.mul(delta1)).getImmutable();
        Element s_delta2 = r_delta2.add(c.mul(delta2)).getImmutable();
        return new GPSig(T1,T2,T3,c,s_alpha,s_beta,s_x,s_delta1,s_delta2);
    }

    public boolean Verify(GPSig sig, String msg){
        Element R1_barPart1 = gpk.u.powZn(sig.s_alpha);
        Element R1_barPart2 = sig.T1.powZn(sig.c.negate());
        Element R1_bar = R1_barPart1.mul(R1_barPart2).getImmutable();
        Element R2_barPart1 = gpk.v.powZn(sig.s_beta);
        Element R2_barPart2 = sig.T2.powZn(sig.c.negate());
        Element R2_bar = R2_barPart1.mul(R2_barPart2).getImmutable();
        Element R3_barPart1Pair = bp.pairing(sig.T3,gpk.g2).getImmutable();
        Element R3_barPart2Pair = bp.pairing(gpk.h,gpk.omega).getImmutable();
        Element R3_barPart3Pair = bp.pairing(gpk.h,gpk.g2).getImmutable();
        Element R3_barPart1 = R3_barPart1Pair.powZn(sig.s_x);
        Element R3_barPart2 = R3_barPart2Pair.powZn((sig.s_alpha.negate()).sub(sig.s_beta));
        Element fu_s_delta1 = sig.s_delta1.negate().getImmutable();
        Element fu_s_delta2 = sig.s_delta2.negate().getImmutable();
        Element R3_barPart3 = R3_barPart3Pair.powZn(fu_s_delta1.add(fu_s_delta2));
        Element e_T3_omega = bp.pairing(sig.T3,gpk.omega).getImmutable();
        Element e_g1_g2 = bp.pairing(gpk.g1,gpk.g2).getImmutable();
        Element R3_barPart4 = (e_T3_omega.div(e_g1_g2)).powZn(sig.c);
        Element R3_bar = R3_barPart1.mul(R3_barPart2).mul(R3_barPart3).mul(R3_barPart4).getImmutable();
        Element R4_barPart1 = sig.T1.powZn(sig.s_x);
        Element R4_barPart2 = gpk.u.powZn(fu_s_delta1);
        Element R4_bar = R4_barPart1.mul(R4_barPart2).getImmutable();
        Element R5_barPart1 = sig.T2.powZn(sig.s_x);
        Element R5_barPart2 = gpk.v.powZn(fu_s_delta2);
        Element R5_bar = R5_barPart1.mul(R5_barPart2).getImmutable();

        String msg_verify = msg;
        msg_verify += sig.T1;
        msg_verify += sig.T2;
        msg_verify += sig.T3;
        msg_verify += R1_bar;
        msg_verify += R2_bar;
        msg_verify += R3_bar;
        msg_verify += R4_bar;
        msg_verify += R5_bar;
        int c_verify = msg_verify.hashCode();
        byte[] c_verify_byte = Integer.toString(c_verify).getBytes(StandardCharsets.UTF_8);
        Element c_ = (Zr.newElementFromHash(c_verify_byte, 0, c_verify_byte.length)).getImmutable();
        return c_.isEqual(sig.c);
    }

    public Element Open(GPSig sig){
        Element A_ = sig.T3.div((sig.T1.powZn(gsk.xi_1)).mul(sig.T2.powZn(gsk.xi_2)));
        return A_;
    }
}
