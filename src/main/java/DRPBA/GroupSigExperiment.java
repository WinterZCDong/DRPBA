package DRPBA;
import org.apache.commons.lang3.*;
public class GroupSigExperiment {
    public static void main(String[] args){
        GroupSig groupSig = new GroupSig();
        groupSig.InitialGroupSig();
        groupSig.generateGPKandGSK();
        long start = new java.util.Date().getTime();
        GroupSig.SK sk = groupSig.generateSK();
        long end = new java.util.Date().getTime();
        long min = 1000;
        long max = 0;
        System.out.println("group member private key generation time:"+(end-start)+"ms");
        for(int i = 10; i < 1000; i++){
            String str = RandomStringUtils.randomAlphanumeric(i);
            long signstart = new java.util.Date().getTime();
            GroupSig.GPSig gpsig = groupSig.Sign(sk,str);
            long signend = new java.util.Date().getTime();
            System.out.println(i+":sign time:"+(signend-signstart)+"ms");
            if(signend-signstart>max) max = signend-signstart;
            if(signend-signstart<min) min = signend-signstart;
        }
        System.out.println("Sign time max:"+max+"ms");
        System.out.println("Sign time min:"+min+"ms");
    }
}
