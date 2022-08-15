package DRPBA;


public class InfoEntropy {
    public static int[] ProbabilityCalculate(byte[] bytes, int level){
        //System.out.println(1<<level);
        int[] pros = new int[1<<level];
        for(int i = 0; i < pros.length; i++) pros[i] = 0;
        for(int i = 0; i < bytes.length; i++){
            int idx = (bytes[i] & 0xff)>>>(8-level);
            //System.out.println(bytes[i]);
            //System.out.println(idx);
            pros[idx]++;
        }
        return pros;
    }

    public static double log2x(double x){
        return Math.log(x) / Math.log(2);
    }

    public static double calculateInfoEntropy(int[] pros){
        double ie = 0;
        int cnt = 0;
        for(int i = 0; i < pros.length; i++){
            cnt += pros[i];
        }
        //System.out.println("cnt:"+cnt);
        for(int i = 0; i < pros.length; i++){
            if(pros[i]==0) continue;
            double pro = pros[i]*1.0/cnt;
            //System.out.println("idx:"+i+",pros:"+pros[i]);
            ie -= pro * log2x(pro);
        }
        //System.out.println("ie:"+ie);
        return ie;
    }
}
