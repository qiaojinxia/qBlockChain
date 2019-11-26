package com.qjx.blockchain.qblockchain.commonutils;

/**
 * Created by caomaoboy 2019-11-20
 **/
import java.util.Scanner;

public class mathUtils {
    public static void main(String[] args) {
        String s1 = new Scanner(System.in).nextLine();
        String s2 = new Scanner(System.in).nextLine();
        //如果s1<s2那么就不运算了，直接返回0
        if(!check(s1,s2)){
            System.out.println(0);
            return;
        }
        String result = chufa(s1, s2);

        System.out.println(result);
    }
    private static boolean check(String s1, String s2) {
        if(s1.length()<s2.length() || (s1.length()==s2.length() && s1.compareTo(s2)<0)){
            return false;
        }
        return true;
    }

    public static String chufa(String s1, String s2) {
        String result="";
        //循环直到s1<s2为止
        while(check(s1,s2)){
            int n=s1.length()-s2.length();
            //判断需要补多少0 ，如果s1的前m位（m=s2.length()）比s2小，那么只要补n-1个0，否则需要补n个0(n=s1和s2的长度差）
            //比如 123/3 ， 3需要补成30，是n-1个,而 450/3 ,3需要补成300，是n个0
            String num_0=s1.substring(0,s2.length()).compareTo(s2)>=0?get0(n):get0(n-1);
            int i=0; //i代表减法执行了几次，也就是当前次的商
            //减法循环
            while(true){
                String m=jian(s1,s2+num_0);
                //如果减成负数了，那么就退出循环
                if(m.startsWith("-"))break;
                //否则 s1重新赋值
                s1=m;
                i++;
            }
            //把商补上位数的0，就是当前次的结果，再用 大数加的 方法和前面的数累加
            result=add(""+i+num_0,result);
        }
        return result;
    }

    //返回 n个0的字符串
    private static String get0(int n) {
        String result="";
        for (int i = 0; i < n; i++) {
            result+='0';
        }
        return result;
    }

    //大数加
    private static String add(String s1, String s2) {
        //保证s1小于等于s2的长度

        if(s1.length()<s2.length()){
            s1=get0(s2.length()-s1.length())+s1;
        }else{
            s2=get0(s1.length()-s2.length())+s2;
        }

        String result="";
        int w=0;
        for (int i = s2.length()-1; i >=0 ; i--) {
            int  c=s2.charAt(i)+s1.charAt(i)-96+w;
            w=c/10;
            result=(c%10) + result;
        }

        if(w==1)result=1+result;

        return result;
    }
    //大数减法
    private static String jian(String s1, String s2) {
        String fuhao = "";
        if (!check(s1,s2)) {
            fuhao = "-";
            String t = s1;
            s1 = s2;
            s2 = t;
        }
        s2=get0(Math.abs(s1.length()-s2.length()))+s2;
        String result="";
        int w=0;
        for (int i = s1.length()-1; i >=0 ; i--) {
            int c=s1.charAt(i)-s2.charAt(i)+w;
            if(c<0){
                c+=10;
                w=-1;
            }else{
                w=0;
            }
            result=c+result;
        }
        result=result.replaceAll("^0+", "");
        return fuhao+result;
    }
}


