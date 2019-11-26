package com.qjx.blockchain.qblockchain.blockprocessing;

public class BlockAlgorithm {

    /**
     * 校验HASH的合法性
     *
     * @param hashcode 待检验的hashcode
     * @param diffculty 挖矿难度
     * @return
     */
    public static boolean isHashValid(String hashcode,Integer diffculty){
        String prefix = repeat("0", diffculty);
        if(hashcode.startsWith(prefix)){
            return true;
        }
        return false;

    }
    private static String repeat(String str, int repeat) {
        final StringBuilder buf = new StringBuilder();
        for (int i = 0; i < repeat; i++) {
            buf.append(str);
        }
        return buf.toString();
    }

    /**
     * 计算挖矿难度按照当前
     */

    private final static Integer Counts =  2100;//币的总数
    private final static Integer unit = 10000;//单位

    private static  double reward =50.0; //最初个数
    private final Integer mintime = 10;//每隔10 分钟 单位分钟
    private final Double decayFrac =  0.5; //衰减比例
    private final Double decayYears =  4.0; //衰减年数

    private Double Calcdecay() {
        Double cc = 1.0;
        Double costs = 0.0;
        while (reward > 1e-7) {
            costs += (60.0 / mintime * 24 * 365   * reward  * decayYears );
            System.out.println(costs);
            reward = reward * 0.5 ;
        }
        System.out.println(costs);
        return costs;

    }

//    private Double getSpendYears(float a,int time){
//        float decayf = (float) (1.0 * decayfrac);
//
//
//    }
//    public Double getnum(){
////        Double a =calcfrac(1.0,0);
//        Double value =  (Counts * unit /1.75) / 4 /365 / 24 /60/mintime ;
//        return value;
//    }
//    public String getbtc(){
//        Double reduce = 1.0;
//        double count =0.0;
//        while(reduce > 0){
//            count += 60 / mintime * 24 * 365 * getnum() * reduce;
//            ReduceCount -=1;
//            if(ReduceCount == 0){
//                reduce *=  0.5;
//                ReduceCount = 4;
//            }
//        }
//        return  String.valueOf(count);
//
//
//    }
    public static void main(String[] args) {


    }
}
