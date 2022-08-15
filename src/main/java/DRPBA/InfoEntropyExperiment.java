package DRPBA;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import static DRPBA.DRPBAUtil.*;
import static DRPBA.InfoEntropy.*;
public class InfoEntropyExperiment {
    public static int LEN = 16;
    public static int LEVEL = 8;
    public static void main(String[] args){
        // 读文件，计算信息熵，加密，计算信息熵
        byte[] bytes = new byte[LEN];
        int readLen = 0;
        int cnt = 0;
        double sum0 = 0;
        double sum1 = 0;
        FileInputStream fileInputStream = null;
        FileInputStream fileInputStream2 = null;
        File f = null;
        try{
            fileInputStream = new FileInputStream("D:\\Spring\\Experiment1\\src\\main\\java\\DRPBA\\InfoEntropyTest");
            while(true){
                readLen = fileInputStream.read(bytes);
                if(readLen <= 0) break;
                int[] pros = ProbabilityCalculate(bytes,LEVEL);
                byte[] encroptydata = getEncryptData(bytes);
                //System.out.println(encroptydata.length);
                cnt++;
                sum0 += calculateInfoEntropy(pros);
                //sum1 +=calculateInfoEntropy(pros2);
                System.out.println(calculateInfoEntropy(pros));
            }
            System.out.println("avg0:"+sum0/cnt);
            //System.out.println("avg1:"+sum1/cnt);
            fileInputStream.close();
            f = new File("D:\\Spring\\Experiment1\\src\\main\\java\\DRPBA\\InfoEntropyTest");
            int length = (int)f.length();
            byte[] data = new byte[length];
            new FileInputStream(f).read(data);
            byte[] edata = getEncryptData(data);
            int cnt2 = 0;
            for(int i = 0; i < edata.length; i +=LEN){
                byte[] edata2 = Arrays.copyOfRange(edata, i,i+LEN);
                //System.out.println(edata2);
                int[] pros2 = ProbabilityCalculate(edata2,LEVEL);
                //System.out.println("0:"+edata2[0]+",1:"+edata2[1]+",2:"+edata2[2]+",3:"+edata2[3]);
                //System.out.println(pros2);
                cnt2++;
                sum1 += calculateInfoEntropy(pros2);
                System.out.println(calculateInfoEntropy(pros2));
            }
            System.out.println("avg1:"+sum1/cnt2);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
