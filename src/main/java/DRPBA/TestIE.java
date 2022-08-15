package DRPBA;

public class TestIE {
    public static int[] arr= {0,0,0,0};
    public static double log2x(double x) {return Math.log(x) / Math.log(2);}
    public static int cnt = 0;
    public static int cnt2 = 0;
    public static double iespecial = 0.375;
    public static int res = 16;
    public static double calculateIE(){
        double ie = 0;
        for(int i = 0; i < 4; i++){
            if(arr[i] == 0) continue;
            double pro = arr[i]*1.0/16;
            ie += pro * log2x(pro);
            //System.out.println(pro*log2x(pro));
        }
        return -ie;
    }
    public static void search(int k){
        if(k==3){
            if(calculateIE() <= iespecial) cnt++;
            cnt2++;
            return;
        }
        for(int i = 0; i <= res; i++){
            res -= i;
            arr[k] = i;
            search(k+1);
            arr[k] = 0;
            res += i;
        }
    }
    public static void main(String[] args){
        arr[0] = 8;
        arr[1] = 3;
        arr[2] = 3;
        arr[3] = 2;
        System.out.println(calculateIE());
        //search(0);
        //System.out.println(cnt);
        //System.out.println(cnt2);
    }
}
